# Simple AR App 🚀

Una aplicación de Realidad Aumentada (AR) desarrollada en Flutter que permite visualizar modelos 3D sobre la vista de la cámara del dispositivo móvil, sin necesidad de utilizar ARCore.

## 📱 Descripción del Proyecto

**Simple AR App** es una aplicación móvil que simula una experiencia de Realidad Aumentada básica utilizando Flutter. La aplicación permite a los usuarios:

- **Visualizar modelos 3D** directamente sobre la vista de la cámara
- **Rotar modelos** con gestos táctiles para ver todas las caras (frente, atrás, laterales)
- **Escalar modelos** usando controles intuitivos
- **Colocar modelos** en la escena AR de forma fija
- **Cambiar entre diferentes modelos 3D** (Astronauta, Robot, Casco)

### ✨ Características Principales

- 🎥 **Vista de cámara en tiempo real**
- 🎯 **Colocación fija de modelos 3D en el centro**
- 🔄 **Rotación 3D interactiva** con controles táctiles
- 📏 **Escalado dinámico** con botones + y -
- 📍 **Sistema de colocación** para establecer modelos en la escena
- 🎨 **Interfaz intuitiva** con indicadores visuales
- 📱 **Multiplataforma** (Android/iOS)

## 🛠️ Tecnologías Utilizadas

- **Flutter 3.0+** - Framework principal
- **Dart 3.0+** - Lenguaje de programación
- **ModelViewer Plus** - Renderizado de modelos 3D
- **Camera Plugin** - Acceso a la cámara del dispositivo
- **Permission Handler** - Gestión de permisos

## 📋 Requisitos Previos

Antes de ejecutar la aplicación, asegúrate de tener instalado:

