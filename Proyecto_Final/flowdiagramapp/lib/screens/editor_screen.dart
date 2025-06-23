import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../widgets/flow_diagram_canvas_final.dart';
import '../widgets/node_palette.dart';
import '../widgets/node_editor_dialog.dart';
import '../widgets/validation_result_dialog.dart';
import 'load_diagram_screen.dart';
import '../widgets/save_diagram_dialog.dart';
import '../models/diagram_node.dart';
import '../models/diagram_validator.dart';
import '../models/code_generator.dart';
import '../models/saved_diagram.dart';
import '../services/database_service.dart';
import '../services/metrics_service.dart'; // Nueva importación

class EditorScreen extends StatefulWidget {
  final SavedDiagram? initialDiagram;

  const EditorScreen({super.key, this.initialDiagram});

  @override
  State<EditorScreen> createState() => _EditorScreenState();
}

class _EditorScreenState extends State<EditorScreen> {
  final List<DiagramNode> nodes = [];
  final List<Connection> connections = [];
  DiagramNode? selectedNode;
  Connection? selectedConnection; // Nueva propiedad para conexión seleccionada
  DiagramNode? connectionStart;
  Offset panOffset = Offset.zero;
  double currentScale = 1.0;
  bool isConnecting = false;

  // Para control de guardado
  SavedDiagram? currentDiagram;
  final DatabaseService _databaseService = DatabaseService();
  final MetricsService _metricsService = MetricsService(); // Nuevo servicio
  bool _hasUnsavedChanges = false;

  @override
  void initState() {
    super.initState();

    // Si se proporciona un diagrama inicial, cargarlo
    if (widget.initialDiagram != null) {
      _loadDiagram(widget.initialDiagram!);
    }
  }

