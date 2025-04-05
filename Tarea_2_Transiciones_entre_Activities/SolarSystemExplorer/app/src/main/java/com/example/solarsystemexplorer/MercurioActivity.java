package com.example.solarsystemexplorer;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MercurioActivity extends AppCompatActivity{
    private TextView planetDescription;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mercurio);
        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Venus");
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
        String description = "Mercurio es el planeta más pequeño y el más cercano al Sol. " +
                "Su superficie está cubierta de cráteres, similar a la Luna. " +
                "Tiene temperaturas extremas, muy calientes durante el día y muy frías durante la noche, " +
                "debido a su delgada atmósfera.";
        planetDescription.setText(description);
    }
}
