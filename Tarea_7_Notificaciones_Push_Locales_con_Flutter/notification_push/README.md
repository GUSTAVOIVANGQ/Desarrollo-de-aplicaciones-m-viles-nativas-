# 📱 Aplicación de Notificaciones Push Locales con Flutter

Una aplicación móvil desarrollada en Flutter que demuestra cómo implementar y gestionar notificaciones push locales en Android. Esta aplicación permite enviar diferentes tipos de notificaciones, programarlas y gestionarlas de manera completa.

## 🎯 Características Principales

- ✅ **Notificaciones inmediatas**: Envío de notificaciones al instante
- ⏰ **Notificaciones programadas**: Programar notificaciones para un tiempo futuro
- 🎨 **Múltiples tipos de notificación**: Simples y detalladas
- 🔐 **Gestión de permisos**: Solicitud automática de permisos de notificación
- ❌ **Cancelación de notificaciones**: Cancelar todas las notificaciones pendientes
- 📊 **Contador de notificaciones**: Seguimiento de notificaciones enviadas
- 🎨 **Interfaz moderna**: UI atractiva con Material Design 3

## 🛠️ Dependencias Utilizadas

```yaml
dependencies:
  flutter_local_notifications: ^17.2.3  # Plugin principal para notificaciones locales
  permission_handler: ^11.3.1            # Gestión de permisos del sistema
  timezone: ^0.9.4                       # Manejo de zonas horarias para notificaciones programadas
```

## 📂 Estructura del Proyecto

```
lib/
├── main.dart                 # Punto de entrada de la aplicación
└── notification_service.dart # Servicio para gestionar notificaciones
```

### Archivos Principales:

1. **`main.dart`**: Contiene la interfaz de usuario y la lógica de la aplicación
2. **`notification_service.dart`**: Encapsula toda la funcionalidad de notificaciones

## 🔧 Configuración del Proyecto

### Android (AndroidManifest.xml)

Se agregaron los siguientes permisos en `android/app/src/main/AndroidManifest.xml`:

