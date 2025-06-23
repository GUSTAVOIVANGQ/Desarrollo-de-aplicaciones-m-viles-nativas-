# 📄 Document Scanner - Aplicación Android con ML Kit

Una aplicación Android moderna desarrollada en **Kotlin** que utiliza **ML Kit Document Scanner API** de Google y **Jetpack Compose** para escanear documentos y convertirlos a PDF de manera inteligente.

## 🚀 Características

- ✅ **Escaneo inteligente de documentos** usando ML Kit
- ✅ **Interfaz moderna** con Jetpack Compose
- ✅ **Conversión automática a PDF**
- ✅ **Importación desde galería**
- ✅ **Visualización de documentos escaneados**
- ✅ **Modo de escaneo completo**
- ✅ **Almacenamiento seguro con FileProvider**

## 🛠️ Stack Tecnológico

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| **Kotlin** | 1.9.0 | Lenguaje de programación principal |
| **Android Gradle Plugin** | 8.3.0 | Sistema de construcción |
| **Jetpack Compose** | 2023.08.00 | Framework de UI moderna |
| **ML Kit Document Scanner** | 16.0.0-beta1 | API de escaneo de documentos |
| **Material 3** | Latest | Diseño Material Design 3 |
| **Android Core KTX** | 1.12.0 | Extensiones de Kotlin para Android |

## 📋 Requisitos del Sistema

- **Android SDK mínimo**: API 24 (Android 7.0)
- **Android SDK objetivo**: API 34 (Android 14)
- **Java**: JDK 8 o superior
- **Gradle**: 8.x
- **Android Studio**: Flamingo o superior

## 🏗️ Arquitectura del Proyecto

```
DocumentScanner/
├── app/
│   ├── src/main/
│   │   ├── java/com/kaushalvasava/apps/documentscanner/
│   │   │   ├── MainActivity.kt              # Actividad principal
│   │   │   └── ui/screen/
│   │   │       └── HomeScreen.kt            # Pantalla principal con funcionalidad
│   │   ├── res/
│   │   │   ├── drawable/                    # Iconos y recursos gráficos
│   │   │   ├── values/strings.xml           # Cadenas de texto
│   │   │   └── xml/filepaths.xml           # Configuración FileProvider
│   │   └── AndroidManifest.xml             # Configuración de la app
│   └── build.gradle.kts                    # Configuración de dependencias
├── gradle/libs.versions.toml               # Catálogo de versiones
└── build.gradle.kts                       # Configuración del proyecto
```

## 🔧 Componentes Principales

### 1. MainActivity.kt
**Actividad principal** que configura el tema y la interfaz de usuario:
- Extiende `ComponentActivity` para soporte de Compose
- Configura el tema `DocumentScannerTheme`
- Renderiza la pantalla principal `HomeScreen`

### 2. HomeScreen.kt
**Pantalla principal** con toda la funcionalidad de escaneo:
- **Scanner Configuration**: Configuración del escáner ML Kit
- **Document List**: Lista de documentos escaneados
- **PDF Viewer**: Visualización de documentos PDF
- **Gallery Import**: Importación desde galería habilitada

#### Funcionalidades implementadas:
```kotlin
// Configuración del escáner
fun getOptions(): GmsDocumentScannerOptions {
    return GmsDocumentScannerOptions.Builder()
        .setGalleryImportAllowed(true)              // Permite importar desde galería
        .setResultFormats(RESULT_FORMAT_PDF)        // Formato de salida PDF
        .setScannerMode(SCANNER_MODE_FULL)          // Modo completo de escaneo
        .build()
}
```

## 📱 Flujo de Usuario

1. **Inicio**: El usuario ve la pantalla principal con lista de documentos escaneados
2. **Escaneo**: Presiona el botón flotante "Scan" para iniciar el escaneo
3. **Captura**: ML Kit abre la cámara y detecta automáticamente documentos
4. **Procesamiento**: La API procesa y mejora la imagen automáticamente
5. **Guardado**: El documento se convierte a PDF y se guarda
6. **Visualización**: El PDF aparece en la lista y puede abrirse para ver