  void _loadDiagram(SavedDiagram diagram) {
    setState(() {
      nodes.clear();
      connections.clear();

      // Agregar nodos y conexiones del diagrama cargado
      nodes.addAll(diagram.nodes);
      connections.addAll(diagram.connections);

      // Almacenar referencia al diagrama actual
      currentDiagram = diagram;
      _hasUnsavedChanges = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(currentDiagram?.name ?? 'Diagrama de Flujo'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => _handleBackNavigation(context),
        ),
        actions: [
          // Botón para validar el diagrama
          IconButton(
            icon: const Icon(Icons.check_circle_outline),
            tooltip: 'Validar diagrama',
            onPressed: _validateDiagram,
          ),
          // Botón para guardar diagrama
          IconButton(
            icon: _hasUnsavedChanges
                ? const Icon(Icons.save, color: Colors.amber)
                : const Icon(Icons.save),
            tooltip: 'Guardar diagrama',
            onPressed: _showSaveDiagramDialog,
          ),
          // Botón para cargar diagrama
          IconButton(
            icon: const Icon(Icons.folder_open),
            tooltip: 'Cargar diagrama',
            onPressed: () => _navigateToLoadDiagram(context),
          ),
          IconButton(
            icon: const Icon(Icons.code),
            tooltip: 'Generar código',
            onPressed: _generateCode,
          ),
          if (selectedNode != null)
            IconButton(
              icon: const Icon(Icons.edit),
              tooltip: 'Editar nodo',
              onPressed: () => _editSelectedNode(),
            ),
          if (selectedNode != null)
            IconButton(
              icon: const Icon(Icons.delete),
              tooltip: 'Eliminar nodo',
              onPressed: () => _deleteSelectedNode(),
            ),
          // Nuevo botón para gestionar conexiones
          if (selectedNode != null)
            IconButton(
              icon: isConnecting
                  ? const Icon(Icons.link_off)
                  : const Icon(Icons.link),
              tooltip: isConnecting ? 'Cancelar conexión' : 'Crear conexión',
              onPressed: () {
                setState(() {
                  if (isConnecting) {
                    connectionStart = null;
                    isConnecting = false;
                    _showSnackBar('Conexión cancelada');
                  } else {
                    connectionStart = selectedNode;
                    isConnecting = true;
                    _showSnackBar('Selecciona otro nodo para conectarlo');
                  }
                });
              },
            ),
        ],
      ),
      body: Row(
        children: [
          // Panel lateral con la paleta de nodos
          NodePalette(
            onNodeSelected: (nodeType) {
              // No seleccionar automáticamente el nodo si estamos en modo conexión
              _addNode(nodeType, autoSelect: !isConnecting);
            },
          ),

          // Área principal del canvas
          Expanded(
            child: Stack(
              children: [
                FlowDiagramCanvas(
                  nodes: nodes,
                  connections: connections,
                  selectedNode: selectedNode,
                  panOffset: panOffset,
                  scale: currentScale,
                  onPanUpdate: (details) {
                    if (!isConnecting) {
                      setState(() {
                        panOffset += details.delta;
                      });
                    }
                  },
                  onScaleUpdate: (scale) {
                    if (!isConnecting) {
                      setState(() {
                        currentScale = scale.scale.clamp(0.5, 2.0);
                      });
                    }
                  },
                  onNodeTap: (node) {
                    print('Editor recibió tap en nodo: ${node?.type}');
                    setState(() {
                      if (node == null) {
                        // Si se toca un área vacía, deseleccionamos todo
                        if (!isConnecting) {
                          print('Deseleccionando nodo');
                          selectedNode = null;
                          selectedConnection = null;
                        }
                      } else if (isConnecting && connectionStart != null) {
                        // Si estamos en modo conexión y ya tenemos un nodo de origen,
                        // este tap es para seleccionar el nodo destino y crear la conexión
                        if (connectionStart != node) {
                          // Evitar conectar un nodo consigo mismo
                          _createConnection(connectionStart!, node);
                          // Después de crear la conexión, salimos del modo conexión
                          isConnecting = false;
                          connectionStart = null;
                        } else {
                          _showSnackBar(
                              'No puedes conectar un nodo consigo mismo');
                        }
                      } else {
                        // Si no estamos en modo conexión, simplemente seleccionamos el nodo
                        print('Seleccionando nodo: ${node.type}');
                        selectedNode = node;
                        selectedConnection =
                            null; // Deseleccionar cualquier conexión
                      }
                    });

                    // Mostrar indicación visual de que el nodo fue seleccionado
                    if (node != null && !isConnecting) {
                      String nodeName = "";
                      switch (node.type) {
                        case NodeType.start:
                          nodeName = "Inicio";
                          break;
                        case NodeType.end:
                          nodeName = "Fin";
                          break;
                        case NodeType.process:
                          nodeName = "Proceso";
                          break;
                        case NodeType.decision:
                          nodeName = "Decisión";
                          break;
                        case NodeType.input:
                          nodeName = "Entrada";
                          break;
                        case NodeType.output:
                          nodeName = "Salida";
                          break;
                        case NodeType.variable:
                          nodeName = "Variable";
                          break;
                      }
                      _showSnackBar('Nodo ${nodeName} seleccionado');
                    }
                  },
                  onNodeLongPress: (node) {
                    setState(() {
                      // Solo iniciar conexión si no estamos ya en ese modo
                      if (!isConnecting) {
                        connectionStart = node;
                        selectedNode = node;
                        isConnecting = true;
                        _showSnackBar('Selecciona otro nodo para conectarlos');
                      }
                    });
                  },
                  onNodeDragUpdate: (node, offset) {
                    setState(() {
                      node.position = offset;
                      _hasUnsavedChanges = true;
                    });
                  },
                  onConnectionTap: (connection) {
                    setState(() {
                      selectedConnection = connection;
                      selectedNode = null; // Deseleccionar cualquier nodo
                      _showConnectionOptionsDialog(connection);
                    });
                  },
                ),

                // Indicador visual cuando estamos en modo conexión
                if (isConnecting)
                  Positioned(
                    top: 16,
                    left: 0,
                    right: 0,
                    child: Center(
                      child: Container(
                        padding: const EdgeInsets.symmetric(
                          horizontal: 16,
                          vertical: 8,
                        ),
                        decoration: BoxDecoration(
                          color: Colors.blue.withOpacity(0.8),
                          borderRadius: BorderRadius.circular(20),
                        ),
                        child: const Text(
                          'Modo conexión: Toca otro nodo para conectar',
                          style: TextStyle(
                            color: Colors.white,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                    ),
                  ),
              ],
            ),
          ),
        ],
      ),
      floatingActionButton: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (isConnecting)
            FloatingActionButton(
              onPressed: () {
                setState(() {
                  connectionStart = null;
                  isConnecting = false;
                });
                _showSnackBar('Conexión cancelada');
              },
              heroTag: 'cancel',
              mini: true,
              tooltip: 'Cancelar conexión',
              backgroundColor: Colors.red,
              child: const Icon(Icons.close),
            ),
          const SizedBox(height: 8),
          FloatingActionButton(
            onPressed: () {
              setState(() {
                // Resetear el desplazamiento y la escala
                panOffset = Offset.zero;
                currentScale = 1.0;
              });
            },
            heroTag: 'center',
            tooltip: 'Centrar diagrama',
            child: const Icon(Icons.center_focus_strong),
          ),
        ],
      ),
    );
  }

  // Control de navegación hacia atrás con verificación de cambios sin guardar
  Future<void> _handleBackNavigation(BuildContext context) async {
    if (!_hasUnsavedChanges) {
      Navigator.pop(context);
      return;
    }

    final bool shouldDiscard = await showDialog<bool>(
          context: context,
          builder: (context) => AlertDialog(
            title: const Text('¿Descartar cambios?'),
            content: const Text(
              'Hay cambios sin guardar. ¿Estás seguro de que quieres salir sin guardar?',
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.of(context).pop(false),
                child: const Text('Cancelar'),
              ),
              FilledButton(
                onPressed: () => Navigator.of(context).pop(true),
                style: FilledButton.styleFrom(backgroundColor: Colors.red),
                child: const Text('Descartar'),
              ),
            ],
          ),
        ) ??
        false;

    if (shouldDiscard && mounted) {
      Navigator.pop(context);
    }
  }