```xml
<!-- Permisos para notificaciones locales -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

- **POST_NOTIFICATIONS**: Requerido para Android 13+ (API 33+)
- **VIBRATE**: Permite vibración en las notificaciones
- **WAKE_LOCK**: Permite despertar la pantalla

## 💻 Código Explicado

### 1. NotificationService (notification_service.dart)

Esta clase maneja toda la lógica de notificaciones:

```dart
class NotificationService {
  // Instancia singleton del plugin de notificaciones
  static final FlutterLocalNotificationsPlugin _notificationsPlugin =
      FlutterLocalNotificationsPlugin();
```

#### Métodos Principales:

- **`initialize()`**: Configura las notificaciones para Android e iOS
- **`requestPermissions()`**: Solicita permisos al usuario
- **`showNotification()`**: Muestra una notificación inmediata
- **`scheduleNotification()`**: Programa una notificación para el futuro
- **`cancelAllNotifications()`**: Cancela todas las notificaciones

#### Configuración de Canales (Android):

```dart
const AndroidNotificationDetails androidPlatformChannelSpecifics =
    AndroidNotificationDetails(
  'default_channel',           // ID único del canal
  'Notificaciones Generales',  // Nombre visible del canal
  channelDescription: 'Canal para notificaciones generales de la aplicación',
  importance: Importance.high, // Importancia alta (aparece como heads-up)
  priority: Priority.high,     // Prioridad alta
  showWhen: true,             // Muestra la hora
  enableVibration: true,      // Habilita vibración
  playSound: true,            // Reproduce sonido
);
```

### 2. Interfaz Principal (main.dart)

#### Inicialización de la App:

```dart
void main() async {
  WidgetsFlutterBinding.ensureInitialized(); // Inicializa Flutter
  tz.initializeTimeZones();                  // Configura zonas horarias
  await NotificationService.initialize();    // Inicializa notificaciones
  runApp(const MyApp());
}
```

#### Estado de la Aplicación:

```dart
class _NotificationHomePageState extends State<NotificationHomePage> {
  int _notificationCounter = 0;    // Contador de notificaciones enviadas
  bool _permissionsGranted = false; // Estado de permisos
  String _statusMessage = '';       // Mensaje de estado para el usuario
```

#### Métodos de Notificación:

1. **Notificación Simple**:
```dart
Future<void> _sendSimpleNotification() async {
  await NotificationService.showNotification(
    title: '¡Notificación Enviada! #$_notificationCounter',
    body: 'Esta es una notificación push local simple.',
    payload: 'simple_notification_$_notificationCounter',
  );
}
```

2. **Notificación Detallada**:
```dart
Future<void> _sendDetailedNotification() async {
  final currentTime = DateTime.now();
  await NotificationService.showNotification(
    title: '📱 Notificación Detallada',
    body: 'Enviada el ${currentTime.day}/${currentTime.month}/${currentTime.year}...',
  );
}
```

3. **Notificación Programada**:
```dart
Future<void> _scheduleNotification() async {
  final scheduledTime = DateTime.now().add(const Duration(seconds: 5));
  await NotificationService.scheduleNotification(
    title: '⏰ Notificación Programada',
    body: 'Esta notificación fue programada hace 5 segundos',
    scheduledTime: scheduledTime,
  );
}
```

## 🎨 Interfaz de Usuario

La aplicación presenta una interfaz intuitiva con:

### Componentes Principales:

1. **Tarjeta de Estado de Permisos**:
   - Indicador visual (✅/❌)
   - Estado de permisos en tiempo real

2. **Mensaje de Estado**:
   - Retroalimentación inmediata de acciones
   - Información sobre la última operación

3. **Contador de Notificaciones**:
   - Lleva la cuenta de notificaciones enviadas
   - Se actualiza automáticamente

4. **Botones de Acción**:
   - **Notificación Simple**: Envía una notificación básica
   - **Notificación Detallada**: Incluye fecha y hora
   - **Programar Notificación**: Se envía después de 5 segundos
   - **Cancelar Todas**: Cancela notificaciones pendientes

## 🚀 Instalación y Ejecución

### Prerrequisitos:
- Flutter SDK instalado
- Android Studio o VS Code
- Dispositivo Android o emulador

### Pasos para ejecutar:

1. **Clonar el proyecto**:
```bash
git clone [url-del-repositorio]
cd notification_push
```

2. **Instalar dependencias**:
```bash
flutter pub get
```

3. **Ejecutar en dispositivo**:
```bash
flutter run
```

## 📱 Funcionamiento de la App

### Flujo Principal:

1. **Inicio**: La app solicita permisos de notificación automáticamente
2. **Permisos**: El usuario debe conceder permisos para que funcionen las notificaciones
3. **Envío**: Presionar cualquier botón envía el tipo correspondiente de notificación
4. **Retroalimentación**: La app muestra confirmación mediante SnackBar y actualiza el estado

### Tipos de Notificación:

- **Simple**: Título y mensaje básico
- **Detallada**: Incluye emoji, fecha y hora específica
- **Programada**: Se programa para 5 segundos después de presionar el botón

## 🔍 Conceptos Técnicos Importantes

### 1. Canales de Notificación (Android 8.0+):
Los canales permiten agrupar notificaciones y que el usuario controle cada tipo por separado.

### 2. Permisos Runtime (Android 13+):
Las notificaciones requieren permiso explícito del usuario desde Android 13.

### 3. Zonas Horarias:
Para notificaciones programadas se usa `TZDateTime` para manejar correctamente las zonas horarias.

### 4. Payloads:
Datos adicionales que se pueden enviar con las notificaciones para identificarlas o manejar acciones específicas.

## 🐛 Solución de Problemas

### Notificaciones no aparecen:
1. Verificar que los permisos estén concedidos
2. Revisar la configuración del canal en ajustes del dispositivo
3. Asegurar que las notificaciones no estén bloqueadas

### Error de compilación:
1. Ejecutar `flutter clean`
2. Ejecutar `flutter pub get`
3. Rebuild del proyecto

## 📋 Funcionalidades Adicionales Posibles

- 🔔 Notificaciones con sonidos personalizados
- 🌅 Notificaciones diarias/semanales
- 👆 Acciones directas en las notificaciones
- 📊 Historial de notificaciones
- 🎨 Notificaciones con imágenes
- 📍 Notificaciones basadas en ubicación

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Por favor:
1. Fork el proyecto
2. Crea una rama para tu feature
3. Commit tus cambios
4. Push a la rama
5. Abre un Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo LICENSE para más detalles.

---

**Desarrollado con ❤️ usando Flutter para demostrar el poder de las notificaciones push locales.**
