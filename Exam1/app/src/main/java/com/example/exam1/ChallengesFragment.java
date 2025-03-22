package com.example.exam1;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.fragment.app.Fragment;

import java.util.Random;

public class ChallengesFragment extends Fragment {

    private TextView tvChallengePrompt;
    private TextView tvTimeRemaining;
    private ProgressBar progressTimer;
    private LinearLayout binaryInputContainer;
    private Button btnSubmit;
    private TextView tvScore;
    private TextView tvFeedback;
    private Button btnStartChallenge;

    private ToggleButton[] bits;
    private Random random = new Random();
    private int currentScore = 0;
    private int currentChallenge = 0;
    private int totalChallenges = 5;
    private CountDownTimer timer;
    private boolean isChallengeActive = false;
    private int currentAnswer;
    private boolean isDecimalToBinary = true; // true = decimal->binary, false = binary->decimal

    public ChallengesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout
        View view = inflater.inflate(R.layout.fragment_challenges, container, false);

        // Initialize views
        tvChallengePrompt = view.findViewById(R.id.tvChallengePrompt);
        tvTimeRemaining = view.findViewById(R.id.tvTimeRemaining);
        progressTimer = view.findViewById(R.id.progressTimer);
        binaryInputContainer = view.findViewById(R.id.binaryInputContainer);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        tvScore = view.findViewById(R.id.tvScore);
        tvFeedback = view.findViewById(R.id.tvFeedback);
        btnStartChallenge = view.findViewById(R.id.btnStartChallenge);

        // Set up binary input toggles
        setupBinaryInputs();

        // Set up button listeners
        btnSubmit.setOnClickListener(v -> submitAnswer());
        btnStartChallenge.setOnClickListener(v -> startChallenge());

        // Initialize UI
        updateScoreDisplay();
        setGameboardEnabled(false);

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
            
            // Add layout params
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            params.setMargins(2, 0, 2, 0);
            toggleButton.setLayoutParams(params);
            
            // Add to container
            binaryInputContainer.addView(toggleButton);
            
            // Store toggle button reference
            bits[i] = toggleButton;
        }
    }

    private void startChallenge() {
        // Reset game state
        currentScore = 0;
        currentChallenge = 0;
        updateScoreDisplay();
        
        // Hide start button, show game board
        btnStartChallenge.setVisibility(View.GONE);
        setGameboardEnabled(true);
        
        // Start the challenge
        isChallengeActive = true;
        nextChallenge();
    }
    
    private void nextChallenge() {
        if (currentChallenge >= totalChallenges) {
            endChallengeSession();
            return;
        }
        
        currentChallenge++;
        resetBinaryInput();
        tvFeedback.setText("");
        
        // Alternate between decimal->binary and binary->decimal challenges
        isDecimalToBinary = !isDecimalToBinary;
        
        if (isDecimalToBinary) {
            // Decimal to binary challenge
            currentAnswer = random.nextInt(256);
            tvChallengePrompt.setText("Convierte a binario: " + currentAnswer);
        } else {
            // Binary to decimal challenge
            currentAnswer = random.nextInt(256);
            String binaryString = Integer.toBinaryString(currentAnswer);
            while (binaryString.length() < 8) {
                binaryString = "0" + binaryString;
            }
            tvChallengePrompt.setText("Convierte a decimal: " + binaryString);
        }
        
        // Start the timer
        startTimer();
    }
    
    private void startTimer() {
        // Cancel any running timer
        if (timer != null) {
            timer.cancel();
        }
        
        // Set max progress
        progressTimer.setMax(100);
        progressTimer.setProgress(100);
        
        // Create new timer - 20 seconds countdown
        timer = new CountDownTimer(20000, 200) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000) + 1;
                tvTimeRemaining.setText(secondsLeft + "s");
                
                // Update progress bar
                int progressValue = (int) (millisUntilFinished * 100 / 20000);
                progressTimer.setProgress(progressValue);
            }
            
            @Override
            public void onFinish() {
                tvTimeRemaining.setText("0s");
                progressTimer.setProgress(0);
                timeUp();
            }
        }.start();
    }
    
    private void timeUp() {
        tvFeedback.setText("¡Tiempo agotado!");
        submitAnswer(); // This will check and move to next challenge
    }
    
    private void submitAnswer() {
        // Cancel the timer
        if (timer != null) {
            timer.cancel();
        }
        
        boolean isCorrect = false;
        
        if (isDecimalToBinary) {
            // Check decimal to binary conversion
            int userAnswer = 0;
            for (int i = 0; i < 8; i++) {
                if (bits[i].isChecked()) {
                    userAnswer += (1 << (7 - i));
                }
            }
            isCorrect = (userAnswer == currentAnswer);
        } else {
            // Check binary to decimal conversion (user inputs decimal using binary toggles)
            // This is a simplified version where we use the binary toggles to represent
            // decimal digits (only using the last 3 toggles for numbers up to 255)
            int userAnswer = 0;
            for (int i = 5; i < 8; i++) {
                if (bits[i].isChecked()) {
                    // Using positions 5,6,7 to represent 100s, 10s, 1s digits
                    userAnswer += (bits[i].isChecked() ? 1 : 0) * Math.pow(10, 7-i);
                }
            }
            isCorrect = (userAnswer == currentAnswer);
        }
        
        if (isCorrect) {
            currentScore++;
            tvFeedback.setText("¡Correcto! +1 punto");
        } else {
            if (isDecimalToBinary) {
                String correctBinary = Integer.toBinaryString(currentAnswer);
                while (correctBinary.length() < 8) {
                    correctBinary = "0" + correctBinary;
                }
                tvFeedback.setText("Incorrecto. La respuesta es: " + correctBinary);
            } else {
                tvFeedback.setText("Incorrecto. La respuesta es: " + currentAnswer);
            }
        }
        
        updateScoreDisplay();
        
        // Short delay before next challenge
        btnSubmit.setEnabled(false);
        new android.os.Handler().postDelayed(() -> {
            btnSubmit.setEnabled(true);
            nextChallenge();
        }, 2000);
    }
    
    private void endChallengeSession() {
        isChallengeActive = false;
        setGameboardEnabled(false);
        
        // Show final score and restart button
        tvChallengePrompt.setText("¡Desafío completado!");
        tvFeedback.setText("Puntuación final: " + currentScore + " de " + totalChallenges);
        btnStartChallenge.setText("Reiniciar Desafío");
        btnStartChallenge.setVisibility(View.VISIBLE);
    }
    
    private void resetBinaryInput() {
        for (ToggleButton bit : bits) {
            bit.setChecked(false);
        }
    }
    
    private void updateScoreDisplay() {
        tvScore.setText("Puntuación: " + currentScore + " / " + totalChallenges);
    }
    
    private void setGameboardEnabled(boolean enabled) {
        for (ToggleButton bit : bits) {
            bit.setEnabled(enabled);
        }
        btnSubmit.setEnabled(enabled);
        
        // Show/hide relevant UI components
        int visibility = enabled ? View.VISIBLE : View.GONE;
        tvTimeRemaining.setVisibility(visibility);
        progressTimer.setVisibility(visibility);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}
