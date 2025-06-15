# Aplicación de Hidratación para Android Móvil

Esta es la aplicación complementaria para dispositivos Android convencionales de la aplicación de hidratación para vestibles.

## Funcionalidades Implementadas

### 🏠 Pantalla Principal
- **Seguimiento de consumo diario**: Visualización del progreso de hidratación con barra de progreso
- **Botones de adición rápida**: 250ml, 500ml, 750ml para registrar consumo fácilmente
- **Estadísticas de la semana**: Resumen del progreso semanal
- **Amigos conectados en tiempo real**: Lista de amigos online con estado en vivo

### ⚙️ Configuración
- **Meta diaria personalizable**: Ajuste de 1000ml a 5000ml
- **Intervalo de recordatorios**: Configuración de 1 a 8 horas
- **Reinicio de datos**: Opción para resetear el consumo diario

### 👥 Gestión de Grupos
- **Creación de grupos**: Crear nuevos grupos de hidratación
- **Visualización de grupos**: Lista de todos los grupos del usuario
- **Gestión de miembros**: Añadir y ver miembros de cada grupo
- **Estado en tiempo real**: Monitoreo de conexión de miembros

### 🔔 Sistema de Notificaciones
- **Recordatorios individuales**: Enviar recordatorios a amigos específicos
- **Recordatorios grupales**: Enviar recordatorios a grupos completos
- **Recordatorio rápido**: Envío masivo a todos los grupos
- **Mensajes personalizables**: Selección de diferentes mensajes motivacionales

### 📊 Estadísticas
- **Progreso diario detallado**: Información completa del día actual
- **Historial semanal**: Visualización de los últimos 7 días con gráficos
- **Porcentajes de cumplimiento**: Seguimiento de metas alcanzadas

### 🔥 Integración con Firebase
- **Autenticación anónima**: Registro automático del dispositivo
- **Firestore Database**: Almacenamiento de perfiles y grupos
- **Firebase Cloud Messaging**: Sistema completo de notificaciones push
- **Estado en tiempo real**: Sincronización de conexión de usuarios

## Características Técnicas

### 🎨 Interfaz de Usuario
- **Material Design**: Diseño moderno siguiendo las guías de Google
- **Cards y layouts responsivos**: Interfaz adaptativa y atractiva
- **Colores temáticos**: Paleta centrada en tonos azules (agua)
- **Iconografía consistente**: Iconos vectoriales coherentes

### 🏗️ Arquitectura
- **MVVM Pattern**: Separación clara de responsabilidades
- **Repository Pattern**: Gestión centralizada de datos
- **Coroutines**: Programación asíncrona para mejor rendimiento
- **RecyclerView**: Listas eficientes y optimizadas

### 💾 Gestión de Datos
- **SharedPreferences**: Almacenamiento local de configuraciones
- **Firebase Firestore**: Base de datos en la nube
- **Caché local**: Datos persistentes offline
- **Sincronización automática**: Actualización en tiempo real

## Funcionalidades Complementarias

### 🔗 Conectividad con Vestible
La aplicación está diseñada para trabajar en conjunto con la aplicación del vestible:
- Comparte la misma base de datos Firebase
- Recibe notificaciones del vestible
- Sincroniza datos de hidratación entre dispositivos

### 📱 Notificaciones Push
- Recepción de recordatorios del vestible
- Notificaciones de metas alcanzadas por amigos
- Recordatorios automáticos programados
- Respuesta a notificaciones con acciones rápidas

### 👫 Sistema Social
- Visualización de amigos online en tiempo real
- Grupos de hidratación colaborativos
- Envío de motivación entre usuarios
- Estado de conexión simulado pero funcional

## Instalación y Configuración

1. **Configurar Firebase**:
   - Asegurar que `google-services.json` esté en la carpeta `app/`
   - Verificar configuración de FCM
   - Configurar Firestore Database

2. **Compilar la aplicación**:
   ```bash
   ./gradlew assembleDebug
   ```

3. **Instalar en dispositivo**:
   ```bash
   ./gradlew installDebug
   ```

## Estructura del Proyecto

```
app/src/main/java/com/example/water_mobile/
├── MainActivity.kt                 # Pantalla principal
├── SettingsActivity.kt            # Configuración
├── GroupsActivity.kt              # Lista de grupos
├── GroupDetailActivity.kt         # Detalle de grupo
├── NotificationsActivity.kt       # Envío de notificaciones
├── StatsActivity.kt               # Estadísticas
├── HydrationManager.kt            # Gestión de datos de hidratación
├── GroupManager.kt                # Gestión de grupos y amigos
├── MobileNotificationManager.kt   # Gestión de notificaciones
├── MobileFirebaseMessagingService.kt # Servicio FCM
└── adapters/                      # Adaptadores RecyclerView
```

## Tecnologías Utilizadas

- **Kotlin**: Lenguaje principal de desarrollo
- **Android Jetpack**: Components para arquitectura moderna
- **Firebase**: Suite completa de backend
- **Material Design Components**: UI/UX moderna
- **Coroutines**: Programación asíncrona
- **RecyclerView**: Listas eficientes

La aplicación móvil complementa perfectamente la funcionalidad del vestible, proporcionando una interfaz completa y rica para la gestión de hidratación, grupos sociales y notificaciones, manteniendo la sincronización y conectividad en tiempo real con Firebase.
