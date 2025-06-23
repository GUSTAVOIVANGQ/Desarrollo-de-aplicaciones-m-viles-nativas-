import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import '../models/metric_model.dart';
import 'auth_service.dart';

class MetricsService {
  static final MetricsService _instance = MetricsService._internal();
  factory MetricsService() => _instance;
  MetricsService._internal();

  final FirebaseFirestore _firestore = FirebaseFirestore.instance;
  final AuthService _authService = AuthService();

  /// Verifica si hay conexión a internet
  Future<bool> _hasInternetConnection() async {
    final connectivityResult = await Connectivity().checkConnectivity();
    return connectivityResult != ConnectivityResult.none;
  }

  /// Registra una métrica específica para el usuario actual
  Future<void> trackUserAction({
    required String action,
    required String category,
    Map<String, dynamic>? metadata,
  }) async {
    final user = _authService.currentUser;
    if (user == null) return;

    final hasInternet = await _hasInternetConnection();
    final metrics = Map<String, dynamic>.from(user.metrics);

    // Actualizar métricas básicas
    metrics['total_acciones'] = (metrics['total_acciones'] ?? 0) + 1;
    metrics['ultima_actividad'] = DateTime.now().toIso8601String();

    // Actualizar métrica específica por acción
    switch (action) {
      case 'diagrama_creado':
        metrics['diagramas_creados'] = (metrics['diagramas_creados'] ?? 0) + 1;
        break;
      case 'codigo_generado':
        metrics['codigo_generado'] = (metrics['codigo_generado'] ?? 0) + 1;
        break;
      case 'validacion_exitosa':
        metrics['validaciones_exitosas'] =
            (metrics['validaciones_exitosas'] ?? 0) + 1;
        metrics['total_validaciones'] =
            (metrics['total_validaciones'] ?? 0) + 1;
        break;
      case 'validacion_fallida':
        metrics['validaciones_fallidas'] =
            (metrics['validaciones_fallidas'] ?? 0) + 1;
        metrics['total_validaciones'] =
            (metrics['total_validaciones'] ?? 0) + 1;
        break;
      case 'plantilla_usada':
        metrics['plantillas_usadas'] = (metrics['plantillas_usadas'] ?? 0) + 1;
        if (metadata?['template_name'] != null) {
          metrics['ultima_plantilla'] = metadata!['template_name'];
        }
        break;
    }

    // Calcular métricas derivadas
    _calculateDerivedMetrics(metrics);

    // Actualizar en la base de datos y cache
    await _authService.updateUserMetrics(user.uid, metrics);

    // Si hay internet, también actualizar métricas globales
    if (hasInternet) {
      await _updateGlobalMetrics(action, metadata);
    }
  }

  /// Registra métricas educativas específicas
  Future<void> trackEducationalMetric({
    required String exerciseId,
    required Duration timeSpent,
    required bool successful,
    required int errorsFound,
    required int hintsUsed,
    int? confidenceLevel,
  }) async {
    final user = _authService.currentUser;
    if (user == null) return;

    final metrics = Map<String, dynamic>.from(user.metrics);

    // Métricas educativas específicas
    final educationalMetrics =
        Map<String, dynamic>.from(metrics['metricas_educativas'] ?? {});

    educationalMetrics['ejercicios_completados'] =
        (educationalMetrics['ejercicios_completados'] ?? 0) + 1;
    educationalMetrics['tiempo_total_minutos'] =
        (educationalMetrics['tiempo_total_minutos'] ?? 0) + timeSpent.inMinutes;
    educationalMetrics['errores_totales'] =
        (educationalMetrics['errores_totales'] ?? 0) + errorsFound;
    educationalMetrics['pistas_usadas'] =
        (educationalMetrics['pistas_usadas'] ?? 0) + hintsUsed;

    if (successful) {
      educationalMetrics['ejercicios_exitosos'] =
          (educationalMetrics['ejercicios_exitosos'] ?? 0) + 1;
    }

    // Calcular promedios
    final totalEjercicios = educationalMetrics['ejercicios_completados'];
    educationalMetrics['tiempo_promedio_minutos'] =
        educationalMetrics['tiempo_total_minutos'] / totalEjercicios;
    educationalMetrics['tasa_exito'] =
        (educationalMetrics['ejercicios_exitosos'] ?? 0) / totalEjercicios;
    educationalMetrics['promedio_errores'] =
        educationalMetrics['errores_totales'] / totalEjercicios;

    // Registrar nivel de confianza si se proporciona
    if (confidenceLevel != null) {
      final assessments = Map<String, dynamic>.from(
          educationalMetrics['autoevaluaciones'] ?? {});
      assessments[exerciseId] = {
        'nivel_confianza': confidenceLevel,
        'fecha': DateTime.now().toIso8601String(),
        'exitoso': successful,
      };
      educationalMetrics['autoevaluaciones'] = assessments;

      // Calcular confianza promedio
      final allAssessments = assessments.values.cast<Map<String, dynamic>>();
      if (allAssessments.isNotEmpty) {
        final sumConfidence = allAssessments
            .map((a) => a['nivel_confianza'] as int)
            .reduce((a, b) => a + b);
        educationalMetrics['confianza_promedio'] =
            sumConfidence / allAssessments.length;
      }
    }

    metrics['metricas_educativas'] = educationalMetrics;

    await _authService.updateUserMetrics(user.uid, metrics);
  }

