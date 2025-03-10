package com.example.solarsystemexplorer;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class VenusActivity extends AppCompatActivity {
    private TextView planetDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venus);
        
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
        String description = "Venus es el planeta más caliente del sistema solar, debido a su densa " +
                "atmósfera de dióxido de carbono que atrapa el calor. Es similar en tamaño a la Tierra, " +
                "pero su superficie está oculta por nubes espesas. Gira en dirección opuesta a la mayoría " +
                "de los planetas (rotación retrógrada).";
        planetDescription.setText(description);
    }
}
