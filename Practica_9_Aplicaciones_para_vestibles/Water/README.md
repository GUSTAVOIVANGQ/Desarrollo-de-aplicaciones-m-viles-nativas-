# 💧 Water - Sistema de Recordatorio de Hidratación

Sistema completo de hidratación que incluye aplicaciones para dispositivos vestibles (Wear OS) y móviles Android, conectadas a través de Firebase para sincronización en tiempo real y notificaciones sociales.

## 📱 Aplicaciones Incluidas

### 🎯 Wear OS (Water)
Aplicación principal para dispositivos vestibles con interfaz optimizada para pantallas pequeñas.

### 📲 Android Mobile (Water_mobile)
Aplicación complementaria para dispositivos Android convencionales con funcionalidades extendidas.

## ⭐ Funcionalidades Principales

### 📊 Aplicación Base (Ambas Plataformas)
- **Seguimiento de hidratación**: Registro preciso del consumo diario de agua
- **Metas personalizables**: Configuración de objetivos diarios de 1000ml a 5000ml
- **Estadísticas detalladas**: Historial de consumo con visualización por días
- **Interfaz adaptativa**: Diseño específico para cada tipo de dispositivo

### ☁️ Integración con Servicios en la Nube
- **Firebase Cloud Messaging (FCM)**: Sistema completo de notificaciones push
- **Registro automático**: Los dispositivos se registran automáticamente en Firebase
- **Notificaciones enriquecidas**: Acciones rápidas para registrar consumo directamente desde la notificación
- **Sincronización en tiempo real**: Datos compartidos entre vestible y móvil

### 👥 Funcionalidades Sociales
- **Creación de grupos**: Gestión completa de grupos de hidratación desde el vestible
- **Amigos en tiempo real**: Visualización de conexión de usuarios registrados en Firebase (simulado)
- **Notificaciones sociales**: Envío de recordatorios entre usuarios del grupo
- **Estado de actividad**: Seguimiento de usuarios online/offline

### 🔔 Sistema de Notificaciones Avanzado
- **Recordatorios automáticos**: Configurables cada 1-8 horas
- **Notificaciones grupales**: Envío masivo a grupos específicos
- **Mensajes personalizados**: Diferentes tipos de recordatorios motivacionales
- **Acciones rápidas**: Registro de consumo sin abrir la aplicación

## 🏗️ Arquitectura Técnica

### 🔥 Firebase Backend
- **Authentication**: Autenticación anónima para identificación de dispositivos
- **Firestore**: Base de datos NoSQL para usuarios, grupos y estadísticas
- **Cloud Messaging**: Infraestructura de notificaciones push
- **Token Management**: Gestión automática de tokens FCM

### 📱 Wear OS (Kotlin + Jetpack Compose)
```
Water/
├── MainActivity.kt                 # Actividad principal con Compose
├── HydrationManager.kt            # Lógica de negocio
├── HydrationNotificationManager.kt # Gestión de notificaciones
├── GroupsCompose.kt               # UI de grupos sociales
└── theme/                         # Material Design adaptado
```

### 📱 Android Mobile (Kotlin + View System)
```
Water_mobile/
├── MainActivity.kt                # Pantalla principal
├── SettingsActivity.kt           # Configuración avanzada
├── GroupsActivity.kt             # Gestión de grupos
├── NotificationsActivity.kt      # Centro de notificaciones
├── StatsActivity.kt              # Estadísticas detalladas
└── adapters/                     # RecyclerView adapters
```

### 🎨 Diseño e Interfaz
- **Material Design**: Consistencia visual en ambas plataformas
- **Responsive Design**: Adaptación automática a diferentes tamaños de pantalla
- **Wear OS Navigation**: SwipeDismissableNavHost para navegación gestual
- **Progressive UI**: Carga progresiva de contenido

## 🚀 Funcionalidades Específicas por Plataforma

### ⌚ Wear OS Exclusivas
- **Interfaz Circular**: Optimizada para pantallas redondas
- **Navegación Gestual**: Swipe para navegar entre pantallas
- **Acceso Rápido**: Registro de agua con un toque
- **Notificaciones Locales**: Sistema de recordatorios independiente