  void _addNode(NodeType nodeType, {bool autoSelect = true}) {
    final node = DiagramNode(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      type: nodeType,
      position: Offset(
        (MediaQuery.of(context).size.width / 2 - panOffset.dx) / currentScale,
        (MediaQuery.of(context).size.height / 2 - panOffset.dy) / currentScale,
      ),
    );

    setState(() {
      nodes.add(node);
      if (autoSelect) {
        selectedNode = node;
      }
      _hasUnsavedChanges = true;
    });

    // Registrar métrica de creación de nodo
    _metricsService.trackUserAction(
      action: 'diagrama_creado',
      category: 'editor',
      metadata: {'node_type': nodeType.toString()},
    );

    // Mostrar diálogo para editar el nodo recién creado
    _editSelectedNode();
  }

  void _createConnection(DiagramNode source, DiagramNode target) {
    // No crear conexión si es el mismo nodo
    if (source == target) {
      _showSnackBar('No se puede conectar un nodo consigo mismo');
      connectionStart = null;
      isConnecting = false;
      return;
    }

    // Verificar que la conexión sea válida según el tipo de nodo
    if (_isValidConnection(source, target)) {
      // Establecer etiqueta predeterminada según el tipo de nodo
      String defaultLabel = '';

      // Si el nodo fuente es una decisión, mostrar diálogo para elegir etiqueta
      if (source.type == NodeType.decision) {
        _showConnectionLabelDialog(source, target);
      } else {
        final connection =
            Connection(source: source, target: target, label: defaultLabel);
        setState(() {
          connections.add(connection);
          _hasUnsavedChanges = true;
          connectionStart = null;
          isConnecting = false;
        });
        _showSnackBar('Conexión creada');
      }
    } else {
      setState(() {
        connectionStart = null;
        isConnecting = false;
      });
    }
  }

