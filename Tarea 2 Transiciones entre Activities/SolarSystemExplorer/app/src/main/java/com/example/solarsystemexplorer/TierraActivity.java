package com.example.solarsystemexplorer;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TierraActivity extends AppCompatActivity {
    private Button btnLuna;
    private TextView planetDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tierra);

        // Inicializar vistas
        btnLuna = findViewById(R.id.btnLuna);
        planetDescription = findViewById(R.id.planetDescription);

        // Establecer la descripción del planeta
        setPlanetDescription();

        // Mostrar el botón de la luna con animación
        new Handler().postDelayed(() -> {
            btnLuna.setVisibility(View.VISIBLE);
        }, 500);

        // Configurar el click listener para el botón de la luna
        btnLuna.setOnClickListener(view -> {
            Intent intent = new Intent(TierraActivity.this, LunaActivity.class);
            startActivity(intent);
        });
        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tierra");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setPlanetDescription() {
        String description = "La Tierra es el tercer planeta del Sistema Solar y el único " +
                "conocido que alberga vida. Su superficie está compuesta por " +
                "océanos y continentes, con una atmósfera rica en nitrógeno y oxígeno.";
        planetDescription.setText(description);
    }
}