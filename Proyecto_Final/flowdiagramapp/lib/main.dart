import 'package:flutter/material.dart';
import 'screens/editor_screen.dart';
import 'screens/load_diagram_screen.dart';

void main() {
  runApp(const FlowDiagramApp());
}

class FlowDiagramApp extends StatelessWidget {
  const FlowDiagramApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flow Diagram App',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
        useMaterial3: true,
      ),
      home: const LoadDiagramScreen(),
    );
  }
}