  // Diálogo para editar la etiqueta de una conexión
  Future<void> _showConnectionLabelDialog(
      DiagramNode source, DiagramNode target) async {
    final TextEditingController labelController = TextEditingController();

    // Establecer etiqueta predeterminada para nodos de decisión
    if (source.type == NodeType.decision) {
      // Verificar si ya hay otras conexiones desde este nodo de decisión
      final existingConnections =
          connections.where((c) => c.source == source).toList();
      if (existingConnections.isEmpty) {
        // Primera conexión, sugerimos "Sí" como etiqueta
        labelController.text = "Sí";
      } else if (existingConnections.length == 1) {
        // Segunda conexión, sugerimos "No" como etiqueta
        labelController.text = "No";
      }
    }

    final String? label = await showDialog<String>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Etiqueta de conexión'),
        content: TextField(
          controller: labelController,
          decoration: const InputDecoration(
            labelText: 'Etiqueta',
            hintText: 'ej: Sí, No, Verdadero, Falso',
          ),
          autofocus: true,
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(null),
            child: const Text('Cancelar'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(context).pop(labelController.text),
            child: const Text('Guardar'),
          ),
        ],
      ),
    );

    if (label != null) {
      final connection =
          Connection(source: source, target: target, label: label);
      setState(() {
        connections.add(connection);
        _hasUnsavedChanges = true;
        connectionStart = null;
        isConnecting = false;
      });
      _showSnackBar('Conexión creada con etiqueta "$label"');
    } else {
      // Si se canceló el diálogo, cancelar la creación de la conexión
      setState(() {
        connectionStart = null;
        isConnecting = false;
      });
    }
  }

  // Método para eliminar una conexión seleccionada
  void _deleteConnection(Connection connection) {
    setState(() {
      connections.remove(connection);
      _hasUnsavedChanges = true;
    });
    _showSnackBar('Conexión eliminada');
  }

  bool _isValidConnection(DiagramNode source, DiagramNode target) {
    // Un nodo final no puede tener salidas
    if (source.type == NodeType.end) {
      _showSnackBar('Un nodo de fin no puede tener conexiones de salida');
      return false;
    }

    // Un nodo inicio no puede tener entradas
    if (target.type == NodeType.start) {
      _showSnackBar('Un nodo de inicio no puede tener conexiones de entrada');
      return false;
    }

    // Evitar conexiones duplicadas
    bool isDuplicate = connections.any(
      (conn) => conn.source == source && conn.target == target,
    );

    if (isDuplicate) {
      _showSnackBar('Esta conexión ya existe');
      return false;
    }

    // Si pasó todas las validaciones, la conexión es válida
    return true;
  }

  Future<void> _editSelectedNode() async {
    if (selectedNode == null) return;

    final String? result = await showDialog<String>(
      context: context,
      builder: (context) => NodeEditorDialog(node: selectedNode!),
    );

    if (result != null) {
      setState(() {
        selectedNode!.text = result;
        _hasUnsavedChanges = true;
      });
    }
  }

  void _deleteSelectedNode() {
    if (selectedNode == null) return;

    // Eliminar también todas las conexiones relacionadas con este nodo
    setState(() {
      connections.removeWhere(
        (connection) =>
            connection.source == selectedNode ||
            connection.target == selectedNode,
      );

      nodes.remove(selectedNode);
      selectedNode = null;
      _hasUnsavedChanges = true;
    });
  }

  void _showSnackBar(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message), duration: const Duration(seconds: 2)),
    );
  }

  // Mostrar diálogo de opciones para editar o eliminar una conexión
  Future<void> _showConnectionOptionsDialog(Connection connection) async {
    final result = await showDialog<String>(
      context: context,
      builder: (context) => SimpleDialog(
        title: const Text('Opciones de conexión'),
        children: [
          ListTile(
            leading: const Icon(Icons.edit),
            title: const Text('Editar etiqueta'),
            onTap: () => Navigator.of(context).pop('edit'),
          ),
          ListTile(
            leading: const Icon(Icons.delete, color: Colors.red),
            title: const Text('Eliminar conexión'),
            onTap: () => Navigator.of(context).pop('delete'),
          ),
          ListTile(
            leading: const Icon(Icons.close),
            title: const Text('Cancelar'),
            onTap: () => Navigator.of(context).pop('cancel'),
          ),
        ],
      ),
    );

    if (result == 'edit') {
      _editConnectionLabel(connection);
    } else if (result == 'delete') {
      _deleteConnection(connection);
      selectedConnection = null;
    }
  }

  // Mostrar diálogo para editar la etiqueta de una conexión existente
  Future<void> _editConnectionLabel(Connection connection) async {
    final TextEditingController labelController =
        TextEditingController(text: connection.label);

    final String? newLabel = await showDialog<String>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Editar etiqueta de conexión'),
        content: TextField(
          controller: labelController,
          decoration: const InputDecoration(
            labelText: 'Etiqueta',
            hintText: 'ej: Sí, No, Verdadero, Falso',
          ),
          autofocus: true,
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(null),
            child: const Text('Cancelar'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(context).pop(labelController.text),
            child: const Text('Actualizar'),
          ),
        ],
      ),
    );

    if (newLabel != null) {
      setState(() {
        connection.label = newLabel;
        _hasUnsavedChanges = true;
      });
      _showSnackBar('Etiqueta de conexión actualizada');
    }
  }

  // Método para validar el diagrama
  void _validateDiagram() {
    final ValidationResult result = DiagramValidator.validateDiagram(
      nodes,
      connections,
    );

    // Registrar métrica de validación
    _metricsService.trackUserAction(
      action: result.isValid ? 'validacion_exitosa' : 'validacion_fallida',
      category: 'validation',
      metadata: {
        'nodes_count': nodes.length,
        'connections_count': connections.length,
        'errors_count': result.errors.length,
        'warnings_count': result.warnings.length,
      },
    );

    _showValidationDialog(result);
  }

  // Mostrar el diálogo con los resultados de la validación
  void _showValidationDialog(ValidationResult result) {
    showDialog(
      context: context,
      builder: (context) => ValidationResultDialog(result: result),
    );
  }

  // Método para generar código
  void _generateCode() {
    // Primero validamos el diagrama
    final validationResult = DiagramValidator.validateDiagram(
      nodes,
      connections,
    );
    if (!validationResult.isValid) {
      // Si hay errores, mostramos el diálogo de validación
      _showValidationDialog(validationResult);
      return;
    }

    // Si el diagrama es válido, generamos el código en C
    final code = CodeGenerator.generateCode(
      nodes,
      connections,
      ProgrammingLanguage.c,
    );

    // Registrar métrica de generación de código
    _metricsService.trackUserAction(
      action: 'codigo_generado',
      category: 'code_generation',
      metadata: {
        'nodes_count': nodes.length,
        'connections_count': connections.length,
        'code_lines': code.split('\n').length,
        'language': 'c',
      },
    );

    _showCodeDialog(code);
  }

  // Mostrar el diálogo con el código generado
  void _showCodeDialog(String code) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Código C generado'),
        content: SizedBox(
          width: double.maxFinite,
          height: MediaQuery.of(context).size.height * 0.6,
          child: SingleChildScrollView(
            child: SelectableText(
              code,
              style: const TextStyle(fontFamily: 'monospace', fontSize: 14),
            ),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Cerrar'),
          ),
          TextButton(
            onPressed: () {
              // Copiar el código al portapapeles
              Clipboard.setData(ClipboardData(text: code));
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                  content: Text('Código copiado al portapapeles'),
                  duration: Duration(seconds: 2),
                ),
              );
              Navigator.of(context).pop();
            },
            child: const Text('Copiar'),
          ),
        ],
      ),
    );
  }

  // Mostrar diálogo para guardar el diagrama
  Future<void> _showSaveDiagramDialog() async {
    final Map<String, dynamic>? result = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (context) => SaveDiagramDialog(
        initialName: currentDiagram?.name,
        initialDescription: currentDiagram?.description,
        isUpdate: currentDiagram != null,
      ),
    );

    if (result != null && mounted) {
      final now = DateTime.now();

      try {
        if (currentDiagram == null) {
          // Crear un nuevo diagrama
          final newDiagram = SavedDiagram(
            name: result['name'],
            description: result['description'],
            createdAt: now,
            updatedAt: now,
            nodes: nodes,
            connections: connections,
          );

          final id = await _databaseService.saveDiagram(newDiagram);
          setState(() {
            currentDiagram = newDiagram.copyWith(id: id);
            _hasUnsavedChanges = false;
          });
          _showSnackBar('Diagrama guardado correctamente');
        } else {
          // Actualizar diagrama existente
          final updatedDiagram = currentDiagram!.copyWith(
            name: result['name'],
            description: result['description'],
            updatedAt: now,
            nodes: nodes,
            connections: connections,
          );

          await _databaseService.updateDiagram(updatedDiagram);
          setState(() {
            currentDiagram = updatedDiagram;
            _hasUnsavedChanges = false;
          });
          _showSnackBar('Diagrama actualizado correctamente');
        }
      } catch (e) {
        _showSnackBar('Error al guardar: ${e.toString()}');
      }
    }
  }

  // Navegar a la pantalla de carga de diagramas
  Future<void> _navigateToLoadDiagram(BuildContext context) async {
    // Verificar si hay cambios sin guardar
    if (_hasUnsavedChanges) {
      final bool? shouldProceed = await showDialog<bool>(
        context: context,
        builder: (context) => AlertDialog(
          title: const Text('Cambios sin guardar'),
          content: const Text(
            'Hay cambios sin guardar. ¿Deseas guardar antes de continuar?',
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(false),
              child: const Text('No guardar'),
            ),
            TextButton(
              onPressed: () async {
                Navigator.of(context).pop();
                await _showSaveDiagramDialog();
              },
              child: const Text('Guardar'),
            ),
            FilledButton(
              onPressed: () => Navigator.of(context).pop(null),
              child: const Text('Cancelar'),
            ),
          ],
        ),
      );

      if (shouldProceed == null) {
        return; // Operación cancelada
      }

      if (shouldProceed) {
        // El usuario eligió guardar, esperamos a que termine
        await _showSaveDiagramDialog();
      }
    }

    // Ahora navegamos a la pantalla de carga
    if (!mounted) return;

    final result = await Navigator.push<SavedDiagram?>(
      context,
      MaterialPageRoute(builder: (context) => const LoadDiagramScreen()),
    );

    // Si se seleccionó un diagrama, cargarlo
    if (result != null && mounted) {
      _loadDiagram(result);
    }
  }
}
