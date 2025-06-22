import 'package:cloud_firestore/cloud_firestore.dart';

enum UserRole { user, admin }

class UserModel {
  final String uid;
  final String email;
  final String displayName;
  final UserRole role;
  final DateTime createdAt;
  final DateTime lastLogin;
  final Map<String, dynamic> metrics;

  UserModel({
    required this.uid,
    required this.email,
    required this.displayName,
    this.role = UserRole.user,
    required this.createdAt,
    required this.lastLogin,
    this.metrics = const {},
  });

  // Convertir a Map para Firestore
  Map<String, dynamic> toMap() {
    return {
      'uid': uid,
      'email': email,
      'displayName': displayName,
      'role': role.name,
      'createdAt': createdAt.toIso8601String(),
      'lastLogin': lastLogin.toIso8601String(),
      'metrics': metrics,
    };
  }

  // Crear desde Map de Firestore
  factory UserModel.fromMap(Map<String, dynamic> map) {
    return UserModel(
      uid: map['uid'] ?? '',
      email: map['email'] ?? '',
      displayName: map['displayName'] ?? '',
      role: UserRole.values.firstWhere(
        (e) => e.name == map['role'],
        orElse: () => UserRole.user,
      ),
      createdAt: DateTime.parse(map['createdAt']),
      lastLogin: DateTime.parse(map['lastLogin']),
      metrics: Map<String, dynamic>.from(map['metrics'] ?? {}),
    );
  }

  // Crear desde DocumentSnapshot de Firestore
  factory UserModel.fromSnapshot(DocumentSnapshot snapshot) {
    final data = snapshot.data() as Map<String, dynamic>;
    return UserModel.fromMap(data);
  }

  // Crear una copia con campos modificados
  UserModel copyWith({
    String? uid,
    String? email,
    String? displayName,
    UserRole? role,
    DateTime? createdAt,
    DateTime? lastLogin,
    Map<String, dynamic>? metrics,
  }) {
    return UserModel(
      uid: uid ?? this.uid,
      email: email ?? this.email,
      displayName: displayName ?? this.displayName,
      role: role ?? this.role,
      createdAt: createdAt ?? this.createdAt,
      lastLogin: lastLogin ?? this.lastLogin,
      metrics: metrics ?? this.metrics,
    );
  }

  bool get isAdmin => role == UserRole.admin;
  bool get isUser => role == UserRole.user;
}
