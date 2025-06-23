# 📱 BluetoothHTML - Compartir HTML por Bluetooth

Una aplicación Android que permite compartir contenido HTML entre dispositivos a través de Bluetooth, con temas personalizables y notificaciones push locales.

## 🚀 Características Principales

### 📡 Comunicación Bluetooth
- **Servidor Bluetooth**: Un dispositivo actúa como servidor para recibir conexiones
- **Cliente Bluetooth**: Conecta a servidores disponibles para enviar URLs
- **Descarga de HTML**: Descarga automáticamente el contenido HTML de las URLs compartidas
- **Visualización**: Muestra el HTML recibido en un WebView integrado

### 🎨 Temas Personalizables
- **Tema IPN Guinda**: Colores representativos del Instituto Politécnico Nacional
- **Tema ESCOM Azul**: Colores representativos de la Escuela Superior de Cómputo
- **Modo Automático**: Responde automáticamente al modo claro/oscuro del sistema
- **Cambio Dinámico**: Cambio de tema en tiempo real sin reiniciar la aplicación

### 🌐 Funciones de Navegador Completas
- **Navegación Local**: Botones Atrás/Adelante totalmente funcionales sin conexión Bluetooth
- **Almacenamiento HTML**: Todo el contenido HTML recibido se almacena localmente para navegación offline
- **Sistema de Marcadores**: Gestión completa de sitios favoritos con opciones para agregar/eliminar
- **Historial Completo**: Acceso y navegación por todo el historial de sitios visitados
- **Recarga Local**: Botón de recarga que funciona con contenido almacenado localmente
- **Persistencia de Datos**: Historial y marcadores se mantienen entre sesiones de la aplicación

### 🔔 Notificaciones Push Locales
- **Sincronización**: Cada Toast genera una notificación push local automáticamente
- **Canal Personalizado**: Canal de notificaciones específico para la aplicación
- **Compatibilidad**: Soporte completo para Android 8.0+ y permisos de Android 13+

## 📋 Requisitos del Sistema

- **Android**: API Level 24+ (Android 7.0)
- **Bluetooth**: Hardware Bluetooth requerido para comunicación entre dispositivos
- **Almacenamiento**: Espacio local para almacenar HTML y datos de navegación
- **Permisos**: Bluetooth, Ubicación, Internet, Notificaciones

## 🛠️ Instalación

### Prerequisitos
- Android Studio Arctic Fox o superior
- JDK 11 o superior
- Dispositivos Android con Bluetooth

### Pasos de Instalación

1. **Clonar el repositorio**
```bash
git clone https://github.com/usuario/bluetoothhtml.git
cd bluetoothhtml
```

2. **Abrir en Android Studio**
- Abre Android Studio
- Selecciona "Open an existing project"
- Navega al directorio del proyecto y ábrelo

3. **Sincronizar dependencias**
```bash
./gradlew build
```

4. **Instalar en dispositivo**
- Conecta tu dispositivo Android
- Ejecuta la aplicación desde Android Studio o:
```bash
./gradlew installDebug
```

## 🎯 Uso de la Aplicación

### Configuración Inicial
1. **Conceder Permisos**: La aplicación solicitará permisos de Bluetooth, ubicación y notificaciones
2. **Activar Bluetooth**: Si no está activado, se solicitará habilitarlo
3. **Seleccionar Tema**: Usa el botón "Cambiar Tema" para elegir entre IPN Guinda o ESCOM Azul

### Modo Servidor
1. Presiona **"Ser Servidor"**
2. El dispositivo se vuelve visible durante 5 minutos
3. Espera conexiones de otros dispositivos
4. Recibe URLs y muestra el HTML descargado

### Modo Cliente
1. Presiona **"Buscar Servidor"**
2. Selecciona un dispositivo de la lista
3. Ingresa una URL en el campo de texto
4. Envía la URL al servidor seleccionado

