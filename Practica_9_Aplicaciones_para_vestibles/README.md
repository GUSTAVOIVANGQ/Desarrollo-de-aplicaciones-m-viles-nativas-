# 💧 Práctica 9: Aplicaciones para Vestibles - Sistema de Hidratación

**Desarrollo de Aplicaciones Móviles Nativas**

Sistema completo de recordatorio de hidratación que incluye aplicaciones sincronizadas para dispositivos vestibles (Wear OS) y móviles Android, conectadas mediante Firebase para funcionalidades sociales y notificaciones en tiempo real.

## 📋 Descripción del Proyecto

Este proyecto implementa un ecosistema completo de aplicaciones de hidratación que demuestra:

- **Desarrollo nativo para Wear OS** con Jetpack Compose
- **Aplicación complementaria Android** con interfaz completa
- **Integración Firebase** para backend en la nube
- **Sistema de notificaciones push** con FCM
- **Funcionalidades sociales** con grupos y seguimiento de amigos
- **Sincronización en tiempo real** entre dispositivos

## 🗂️ Estructura del Proyecto

```
Practica_9_Aplicaciones_para_vestibles/
├── README.md                          # Este archivo
├── AndroidPractica9REQ.pdf           # Requisitos del proyecto
├── Water.mp4                         # Demo aplicación vestible
├── Water mobile.mp4                  # Demo aplicación móvil
├── Water/                            # 📱 Aplicación Wear OS
│   ├── app/src/main/java/com/example/water/
│   │   └── presentation/
│   │       ├── MainActivity.kt       # UI principal Compose
│   │       ├── HydrationManager.kt   # Lógica de negocio
│   │       └── GroupsCompose.kt      # Funcionalidades sociales
│   ├── google-services.json         # Configuración Firebase
│   └── README.md                    # Documentación Wear OS
└── Water_mobile/                    # 📲 Aplicación Android
    ├── app/src/main/java/com/example/water_mobile/
    │   ├── MainActivity.kt          # Pantalla principal
    │   ├── SettingsActivity.kt     # Configuración
    │   ├── GroupsActivity.kt       # Gestión de grupos
    │   ├── NotificationsActivity.kt # Centro notificaciones
    │   └── StatsActivity.kt        # Estadísticas
    ├── google-services.json        # Configuración Firebase
    └── README.md                   # Documentación Android
```

## ⭐ Funcionalidades Implementadas

### 🎯 2. Desarrollo de la Aplicación Base

#### Wear OS:
- ✅ Seguimiento de consumo diario de agua
- ✅ Metas personalizables (1000-5000ml)
- ✅ Interfaz Jetpack Compose adaptada a pantallas circulares
- ✅ Navegación gestual con SwipeDismissableNavHost
- ✅ Botones de registro rápido (250ml, 500ml, 750ml)

#### Android Mobile:
- ✅ Dashboard completo con estadísticas visuales
- ✅ Configuración avanzada de metas y recordatorios
- ✅ Historial semanal con gráficos de progreso
- ✅ Material Design responsivo

### ☁️ 3. Integración con Servicios en la Nube

#### 3.1 Firebase Cloud Messaging (FCM):
- ✅ **Registro automático** de dispositivos vestibles en Firebase
- ✅ **Autenticación anónima** para identificación única
- ✅ **Tokens FCM** gestionados automáticamente
- ✅ **Sincronización** de datos entre vestible y móvil

#### 3.2 Notificaciones Enriquecidas:
- ✅ **Acciones específicas** en notificaciones (registrar agua)
- ✅ **Notificaciones personalizadas** según tipo de mensaje
- ✅ **Respuesta directa** sin abrir la aplicación
- ✅ **Iconos y colores** adaptativos por contexto

### 👥 4. Funcionalidades Sociales

#### Creación de Grupos desde Vestible:
- ✅ **Interfaz Compose** para crear grupos
- ✅ **Gestión completa** desde la muñeca
- ✅ **Validación en tiempo real** de nombres de grupo
- ✅ **Sincronización inmediata** con Firebase Firestore

#### Visualización de Amigos en Tiempo Real:
- ✅ **Conexión simulada** pero visualmente realista
- ✅ **Estado online/offline** con indicadores visuales
- ✅ **Lista de amigos** actualizada en tiempo real
- ✅ **Usuarios reales** registrados en Firebase
- ✅ **Avatares y nombres** dinámicos

### 🔔 5. Sistema de Notificaciones Avanzado

#### Firebase Cloud Messaging:
- ✅ **Envío push** desde aplicación móvil a vestibles
- ✅ **Base de datos Firebase** con usuarios registrados
- ✅ **Recordatorios grupales** e individuales
- ✅ **Mensajes motivacionales** personalizados

#### Tipos de Notificaciones:
- ✅ **Recordatorios individuales** a usuarios específicos
- ✅ **Notificaciones grupales** a grupos completos
- ✅ **Recordatorio rápido** masivo a todos los grupos
- ✅ **Notificaciones locales** para testing (vestible)

### 📱 6. Aplicación Complementaria Android

