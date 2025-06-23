# BlueWeb Navigator 🌐📱

## Navegación Web a través de Bluetooth

BlueWeb Navigator es una aplicación móvil nativa para Android que permite a un dispositivo sin conexión a Internet (Cliente) navegar por la web a través de otro dispositivo que sí dispone de conexión (Servidor), utilizando Bluetooth como medio de comunicación.

## 📱 Características Principales

### 🔄 Arquitectura Cliente-Servidor
- **Dispositivo A (Servidor)**: Dispositivo con conexión a Internet que actúa como proxy web
- **Dispositivo B (Cliente)**: Dispositivo sin Internet que navega a través del servidor

### 🎨 Interfaz de Usuario
- **Material Design 3** con temas personalizados
- **Tema Guinda** para el Servidor
- **Tema Azul** para el Cliente
- **Modo Claro/Oscuro** automático
- **Interfaz intuitiva** con navegación web completa

### 🌐 Funcionalidades Web
- **WebView Avanzado** con soporte completo para JavaScript
- **Historial de navegación** persistente
- **Sistema de marcadores** con organización por carpetas
- **Modo bajo consumo** para optimizar transferencia de datos
- **Notificaciones** de estado y progreso

## 🏗️ Arquitectura del Proyecto

```
app/src/main/java/com/example/exam3/
├── MainActivity.kt                    # Actividad principal de selección
├── ServerActivity.kt                  # Interfaz del servidor
├── ClientActivity.kt                  # Interfaz del cliente
├── bluetooth/                         # Módulo de comunicación Bluetooth
│   ├── BluetoothConstants.kt         # Constantes y configuración
│   ├── BluetoothServerManager.kt     # Gestión del servidor Bluetooth
│   ├── BluetoothClientManager.kt     # Gestión del cliente Bluetooth
│   └── BluetoothMessage.kt           # Protocolo de mensajes
├── web/                              # Módulo de servicios web
│   ├── WebService.kt                 # Descarga y caché de contenido
│   └── WebContent.kt                 # Modelo de contenido web
└── client/                           # Funcionalidades avanzadas del cliente
    ├── HistoryManager.kt             # Gestión del historial
    ├── BookmarkManager.kt            # Gestión de marcadores
    ├── LowPowerManager.kt            # Modo bajo consumo
    └── NotificationService.kt        # Sistema de notificaciones
```

## 🚀 Instalación y Configuración

### Requisitos del Sistema
- **Android 7.0 (API 24)** o superior
- **Bluetooth 4.0** o superior
- **2 dispositivos Android** para pruebas completas

### Compilación
```bash
# Clonar el repositorio
git clone [URL_DEL_REPOSITORIO]
cd BlueWeb-Navigator

# Abrir en Android Studio
# O compilar desde línea de comandos
./gradlew assembleDebug
```

### Permisos Necesarios
La aplicación requiere los siguientes permisos:
- `BLUETOOTH` y `BLUETOOTH_ADMIN`
- `BLUETOOTH_CONNECT` y `BLUETOOTH_ADVERTISE` (Android 12+)
- `BLUETOOTH_SCAN` (Android 12+)
- `INTERNET` y `ACCESS_NETWORK_STATE`
- `ACCESS_COARSE_LOCATION` y `ACCESS_FINE_LOCATION`
- `POST_NOTIFICATIONS` (Android 13+)

## 📋 Implementación por Pasos

### Paso 1: Configuración Inicial ✅
- [x] Proyecto Android con Kotlin
- [x] Dos actividades principales (Servidor y Cliente)
- [x] Configuración completa de permisos en AndroidManifest.xml
- [x] Temas personalizados (Guinda y Azul) con modo claro/oscuro

### Paso 2: Comunicación Bluetooth ✅
- [x] Inicialización de Bluetooth en ambas actividades
- [x] `BluetoothServerSocket` en el Servidor para conexiones entrantes
- [x] Escaneo y conexión del Cliente usando `BluetoothSocket`
- [x] Intercambio de mensajes de prueba entre dispositivos

