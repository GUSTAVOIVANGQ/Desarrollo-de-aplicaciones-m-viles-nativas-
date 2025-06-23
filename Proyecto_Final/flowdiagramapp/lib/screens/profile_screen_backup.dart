import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../services/auth_service.dart';
import '../models/user_model.dart';
import 'login_screen.dart';
import 'metrics_screen.dart';
import 'admin_metrics_screen.dart';
                        title: const Text('Panel de Administración'),
                        subtitle: const Text(
                            'Ver métricas globales y gestionar usuarios'),
                        trailing: const Icon(Icons.arrow_forward_ios),
                        onTap: () {
                          Navigator.of(context).push(
                            MaterialPageRoute(
                              builder: (context) => const AdminMetricsScreen(),
                            ),
                          );
                        }, 'admin_metrics_screen.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key});

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  final AuthService _authService = AuthService();

  Future<void> _signOut() async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Cerrar Sesión'),
        content: const Text('¿Estás seguro de que deseas cerrar sesión?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('Cancelar'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(context).pop(true),
            child: const Text('Cerrar Sesión'),
          ),
        ],
      ),
    );

    if (confirmed == true && mounted) {
      await _authService.signOut();
      Navigator.of(context).pushAndRemoveUntil(
        MaterialPageRoute(builder: (context) => const LoginScreen()),
        (route) => false,
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final user = _authService.currentUser;
    if (user == null) {
      return const LoginScreen();
    }

    return Scaffold(
      appBar: AppBar(
        title: const Text('Mi Perfil'),
        backgroundColor: Colors.transparent,
        elevation: 0,
      ),
      body: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          children: [
            // Avatar y información básica
            Container(
              padding: const EdgeInsets.all(24),
              decoration: BoxDecoration(
                color: Theme.of(context).primaryColor.withOpacity(0.1),
                borderRadius: BorderRadius.circular(20),
              ),
              child: Column(
                children: [
                  CircleAvatar(
                    radius: 50,
                    backgroundColor: Theme.of(context).primaryColor,
                    child: Text(
                      user.displayName.isNotEmpty
                          ? user.displayName.substring(0, 1).toUpperCase()
                          : 'U',
                      style: const TextStyle(
                        fontSize: 32,
                        fontWeight: FontWeight.bold,
                        color: Colors.white,
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),
                  Text(
                    user.displayName,
                    style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    user.email,
                    style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                          color: Colors.grey[600],
                        ),
                  ),
                  const SizedBox(height: 8),
                  Container(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                    decoration: BoxDecoration(
                      color: user.isAdmin
                          ? Colors.red.withOpacity(0.1)
                          : Colors.blue.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(
                        color: user.isAdmin
                            ? Colors.red.withOpacity(0.3)
                            : Colors.blue.withOpacity(0.3),
                      ),
                    ),
                    child: Text(
                      user.isAdmin ? 'Administrador' : 'Usuario',
                      style: TextStyle(
                        color:
                            user.isAdmin ? Colors.red[700] : Colors.blue[700],
                        fontWeight: FontWeight.bold,
                        fontSize: 12,
                      ),
                    ),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 32),

            // Información detallada
            Expanded(
              child: ListView(
                children: [
                  _buildInfoCard(
                    title: 'Información de la cuenta',
                    items: [
                      _buildInfoItem(
                        icon: Icons.email_outlined,
                        label: 'Correo electrónico',
                        value: user.email,
                      ),
                      _buildInfoItem(
                        icon: Icons.person_outline,
                        label: 'Nombre',
                        value: user.displayName,
                      ),
                      _buildInfoItem(
                        icon: Icons.admin_panel_settings_outlined,
                        label: 'Tipo de cuenta',
                        value: user.isAdmin ? 'Administrador' : 'Usuario',
                      ),
                      _buildInfoItem(
                        icon: Icons.calendar_today_outlined,
                        label: 'Fecha de registro',
                        value: DateFormat('dd/MM/yyyy').format(user.createdAt),
                      ),
                      _buildInfoItem(
                        icon: Icons.access_time_outlined,
                        label: 'Último acceso',
                        value: DateFormat('dd/MM/yyyy HH:mm')
                            .format(user.lastLogin),
                      ),
                    ],
                  ),

                  const SizedBox(height: 16),

                  // Métricas (si existen)
                  if (user.metrics.isNotEmpty)
                    _buildInfoCard(
                      title: 'Métricas personales',
                      items: user.metrics.entries.map((entry) {
                        return _buildInfoItem(
                          icon: Icons.analytics_outlined,
                          label: entry.key,
                          value: entry.value.toString(),
                        );
                      }).toList(),
                    ),

                  const SizedBox(height: 24),

                  // Botón para ver métricas personales
                  Card(
                    child: ListTile(
                      leading: const Icon(Icons.analytics_outlined),
                      title: const Text('Mis Métricas'),
                      subtitle: const Text('Ver estadísticas de uso y progreso'),
                      trailing: const Icon(Icons.arrow_forward_ios),
                      onTap: () {
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (context) => const MetricsScreen(),
                          ),
                        );
                      },
                    ),
                  ),

                  const SizedBox(height: 16),

                  // Opciones administrativas
                  if (user.isAdmin) ...[
                    Card(
                      color: Colors.purple.shade50,
                      child: ListTile(
                        leading: const Icon(Icons.admin_panel_settings, color: Colors.purple),
                        title: const Text('Panel de Administración'),
                        subtitle: const Text('Ver métricas globales y gestionar usuarios'),
                        trailing: const Icon(Icons.arrow_forward_ios),
                        onTap: () {
                          Navigator.of(context).push(
                            MaterialPageRoute(
                              builder: (context) => const AdminMetricsScreen(),
                            ),
                          );
                        },
                      ),
                    ),
                    const SizedBox(height: 16),
                  ],

                  // Botón de cerrar sesión
                  SizedBox(
                    width: double.infinity,
                    height: 50,
                    child: OutlinedButton.icon(
                      onPressed: _signOut,
                      icon: const Icon(Icons.logout),
                      label: const Text('Cerrar Sesión'),
                      style: OutlinedButton.styleFrom(
                        foregroundColor: Colors.red,
                        side: const BorderSide(color: Colors.red),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildInfoCard({required String title, required List<Widget> items}) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              title,
              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
            ),
            const SizedBox(height: 16),
            ...items,
          ],
        ),
      ),
    );
  }

  Widget _buildInfoItem({
    required IconData icon,
    required String label,
    required String value,
  }) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12.0),
      child: Row(
        children: [
          Icon(icon, size: 20, color: Colors.grey[600]),
          const SizedBox(width: 12),
          Expanded(
            flex: 2,
            child: Text(
              label,
              style: TextStyle(
                color: Colors.grey[600],
                fontSize: 14,
              ),
            ),
          ),
          Expanded(
            flex: 3,
            child: Text(
              value,
              style: const TextStyle(
                fontWeight: FontWeight.w500,
                fontSize: 14,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
