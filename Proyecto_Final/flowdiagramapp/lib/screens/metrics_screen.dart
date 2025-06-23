import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../services/metrics_service.dart';
import '../services/auth_service.dart';
import '../models/metric_model.dart';
import '../models/user_model.dart';
import 'admin_metrics_screen.dart';

class MetricsScreen extends StatefulWidget {
  const MetricsScreen({super.key});

  @override
  State<MetricsScreen> createState() => _MetricsScreenState();
}

class _MetricsScreenState extends State<MetricsScreen> {
  final MetricsService _metricsService = MetricsService();
  final AuthService _authService = AuthService();
  MetricsSummary? _summary;
  bool _isLoading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadMetrics();
  }

  Future<void> _loadMetrics() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final summary = await _metricsService.getUserMetricsSummary();
      setState(() {
        _summary = summary;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _error = e.toString();
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final user = _authService.currentUser;

    if (user == null) {
      return Scaffold(
        appBar: AppBar(title: const Text('Métricas')),
        body: const Center(
          child: Text('Debes iniciar sesión para ver las métricas'),
        ),
      );
    }

    return Scaffold(
      appBar: AppBar(
        title: Text(user.isAdmin ? 'Métricas del Sistema' : 'Mis Métricas'),
        backgroundColor: Colors.transparent,
        elevation: 0,
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _loadMetrics,
          ),
        ],
      ),
      body: _buildBody(user),
    );
  }

  Widget _buildBody(UserModel user) {
    if (_isLoading) {
      return const Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            CircularProgressIndicator(),
            SizedBox(height: 16),
            Text('Cargando métricas...'),
          ],
        ),
      );
    }

    if (_error != null) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(
              Icons.error_outline,
              size: 64,
              color: Colors.red,
            ),
            const SizedBox(height: 16),
            Text(
              'Error al cargar métricas',
              style: Theme.of(context).textTheme.headlineSmall,
            ),
            const SizedBox(height: 8),
            Text(
              _error!,
              style: Theme.of(context).textTheme.bodyMedium,
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: _loadMetrics,
              child: const Text('Reintentar'),
            ),
          ],
        ),
      );
    }

    if (_summary == null) {
      return const Center(
        child: Text('No hay datos de métricas disponibles'),
      );
    }

    return RefreshIndicator(
      onRefresh: _loadMetrics,
      child: SingleChildScrollView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildUserInfo(user),
            const SizedBox(height: 24),
            _buildTechnicalMetrics(_summary!),
            const SizedBox(height: 16),
            _buildEducationalMetrics(_summary!),
            const SizedBox(height: 16),
            _buildProgressChart(_summary!),
            const SizedBox(height: 16),
            _buildRecentActivity(user),
            if (user.isAdmin) ...[
              const SizedBox(height: 24),
              _buildAdminSection(),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildUserInfo(UserModel user) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Row(
          children: [
            CircleAvatar(
              radius: 30,
              backgroundColor: user.isAdmin ? Colors.purple : Colors.blue,
              child: Text(
                user.displayName.isNotEmpty
                    ? user.displayName[0].toUpperCase()
                    : 'U',
                style: const TextStyle(
                  fontSize: 24,
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    user.displayName,
                    style: Theme.of(context).textTheme.headlineSmall,
                  ),
                  Text(
                    user.email,
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                          color: Colors.grey[600],
                        ),
                  ),
                  const SizedBox(height: 4),
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 8,
                      vertical: 4,
                    ),
                    decoration: BoxDecoration(
                      color: user.isAdmin ? Colors.purple : Colors.blue,
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Text(
                      user.isAdmin ? 'Administrador' : 'Usuario',
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 12,
                        fontWeight: FontWeight.bold,
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

  Widget _buildTechnicalMetrics(MetricsSummary summary) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(Icons.code, color: Colors.blue),
                const SizedBox(width: 8),
                Text(
                  'Métricas Técnicas',
                  style: Theme.of(context).textTheme.titleLarge?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(
                  child: _buildMetricCard(
                    'Diagramas Creados',
                    summary.totalDiagrams.toString(),
                    Icons.account_tree,
                    Colors.green,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: _buildMetricCard(
                    'Código Generado',
                    summary.totalCodeGenerations.toString(),
                    Icons.code,
                    Colors.orange,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(
                  child: _buildMetricCard(
                    'Validaciones',
                    summary.totalValidations.toString(),
                    Icons.check_circle,
                    Colors.blue,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: _buildMetricCard(
                    'Plantillas Usadas',
                    summary.totalTemplatesUsed.toString(),
                    Icons.description,
                    Colors.purple,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildEducationalMetrics(MetricsSummary summary) {
    final educationalMetrics = summary.educationalMetrics;

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(Icons.school, color: Colors.green),
                const SizedBox(width: 8),
                Text(
                  'Métricas Educativas',
                  style: Theme.of(context).textTheme.titleLarge?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(
                  child: _buildMetricCard(
                    'Tasa de Éxito',
                    '${(summary.successRate * 100).toStringAsFixed(1)}%',
                    Icons.trending_up,
                    Colors.green,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: _buildMetricCard(
                    'Tiempo Promedio',
                    '${summary.averageTime.toStringAsFixed(1)} min',
                    Icons.timer,
                    Colors.blue,
                  ),
                ),
              ],
            ),
            if (educationalMetrics.isNotEmpty) ...[
              const SizedBox(height: 16),
              const Divider(),
              const SizedBox(height: 8),
              Text(
                'Detalles Educativos',
                style: Theme.of(context).textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
              ),
              const SizedBox(height: 12),
              ...educationalMetrics.entries.map((entry) {
                return _buildEducationalMetricRow(entry.key, entry.value);
              }).toList(),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildEducationalMetricRow(String key, dynamic value) {
    String displayKey = _formatMetricKey(key);
    String displayValue = _formatMetricValue(value);

    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Expanded(
            child: Text(
              displayKey,
              style: Theme.of(context).textTheme.bodyMedium,
            ),
          ),
          Text(
            displayValue,
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: Colors.blue,
                ),
          ),
        ],
      ),
    );
  }

  String _formatMetricKey(String key) {
    switch (key) {
      case 'ejercicios_completados':
        return 'Ejercicios Completados';
      case 'ejercicios_exitosos':
        return 'Ejercicios Exitosos';
      case 'tiempo_total_minutos':
        return 'Tiempo Total (min)';
      case 'errores_totales':
        return 'Errores Totales';
      case 'pistas_usadas':
        return 'Pistas Usadas';
      case 'confianza_promedio':
        return 'Confianza Promedio';
      case 'tasa_exito':
        return 'Tasa de Éxito';
      default:
        return key
            .replaceAll('_', ' ')
            .split(' ')
            .map((word) => word[0].toUpperCase() + word.substring(1))
            .join(' ');
    }
  }

  String _formatMetricValue(dynamic value) {
    if (value is double) {
      if (value < 1) {
        return '${(value * 100).toStringAsFixed(1)}%';
      }
      return value.toStringAsFixed(1);
    }
    return value.toString();
  }

  Widget _buildMetricCard(
      String title, String value, IconData icon, Color color) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: color.withOpacity(0.1),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: color.withOpacity(0.3)),
      ),
      child: Column(
        children: [
          Icon(icon, color: color, size: 32),
          const SizedBox(height: 8),
          Text(
            value,
            style: TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.bold,
              color: color,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            title,
            style: TextStyle(
              fontSize: 12,
              color: Colors.grey[600],
            ),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }

  Widget _buildProgressChart(MetricsSummary summary) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(Icons.show_chart, color: Colors.orange),
                const SizedBox(width: 8),
                Text(
                  'Progreso General',
                  style: Theme.of(context).textTheme.titleLarge?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            _buildProgressBar(
              'Diagramas',
              summary.totalDiagrams,
              100, // Meta hipotética
              Colors.green,
            ),
            const SizedBox(height: 12),
            _buildProgressBar(
              'Validaciones Exitosas',
              (summary.totalValidations * summary.successRate).round(),
              50, // Meta hipotética
              Colors.blue,
            ),
            const SizedBox(height: 12),
            _buildProgressBar(
              'Uso de Plantillas',
              summary.totalTemplatesUsed,
              20, // Meta hipotética
              Colors.purple,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildProgressBar(String label, int current, int target, Color color) {
    final progress = current / target;
    final clampedProgress = progress.clamp(0.0, 1.0);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              label,
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    fontWeight: FontWeight.w500,
                  ),
            ),
            Text(
              '$current / $target',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    color: Colors.grey[600],
                  ),
            ),
          ],
        ),
        const SizedBox(height: 8),
        LinearProgressIndicator(
          value: clampedProgress,
          backgroundColor: color.withOpacity(0.2),
          valueColor: AlwaysStoppedAnimation<Color>(color),
          minHeight: 8,
        ),
        const SizedBox(height: 4),
        Text(
          '${(clampedProgress * 100).toStringAsFixed(1)}%',
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
                color: color,
                fontWeight: FontWeight.bold,
              ),
        ),
      ],
    );
  }

  Widget _buildRecentActivity(UserModel user) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(Icons.history, color: Colors.grey),
                const SizedBox(width: 8),
                Text(
                  'Actividad Reciente',
                  style: Theme.of(context).textTheme.titleLarge?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            _buildActivityItem(
              'Último acceso',
              DateFormat('dd/MM/yyyy HH:mm').format(user.lastLogin),
              Icons.login,
            ),
            _buildActivityItem(
              'Miembro desde',
              DateFormat('dd/MM/yyyy').format(user.createdAt),
              Icons.person_add,
            ),
            if (user.metrics['ultima_actividad'] != null)
              _buildActivityItem(
                'Última actividad',
                _formatDateTime(user.metrics['ultima_actividad']),
                Icons.update,
              ),
            if (user.metrics['ultima_plantilla'] != null)
              _buildActivityItem(
                'Última plantilla usada',
                user.metrics['ultima_plantilla'].toString(),
                Icons.description,
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildActivityItem(String label, String value, IconData icon) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: Row(
        children: [
          Icon(icon, size: 20, color: Colors.grey[600]),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  label,
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                        color: Colors.grey[600],
                      ),
                ),
                Text(
                  value,
                  style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                        fontWeight: FontWeight.w500,
                      ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAdminSection() {
    return Card(
      color: Colors.purple.shade50,
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(Icons.admin_panel_settings, color: Colors.purple),
                const SizedBox(width: 8),
                Text(
                  'Panel de Administrador',
                  style: Theme.of(context).textTheme.titleLarge?.copyWith(
                        fontWeight: FontWeight.bold,
                        color: Colors.purple,
                      ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Text(
              'Como administrador, tienes acceso a métricas globales y análisis detallados de todos los usuarios.',
              style: Theme.of(context).textTheme.bodyMedium,
            ),
            const SizedBox(height: 16),
            ElevatedButton.icon(
              onPressed: () {
                Navigator.of(context).push(
                  MaterialPageRoute(
                    builder: (context) => const AdminMetricsScreen(),
                  ),
                );
              },
              icon: const Icon(Icons.analytics),
              label: const Text('Ver Métricas Globales'),
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.purple,
                foregroundColor: Colors.white,
              ),
            ),
          ],
        ),
      ),
    );
  }

  String _formatDateTime(String dateTimeStr) {
    try {
      final dateTime = DateTime.parse(dateTimeStr);
      return DateFormat('dd/MM/yyyy HH:mm').format(dateTime);
    } catch (e) {
      return dateTimeStr;
    }
  }
}