  /// Obtiene el resumen de métricas del usuario actual
  Future<MetricsSummary> getUserMetricsSummary() async {
    final user = _authService.currentUser;
    if (user == null) {
      throw Exception('Usuario no autenticado');
    }

    return MetricsSummary.fromUserMetrics(user.metrics);
  }

  /// Obtiene métricas globales (solo para administradores)
  Future<GlobalMetrics> getGlobalMetrics() async {
    final user = _authService.currentUser;
    if (user == null) {
      throw Exception('Usuario no autenticado');
    }

    // Verificar permisos de administrador
    if (!user.isAdmin) {
      throw Exception(
          'Acceso denegado: Solo administradores pueden ver métricas globales');
    }

    final hasInternet = await _hasInternetConnection();
    if (!hasInternet) {
      throw Exception(
          'Se requiere conexión a internet para obtener métricas globales');
    }

    try {
      // Intentar obtener métricas globales existentes
      final doc =
          await _firestore.collection('global_metrics').doc('current').get();

      if (doc.exists) {
        return GlobalMetrics.fromMap(doc.data()!);
      } else {
        // Si no existe, generar métricas globales por primera vez
        return await _generateGlobalMetrics();
      }
    } catch (e) {
      // Si hay error de permisos, crear métricas por defecto
      if (e.toString().contains('permission-denied')) {
        return _createDefaultGlobalMetrics();
      }
      throw Exception('Error obteniendo métricas globales: ${e.toString()}');
    }
  }

  /// Crea métricas globales por defecto cuando no hay acceso a Firestore
  GlobalMetrics _createDefaultGlobalMetrics() {
    return GlobalMetrics(
      totalUsers: 1,
      activeUsers: 1,
      totalDiagrams: 0,
      totalValidations: 0,
      averageUserProgress: 0.0,
      usersByRole: {'user': 1, 'admin': 0},
      performanceMetrics: {
        'averageSessionTime': 0.0,
        'completionRate': 0.0,
        'errorRate': 0.0,
      },
      topUsers: [],
      generatedAt: DateTime.now(),
    );
  }

  /// Obtiene la lista de usuarios con sus métricas (solo para administradores)
  Future<List<Map<String, dynamic>>> getUsersWithMetrics() async {
    final user = _authService.currentUser;
    if (user == null) {
      throw Exception('Usuario no autenticado');
    }

    if (!user.isAdmin) {
      throw Exception(
          'Acceso denegado: Solo administradores pueden ver métricas de usuarios');
    }

    final hasInternet = await _hasInternetConnection();
    if (!hasInternet) {
      throw Exception(
          'Se requiere conexión a internet para obtener métricas de usuarios');
    }

    try {
      final users = await _authService.getAllUsers();

      return users.map((user) {
        final summary = MetricsSummary.fromUserMetrics(user.metrics);
        return {
          'user': user,
          'summary': summary,
          'metrics': user.metrics,
        };
      }).toList();
    } catch (e) {
      // Si hay error de permisos, retornar lista con solo el usuario actual
      if (e.toString().contains('permission-denied')) {
        final currentUser = _authService.currentUser!;
        final summary = MetricsSummary.fromUserMetrics(currentUser.metrics);
        return [
          {
            'user': currentUser,
            'summary': summary,
            'metrics': currentUser.metrics,
          }
        ];
      }
      throw Exception('Error obteniendo métricas de usuarios: ${e.toString()}');
    }
  }

  /// Calcula métricas derivadas a partir de las métricas básicas
  void _calculateDerivedMetrics(Map<String, dynamic> metrics) {
    // Calcular tasa de éxito en validaciones
    final totalValidaciones = metrics['total_validaciones'] ?? 0;
    if (totalValidaciones > 0) {
      final validacionesExitosas = metrics['validaciones_exitosas'] ?? 0;
      metrics['tasa_exito_validaciones'] =
          validacionesExitosas / totalValidaciones;
    }

    // Calcular productividad (acciones por día desde el registro)
    final totalAcciones = metrics['total_acciones'] ?? 0;
    final user = _authService.currentUser;
    if (user != null && totalAcciones > 0) {
      final daysSinceRegistration =
          DateTime.now().difference(user.createdAt).inDays + 1;
      metrics['productividad_diaria'] = totalAcciones / daysSinceRegistration;
    }
  }

