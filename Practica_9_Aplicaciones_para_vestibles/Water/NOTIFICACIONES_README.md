# Sistema de Notificaciones FCM - Recordatorios de Hidratación

## Descripción

Esta funcionalidad implementa un sistema completo de notificaciones push usando Firebase Cloud Messaging (FCM) para enviar recordatorios de hidratación desde el dispositivo vestible a otros usuarios registrados en Firebase.

## Funcionalidades Implementadas

### 1. Inicialización automática de FCM
- **Archivo**: `NotificationManager.kt`
- **Función**: `initializeFCMToken()`
- El vestible se registra automáticamente en Firebase y obtiene un token FCM
- El token se guarda en Firestore asociado al usuario actual
- Suscripción automática a tópicos generales de notificaciones

### 2. Envío de recordatorios individuales
- **Pantalla**: `SendToFriendScreen`
- **Función**: `sendHydrationReminderToUser()`
- Permite seleccionar un mensaje predefinido
- Muestra la lista de amigos disponibles con estado online simulado
- Envía notificación personalizada a un amigo específico

### 3. Envío de recordatorios grupales
- **Pantalla**: `SendToGroupScreen`
- **Función**: `sendHydrationReminderToGroup()`
- Permite seleccionar un mensaje predefinido para grupos
- Muestra los grupos del usuario con número de miembros
- Envía notificación a todos los miembros del grupo seleccionado

### 4. Recordatorios rápidos
- **Pantalla**: `QuickReminderScreen`
- Mensajes predefinidos para envío rápido
- Envía a todos los grupos del usuario de una vez

### 5. Notificaciones automáticas por metas
- **Archivo**: `HydrationManager.kt`
- **Función**: `onGoalAchieved()`
- Cuando un usuario alcanza su meta diaria, se notifica automáticamente a sus amigos
- Se ejecuta solo la primera vez que se alcanza la meta cada día

### 6. Navegación integrada
- **Archivo**: `MainActivity.kt`
- Nuevo botón "Notif" en la pantalla principal
- Navegación completa entre todas las pantallas de notificaciones
- Integración con el sistema de navegación existente

## Estructura de Firebase

### Colección `device_tokens`
```
{
  userId: "user_id",
  fcmToken: "fcm_token_string",
  deviceType: "wearable",
  lastUpdated: timestamp,
  isActive: true
}
```

### Colección `notification_logs`
```
{
  targetUserId: "user_id",
  senderUserId: "sender_id", 
  type: "hydration_reminder",
  title: "título",
  body: "mensaje",
  timestamp: timestamp,
  status: "sent|error",
  fcmResponse: "response_id"
}
```

## Firebase Cloud Functions

### Funciones Implementadas

1. **`sendHydrationReminder`**
   - Envía recordatorio a un usuario específico
   - Parámetros: targetUserId, title, body, senderUserId, etc.

2. **`sendGroupHydrationReminder`**
   - Envía recordatorio a un grupo de usuarios
   - Parámetros: targetUserIds[], title, body, groupId, etc.

3. **`sendGoalAchievedNotification`**
   - Notifica cuando alguien alcanza su meta
   - Parámetros: targetUserIds[], senderUserId, intake, goal

4. **`sendScheduledHydrationReminders`**
   - Recordatorios automáticos cada 2 horas
   - Se ejecuta como cron job en Firebase

### Instalación de Cloud Functions

```bash
cd Water/firebase-functions
npm install
firebase deploy --only functions
```

## Configuración Requerida

### 1. Firebase Project Setup
- Proyecto Firebase configurado
- Firebase Cloud Messaging habilitado
- Firestore Database creado
- Firebase Functions habilitado

### 2. Dependencias Android
```kotlin
implementation("com.google.firebase:firebase-messaging-ktx")
implementation("com.google.firebase:firebase-functions-ktx")
implementation("com.google.firebase:firebase-firestore-ktx")
implementation("com.google.firebase:firebase-auth-ktx")
```

### 3. Permisos AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.INTERNET" />
```

### 4. Servicio FCM AndroidManifest.xml
```xml
<service
    android:name=".presentation.HydrationFirebaseMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

## Flujo de Uso

1. **Usuario abre la app del vestible**
   - Se inicializa automáticamente FCM
   - Se registra el token en Firebase
   - Se suscribe a tópicos generales

2. **Enviar recordatorio individual**
   - Home → Botón "Notif" → "A un Amigo"
   - Seleccionar mensaje predefinido
   - Seleccionar amigo de la lista
   - Confirmación de envío

3. **Enviar recordatorio grupal**
   - Home → Botón "Notif" → "A un Grupo"
   - Seleccionar mensaje predefinido
   - Seleccionar grupo
   - Se envía a todos los miembros

4. **Recordatorio rápido**
   - Home → Botón "Notif" → "Recordatorio Rápido"
   - Seleccionar mensaje
   - Se envía a todos los grupos automáticamente

5. **Notificación automática de meta**
   - Al agregar agua y alcanzar la meta diaria
   - Se notifica automáticamente a todos los amigos

## Características de las Notificaciones

### Notificaciones Enriquecidas
- Título personalizado según el tipo
- Mensaje del remitente incluido
- Datos adicionales (progreso del remitente, meta, etc.)
- Iconos y colores diferenciados por tipo

### Tipos de Notificaciones
- `hydration_reminder`: Recordatorio individual
- `group_hydration_reminder`: Recordatorio grupal  
- `goal_achieved`: Meta alcanzada
- `scheduled_reminder`: Recordatorio automático

### Canales de Notificación
- Canal: `HYDRATION_CHANNEL`
- Prioridad: Alta para recordatorios, Normal para programados
- Sonido y vibración configurables

## Testing

### Notificaciones Locales de Prueba
- Función `startLocalNotifications()` en MainActivity
- Notificaciones cada 30 segundos para testing
- Se puede activar/desactivar desde la configuración

### Logs y Debugging
- Todos los envíos se registran en `notification_logs`
- Logs detallados en Android Studio y Firebase Console
- Manejo de errores con mensajes descriptivos

## Consideraciones de Rendimiento

- Las notificaciones se envían de forma asíncrona
- Timeouts configurados para evitar bloqueos
- Manejo de errores sin afectar la funcionalidad principal
- Batching para envíos grupales grandes

## Próximas Mejoras

1. **Programación de recordatorios**
   - Permitir al usuario configurar horarios específicos
   - Recordatorios basados en ubicación

2. **Personalización avanzada**
   - Mensajes completamente personalizados
   - Plantillas de mensajes por grupo

3. **Analytics y métricas**
   - Tracking de efectividad de recordatorios
   - Estadísticas de engagement

4. **Notificaciones bidireccionales**
   - Respuestas rápidas desde la notificación
   - Confirmación de hidratación directa
