package com.example.myxapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class CalculatorActivity extends AppCompatActivity {

    EditText display;
    Button button0, button1, button2, button3, button4, button5, button6, button7, button8, button9;
    Button buttonAdd, buttonSubtract, buttonMultiply, buttonDivide, buttonEquals, buttonClear, buttonDelete, buttonDot;

    double operand1 = 0;
    String operator = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Initialize UI elements
        display = findViewById(R.id.display);
        button0 = findViewById(R.id.button0);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        button5 = findViewById(R.id.button5);
        button6 = findViewById(R.id.button6);
        button7 = findViewById(R.id.button7);
        button8 = findViewById(R.id.button8);
        button9 = findViewById(R.id.button9);
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonSubtract = findViewById(R.id.buttonSubtract);
        buttonMultiply = findViewById(R.id.buttonMultiply);
        buttonDivide = findViewById(R.id.buttonDivide);
        buttonEquals = findViewById(R.id.buttonEquals);
        buttonClear = findViewById(R.id.buttonClear);
        buttonDelete = findViewById(R.id.buttonDelete);
        buttonDot = findViewById(R.id.buttonDot);

        // Set click listeners for number buttons
        View.OnClickListener numberClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button = (Button) v;
                String currentText = display.getText().toString();
                if (currentText.equals("0")) {
                    display.setText(button.getText().toString());
                } else {
                    display.setText(currentText + button.getText().toString());
                }
            }
        };

        button0.setOnClickListener(numberClickListener);
        button1.setOnClickListener(numberClickListener);
        button2.setOnClickListener(numberClickListener);
        button3.setOnClickListener(numberClickListener);
        button4.setOnClickListener(numberClickListener);
        button5.setOnClickListener(numberClickListener);
        button6.setOnClickListener(numberClickListener);
        button7.setOnClickListener(numberClickListener);
        button8.setOnClickListener(numberClickListener);
        button9.setOnClickListener(numberClickListener);

        // Set click listeners for operator buttons
        View.OnClickListener operatorClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button = (Button) v;
                operand1 = Double.parseDouble(display.getText().toString());
                operator = button.getText().toString();
                display.setText("0");
            }
        };

        buttonAdd.setOnClickListener(operatorClickListener);
        buttonSubtract.setOnClickListener(operatorClickListener);
        buttonMultiply.setOnClickListener(operatorClickListener);
        buttonDivide.setOnClickListener(operatorClickListener);

        // Set click listener for equals button
        buttonEquals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double operand2 = Double.parseDouble(display.getText().toString());
                double result = 0;

                switch (operator) {
                    case "+":
                        result = operand1 + operand2;
                        break;
                    case "-":
                        result = operand1 - operand2;
                        break;
                    case "*":
                        result = operand1 * operand2;
                        break;
                    case "/":
                        if (operand2 != 0) {
                            result = operand1 / operand2;
                        } else {
                            display.setText("Error");
                            return;
                        }
                        break;
                }

                display.setText(String.valueOf(result));
                operand1 = result;
                operator = "";
            }
        });

        // Set click listener for clear button
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                display.setText("0");
                operand1 = 0;
                operator = "";
            }
        });

        // Set click listener for delete button
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentText = display.getText().toString();
                if (currentText.length() > 0) {
                    display.setText(currentText.substring(0, currentText.length() - 1));
                }
            }
        });

        // Set click listener for dot button
        buttonDot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentText = display.getText().toString();
                if (!currentText.contains(".")) {
                    display.setText(currentText + ".");
                }
            }
        });
    }
}