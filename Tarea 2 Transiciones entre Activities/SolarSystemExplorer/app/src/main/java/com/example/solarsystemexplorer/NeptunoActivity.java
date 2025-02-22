package com.example.solarsystemexplorer;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class NeptunoActivity extends AppCompatActivity{
    TextView planetDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neptuno);
        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Neptuno");
        }

        planetDescription = findViewById(R.id.planetDescription);
        setPlanetDescription();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    private void setPlanetDescription() {
        String description = "Neptuno es el planeta más alejado del Sol. También es un gigante helado, similar " +
                "a Urano en composición. Tiene fuertes vientos y tormentas, incluyendo la Gran Mancha Oscura, " +
                "similar a la Gran Mancha Roja de Júpiter.";
        planetDescription.setText(description);
    }

}
