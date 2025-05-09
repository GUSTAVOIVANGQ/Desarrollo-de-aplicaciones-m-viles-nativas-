import 'package:flutter/material.dart';
import 'package:flutter/gestures.dart';
import '../models/diagram_node.dart';
import 'dart:math' as math;

extension OffsetExtensions on Offset {
  Offset normalize() {
    final length = math.sqrt(dx * dx + dy * dy);
    return length == 0 ? Offset.zero : Offset(dx / length, dy / length);
  }
}

class FlowDiagramCanvas extends StatefulWidget {
  final List<DiagramNode> nodes;
  final List<Connection> connections;
  final DiagramNode? selectedNode;
  final Offset panOffset;
  final double scale;
  final Function(DragUpdateDetails) onPanUpdate;
  final Function(ScaleUpdateDetails) onScaleUpdate;
  final Function(DiagramNode) onNodeTap;
  final Function(DiagramNode) onNodeLongPress;
  final Function(DiagramNode, Offset) onNodeDragUpdate;
  final Function(Connection)? onConnectionTap;

  const FlowDiagramCanvas({
    super.key,
    required this.nodes,
    required this.connections,
    this.selectedNode,
    required this.panOffset,
    required this.scale,
    required this.onPanUpdate,
    required this.onScaleUpdate,
    required this.onNodeTap,
    required this.onNodeLongPress,
    required this.onNodeDragUpdate,
    this.onConnectionTap,
  });

  @override
  State<FlowDiagramCanvas> createState() => _FlowDiagramCanvasState();
}

class _FlowDiagramCanvasState extends State<FlowDiagramCanvas> {
  DiagramNode? draggingNode;
  Offset? dragStart;
  Offset? nodeDragStart;
  bool isLongPressing = false;
  bool isSnappingEnabled = false;

  Offset _applySnapping(Offset position) {
    final snappedX = (position.dx / FlowDiagramPainter.gridSize).round() *
        FlowDiagramPainter.gridSize;
    final snappedY = (position.dy / FlowDiagramPainter.gridSize).round() *
        FlowDiagramPainter.gridSize;
    return Offset(snappedX, snappedY);
  }

  DiagramNode? _findNodeAtPosition(Offset position) {
    final localPosition = position - widget.panOffset;
    final scaledPosition = Offset(
      localPosition.dx / widget.scale,
      localPosition.dy / widget.scale,
    );

    for (int i = widget.nodes.length - 1; i >= 0; i--) {
      final node = widget.nodes[i];
      if (node.containsPoint(scaledPosition)) {
        return node;
      }
    }
    return null;
  }

  Connection? _findConnectionAtPosition(Offset position) {
    final localPosition = position - widget.panOffset;
    final scaledPosition = Offset(
      localPosition.dx / widget.scale,
      localPosition.dy / widget.scale,
    );

    const double hitDistance = 10.0;

    for (final connection in widget.connections) {
      final points = connection.getConnectionPoints();
      if (points.length < 2) continue;

      final start = points[0];
      final end = points[1];

      final distance = _distanceToLine(scaledPosition, start, end);

      if (distance < hitDistance) {
        return connection;
      }
    }

    return null;
  }

