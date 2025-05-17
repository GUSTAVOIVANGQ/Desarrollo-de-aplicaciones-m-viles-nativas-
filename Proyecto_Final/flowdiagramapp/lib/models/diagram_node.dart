import 'package:flutter/material.dart';

enum NodeType { start, end, process, decision, input, output, variable }

class DiagramNode {
  final String id;
  final NodeType type;
  Offset position;
  String text;

  // Dimensiones del nodo (pueden variar según el tipo)
  Size get size {
    switch (type) {
      case NodeType.start:
      case NodeType.end:
        return const Size(120, 60);
      case NodeType.decision:
        return const Size(140, 100);
      case NodeType.process:
      case NodeType.input:
      case NodeType.output:
      case NodeType.variable:
        return const Size(160, 80);
    }
  }

  DiagramNode({
    required this.id,
    required this.type,
    required this.position,
    this.text = '',
  });

  // Obtener la forma del nodo según su tipo
  Path getPath() {
    final Path path = Path();

    switch (type) {
      case NodeType.start:
      case NodeType.end:
        // Óvalo para inicio/fin
        path.addOval(
          Rect.fromCenter(
            center: Offset(size.width / 2, size.height / 2),
            width: size.width,
            height: size.height,
          ),
        );
        break;

      case NodeType.process:
      case NodeType.variable:
        // Rectángulo para proceso/variable
        path.addRect(Rect.fromLTWH(0, 0, size.width, size.height));
        break;

      case NodeType.decision:
        // Rombo para decisión
        path.moveTo(size.width / 2, 0);
        path.lineTo(size.width, size.height / 2);
        path.lineTo(size.width / 2, size.height);
        path.lineTo(0, size.height / 2);
        path.close();
        break;

      case NodeType.input:
      case NodeType.output:
        // Paralelogramo para entrada/salida
        final double offset = size.width * 0.15;
        path.moveTo(offset, 0);
        path.lineTo(size.width, 0);
        path.lineTo(size.width - offset, size.height);
        path.lineTo(0, size.height);
        path.close();
        break;
    }

    return path;
  }

  // Verificar si un punto está dentro del nodo
  bool containsPoint(Offset point) {
    // Asegurarse de que el punto está en coordenadas locales al nodo
    final localPoint = point - position;

    // Para detección más precisa, añadimos una verificación simplificada usando un rectángulo
    if (localPoint.dx >= 0 &&
        localPoint.dx <= size.width &&
        localPoint.dy >= 0 &&
        localPoint.dy <= size.height) {
      // Si estamos dentro del rectángulo delimitador, verificamos la forma exacta
      return getPath().contains(localPoint);
    }

    return false;
  }

  // Puntos de conexión para las líneas
  Offset getInputPoint() {
    return Offset(position.dx + size.width / 2, position.dy);
  }

  Offset getOutputPoint() {
    return Offset(position.dx + size.width / 2, position.dy + size.height);
  }

  Offset getLeftPoint() {
    return Offset(position.dx, position.dy + size.height / 2);
  }

  Offset getRightPoint() {
    return Offset(position.dx + size.width, position.dy + size.height / 2);
  }

  // Encontrar el punto más cercano para una conexión
  Offset getNearestConnectionPoint(Offset target) {
    final List<Offset> points = [
      getInputPoint(),
      getOutputPoint(),
      getLeftPoint(),
      getRightPoint(),
    ];

    Offset nearest = points.first;
    double minDistance = (target - nearest).distance;

    for (Offset point in points) {
      final distance = (target - point).distance;
      if (distance < minDistance) {
        minDistance = distance;
        nearest = point;
      }
    }

    return nearest;
  }
}

class Connection {
  final DiagramNode source;
  final DiagramNode target;
  String label;

  Connection({required this.source, required this.target, this.label = ''});

  // Calcular los puntos de conexión entre nodos
  List<Offset> getConnectionPoints() {
    final sourceCenter =
        source.position + Offset(source.size.width / 2, source.size.height / 2);
    final targetCenter =
        target.position + Offset(target.size.width / 2, target.size.height / 2);

    final sourcePoint = source.getNearestConnectionPoint(targetCenter);
    final targetPoint = target.getNearestConnectionPoint(sourceCenter);

    return [sourcePoint, targetPoint];
  }
}