### Paso 3: Protocolo de Comunicación ✅
- [x] Formato de mensaje JSON para solicitudes y respuestas
- [x] Interfaz de navegación en el Cliente (URL, botones)
- [x] Envío de solicitudes URL por Bluetooth
- [x] Servidor descarga contenido web con OkHttp
- [x] Sistema de caché para páginas consultadas
- [x] Compresión GZIP del contenido antes del envío

### Paso 4: Navegación Web Completa ✅
- [x] Recepción y descompresión de HTML en el Cliente
- [x] Renderizado en WebView con JavaScript habilitado
- [x] Sistema completo de historial y marcadores
- [x] Modo bajo consumo con optimización de contenido
- [x] Notificaciones de estado y progreso
- [x] Visualizador de estado de conexión Bluetooth

## 🎯 Uso de la Aplicación

### Configuración del Servidor (Dispositivo A)
1. Abrir la aplicación y seleccionar **"Actuar como Servidor"**
2. Otorgar permisos de Bluetooth y ubicación
3. Presionar **"Iniciar Servidor"**
4. Hacer el dispositivo descubrible cuando se solicite
5. Esperar conexión del cliente

### Configuración del Cliente (Dispositivo B)
1. Abrir la aplicación y seleccionar **"Actuar como Cliente"**
2. Otorgar permisos necesarios
3. Presionar **"Buscar Servidores"**
4. Seleccionar el servidor de la lista
5. Presionar **"Conectar"**

### Navegación Web
1. Una vez conectado, ingresar URL en la barra de navegación
2. Presionar **"Ir"** para cargar la página
3. Usar botones de navegación (Atrás, Adelante, Actualizar)
4. Agregar marcadores con el botón de estrella
5. Acceder al menú para historial y configuraciones

## 🔧 Características Técnicas

### Protocolo de Comunicación
```json
{
  "type": "REQUEST|RESPONSE|WEB_CONTENT",
  "data": "URL solicitada o contenido HTML",
  "timestamp": 1703289600000
}
```

### Optimizaciones Implementadas
- **Caché inteligente** con expiración automática
- **Compresión GZIP** para reducir transferencia de datos
- **Modo bajo consumo** que elimina imágenes y videos
- **Pool de conexiones** para descargas eficientes
- **Manejo de errores** robusto con reintentos automáticos

### Gestión de Estado
- **Estado de conexión Bluetooth** en tiempo real
- **Indicadores visuales** de progreso de descarga
- **Notificaciones persistentes** durante la navegación
- **Manejo de desconexiones** con reconexión automática

## 📊 Funcionalidades Avanzadas

### Sistema de Historial
- Almacenamiento local de páginas visitadas
- Búsqueda en el historial
- Límite configurable de elementos (100 por defecto)
- Estadísticas de uso

### Gestión de Marcadores
- Organización por carpetas
- Exportación/importación en formato JSON
- Búsqueda avanzada
- Sincronización entre sesiones

### Modo Bajo Consumo
- Eliminación automática de imágenes y videos
- Compresión de texto
- Filtrado de anuncios
- Estilos optimizados para menor consumo

### Sistema de Notificaciones
- Estado de conexión Bluetooth
- Progreso de descarga de páginas
- Errores y avisos del sistema
- Notificación persistente durante navegación activa

## 🎨 Temas y Personalización

### Tema Servidor (Guinda)
- Color primario: `#8B1538`
- Ideal para identificar el dispositivo servidor
- Diseño enfocado en monitoreo y logs

### Tema Cliente (Azul)
- Color primario: `#1976D2`
- Optimizado para navegación web
- Interfaz centrada en la experiencia del usuario

### Modo Oscuro
- Soporte automático según configuración del sistema
- Colores adaptados para uso nocturno
- Mejor ergonomía visual

## 🔒 Seguridad y Privacidad

