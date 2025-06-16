import 'package:flutter/material.dart';
import 'package:timezone/data/latest.dart' as tz;
import 'notification_service.dart';

/// Función principal de la aplicación
/// Inicializa las zonas horarias y el servicio de notificaciones antes de ejecutar la app
void main() async {
  // Asegura que los widgets estén inicializados antes de cualquier operación asíncrona
  WidgetsFlutterBinding.ensureInitialized();

  // Inicializa las zonas horarias para notificaciones programadas
  tz.initializeTimeZones();

  // Inicializa el servicio de notificaciones
  await NotificationService.initialize();

  // Ejecuta la aplicación
  runApp(const MyApp());
}

/// Widget principal de la aplicación
/// Configura el tema y la pantalla inicial
class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Notificaciones Push Locales',
      theme: ThemeData(
        // Configuración del tema con Material Design 3
        colorScheme: ColorScheme.fromSeed(
          seedColor: Colors.deepPurple,
          brightness: Brightness.light,
        ),
        useMaterial3: true,
      ),
      // Pantalla inicial de la aplicación
      home: const NotificationHomePage(title: 'Notificaciones Push Locales'),
    );
  }
}

/// Pantalla principal de la aplicación
/// Permite enviar diferentes tipos de notificaciones locales
class NotificationHomePage extends StatefulWidget {
  const NotificationHomePage({super.key, required this.title});

  final String title;

  @override
  State<NotificationHomePage> createState() => _NotificationHomePageState();
}

/// Estado de la pantalla principal
/// Maneja la lógica para enviar notificaciones y mostrar mensajes de estado
class _NotificationHomePageState extends State<NotificationHomePage> {
  // Contador para demostrar diferentes notificaciones
  int _notificationCounter = 0;

  // Variable para mostrar el estado de los permisos
  bool _permissionsGranted = false;

  // Mensaje de estado para mostrar al usuario
  String _statusMessage = 'Presiona un botón para enviar una notificación';

  @override
  void initState() {
    super.initState();
    // Solicita permisos al inicializar la pantalla
    _requestPermissions();
  }

  /// Solicita permisos para mostrar notificaciones
  Future<void> _requestPermissions() async {
    final granted = await NotificationService.requestPermissions();
    setState(() {
      _permissionsGranted = granted;
      _statusMessage =
          granted
              ? 'Permisos concedidos. Listo para enviar notificaciones.'
              : 'Permisos denegados. No se pueden enviar notificaciones.';
    });
  }

  /// Envía una notificación inmediata simple
  Future<void> _sendSimpleNotification() async {
    if (!_permissionsGranted) {
      _showSnackBar('Permisos de notificación no concedidos');
      return;
    }

    _notificationCounter++;

    await NotificationService.showNotification(
      title: '¡Notificación Enviada! #$_notificationCounter',
      body: 'Esta es una notificación push local simple.',
      payload: 'simple_notification_$_notificationCounter',
    );

    setState(() {
      _statusMessage = 'Notificación simple enviada #$_notificationCounter';
    });

    _showSnackBar('Notificación enviada correctamente');
  }

  /// Envía una notificación con información detallada
  Future<void> _sendDetailedNotification() async {
    if (!_permissionsGranted) {
      _showSnackBar('Permisos de notificación no concedidos');
      return;
    }

    _notificationCounter++;
    final currentTime = DateTime.now();

    await NotificationService.showNotification(
      title: '📱 Notificación Detallada',
      body:
          'Enviada el ${currentTime.day}/${currentTime.month}/${currentTime.year} '
          'a las ${currentTime.hour}:${currentTime.minute.toString().padLeft(2, '0')}',
      payload: 'detailed_notification_$_notificationCounter',
    );

    setState(() {
      _statusMessage = 'Notificación detallada enviada #$_notificationCounter';
    });

    _showSnackBar('Notificación detallada enviada');
  }

