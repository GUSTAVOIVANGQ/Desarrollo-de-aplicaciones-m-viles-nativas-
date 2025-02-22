package com.example.solarsystemexplorer;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class JupiterActivity extends AppCompatActivity{
    TextView planetDescription;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jupiter);
        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Jupiter");
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
        String description = "Júpiter es el planeta más grande del sistema solar. Es un gigante gaseoso " +
                "compuesto principalmente de hidrógeno y helio. Tiene una Gran Mancha Roja, una tormenta gigante " +
                "que ha durado cientos de años. Posee un sistema de anillos tenues y muchas lunas.";
        planetDescription.setText(description);
    }

}