  double _distanceToLine(Offset point, Offset lineStart, Offset lineEnd) {
    final double lineLength = (lineEnd - lineStart).distance;
    if (lineLength == 0) return double.infinity;

    final double t = ((point.dx - lineStart.dx) * (lineEnd.dx - lineStart.dx) +
            (point.dy - lineStart.dy) * (lineEnd.dy - lineStart.dy)) /
        (lineLength * lineLength);

    if (t < 0) return (point - lineStart).distance;
    if (t > 1) return (point - lineEnd).distance;

    final projection = Offset(
      lineStart.dx + t * (lineEnd.dx - lineStart.dx),
      lineStart.dy + t * (lineEnd.dy - lineStart.dy),
    );

    return (point - projection).distance;
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      behavior: HitTestBehavior.opaque,
      // Usar onScaleStart/Update/End para manejar tanto arrastre como escala
      onScaleStart: (details) {
        final node = _findNodeAtPosition(details.localFocalPoint);

        if (node != null) {
          setState(() {
            draggingNode = node;
            dragStart = details.localFocalPoint;
            nodeDragStart = node.position;
          });
        } else {
          setState(() {
            dragStart = details.localFocalPoint;
          });
        }
      },

      onScaleUpdate: (details) {
        if (draggingNode != null &&
            dragStart != null &&
            nodeDragStart != null &&
            !isLongPressing) {
          // Si estamos arrastrando un nodo
          final rawDelta = details.localFocalPoint - dragStart!;
          final adjustedDelta = rawDelta / widget.scale;

          // Calcular nueva posición
          final newPosition = nodeDragStart! + adjustedDelta;

          // Actualizar posición directamente
          draggingNode!.position = newPosition;

          // Forzar redibujado sin reconstruir el widget tree
          context.findRenderObject()?.markNeedsPaint();

          // Notificar al padre del cambio
          widget.onNodeDragUpdate(draggingNode!, newPosition);
        } else if (details.scale != 1.0 && !isLongPressing) {
          // Si estamos haciendo zoom
          widget.onScaleUpdate(details);
        } else if (!isLongPressing && details.scale == 1.0) {
          // Si estamos moviendo el canvas (pan)
          widget.onPanUpdate(
            DragUpdateDetails(
              globalPosition: details.localFocalPoint,
              delta: details.focalPointDelta,
            ),
          );
        }
      },

      onScaleEnd: (details) {
        if (draggingNode != null) {
          // Aplicar ajuste a cuadrícula si está habilitado
          if (isSnappingEnabled) {
            final snappedPosition = _applySnapping(draggingNode!.position);
            if (snappedPosition != draggingNode!.position) {
              widget.onNodeDragUpdate(draggingNode!, snappedPosition);
            }
          }

          // Limpiar estado
          setState(() {
            draggingNode = null;
            dragStart = null;
            nodeDragStart = null;
          });
        }
      },

      onLongPress: () {
        // Para iniciar conexión entre nodos
        if (dragStart != null) {
          final node = _findNodeAtPosition(dragStart!);
          if (node != null) {
            setState(() {
              isLongPressing = true;
            });
            widget.onNodeLongPress(node);
          }
        }
      },

      onLongPressEnd: (details) {
        setState(() {
          isLongPressing = false;
        });
      },

      onTap: () {
        // Para seleccionar un nodo o conexión
        if (!isLongPressing && dragStart != null) {
          final node = _findNodeAtPosition(dragStart!);
          if (node != null) {
            widget.onNodeTap(node);
          } else {
            final connection = _findConnectionAtPosition(dragStart!);
            if (connection != null && widget.onConnectionTap != null) {
              widget.onConnectionTap!(connection);
            }
          }
        }
      },

      child: Container(
        color: Colors.grey[100],
        child: ClipRect(
          child: CustomPaint(
            painter: FlowDiagramPainter(
              nodes: widget.nodes,
              connections: widget.connections,
              selectedNode: widget.selectedNode,
              draggingNode: draggingNode,
              panOffset: widget.panOffset,
              scale: widget.scale,
            ),
            child: Container(),
          ),
        ),
      ),
    );
  }
}

class FlowDiagramPainter extends CustomPainter {
  final List<DiagramNode> nodes;
  final List<Connection> connections;
  final DiagramNode? selectedNode;
  final DiagramNode? draggingNode;
  final Offset panOffset;
  final double scale;

  static const gridSize = 20.0;
  static const arrowSize = 10.0;

  final Paint gridPaint = Paint()
    ..color = Colors.grey.withOpacity(0.2)
    ..style = PaintingStyle.stroke
    ..strokeWidth = 1.0;

  final Paint nodeFillPaint = Paint()
    ..color = Colors.white
    ..style = PaintingStyle.fill;

  final Paint nodeStrokePaint = Paint()
    ..color = Colors.black
    ..style = PaintingStyle.stroke
    ..strokeWidth = 1.5;

  final Paint selectedNodePaint = Paint()
    ..color = Colors.blue
    ..style = PaintingStyle.stroke
    ..strokeWidth = 2.5;

  final Paint draggingNodePaint = Paint()
    ..color = Colors.blue.withOpacity(0.7)
    ..style = PaintingStyle.stroke
    ..strokeWidth = 2.0;

  final Paint connectionPaint = Paint()
    ..color = Colors.black
    ..style = PaintingStyle.stroke
    ..strokeWidth = 1.5;

  final TextStyle nodeTextStyle = const TextStyle(
    fontSize: 14,
    color: Colors.black87,
  );

  FlowDiagramPainter({
    required this.nodes,
    required this.connections,
    this.selectedNode,
    this.draggingNode,
    required this.panOffset,
    required this.scale,
  });

  @override
  void paint(Canvas canvas, Size size) {
    canvas.save();

    canvas.translate(panOffset.dx, panOffset.dy);
    canvas.scale(scale, scale);

    _drawGrid(canvas, size);

    for (final connection in connections) {
      _drawConnection(canvas, connection);
    }

    for (final node in nodes) {
      final bool isSelected = node == selectedNode;
      final bool isDragging = node == draggingNode;

      _drawNode(canvas, node, isSelected, isDragging);
    }

    canvas.restore();
  }

