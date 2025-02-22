package com.example.solarsystemexplorer;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MarteActivity extends AppCompatActivity{
    private TextView planetDescriptionMars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marte);
        // Correct initialization
        planetDescriptionMars = findViewById(R.id.planetDescriptionMars);
        setMarsPlanetDescription();
        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Marte");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    private void setMarsPlanetDescription() {
        String description = "Marte es conocido como el 'Planeta Rojo' por su color debido al óxido de hierro " +
                "en su superficie. Tiene una atmósfera delgada y es un planeta frío. Posee volcanes, valles y " +
                "cañones, incluyendo el Monte Olimpo, el volcán más grande del sistema solar. Hay evidencia de " +
                "que alguna vez tuvo agua líquida en su superficie.";
        planetDescriptionMars.setText(description);
    }
}