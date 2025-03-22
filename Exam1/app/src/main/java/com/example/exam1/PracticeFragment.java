package com.example.exam1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.fragment.app.Fragment;

import java.util.Random;

public class PracticeFragment extends Fragment {

    private TextView tvDecimalQuestion;
    private TextView tvDifficultyLevel;
    private TextView tvCorrectCounter;
    private LinearLayout binaryInputContainer;
    private Button btnSubmit;
    private Button btnNextProblem;
    private TextView tvFeedback;

    private int currentLevel = 1;
    private int maxLevel = 5;
    private int currentDecimalValue;
    private int correctAnswers = 0;
    private int totalProblems = 0;
    private ToggleButton[] bits;
    private Random random = new Random();

    public PracticeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout
        View view = inflater.inflate(R.layout.fragment_practice, container, false);

        // Initialize views
        tvDecimalQuestion = view.findViewById(R.id.tvDecimalQuestion);
        tvDifficultyLevel = view.findViewById(R.id.tvDifficultyLevel);
        tvCorrectCounter = view.findViewById(R.id.tvCorrectCounter);
        binaryInputContainer = view.findViewById(R.id.binaryInputContainer);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnNextProblem = view.findViewById(R.id.btnNextProblem);
        tvFeedback = view.findViewById(R.id.tvFeedback);

        // Set up binary input toggles
        setupBinaryInputs();

        // Set up button listeners
        btnSubmit.setOnClickListener(v -> checkAnswer());
        btnNextProblem.setOnClickListener(v -> generateNewProblem());

        // Initialize first problem
        updateLevelDisplay();
        generateNewProblem();
        updateScoreDisplay();

        return view;
    }

    private void setupBinaryInputs() {
        // Create 8 toggle buttons for binary input
        bits = new ToggleButton[8];
        
        for (int i = 0; i < 8; i++) {
            ToggleButton toggleButton = new ToggleButton(getContext());
            toggleButton.setTextOn("1");
            toggleButton.setTextOff("0");
            toggleButton.setChecked(false);
            
            // Create layout for bit
            LinearLayout bitLayout = new LinearLayout(getContext());
            bitLayout.setOrientation(LinearLayout.VERTICAL);
            bitLayout.addView(toggleButton);
            
            // Add positional value label
            TextView tvValue = new TextView(getContext());
            tvValue.setText(String.valueOf(1 << (7 - i))); // 128, 64, 32, etc.
            tvValue.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            bitLayout.addView(tvValue);
            
            // Add layout params
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            params.setMargins(4, 0, 4, 0);
            bitLayout.setLayoutParams(params);
            
            // Add to container
            binaryInputContainer.addView(bitLayout);
            
            // Store toggle button reference
            bits[i] = toggleButton;
        }
    }

    private void generateNewProblem() {
        // Clear previous feedback
        tvFeedback.setText("");
        resetBinaryInput();
        
        // Generate problem based on current level
        int maxValue = getMaxValueForLevel(currentLevel);
        currentDecimalValue = random.nextInt(maxValue) + 1;
        tvDecimalQuestion.setText(String.valueOf(currentDecimalValue));
        
        // Enable submit button for new problem
        btnSubmit.setEnabled(true);
    }
    
    private int getMaxValueForLevel(int level) {
        switch (level) {
            case 1: return 15;    // 4-bit numbers (max 15)
            case 2: return 31;    // 5-bit numbers (max 31)
            case 3: return 63;    // 6-bit numbers (max 63)
            case 4: return 127;   // 7-bit numbers (max 127)
            case 5: return 255;   // 8-bit numbers (max 255)
            default: return 15;   // Default to level 1
        }
    }
    
    private void checkAnswer() {
        totalProblems++;
        
        // Get user's binary input
        int userAnswer = 0;
        for (int i = 0; i < 8; i++) {
            if (bits[i].isChecked()) {
                userAnswer += (1 << (7 - i));
            }
        }
        
        // Check if correct
        boolean isCorrect = (userAnswer == currentDecimalValue);
        
        if (isCorrect) {
            correctAnswers++;
            tvFeedback.setText("¡Correcto! 🎉");
            
            // Check if should level up (every 3 correct answers)
            if (correctAnswers % 3 == 0 && currentLevel < maxLevel) {
                currentLevel++;
                updateLevelDisplay();
                Toast.makeText(getContext(), "¡Subiste al nivel " + currentLevel + "!", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Calculate correct binary representation for feedback
            String correctBinary = Integer.toBinaryString(currentDecimalValue);
            while (correctBinary.length() < 8) {
                correctBinary = "0" + correctBinary;
            }
            
            tvFeedback.setText("Incorrecto. La respuesta correcta es: " + correctBinary);
        }
        
        updateScoreDisplay();
        
        // Disable submit button until next problem
        btnSubmit.setEnabled(false);
    }
    
    private void resetBinaryInput() {
        for (ToggleButton bit : bits) {
            bit.setChecked(false);
        }
    }
    
    private void updateLevelDisplay() {
        tvDifficultyLevel.setText("Nivel: " + currentLevel + " de " + maxLevel);
    }
    
    private void updateScoreDisplay() {
        tvCorrectCounter.setText("Aciertos: " + correctAnswers + " de " + totalProblems);
    }
}
