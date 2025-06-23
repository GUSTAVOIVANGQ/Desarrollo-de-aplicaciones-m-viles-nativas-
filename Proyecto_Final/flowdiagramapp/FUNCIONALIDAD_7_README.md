# Funcionalidad 7: Secciones de métricas y visualización

## 📊 Descripción General

La Funcionalidad 7 implementa un sistema completo de métricas y visualización para la aplicación FlowDiagram App, proporcionando tanto métricas personales para usuarios como métricas globales y análisis para administradores. El sistema está diseñado para mejorar la comprensión de algoritmos mediante el seguimiento del progreso educativo y técnico.

## ✨ Características Implementadas

### 🎯 Para Usuarios Normales
- **Métricas Personales**: Seguimiento individual del progreso
- **Visualización de Progreso**: Gráficos y estadísticas de actividad
- **Métricas Educativas**: Tiempo de estudio, errores, pistas utilizadas
- **Métricas Técnicas**: Diagramas creados, validaciones, código generado
- **Historial de Actividad**: Registro de últimas acciones realizadas

### 👑 Para Administradores
- **Panel de Control Global**: Vista general del sistema
- **Métricas de Usuarios**: Análisis de todos los usuarios
- **Estadísticas del Sistema**: Uso general de la aplicación
- **Rankings y Tendencias**: Usuarios más activos y patrones de uso
- **Recomendaciones**: Sugerencias basadas en datos del sistema

## 🏗️ Arquitectura del Sistema

### Modelos de Datos

#### `MetricModel`
```dart
class MetricModel {
  final String id;
  final String name;
  final double value;
  final String unit;
  final String category;
  final DateTime lastUpdated;
  final Map<String, dynamic> metadata;
}
```

#### `MetricsSummary` 
```dart
class MetricsSummary {
  final int totalDiagrams;
  final int totalCodeGenerations;
  final int totalValidations;
  final int totalTemplatesUsed;
  final double averageTime;
  final double successRate;
  final DateTime lastActivity;
  final Map<String, dynamic> educationalMetrics;
  final Map<String, dynamic> technicalMetrics;
}
```

#### `GlobalMetrics`
```dart
class GlobalMetrics {
  final int totalUsers;
  final int activeUsers;
  final int totalDiagrams;
  final int totalValidations;
  final double averageUserProgress;
  final Map<String, int> usersByRole;
  final Map<String, double> performanceMetrics;
  final List<Map<String, dynamic>> topUsers;
  final DateTime generatedAt;
}
```

### Servicios

#### `MetricsService`
Servicio principal para el manejo de métricas con las siguientes funcionalidades:
- **Seguimiento de acciones**: `trackUserAction()`
- **Métricas educativas**: `trackEducationalMetric()`
- **Resumen de usuario**: `getUserMetricsSummary()`
- **Métricas globales**: `getGlobalMetrics()` (solo admins)
- **Lista de usuarios**: `getUsersWithMetrics()` (solo admins)
- **Modo offline**: Soporte completo sin conexión

## 📱 Interfaces de Usuario

### Pantalla de Métricas del Usuario (`MetricsScreen`)

**Ubicación**: `lib/screens/metrics_screen.dart`

**Características**:
- Información del perfil del usuario
- Métricas técnicas (diagramas, validaciones, código)
- Métricas educativas (tiempo, errores, confianza)
- Gráfico de progreso visual
- Actividad reciente
- Acceso al panel de administrador (solo admins)

**Navegación**:
- Desde el perfil del usuario
- Desde la pantalla principal (botón de métricas)

### Panel de Administrador (`AdminMetricsScreen`)

**Ubicación**: `lib/screens/admin_metrics_screen.dart`

**Características**:
- **Pestaña Resumen**: Vista general del sistema
  - Tarjetas de métricas principales
  - Distribución de usuarios por rol
  - Métricas de rendimiento
  - Información del sistema
  
- **Pestaña Usuarios**: Análisis detallado de usuarios
  - Lista expandible de todos los usuarios
  - Métricas individuales de cada usuario
  - Indicadores de rol (Usuario/Admin)
  - Estado de actividad
  
- **Pestaña Análisis**: Análisis avanzado
  - Top usuarios más activos
  - Tendencias de uso
  - Recomendaciones del sistema

**Acceso**:
- Solo para usuarios con rol de administrador
- Verificación automática de permisos
- Redirección si no se tienen permisos

### Configuración de Administrador (`AdminSetupScreen`)

**Ubicación**: `lib/screens/admin_setup_screen.dart`

**Propósito**: Resolver problemas de configuración inicial y permisos

**Funcionalidades**:
- Crear administrador por defecto
- Promover usuarios existentes a administrador
- Diagnóstico del estado actual
- Información de credenciales

**Credenciales de Admin por Defecto**:
- **Email**: `admin@flowdiagram.com`
- **Contraseña**: `Admin123456`

## 🎨 Widgets de Visualización

