import 'package:flutter/material.dart';
import 'package:model_viewer_plus/model_viewer_plus.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Visor de Modelos 3D',
      theme: ThemeData(
        primarySwatch: Colors.blue,
        useMaterial3: true,
      ),
      home: const ModelViewer3D(),
    );
  }
}

class ModelViewer3D extends StatefulWidget {
  const ModelViewer3D({super.key});

  @override
  State<ModelViewer3D> createState() => _ModelViewer3DState();
}

class _ModelViewer3DState extends State<ModelViewer3D> {
  int selectedModelIndex = 0;
  double rotationX = 0.0;
  double rotationY = 0.0;
  double distance = 5.0;
  double scale = 1.0;

  // Lista de modelos disponibles
  final List<Map<String, String>> models = [
    {
      'name': 'Astronauta',
      'url': 'https://modelviewer.dev/shared-assets/models/Astronaut.glb'
    },
    {
      'name': 'Robot',
      'url': 'https://modelviewer.dev/shared-assets/models/RobotExpressive.glb'
    },
    {
      'name': 'Zapato',
      'url': 'https://modelviewer.dev/shared-assets/models/NikeAirMax.glb'
    },
    {
      'name': 'Silla',
      'url': 'https://modelviewer.dev/shared-assets/models/Chair.glb'
    },
  ];

  void _changeModel(int index) {
    setState(() {
      selectedModelIndex = index;
    });
  }

  void _resetPosition() {
    setState(() {
      rotationX = 0.0;
      rotationY = 0.0;
      distance = 5.0;
      scale = 1.0;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Visor de Modelos 3D'),
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _resetPosition,
            tooltip: 'Reset posición',
          ),
        ],
      ),
      body: Column(
        children: [
          // Selector de modelos
          Container(
            height: 50,
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: ListView.builder(
              scrollDirection: Axis.horizontal,
              itemCount: models.length,
              itemBuilder: (context, index) {
                return Padding(
                  padding: const EdgeInsets.only(right: 8),
                  child: ChoiceChip(
                    label: Text(models[index]['name']!),
                    selected: selectedModelIndex == index,
                    onSelected: (selected) {
                      if (selected) _changeModel(index);
                    },
                  ),
                );
              },
            ),
          ),
          
          // Visor 3D
          Expanded(
            flex: 2,
            child: Container(
              width: double.infinity,
              decoration: BoxDecoration(
                border: Border.all(color: Colors.grey),
                borderRadius: BorderRadius.circular(8),
              ),
              margin: const EdgeInsets.all(8),
              child: ModelViewer(
                backgroundColor: const Color.fromARGB(0xFF, 0xEE, 0xEE, 0xEE),
                src: models[selectedModelIndex]['url']!,
                alt: 'Modelo 3D - ${models[selectedModelIndex]['name']}',
                ar: false,
                autoRotate: false,
                disableZoom: false,
                cameraControls: true,
                // Aplicar transformaciones
                cameraOrbit: '${rotationY}deg ${rotationX + 75}deg ${distance}m',
                fieldOfView: '${30 + (scale - 1) * 20}deg',
              ),
            ),
          ),
          
          // Panel de controles
          Expanded(
            flex: 1,
            child: Container(
              padding: const EdgeInsets.all(16),
              child: SingleChildScrollView(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Controles de Posición',
                      style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(height: 16),
                    
                    // Controles de rotación
                    Row(
                      children: [
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text('Rotación X: ${rotationX.toInt()}°'),
                              Slider(
                                value: rotationX,
                                min: -90,
                                max: 90,
                                divisions: 36,
                                onChanged: (value) {
                                  setState(() {
                                    rotationX = value;
                                  });
                                },
                              ),
                            ],
                          ),
                        ),
                        const SizedBox(width: 16),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text('Rotación Y: ${rotationY.toInt()}°'),
                              Slider(
                                value: rotationY,
                                min: -180,
                                max: 180,
                                divisions: 72,
                                onChanged: (value) {
                                  setState(() {
                                    rotationY = value;
                                  });
                                },
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                    
                    // Control de escala
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Escala: ${scale.toStringAsFixed(1)}x'),
                        Slider(
                          value: scale,
                          min: 0.5,
                          max: 2.0,
                          divisions: 15,
                          onChanged: (value) {
                            setState(() {
                              scale = value;
                            });
                          },
                        ),
                      ],
                    ),
                    
                    // Control de distancia
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Distancia: ${distance.toStringAsFixed(1)}m'),
                        Slider(
                          value: distance,
                          min: 2.0,
                          max: 10.0,
                          divisions: 16,
                          onChanged: (value) {
                            setState(() {
                              distance = value;
                            });
                          },
                        ),
                      ],
                    ),
                    
                    const SizedBox(height: 16),
                    
                    // Botón de reset
                    Center(
                      child: ElevatedButton.icon(
                        onPressed: _resetPosition,
                        icon: const Icon(Icons.refresh),
                        label: const Text('Resetear Posición'),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}