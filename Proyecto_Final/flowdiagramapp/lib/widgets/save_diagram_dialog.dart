import 'package:flutter/material.dart';

class SaveDiagramDialog extends StatefulWidget {
  final String? initialName;
  final String? initialDescription;
  final bool isUpdate;

  const SaveDiagramDialog({
    super.key,
    this.initialName,
    this.initialDescription,
    this.isUpdate = false,
  });

  @override
  State<SaveDiagramDialog> createState() => _SaveDiagramDialogState();
}

class _SaveDiagramDialogState extends State<SaveDiagramDialog> {
  late TextEditingController _nameController;
  late TextEditingController _descriptionController;
  final _formKey = GlobalKey<FormState>();

  @override
  void initState() {
    super.initState();
    _nameController = TextEditingController(text: widget.initialName ?? '');
    _descriptionController = TextEditingController(
      text: widget.initialDescription ?? '',
    );
  }

  @override
  void dispose() {
    _nameController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text(widget.isUpdate ? 'Actualizar diagrama' : 'Guardar diagrama'),
      content: Form(
        key: _formKey,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            TextFormField(
              controller: _nameController,
              decoration: const InputDecoration(
                labelText: 'Nombre del diagrama',
                hintText: 'Ej: Cálculo de factorial',
              ),
              validator: (value) {
                if (value == null || value.isEmpty) {
                  return 'Por favor, ingresa un nombre';
                }
                return null;
              },
            ),
            const SizedBox(height: 16),
            TextFormField(
              controller: _descriptionController,
              decoration: const InputDecoration(
                labelText: 'Descripción (opcional)',
                hintText: 'Añade una breve descripción',
              ),
              maxLines: 3,
            ),
          ],
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('Cancelar'),
        ),
        FilledButton(
          onPressed: () {
            if (_formKey.currentState?.validate() ?? false) {
              Navigator.of(context).pop({
                'name': _nameController.text,
                'description': _descriptionController.text,
              });
            }
          },
          child: Text(widget.isUpdate ? 'Actualizar' : 'Guardar'),
        ),
      ],
    );
  }
}
