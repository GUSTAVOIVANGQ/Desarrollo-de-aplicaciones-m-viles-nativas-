/*
 * INTEGRACIÓN DE MÉTRICAS CON FUNCIONALIDAD EXISTENTE
 * 
 * Este archivo muestra cómo integrar el sistema de métricas y autenticación
 * con las funcionalidades ya existentes en la aplicación.
 */

// Ejemplo 1: Integración con el Editor de Diagramas
/*
En editor_screen.dart, agregar después de las líneas existentes:

import '../examples/auth_integration_example.dart';

// En el método _addNode:
void _addNode(NodeType nodeType, {bool autoSelect = true}) {
  final node = DiagramNode(
    id: DateTime.now().millisecondsSinceEpoch.toString(),
    type: nodeType,
    position: Offset(200, 200),
    text: _getDefaultNodeText(nodeType),
  );

  setState(() {
    nodes.add(node);
    if (autoSelect) {
      selectedNode = node;
    }
    _hasUnsavedChanges = true;
  });

  // NUEVA LÍNEA: Registrar métrica de creación de nodo
  AuthIntegrationExample.trackDiagramCreation();

  _editSelectedNode();
}

// En el método _generateCode:
void _generateCode() {
  if (nodes.isEmpty) {
    _showSnackBar('No hay nodos para generar código');
    return;
  }

  final generator = CodeGenerator(nodes: nodes, connections: connections);
  final code = generator.generateCode();
  
  // NUEVA LÍNEA: Registrar métrica de generación de código
  AuthIntegrationExample.trackCodeGeneration();
  
  _showCodeDialog(code);
}

// En el método _validateDiagram:
void _validateDiagram() {
  final validator = DiagramValidator(nodes: nodes, connections: connections);
  final result = validator.validate();
  
  // NUEVA LÍNEA: Registrar métrica de validación
  AuthIntegrationExample.trackValidation(result.isValid);
  
  _showValidationDialog(result);
}
*/

// Ejemplo 2: Integración con Base de Datos
/*
En database_service.dart, agregar después de saveDiagram:

// MÉTODO MODIFICADO para incluir métricas
Future<int> saveDiagram(SavedDiagram diagram) async {
  final Database db = await database;
  
  // Asociar diagrama al usuario autenticado
  final authService = AuthService();
  final user = authService.currentUser;
  
  // Modificar el diagrama para incluir información del usuario
  final modifiedDiagram = diagram.copyWith(
    // Agregar campo user_id al modelo SavedDiagram si es necesario
  );
  
  final result = await db.insert(
    'diagrams',
    modifiedDiagram.toMap(),
    conflictAlgorithm: ConflictAlgorithm.replace,
  );
  
  // Actualizar métricas del usuario
  if (user != null) {
    final metrics = Map<String, dynamic>.from(user.metrics);
    metrics['diagramas_guardados'] = (metrics['diagramas_guardados'] ?? 0) + 1;
    metrics['ultimo_guardado'] = DateTime.now().toIso8601String();
    
    await authService.updateUserMetrics(user.uid, metrics);
  }
  
  return result;
}
*/

// Ejemplo 3: Integración con Plantillas
/*
En load_diagram_screen.dart, modificar al cargar plantillas:

Widget _buildDiagramList(List<SavedDiagram> items, {bool canDelete = false}) {
  return ListView.builder(
    itemCount: items.length,
    padding: const EdgeInsets.all(8.0),
    itemBuilder: (context, index) {
      final item = items[index];
      return Card(
        elevation: 2,
        margin: const EdgeInsets.symmetric(vertical: 8.0, horizontal: 4.0),
        child: ListTile(
          title: Text(item.name),
          subtitle: Text(item.description),
          onTap: () {
            // NUEVA LÍNEA: Registrar uso de plantilla
            if (item.isTemplate) {
              _trackTemplateUsage(item);
            }
            
            Navigator.of(context).push(
              MaterialPageRoute(
                builder: (context) => EditorScreen(initialDiagram: item),
              ),
            );
          },
        ),
      );
    },
  );
}

// NUEVO MÉTODO para registrar uso de plantillas
void _trackTemplateUsage(SavedDiagram template) async {
  final authService = AuthService();
  final user = authService.currentUser;
  
  if (user != null) {
    final metrics = Map<String, dynamic>.from(user.metrics);
    metrics['plantillas_usadas'] = (metrics['plantillas_usadas'] ?? 0) + 1;
    metrics['ultima_plantilla'] = template.name;
    metrics['fecha_uso_plantilla'] = DateTime.now().toIso8601String();
    
    await authService.updateUserMetrics(user.uid, metrics);
  }
}
*/

