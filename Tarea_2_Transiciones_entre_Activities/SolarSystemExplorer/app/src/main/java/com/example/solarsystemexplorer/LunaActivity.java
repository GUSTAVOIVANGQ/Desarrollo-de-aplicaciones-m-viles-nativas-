package com.example.solarsystemexplorer;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class LunaActivity extends AppCompatActivity {
    TextView planetDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_luna); // Asegúrate de que este layout exista
        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Luna");
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
        String description = "La Luna es el único satélite natural de la Tierra. Es el quinto satélite " +
                "más grande del sistema solar, y el más grande en relación con el tamaño de su planeta. " +
                "La Luna es un mundo rocoso, sin vida y con una atmósfera muy tenue. Su superficie está " +
                "cubierta de cráteres, mares y montañas. La Luna tiene una gran influencia en la Tierra, " +
                "causando las mareas y afectando el clima.";
        planetDescription.setText(description);
    }
}