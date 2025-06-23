import 'package:firebase_auth/firebase_auth.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import '../models/user_model.dart';
import 'dart:convert';

class AuthService {
  static final AuthService _instance = AuthService._internal();
  factory AuthService() => _instance;
  AuthService._internal();

  final FirebaseAuth _auth = FirebaseAuth.instance;
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;
  UserModel? _currentUser;

  // Stream del estado de autenticación
  Stream<User?> get authStateChanges => _auth.authStateChanges();

  // Usuario actual
  UserModel? get currentUser => _currentUser;

  // Verificar conexión a internet
  Future<bool> _hasInternetConnection() async {
    final connectivityResult = await Connectivity().checkConnectivity();
    return connectivityResult != ConnectivityResult.none;
  }

  // Guardar usuario en cache local
  Future<void> _saveUserToCache(UserModel user) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('cached_user', jsonEncode(user.toMap()));
  }

  // Cargar usuario desde cache local
  Future<UserModel?> _loadUserFromCache() async {
    final prefs = await SharedPreferences.getInstance();
    final cachedUserString = prefs.getString('cached_user');
    if (cachedUserString != null) {
      final userMap = jsonDecode(cachedUserString) as Map<String, dynamic>;
      return UserModel.fromMap(userMap);
    }
    return null;
  }

  // Limpiar cache de usuario
  Future<void> _clearUserCache() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('cached_user');
  }

  // Inicializar el servicio de autenticación
  Future<UserModel?> initialize() async {
    final hasInternet = await _hasInternetConnection();

    if (hasInternet) {
      // Si hay internet, verificar usuario autenticado en Firebase
      final firebaseUser = _auth.currentUser;
      if (firebaseUser != null) {
        try {
          _currentUser = await _getUserFromFirestore(firebaseUser.uid);
          if (_currentUser != null) {
            await _saveUserToCache(_currentUser!);
            // Actualizar último login
            await _updateLastLogin(_currentUser!.uid);
          }
        } catch (e) {
          // Si hay error con Firestore, usar cache local
          _currentUser = await _loadUserFromCache();
        }
      }
    } else {
      // Sin internet, usar cache local
      _currentUser = await _loadUserFromCache();
    }

    return _currentUser;
  }

  // Registro de usuario
  Future<UserModel?> registerWithEmailPassword({
    required String email,
    required String password,
    required String displayName,
    UserRole role = UserRole.user,
  }) async {
    final hasInternet = await _hasInternetConnection();
    if (!hasInternet) {
      throw Exception(
          'Se requiere conexión a internet para registrar un nuevo usuario');
    }

    try {
      // Primero verificar si el email ya existe en Firestore
      final emailExists = await checkIfEmailExists(email);
      if (emailExists) {
        throw Exception(
            'El email ya está registrado. Intenta iniciar sesión en su lugar.');
      }

      // Crear usuario en Firebase Authentication
      final userCredential = await _auth.createUserWithEmailAndPassword(
        email: email,
        password: password,
      );

      final user = userCredential.user;
      if (user == null) throw Exception('Error al crear usuario');

      // Actualizar el perfil del usuario de forma más robusta
      try {
        await user.updateDisplayName(displayName);
        // Forzar recarga para asegurar que los cambios se apliquen
        await user.reload();
      } catch (e) {
        print(
            'Advertencia: No se pudo actualizar el displayName en Firebase Auth: $e');
        // Continuamos ya que el nombre se guardará en Firestore de todas formas
      }

      // Crear el documento del usuario en Firestore
      final now = DateTime.now();
      final userModel = UserModel(
        uid: user.uid,
        email: email,
        displayName: displayName, // Usar el displayName que nos pasaron
        role: role,
        createdAt: now,
        lastLogin: now,
      );

      await _firestore.collection('users').doc(user.uid).set(userModel.toMap());

      _currentUser = userModel;
      await _saveUserToCache(userModel);

      return userModel;
    } on FirebaseAuthException catch (e) {
      String errorMessage;
      switch (e.code) {
        case 'email-already-in-use':
          errorMessage =
              'El email ya está registrado. Intenta iniciar sesión en su lugar.';
          break;
        case 'invalid-email':
          errorMessage = 'El formato del email no es válido.';
          break;
        case 'weak-password':
          errorMessage =
              'La contraseña es muy débil. Debe tener al menos 6 caracteres.';
          break;
        case 'operation-not-allowed':
          errorMessage = 'El registro con email/contraseña no está habilitado.';
          break;
        default:
          errorMessage = 'Error al registrar usuario: ${e.message}';
      }
      throw Exception(errorMessage);
    } catch (e) {
      if (e.toString().contains('Exception:')) {
        rethrow; // Re-lanzar excepciones personalizadas
      }
      throw Exception('Error al registrar usuario: ${e.toString()}');
    }
  }

  // Inicio de sesión
  Future<UserModel?> signInWithEmailPassword({
    required String email,
    required String password,
  }) async {
    final hasInternet = await _hasInternetConnection();

    if (hasInternet) {
      try {
        final userCredential = await _auth.signInWithEmailAndPassword(
          email: email,
          password: password,
        );

        final user = userCredential.user;
        if (user == null) throw Exception('Error al iniciar sesión');

        _currentUser = await _getUserFromFirestore(user.uid);
        if (_currentUser != null) {
          await _saveUserToCache(_currentUser!);
          await _updateLastLogin(_currentUser!.uid);
        }

        return _currentUser;
      } catch (e) {
        throw Exception('Error al iniciar sesión: ${e.toString()}');
      }
    } else {
      // Modo offline: verificar con cache local
      final cachedUser = await _loadUserFromCache();
      if (cachedUser != null && cachedUser.email == email) {
        _currentUser = cachedUser;
        return _currentUser;
      } else {
        throw Exception(
            'Sin conexión a internet. No se puede verificar las credenciales.');
      }
    }
  }

  // Cerrar sesión
  Future<void> signOut() async {
    final hasInternet = await _hasInternetConnection();

    if (hasInternet) {
      await _auth.signOut();
    }

    _currentUser = null;
    await _clearUserCache();
  }

  // Obtener usuario desde Firestore
  Future<UserModel?> _getUserFromFirestore(String uid) async {
    try {
      final doc = await _firestore.collection('users').doc(uid).get();
      if (doc.exists) {
        return UserModel.fromSnapshot(doc);
      }
    } catch (e) {
      print('Error obteniendo usuario de Firestore: $e');
    }
    return null;
  }

  // Actualizar último login
  Future<void> _updateLastLogin(String uid) async {
    try {
      await _firestore.collection('users').doc(uid).update({
        'lastLogin': DateTime.now().toIso8601String(),
      });
    } catch (e) {
      print('Error actualizando último login: $e');
    }
  }

  // Obtener todos los usuarios (solo para administradores)
  Future<List<UserModel>> getAllUsers() async {
    if (_currentUser?.role != UserRole.admin) {
      throw Exception(
          'Acceso denegado: Solo administradores pueden ver todos los usuarios');
    }

    final hasInternet = await _hasInternetConnection();
    if (!hasInternet) {
      throw Exception(
          'Se requiere conexión a internet para obtener la lista de usuarios');
    }

    try {
      final querySnapshot = await _firestore.collection('users').get();
      return querySnapshot.docs
          .map((doc) => UserModel.fromSnapshot(doc))
          .toList();
    } catch (e) {
      throw Exception('Error obteniendo usuarios: ${e.toString()}');
    }
  }

  // Actualizar métricas del usuario
  Future<void> updateUserMetrics(
      String uid, Map<String, dynamic> metrics) async {
    final hasInternet = await _hasInternetConnection();

    if (hasInternet && _currentUser != null) {
      try {
        await _firestore.collection('users').doc(uid).update({
          'metrics': metrics,
        });

        // Actualizar usuario en cache
        _currentUser = _currentUser!.copyWith(metrics: metrics);
        await _saveUserToCache(_currentUser!);
      } catch (e) {
        print('Error actualizando métricas: $e');
        // En caso de error, actualizar solo en cache local
        _currentUser = _currentUser!.copyWith(metrics: metrics);
        await _saveUserToCache(_currentUser!);
      }
    } else {
      // Sin internet, actualizar solo en cache local
      if (_currentUser != null) {
        _currentUser = _currentUser!.copyWith(metrics: metrics);
        await _saveUserToCache(_currentUser!);
      }
    }
  }

  // Verificar si el usuario actual es administrador
  bool get isAdmin => _currentUser?.isAdmin ?? false;

  // Promover un usuario a administrador (solo para desarrollo/configuración inicial)
  Future<void> promoteToAdmin(String email) async {
    final hasInternet = await _hasInternetConnection();
    if (!hasInternet) {
      throw Exception('Se requiere conexión a internet para promover usuarios');
    }

    try {
      // Buscar usuario por email
      final querySnapshot = await _firestore
          .collection('users')
          .where('email', isEqualTo: email)
          .limit(1)
          .get();

      if (querySnapshot.docs.isNotEmpty) {
        final userDoc = querySnapshot.docs.first;
        await userDoc.reference.update({'role': 'admin'});

        // Si es el usuario actual, actualizar en memoria
        if (_currentUser?.email == email) {
          _currentUser = _currentUser!.copyWith(role: UserRole.admin);
          await _saveUserToCache(_currentUser!);
        }

        print('✅ Usuario $email promovido a administrador');
      } else {
        throw Exception('Usuario con email $email no encontrado');
      }
    } catch (e) {
      throw Exception('Error promoviendo usuario: ${e.toString()}');
    }
  }

  // Crear usuario administrador por defecto
  Future<void> createDefaultAdmin() async {
    const adminEmail = 'admin@flowdiagram.com';
    const adminPassword = 'Admin123456';
    const adminName = 'Administrador';

    try {
      // Verificar si ya existe
      final exists = await checkIfEmailExists(adminEmail);
      if (exists) {
        print('ℹ️ Usuario administrador ya existe');
        return;
      }

      // Crear usuario administrador
      await registerWithEmailPassword(
        email: adminEmail,
        password: adminPassword,
        displayName: adminName,
        role: UserRole.admin,
      );

      print('✅ Usuario administrador creado: $adminEmail');
      print('🔑 Contraseña: $adminPassword');
    } catch (e) {
      print('❌ Error creando administrador: $e');
    }
  }

  // MÉTODOS DE DIAGNÓSTICO Y LIMPIEZA

  // Verificar el estado actual de Authentication vs Firestore
  Future<Map<String, dynamic>> diagnoseUserState() async {
    final hasInternet = await _hasInternetConnection();
    if (!hasInternet) {
      return {
        'error': 'Sin conexión a internet',
        'internet': false,
      };
    }

    final authUser = _auth.currentUser;
    UserModel? firestoreUser;

    if (authUser != null) {
      try {
        firestoreUser = await _getUserFromFirestore(authUser.uid);
      } catch (e) {
        // Error al obtener desde Firestore
      }
    }

    return {
      'internet': true,
      'auth_user': authUser != null
          ? {
              'uid': authUser.uid,
              'email': authUser.email,
              'displayName': authUser.displayName,
            }
          : null,
      'firestore_user': firestoreUser?.toMap(),
      'current_user': _currentUser?.toMap(),
      'cache_user': (await _loadUserFromCache())?.toMap(),
    };
  }

  // Limpiar completamente el estado del usuario
  Future<void> forceSignOut() async {
    try {
      final hasInternet = await _hasInternetConnection();
      if (hasInternet && _auth.currentUser != null) {
        await _auth.signOut();
      }
    } catch (e) {
      print('Error al cerrar sesión en Firebase: $e');
    }

    _currentUser = null;
    await _clearUserCache();
  }

  // Sincronizar usuario de Authentication con Firestore
  Future<UserModel?> syncAuthUserWithFirestore() async {
    final hasInternet = await _hasInternetConnection();
    if (!hasInternet) {
      throw Exception('Se requiere conexión a internet para sincronizar');
    }

    final authUser = _auth.currentUser;
    if (authUser == null) {
      throw Exception('No hay usuario autenticado en Firebase Authentication');
    }

    try {
      // Intentar obtener desde Firestore
      final firestoreUser = await _getUserFromFirestore(authUser.uid);
      if (firestoreUser != null) {
        _currentUser = firestoreUser;
        await _saveUserToCache(firestoreUser);
        return firestoreUser;
      }

      // Si no existe en Firestore, crear el documento
      final now = DateTime.now();
      final userModel = UserModel(
        uid: authUser.uid,
        email: authUser.email ?? '',
        displayName: authUser.displayName ?? 'Usuario',
        role: UserRole.user,
        createdAt: now,
        lastLogin: now,
      );

      await _firestore
          .collection('users')
          .doc(authUser.uid)
          .set(userModel.toMap());
      _currentUser = userModel;
      await _saveUserToCache(userModel);

      return userModel;
    } catch (e) {
      throw Exception('Error al sincronizar usuario: ${e.toString()}');
    }
  }

  // Método de emergencia para eliminar usuario problemático (solo para desarrollo)
  Future<void> deleteCurrentAuthUser() async {
    final hasInternet = await _hasInternetConnection();
    if (!hasInternet) {
      throw Exception('Se requiere conexión a internet para eliminar usuario');
    }

    final authUser = _auth.currentUser;
    if (authUser == null) {
      throw Exception('No hay usuario autenticado para eliminar');
    }

    try {
      final uid = authUser.uid;

      // Eliminar de Firestore primero
      try {
        await _firestore.collection('users').doc(uid).delete();
      } catch (e) {
        print('Error al eliminar de Firestore (puede que no exista): $e');
      }

      // Eliminar de Authentication
      await authUser.delete();

      // Limpiar estado local
      _currentUser = null;
      await _clearUserCache();
    } catch (e) {
      throw Exception('Error al eliminar usuario: ${e.toString()}');
    }
  }

  // Método para eliminar usuario por email (útil para limpiar duplicados)
  Future<void> deleteUserByEmail(String email, String password) async {
    final hasInternet = await _hasInternetConnection();
    if (!hasInternet) {
      throw Exception('Se requiere conexión a internet');
    }

    try {
      // Primero hacer login con el usuario problemático
      final userCredential = await _auth.signInWithEmailAndPassword(
        email: email,
        password: password,
      );

      final user = userCredential.user;
      if (user != null) {
        final uid = user.uid;

        // Eliminar de Firestore
        try {
          await _firestore.collection('users').doc(uid).delete();
        } catch (e) {
          print('Error al eliminar de Firestore: $e');
        }

        // Eliminar de Authentication
        await user.delete();

        // Limpiar estado local
        _currentUser = null;
        await _clearUserCache();
      }
    } catch (e) {
      throw Exception('Error al eliminar usuario por email: ${e.toString()}');
    }
  }

  // Método mejorado para verificar si un email existe usando solo Firestore
  Future<bool> checkIfEmailExists(String email) async {
    final hasInternet = await _hasInternetConnection();
    if (!hasInternet) {
      throw Exception('Se requiere conexión a internet');
    }

    try {
      // Buscar en Firestore si existe un documento con este email
      final querySnapshot = await _firestore
          .collection('users')
          .where('email', isEqualTo: email)
          .limit(1)
          .get();

      return querySnapshot.docs.isNotEmpty;
    } catch (e) {
      print('Error al verificar email en Firestore: $e');
      // En caso de error con Firestore, asumimos que no existe para permitir el registro
      return false;
    }
  }
}
