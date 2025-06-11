import 'package:flutter/material.dart';
import 'package:camera/camera.dart';
import 'package:model_viewer_plus/model_viewer_plus.dart';
import 'dart:math' as math;

class ARCameraScreen extends StatefulWidget {
  @override
  _ARCameraScreenState createState() => _ARCameraScreenState();
}

class _ARCameraScreenState extends State<ARCameraScreen>
    with TickerProviderStateMixin {
  CameraController? _cameraController;
  bool _isCameraInitialized = false;
  bool _showModel = false;
  String _currentModel = '';
  // Posición del modelo 3D
  Offset _modelPosition = Offset(0.5, 0.5);
  double _modelScale = 1.0;
  bool _isModelPlaced =
      false; // Controla si el modelo está colocado en la escena

  // Animaciones
  late AnimationController _scaleController;

  // Lista de modelos 3D disponibles online
  final List<Map<String, String>> _models = [
    {
      'name': 'Astronauta',
      'url': 'https://modelviewer.dev/shared-assets/models/Astronaut.glb',
    },
    {
      'name': 'Robot',
      'url': 'https://modelviewer.dev/shared-assets/models/RobotExpressive.glb',
    },
    {
      'name': 'Casco',
      'url': 'https://modelviewer.dev/shared-assets/models/DamagedHelmet.glb',
    },
  ];

  @override
  void initState() {
    super.initState();
    _initializeCamera();
    _initializeAnimations();
    _currentModel = _models[0]['url']!;
  }

  void _initializeAnimations() {
    _scaleController = AnimationController(
      duration: Duration(milliseconds: 500),
      vsync: this,
    );
  }

  Future<void> _initializeCamera() async {
    try {
      final cameras = await availableCameras();
      if (cameras.isNotEmpty) {
        _cameraController = CameraController(
          cameras[0],
          ResolutionPreset.medium,
          enableAudio: false,
        );

        await _cameraController!.initialize();

        if (mounted) {
          setState(() {
            _isCameraInitialized = true;
          });
        }
      }
    } catch (e) {
      print('Error initializing camera: $e');
    }
  }

  @override
  void dispose() {
    _cameraController?.dispose();
    _scaleController.dispose();
    super.dispose();
  }

  void _toggleModel() {
    setState(() {
      _showModel = !_showModel;
      if (_showModel) {
        _scaleController.forward();
      } else {
        _scaleController.reverse();
        _isModelPlaced = false; // Reset cuando se oculta el modelo
      }
    });
  }

  void _placeModel() {
    setState(() {
      _isModelPlaced = !_isModelPlaced;
    });

    // Mostrar mensaje de confirmación
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(
          _isModelPlaced
              ? '¡Modelo colocado en la escena!'
              : 'Modelo removido de la escena',
        ),
        duration: Duration(seconds: 2),
        backgroundColor: _isModelPlaced ? Colors.green : Colors.orange,
      ),
    );
  }

  void _changeModel() {
    showModalBottomSheet(
      context: context,
      builder: (BuildContext context) {
        return Container(
          height: 200,
          padding: EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Selecciona un modelo 3D',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              ),
              SizedBox(height: 16),
              Expanded(
                child: ListView.builder(
                  itemCount: _models.length,
                  itemBuilder: (context, index) {
                    final model = _models[index];
                    return ListTile(
                      title: Text(model['name']!),
                      leading: Icon(Icons.view_in_ar),
                      onTap: () {
                        setState(() {
                          _currentModel = model['url']!;
                        });
                        Navigator.pop(context);
                      },
                    );
                  },
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    if (!_isCameraInitialized || _cameraController == null) {
      return Scaffold(
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              CircularProgressIndicator(),
              SizedBox(height: 20),
              Text('Inicializando cámara...'),
            ],
          ),
        ),
      );
    }

    return Scaffold(
      body: Stack(
        children: [
          // Vista de la cámara
          Positioned.fill(child: CameraPreview(_cameraController!)),

          // Modelo 3D superpuesto con rotación integrada
          if (_showModel)
            AnimatedBuilder(
              animation: _scaleController,
              builder: (context, child) {
                return Positioned(
                  left: MediaQuery.of(context).size.width * _modelPosition.dx -
                      100,
                  top: MediaQuery.of(context).size.height * _modelPosition.dy -
                      100,
                  child: Container(
                    decoration: BoxDecoration(
                      border: _isModelPlaced
                          ? Border.all(color: Colors.green, width: 3)
                          : Border.all(
                              color: Colors.blue.withOpacity(0.5), width: 2),
                      borderRadius: BorderRadius.circular(10),
                    ),
                    child: Transform(
                      alignment: Alignment.center,
                      transform: Matrix4.identity()..scale(_modelScale),
                      child: SizedBox(
                        width: 200,
                        height: 200,
                        child: ModelViewer(
                          src: _currentModel,
                          alt: "Modelo 3D",
                          ar: false,
                          autoRotate: false,
                          cameraControls: true, // Permite rotación con gestos
                          backgroundColor: Colors.transparent,
                          disableZoom:
                              true, // Deshabilitamos zoom del ModelViewer
                          disablePan:
                              true, // Deshabilitamos pan del ModelViewer
                          interactionPrompt: InteractionPrompt.none,
                        ),
                      ),
                    ),
                  ),
                );
              },
            ), // Controles superiores
          Positioned(
            top: MediaQuery.of(context).padding.top + 10,
            left: 10,
            right: 10,
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                IconButton(
                  onPressed: () => Navigator.pop(context),
                  icon: Icon(Icons.arrow_back, color: Colors.white),
                  style: IconButton.styleFrom(backgroundColor: Colors.black54),
                ),
                Column(
                  children: [
                    Text(
                      'Simple AR Camera',
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    if (_showModel && _isModelPlaced)
                      Container(
                        padding:
                            EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                        decoration: BoxDecoration(
                          color: Colors.green,
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Text(
                          '🎯 Modelo en escena',
                          style: TextStyle(
                            color: Colors.white,
                            fontSize: 12,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                  ],
                ),
                IconButton(
                  onPressed: _changeModel,
                  icon: Icon(Icons.swap_horiz, color: Colors.white),
                  style: IconButton.styleFrom(backgroundColor: Colors.black54),
                ),
              ],
            ),
          ),

          // Controles inferiores
          Positioned(
            bottom: 30,
            left: 20,
            right: 20,
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                // Botón para mostrar/ocultar modelo
                FloatingActionButton(
                  onPressed: _toggleModel,
                  backgroundColor: _showModel ? Colors.red : Colors.blue,
                  child: Icon(
                    _showModel ? Icons.visibility_off : Icons.view_in_ar,
                    color: Colors.white,
                  ),
                ), // Controles de escala y colocación
                if (_showModel) ...[
                  // Botón para colocar/remover modelo
                  FloatingActionButton(
                    onPressed: _placeModel,
                    backgroundColor:
                        _isModelPlaced ? Colors.purple : Colors.grey,
                    child: Icon(
                      _isModelPlaced ? Icons.place : Icons.add_location,
                      color: Colors.white,
                    ),
                  ),
                  FloatingActionButton(
                    onPressed: () {
                      setState(() {
                        _modelScale = math.max(0.5, _modelScale - 0.2);
                      });
                    },
                    backgroundColor: Colors.orange,
                    child: Icon(Icons.remove, color: Colors.white),
                  ),
                  FloatingActionButton(
                    onPressed: () {
                      setState(() {
                        _modelScale = math.min(3.0, _modelScale + 0.2);
                      });
                    },
                    backgroundColor: Colors.green,
                    child: Icon(Icons.add, color: Colors.white),
                  ),
                ],
              ],
            ),
          ),

          // Instrucciones actualizadas
          if (_showModel)
            Positioned(
              top: MediaQuery.of(context).size.height * 0.3,
              left: 20,
              right: 20,
              child: Container(
                padding: EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: Colors.black54,
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Text(
                  'Arrastra sobre el modelo para rotarlo\ny ver todas las caras\nBotones +/- para escalar\n${_isModelPlaced ? "📍 Modelo colocado en la escena" : "📍 Toca para colocar el modelo"}',
                  textAlign: TextAlign.center,
                  style: TextStyle(color: Colors.white, fontSize: 14),
                ),
              ),
            ),
        ],
      ),
    );
  }
}