  /// Actualiza las métricas globales agregadas
  Future<void> _updateGlobalMetrics(
      String action, Map<String, dynamic>? metadata) async {
    try {
      final globalRef = _firestore.collection('global_metrics').doc('current');

      await _firestore.runTransaction((transaction) async {
        final doc = await transaction.get(globalRef);

        Map<String, dynamic> data;
        if (doc.exists) {
          data = Map<String, dynamic>.from(doc.data()!);
        } else {
          data = {
            'totalUsers': 0,
            'activeUsers': 0,
            'totalDiagrams': 0,
            'totalValidations': 0,
            'totalCodeGenerations': 0,
            'totalTemplatesUsed': 0,
            'generatedAt': DateTime.now().toIso8601String(),
          };
        }

        // Actualizar según la acción
        switch (action) {
          case 'diagrama_creado':
            data['totalDiagrams'] = (data['totalDiagrams'] ?? 0) + 1;
            break;
          case 'codigo_generado':
            data['totalCodeGenerations'] =
                (data['totalCodeGenerations'] ?? 0) + 1;
            break;
          case 'validacion_exitosa':
          case 'validacion_fallida':
            data['totalValidations'] = (data['totalValidations'] ?? 0) + 1;
            break;
          case 'plantilla_usada':
            data['totalTemplatesUsed'] = (data['totalTemplatesUsed'] ?? 0) + 1;
            break;
        }

        data['lastUpdated'] = DateTime.now().toIso8601String();

        transaction.set(globalRef, data, SetOptions(merge: true));
      });
    } catch (e) {
      print('Error actualizando métricas globales: $e');
    }
  }

  /// Genera métricas globales desde cero
  Future<GlobalMetrics> _generateGlobalMetrics() async {
    try {
      final users = await _authService.getAllUsers();

      int totalDiagrams = 0;
      int totalValidations = 0;
      int activeUsers = 0;
      double totalProgress = 0;
      Map<String, int> usersByRole = {'user': 0, 'admin': 0};

      final now = DateTime.now();
      final oneWeekAgo = now.subtract(const Duration(days: 7));

      for (final user in users) {
        // Contar por rol
        usersByRole[user.role.name] = (usersByRole[user.role.name] ?? 0) + 1;

        // Verificar usuarios activos (última actividad en la última semana)
        if (user.lastLogin.isAfter(oneWeekAgo)) {
          activeUsers++;
        }

        // Sumar métricas
        totalDiagrams += (user.metrics['diagramas_creados'] ?? 0) as int;
        totalValidations += (user.metrics['total_validaciones'] ?? 0) as int;

        // Calcular progreso del usuario (basado en múltiples métricas)
        final userProgress = _calculateUserProgress(user.metrics);
        totalProgress += userProgress;
      }

      final averageProgress =
          users.isNotEmpty ? totalProgress / users.length : 0.0;

      // Top usuarios por productividad
      final topUsers = users
          .map((user) => {
                'uid': user.uid,
                'displayName': user.displayName,
                'diagramas': user.metrics['diagramas_creados'] ?? 0,
                'validaciones': user.metrics['total_validaciones'] ?? 0,
                'progreso': _calculateUserProgress(user.metrics),
              })
          .toList()
        ..sort((a, b) =>
            (b['progreso'] as double).compareTo(a['progreso'] as double))
        ..take(10).toList();

      final globalMetrics = GlobalMetrics(
        totalUsers: users.length,
        activeUsers: activeUsers,
        totalDiagrams: totalDiagrams,
        totalValidations: totalValidations,
        averageUserProgress: averageProgress,
        usersByRole: usersByRole,
        performanceMetrics: {
          'diagrams_per_user':
              users.isNotEmpty ? totalDiagrams / users.length : 0.0,
          'validations_per_user':
              users.isNotEmpty ? totalValidations / users.length : 0.0,
          'activity_rate': users.isNotEmpty ? activeUsers / users.length : 0.0,
        },
        topUsers: topUsers,
        generatedAt: now,
      );

      // Guardar en Firestore
      await _firestore
          .collection('global_metrics')
          .doc('current')
          .set(globalMetrics.toMap());

      return globalMetrics;
    } catch (e) {
      throw Exception('Error generando métricas globales: ${e.toString()}');
    }
  }

  /// Calcula el progreso general de un usuario basado en sus métricas
  double _calculateUserProgress(Map<String, dynamic> metrics) {
    // Algoritmo simple de progreso basado en múltiples factores
    final diagramas = (metrics['diagramas_creados'] ?? 0) as int;
    final validaciones = (metrics['validaciones_exitosas'] ?? 0) as int;
    final plantillas = (metrics['plantillas_usadas'] ?? 0) as int;
    final tasaExito = (metrics['tasa_exito_validaciones'] ?? 0.0) as double;

    // Puntuación ponderada
    double score = 0;
    score += diagramas * 10; // 10 puntos por diagrama
    score += validaciones * 5; // 5 puntos por validación exitosa
    score += plantillas * 3; // 3 puntos por plantilla usada
    score += tasaExito * 100; // Hasta 100 puntos por tasa de éxito

    return score;
  }
}
