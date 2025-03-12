package com.example.solarsystemexplorer;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SaturnoActivity extends AppCompatActivity{
    TextView planetDescription;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saturno);
        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Saturno");
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
        String description = "Urano es un gigante helado, compuesto principalmente de hielo de agua, metano y " +
                "amoníaco. Tiene una atmósfera de hidrógeno y helio. Su característica más distintiva es que gira " +
                "de lado, con su eje de rotación casi paralelo a su órbita.";
        planetDescription.setText(description);
    }

}