package com.example.flowdiagramapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.flowdiagramapp.controller.DiagramCodeSynchronizer;
import com.example.flowdiagramapp.controller.DiagramController;
import com.example.flowdiagramapp.model.ConditionalElement;
import com.example.flowdiagramapp.model.FlowElement;
import com.example.flowdiagramapp.model.VariableElement;
import com.example.flowdiagramapp.view.CodeView;
import com.example.flowdiagramapp.view.FlowDiagramView;

public class MainActivity extends AppCompatActivity {

    private FlowDiagramView diagramView;
    private CodeView codeView;
    private DiagramController diagramController;
    private DiagramCodeSynchronizer synchronizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize views
        diagramView = findViewById(R.id.diagram_view);
        codeView = findViewById(R.id.code_view);

        // Initialize controller
        diagramController = new DiagramController();
        diagramController.setViews(diagramView, codeView);

        // Initialize synchronizer
        synchronizer = new DiagramCodeSynchronizer(diagramController);

        // Set up element creation buttons
        setupElementButtons();

        // Create a sample diagram for testing
        diagramController.createSampleDiagram();
    }

    private void setupElementButtons() {
        Button btnStart = findViewById(R.id.btn_add_start);
        Button btnEnd = findViewById(R.id.btn_add_end);
        Button btnVariable = findViewById(R.id.btn_add_variable);
        Button btnConditional = findViewById(R.id.btn_add_conditional);
        Button btnConnect = findViewById(R.id.btn_connect);

        btnStart.setOnClickListener(v -> {
            diagramController.addElement("start", diagramView.getWidth() / 2f, 100);
        });

        btnEnd.setOnClickListener(v -> {
            diagramController.addElement("end", diagramView.getWidth() / 2f, diagramView.getHeight() - 100);
        });

        btnVariable.setOnClickListener(v -> {
            showVariableDialog();
        });

        btnConditional.setOnClickListener(v -> {
            showConditionalDialog();
        });

        btnConnect.setOnClickListener(v -> {
            Toast.makeText(this, "Selecciona primero el elemento de origen y luego el destino",
                    Toast.LENGTH_SHORT).show();
            // Start connection mode (would require additional implementation)
        });
    }

    private void showVariableDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Crear Variable");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText typeInput = new EditText(this);
        typeInput.setHint("Tipo (int, float, etc.)");
        typeInput.setText("int");
        layout.addView(typeInput);

        final EditText nameInput = new EditText(this);
        nameInput.setHint("Nombre de la variable");
        layout.addView(nameInput);

        final EditText valueInput = new EditText(this);
        valueInput.setHint("Valor inicial (opcional)");
        layout.addView(valueInput);

        builder.setView(layout);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String type = typeInput.getText().toString();
            String name = nameInput.getText().toString();
            String value = valueInput.getText().toString();

            if (type.isEmpty() || name.isEmpty()) {
                Toast.makeText(MainActivity.this, "Tipo y nombre son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create element at center of view
            float x = diagramView.getWidth() / 2f;
            float y = diagramView.getHeight() / 2f;

            FlowElement element = new VariableElement(-1, x, y, type, name, value);
            diagramController.addElement("variable", x, y);

            // Update the element properties (this is simplified - you'd need to get the actual added element)
            FlowElement addedElement = diagramController.getElements().get(diagramController.getElements().size() - 1);
            if (addedElement instanceof VariableElement) {
                VariableElement varElement = (VariableElement) addedElement;
                varElement.setVarType(type);
                varElement.setVarName(name);
                varElement.setVarValue(value);
                diagramController.updateElement(varElement);
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showConditionalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Crear Condicional");

        final EditText conditionInput = new EditText(this);
        conditionInput.setHint("Condición (ej: x > 5)");
        builder.setView(conditionInput);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String condition = conditionInput.getText().toString();

            if (condition.isEmpty()) {
                Toast.makeText(MainActivity.this, "La condición es obligatoria", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create element at center of view
            float x = diagramView.getWidth() / 2f;
            float y = diagramView.getHeight() / 2f;

            diagramController.addElement("conditional", x, y);

            // Update the element properties
            FlowElement addedElement = diagramController.getElements().get(diagramController.getElements().size() - 1);
            if (addedElement instanceof ConditionalElement) {
                ConditionalElement condElement = (ConditionalElement) addedElement;
                condElement.setCondition(condition);
                diagramController.updateElement(condElement);
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_clear) {
            clearDiagram();
            return true;
        } else if (id == R.id.action_sample) {
            diagramController.createSampleDiagram();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void clearDiagram() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Limpiar Diagrama");
        builder.setMessage("¿Estás seguro de que quieres eliminar todos los elementos?");

        builder.setPositiveButton("Sí", (dialog, which) -> {
            // Clear all elements and connections
            diagramController.getElements().clear();
            diagramController.getConnections().clear();
            diagramView.invalidate();
            codeView.setCode("");
        });

        builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}