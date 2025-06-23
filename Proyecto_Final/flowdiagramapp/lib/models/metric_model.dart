/// Modelo para representar métricas individuales
class MetricModel {
  final String id;
  final String name;
  final double value;
  final String unit;
  final String category;
  final DateTime lastUpdated;
  final Map<String, dynamic> metadata;

  MetricModel({
    required this.id,
    required this.name,
    required this.value,
    required this.unit,
    required this.category,
    required this.lastUpdated,
    this.metadata = const {},
  });

  factory MetricModel.fromMap(Map<String, dynamic> map) {
    return MetricModel(
      id: map['id'] ?? '',
      name: map['name'] ?? '',
      value: (map['value'] ?? 0).toDouble(),
      unit: map['unit'] ?? '',
      category: map['category'] ?? '',
      lastUpdated: DateTime.parse(map['lastUpdated']),
      metadata: Map<String, dynamic>.from(map['metadata'] ?? {}),
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'name': name,
      'value': value,
      'unit': unit,
      'category': category,
      'lastUpdated': lastUpdated.toIso8601String(),
      'metadata': metadata,
    };
  }
}

/// Modelo para estadísticas agregadas
class MetricsSummary {
  final int totalDiagrams;
  final int totalCodeGenerations;
  final int totalValidations;
  final int totalTemplatesUsed;
  final double averageTime;
  final double successRate;
  final DateTime lastActivity;
  final Map<String, dynamic> educationalMetrics;
  final Map<String, dynamic> technicalMetrics;

  MetricsSummary({
    required this.totalDiagrams,
    required this.totalCodeGenerations,
    required this.totalValidations,
    required this.totalTemplatesUsed,
    required this.averageTime,
    required this.successRate,
    required this.lastActivity,
    required this.educationalMetrics,
    required this.technicalMetrics,
  });

  factory MetricsSummary.fromUserMetrics(Map<String, dynamic> metrics) {
    return MetricsSummary(
      totalDiagrams: metrics['diagramas_creados'] ?? 0,
      totalCodeGenerations: metrics['codigo_generado'] ?? 0,
      totalValidations: metrics['total_validaciones'] ?? 0,
      totalTemplatesUsed: metrics['plantillas_usadas'] ?? 0,
      averageTime: (metrics['tiempo_promedio_minutos'] ?? 0.0).toDouble(),
      successRate: (metrics['tasa_exito'] ?? 0.0).toDouble(),
      lastActivity: metrics['ultima_actividad'] != null
          ? DateTime.parse(metrics['ultima_actividad'])
          : DateTime.now(),
      educationalMetrics:
          Map<String, dynamic>.from(metrics['metricas_educativas'] ?? {}),
      technicalMetrics:
          Map<String, dynamic>.from(metrics['metricas_tecnicas'] ?? {}),
    );
  }
}

/// Modelo para métricas globales (solo para administradores)
class GlobalMetrics {
  final int totalUsers;
  final int activeUsers;
  final int totalDiagrams;
  final int totalValidations;
  final double averageUserProgress;
  final Map<String, int> usersByRole;
  final Map<String, double> performanceMetrics;
  final List<Map<String, dynamic>> topUsers;
  final DateTime generatedAt;

  GlobalMetrics({
    required this.totalUsers,
    required this.activeUsers,
    required this.totalDiagrams,
    required this.totalValidations,
    required this.averageUserProgress,
    required this.usersByRole,
    required this.performanceMetrics,
    required this.topUsers,
    required this.generatedAt,
  });
  factory GlobalMetrics.fromMap(Map<String, dynamic> map) {
    return GlobalMetrics(
      totalUsers: map['totalUsers'] ?? 0,
      activeUsers: map['activeUsers'] ?? 0,
      totalDiagrams: map['totalDiagrams'] ?? 0,
      totalValidations: map['totalValidations'] ?? 0,
      averageUserProgress: (map['averageUserProgress'] ?? 0.0).toDouble(),
      usersByRole:
          Map<String, int>.from(map['usersByRole'] ?? {'user': 0, 'admin': 0}),
      performanceMetrics: Map<String, double>.from(map['performanceMetrics'] ??
          {
            'averageSessionTime': 0.0,
            'completionRate': 0.0,
            'errorRate': 0.0,
          }),
      topUsers: List<Map<String, dynamic>>.from(map['topUsers'] ?? []),
      generatedAt: map['generatedAt'] != null
          ? DateTime.parse(map['generatedAt'])
          : DateTime.now(),
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'totalUsers': totalUsers,
      'activeUsers': activeUsers,
      'totalDiagrams': totalDiagrams,
      'totalValidations': totalValidations,
      'averageUserProgress': averageUserProgress,
      'usersByRole': usersByRole,
      'performanceMetrics': performanceMetrics,
      'topUsers': topUsers,
      'generatedAt': generatedAt.toIso8601String(),
    };
  }
}
