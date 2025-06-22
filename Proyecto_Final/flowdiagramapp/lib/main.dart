import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';
import 'firebase_options.dart';
import 'screens/editor_screen.dart';
import 'screens/load_diagram_screen.dart';
import 'widgets/auth_guard.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  try {
    await Firebase.initializeApp(
      options: DefaultFirebaseOptions.currentPlatform,
    );
  } catch (e) {
    print('Error inicializando Firebase: $e');
    // La app puede funcionar sin Firebase en modo offline
  }

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
      home: const AuthGuard(
        child: LoadDiagramScreen(),
      ),
    );
  }
}
