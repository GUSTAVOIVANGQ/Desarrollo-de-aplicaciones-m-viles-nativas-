// Script de prueba para funciones de administrador
// Ejecutar desde debug console o crear un test

import '../lib/services/auth_service.dart';
import '../lib/services/metrics_service.dart';

void testAdminFunctions() async {
  print('🧪 Iniciando pruebas de funciones de administrador...');

  final authService = AuthService();
  final metricsService = MetricsService();

  try {
    // Paso 1: Verificar usuario actual
    print('1️⃣ Verificando usuario actual...');
    final currentUser = authService.currentUser;
    print('   Usuario actual: ${currentUser?.email ?? 'No autenticado'}');
    print('   Rol: ${currentUser?.role.name ?? 'Desconocido'}');
    print('   Es admin: ${currentUser?.isAdmin ?? false}');

    // Paso 2: Crear administrador por defecto si no existe
    print('2️⃣ Creando administrador por defecto...');
    await authService.createDefaultAdmin();

    // Paso 3: Intentar obtener métricas globales
    print('3️⃣ Probando acceso a métricas globales...');
    try {
      final globalMetrics = await metricsService.getGlobalMetrics();
      print('   ✅ Métricas globales obtenidas correctamente');
      print('   Total usuarios: ${globalMetrics.totalUsers}');
      print('   Usuarios activos: ${globalMetrics.activeUsers}');
    } catch (e) {
      print('   ❌ Error obteniendo métricas globales: $e');
    }

    // Paso 4: Intentar obtener métricas de usuarios
    print('4️⃣ Probando acceso a métricas de usuarios...');
    try {
      final usersMetrics = await metricsService.getUsersWithMetrics();
      print('   ✅ Métricas de usuarios obtenidas correctamente');
      print('   Total de usuarios con métricas: ${usersMetrics.length}');
    } catch (e) {
      print('   ❌ Error obteniendo métricas de usuarios: $e');
    }

    print('🏁 Pruebas completadas');
  } catch (e) {
    print('❌ Error general: $e');
  }
}

// Función para diagnosticar problemas de null safety
void diagnoseBadNullSafety() async {
  print('🔍 Diagnosticando problemas de null safety...');

  final authService = AuthService();

  try {
    // Verificar estado de autenticación
    final diagnosticData = await authService.diagnoseUserState();
    print('📋 Datos de diagnóstico:');
    print('   Internet: ${diagnosticData['internet']}');
    print(
        '   Auth user: ${diagnosticData['auth_user'] != null ? 'Presente' : 'Ausente'}');
    print(
        '   Firestore user: ${diagnosticData['firestore_user'] != null ? 'Presente' : 'Ausente'}');
    print(
        '   Current user: ${diagnosticData['current_user'] != null ? 'Presente' : 'Ausente'}');
    print(
        '   Cache user: ${diagnosticData['cache_user'] != null ? 'Presente' : 'Ausente'}');

    // Verificar si currentUser es null
    final currentUser = authService.currentUser;
    if (currentUser == null) {
      print(
          '⚠️  Usuario actual es null - esto puede causar errores de null safety');
      print('   Intentando inicializar...');
      await authService.initialize();
      final userAfterInit = authService.currentUser;
      print(
          '   Después de inicializar: ${userAfterInit != null ? 'Usuario presente' : 'Sigue null'}');
    } else {
      print('✅ Usuario actual presente');
      print('   Email: ${currentUser.email}');
      print('   DisplayName: ${currentUser.displayName}');
      print('   Role: ${currentUser.role}');
      print('   IsAdmin: ${currentUser.isAdmin}');
    }
  } catch (e) {
    print('❌ Error en diagnóstico: $e');
  }
}

// Función para limpiar estado problemático
void cleanProblematicState() async {
  print('🧹 Limpiando estado problemático...');

  final authService = AuthService();

  try {
    // Forzar cierre de sesión
    await authService.forceSignOut();
    print('✅ Estado limpiado');

    // Reinicializar
    await authService.initialize();
    print('✅ Estado reinicializado');
  } catch (e) {
    print('❌ Error limpiando estado: $e');
  }
}