- [Flutter SDK](https://flutter.dev/docs/get-started/install) (versión 3.0 o superior)
- [Android Studio](https://developer.android.com/studio) o [VS Code](https://code.visualstudio.com/)
- [Git](https://git-scm.com/)
- Un dispositivo Android (físico o emulador) con cámara
- SDK de Android (API nivel 21 o superior)

### Verificación de la instalación

```bash
flutter doctor
```

Asegúrate de que todos los checkmarks estén en verde.

## 🚀 Instalación y Configuración

### 1. Clonar el repositorio

```bash
git clone https://github.com/GUSTAVOIVANGQ/Desarrollo-de-aplicaciones-m-viles-nativas-/tree/main/Tarea_6_Desarrollo_de_una_Aplicacion_de_Realidad_Aumentada/RA/simple_ar_app.git
cd simple-ar-app
```

### 2. Instalar dependencias

```bash
flutter pub get
```

### 3. Configurar permisos (Android)

Los permisos ya están configurados en `android/app/src/main/AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### 4. Conectar dispositivo o iniciar emulador

**Para dispositivo físico:**
- Habilitar "Opciones de desarrollador"
- Activar "Depuración USB"
- Conectar el dispositivo

**Para emulador:**
```bash
flutter emulators --launch [nombre_emulador]
```

### 5. Ejecutar la aplicación

```bash
flutter run
```

## 📖 Cómo Usar la Aplicación

### Pantalla Principal
1. Abre la aplicación
2. Toca **"Abrir Cámara AR"**
3. Concede permisos de cámara cuando se solicite

### Vista AR
1. **Mostrar modelo**: Toca el botón azul con ícono de AR 👁️
2. **Rotar modelo**: Arrastra directamente sobre el modelo 3D para rotarlo
3. **Escalar modelo**: 
   - Usa los botones **+** (verde) y **-** (naranja)
4. **Colocar en escena**: Toca el botón púrpura 📍 para fijar el modelo
5. **Cambiar modelo**: Toca el botón de intercambio ↔️ en la parte superior

### Indicadores Visuales
- **Borde azul**: Modelo visible pero no colocado
- **Borde verde**: Modelo colocado en la escena
- **"🎯 Modelo en escena"**: Confirmación de colocación

## 🎯 Arquitectura del Proyecto

```
lib/
├── main.dart                    # Punto de entrada y pantalla principal
└── ar_camera_screen.dart        # Pantalla principal de AR

android/
└── app/
    ├── build.gradle.kts         # Configuración de compilación
    └── src/main/
        └── AndroidManifest.xml  # Permisos y configuración

pubspec.yaml                     # Dependencias del proyecto
```

### Componentes Principales

#### `main.dart`
- **HomeScreen**: Pantalla de bienvenida con botón de acceso
- **Gestión de permisos**: Solicita permisos de cámara antes de acceder

#### `ar_camera_screen.dart`
- **ARCameraScreen**: Vista principal de AR
- **Gestión de cámara**: Inicialización y preview
- **Renderizado 3D**: Integración con ModelViewer
- **Controles de usuario**: Botones y gestos táctiles

## 🔧 Dependencias Utilizadas

### Dependencias Principales

| Dependencia | Versión | Propósito |
|-------------|---------|-----------|
| **flutter** | SDK | Framework principal de desarrollo |
| **camera** | ^0.10.5+9 | Acceso y control de la cámara del dispositivo |
| **permission_handler** | ^11.0.1 | Gestión de permisos del sistema (cámara) |
| **model_viewer_plus** | ^1.7.0 | Renderizado y visualización de modelos 3D GLB |
| **cupertino_icons** | ^1.0.2 | Iconos del sistema iOS (compatibilidad) |

### Dependencias de Desarrollo

| Dependencia | Versión | Propósito |
|-------------|---------|-----------|
| **flutter_test** | SDK | Testing y pruebas unitarias |
| **flutter_lints** | ^3.0.0 | Análisis estático y mejores prácticas |

### Detalles de Dependencias

#### 📹 Camera (^0.10.5+9)
- **Función**: Proporciona acceso completo a la cámara del dispositivo
- **Uso**: Vista previa en tiempo real para el fondo AR
- **Características**: Múltiples resoluciones, control de enfoque

#### 🔐 Permission Handler (^11.0.1)
- **Función**: Gestiona permisos del sistema de forma multiplataforma
- **Uso**: Solicitar permisos de cámara antes de acceder
- **Características**: Soporte Android/iOS, manejo de estados

#### 🎯 Model Viewer Plus (^1.7.0)
- **Función**: Renderiza modelos 3D en formato GLB/GLTF
- **Uso**: Mostrar y manipular objetos 3D sobre la cámara
- **Características**: Rotación, escalado, controles táctiles

## 🎨 Modelos 3D Utilizados

La aplicación utiliza modelos 3D de ejemplo proporcionados por [ModelViewer.dev](https://modelviewer.dev/):

1. **Astronauta** - Modelo detallado de astronauta
2. **Robot Expresivo** - Robot animado con expresiones
3. **Casco Dañado** - Casco de batalla con texturas realistas

### Formato de Modelos
- **Formato**: GLB (GL Transmission Format Binary)
- **Fuente**: Repositorio oficial de ModelViewer
- **Carga**: Mediante URLs remotas (requiere conexión a internet)

## 🚧 Desafíos Encontrados y Soluciones

### 1. 🔍 **Búsqueda de Librerías Compatibles**

**Desafío**: Encontrar librerías de AR que funcionaran sin ARCore/ARKit y fueran compatibles con Flutter.

**Problema específico**:
- Las librerías principales de AR (ARCore, ARKit) requieren configuración compleja
- Muchas librerías de modelos 3D no eran compatibles con las versiones recientes de Flutter
- Conflictos de versiones entre dependencias

**Solución implementada**:
```yaml
# Dependencias seleccionadas después de investigación
dependencies:
  camera: ^0.10.5+9              # Estable y bien mantenida
  model_viewer_plus: ^1.7.0      # Fork activo de model_viewer
  permission_handler: ^11.0.1    # Soporte multiplataforma
```

**Proceso de selección**:
1. **Investigación**: Revisión de pub.dev para librerías de modelos 3D
2. **Pruebas**: Testeo de compatibilidad con Flutter 3.0+
3. **Evaluación**: Análisis de documentación y soporte comunitario
4. **Decisión**: Selección de `model_viewer_plus` por ser fork activo

### 2. ⚙️ **Configuración de Build para Android**

**Desafío**: Errores de compilación en Android con Kotlin DSL.

**Problemas encontrados**:
```gradle
// Sintaxis incorrecta inicial
apply plugin: 'com.android.application'  // Error: Groovy en archivo .kts
```

**Solución aplicada**:
```kotlin
// Sintaxis correcta para Kotlin DSL
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("dev.flutter.flutter-gradle-plugin")
}
```

### 3. 🔄 **Implementación de Rotación 3D**

**Desafío**: El modelo se trasladaba horizontalmente en lugar de rotar.

**Problema inicial**:
```dart
// Código que causaba traslación en lugar de rotación
transform: Matrix4.identity()..rotateZ(_modelRotation)
```

**Solución final**:
```dart
// Habilitación de controles nativos del ModelViewer
ModelViewer(
  cameraControls: true,  // Permite rotación nativa
  disableZoom: true,     // Control específico
  disablePan: true,      // Evita traslación
)
```

### 4. 📱 **Gestión de Permisos**

**Desafío**: Acceso consistente a la cámara en diferentes dispositivos.

**Implementación**:
```dart
// Verificación y solicitud de permisos
final status = await Permission.camera.request();
if (status.isGranted) {
  // Proceder con la cámara
} else {
  // Mostrar error al usuario
}
```

## 📊 Estructura de Estados

La aplicación maneja varios estados importantes:

```dart
// Estados principales
bool _isCameraInitialized = false;  // Cámara lista
bool _showModel = false;            // Modelo visible
bool _isModelPlaced = false;        // Modelo colocado en escena
double _modelScale = 1.0;           // Escala del modelo
String _currentModel = '';          // Modelo actualmente seleccionado
```

## 🔮 Futuras Mejoras

### Funcionalidades Planificadas
- [ ] **Detección de superficies** usando sensores del dispositivo
- [ ] **Múltiples modelos** simultáneos en la escena
- [ ] **Animaciones** de los modelos 3D
- [ ] **Captura de pantalla** de la escena AR
- [ ] **Modelos personalizados** cargados por el usuario
- [ ] **Efectos de iluminación** y sombras
- [ ] **Sonidos** y efectos de audio

### Mejoras Técnicas
- [ ] **Optimización de rendimiento** para dispositivos de gama baja
- [ ] **Cache de modelos** para uso offline
- [ ] **Integración con ARCore/ARKit** opcional
- [ ] **Tests automatizados** unitarios e integración

## 🤝 Contribuciones

¡Las contribuciones son bienvenidas! Si deseas contribuir:

1. **Fork** el repositorio
2. Crea una **rama feature** (`git checkout -b feature/nueva-funcionalidad`)
3. **Commit** tus cambios (`git commit -am 'Agrega nueva funcionalidad'`)
4. **Push** a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abre un **Pull Request**

### Guías de Contribución
- Sigue las convenciones de código de Dart/Flutter
- Agrega tests para nuevas funcionalidades
- Actualiza la documentación según sea necesario
- Asegúrate de que `flutter analyze` no reporte errores

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo [LICENSE](LICENSE) para más detalles.

## 👨‍💻 Autor

**Gustavo Ivan Garcia Quiroz**
- GitHub: [@GUSTAVOIVANGQ](https://github.com/GUSTAVOIVANGQ)
- Email: ggarciaq1800@alumno.ipn.mx

## 🙏 Agradecimientos

- **Flutter Team** por el excelente framework
- **ModelViewer.dev** por los modelos 3D de ejemplo
- **Comunidad de Flutter** por las librerías y soporte
- **Google** por las herramientas de desarrollo Android

---

## 🐛 Reporte de Problemas

Si encuentras algún bug o tienes sugerencias:

1. Revisa los [issues existentes](https://github.com/tu-usuario/simple-ar-app/issues)
2. Si no existe, [crea un nuevo issue](https://github.com/tu-usuario/simple-ar-app/issues/new)
3. Proporciona la mayor información posible:
   - Versión de Flutter
   - Dispositivo utilizado
   - Pasos para reproducir el problema
   - Screenshots si es relevante

---

**¡Disfruta explorando la Realidad Aumentada con Simple AR App! 🚀🎯**
