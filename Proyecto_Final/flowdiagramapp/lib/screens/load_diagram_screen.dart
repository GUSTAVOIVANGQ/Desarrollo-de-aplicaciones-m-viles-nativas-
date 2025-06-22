import 'package:flutter/material.dart';
import 'package:intl/intl.dart'; // Ensure this import is present
import '../models/saved_diagram.dart';
import '../services/database_service.dart';
import '../services/auth_service.dart';
import 'editor_screen.dart';
import 'profile_screen.dart';

class LoadDiagramScreen extends StatefulWidget {
  const LoadDiagramScreen({super.key});

  @override
  State<LoadDiagramScreen> createState() => _LoadDiagramScreenState();
}

class _LoadDiagramScreenState extends State<LoadDiagramScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  final DatabaseService _databaseService = DatabaseService();
  List<SavedDiagram> _diagrams = [];
  List<SavedDiagram> _templates = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    _loadData();
  }

  Future<void> _loadData() async {
    setState(() {
      _isLoading = true;
    });

    try {
      final diagrams = await _databaseService.getAllDiagrams();
      final templates = await _databaseService.getAllTemplates();

      setState(() {
        _diagrams = diagrams;
        _templates = templates;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _isLoading = false;
      });
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error cargando diagramas: ${e.toString()}')),
        );
      }
    }
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Cargar diagrama'),
        actions: [
          IconButton(
            icon: const Icon(Icons.person),
            onPressed: () {
              Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (context) => const ProfileScreen(),
                ),
              );
            },
            tooltip: 'Mi perfil',
          ),
        ],
        bottom: TabBar(
          controller: _tabController,
          tabs: const [Tab(text: 'Mis diagramas'), Tab(text: 'Plantillas')],
        ),
      ),
      body: TabBarView(
        controller: _tabController,
        children: [
          // Pestaña de diagramas guardados
          _isLoading
              ? const Center(child: CircularProgressIndicator())
              : _diagrams.isEmpty
                  ? const Center(
                      child: Text(
                        'No hay diagramas guardados',
                        style: TextStyle(fontSize: 16),
                      ),
                    )
                  : _buildDiagramList(_diagrams, canDelete: true),

          // Pestaña de plantillas
          _isLoading
              ? const Center(child: CircularProgressIndicator())
              : _buildDiagramList(_templates),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          // En lugar de cerrar la pantalla actual, navegamos a la pantalla del editor
          Navigator.of(
            context,
          ).push(MaterialPageRoute(builder: (context) => const EditorScreen()));
        },
        tooltip: 'Crear nuevo',
        child: const Icon(Icons.add),
      ),
    );
  }

  Widget _buildDiagramList(List<SavedDiagram> items, {bool canDelete = false}) {
    return ListView.builder(
      itemCount: items.length,
      padding: const EdgeInsets.all(8.0),
      itemBuilder: (context, index) {
        final item = items[index];
        return Card(
          elevation: 2,
          margin: const EdgeInsets.symmetric(vertical: 8.0, horizontal: 4.0),
          child: ListTile(
            title: Text(
              item.name,
              style: const TextStyle(fontWeight: FontWeight.bold),
            ),
            subtitle: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                if (item.description.isNotEmpty)
                  Text(
                    item.description,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                  ),
                Text(
                  'Modificado: ${DateFormat('dd/MM/yyyy HH:mm').format(item.updatedAt)}',
                  style: TextStyle(fontSize: 12, color: Colors.grey[600]),
                ),
              ],
            ),
            leading: CircleAvatar(
              backgroundColor: item.isTemplate ? Colors.amber : Colors.blue,
              child: Icon(
                item.isTemplate ? Icons.article : Icons.insert_chart,
                color: Colors.white,
              ),
            ),
            trailing: canDelete
                ? IconButton(
                    icon: const Icon(Icons.delete),
                    onPressed: () => _confirmDeleteDiagram(item),
                  )
                : null,
            onTap: () {
              // En lugar de cerrar la pantalla, navegamos al editor con el diagrama seleccionado
              Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (context) => EditorScreen(initialDiagram: item),
                ),
              );
            },
          ),
        );
      },
    );
  }

  Future<void> _confirmDeleteDiagram(SavedDiagram diagram) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Eliminar diagrama'),
        content: Text(
          '¿Estás seguro de que deseas eliminar "${diagram.name}"?',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('Cancelar'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(context).pop(true),
            style: FilledButton.styleFrom(backgroundColor: Colors.red),
            child: const Text('Eliminar'),
          ),
        ],
      ),
    );

    if (confirmed == true && mounted) {
      try {
        await _databaseService.deleteDiagram(diagram.id!);
        setState(() {
          _diagrams.removeWhere((d) => d.id == diagram.id);
        });
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(const SnackBar(content: Text('Diagrama eliminado')));
      } catch (e) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error eliminando diagrama: ${e.toString()}')),
        );
      }
    }
  }
}
