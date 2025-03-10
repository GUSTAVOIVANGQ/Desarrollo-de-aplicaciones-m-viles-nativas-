package com.example.solarsystemexplorer;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

public class SolarSystemActivity extends AppCompatActivity {
    // Declaración de los botones
    private Button btnMercurio, btnVenus, btnTierra, btnMarte;
    private Button btnJupiter, btnSaturno, btnUrano, btnNeptuno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solar_system);

        // Inicializar los botones
        initializeButtons();

        // Mostrar los botones gradualmente
        showButtonsSequentially();

        // Configurar los click listeners
        setupClickListeners();
    }

    private void initializeButtons() {
        btnMercurio = findViewById(R.id.btnMercurio);
        btnVenus = findViewById(R.id.btnVenus);
        btnTierra = findViewById(R.id.btnTierra);
        btnMarte = findViewById(R.id.btnMarte);
        btnJupiter = findViewById(R.id.btnJupiter);
        btnSaturno = findViewById(R.id.btnSaturno);
        btnUrano = findViewById(R.id.btnUrano);
        btnNeptuno = findViewById(R.id.btnNeptunox);
    }

    private void showButtonsSequentially() {
        Handler handler = new Handler();
        Button[] buttons = {btnMercurio, btnVenus, btnTierra, btnMarte,
                btnJupiter, btnSaturno, btnUrano, btnNeptuno};

        for (int i = 0; i < buttons.length; i++) {
            final Button button = buttons[i];
            handler.postDelayed(() -> {
                button.setVisibility(View.VISIBLE);
            }, 200 * (i + 1)); // Cada botón aparece con 200ms de retraso
        }
    }

    private void setupClickListeners() {
        // Mercurio
        btnMercurio.setOnClickListener(view -> {
            Intent intent = new Intent(SolarSystemActivity.this, MercurioActivity.class);
            startActivity(intent);
        });

        // Venus
        btnVenus.setOnClickListener(view -> {
            Intent intent = new Intent(SolarSystemActivity.this, VenusActivity.class);
            startActivity(intent);
        });

        // Tierra
        btnTierra.setOnClickListener(view -> {
            Intent intent = new Intent(SolarSystemActivity.this, TierraActivity.class);
            startActivity(intent);
        });

        // Marte
        btnMarte.setOnClickListener(view -> {
            Intent intent = new Intent(SolarSystemActivity.this, MarteActivity.class);
            startActivity(intent);
        });

        // Júpiter
        btnJupiter.setOnClickListener(view -> {
            Intent intent = new Intent(SolarSystemActivity.this, JupiterActivity.class);
            startActivity(intent);
        });

        // Saturno
        btnSaturno.setOnClickListener(view -> {
            Intent intent = new Intent(SolarSystemActivity.this, SaturnoActivity.class);
            startActivity(intent);
        });

        // Urano
        btnUrano.setOnClickListener(view -> {
            Intent intent = new Intent(SolarSystemActivity.this, UranoActivity.class);
            startActivity(intent);
        });

        // Neptuno
        btnNeptuno.setOnClickListener(view -> {
            Intent intent = new Intent(SolarSystemActivity.this, NeptunoActivity.class);
            startActivity(intent);
        });
    }

}