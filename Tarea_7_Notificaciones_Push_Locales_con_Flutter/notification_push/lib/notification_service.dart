import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:timezone/timezone.dart' as tz;

/// Servicio para manejar notificaciones locales
/// Esta clase encapsula toda la lógica para configurar y mostrar notificaciones
class NotificationService {
  // Instancia singleton del plugin de notificaciones locales
  static final FlutterLocalNotificationsPlugin _notificationsPlugin =
      FlutterLocalNotificationsPlugin();

  /// Inicializa el servicio de notificaciones
  /// Configura los ajustes para Android e iOS
  static Future<void> initialize() async {
    // Configuración para Android
    const AndroidInitializationSettings initializationSettingsAndroid =
        AndroidInitializationSettings('@mipmap/ic_launcher');

    // Configuración para iOS
    const DarwinInitializationSettings initializationSettingsIOS =
        DarwinInitializationSettings(
          requestAlertPermission: true,
          requestBadgePermission: true,
          requestSoundPermission: true,
        );

    // Configuración general que combina Android e iOS
    const InitializationSettings initializationSettings =
        InitializationSettings(
          android: initializationSettingsAndroid,
          iOS: initializationSettingsIOS,
        );

    // Inicializa el plugin con las configuraciones
    await _notificationsPlugin.initialize(
      initializationSettings,
      onDidReceiveNotificationResponse: (NotificationResponse response) {
        // Callback que se ejecuta cuando el usuario toca la notificación
        print('Notificación tocada: ${response.payload}');
      },
    );
  }

  /// Solicita permisos para mostrar notificaciones
  /// En Android 13+ es necesario solicitar permiso explícitamente
  static Future<bool> requestPermissions() async {
    // Solicitar permiso para notificaciones
    PermissionStatus status = await Permission.notification.request();

    return status == PermissionStatus.granted;
  }

  /// Muestra una notificación inmediata
  /// [title] - Título de la notificación
  /// [body] - Cuerpo del mensaje de la notificación
  /// [payload] - Datos adicionales que se pueden enviar con la notificación
  static Future<void> showNotification({
    required String title,
    required String body,
    String? payload,
  }) async {
    // Detalles específicos para Android
    const AndroidNotificationDetails androidPlatformChannelSpecifics =
        AndroidNotificationDetails(
          'default_channel', // ID del canal
          'Notificaciones Generales', // Nombre del canal
          channelDescription:
              'Canal para notificaciones generales de la aplicación',
          importance:
              Importance
                  .high, // Importancia alta para que aparezca como heads-up
          priority: Priority.high, // Prioridad alta
          showWhen: true, // Muestra la hora de la notificación
          enableVibration: true, // Habilita vibración
          playSound: true, // Reproduce sonido
        );

    // Detalles específicos para iOS
    const DarwinNotificationDetails iOSPlatformChannelSpecifics =
        DarwinNotificationDetails(
          presentAlert: true, // Muestra alerta
          presentBadge: true, // Muestra badge en el icono
          presentSound: true, // Reproduce sonido
        );

    // Detalles de la notificación que combina ambas plataformas
    const NotificationDetails platformChannelSpecifics = NotificationDetails(
      android: androidPlatformChannelSpecifics,
      iOS: iOSPlatformChannelSpecifics,
    );

    // Muestra la notificación
    await _notificationsPlugin.show(
      0, // ID único de la notificación
      title,
      body,
      platformChannelSpecifics,
      payload: payload,
    );
  }

  /// Programa una notificación para un tiempo específico en el futuro
  /// [title] - Título de la notificación
  /// [body] - Cuerpo del mensaje
  /// [scheduledTime] - Tiempo en el que se debe mostrar la notificación
  /// [payload] - Datos adicionales
  static Future<void> scheduleNotification({
    required String title,
    required String body,
    required DateTime scheduledTime,
    String? payload,
  }) async {
    // Configuración para notificación programada en Android
    const AndroidNotificationDetails androidPlatformChannelSpecifics =
        AndroidNotificationDetails(
          'scheduled_channel',
          'Notificaciones Programadas',
          channelDescription: 'Canal para notificaciones programadas',
          importance: Importance.high,
          priority: Priority.high,
        );

    const NotificationDetails platformChannelSpecifics = NotificationDetails(
      android: androidPlatformChannelSpecifics,
    );

    // Convierte DateTime a TZDateTime para la zona horaria local
    final tz.TZDateTime scheduledTZTime = tz.TZDateTime.from(
      scheduledTime,
      tz.local,
    );

    // Programa la notificación para el tiempo especificado
    await _notificationsPlugin.zonedSchedule(
      1, // ID único para notificaciones programadas
      title,
      body,
      scheduledTZTime, // Usa TZDateTime en lugar de DateTime
      platformChannelSpecifics,
      payload: payload,
      uiLocalNotificationDateInterpretation:
          UILocalNotificationDateInterpretation.absoluteTime,
    );
  }

  /// Cancela todas las notificaciones pendientes
  static Future<void> cancelAllNotifications() async {
    await _notificationsPlugin.cancelAll();
  }

  /// Cancela una notificación específica por su ID
  /// [notificationId] - ID de la notificación a cancelar
  static Future<void> cancelNotification(int notificationId) async {
    await _notificationsPlugin.cancel(notificationId);
  }
}
