// MainActivity.java
package com.example.solarsystemexplorer;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button btnViaLactea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar el botón
        btnViaLactea = findViewById(R.id.btnViaLactea);

        // Configurar el click listener para el botón
        btnViaLactea.setOnClickListener(view -> {
            // Crear un Intent para abrir SolarSystemActivity
            Intent intent = new Intent(MainActivity.this, SolarSystemActivity.class);
            startActivity(intent);
        });
    }
}