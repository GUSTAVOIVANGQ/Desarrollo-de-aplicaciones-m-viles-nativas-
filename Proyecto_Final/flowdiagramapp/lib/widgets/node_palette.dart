import 'package:flutter/material.dart';
import '../models/diagram_node.dart';

class NodePalette extends StatelessWidget {
  final Function(NodeType) onNodeSelected;

  const NodePalette({super.key, required this.onNodeSelected});

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 80,
      decoration: BoxDecoration(
        border: Border(
          right: BorderSide(color: Colors.grey.shade300, width: 1),
        ),
        color: Colors.grey.shade50,
      ),
      child: ListView(
        padding: const EdgeInsets.symmetric(vertical: 12),
        children: [
          _buildPaletteItem(
            context,
            NodeType.start,
            'Inicio',
            Icons.play_circle_outline,
            Colors.green,
          ),
          _buildPaletteItem(
            context,
            NodeType.end,
            'Fin',
            Icons.stop_circle_outlined,
            Colors.red,
          ),
          _buildPaletteItem(
            context,
            NodeType.process,
            'Proceso',
            Icons.square_outlined,
            Colors.blue,
          ),
          _buildPaletteItem(
            context,
            NodeType.decision,
            'Decisión',
            Icons.change_history_outlined,
            Colors.amber,
          ),
          _buildPaletteItem(
            context,
            NodeType.input,
            'Entrada',
            Icons.arrow_circle_down_outlined,
            Colors.purple,
          ),
          _buildPaletteItem(
            context,
            NodeType.output,
            'Salida',
            Icons.arrow_circle_up_outlined,
            Colors.indigo,
          ),
          _buildPaletteItem(
            context,
            NodeType.variable,
            'Variable',
            Icons.data_array_outlined,
            Colors.teal,
          ),
        ],
      ),
    );
  }

  Widget _buildPaletteItem(
    BuildContext context,
    NodeType type,
    String label,
    IconData icon,
    Color color,
  ) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(vertical: 8.0),
          child: Tooltip(
            message: label,
            child: InkWell(
              onTap: () => onNodeSelected(type),
              borderRadius: BorderRadius.circular(8),
              child: Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  border: Border.all(color: color.withOpacity(0.5)),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Icon(icon, color: color, size: 32),
              ),
            ),
          ),
        ),
        Text(
          label,
          style: const TextStyle(fontSize: 12),
          textAlign: TextAlign.center,
        ),
        const Divider(height: 24),
      ],
    );
  }
}
