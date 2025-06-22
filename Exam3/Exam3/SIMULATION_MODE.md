# BlueWeb Navigator - Modo Simulación

## 🔄 Modo Simulación Activado

La aplicación está configurada en **MODO SIMULACIÓN** para demostrar el funcionamiento sin usar Bluetooth real.

## 📱 Características del Modo Simulación

### ✅ Funcionalidades Que Funcionan
- **Conexión Simulada**: Se simula una conexión exitosa al "servidor"
- **Navegación Web**: Carga páginas web usando la conexión a Internet del dispositivo
- **Historial**: Guarda el historial de navegación
- **Marcadores**: Permite agregar y gestionar marcadores
- **Notificaciones**: Muestra notificaciones de estado y descarga
- **Modo Bajo Consumo**: Simula la optimización de contenido
- **Interfaz Completa**: Todos los botones y funciones están operativos

### 🎯 Cómo Usar la Aplicación

1. **Iniciar la Aplicación**
   - Abrir "BlueWeb Navigator"
   - Seleccionar "Actuar como Cliente"
   - Verás "(SIMULADO)" en el título

2. **Simular Conexión**
   - Presionar "Buscar Servidores"
   - Aparecerá un diálogo con "Dispositivo Simulado"
   - Presionar "Conectar"
   - El estado cambiará a "Conectado"

3. **Navegar por la Web**
   - Ingresar una URL (ej: `google.com`)
   - Presionar "Ir"
   - La página se cargará directamente
   - Verás mensajes simulando comunicación con el servidor

4. **Usar el Menú**
   - Presionar el botón "Menú"
   - Acceder a:
     - 🌐 **Páginas Populares**: URLs pre-configuradas
     - 📖 **Marcadores**: Gestionar sitios guardados
     - 📜 **Historial**: Ver páginas visitadas
     - ⚙️ **Configuración**: Ajustes de la app
     - 🔋 **Modo Bajo Consumo**: Optimizar navegación

## 🔧 URLs de Ejemplo

Para facilitar las pruebas, puedes usar estas URLs:
- `google.com`
- `youtube.com`
- `wikipedia.org`
- `bbc.com`
- `github.com`

## 🚀 Experiencia de Usuario

El modo simulación replica fielmente cómo funcionaría la aplicación con Bluetooth real:

1. **Mensajes Realistas**: 
   - "🔄 Solicitando página al servidor simulado..."
   - "📡 Recibiendo contenido del servidor..."
   - "✅ Página cargada desde servidor BlueWeb"

2. **Delays Simulados**: 
   - 2 segundos para búsqueda de dispositivos
   - 1 segundo para carga de páginas
   - Simula la latencia real de Bluetooth

3. **Notificaciones**: 
   - Estado de conexión
   - Descarga de páginas
   - Errores de navegación

## 🔄 Cambiar a Modo Real

Para usar Bluetooth real, cambiar en `ClientActivity.kt`:
```kotlin
private val SIMULATION_MODE = false
```

## 📋 Funcionalidades Demostradas

- ✅ **Interfaz de Usuario**: Material Design con temas personalizados
- ✅ **Gestión de Estado**: Conexión, navegación, errores
- ✅ **WebView Avanzado**: JavaScript, DOM storage, zoom
- ✅ **Persistencia**: Historial y marcadores guardados
- ✅ **Notificaciones**: Sistema completo de notificaciones
- ✅ **Experiencia Completa**: Navegación fluida y realista

## 🎯 Objetivo de la Demostración

Este modo simulación permite:
1. **Demostrar la funcionalidad completa** sin necesidad de 2 dispositivos
2. **Probar la interfaz de usuario** y flujo de navegación
3. **Verificar características avanzadas** como historial y marcadores
4. **Mostrar el diseño y experiencia** que tendría el usuario final

La aplicación está lista para ser usada con Bluetooth real simplemente cambiando el flag `SIMULATION_MODE`.