### `MetricsChartWidget`

**Ubicación**: `lib/widgets/metrics_chart_widget.dart`

**Tipos de Gráficos Soportados**:
- **Gráfico de Barras**: Para comparaciones
- **Gráfico de Líneas**: Para tendencias temporales  
- **Gráfico de Pastel**: Para distribuciones

**Widgets Adicionales**:
- `QuickStatsWidget`: Estadísticas rápidas en chips
- `ComparisonWidget`: Comparaciones con barras de progreso

## 🔧 Integración con la Aplicación

### Seguimiento Automático de Métricas

Las métricas se registran automáticamente en las siguientes acciones:

#### En `EditorScreen`:
```dart
// Creación de nodos
await _metricsService.trackUserAction(
  action: 'nodo_creado',
  category: 'technical',
  metadata: {'nodeType': nodeType.toString()},
);

// Validación de diagramas
await _metricsService.trackUserAction(
  action: successful ? 'validacion_exitosa' : 'validacion_fallida',
  category: 'educational',
  metadata: {'errorsFound': result.errors.length},
);

// Generación de código
await _metricsService.trackUserAction(
  action: 'codigo_generado',
  category: 'technical',
  metadata: {'nodesCount': nodes.length},
);
```

#### En `LoadDiagramScreen`:
```dart
// Uso de plantillas
await _metricsService.trackUserAction(
  action: 'plantilla_usada',
  category: 'educational',
  metadata: {'templateName': template.name},
);
```

### Navegación a Métricas

**Desde la Pantalla Principal**:
```dart
IconButton(
  icon: const Icon(Icons.analytics),
  onPressed: () {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (context) => const MetricsScreen(),
      ),
    );
  },
  tooltip: 'Mis métricas',
),
```

**Desde el Perfil**:
```dart
ElevatedButton.icon(
  onPressed: () {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (context) => const MetricsScreen(),
      ),
    );
  },
  icon: const Icon(Icons.analytics),
  label: const Text('Ver Mis Métricas'),
),
```

## 🔐 Sistema de Permisos y Seguridad

### Verificación de Administrador

```dart
void _checkAdminAccess() {
  final user = _authService.currentUser;
  if (user == null || !user.isAdmin) {
    Navigator.of(context).pop();
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Acceso denegado: Solo administradores pueden ver esta sección'),
        backgroundColor: Colors.red,
      ),
    );
    return;
  }
  _loadAllMetrics();
}
```

### Manejo de Errores de Permisos

```dart
try {
  final globalMetrics = await _metricsService.getGlobalMetrics();
  // Procesar métricas...
} catch (e) {
  if (e.toString().contains('permission-denied')) {
    // Usar métricas por defecto
    return _createDefaultGlobalMetrics();
  }
  throw Exception('Error obteniendo métricas globales: ${e.toString()}');
}
```

## 📊 Almacenamiento de Datos

### Firebase Firestore
- **Colección `users`**: Métricas por usuario
- **Colección `global_metrics`**: Métricas agregadas del sistema
- **Documentos en tiempo real**: Actualizaciones automáticas

### Modo Offline
- **Cache local**: Métricas almacenadas localmente
- **Sincronización automática**: Al recuperar conexión
- **Funcionalidad completa**: Sin dependencia de internet

## 🛠️ Configuración e Instalación

### Dependencias Requeridas

Agregar en `pubspec.yaml`:
```yaml
dependencies:
  fl_chart: ^0.68.0        # Para gráficos y visualización
  firebase_core: ^2.27.0   # Core de Firebase
  firebase_auth: ^4.17.8   # Autenticación
  cloud_firestore: ^4.15.8 # Base de datos
  connectivity_plus: ^5.0.2 # Detección de conectividad
  intl: ^0.18.1            # Formateo de fechas
```

### Configuración de Firebase

1. **Configurar proyecto en Firebase Console**
2. **Habilitar Authentication y Firestore**
3. **Configurar reglas de seguridad**:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Usuarios pueden leer/escribir sus propios datos
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Solo admins pueden acceder a métricas globales
    match /global_metrics/{document} {
      allow read, write: if request.auth != null && 
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }
  }
}
```

## 🚀 Uso y Flujo de Trabajo

### Para Usuarios Normales

1. **Iniciar sesión** en la aplicación
2. **Usar el editor** para crear diagramas (métricas se registran automáticamente)
3. **Acceder a métricas personales**:
   - Desde el perfil → "Ver Mis Métricas"
   - Desde pantalla principal → botón de análisis
4. **Revisar progreso** en gráficos y estadísticas

### Para Administradores

1. **Configurar administrador** (primera vez):
   - Usar botón de configuración de admin (escudo)
   - Crear admin por defecto o promover usuario existente
2. **Acceder al panel de administrador**:
   - Desde métricas personales → "Ver Métricas Globales"
   - Desde perfil → "Panel de Administrador"
3. **Analizar datos** en las tres pestañas disponibles

## 🔧 Solución de Problemas Comunes

### Error "Null check operator used on a null value"

**Causa**: Uso de operador `!` en valores que pueden ser null

**Solución**: Implementada verificación segura de null
```dart
// ✅ Correcto
final metrics = _globalMetrics;
if (metrics == null) return Center(child: Text('No hay datos disponibles'));

