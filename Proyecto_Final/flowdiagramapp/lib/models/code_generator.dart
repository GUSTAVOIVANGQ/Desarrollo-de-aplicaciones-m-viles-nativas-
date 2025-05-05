import 'diagram_node.dart';

enum ProgrammingLanguage { c, python, javascript, java, pseudocode }

class CodeGenerator {
  // Convierte el diagrama en código fuente del lenguaje especificado
  static String generateCode(
    List<DiagramNode> nodes,
    List<Connection> connections,
    ProgrammingLanguage language,
  ) {
    // Verificar que exista un nodo de inicio
    final startNodes =
        nodes.where((node) => node.type == NodeType.start).toList();
    if (startNodes.isEmpty) {
      return "// Error: El diagrama debe tener un nodo de inicio";
    }
    final DiagramNode startNode = startNodes.first;

    // Por ahora, solo generamos código C
    if (language == ProgrammingLanguage.c) {
      return _generateCCode(startNode, nodes, connections);
    } else {
      return "// La generación de código para este lenguaje estará disponible próximamente";
    }
  }

  // Genera código C
  static String _generateCCode(
    DiagramNode startNode,
    List<DiagramNode> nodes,
    List<Connection> connections,
  ) {
    StringBuffer code = StringBuffer();

    code.writeln(
      "// Código C generado automáticamente a partir del diagrama de flujo",
    );
    code.writeln("// Generado el ${DateTime.now().toString()}");
    code.writeln("");
    code.writeln("#include <stdio.h>");
    code.writeln("#include <stdlib.h>");
    code.writeln("#include <stdbool.h>");
    code.writeln("");

    // Declarar variables utilizadas en el diagrama
    List<DiagramNode> variableNodes =
        nodes.where((node) => node.type == NodeType.variable).toList();
    if (variableNodes.isNotEmpty) {
      code.writeln("// Declaración de variables");
      for (var node in variableNodes) {
        code.writeln(_formatCVariableDeclaration(node.text));
      }
      code.writeln("");
    }

    // Función principal
    code.writeln("int main() {");

    // Variables para seguimiento
    Map<String, bool> processedNodes = {};

    // Comenzar la generación recursiva desde el nodo de inicio
    _generateCNodeCode(
      startNode,
      nodes,
      connections,
      code,
      "    ",
      processedNodes,
    );

    // Terminar la función principal
    code.writeln("");
    code.writeln("    return 0;");
    code.writeln("}");

    return code.toString();
  }

  // Genera código recursivamente para un nodo en C
  static void _generateCNodeCode(
    DiagramNode node,
    List<DiagramNode> allNodes,
    List<Connection> connections,
    StringBuffer code,
    String indent,
    Map<String, bool> processedNodes,
  ) {
    // Evitar ciclos infinitos
    if (processedNodes[node.id] == true) {
      return;
    }
    processedNodes[node.id] = true;

    switch (node.type) {
      case NodeType.start:
        // El nodo de inicio no genera código específico
        _processNextNodes(
          node,
          allNodes,
          connections,
          code,
          indent,
          processedNodes,
        );
        break;

      case NodeType.end:
        code.writeln("${indent}// Fin del programa");
        break;

      case NodeType.process:
        code.writeln("${indent}// Proceso: ${node.text}");
        code.writeln("${indent}${_formatCProcessStatement(node.text)};");
        _processNextNodes(
          node,
          allNodes,
          connections,
          code,
          indent,
          processedNodes,
        );
        break;

      case NodeType.decision:
        code.writeln("${indent}// Decisión: ${node.text}");
        code.writeln("${indent}if (${_formatCCondition(node.text)}) {");

        // Encontrar las conexiones de salida (true y false)
        final outConnections =
            connections.where((conn) => conn.source == node).toList();

        // Procesar rama 'true' con indentación adicional
        if (outConnections.isNotEmpty) {
          final trueConnection = outConnections.firstWhere(
            (conn) =>
                conn.label.toLowerCase().contains('true') ||
                conn.label.toLowerCase().contains('sí') ||
                conn.label.toLowerCase().contains('yes'),
            orElse: () => outConnections.first,
          );
          _generateCNodeCode(
            trueConnection.target,
            allNodes,
            connections,
            code,
            indent + "    ",
            Map.from(processedNodes),
          );
        }

        // Procesar rama 'false'
        if (outConnections.length > 1) {
          final falseConnection = outConnections.firstWhere(
            (conn) =>
                conn.label.toLowerCase().contains('false') ||
                conn.label.toLowerCase().contains('no'),
            orElse: () => outConnections[1],
          );
          code.writeln("${indent}} else {");
          _generateCNodeCode(
            falseConnection.target,
            allNodes,
            connections,
            code,
            indent + "    ",
            Map.from(processedNodes),
          );
        }

        code.writeln("${indent}}");
        break;

      case NodeType.input:
        code.writeln("${indent}// Entrada: ${node.text}");
        code.writeln("${indent}${_formatCInputStatement(node.text)};");
        _processNextNodes(
          node,
          allNodes,
          connections,
          code,
          indent,
          processedNodes,
        );
        break;

      case NodeType.output:
        code.writeln("${indent}// Salida: ${node.text}");
        code.writeln("${indent}${_formatCOutputStatement(node.text)};");
        _processNextNodes(
          node,
          allNodes,
          connections,
          code,
          indent,
          processedNodes,
        );
        break;

      case NodeType.variable:
        code.writeln("${indent}// Inicialización de variable: ${node.text}");
        code.writeln("${indent}${_formatCVariableInitialization(node.text)};");
        _processNextNodes(
          node,
          allNodes,
          connections,
          code,
          indent,
          processedNodes,
        );
        break;
    }
  }

