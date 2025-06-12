# Water - Aplicación de Recordatorio de Hidratación para Wear OS

Esta es una aplicación para dispositivos vestibles (Wear OS) que ayuda a los usuarios a mantenerse hidratados, recordándoles beber agua a lo largo del día.

## Funcionalidades

### Aplicación Base
- Seguimiento de la ingesta diaria de agua
- Establecimiento de metas diarias personalizables
- Interfaz adaptada para dispositivos vestibles
- Estadísticas de consumo de agua por día

### Integración con Servicios en la Nube
- Configuración de Firebase Cloud Messaging (FCM) para notificaciones push
- Registro automático del dispositivo en Firebase
- Notificaciones enriquecidas con acciones rápidas para registrar consumo de agua

### Funcionalidades Sociales
- Creación y gestión de grupos desde el vestible
- Visualización de amigos conectados en tiempo real (simulado)
- Seguimiento del progreso de hidratación de amigos

### Integración con Aplicación de Administrador
- Recepción de notificaciones enviadas por el administrador
- Actualización de metas mediante comandos remotos

## Implementación Técnica

### Firebase
- Autenticación anónima para identificar dispositivos
- Cloud Messaging para notificaciones push
- Firestore para almacenamiento de datos de usuarios y grupos

### Arquitectura
- Patrón Presentation-Domain-Data
- Componentes de Jetpack Compose para Wear OS
- SwipeDismissableNavHost para navegación

### Notificaciones
- Acciones rápidas en las notificaciones para registrar consumo
- Notificaciones personalizadas según el tipo de mensaje

## Requisitos

- Wear OS 2.0 o superior
- Google Play Services
- Conexión a internet

## Configuración

Para configurar el proyecto, necesitas:

1. Crear un proyecto en Firebase Console
2. Descargar el archivo google-services.json y colocarlo en la carpeta app/
3. Habilitar Firebase Authentication, Firestore y Cloud Messaging