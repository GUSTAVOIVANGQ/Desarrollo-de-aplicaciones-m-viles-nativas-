import 'package:flutter/material.dart';
import '../services/auth_service.dart';

class DebugScreen extends StatefulWidget {
  const DebugScreen({super.key});

  @override
  State<DebugScreen> createState() => _DebugScreenState();
}

class _DebugScreenState extends State<DebugScreen> {
  final AuthService _authService = AuthService();
  Map<String, dynamic>? _diagnosticData;
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _runDiagnostic();
  }

  Future<void> _runDiagnostic() async {
    setState(() {
      _isLoading = true;
    });

    try {
      final data = await _authService.diagnoseUserState();
      setState(() {
        _diagnosticData = data;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _diagnosticData = {'error': e.toString()};
        _isLoading = false;
      });
    }
  }

  Future<void> _forceSignOut() async {
    setState(() {
      _isLoading = true;
    });

    try {
      await _authService.forceSignOut();
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Sesión cerrada completamente'),
          backgroundColor: Colors.green,
        ),
      );
      await _runDiagnostic();
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Error: ${e.toString()}'),
          backgroundColor: Colors.red,
        ),
      );
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  Future<void> _syncUser() async {
    setState(() {
      _isLoading = true;
    });

    try {
      await _authService.syncAuthUserWithFirestore();
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Usuario sincronizado exitosamente'),
          backgroundColor: Colors.green,
        ),
      );
      await _runDiagnostic();
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Error: ${e.toString()}'),
          backgroundColor: Colors.red,
        ),
      );
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  Future<void> _deleteCurrentUser() async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('PELIGRO'),
        content: const Text(
            '¿Estás seguro de que deseas ELIMINAR COMPLETAMENTE el usuario actual?\n\n'
            'Esta acción NO SE PUEDE DESHACER y eliminará:\n'
            '• El usuario de Firebase Authentication\n'
            '• Los datos del usuario en Firestore\n'
            '• El cache local\n\n'
            'Solo usar en caso de problemas graves.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('Cancelar'),
          ),
          TextButton(
            onPressed: () => Navigator.of(context).pop(true),
            style: TextButton.styleFrom(foregroundColor: Colors.red),
            child: const Text('ELIMINAR'),
          ),
        ],
      ),
    );

    if (confirmed != true) return;

    setState(() {
      _isLoading = true;
    });

    try {
      await _authService.deleteCurrentAuthUser();
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Usuario eliminado completamente'),
          backgroundColor: Colors.orange,
        ),
      );
      await _runDiagnostic();
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Error: ${e.toString()}'),
          backgroundColor: Colors.red,
        ),
      );
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  Future<void> _deleteUserByEmail() async {
    final emailController = TextEditingController();
    final passwordController = TextEditingController();

    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Eliminar Usuario por Email'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text(
                'Introduce las credenciales del usuario que quieres eliminar:\n\n'
                '⚠️ Esta acción eliminará completamente el usuario.'),
            const SizedBox(height: 16),
            TextField(
              controller: emailController,
              decoration: const InputDecoration(
                labelText: 'Email',
                border: OutlineInputBorder(),
              ),
              keyboardType: TextInputType.emailAddress,
            ),
            const SizedBox(height: 16),
            TextField(
              controller: passwordController,
              decoration: const InputDecoration(
                labelText: 'Contraseña',
                border: OutlineInputBorder(),
              ),
              obscureText: true,
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('Cancelar'),
          ),
          TextButton(
            onPressed: () => Navigator.of(context).pop(true),
            style: TextButton.styleFrom(foregroundColor: Colors.red),
            child: const Text('ELIMINAR'),
          ),
        ],
      ),
    );

    if (confirmed != true) return;

    if (emailController.text.isEmpty || passwordController.text.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Por favor completa ambos campos'),
          backgroundColor: Colors.orange,
        ),
      );
      return;
    }

    setState(() {
      _isLoading = true;
    });

    try {
      await _authService.deleteUserByEmail(
        emailController.text.trim(),
        passwordController.text,
      );
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content:
              Text('Usuario ${emailController.text} eliminado exitosamente'),
          backgroundColor: Colors.green,
        ),
      );
      await _runDiagnostic();
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Error: ${e.toString()}'),
          backgroundColor: Colors.red,
        ),
      );
    } finally {
      setState(() {
        _isLoading = false;
      });
      emailController.dispose();
      passwordController.dispose();
    }
  }

  Future<void> _checkEmailExists() async {
    final emailController = TextEditingController();

    final result = await showDialog<String>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Verificar Email'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text('Introduce el email que quieres verificar:'),
            const SizedBox(height: 16),
            TextField(
              controller: emailController,
              decoration: const InputDecoration(
                labelText: 'Email',
                border: OutlineInputBorder(),
              ),
              keyboardType: TextInputType.emailAddress,
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(null),
            child: const Text('Cancelar'),
          ),
          TextButton(
            onPressed: () => Navigator.of(context).pop(emailController.text),
            child: const Text('Verificar'),
          ),
        ],
      ),
    );

    if (result == null || result.isEmpty) return;

    setState(() {
      _isLoading = true;
    });

    try {
      final exists = await _authService.checkIfEmailExists(result.trim());
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(exists
              ? '✅ El email $result SÍ está registrado'
              : '❌ El email $result NO está registrado'),
          backgroundColor: exists ? Colors.orange : Colors.green,
        ),
      );
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Error al verificar: ${e.toString()}'),
          backgroundColor: Colors.red,
        ),
      );
    } finally {
      setState(() {
        _isLoading = false;
      });
      emailController.dispose();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Debug Firebase'),
        backgroundColor: Colors.orange,
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Diagnóstico de Firebase Authentication',
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 20),
            Row(
              children: [
                Expanded(
                  child: ElevatedButton(
                    onPressed: _isLoading ? null : _runDiagnostic,
                    child: const Text('Actualizar Diagnóstico'),
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: ElevatedButton(
                    onPressed: _isLoading ? null : _forceSignOut,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.red,
                      foregroundColor: Colors.white,
                    ),
                    child: const Text('Cerrar Sesión Completa'),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 10),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: _isLoading ? null : _syncUser,
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.blue,
                  foregroundColor: Colors.white,
                ),
                child: const Text('Sincronizar Usuario'),
              ),
            ),
            const SizedBox(height: 10),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: _isLoading ? null : _deleteCurrentUser,
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.red[700],
                  foregroundColor: Colors.white,
                ),
                child: const Text('🗑️ ELIMINAR Usuario Actual (PELIGRO)'),
              ),
            ),
            const SizedBox(height: 10),
            Row(
              children: [
                Expanded(
                  child: ElevatedButton(
                    onPressed: _isLoading ? null : _deleteUserByEmail,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.red[600],
                      foregroundColor: Colors.white,
                    ),
                    child: const Text('🗑️ Eliminar por Email'),
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: ElevatedButton(
                    onPressed: _isLoading ? null : _checkEmailExists,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.purple,
                      foregroundColor: Colors.white,
                    ),
                    child: const Text('🔍 Verificar Email'),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 20),
            if (_isLoading)
              const Center(child: CircularProgressIndicator())
            else if (_diagnosticData != null)
              Expanded(
                child: SingleChildScrollView(
                  child: Container(
                    width: double.infinity,
                    padding: const EdgeInsets.all(16),
                    decoration: BoxDecoration(
                      color: Colors.grey[100],
                      borderRadius: BorderRadius.circular(8),
                      border: Border.all(color: Colors.grey[300]!),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          'Estado Actual:',
                          style: TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        const SizedBox(height: 10),
                        Text(
                          _formatDiagnosticData(_diagnosticData!),
                          style: const TextStyle(
                            fontFamily: 'monospace',
                            fontSize: 12,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }

  String _formatDiagnosticData(Map<String, dynamic> data) {
    final buffer = StringBuffer();

    buffer.writeln('=== DIAGNÓSTICO FIREBASE ===\n');

    buffer.writeln('Internet: ${data['internet'] ?? 'Desconocido'}');

    if (data.containsKey('error')) {
      buffer.writeln('ERROR: ${data['error']}');
      return buffer.toString();
    }
    buffer.writeln('\n--- Firebase Authentication ---');
    final authUser = data['auth_user'];
    if (authUser != null) {
      buffer.writeln('Usuario autenticado: SÍ');
      buffer.writeln('  - UID: ${authUser['uid']}');
      buffer.writeln('  - Email: ${authUser['email']}');
      final authDisplayName = authUser['displayName'];
      if (authDisplayName == null || authDisplayName.toString().isEmpty) {
        buffer.writeln(
            '  - Nombre: null (⚠️ Normal - Firebase Auth a veces no mantiene displayName)');
      } else {
        buffer.writeln('  - Nombre: $authDisplayName');
      }
    } else {
      buffer.writeln('Usuario autenticado: NO');
    }

    buffer.writeln('\n--- Firestore Database ---');
    final firestoreUser = data['firestore_user'];
    if (firestoreUser != null) {
      buffer.writeln('Usuario en Firestore: SÍ');
      buffer.writeln('  - UID: ${firestoreUser['uid']}');
      buffer.writeln('  - Email: ${firestoreUser['email']}');
      buffer.writeln('  - Nombre: ${firestoreUser['displayName']}');
      buffer.writeln('  - Rol: ${firestoreUser['role']}');
      buffer.writeln('  - Creado: ${firestoreUser['createdAt']}');
      buffer.writeln('  - Último login: ${firestoreUser['lastLogin']}');
    } else {
      buffer.writeln('Usuario en Firestore: NO');
    }

    buffer.writeln('\n--- Usuario Actual (App) ---');
    final currentUser = data['current_user'];
    if (currentUser != null) {
      buffer.writeln('Usuario actual cargado: SÍ');
      buffer.writeln('  - Email: ${currentUser['email']}');
      buffer.writeln('  - Nombre: ${currentUser['displayName']}');
    } else {
      buffer.writeln('Usuario actual cargado: NO');
    }

    buffer.writeln('\n--- Cache Local ---');
    final cacheUser = data['cache_user'];
    if (cacheUser != null) {
      buffer.writeln('Usuario en cache: SÍ');
      buffer.writeln('  - Email: ${cacheUser['email']}');
      buffer.writeln('  - Nombre: ${cacheUser['displayName']}');
    } else {
      buffer.writeln('Usuario en cache: NO');
    }

    // Análisis de problemas
    buffer.writeln('\n=== ANÁLISIS ===');
    if (authUser != null && firestoreUser == null) {
      buffer.writeln(
          '⚠️  PROBLEMA: Usuario existe en Authentication pero NO en Firestore');
      buffer.writeln('   Solución: Usar "Sincronizar Usuario"');
    } else if (authUser == null && firestoreUser != null) {
      buffer.writeln(
          '⚠️  PROBLEMA: Usuario existe en Firestore pero NO en Authentication');
      buffer.writeln('   Esto no debería ocurrir normalmente');
    } else if (authUser != null && firestoreUser != null) {
      buffer.writeln(
          '✅ OK: Usuario sincronizado entre Authentication y Firestore');
    } else {
      buffer.writeln('✅ OK: No hay usuarios registrados');
    }

    return buffer.toString();
  }
}