  // Procesa los nodos siguientes conectados al nodo actual
  static void _processNextNodes(
    DiagramNode node,
    List<DiagramNode> allNodes,
    List<Connection> connections,
    StringBuffer code,
    String indent,
    Map<String, bool> processedNodes,
  ) {
    // Encuentra el siguiente nodo para procesos lineales (no decisiones)
    if (node.type != NodeType.decision) {
      final outConnections =
          connections.where((conn) => conn.source == node).toList();
      for (final connection in outConnections) {
        _generateCNodeCode(
          connection.target,
          allNodes,
          connections,
          code,
          indent,
          processedNodes,
        );
      }
    }
  }

  // Formatea una declaración de proceso para C
  static String _formatCProcessStatement(String text) {
    // Si ya contiene una asignación, usarla directamente
    if (text.contains('=') && !text.contains('==')) {
      return text;
    }

    // Intentar convertir la descripción del proceso a código C
    return text;
  }

  // Formatea una condición para C
  static String _formatCCondition(String text) {
    // Si ya contiene operadores de comparación, usarla directamente
    if (text.contains('==') ||
        text.contains('>') ||
        text.contains('<') ||
        text.contains('>=') ||
        text.contains('<=') ||
        text.contains('!=')) {
      return text;
    }

    // Intentar inferir la condición
    if (text.contains(' mayor que ')) {
      return text.replaceAll(' mayor que ', ' > ');
    } else if (text.contains(' menor que ')) {
      return text.replaceAll(' menor que ', ' < ');
    } else if (text.contains(' igual a ')) {
      return text.replaceAll(' igual a ', ' == ');
    }

    // Si no se puede inferir, usar el texto como está
    return text;
  }

  // Formatea una declaración de entrada para C
  static String _formatCInputStatement(String text) {
    String varName = text.trim();

    // Si el texto contiene una asignación o especificación de variable
    if (text.contains('=')) {
      varName = text.split('=')[0].trim();
      return "scanf(\"%d\", &$varName)";
    }

    // Si el texto contiene instrucciones sobre el tipo de variable
    if (text.toLowerCase().contains('entero') ||
        text.toLowerCase().contains('int')) {
      return "printf(\"Ingrese $varName: \"); scanf(\"%d\", &$varName)";
    } else if (text.toLowerCase().contains('flotante') ||
        text.toLowerCase().contains('float') ||
        text.toLowerCase().contains('decimal')) {
      return "printf(\"Ingrese $varName: \"); scanf(\"%f\", &$varName)";
    } else if (text.toLowerCase().contains('caracter') ||
        text.toLowerCase().contains('char')) {
      return "printf(\"Ingrese $varName: \"); scanf(\" %c\", &$varName)";
    } else {
      // Por defecto, asumir entero
      return "printf(\"Ingrese $varName: \"); scanf(\"%d\", &$varName)";
    }
  }

  // Formatea una declaración de salida para C
  static String _formatCOutputStatement(String text) {
    // Si parece una variable sola, la mostramos
    if (_isSimpleVariableName(text)) {
      return "printf(\"%d\\n\", $text)";
    }

    // Si contiene comillas, probablemente sea un mensaje formateado
    if (text.contains('"')) {
      return "printf($text)";
    }

    // Si no tiene formato especial, lo tratamos como texto a mostrar
    return "printf(\"$text\\n\")";
  }

  // Formatea una declaración de variable para C
  static String _formatCVariableDeclaration(String text) {
    if (text.contains('=')) {
      // Ya tiene inicialización, determinar tipo de variable
      String varName = text.split('=')[0].trim();
      String varValue = text.split('=')[1].trim();

      if (varValue.contains('.')) {
        return "float $text";
      } else if (varValue.contains('"') || varValue.contains("'")) {
        if (varValue.length == 3) {
          // 'a' (char con comillas simples)
          return "char $text";
        } else {
          return "char $varName[100] = $varValue";
        }
      } else {
        return "int $text";
      }
    } else if (text.toLowerCase().contains('entero') ||
        text.toLowerCase().contains('int')) {
      String varName =
          text.replaceAll('entero', '').replaceAll('int', '').trim();
      return "int $varName";
    } else if (text.toLowerCase().contains('flotante') ||
        text.toLowerCase().contains('float') ||
        text.toLowerCase().contains('decimal')) {
      String varName =
          text
              .replaceAll('flotante', '')
              .replaceAll('float', '')
              .replaceAll('decimal', '')
              .trim();
      return "float $varName";
    } else if (text.toLowerCase().contains('caracter') ||
        text.toLowerCase().contains('char')) {
      String varName =
          text.replaceAll('caracter', '').replaceAll('char', '').trim();
      return "char $varName";
    } else {
      // Por defecto, int
      return "int $text";
    }
  }

  // Formatea una inicialización de variable para C
  static String _formatCVariableInitialization(String text) {
    // Si ya contiene una asignación, usarla directamente
    if (text.contains('=')) {
      // Verificar si ya está correctamente declarada con tipo
      if (text.contains('int') ||
          text.contains('float') ||
          text.contains('double') ||
          text.contains('char')) {
        return text;
      }

      return text;
    }

    // Si no tiene asignación, asignar un valor predeterminado según contexto
    return "$text = 0";
  }

  // Verifica si el texto parece un nombre de variable simple
  static bool _isSimpleVariableName(String text) {
    final simpleName = RegExp(r'^[a-zA-Z_][a-zA-Z0-9_]*$');
    return simpleName.hasMatch(text.trim());
  }
}