### 📱 Android Mobile Exclusivas
- **Dashboard Completo**: Vista general con estadísticas avanzadas
- **Gestión de Grupos**: Creación, edición y administración completa
- **Centro de Notificaciones**: Envío de recordatorios a usuarios y grupos
- **Configuración Avanzada**: Ajustes detallados y personalización
- **Estadísticas Visuales**: Gráficos y análisis de progreso

## 🔧 Configuración e Instalación

### Prerrequisitos
- Android Studio Arctic Fox o superior
- Wear OS 2.0+ (para vestible)
- Android 7.0+ (API 24+) para móvil
- Google Play Services
- Cuenta de Firebase

### Configuración Firebase
1. **Crear proyecto Firebase**:
   ```
   - Ir a Firebase Console
   - Crear nuevo proyecto "water-hydration"
   - Habilitar Authentication, Firestore, FCM
   ```

2. **Configurar aplicaciones**:
   ```
   - Registrar app vestible: com.example.water
   - Registrar app móvil: com.example.water_mobile
   - Descargar google-services.json para cada app
   ```

3. **Estructura de Firestore**:
   ```
   /users/{userId}
     - name: string
     - deviceType: "wearable" | "mobile"
     - isActive: boolean
     - dailyGoal: number
     - dailyIntake: number
   
   /groups/{groupId}
     - name: string
     - members: array
     - createdBy: string
     - createdAt: timestamp
   ```

### Instalación
```bash
# Clonar repositorio
git clone [repository-url]

# Compilar aplicación vestible
cd Water/
./gradlew assembleDebug

# Compilar aplicación móvil
cd ../Water_mobile/
./gradlew assembleDebug

# Instalar en dispositivos
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 📋 Casos de Uso

### 👤 Usuario Individual
1. **Registro inicial**: El dispositivo se registra automáticamente en Firebase
2. **Configuración personal**: Establecer meta diaria y intervalos de recordatorio
3. **Uso diario**: Registrar consumo de agua y recibir notificaciones
4. **Seguimiento**: Consultar estadísticas y progreso

### 👥 Usuario Social
1. **Crear grupo**: Formar grupo de hidratación con amigos
2. **Invitar miembros**: Añadir usuarios mediante ID de Firebase
3. **Enviar recordatorios**: Motivar a amigos con notificaciones grupales
4. **Monitorear progreso**: Ver estado de conexión y progreso del grupo

### 🎯 Administrador de Grupo
1. **Gestión completa**: Usar aplicación móvil para administración avanzada
2. **Notificaciones masivas**: Enviar recordatorios a múltiples grupos
3. **Análisis grupal**: Seguimiento de participación y engagement
4. **Configuración grupal**: Establecer metas y recordatorios para el grupo

## 🔒 Seguridad y Privacidad

- **Autenticación anónima**: No se requiere información personal
- **Datos mínimos**: Solo se almacena información esencial de hidratación
- **Tokens seguros**: Gestión segura de tokens FCM
- **Firestore Rules**: Reglas de seguridad para proteger datos

## 🚀 Futuras Mejoras

- [ ] Integración con sensores de salud
- [ ] Análisis predictivo de hidratación
- [ ] Gamificación con logros y recompensas
- [ ] Integración con otras apps de salud
- [ ] Widget para pantalla de inicio
- [ ] Wear OS Tiles para acceso rápido

## 🛠️ Tecnologías Utilizadas

**Frontend**:
- Kotlin
- Jetpack Compose (Wear OS)
- Android View System (Mobile)
- Material Design Components

**Backend**:
- Firebase Authentication
- Firebase Firestore
- Firebase Cloud Messaging
- Firebase Functions (futuro)

**Herramientas**:
- Android Studio
- Gradle Build System
- Git Version Control

Este sistema proporciona una solución completa de hidratación que combina la conveniencia de los dispositivos vestibles con la funcionalidad completa de las aplicaciones móviles, todo sincronizado en tiempo real a través de Firebase.