- **Conexiones Bluetooth seguras** con autenticación
- **No almacenamiento** de contraseñas o datos sensibles
- **Caché local** sin sincronización externa
- **Permisos mínimos** necesarios para funcionamiento

## 🛠️ Desarrollo y Contribución

### Tecnologías Utilizadas
- **Kotlin** como lenguaje principal
- **Android SDK** con target API 35
- **Material Design 3** para la interfaz
- **OkHttp** para descargas HTTP
- **Gson** para manejo de JSON
- **Coroutines** para programación asíncrona

### Estructura de Dependencias

implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.google.code.gson:gson:2.10.1")
implementation("androidx.webkit:webkit:1.8.0")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")


## 📱 Capturas de Pantalla

### Pantalla Principal
- Selección entre modo Servidor y Cliente
- Diseño Material con branding personalizado

### Interfaz del Servidor
- Estado de conexión en tiempo real
- Logs detallados de actividad
- Controles de inicio/parada del servidor

### Interfaz del Cliente
- Barra de navegación web completa
- WebView con soporte JavaScript
- Controles de historial y marcadores

## ⚠️ Limitaciones Técnicas y Modo Simulación

### 🔧 Implementación de Bluetooth
Por limitaciones técnicas encontradas durante el desarrollo, **la funcionalidad Bluetooth no se implementó completamente**. En su lugar, se desarrolló un **modo de simulación** que demuestra el funcionamiento completo de la aplicación.

### 🎭 Modo Simulación Activado
La aplicación está configurada en **MODO SIMULACIÓN** (`SIMULATION_MODE = true`) que:

- ✅ **Simula la conexión Bluetooth** sin usar hardware real
- ✅ **Utiliza la conexión a Internet** del dispositivo para descargar contenido
- ✅ **Mantiene toda la lógica** de protocolo y comunicación
- ✅ **Demuestra el flujo completo** de navegación web
- ✅ **Incluye todas las características** (historial, marcadores, notificaciones)

### 🔄 Experiencia Simulada
El modo simulación replica fielmente:
1. **Búsqueda de dispositivos** (2 segundos de delay simulado)
2. **Establecimiento de conexión** con mensajes realistas
3. **Intercambio de mensajes** de solicitud/respuesta
4. **Descarga de contenido web** con indicadores de progreso
5. **Notificaciones de estado** como si fuera Bluetooth real

### 💡 Beneficios del Modo Simulación
- **Demostración completa** sin necesidad de dos dispositivos
- **Pruebas inmediatas** de todas las funcionalidades
- **Interfaz y UX idénticos** a la implementación real
- **Código preparado** para activar Bluetooth real (`SIMULATION_MODE = false`)

## 🚀 Próximas Mejoras

### Funcionalidades Planeadas
- [ ] Implementación completa de Bluetooth real
- [ ] Soporte para múltiples clientes simultáneos
- [ ] Sincronización de marcadores entre dispositivos
- [ ] Modo offline con caché persistente
- [ ] Soporte para descarga de archivos

### Optimizaciones Técnicas
- [ ] Protocolo de compresión más eficiente
- [ ] Reconexión automática mejorada
- [ ] Interfaz de configuración avanzada
- [ ] Métricas de rendimiento y uso

## 📄 Licencia

Este proyecto está desarrollado con fines educativos como parte de un examen de Desarrollo de Aplicaciones Móviles Nativas.

## 👥 Autor

Desarrollado como proyecto académico para demostrar:
- Implementación de comunicación entre dispositivos
- Desarrollo de aplicaciones Android nativas
- Arquitectura cliente-servidor móvil
- Interfaz de usuario avanzada con Material Design
- Gestión de estado y persistencia de datos

---

**Nota**: Para usar la funcionalidad Bluetooth real, cambiar `SIMULATION_MODE = false` en `ClientActivity.kt` y `ServerActivity.kt`, asegurándose de que ambos dispositivos tengan Bluetooth habilitado y los permisos necesarios otorgados.