// ❌ Incorrecto  
_buildOverviewCards(_globalMetrics!)
```

### Error de permisos "Permission Denied"

**Causa**: Usuario no tiene permisos de administrador o reglas de Firestore

**Solución**: 
1. Usar `AdminSetupScreen` para crear administrador
2. Verificar reglas de seguridad en Firestore
3. Sistema de respaldo con métricas por defecto

### Error de navegación

**Causa**: Uso de rutas nombradas sin configuración

**Solución**: Usar `MaterialPageRoute` directamente
```dart
Navigator.of(context).push(
  MaterialPageRoute(
    builder: (context) => const AdminMetricsScreen(),
  ),
);
```

## 📈 Métricas Disponibles

### Métricas Técnicas
- **Diagramas creados**: Cantidad total de diagramas
- **Código generado**: Veces que se generó código C
- **Validaciones realizadas**: Total de validaciones
- **Plantillas utilizadas**: Uso de plantillas predefinidas
- **Tiempo promedio**: Tiempo por sesión/actividad
- **Tasa de éxito**: Porcentaje de validaciones exitosas

### Métricas Educativas
- **Ejercicios completados**: Tareas finalizadas
- **Tiempo total de estudio**: Minutos acumulados
- **Errores cometidos**: Cantidad de errores
- **Pistas utilizadas**: Ayudas solicitadas
- **Autoevaluaciones**: Niveles de confianza registrados
- **Progreso general**: Puntuación calculada automáticamente

### Métricas Globales (Solo Admins)
- **Total de usuarios**: Usuarios registrados en el sistema
- **Usuarios activos**: Actividad en período reciente
- **Distribución por roles**: Usuario vs Administrador
- **Métricas de rendimiento**: Promedios del sistema
- **Top usuarios**: Ranking de más activos
- **Tendencias de uso**: Patrones temporales

## 🎯 Beneficios Educativos

### Para Estudiantes
- **Autoconocimiento**: Visualización del progreso personal
- **Motivación**: Gamificación a través de métricas
- **Identificación de áreas de mejora**: Análisis de errores y tiempo
- **Seguimiento temporal**: Evolución del aprendizaje

### Para Educadores
- **Monitoreo grupal**: Vista global de la clase
- **Identificación de dificultades**: Análisis de errores comunes
- **Personalización**: Adaptar enseñanza según métricas
- **Evaluación objetiva**: Datos cuantificables del progreso

## 🔮 Futuras Mejoras

### Visualizaciones Avanzadas
- Gráficos de correlación entre métricas
- Heatmaps de actividad temporal
- Predicciones de progreso con ML

### Métricas Adicionales
- Tiempo de resolución por tipo de problema
- Patrones de errores más comunes
- Comparación con promedios grupales

### Gamificación
- Sistema de logros y badges
- Competencias entre usuarios
- Niveles de progreso

## 📁 Estructura de Archivos

```
lib/
├── models/
│   └── metric_model.dart              # Modelos de datos de métricas
├── screens/
│   ├── metrics_screen.dart            # Pantalla de métricas del usuario
│   ├── admin_metrics_screen.dart      # Panel de administrador
│   └── admin_setup_screen.dart        # Configuración de administrador
├── services/
│   └── metrics_service.dart           # Lógica de negocio de métricas
└── widgets/
    └── metrics_chart_widget.dart      # Widgets de visualización

docs/
└── NULL_SAFETY_FIX_GUIDE.md          # Guía de solución de problemas
```

## 🏆 Estado de Implementación

- ✅ **Modelos de datos**: Completamente implementados
- ✅ **Servicios de métricas**: Funcionales con modo offline
- ✅ **Pantallas de usuario**: Métricas personales completas
- ✅ **Panel de administrador**: Tres pestañas funcionales
- ✅ **Widgets de visualización**: Gráficos implementados
- ✅ **Integración automática**: Seguimiento en tiempo real
- ✅ **Sistema de permisos**: Verificación robusta
- ✅ **Manejo de errores**: Null safety y fallbacks
- ✅ **Documentación**: Guías completas

## 👥 Contribución

Para contribuir a esta funcionalidad:

1. **Revisar la documentación** de solución de problemas
2. **Seguir las buenas prácticas** de null safety
3. **Probar en modo offline** para asegurar funcionamiento
4. **Verificar permisos** antes de implementar nuevas características
5. **Documentar cambios** en los archivos README correspondientes

---

*Funcionalidad 7 desarrollada como parte del Proyecto Final de Desarrollo de Aplicaciones Móviles Nativas - FlowDiagram App*
