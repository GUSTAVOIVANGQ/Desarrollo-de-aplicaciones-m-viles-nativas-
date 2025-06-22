# Funcionalidad 6: Inicio de sesión y gestión de usuarios

## 📋 Implementación Completada

### Archivos Creados:

1. **Modelos de Usuario**
   - `lib/models/user_model.dart`: Define el modelo de usuario con roles (usuario/administrador)

2. **Servicio de Autenticación**
   - `lib/services/auth_service.dart`: Maneja autenticación con Firebase y modo offline

3. **Pantallas de Autenticación**
   - `lib/screens/login_screen.dart`: Pantalla de inicio de sesión
   - `lib/screens/register_screen.dart`: Pantalla de registro
   - `lib/screens/profile_screen.dart`: Pantalla de perfil del usuario

4. **Componentes de Seguridad**
   - `lib/widgets/auth_guard.dart`: Protege rutas que requieren autenticación

5. **Configuración de Firebase**
   - `android/app/google-services.json`: Configuración de Firebase para Android
   - `lib/firebase_options.dart`: Opciones de configuración de Firebase

### Características Implementadas:

#### 🔐 Sistema de Autenticación
- **Registro de usuarios** con email y contraseña
- **Inicio de sesión** con validación
- **Roles de usuario**: Usuario normal y Administrador
- **Modo offline**: Permite acceso sin internet usando credenciales guardadas
- **Protección de rutas** con AuthGuard

#### 👥 Gestión de Usuarios
- **Perfiles de usuario** con información detallada
- **Distinción de roles** visualizada en la UI
- **Métricas personales** almacenadas por usuario
- **Último acceso** registrado automáticamente

#### 📱 Funcionalidad Offline
- **Cache local** de credenciales para acceso sin internet
- **Sincronización automática** cuando se recupera la conexión
- **Indicadores visuales** del estado de conexión
- **Almacenamiento local** de métricas cuando no hay internet

## 🚀 Configuración Requerida

### Paso 1: Configurar Firebase (Requerido para funcionamiento completo)

1. **Crear proyecto en Firebase Console**:
   - Ve a [Firebase Console](https://console.firebase.google.com/)
   - Crea un nuevo proyecto llamado "flowdiagram-app"
   - Habilita Authentication y Firestore Database

2. **Configurar Authentication**:
   - Ve a Authentication > Sign-in method
   - Habilita "Email/Password"
   - Opcionalmente, configura otros proveedores

3. **Configurar Firestore**:
   - Ve a Firestore Database
   - Crea la base de datos en modo producción
   - Configura las reglas de seguridad:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Permitir a usuarios autenticados acceder a sus propios datos
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Permitir a administradores acceder a todos los datos de usuarios
    match /users/{userId} {
      allow read: if request.auth != null && 
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }
  }
}
```

4. **Descargar archivos de configuración**:
   - Para Android: Descargar `google-services.json` y colocarlo en `android/app/`
   - Para iOS: Descargar `GoogleService-Info.plist` y colocarlo en `ios/Runner/`

### Paso 2: Actualizar Configuración

Reemplaza el contenido de `lib/firebase_options.dart` con la configuración real de tu proyecto Firebase.

### Paso 3: Instalar Dependencias

```bash
flutter pub get
```

### Paso 4: Ejecutar la Aplicación

```bash
flutter run
```

## 🎯 Uso de la Funcionalidad

### Para Usuarios Normales:
1. **Registro**: Crear cuenta con email, contraseña y nombre
2. **Inicio de sesión**: Acceder con credenciales
3. **Perfil**: Ver información personal y métricas
4. **Modo offline**: Acceder sin internet con credenciales guardadas

### Para Administradores:
1. **Registro como admin**: Seleccionar rol "Administrador" durante el registro
2. **Acceso a métricas globales**: Panel de administración (próximamente)
3. **Gestión de usuarios**: Ver información de todos los usuarios

## 🔧 Integración con la App Existente

La funcionalidad de autenticación se integra perfectamente con:

- **Editor de diagramas**: Asocia diagramas al usuario autenticado
- **Base de datos local**: Mantiene sincronización con datos del usuario
- **Métricas**: Registra automáticamente el progreso del usuario
- **Navegación**: Protege rutas sensibles con AuthGuard

## 📊 Métricas Implementadas

El sistema registra automáticamente:
- Fecha de registro
- Último acceso
- Tiempo de uso de la aplicación
- Diagramas creados y editados
- Errores encontrados y corregidos
- Uso de plantillas vs. creación desde cero

## 🔄 Próximos Pasos

1. **Configurar Firebase real** (reemplazar configuración demo)
2. **Implementar panel de administración**
3. **Agregar métricas educativas detalladas**
4. **Implementar sincronización avanzada**
5. **Agregar notificaciones push**

## ⚠️ Notas Importantes

- **Primer uso**: Requiere conexión a internet para registro
- **Modo offline**: Limitado a usuarios ya registrados
- **Sincronización**: Los datos se sincronizan automáticamente al reconectar
- **Seguridad**: Las contraseñas se almacenan encriptadas en Firebase
- **Privacidad**: Los datos del usuario se mantienen privados según las reglas de Firestore
