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

### 🌐 Funciones de Navegador
- **Botones de Navegación**: Atrás, Adelante, Recargar (interfaz visual)
- **Marcadores**: Botón para gestión de favoritos (interfaz visual)
- **Historial**: Acceso al historial de navegación (interfaz visual)

### 🔔 Notificaciones Push Locales
- **Sincronización**: Cada Toast genera una notificación push local automáticamente
- **Canal Personalizado**: Canal de notificaciones específico para la aplicación
- **Compatibilidad**: Soporte completo para Android 8.0+ y permisos de Android 13+

## 📋 Requisitos del Sistema

- **Android**: API Level 24+ (Android 7.0)
- **Bluetooth**: Hardware Bluetooth requerido
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
- **Botones de Navegador**: Funciones visuales que muestran Toast y notificaciones
- **Cambio de Tema**: Alternativa entre temas IPN y ESCOM

## 🏗️ Arquitectura del Proyecto

```
app/
├── src/main/
│   ├── java/com/example/bluetoothhtml/
│   │   └── MainActivity.java          # Actividad principal
│   ├── res/
│   │   ├── layout/
│   │   │   └── activity_main.xml      # Diseño de la interfaz
│   │   ├── values/
│   │   │   ├── colors.xml             # Paleta de colores
│   │   │   ├── strings.xml            # Cadenas de texto
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
- Verifica la conexión a Internet
- Comprueba que la URL sea válida
- Asegúrate de que el servidor responda

## 📱 Compatibilidad

| Característica | Versión Mínima | Notas |
|----------------|----------------|-------|
| Android | 7.0 (API 24) | Funcionalidad básica |
| Bluetooth LE | 4.0+ | Para mejor rendimiento |
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

- 📧 Email: support@example.com
- 🐛 Issues: [GitHub Issues](https://github.com/usuario/bluetoothhtml/issues)
- 📖 Wiki: [Documentación](https://github.com/usuario/bluetoothhtml/wiki)

---

**Nota**: Esta aplicación fue desarrollada con fines educativos como parte del desarrollo de aplicaciones móviles nativas.
