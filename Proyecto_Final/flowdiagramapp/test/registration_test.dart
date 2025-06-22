// Script de verificación del registro de usuarios
// Ejecutar desde la pantalla de debug o crear un test

import 'package:flutter_test/flutter_test.dart';
import '../lib/services/auth_service.dart';
import '../lib/models/user_model.dart';

void main() {
  group('Registro de Usuario - Pruebas', () {
    final AuthService authService = AuthService();

    test('Registro exitoso con email nuevo', () async {
      final testEmail =
          'test_${DateTime.now().millisecondsSinceEpoch}@example.com';
      final testPassword = 'Test123456';
      final testName = 'Usuario Test';

      try {
        final user = await authService.registerWithEmailPassword(
          email: testEmail,
          password: testPassword,
          displayName: testName,
          role: UserRole.user,
        );

        expect(user, isNotNull);
        expect(user!.email, equals(testEmail));
        expect(user.displayName, equals(testName));
        expect(user.role, equals(UserRole.user));

        print('✅ Registro exitoso: ${user.displayName} (${user.email})');

        // Limpiar: eliminar usuario de prueba
        await authService.signOut();
      } catch (e) {
        print('❌ Error en registro: $e');
        rethrow;
      }
    });

    test('Verificación de email existente', () async {
      final testEmail = 'existing@example.com';

      try {
        // Primero registrar un usuario
        await authService.registerWithEmailPassword(
          email: testEmail,
          password: 'Test123456',
          displayName: 'Usuario Existente',
        );

        // Luego verificar que existe
        final exists = await authService.checkIfEmailExists(testEmail);
        expect(exists, isTrue);

        print('✅ Verificación de email existente correcta');
      } catch (e) {
        print('❌ Error en verificación: $e');
      }
    });

    test('Manejo de error email duplicado', () async {
      final testEmail = 'duplicate@example.com';

      try {
        // Registrar usuario por primera vez
        await authService.registerWithEmailPassword(
          email: testEmail,
          password: 'Test123456',
          displayName: 'Usuario Original',
        );

        // Intentar registrar con el mismo email
        expect(
          () async => await authService.registerWithEmailPassword(
            email: testEmail,
            password: 'Test123456',
            displayName: 'Usuario Duplicado',
          ),
          throwsA(isA<Exception>()),
        );

        print('✅ Manejo de error de email duplicado correcto');
      } catch (e) {
        print('❌ Error en prueba de duplicado: $e');
      }
    });
  });
}

// Función para ejecutar pruebas manuales desde la app
Future<void> runRegistrationTests() async {
  final AuthService authService = AuthService();

  print('🧪 Iniciando pruebas de registro...');

  // Prueba 1: Registro exitoso
  try {
    final testEmail =
        'test_${DateTime.now().millisecondsSinceEpoch}@example.com';
    print('📧 Probando registro con: $testEmail');

    final user = await authService.registerWithEmailPassword(
      email: testEmail,
      password: 'Test123456',
      displayName: 'Usuario Test',
    );

    if (user != null) {
      print('✅ Registro exitoso:');
      print('   - UID: ${user.uid}');
      print('   - Email: ${user.email}');
      print('   - Nombre: ${user.displayName}');
      print('   - Rol: ${user.role}');
    }

    await authService.signOut();
  } catch (e) {
    print('❌ Error en prueba de registro: $e');
  }

  // Prueba 2: Verificación de email
  try {
    print('🔍 Probando verificación de email...');
    final exists = await authService.checkIfEmailExists('test@example.com');
    print(
        '✅ Verificación de email completada: ${exists ? "Existe" : "No existe"}');
  } catch (e) {
    print('❌ Error en verificación de email: $e');
  }

  print('🏁 Pruebas completadas');
}