  void _drawGrid(Canvas canvas, Size size) {
    final visibleRect = Rect.fromLTWH(
      -panOffset.dx / scale,
      -panOffset.dy / scale,
      size.width / scale,
      size.height / scale,
    );

    double y = (visibleRect.top / gridSize).floor() * gridSize;
    while (y < visibleRect.bottom) {
      canvas.drawLine(
        Offset(visibleRect.left, y),
        Offset(visibleRect.right, y),
        gridPaint,
      );
      y += gridSize;
    }

    double x = (visibleRect.left / gridSize).floor() * gridSize;
    while (x < visibleRect.right) {
      canvas.drawLine(
        Offset(x, visibleRect.top),
        Offset(x, visibleRect.bottom),
        gridPaint,
      );
      x += gridSize;
    }
  }

  void _drawNode(
      Canvas canvas, DiagramNode node, bool isSelected, bool isDragging) {
    canvas.save();

    canvas.translate(node.position.dx, node.position.dy);

    final path = node.getPath();

    if (isDragging) {
      final shadowPaint = Paint()
        ..color = Colors.black.withOpacity(0.2)
        ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 4.0);

      canvas.drawPath(path, shadowPaint);
      canvas.drawPath(path, nodeFillPaint);
      canvas.drawPath(path, draggingNodePaint);
    } else {
      canvas.drawPath(path, nodeFillPaint);
      canvas.drawPath(path, isSelected ? selectedNodePaint : nodeStrokePaint);
    }

    final textSpan = TextSpan(
      text: node.text.isEmpty ? _getDefaultNodeText(node.type) : node.text,
      style: nodeTextStyle,
    );

    final textPainter = TextPainter(
      text: textSpan,
      textDirection: TextDirection.ltr,
      textAlign: TextAlign.center,
    );

    textPainter.layout(minWidth: 0, maxWidth: node.size.width - 20);

    textPainter.paint(
      canvas,
      Offset(
        (node.size.width - textPainter.width) / 2,
        (node.size.height - textPainter.height) / 2,
      ),
    );

    canvas.restore();
  }

  void _drawConnection(Canvas canvas, Connection connection) {
    final points = connection.getConnectionPoints();

    if (points.length >= 2) {
      final start = points[0];
      final end = points[1];

      final path = Path();
      path.moveTo(start.dx, start.dy);
      path.lineTo(end.dx, end.dy);

      canvas.drawPath(path, connectionPaint);

      _drawArrow(canvas, end, start);

      if (connection.label.isNotEmpty) {
        final midPoint = Offset(
          (start.dx + end.dx) / 2,
          (start.dy + end.dy) / 2,
        );

        final textSpan = TextSpan(
          text: connection.label,
          style: const TextStyle(
            fontSize: 12,
            color: Colors.black87,
            backgroundColor: Colors.white70,
          ),
        );

        final textPainter = TextPainter(
          text: textSpan,
          textDirection: TextDirection.ltr,
        );

        textPainter.layout();

        textPainter.paint(
          canvas,
          Offset(
            midPoint.dx - textPainter.width / 2,
            midPoint.dy - textPainter.height / 2,
          ),
        );
      }
    }
  }

  void _drawArrow(Canvas canvas, Offset tip, Offset from) {
    final direction = (tip - from).normalize();

    final angle = math.atan2(direction.dy, direction.dx);

    final p1 = tip -
        Offset(
          arrowSize * math.cos(angle - math.pi / 6),
          arrowSize * math.sin(angle - math.pi / 6),
        );

    final p2 = tip -
        Offset(
          arrowSize * math.cos(angle + math.pi / 6),
          arrowSize * math.sin(angle + math.pi / 6),
        );

    final arrowPath = Path()
      ..moveTo(tip.dx, tip.dy)
      ..lineTo(p1.dx, p1.dy)
      ..lineTo(p2.dx, p2.dy)
      ..close();

    canvas.drawPath(arrowPath, connectionPaint);
  }

  String _getDefaultNodeText(NodeType type) {
    switch (type) {
      case NodeType.start:
        return 'Inicio';
      case NodeType.end:
        return 'Fin';
      case NodeType.process:
        return 'Proceso';
      case NodeType.decision:
        return '¿Condición?';
      case NodeType.input:
        return 'Entrada';
      case NodeType.output:
        return 'Salida';
      case NodeType.variable:
        return 'Variable';
    }
  }

  @override
  bool shouldRepaint(FlowDiagramPainter oldDelegate) {
    return oldDelegate.nodes != nodes ||
        oldDelegate.connections != connections ||
        oldDelegate.selectedNode != selectedNode ||
        oldDelegate.draggingNode != draggingNode ||
        oldDelegate.panOffset != panOffset ||
        oldDelegate.scale != scale;
  }
}
