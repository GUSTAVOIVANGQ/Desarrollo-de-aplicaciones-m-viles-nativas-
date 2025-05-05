package com.example.flowdiagramapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.flowdiagramapp.model.Node;
import com.example.flowdiagramapp.view.FlowDiagramView;

public class MainActivity extends AppCompatActivity {

    private FlowDiagramView diagramView;
    private Button btnStartNode, btnEndNode, btnVariableNode, btnConditionalNode, btnDeleteNode, btnClear, btnGenerateCode;
    private TextView tvGeneratedCode;
    
    private static final float NODE_DEFAULT_X = 300f;
    private static final float NODE_DEFAULT_Y = 200f;
    private float lastNodeX = NODE_DEFAULT_X;
    private float lastNodeY = NODE_DEFAULT_Y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Inicializar la barra de herramientas
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // Inicializar los componentes de UI
        initializeViews();
        
        // Configurar los listeners para los botones
        setupButtonListeners();
        
        // Configurar insets para el modo edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    
    private void initializeViews() {
        diagramView = findViewById(R.id.diagramView);
        btnStartNode = findViewById(R.id.btnStartNode);
        btnEndNode = findViewById(R.id.btnEndNode);
        btnVariableNode = findViewById(R.id.btnVariableNode);
        btnConditionalNode = findViewById(R.id.btnConditionalNode);
        btnDeleteNode = findViewById(R.id.btnDeleteNode);
        btnClear = findViewById(R.id.btnClear);
        btnGenerateCode = findViewById(R.id.btnGenerateCode);
        tvGeneratedCode = findViewById(R.id.tvGeneratedCode);
    }
    
    private void setupButtonListeners() {
        // Botón para añadir nodo de inicio
        btnStartNode.setOnClickListener(v -> {
            updateNodePosition();
            diagramView.addStartNode(lastNodeX, lastNodeY);
            Toast.makeText(this, "Nodo de inicio añadido", Toast.LENGTH_SHORT).show();
        });
        
        // Botón para añadir nodo de fin
        btnEndNode.setOnClickListener(v -> {
            updateNodePosition();
            diagramView.addEndNode(lastNodeX, lastNodeY);
            Toast.makeText(this, "Nodo de fin añadido", Toast.LENGTH_SHORT).show();
        });
        
        // Botón para añadir nodo de variable
        btnVariableNode.setOnClickListener(v -> {
            showVariableDialog();
        });
        
        // Botón para añadir nodo condicional
        btnConditionalNode.setOnClickListener(v -> {
            showConditionalDialog();
        });
        
        // Botón para eliminar el nodo seleccionado
        btnDeleteNode.setOnClickListener(v -> {
            Node selectedNode = diagramView.getSelectedNode();
            if (selectedNode != null) {
                diagramView.removeNode(selectedNode);
                Toast.makeText(this, "Nodo eliminado", Toast.LENGTH_SHORT).show();
            } else if (diagramView.getSelectedConnection() != null) {
                diagramView.removeConnection(diagramView.getSelectedConnection());
                Toast.makeText(this, "Conexión eliminada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Selecciona un nodo o conexión para eliminar", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Botón para limpiar el diagrama
        btnClear.setOnClickListener(v -> {
            showClearConfirmationDialog();
        });
        
        // Botón para generar código
        btnGenerateCode.setOnClickListener(v -> {
            Toast.makeText(this, "Funcionalidad de generación de código aún no implementada", Toast.LENGTH_SHORT).show();
            // Esta funcionalidad se implementará en la siguiente parte del proyecto
        });
    }
    
    /**
     * Actualiza las coordenadas para el próximo nodo, permitiendo crear nodos con un espaciado adecuado
     */
    private void updateNodePosition() {
        // Incrementamos ligeramente las coordenadas para no solapar los nodos
        lastNodeX += 50f;
        lastNodeY += 30f;
        
        // Si nos vamos muy lejos, volvemos a una posición inicial pero más abajo
        if (lastNodeX > diagramView.getWidth() - 100) {
            lastNodeX = NODE_DEFAULT_X;
            lastNodeY += 100f;
        }
        
        // Si nos salimos por abajo, volvemos arriba
        if (lastNodeY > diagramView.getHeight() - 100) {
            lastNodeY = NODE_DEFAULT_Y;
        }
    }
    
    /**
     * Muestra un diálogo para introducir el texto de un nodo de variable
     */
    private void showVariableDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Declaración de Variable");
        
        View view = getLayoutInflater().inflate(R.layout.dialog_variable, null);
        final EditText input = view.findViewById(R.id.etVariableName);
        
        builder.setView(view);
        builder.setPositiveButton("Añadir", (dialog, which) -> {
            String text = input.getText().toString().trim();
            if (!text.isEmpty()) {
                updateNodePosition();
                diagramView.addVariableNode(lastNodeX, lastNodeY, text);
                Toast.makeText(MainActivity.this, "Nodo de variable añadido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "El texto no puede estar vacío", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }
    
    /**
     * Muestra un diálogo para introducir la condición de un nodo condicional
     */
    private void showConditionalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Condición");
        
        View view = getLayoutInflater().inflate(R.layout.dialog_conditional, null);
        final EditText input = view.findViewById(R.id.etCondition);
        
        builder.setView(view);
        builder.setPositiveButton("Añadir", (dialog, which) -> {
            String condition = input.getText().toString().trim();
            if (!condition.isEmpty()) {
                updateNodePosition();
                diagramView.addConditionalNode(lastNodeX, lastNodeY, condition);
                Toast.makeText(MainActivity.this, "Nodo condicional añadido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "La condición no puede estar vacía", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }
    
    /**
     * Muestra un diálogo de confirmación antes de limpiar el diagrama
     */
    private void showClearConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Limpiar diagrama");
        builder.setMessage("¿Estás seguro de que quieres eliminar todos los nodos y conexiones?");
        
        builder.setPositiveButton("Sí", (dialog, which) -> {
            diagramView.clear();
            // Reiniciar la posición de los nodos
            lastNodeX = NODE_DEFAULT_X;
            lastNodeY = NODE_DEFAULT_Y;
            Toast.makeText(MainActivity.this, "Diagrama limpiado", Toast.LENGTH_SHORT).show();
        });
        
        builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}