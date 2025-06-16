# Notificación Local Simple con Flutter

Esta es una aplicación de ejemplo desarrollada en Flutter que muestra cómo implementar notificaciones push locales en Android. Al presionar un botón, la aplicación genera una notificación local en el dispositivo.

## Características
- Interfaz sencilla con un contador.
- Cada vez que se presiona el botón flotante, se incrementa el contador y se muestra una notificación local.
- Uso del paquete [`flutter_local_notifications`](https://pub.dev/packages/flutter_local_notifications) para gestionar las notificaciones.

## Cambios recientes y configuración importante
- Se agregó la dependencia `flutter_local_notifications` en `pubspec.yaml`:
  ```yaml
  dependencies:
    flutter:
      sdk: flutter
    flutter_local_notifications: ^17.0.0
  ```
- Se actualizó la configuración de NDK en `android/app/build.gradle.kts`:
  ```kotlin
  android {
      ndkVersion = "27.0.12077973"
      compileOptions {
          sourceCompatibility = JavaVersion.VERSION_11
          targetCompatibility = JavaVersion.VERSION_11
          // Habilita desugaring para soporte de Java 8+
          isCoreLibraryDesugaringEnabled = true
      }
      // ...
  }
  dependencies {
      coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
  }
  ```
- Se habilitó el core library desugaring para compatibilidad con Java 8+ y el plugin de notificaciones.

## Requisitos previos
- [Flutter](https://flutter.dev/docs/get-started/install) instalado en tu máquina.
- Un emulador o dispositivo Android para pruebas.

## Instalación y ejecución
1. Clona este repositorio o descarga el código fuente.
2. Abre el proyecto en tu editor favorito (por ejemplo, VS Code o Android Studio).
3. Abre una terminal en la raíz del proyecto y ejecuta:
   ```sh
   flutter clean
   flutter pub get
   flutter run
   ```

## Explicación del Código

### 1. Importación de paquetes
```dart
import 'package:flutter/material.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
```

### 2. Inicialización del plugin
En el método `initState` del `State` principal, se inicializa el plugin de notificaciones locales:
```dart
final FlutterLocalNotificationsPlugin flutterLocalNotificationsPlugin = FlutterLocalNotificationsPlugin();

@override
void initState() {
  super.initState();
  final AndroidInitializationSettings initializationSettingsAndroid = AndroidInitializationSettings('@mipmap/ic_launcher');
  final InitializationSettings initializationSettings = InitializationSettings(
    android: initializationSettingsAndroid,
  );
  flutterLocalNotificationsPlugin.initialize(initializationSettings);
}
```

### 3. Mostrar una notificación local
Cada vez que se presiona el botón, se ejecuta el método `_showNotification`:
```dart
Future<void> _showNotification() async {
  const AndroidNotificationDetails androidPlatformChannelSpecifics = AndroidNotificationDetails(
    'your_channel_id',
    'your_channel_name',
    channelDescription: 'your_channel_description',
    importance: Importance.max,
    priority: Priority.high,
    ticker: 'ticker',
  );
  const NotificationDetails platformChannelSpecifics = NotificationDetails(
    android: androidPlatformChannelSpecifics,
  );
  await flutterLocalNotificationsPlugin.show(
    0,
    '¡Notificación local!',
    'Has presionado el botón $_counter veces.',
    platformChannelSpecifics,
  );
}
```

### 4. Botón para disparar la notificación
El botón flotante incrementa el contador y llama a la función de notificación:
```dart
floatingActionButton: FloatingActionButton(
  onPressed: _incrementCounter,
  tooltip: 'Increment',
  child: const Icon(Icons.add),
),
```

## Comentarios en el código
El código fuente está ampliamente comentado para facilitar su comprensión.

## Recursos
- [Documentación oficial de flutter_local_notifications](https://pub.dev/packages/flutter_local_notifications)
- [Documentación de Flutter](https://flutter.dev/docs)

---

**Autor:**
- [Tu Nombre]