// Ejemplo 4: Métricas Educativas Avanzadas
/*
Crear un nuevo servicio para métricas educativas:

class EducationalMetricsService {
  static final AuthService _authService = AuthService();
  
  // Registrar tiempo de resolución de ejercicios
  static void trackExerciseCompletion({
    required String exerciseId,
    required Duration timeSpent,
    required bool successful,
    required int errorsFound,
    required int hintsUsed,
  }) async {
    final user = _authService.currentUser;
    if (user == null) return;
    
    final metrics = Map<String, dynamic>.from(user.metrics);
    
    // Métricas específicas del ejercicio
    metrics['ejercicios_completados'] = (metrics['ejercicios_completados'] ?? 0) + 1;
    metrics['tiempo_total_minutos'] = (metrics['tiempo_total_minutos'] ?? 0) + timeSpent.inMinutes;
    metrics['errores_totales'] = (metrics['errores_totales'] ?? 0) + errorsFound;
    metrics['pistas_usadas'] = (metrics['pistas_usadas'] ?? 0) + hintsUsed;
    
    if (successful) {
      metrics['ejercicios_exitosos'] = (metrics['ejercicios_exitosos'] ?? 0) + 1;
    }
    
    // Calcular promedios
    final totalEjercicios = metrics['ejercicios_completados'];
    metrics['tiempo_promedio_minutos'] = metrics['tiempo_total_minutos'] / totalEjercicios;
    metrics['tasa_exito'] = (metrics['ejercicios_exitosos'] ?? 0) / totalEjercicios;
    
    await _authService.updateUserMetrics(user.uid, metrics);
  }
  
  // Registrar autoevaluación de confianza
  static void trackSelfAssessment({
    required int confidenceLevel, // 1-5
    required String topic,
  }) async {
    final user = _authService.currentUser;
    if (user == null) return;
    
    final metrics = Map<String, dynamic>.from(user.metrics);
    
    // Guardar autoevaluaciones por tema
    final assessments = metrics['autoevaluaciones'] ?? {};
    assessments[topic] = {
      'nivel_confianza': confidenceLevel,
      'fecha': DateTime.now().toIso8601String(),
    };
    
    metrics['autoevaluaciones'] = assessments;
    
    // Calcular confianza promedio
    final totalAssessments = assessments.values.length;
    final sumConfidence = assessments.values
        .map((a) => a['nivel_confianza'] as int)
        .reduce((a, b) => a + b);
    
    metrics['confianza_promedio'] = sumConfidence / totalAssessments;
    
    await _authService.updateUserMetrics(user.uid, metrics);
  }
}
*/

// Ejemplo 5: Panel de Métricas para Usuarios
/*
Crear nueva pantalla: metrics_screen.dart

class MetricsScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final authService = AuthService();
    final user = authService.currentUser;
    
    if (user == null) {
      return Scaffold(
        body: Center(child: Text('Debes iniciar sesión para ver las métricas')),
      );
    }
    
    return Scaffold(
      appBar: AppBar(title: Text('Mis Métricas')),
      body: SingleChildScrollView(
        child: Padding(
          padding: EdgeInsets.all(16),
          child: Column(
            children: [
              _buildTechnicalMetrics(user.metrics),
              _buildEducationalMetrics(user.metrics),
              if (user.isAdmin) _buildAdminMetrics(),
            ],
          ),
        ),
      ),
    );
  }
  
  Widget _buildTechnicalMetrics(Map<String, dynamic> metrics) {
    return Card(
      child: Padding(
        padding: EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Métricas Técnicas', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
            SizedBox(height: 16),
            _buildMetricRow('Diagramas creados', metrics['diagramas_creados']?.toString() ?? '0'),
            _buildMetricRow('Código generado', metrics['codigo_generado']?.toString() ?? '0'),
            _buildMetricRow('Validaciones exitosas', metrics['validaciones_exitosas']?.toString() ?? '0'),
            _buildMetricRow('Plantillas usadas', metrics['plantillas_usadas']?.toString() ?? '0'),
          ],
        ),
      ),
    );
  }
  
  Widget _buildEducationalMetrics(Map<String, dynamic> metrics) {
    return Card(
      child: Padding(
        padding: EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Métricas Educativas', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
            SizedBox(height: 16),
            _buildMetricRow('Ejercicios completados', metrics['ejercicios_completados']?.toString() ?? '0'),
            _buildMetricRow('Tiempo promedio (min)', metrics['tiempo_promedio_minutos']?.toStringAsFixed(1) ?? '0'),
            _buildMetricRow('Tasa de éxito', '${((metrics['tasa_exito'] ?? 0) * 100).toStringAsFixed(1)}%'),
            _buildMetricRow('Confianza promedio', '${metrics['confianza_promedio']?.toStringAsFixed(1) ?? '0'}/5'),
          ],
        ),
      ),
    );
  }
}
*/

// INSTRUCCIONES DE IMPLEMENTACIÓN:

/*
1. Para habilitar autenticación completa:
   - Configurar Firebase según FUNCIONALIDAD_6_README.md
   - Descomentar líneas en main.dart
   - Reemplazar google-services.json con archivo real

2. Para integrar métricas:
   - Agregar las líneas comentadas en los archivos correspondientes
   - Crear métodos de tracking en cada funcionalidad
   - Implementar pantalla de métricas

3. Para modo administrador:
   - Crear admin_panel_screen.dart
   - Implementar vistas de métricas globales
   - Agregar funciones de gestión de usuarios

4. Estructura de base de datos recomendada:
   - Colección 'users' con documentos por userId
   - Subcolecciones 'diagrams' bajo cada usuario
   - Colección 'global_metrics' para estadísticas generales

5. Reglas de seguridad Firestore:
   - Usuarios solo acceden a sus datos
   - Administradores acceden a datos globales
   - Métricas agregadas públicas para comparación
*/
