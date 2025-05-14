# Integración de Firebase en SystemBooks

Este documento describe la integración de Firebase en la aplicación móvil SystemBooks para permitir autenticación, almacenamiento de datos y notificaciones push.

## 1. Servicios de Firebase Implementados

### Firebase Authentication
- Registro de usuarios con email y contraseña
- Inicio de sesión con email y contraseña
- Gestión de roles (administrador y usuario)
- Validación de roles mediante contraseña maestra para administradores

### Firebase Firestore
- Almacenamiento de perfiles de usuarios
- Gestión de roles y permisos
- Historial de búsquedas y favoritos (preparado para implementar)

### Firebase Cloud Messaging (FCM)
- Sistema de notificaciones push
- Recepción de mensajes en primer y segundo plano
- Soporte para envío de notificaciones a usuarios específicos o a todos

## 2. Estructura de Archivos

### Clases Principales:

- `FirebaseManager`: Singleton para inicializar y acceder a los servicios de Firebase
- `FirebaseAuthRepository`: Gestiona las operaciones de autenticación
- `FirebaseUser`: Modelo de datos para usuarios en Firebase
- `NotificationHelper`: Ayuda a gestionar las notificaciones FCM
- `FCMService`: Servicio para recibir y procesar notificaciones

### Fragmentos de UI:

- `FirebaseLoginFragment`: Interfaz para inicio de sesión con Firebase
- `FirebaseRegisterFragment`: Interfaz para registro con Firebase

## 3. Configuración Inicial

Para utilizar esta integración, es necesario:

1. Crear un proyecto en la consola de Firebase (https://console.firebase.google.com/)
2. Registrar la aplicación Android en Firebase
3. Descargar el archivo `google-services.json` y colocarlo en la carpeta `app/`
4. Asegurarse de tener las dependencias correctas en los archivos build.gradle

## 4. Flujo de Autenticación

1. El usuario puede elegir entre autenticación tradicional (API) o Firebase
2. Si elige Firebase:
   - Puede registrarse como usuario normal o como administrador (con contraseña maestra)
   - Al iniciar sesión, se carga su perfil desde Firestore
   - Los permisos se comprueban en tiempo real según su rol
3. El token de FCM se registra automáticamente para recibir notificaciones

## 5. Sistema de Notificaciones

- Las notificaciones se procesan a través de FCMService
- Se crean canales de notificación para Android 8.0+
- Los usuarios reciben notificaciones específicas o generales
- Los administradores pueden enviar notificaciones a usuarios específicos o a todos

## 6. Seguridad

- Las reglas de seguridad de Firestore deben configurarse para restringir el acceso
- Solo los administradores pueden acceder a datos de todos los usuarios
- Los usuarios solo pueden acceder a sus propios datos
- La contraseña maestra para crear administradores debe cambiarse en producción

## 7. Próximos Pasos

- Implementar sincronización de favoritos y búsquedas con Firestore
- Añadir opciones de recuperación de contraseña
- Implementar autenticación con proveedores sociales (Google, Facebook)
- Mejorar la interfaz de usuario para las funciones de Firebase