## 🎨 Interfaz de Usuario

### Componentes de UI utilizados:
- **Scaffold**: Estructura base con TopBar y FAB
- **TopAppBar**: Barra superior con título de la app
- **ExtendedFloatingActionButton**: Botón flotante para escanear
- **LazyColumn**: Lista eficiente de documentos
- **Card**: Tarjetas para mostrar cada documento
- **Material 3 Theme**: Tema moderno con colores dinámicos

### Iconografía:
- `ic_camera`: Icono de cámara para el botón de escaneo
- `ic_pdf`: Icono PDF para documentos en la lista

## 🔐 Permisos y Seguridad

### FileProvider Configuration:
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.provider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/filepaths"/>
</provider>
```

**Propósito**: Permite compartir archivos de forma segura entre aplicaciones sin exponer rutas internas del sistema.

## 🚀 Instalación y Configuración

### 1. Clonar el repositorio
```bash
git clone [URL_DEL_REPOSITORIO]
cd DocumentScanner
```

### 2. Abrir en Android Studio
- Abrir Android Studio
- Seleccionar "Open an existing project"
- Navegar a la carpeta del proyecto

### 3. Sincronizar dependencias
```bash
./gradlew build
```

### 4. Ejecutar la aplicación
- Conectar un dispositivo Android o iniciar un emulador
- Hacer clic en "Run" o usar `Shift + F10`

## 📦 Dependencias Principales

```kotlin
dependencies {
    // Android Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    
    // Jetpack Compose
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    
    // ML Kit Document Scanner
    implementation("com.google.android.gms:play-services-mlkit-document-scanner:16.0.0-beta1")
}
```

## 🔍 Características Técnicas Avanzadas

### 1. Gestión de Estado
- Uso de `mutableStateListOf<Pdf>()` para manejar la lista de documentos
- Estado reactivo que actualiza la UI automáticamente

### 2. Manejo de Resultados de Actividad
```kotlin
val scannerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartIntentSenderForResult()
) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
        scanningResult?.pdf?.let { pdf -> docs.add(pdf) }
    }
}
```

### 3. Integración con Sistema de Archivos
- Uso de `FileProvider` para acceso seguro a archivos
- Conversión de URI internos a URI compartibles
- Apertura de PDFs con aplicaciones externas

## 🐛 Manejo de Errores

La aplicación incluye manejo de errores para:
- Fallos en la inicialización del escáner
- Errores durante el proceso de escaneo
- Problemas de acceso a archivos
- Logging de errores con `Log.d()` para debugging

## 🔄 Flujo de Datos

```
Usuario → FAB Click → Scanner Intent → ML Kit Processing → PDF Result → UI Update → File Access
```

## 📋 Funcionalidades Futuras (Sugerencias)

- [ ] **Edición de documentos**: Recortar, rotar, ajustar brillo
- [ ] **OCR Integration**: Extracción de texto de documentos
- [ ] **Cloud Storage**: Sincronización con Google Drive/Dropbox
- [ ] **Batch Scanning**: Escaneo múltiple en una sesión
- [ ] **Document Organization**: Carpetas y categorías
- [ ] **Search Functionality**: Búsqueda por contenido de texto
- [ ] **Export Options**: Formatos JPEG, PNG, Word

## 🤝 Contribución

1. Fork el proyecto
2. Crear una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir un Pull Request

## 📄 Licencia

```
Copyright 2024 Kaushal Vasava

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## 📚 Referencias y Documentación

- [ML Kit Document Scanner Documentation](https://developers.google.com/ml-kit/vision/doc-scanner/android)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Material 3 Design Guidelines](https://m3.material.io/)
- [Android FileProvider Guide](https://developer.android.com/reference/androidx/core/content/FileProvider)

## 👨‍💻 Desarrollador

**Kaushal Vasava**
- GitHub: [@KaushalVasava](https://github.com/KaushalVasava)

---

⭐ Si este proyecto te resultó útil, ¡no olvides darle una estrella!
