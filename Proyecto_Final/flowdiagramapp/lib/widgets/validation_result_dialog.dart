import 'package:flutter/material.dart';
import '../models/diagram_validator.dart';

class ValidationResultDialog extends StatelessWidget {
  final ValidationResult result;

  const ValidationResultDialog({super.key, required this.result});

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text(
        result.isValid ? 'Diagrama válido' : 'Diagrama con errores',
        style: TextStyle(
          color: result.isValid ? Colors.green : Colors.red,
          fontWeight: FontWeight.bold,
        ),
      ),
      content: SizedBox(
        width: double.maxFinite,
        child: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              if (result.isValid && result.warnings.isEmpty)
                const Text(
                  '¡El diagrama es válido y está listo para generar código!',
                  style: TextStyle(fontSize: 16),
                )
              else ...[
                if (result.errors.isNotEmpty) ...[
                  const Text(
                    'Errores:',
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                      color: Colors.red,
                      fontSize: 16,
                    ),
                  ),
                  const SizedBox(height: 8),
                  ...result.errors.map(
                    (error) => Padding(
                      padding: const EdgeInsets.only(left: 8, bottom: 8),
                      child: Row(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Icon(
                            Icons.error_outline,
                            color: Colors.red,
                            size: 20,
                          ),
                          const SizedBox(width: 8),
                          Expanded(child: Text(error)),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 12),
                ],
                if (result.warnings.isNotEmpty) ...[
                  const Text(
                    'Advertencias:',
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                      color: Colors.amber,
                      fontSize: 16,
                    ),
                  ),
                  const SizedBox(height: 8),
                  ...result.warnings.map(
                    (warning) => Padding(
                      padding: const EdgeInsets.only(left: 8, bottom: 8),
                      child: Row(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Icon(
                            Icons.warning_amber_rounded,
                            color: Colors.amber,
                            size: 20,
                          ),
                          const SizedBox(width: 8),
                          Expanded(child: Text(warning)),
                        ],
                      ),
                    ),
                  ),
                ],
                const SizedBox(height: 16),
                Text(
                  result.isValid
                      ? 'Puedes continuar, pero considera revisar las advertencias para mejorar tu diagrama.'
                      : 'Por favor, corrige los errores antes de generar código.',
                  style: TextStyle(
                    fontStyle: FontStyle.italic,
                    color: result.isValid ? Colors.black87 : Colors.redAccent,
                  ),
                ),
              ],
            ],
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('Cerrar'),
        ),
      ],
    );
  }
}