#### Funcionalidades Principales:
- ✅ **Replica todas las funciones** del vestible
- ✅ **Interfaz adaptada** a pantallas grandes
- ✅ **Gestión avanzada** de grupos y notificaciones
- ✅ **Dashboard completo** con estadísticas detalladas
- ✅ **Centro de notificaciones** para envío masivo

#### Características Exclusivas:
- ✅ **Configuración avanzada** de intervalos y metas
- ✅ **Estadísticas visuales** con gráficos semanales
- ✅ **Gestión de miembros** de grupos
- ✅ **Envío de recordatorios** personalizados
- ✅ **Monitoreo en tiempo real** de amigos conectados

## 🏗️ Arquitectura Técnica

### Frontend:
- **Wear OS**: Kotlin + Jetpack Compose + Wear Compose
- **Android**: Kotlin + Android Views + Material Components
- **Navegación**: Navigation Component + SwipeDismissable

### Backend:
- **Firebase Authentication**: Autenticación anónima
- **Firebase Firestore**: Base de datos NoSQL en tiempo real
- **Firebase Cloud Messaging**: Infraestructura de push notifications
- **Firebase Security Rules**: Protección de datos

### Patrones de Diseño:
- **MVVM**: Separación de responsabilidades
- **Repository Pattern**: Gestión centralizada de datos
- **Observer Pattern**: Actualizaciones en tiempo real
- **Singleton**: Managers globales (HydrationManager, GroupManager)

## 🚀 Instalación y Configuración

### Prerrequisitos:
```bash
- Android Studio Arctic Fox+
- Wear OS 2.0+ (para vestible)
- Android 7.0+ API 24+ (para móvil)
- Google Play Services
- Cuenta Firebase (gratuita)
```

### Configuración Firebase:
1. **Crear proyecto**: Ir a [Firebase Console](https://console.firebase.google.com)
2. **Habilitar servicios**: Authentication, Firestore, Cloud Messaging
3. **Registrar apps**:
   - Vestible: `com.example.water`
   - Móvil: `com.example.water_mobile`
4. **Descargar configuración**: `google-services.json` para cada app

### Compilación:
```bash
# Aplicación vestible
cd Water/
./gradlew assembleDebug

# Aplicación móvil
cd Water_mobile/
./gradlew assembleDebug
```

### Instalación:
```bash
# En dispositivo vestible
adb install Water/app/build/outputs/apk/debug/app-debug.apk

# En dispositivo móvil
adb install Water_mobile/app/build/outputs/apk/debug/app-debug.apk
```

## 📊 Casos de Uso Demostrados

### 🎯 Usuario Individual:
1. **Registro automático** al abrir la aplicación
2. **Configuración personal** de metas diarias
3. **Uso diario** con registro rápido de consumo
4. **Recepción de recordatorios** automáticos

### 👥 Usuario Social:
1. **Crear grupo** desde el vestible
2. **Ver amigos online** en tiempo real
3. **Recibir recordatorios** de otros usuarios
4. **Enviar motivación** a amigos

### 📱 Administrador (App Móvil):
1. **Gestión completa** de grupos
2. **Envío masivo** de notificaciones
3. **Monitoreo** de actividad grupal
4. **Configuración avanzada** del sistema

## 🎯 Características Destacadas

### 💡 Innovaciones Técnicas:
- **Jetpack Compose para Wear OS**: Implementación moderna de UI
- **Notificaciones enriquecidas**: Acciones directas sin abrir app
- **Sincronización real-time**: Firebase Firestore listeners
- **Arquitectura híbrida**: Vestible + móvil sincronizados

### 🎨 Experiencia de Usuario:
- **Interfaz intuitiva**: Diseño específico para cada plataforma
- **Navegación gestual**: Swipe natural en vestibles
- **Feedback inmediato**: Animaciones y confirmaciones
- **Consistencia visual**: Material Design coherente

### 🔒 Seguridad y Rendimiento:
- **Autenticación anónima**: Sin datos personales sensibles
- **Reglas de seguridad**: Firestore rules para protección
- **Gestión eficiente**: Caché local y sincronización inteligente
- **Optimización de batería**: Uso responsable de recursos

## 📱 Demos Incluidos

- **`Water.mp4`**: Demostración completa de la aplicación vestible
- **`Water mobile.mp4`**: Recorrido por todas las funciones móviles

## 🎓 Objetivos Académicos Cumplidos

✅ **Desarrollo nativo Wear OS** con tecnologías modernas  
✅ **Integración Firebase** completa y funcional  
✅ **Sistema de notificaciones** robusto y escalable  
✅ **Funcionalidades sociales** con tiempo real simulado  
✅ **Aplicación complementaria** con todas las funciones  
✅ **Arquitectura profesional** siguiendo mejores prácticas  
✅ **Documentación completa** y casos de uso demostrados  

Este proyecto demuestra un dominio completo del desarrollo de aplicaciones vestibles modernas, integrando servicios en la nube, funcionalidades sociales y sincronización multi-dispositivo en un ecosistema funcional y escalable.
