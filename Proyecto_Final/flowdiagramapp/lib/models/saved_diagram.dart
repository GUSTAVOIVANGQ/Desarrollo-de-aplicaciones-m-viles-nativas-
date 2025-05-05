import 'dart:convert';
import '../models/diagram_node.dart';
import 'dart:ui';

class SavedDiagram {
  final int? id;
  final String name;
  final String description;
  final DateTime createdAt;
  final DateTime updatedAt;
  final List<DiagramNode> nodes;
  final List<Connection> connections;
  final bool isTemplate;

  SavedDiagram({
    this.id,
    required this.name,
    this.description = '',
    required this.createdAt,
    required this.updatedAt,
    required this.nodes,
    required this.connections,
    this.isTemplate = false,
  });

  // Crear una copia del diagrama con un nuevo ID u otros campos modificados
  SavedDiagram copyWith({
    int? id,
    String? name,
    String? description,
    DateTime? createdAt,
    DateTime? updatedAt,
    List<DiagramNode>? nodes,
    List<Connection>? connections,
    bool? isTemplate,
  }) {
    return SavedDiagram(
      id: id ?? this.id,
      name: name ?? this.name,
      description: description ?? this.description,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
      nodes: nodes ?? this.nodes,
      connections: connections ?? this.connections,
      isTemplate: isTemplate ?? this.isTemplate,
    );
  }

  // Convertir el diagrama a un mapa para guardarlo en la base de datos
  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'name': name,
      'description': description,
      'created_at': createdAt.toIso8601String(),
      'updated_at': updatedAt.toIso8601String(),
      'nodes_data': jsonEncode(_serializeNodes()),
      'connections_data': jsonEncode(_serializeConnections()),
      'is_template': isTemplate ? 1 : 0,
    };
  }

  // Crear un diagrama desde un mapa (registro de la base de datos)
  factory SavedDiagram.fromMap(Map<String, dynamic> map) {
    final List<Map<String, dynamic>> nodesData =
        List<Map<String, dynamic>>.from(jsonDecode(map['nodes_data']));

    final List<DiagramNode> nodes =
        nodesData.map((nodeData) {
          return DiagramNode(
            id: nodeData['id'],
            type: NodeType.values.byName(nodeData['type']),
            position: Offset(nodeData['x'], nodeData['y']),
            text: nodeData['text'],
          );
        }).toList();

    final List<Map<String, dynamic>> connectionsData =
        List<Map<String, dynamic>>.from(jsonDecode(map['connections_data']));

    final List<Connection> connections =
        connectionsData.map((connData) {
          final sourceNode = nodes.firstWhere(
            (node) => node.id == connData['source_id'],
          );
          final targetNode = nodes.firstWhere(
            (node) => node.id == connData['target_id'],
          );

          return Connection(
            source: sourceNode,
            target: targetNode,
            label: connData['label'],
          );
        }).toList();

    return SavedDiagram(
      id: map['id'],
      name: map['name'],
      description: map['description'],
      createdAt: DateTime.parse(map['created_at']),
      updatedAt: DateTime.parse(map['updated_at']),
      nodes: nodes,
      connections: connections,
      isTemplate: map['is_template'] == 1,
    );
  }

  // Serializar los nodos para su almacenamiento
  List<Map<String, dynamic>> _serializeNodes() {
    return nodes.map((node) {
      return {
        'id': node.id,
        'type': node.type.name,
        'x': node.position.dx,
        'y': node.position.dy,
        'text': node.text,
      };
    }).toList();
  }

  // Serializar las conexiones para su almacenamiento
  List<Map<String, dynamic>> _serializeConnections() {
    return connections.map((conn) {
      return {
        'source_id': conn.source.id,
        'target_id': conn.target.id,
        'label': conn.label,
      };
    }).toList();
  }
}
