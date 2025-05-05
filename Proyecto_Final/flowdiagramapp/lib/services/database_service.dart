import 'dart:async';
import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';
import 'package:path_provider/path_provider.dart';
import 'dart:io';
import 'package:flutter/material.dart'; // Añadimos esta importación para Offset
import '../models/saved_diagram.dart';
import '../models/diagram_node.dart';
import 'package:flutter/services.dart' show rootBundle;

class DatabaseService {
  static final DatabaseService _instance = DatabaseService._internal();
  static Database? _database;

  factory DatabaseService() {
    return _instance;
  }

  DatabaseService._internal();

  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDatabase();
    return _database!;
  }

  Future<Database> _initDatabase() async {
    Directory documentsDirectory = await getApplicationDocumentsDirectory();
    String path = join(documentsDirectory.path, 'flowdiagram.db');

    return await openDatabase(path, version: 1, onCreate: _onCreate);
  }

  Future<void> _onCreate(Database db, int version) async {
    await db.execute('''
      CREATE TABLE diagrams(
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        description TEXT,
        created_at TEXT NOT NULL,
        updated_at TEXT NOT NULL,
        nodes_data TEXT NOT NULL,
        connections_data TEXT NOT NULL,
        is_template INTEGER NOT NULL DEFAULT 0
      )
    ''');

    // Inicializar con plantillas predeterminadas
    await _loadTemplates(db);
  }

  Future<void> _loadTemplates(Database db) async {
    // Plantilla 1: Suma de dos números
    SavedDiagram sumTemplate = await _createSumTemplate();
    await db.insert(
      'diagrams',
      sumTemplate.toMap(),
      conflictAlgorithm: ConflictAlgorithm.replace,
    );

    // Plantilla 2: Verificación de número par/impar
    SavedDiagram evenOddTemplate = await _createEvenOddTemplate();
    await db.insert(
      'diagrams',
      evenOddTemplate.toMap(),
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  // Crear plantilla para suma de dos números
  Future<SavedDiagram> _createSumTemplate() async {
    final now = DateTime.now();

    final startNode = DiagramNode(
      id: "start_${now.millisecondsSinceEpoch}_1",
      type: NodeType.start,
      position: const Offset(250, 50),
      text: "Inicio",
    );

    final inputANode = DiagramNode(
      id: "input_${now.millisecondsSinceEpoch}_2",
      type: NodeType.input,
      position: const Offset(250, 150),
      text: "Ingrese el primer número (a)",
    );

    final inputBNode = DiagramNode(
      id: "input_${now.millisecondsSinceEpoch}_3",
      type: NodeType.input,
      position: const Offset(250, 250),
      text: "Ingrese el segundo número (b)",
    );

    final processNode = DiagramNode(
      id: "process_${now.millisecondsSinceEpoch}_4",
      type: NodeType.process,
      position: const Offset(250, 350),
      text: "resultado = a + b",
    );

    final outputNode = DiagramNode(
      id: "output_${now.millisecondsSinceEpoch}_5",
      type: NodeType.output,
      position: const Offset(250, 450),
      text: "Mostrar resultado",
    );

    final endNode = DiagramNode(
      id: "end_${now.millisecondsSinceEpoch}_6",
      type: NodeType.end,
      position: const Offset(250, 550),
      text: "Fin",
    );

    final nodes = [
      startNode,
      inputANode,
      inputBNode,
      processNode,
      outputNode,
      endNode,
    ];

    final connections = [
      Connection(source: startNode, target: inputANode, label: ""),
      Connection(source: inputANode, target: inputBNode, label: ""),
      Connection(source: inputBNode, target: processNode, label: ""),
      Connection(source: processNode, target: outputNode, label: ""),
      Connection(source: outputNode, target: endNode, label: ""),
    ];

    return SavedDiagram(
      name: "Suma de dos números",
      description: "Plantilla para sumar dos números ingresados por el usuario",
      createdAt: now,
      updatedAt: now,
      nodes: nodes,
      connections: connections,
      isTemplate: true,
    );
  }

  // Crear plantilla para verificar si un número es par o impar
  Future<SavedDiagram> _createEvenOddTemplate() async {
    final now = DateTime.now();

    final startNode = DiagramNode(
      id: "start_${now.millisecondsSinceEpoch}_10",
      type: NodeType.start,
      position: const Offset(250, 50),
      text: "Inicio",
    );

    final inputNode = DiagramNode(
      id: "input_${now.millisecondsSinceEpoch}_11",
      type: NodeType.input,
      position: const Offset(250, 150),
      text: "Ingrese un número",
    );

    final decisionNode = DiagramNode(
      id: "decision_${now.millisecondsSinceEpoch}_12",
      type: NodeType.decision,
      position: const Offset(250, 250),
      text: "numero % 2 == 0",
    );

    final outputEvenNode = DiagramNode(
      id: "output_${now.millisecondsSinceEpoch}_13",
      type: NodeType.output,
      position: const Offset(400, 350),
      text: "El número es par",
    );

    final outputOddNode = DiagramNode(
      id: "output_${now.millisecondsSinceEpoch}_14",
      type: NodeType.output,
      position: const Offset(100, 350),
      text: "El número es impar",
    );

    final endNode = DiagramNode(
      id: "end_${now.millisecondsSinceEpoch}_15",
      type: NodeType.end,
      position: const Offset(250, 450),
      text: "Fin",
    );

    final nodes = [
      startNode,
      inputNode,
      decisionNode,
      outputEvenNode,
      outputOddNode,
      endNode,
    ];

    final connections = [
      Connection(source: startNode, target: inputNode, label: ""),
      Connection(source: inputNode, target: decisionNode, label: ""),
      Connection(source: decisionNode, target: outputEvenNode, label: "Sí"),
      Connection(source: decisionNode, target: outputOddNode, label: "No"),
      Connection(source: outputEvenNode, target: endNode, label: ""),
      Connection(source: outputOddNode, target: endNode, label: ""),
    ];

    return SavedDiagram(
      name: "Verificación par/impar",
      description: "Plantilla para verificar si un número es par o impar",
      createdAt: now,
      updatedAt: now,
      nodes: nodes,
      connections: connections,
      isTemplate: true,
    );
  }

  // Operaciones CRUD para los diagramas

  // Crear un nuevo diagrama
  Future<int> saveDiagram(SavedDiagram diagram) async {
    final Database db = await database;
    return await db.insert(
      'diagrams',
      diagram.toMap(),
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  // Actualizar un diagrama existente
  Future<int> updateDiagram(SavedDiagram diagram) async {
    final Database db = await database;
    return await db.update(
      'diagrams',
      diagram.toMap(),
      where: 'id = ?',
      whereArgs: [diagram.id],
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  // Obtener un diagrama por su ID
  Future<SavedDiagram?> getDiagram(int id) async {
    final Database db = await database;
    final List<Map<String, dynamic>> maps = await db.query(
      'diagrams',
      where: 'id = ?',
      whereArgs: [id],
    );

    if (maps.isEmpty) {
      return null;
    }

    return SavedDiagram.fromMap(maps.first);
  }

  // Obtener todos los diagramas (excluyendo plantillas por defecto)
  Future<List<SavedDiagram>> getAllDiagrams({
    bool includeTemplates = false,
  }) async {
    final Database db = await database;

    List<Map<String, dynamic>> maps;
    if (!includeTemplates) {
      maps = await db.query(
        'diagrams',
        where: 'is_template = ?',
        whereArgs: [0],
        orderBy: 'updated_at DESC',
      );
    } else {
      maps = await db.query('diagrams', orderBy: 'updated_at DESC');
    }

    return List.generate(maps.length, (i) {
      return SavedDiagram.fromMap(maps[i]);
    });
  }

  // Obtener solo las plantillas
  Future<List<SavedDiagram>> getAllTemplates() async {
    final Database db = await database;
    final List<Map<String, dynamic>> maps = await db.query(
      'diagrams',
      where: 'is_template = ?',
      whereArgs: [1],
    );

    return List.generate(maps.length, (i) {
      return SavedDiagram.fromMap(maps[i]);
    });
  }

  // Eliminar un diagrama
  Future<int> deleteDiagram(int id) async {
    final Database db = await database;
    return await db.delete('diagrams', where: 'id = ?', whereArgs: [id]);
  }
}
