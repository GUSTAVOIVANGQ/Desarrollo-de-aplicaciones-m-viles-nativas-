import 'package:flutter/material.dart';
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
  final Function(DiagramNode?) onNodeTap;
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
  bool isDragging = false;

  Offset _applySnapping(Offset position) {
    final snappedX = (position.dx / FlowDiagramPainter.gridSize).round() *
        FlowDiagramPainter.gridSize;
    final snappedY = (position.dy / FlowDiagramPainter.gridSize).round() *
        FlowDiagramPainter.gridSize;
    return Offset(snappedX, snappedY);
  }

  DiagramNode? _findNodeAtPosition(Offset position) {
    // Convertimos la posición del tap a coordenadas del canvas teniendo en cuenta el desplazamiento y la escala
    final localPosition = position - widget.panOffset;
    final scaledPosition = Offset(
      localPosition.dx / widget.scale,
      localPosition.dy / widget.scale,
    );

    print('Buscando nodo en posición original: $position');
    print(
        'Posición ajustada para buscar: $scaledPosition (con panOffset: ${widget.panOffset}, scale: ${widget.scale})');

    // Revisamos los nodos en orden inverso para que los que están encima (dibujados último) tengan prioridad
    for (int i = widget.nodes.length - 1; i >= 0; i--) {
      final node = widget.nodes[i];

      // Verificar si el punto está dentro del nodo
      if (node.containsPoint(scaledPosition)) {
        print('Nodo encontrado: ${node.type} en posición ${node.position}');
        print(
            'Tamaño del nodo: ${node.size}, Distancia al centro: ${(scaledPosition - (node.position + Offset(node.size.width / 2, node.size.height / 2))).distance}');
        return node;
      }
    }
    print('Ningún nodo encontrado en posición $scaledPosition');
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
      // Detectar toques simples (tap down) para almacenar el punto de inicio
      onTapDown: (details) {
        // Almacenar el punto donde se tocó
        setState(() {
          dragStart = details.localPosition;
          isDragging = false; // Resetear la bandera de arrastre
        });

        // Verificamos inmediatamente si hay un nodo en esta posición
        // para proporcionar retroalimentación instantánea
        final node = _findNodeAtPosition(details.localPosition);
        if (node != null) {
          print('TapDown en nodo: ${node.type}');
        }
      },

      onScaleStart: (details) {
        print('onScaleStart en ${details.localFocalPoint}');
        final node = _findNodeAtPosition(details.localFocalPoint);

        if (node != null) {
          print('Iniciando arrastre del nodo: ${node.type}');
          setState(() {
            draggingNode = node;
            dragStart = details.localFocalPoint;
            nodeDragStart = node.position;
          });
          // Notificar al padre para seleccionar este nodo inmediatamente
          widget.onNodeTap(node);
        } else {
          setState(() {
            dragStart = details.localFocalPoint;
          });
        }
      },

      onScaleUpdate: (details) {
        // Si el gesto ha movido lo suficiente, considerarlo como un arrastre
        if (dragStart != null &&
            (details.localFocalPoint - dragStart!).distance > 3.0) {
          isDragging = true;
        }

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

          // Guardar referencia al nodo arrastrado
          final dragged = draggingNode;

          setState(() {
            draggingNode = null;
            nodeDragStart = null;
          });

          // Mantener el nodo seleccionado después del arrastre
          widget.onNodeTap(dragged);
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
        // Para seleccionar un nodo o conexión, o deseleccionar si se toca en un espacio vacío
        if (!isLongPressing && dragStart != null && !isDragging) {
          // Solo procesamos el tap si no estamos arrastrando
          final node = _findNodeAtPosition(dragStart!);
          print('Tap detectado. Nodo encontrado: ${node?.type}');

          if (node != null) {
            // Si se encontró un nodo, notificar para seleccionar
            widget.onNodeTap(node);
          } else {
            final connection = _findConnectionAtPosition(dragStart!);
            if (connection != null && widget.onConnectionTap != null) {
              widget.onConnectionTap!(connection);
            } else {
              // Si no se tocó un nodo ni una conexión, notificar al padre para deseleccionar
              widget.onNodeTap(null);
              print('Enviando null para deseleccionar');
            }
          }
        }

        // Resetear el estado de arrastre
        setState(() {
          isDragging = false;
        });
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
    // Aplicar transformación para pan y zoom
    canvas.save();
    canvas.translate(panOffset.dx, panOffset.dy);
    canvas.scale(scale);

    // Dibujar cuadrícula
    _drawGrid(canvas, size);

    // Dibujar conexiones
    for (final connection in connections) {
      _drawConnection(canvas, connection);
    }

    // Dibujar nodos
    for (final node in nodes) {
      _drawNode(canvas, node);
    }

    canvas.restore();
  }

  void _drawGrid(Canvas canvas, Size size) {
    final width = size.width / scale;
    final height = size.height / scale;

    final startX = (-panOffset.dx / scale / gridSize).floor() * gridSize;
    final startY = (-panOffset.dy / scale / gridSize).floor() * gridSize;

    // Líneas verticales
    for (double x = startX; x <= startX + width; x += gridSize) {
      canvas.drawLine(
        Offset(x, startY),
        Offset(x, startY + height),
        gridPaint,
      );
    }

    // Líneas horizontales
    for (double y = startY; y <= startY + height; y += gridSize) {
      canvas.drawLine(
        Offset(startX, y),
        Offset(startX + width, y),
        gridPaint,
      );
    }
  }

  void _drawNode(Canvas canvas, DiagramNode node) {
    canvas.save();
    canvas.translate(node.position.dx, node.position.dy);

    // Obtener la forma del nodo según su tipo
    final path = node.getPath();

    // Dibujar el fondo del nodo
    canvas.drawPath(path, nodeFillPaint);

    // Dibujar el borde con estilo adecuado según el estado del nodo
    if (node == selectedNode) {
      canvas.drawPath(path, selectedNodePaint);
    } else if (node == draggingNode) {
      canvas.drawPath(path, draggingNodePaint);
    } else {
      canvas.drawPath(path, nodeStrokePaint);
    }

    // Dibujar texto del nodo
    _drawNodeText(canvas, node);

    canvas.restore();
  }

  void _drawNodeText(Canvas canvas, DiagramNode node) {
    final textSpan = TextSpan(
      text: node.text,
      style: nodeTextStyle,
    );

    final textPainter = TextPainter(
      text: textSpan,
      textAlign: TextAlign.center,
      textDirection: TextDirection.ltr,
    );

    textPainter.layout(maxWidth: node.size.width - 10);

    final xCenter = (node.size.width - textPainter.width) / 2;
    final yCenter = (node.size.height - textPainter.height) / 2;
    textPainter.paint(canvas, Offset(xCenter, yCenter));
  }

  void _drawConnection(Canvas canvas, Connection connection) {
    final points = connection.getConnectionPoints();
    if (points.length < 2) return;

    final start = points[0];
    final end = points[1];

    // Dibujar la línea
    final path = Path();
    path.moveTo(start.dx, start.dy);
    path.lineTo(end.dx, end.dy);
    canvas.drawPath(path, connectionPaint);

    // Dibujar la flecha
    _drawArrow(canvas, start, end);

    // Dibujar la etiqueta si existe
    if (connection.label.isNotEmpty) {
      _drawConnectionLabel(canvas, connection, start, end);
    }
  }

  void _drawArrow(Canvas canvas, Offset start, Offset end) {
    final direction = (end - start).normalize();
    final perpendicular = Offset(-direction.dy, direction.dx);

    final arrowBase = end - direction * arrowSize;
    final arrowLeft = arrowBase - perpendicular * arrowSize / 2;
    final arrowRight = arrowBase + perpendicular * arrowSize / 2;

    final arrowPath = Path()
      ..moveTo(end.dx, end.dy)
      ..lineTo(arrowLeft.dx, arrowLeft.dy)
      ..lineTo(arrowRight.dx, arrowRight.dy)
      ..close();

    canvas.drawPath(arrowPath, Paint()..color = Colors.black);
  }

  void _drawConnectionLabel(
      Canvas canvas, Connection connection, Offset start, Offset end) {
    final midpoint = Offset(
      (start.dx + end.dx) / 2,
      (start.dy + end.dy) / 2,
    );

    final textSpan = TextSpan(
      text: connection.label,
      style: const TextStyle(
        fontSize: 12,
        color: Colors.black,
        backgroundColor: Color(0xBBFFFFFF),
      ),
    );
    final textPainter = TextPainter(
      text: textSpan,
      textAlign: TextAlign.center,
      textDirection: TextDirection.ltr,
    );

    textPainter.layout();

    // Desplazamiento pequeño para que no esté directamente sobre la línea
    final offset = Offset(
      midpoint.dx - textPainter.width / 2,
      midpoint.dy - textPainter.height - 5,
    );

    // Dibujar un rectángulo de fondo
    final rect = Rect.fromLTWH(
      offset.dx - 2,
      offset.dy - 2,
      textPainter.width + 4,
      textPainter.height + 4,
    );
    canvas.drawRect(
      rect,
      Paint()..color = Colors.white.withOpacity(0.8),
    );

    textPainter.paint(canvas, offset);
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