### Funciones Adicionales
- **Refrescar Dispositivos**: Busca nuevos dispositivos Bluetooth
- **Navegación Offline**: Los botones del navegador funcionan completamente sin conexión Bluetooth
- **Gestión de Marcadores**: Sistema completo para guardar y organizar sitios favoritos
- **Historial Navegable**: Acceso rápido a cualquier página visitada anteriormente
- **Cambio de Tema**: Alternativa entre temas IPN y ESCOM

### Funcionalidades del Navegador Local
1. **Atrás/Adelante**: Navega por el historial de páginas HTML almacenadas localmente
2. **Recargar**: Recarga la página actual desde el almacenamiento local
3. **Marcadores**: 
   - Agregar sitios actuales como favoritos
   - Navegar directamente a sitios marcados
   - Eliminar marcadores no deseados
4. **Historial**:
   - Ver lista completa de sitios visitados
   - Navegar a cualquier página del historial
   - Opción para limpiar todo el historial

## 🗂️ Almacenamiento y Navegación Local

### Sistema de Almacenamiento
La aplicación utiliza `SharedPreferences` para almacenar:
- **HTML Content**: Todo el contenido HTML recibido se guarda localmente
- **Historial de Navegación**: Lista de URLs visitadas con timestamps
- **Marcadores**: URLs favoritas del usuario
- **Configuraciones**: Temas y preferencias de la aplicación

### Funcionalidades de Navegación Offline
```java
// Navegación sin conexión Bluetooth
- Botón Atrás: currentHistoryIndex--
- Botón Adelante: currentHistoryIndex++
- Recarga: Desde almacenamiento local
- Marcadores: Acceso directo a sitios guardados
```

### Flujo de Datos
```
1. Cliente solicita URL → Servidor Bluetooth
2. Servidor descarga HTML → Envía a Cliente
3. Cliente recibe HTML → Almacena localmente
4. Cliente agrega al historial → Actualiza navegación
5. Usuario navega → Usa contenido local (SIN Bluetooth)
```

## 🏗️ Arquitectura del Proyecto

```
app/
├── src/main/
│   ├── java/com/example/bluetoothhtml/
│   │   └── MainActivity.java          # Actividad principal con navegador completo
│   ├── res/
│   │   ├── layout/
│   │   │   └── activity_main.xml      # Diseño de la interfaz con botones funcionales
│   │   ├── drawable/                  # Recursos gráficos para botones
│   │   │   ├── rounded_button.xml
│   │   │   ├── rounded_reload_button.xml
│   │   │   ├── rounded_bookmark_button.xml
│   │   │   └── rounded_history_button.xml
│   │   ├── values/
│   │   │   ├── colors.xml             # Paleta de colores
│   │   │   ├── strings.xml            # Cadenas de texto (incluye navegador)
│   │   │   └── themes.xml             # Temas claro
│   │   └── values-night/
│   │       └── themes.xml             # Temas oscuro
│   └── AndroidManifest.xml            # Configuración y permisos
```

## 🎨 Personalización de Temas

### Tema IPN Guinda
```xml
<!-- Colores principales -->
<color name="ipn_guinda_primary">#8B1538</color>
<color name="ipn_guinda_secondary">#FFD700</color>
```

### Tema ESCOM Azul
```xml
<!-- Colores principales -->
<color name="escom_azul_primary">#1976D2</color>
<color name="escom_azul_secondary">#FFC107</color>
```

## 🔧 Configuración de Desarrollo

### Dependencias Principales
```gradle
dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
}
```

