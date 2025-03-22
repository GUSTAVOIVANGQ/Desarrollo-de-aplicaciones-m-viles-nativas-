package com.example.exam1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.fragment.app.Fragment;

public class BinaryVisualizationFragment extends Fragment {

    private TextView tvDecimalValue;
    private LinearLayout binaryToggleContainer;
    private Button btnReset;
    
    // Array of positional values (MSB to LSB)
    private final int[] POSITIONAL_VALUES = {128, 64, 32, 16, 8, 4, 2, 1};
    private ToggleButton[] bits;

    public BinaryVisualizationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_binary_visualization, container, false);
        
        // Initialize views
        tvDecimalValue = view.findViewById(R.id.tvDecimalValue);
        binaryToggleContainer = view.findViewById(R.id.binaryToggleContainer);
        btnReset = view.findViewById(R.id.btnReset);
        
        // Set up bit toggles
        setupBitToggles();
        
        // Reset button listener
        btnReset.setOnClickListener(v -> resetBits());
        
        return view;
    }
    
    private void setupBitToggles() {
        // Initialize the bits array
        bits = new ToggleButton[8];
        
        // Create 8 toggle buttons for binary bits
        for (int i = 0; i < 8; i++) {
            final int position = i;
            ToggleButton toggleButton = new ToggleButton(getContext());
            toggleButton.setTextOn("1");
            toggleButton.setTextOff("0");
            toggleButton.setChecked(false);
            
            // Add position value label
            TextView tvValue = new TextView(getContext());
            tvValue.setText(String.valueOf(POSITIONAL_VALUES[i]));
            tvValue.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            
            // Create layout for each bit
            LinearLayout bitLayout = new LinearLayout(getContext());
            bitLayout.setOrientation(LinearLayout.VERTICAL);
            bitLayout.addView(toggleButton);
            bitLayout.addView(tvValue);
            
            // Add layout params
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            params.setMargins(4, 0, 4, 0);
            bitLayout.setLayoutParams(params);
            
            // Add to container
            binaryToggleContainer.addView(bitLayout);
            
            // Store toggle button reference
            bits[i] = toggleButton;
            
            // Set listener to update decimal value
            toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> updateDecimalValue());
        }
    }
    
    private void updateDecimalValue() {
        int decimalValue = 0;
        
        // Calculate decimal value
        for (int i = 0; i < 8; i++) {
            if (bits[i].isChecked()) {
                decimalValue += POSITIONAL_VALUES[i];
            }
        }
        
        // Update text view
        tvDecimalValue.setText(String.valueOf(decimalValue));
        
        // Visual animation - make the text briefly larger to show changes
        tvDecimalValue.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200)
                .withEndAction(() -> tvDecimalValue.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200));
    }
    
    private void resetBits() {
        // Reset all bits to 0
        for (ToggleButton bit : bits) {
            bit.setChecked(false);
        }
        
        // Update display
        updateDecimalValue();
    }
}
