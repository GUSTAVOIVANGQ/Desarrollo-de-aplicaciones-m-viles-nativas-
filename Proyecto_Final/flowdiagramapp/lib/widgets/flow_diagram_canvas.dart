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
  });

  @override
  State<FlowDiagramCanvas> createState() => _FlowDiagramCanvasState();
}

class _FlowDiagramCanvasState extends State<FlowDiagramCanvas> {
  DiagramNode? draggingNode;
  Offset? dragStart;
  Offset? nodeDragStart;
  bool isLongPressing = false;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      behavior: HitTestBehavior.opaque,
      onScaleStart: _handleScaleStart,
      onScaleUpdate: _handleScaleUpdate,
      onScaleEnd: _handleScaleEnd,
      onTapUp: _handleTapUp,
      onLongPressStart: _handleLongPressStart,
      onLongPressEnd: _handleLongPressEnd,
      child: Container(
        color: Colors.grey[100],
        child: ClipRect(
          child: CustomPaint(
            painter: FlowDiagramPainter(
              nodes: widget.nodes,
              connections: widget.connections,
              selectedNode: widget.selectedNode,
              panOffset: widget.panOffset,
              scale: widget.scale,
            ),
            child: Container(),
          ),
        ),
      ),
    );
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

  void _handleScaleStart(ScaleStartDetails details) {
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
  }

  void _handleScaleUpdate(ScaleUpdateDetails details) {
    if (draggingNode != null &&
        dragStart != null &&
        nodeDragStart != null &&
        !isLongPressing) {
      final delta = (details.localFocalPoint - dragStart!) / widget.scale;
      final newPosition = nodeDragStart! + delta;
      widget.onNodeDragUpdate(draggingNode!, newPosition);
    } else if (details.scale == 1.0 && !isLongPressing) {
      widget.onPanUpdate(
        DragUpdateDetails(
          globalPosition: details.localFocalPoint,
          delta: details.focalPointDelta,
        ),
      );
    } else if (!isLongPressing) {
      widget.onScaleUpdate(details);
    }
  }

  void _handleScaleEnd(ScaleEndDetails details) {
    setState(() {
      draggingNode = null;
      dragStart = null;
      nodeDragStart = null;
    });
  }

  void _handleTapUp(TapUpDetails details) {
    if (draggingNode == null && !isLongPressing) {
      final node = _findNodeAtPosition(details.localPosition);
      if (node != null) {
        widget.onNodeTap(node);
      }
    }
  }

  void _handleLongPressStart(LongPressStartDetails details) {
    final node = _findNodeAtPosition(details.localPosition);
    if (node != null) {
      setState(() {
        isLongPressing = true;
      });
      widget.onNodeLongPress(node);
    }
  }

  void _handleLongPressEnd(LongPressEndDetails details) {
    setState(() {
      isLongPressing = false;
    });
  }
}

class FlowDiagramPainter extends CustomPainter {
  final List<DiagramNode> nodes;
  final List<Connection> connections;
  final DiagramNode? selectedNode;
  final Offset panOffset;
  final double scale;

  static const gridSize = 20.0;
  static const arrowSize = 10.0;

  final Paint gridPaint =
      Paint()
        ..color = Colors.grey.withOpacity(0.2)
        ..style = PaintingStyle.stroke
        ..strokeWidth = 1.0;

  final Paint nodeFillPaint =
      Paint()
        ..color = Colors.white
        ..style = PaintingStyle.fill;

  final Paint nodeStrokePaint =
      Paint()
        ..color = Colors.black
        ..style = PaintingStyle.stroke
        ..strokeWidth = 1.5;

  final Paint selectedNodePaint =
      Paint()
        ..color = Colors.blue
        ..style = PaintingStyle.stroke
        ..strokeWidth = 2.5;

  final Paint connectionPaint =
      Paint()
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
      _drawNode(canvas, node, node == selectedNode);
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

  void _drawNode(Canvas canvas, DiagramNode node, bool isSelected) {
    canvas.save();

    canvas.translate(node.position.dx, node.position.dy);

    final path = node.getPath();

    canvas.drawPath(path, nodeFillPaint);
    canvas.drawPath(path, isSelected ? selectedNodePaint : nodeStrokePaint);

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

    final p1 =
        tip -
        Offset(
          arrowSize * math.cos(angle - math.pi / 6),
          arrowSize * math.sin(angle - math.pi / 6),
        );

    final p2 =
        tip -
        Offset(
          arrowSize * math.cos(angle + math.pi / 6),
          arrowSize * math.sin(angle + math.pi / 6),
        );

    final arrowPath =
        Path()
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
        oldDelegate.panOffset != panOffset ||
        oldDelegate.scale != scale;
  }
}