### Permisos Requeridos
```xml
<!-- Bluetooth -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />

<!-- Ubicación -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- Otros -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

## 🐛 Solución de Problemas

### Problemas Comunes

**Bluetooth no funciona**
- Verifica que los permisos estén concedidos
- Asegúrate de que Bluetooth esté activado
- Verifica compatibilidad del dispositivo

**No se pueden descubrir dispositivos**
- Activa la ubicación en el dispositivo
- Asegúrate de que ambos dispositivos sean visibles
- Reinicia Bluetooth si es necesario

**HTML no se muestra correctamente**
- Verifica la conexión a Internet (solo para descarga inicial)
- Comprueba que la URL sea válida
- Asegúrate de que el servidor responda
- Para páginas ya visitadas, usa la navegación local

**Los botones de navegación no funcionan**
- Verifica que hay historial de navegación disponible
- Los botones Atrás/Adelante se deshabilitan cuando no hay páginas disponibles
- Asegúrate de haber recibido al menos una página HTML

**Marcadores o historial perdido**
- Los datos se almacenan localmente y persisten entre sesiones
- Si el almacenamiento se corrompe, se puede limpiar desde los botones correspondientes
- Verifica permisos de almacenamiento de la aplicación

## 📱 Compatibilidad

| Característica | Versión Mínima | Notas |
|----------------|----------------|-------|
| Android | 7.0 (API 24) | Funcionalidad básica |
| Bluetooth LE | 4.0+ | Para comunicación entre dispositivos |
| Almacenamiento Local | N/A | SharedPreferences para navegación offline |
| Navegación Offline | N/A | Funciona sin conexión después de recibir HTML |
| Notificaciones | 8.0 (API 26) | Canales de notificación |
| Permisos Dinámicos | 13.0 (API 33) | Notificaciones |

## 🤝 Contribución

1. **Fork** el proyecto
2. Crea una **rama** para tu feature (`git checkout -b feature/AmazingFeature`)
3. **Commit** tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. **Push** a la rama (`git push origin feature/AmazingFeature`)
5. Abre un **Pull Request**

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - consulta el archivo [LICENSE.md](LICENSE.md) para más detalles.

## 👥 Autores

- **Desarrollador Principal** - *Trabajo inicial* - [Tu Usuario](https://github.com/tuusuario)

## 🙏 Agradecimientos

- Instituto Politécnico Nacional (IPN)
- Escuela Superior de Cómputo (ESCOM)
- Comunidad Android Developer

## 📞 Soporte

¿Tienes preguntas o problemas? 

- 📧 Email: ggarciaq1800@alumno.ipn.mx
- 🐛 Issues: [GitHub Issues](https://github.com/usuario/bluetoothhtml/issues)
- 📖 Wiki: [Documentación](https://github.com/usuario/bluetoothhtml/wiki)

---

**Nota**: Esta aplicación fue desarrollada con fines educativos como parte del desarrollo de aplicaciones móviles nativas. La versión 2.0 incluye funcionalidades completas de navegador offline que mejoran significativamente la experiencia del usuario.

## 🔄 Nuevas Funcionalidades v2.0

### ✨ Lo Nuevo en Esta Versión

#### 🌐 Navegador Completo Offline
- **Navegación Sin Bluetooth**: Una vez recibido el HTML, puedes navegar completamente offline
- **Almacenamiento Inteligente**: Todo el contenido HTML se guarda automáticamente
- **Historial Persistente**: El historial se mantiene entre sesiones de la aplicación

#### 📚 Gestión Avanzada de Contenido
- **Marcadores Funcionales**: Sistema completo de favoritos con gestión visual
- **Historial Navegable**: Acceso rápido a cualquier página visitada
- **Navegación Bidireccional**: Botones Atrás/Adelante completamente funcionales

#### 🎯 Mejoras de Experiencia de Usuario
- **Botones Inteligentes**: Se deshabilitan visualmente cuando no están disponibles
- **Gestión de Datos**: Opciones para limpiar historial y eliminar marcadores
- **Interfaz Mejorada**: Diálogos intuitivos para todas las funciones

### 🚀 Flujo de Trabajo Mejorado

1. **Comunicación Bluetooth** (igual que antes)
   - Servidor/Cliente se conectan por Bluetooth
   - Se envía URL y se recibe HTML

2. **Almacenamiento Automático** (NUEVO)
   - HTML se guarda localmente automáticamente
   - URL se agrega al historial
   - Navegación se actualiza

3. **Navegación Offline** (NUEVO)
   - Botones Atrás/Adelante funcionan sin Bluetooth
   - Acceso rápido desde historial y marcadores
   - Recarga desde almacenamiento local
