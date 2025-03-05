package com.example.myxapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Button btnCalculator = findViewById(R.id.btnCalculator);
        Button btnChronometer = findViewById(R.id.btnChronometer);

        btnCalculator.setOnClickListener(v -> startActivity(new Intent(MainMenuActivity.this, CalculatorActivity.class)));
        btnChronometer.setOnClickListener(v -> startActivity(new Intent(MainMenuActivity.this, ChronometerActivity.class)));
    }
}
