import 'package:flutter/material.dart';
import '../models/diagram_node.dart';

class NodeEditorDialog extends StatefulWidget {
  final DiagramNode node;

  const NodeEditorDialog({super.key, required this.node});

  @override
  State<NodeEditorDialog> createState() => _NodeEditorDialogState();
}

class _NodeEditorDialogState extends State<NodeEditorDialog> {
  late TextEditingController _textController;

  @override
  void initState() {
    super.initState();
    _textController = TextEditingController(text: widget.node.text);
  }

  @override
  void dispose() {
    _textController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    String dialogTitle;
    String hintText;

    switch (widget.node.type) {
      case NodeType.start:
        dialogTitle = 'Editar Nodo de Inicio';
        hintText = 'Texto opcional para el nodo de inicio';
        break;
      case NodeType.end:
        dialogTitle = 'Editar Nodo de Fin';
        hintText = 'Texto opcional para el nodo de fin';
        break;
      case NodeType.process:
        dialogTitle = 'Editar Proceso';
        hintText = 'Describe el proceso (ej: suma = a + b)';
        break;
      case NodeType.decision:
        dialogTitle = 'Editar Condición';
        hintText = '¿Condición? (ej: edad >= 18)';
        break;
      case NodeType.input:
        dialogTitle = 'Editar Entrada';
        hintText = 'Describe la entrada (ej: leer variable)';
        break;
      case NodeType.output:
        dialogTitle = 'Editar Salida';
        hintText = 'Describe la salida (ej: mostrar resultado)';
        break;
      case NodeType.variable:
        dialogTitle = 'Editar Variable';
        hintText = 'Declaración de variable (ej: int contador = 0)';
        break;
    }

    return AlertDialog(
      title: Text(dialogTitle),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Tipo: ${_getNodeTypeName(widget.node.type)}',
            style: const TextStyle(fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 16),
          TextField(
            controller: _textController,
            decoration: InputDecoration(
              labelText: 'Texto',
              hintText: hintText,
              border: const OutlineInputBorder(),
            ),
            maxLines: 3,
          ),
        ],
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('Cancelar'),
        ),
        FilledButton(
          onPressed: () {
            Navigator.of(context).pop(_textController.text);
          },
          child: const Text('Guardar'),
        ),
      ],
    );
  }

  String _getNodeTypeName(NodeType type) {
    switch (type) {
      case NodeType.start:
        return 'Inicio';
      case NodeType.end:
        return 'Fin';
      case NodeType.process:
        return 'Proceso';
      case NodeType.decision:
        return 'Decisión';
      case NodeType.input:
        return 'Entrada';
      case NodeType.output:
        return 'Salida';
      case NodeType.variable:
        return 'Variable';
    }
  }
}
