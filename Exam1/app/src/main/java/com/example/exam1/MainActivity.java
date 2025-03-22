package com.example.exam1;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private Button btnEducational;
    private Button btnGames;
    private Button btnTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before setting content view
        ThemeManager.applyTheme(this);
        
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Initialize UI components
        btnEducational = findViewById(R.id.btnEducational);
        btnGames = findViewById(R.id.btnGames);
        btnTheme = findViewById(R.id.btnTheme);
        
        // Set click listeners
        btnEducational.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EducationalActivity.class);
                startActivity(intent);
            }
        });
        
        btnGames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GamesActivity.class);
                startActivity(intent);
            }
        });
        
        btnTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showThemeDialog();
            }
        });
        
        // Apply window insets with consideration for screen size
        applyWindowInsets();
    }
    
    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Adjust padding based on screen size
            boolean isTablet = getResources().getBoolean(R.bool.isTablet);
            int padding = isTablet ? 
                    getResources().getDimensionPixelSize(R.dimen.margin_medium) : 
                    0;
            
            v.setPadding(
                systemBars.left + padding, 
                systemBars.top + padding, 
                systemBars.right + padding, 
                systemBars.bottom + padding
            );
            return insets;
        });
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Handle configuration changes for smoother transitions
        applyWindowInsets();
    }
    
    private void showThemeDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_theme_selector);
        dialog.setTitle("Seleccionar Tema");
        
        // Get current theme
        int currentTheme = ThemeManager.getTheme(this);
        
        // Set up radio buttons
        RadioGroup radioGroup = dialog.findViewById(R.id.themeRadioGroup);
        RadioButton radioGuinda = dialog.findViewById(R.id.radioGuinda);
        RadioButton radioEscom = dialog.findViewById(R.id.radioEscom);
        
        // Check the current theme
        if (currentTheme == ThemeManager.THEME_GUINDA) {
            radioGuinda.setChecked(true);
        } else {
            radioEscom.setChecked(true);
        }
        
        // Set listener for theme selection
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int selectedTheme;
                
                if (checkedId == R.id.radioGuinda) {
                    selectedTheme = ThemeManager.THEME_GUINDA;
                } else {
                    selectedTheme = ThemeManager.THEME_ESCOM;
                }
                
                // Save and apply the selected theme
                ThemeManager.setTheme(MainActivity.this, selectedTheme);
                dialog.dismiss();
                
                // Restart the activity to apply the theme
                recreate();
            }
        });
        
        dialog.show();
    }
}