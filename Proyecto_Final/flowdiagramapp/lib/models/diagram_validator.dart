import 'diagram_node.dart';

class ValidationResult {
  final bool isValid;
  final List<String> errors;
  final List<String> warnings;

  ValidationResult({
    this.isValid = true,
    List<String>? errors,
    List<String>? warnings,
  }) : errors = errors ?? [],
       warnings = warnings ?? [];

  ValidationResult.withError(String error)
    : isValid = false,
      errors = [error],
      warnings = [];

  ValidationResult.withWarning(String warning)
    : isValid = true,
      errors = [],
      warnings = [warning];

  ValidationResult merge(ValidationResult other) {
    return ValidationResult(
      isValid: isValid && other.isValid,
      errors: [...errors, ...other.errors],
      warnings: [...warnings, ...other.warnings],
    );
  }
}

class DiagramValidator {
  /// Validación principal del diagrama
  static ValidationResult validateDiagram(
    List<DiagramNode> nodes,
    List<Connection> connections,
  ) {
    // Resultado inicial válido
    ValidationResult result = ValidationResult();

    // Validar que exista al menos un nodo
    if (nodes.isEmpty) {
      return ValidationResult.withError(
        "El diagrama está vacío. Agrega al menos un nodo de inicio.",
      );
    }

    // Validar presencia de nodo de inicio y fin
    final startNodeValidation = _validateStartNode(nodes);
    final endNodeValidation = _validateEndNode(nodes);

    // Combinar resultados
    result = result.merge(startNodeValidation);
    result = result.merge(endNodeValidation);

    // Validar conexiones
    final connectionValidation = _validateConnections(nodes, connections);
    result = result.merge(connectionValidation);

    // Validar que no haya nodos sueltos (excepto el nodo final)
    final disconnectedValidation = _validateNoDisconnectedNodes(
      nodes,
      connections,
    );
    result = result.merge(disconnectedValidation);

    return result;
  }

  /// Validar que exista un único nodo de inicio
  static ValidationResult _validateStartNode(List<DiagramNode> nodes) {
    final startNodes =
        nodes.where((node) => node.type == NodeType.start).toList();

    if (startNodes.isEmpty) {
      return ValidationResult.withError(
        "El diagrama debe tener un nodo de inicio.",
      );
    }

    if (startNodes.length > 1) {
      return ValidationResult.withError(
        "El diagrama solo puede tener un nodo de inicio.",
      );
    }

    return ValidationResult();
  }

  /// Validar que exista al menos un nodo de fin
  static ValidationResult _validateEndNode(List<DiagramNode> nodes) {
    final endNodes = nodes.where((node) => node.type == NodeType.end).toList();

    if (endNodes.isEmpty) {
      return ValidationResult.withError(
        "El diagrama debe tener al menos un nodo de fin.",
      );
    }

    return ValidationResult();
  }

  /// Validar conexiones lógicas entre nodos
  static ValidationResult _validateConnections(
    List<DiagramNode> nodes,
    List<Connection> connections,
  ) {
    ValidationResult result = ValidationResult();

    // Validar que el nodo de inicio tenga al menos una salida
    final startNodes =
        nodes.where((node) => node.type == NodeType.start).toList();
    if (startNodes.isNotEmpty) {
      final startNode = startNodes.first;
      final startNodeOutputs =
          connections.where((conn) => conn.source == startNode).toList();

      if (startNodeOutputs.isEmpty) {
        result = result.merge(
          ValidationResult.withError(
            "El nodo de inicio debe tener al menos una conexión de salida.",
          ),
        );
      }
    }

    // Validar que el nodo de fin no tenga salidas
    final endNodes = nodes.where((node) => node.type == NodeType.end).toList();
    for (final endNode in endNodes) {
      final endNodeOutputs =
          connections.where((conn) => conn.source == endNode).toList();

      if (endNodeOutputs.isNotEmpty) {
        result = result.merge(
          ValidationResult.withError(
            "Un nodo de fin no puede tener conexiones de salida.",
          ),
        );
        break;
      }
    }

    // Validar que los nodos de decisión tengan al menos dos salidas
    final decisionNodes =
        nodes.where((node) => node.type == NodeType.decision).toList();
    for (final decisionNode in decisionNodes) {
      final outputs =
          connections.where((conn) => conn.source == decisionNode).toList();

      if (outputs.isEmpty) {
        result = result.merge(
          ValidationResult.withWarning(
            "El nodo de decisión '${decisionNode.text}' no tiene conexiones de salida.",
          ),
        );
      } else if (outputs.length < 2) {
        result = result.merge(
          ValidationResult.withWarning(
            "El nodo de decisión '${decisionNode.text}' debe tener al menos dos salidas (verdadero/falso).",
          ),
        );
      }
    }

    return result;
  }

  /// Validar que no haya nodos desconectados (excepto posiblemente el de inicio)
  static ValidationResult _validateNoDisconnectedNodes(
    List<DiagramNode> nodes,
    List<Connection> connections,
  ) {
    ValidationResult result = ValidationResult();

    for (final node in nodes) {
      // Ignorar el nodo de inicio en esta validación
      if (node.type == NodeType.start) continue;

      // Verificar si el nodo tiene conexiones entrantes
      final incomingConnections =
          connections.where((conn) => conn.target == node).toList();

      // Verificar si el nodo tiene conexiones salientes (excepto para los nodos de fin)
      final outgoingConnections =
          connections.where((conn) => conn.source == node).toList();

      if (incomingConnections.isEmpty) {
        result = result.merge(
          ValidationResult.withWarning(
            "El nodo '${node.text.isEmpty ? _getNodeTypeName(node.type) : node.text}' no tiene conexiones entrantes.",
          ),
        );
      }

      if (outgoingConnections.isEmpty && node.type != NodeType.end) {
        result = result.merge(
          ValidationResult.withWarning(
            "El nodo '${node.text.isEmpty ? _getNodeTypeName(node.type) : node.text}' no tiene conexiones salientes.",
          ),
        );
      }
    }

    return result;
  }

  static String _getNodeTypeName(NodeType type) {
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
