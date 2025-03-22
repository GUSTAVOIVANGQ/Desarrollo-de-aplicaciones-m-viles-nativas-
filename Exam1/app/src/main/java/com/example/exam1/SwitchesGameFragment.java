package com.example.exam1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class SwitchesGameFragment extends Fragment {

    private TextView tvDecimalValue;
    private TextView tvBinaryValue;
    private Button btnReset;
    private Switch[] switches = new Switch[8];
    private int[] bitValues = {1, 2, 4, 8, 16, 32, 64, 128}; // 2^0, 2^1, 2^2, etc.

    public SwitchesGameFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_switches_game, container, false);
        
        // Initialize UI elements
        tvDecimalValue = view.findViewById(R.id.tvDecimalValue);
        tvBinaryValue = view.findViewById(R.id.tvBinaryValue);
        btnReset = view.findViewById(R.id.btnReset);
        
        // Initialize switches
        switches[0] = view.findViewById(R.id.switch0); // 2^0 = 1
        switches[1] = view.findViewById(R.id.switch1); // 2^1 = 2
        switches[2] = view.findViewById(R.id.switch2); // 2^2 = 4
        switches[3] = view.findViewById(R.id.switch3); // 2^3 = 8
        switches[4] = view.findViewById(R.id.switch4); // 2^4 = 16
        switches[5] = view.findViewById(R.id.switch5); // 2^5 = 32
        switches[6] = view.findViewById(R.id.switch6); // 2^6 = 64
        switches[7] = view.findViewById(R.id.switch7); // 2^7 = 128
        
        // Set listeners for all switches
        CompoundButton.OnCheckedChangeListener switchListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateValues();
            }
        };
        
        // Attach listeners to all switches
        for (Switch bitSwitch : switches) {
            bitSwitch.setOnCheckedChangeListener(switchListener);
        }
        
        // Set up reset button
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetSwitches();
            }
        });
        
        // Initialize with default values (all 0s)
        updateValues();
        
        return view;
    }
    
    /**
     * Calculate the decimal value based on switch states and update the UI
     */
    private void updateValues() {
        int decimalValue = 0;
        StringBuilder binaryValue = new StringBuilder();
        
        // Process switches from most significant bit (left) to least significant bit (right)
        for (int i = switches.length - 1; i >= 0; i--) {
            boolean isOn = switches[i].isChecked();
            
            // Add to binary string representation
            binaryValue.append(isOn ? "1" : "0");
            
            // Add to decimal value if switch is on
            if (isOn) {
                decimalValue += bitValues[i];
            }
        }
        
        // Update UI with calculated values
        tvDecimalValue.setText(String.valueOf(decimalValue));
        tvBinaryValue.setText("Binario: " + binaryValue.toString());
        
        // Add a visual effect to show the value has changed
        tvDecimalValue.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(150)
            .withEndAction(new Runnable() {
                @Override
                public void run() {
                    tvDecimalValue.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(150);
                }
            });
    }
    
    /**
     * Reset all switches to OFF position
     */
    private void resetSwitches() {
        for (Switch bitSwitch : switches) {
            bitSwitch.setChecked(false);
        }
        updateValues();
    }
}