  /// Programa una notificación para 5 segundos en el futuro
  Future<void> _scheduleNotification() async {
    if (!_permissionsGranted) {
      _showSnackBar('Permisos de notificación no concedidos');
      return;
    }

    final scheduledTime = DateTime.now().add(const Duration(seconds: 5));

    await NotificationService.scheduleNotification(
      title: '⏰ Notificación Programada',
      body: 'Esta notificación fue programada hace 5 segundos',
      scheduledTime: scheduledTime,
      payload: 'scheduled_notification',
    );

    setState(() {
      _statusMessage = 'Notificación programada para dentro de 5 segundos';
    });

    _showSnackBar('Notificación programada para dentro de 5 segundos');
  }

  /// Cancela todas las notificaciones pendientes
  Future<void> _cancelAllNotifications() async {
    await NotificationService.cancelAllNotifications();

    setState(() {
      _statusMessage = 'Todas las notificaciones han sido canceladas';
    });

    _showSnackBar('Notificaciones canceladas');
  }

  /// Muestra un SnackBar con un mensaje
  void _showSnackBar(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        duration: const Duration(seconds: 2),
        behavior: SnackBarBehavior.floating,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        // AppBar con gradiente de color
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(
          widget.title,
          style: const TextStyle(fontWeight: FontWeight.bold),
        ),
        centerTitle: true,
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Tarjeta con el estado de permisos
            Card(
              elevation: 4,
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  children: [
                    Icon(
                      _permissionsGranted ? Icons.check_circle : Icons.error,
                      size: 48,
                      color: _permissionsGranted ? Colors.green : Colors.red,
                    ),
                    const SizedBox(height: 8),
                    Text(
                      'Estado de Permisos',
                      style: Theme.of(context).textTheme.headlineSmall,
                    ),
                    const SizedBox(height: 8),
                    Text(
                      _permissionsGranted
                          ? 'Permisos concedidos ✅'
                          : 'Permisos denegados ❌',
                      style: TextStyle(
                        color: _permissionsGranted ? Colors.green : Colors.red,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 20),

            // Mensaje de estado
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Theme.of(context).colorScheme.surfaceVariant,
                borderRadius: BorderRadius.circular(8),
              ),
              child: Text(
                _statusMessage,
                textAlign: TextAlign.center,
                style: Theme.of(context).textTheme.bodyLarge,
              ),
            ),

            const SizedBox(height: 20),

            // Contador de notificaciones
            Text(
              'Notificaciones enviadas: $_notificationCounter',
              textAlign: TextAlign.center,
              style: Theme.of(context).textTheme.headlineSmall,
            ),

            const SizedBox(height: 30),

            // Botones para diferentes tipos de notificaciones
            ElevatedButton.icon(
              onPressed: _sendSimpleNotification,
              icon: const Icon(Icons.notifications),
              label: const Text('Notificación Simple'),
              style: ElevatedButton.styleFrom(
                padding: const EdgeInsets.all(16),
                textStyle: const TextStyle(fontSize: 16),
              ),
            ),

            const SizedBox(height: 12),

            ElevatedButton.icon(
              onPressed: _sendDetailedNotification,
              icon: const Icon(Icons.info),
              label: const Text('Notificación Detallada'),
              style: ElevatedButton.styleFrom(
                padding: const EdgeInsets.all(16),
                textStyle: const TextStyle(fontSize: 16),
              ),
            ),

            const SizedBox(height: 12),

            ElevatedButton.icon(
              onPressed: _scheduleNotification,
              icon: const Icon(Icons.schedule),
              label: const Text('Programar Notificación (5s)'),
              style: ElevatedButton.styleFrom(
                padding: const EdgeInsets.all(16),
                textStyle: const TextStyle(fontSize: 16),
              ),
            ),

            const SizedBox(height: 20),

            // Botón para cancelar notificaciones
            OutlinedButton.icon(
              onPressed: _cancelAllNotifications,
              icon: const Icon(Icons.cancel, color: Colors.red),
              label: const Text(
                'Cancelar Todas las Notificaciones',
                style: TextStyle(color: Colors.red),
              ),
              style: OutlinedButton.styleFrom(
                padding: const EdgeInsets.all(16),
                side: const BorderSide(color: Colors.red),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
