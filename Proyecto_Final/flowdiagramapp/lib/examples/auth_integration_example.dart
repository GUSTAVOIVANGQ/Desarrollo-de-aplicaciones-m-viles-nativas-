import 'package:flutter/material.dart';
import '../services/auth_service.dart';
import '../models/user_model.dart';
import '../screens/login_screen.dart';
import '../screens/profile_screen.dart';

/// Ejemplo de cómo implementar la funcionalidad de autenticación
/// en diferentes partes de la aplicación
class AuthExampleWidget extends StatelessWidget {
  const AuthExampleWidget({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Ejemplo de Autenticación'),
        actions: [
          IconButton(
            icon: const Icon(Icons.person),
            onPressed: () => _navigateToProfile(context),
          ),
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildUserStatus(),
            const SizedBox(height: 20),
            _buildAuthActions(context),
            const SizedBox(height: 20),
            _buildMetricsExample(),
          ],
        ),
      ),
    );
  }

  Widget _buildUserStatus() {
    return FutureBuilder<UserModel?>(
      future: _getCurrentUser(),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return const CircularProgressIndicator();
        }

        final user = snapshot.data;
        if (user == null) {
          return const Card(
            child: Padding(
              padding: EdgeInsets.all(16.0),
              child: Text('No hay usuario autenticado'),
            ),
          );
        }

        return Card(
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Usuario Autenticado',
                  style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 8),
                Text('Nombre: ${user.displayName}'),
                Text('Email: ${user.email}'),
                Text('Rol: ${user.isAdmin ? 'Administrador' : 'Usuario'}'),
                Text('Último acceso: ${user.lastLogin}'),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _buildAuthActions(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Acciones de Autenticación',
          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 12),
        Wrap(
          spacing: 8,
          children: [
            ElevatedButton(
              onPressed: () => _navigateToLogin(context),
              child: const Text('Iniciar Sesión'),
            ),
            ElevatedButton(
              onPressed: () => _signOut(context),
              child: const Text('Cerrar Sesión'),
            ),
            ElevatedButton(
              onPressed: () => _checkAdminAccess(context),
              child: const Text('Verificar Admin'),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildMetricsExample() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Ejemplo de Métricas',
          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 12),
        ElevatedButton(
          onPressed: _updateUserMetrics,
          child: const Text('Actualizar Métricas'),
        ),
        const SizedBox(height: 8),
        const Text(
          'Las métricas se actualizan automáticamente cuando el usuario:\n'
          '• Crea o edita diagramas\n'
          '• Completa validaciones\n'
          '• Genera código\n'
          '• Usa plantillas',
          style: TextStyle(fontSize: 12, color: Colors.grey),
        ),
      ],
    );
  }

  Future<UserModel?> _getCurrentUser() async {
    final authService = AuthService();
    await authService.initialize();
    return authService.currentUser;
  }

  void _navigateToLogin(BuildContext context) {
    Navigator.of(context).push(
      MaterialPageRoute(builder: (context) => const LoginScreen()),
    );
  }

  void _navigateToProfile(BuildContext context) {
    Navigator.of(context).push(
      MaterialPageRoute(builder: (context) => const ProfileScreen()),
    );
  }

  void _signOut(BuildContext context) async {
    final authService = AuthService();
    await authService.signOut();
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Sesión cerrada')),
    );
  }

  void _checkAdminAccess(BuildContext context) {
    final authService = AuthService();
    if (authService.isAdmin) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Acceso de administrador confirmado'),
          backgroundColor: Colors.green,
        ),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('No tienes permisos de administrador'),
          backgroundColor: Colors.orange,
        ),
      );
    }
  }

  void _updateUserMetrics() async {
    final authService = AuthService();
    final user = authService.currentUser;

    if (user != null) {
      // Ejemplo de métricas que se pueden registrar
      final metrics = {
        'diagramas_creados': (user.metrics['diagramas_creados'] ?? 0) + 1,
        'tiempo_uso_minutos': (user.metrics['tiempo_uso_minutos'] ?? 0) + 30,
        'validaciones_exitosas':
            (user.metrics['validaciones_exitosas'] ?? 0) + 1,
        'codigo_generado': (user.metrics['codigo_generado'] ?? 0) + 1,
        'ultima_actividad': DateTime.now().toIso8601String(),
      };

      await authService.updateUserMetrics(user.uid, metrics);
    }
  }
}

/// Ejemplo de cómo usar AuthService en cualquier widget
class AuthIntegrationExample {
  static Future<bool> requireAuth() async {
    final authService = AuthService();
    await authService.initialize();
    return authService.isAuthenticated;
  }

  static Future<bool> requireAdminAuth() async {
    final authService = AuthService();
    await authService.initialize();
    return authService.isAdmin;
  }

  static void trackDiagramCreation() async {
    final authService = AuthService();
    final user = authService.currentUser;

    if (user != null) {
      final metrics = Map<String, dynamic>.from(user.metrics);
      metrics['diagramas_creados'] = (metrics['diagramas_creados'] ?? 0) + 1;
      metrics['ultima_creacion'] = DateTime.now().toIso8601String();

      await authService.updateUserMetrics(user.uid, metrics);
    }
  }

  static void trackCodeGeneration() async {
    final authService = AuthService();
    final user = authService.currentUser;

    if (user != null) {
      final metrics = Map<String, dynamic>.from(user.metrics);
      metrics['codigo_generado'] = (metrics['codigo_generado'] ?? 0) + 1;
      metrics['ultima_generacion'] = DateTime.now().toIso8601String();

      await authService.updateUserMetrics(user.uid, metrics);
    }
  }

  static void trackValidation(bool isSuccessful) async {
    final authService = AuthService();
    final user = authService.currentUser;

    if (user != null) {
      final metrics = Map<String, dynamic>.from(user.metrics);

      if (isSuccessful) {
        metrics['validaciones_exitosas'] =
            (metrics['validaciones_exitosas'] ?? 0) + 1;
      } else {
        metrics['validaciones_fallidas'] =
            (metrics['validaciones_fallidas'] ?? 0) + 1;
      }

      metrics['total_validaciones'] = (metrics['total_validaciones'] ?? 0) + 1;
      metrics['ultima_validacion'] = DateTime.now().toIso8601String();

      await authService.updateUserMetrics(user.uid, metrics);
    }
  }
}
