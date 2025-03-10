package com.example.myxapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageButton;

public class ChronometerActivity extends AppCompatActivity {
    private Chronometer chronometer;
    private TextView tvMillis;
    private boolean running;
    private long pauseOffset;
    private Handler handler;
    private Runnable updateMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chronometer);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        chronometer = findViewById(R.id.chronometer);
        tvMillis = findViewById(R.id.tvMillis);
        Button btnStart = findViewById(R.id.btnStart);
        Button btnPause = findViewById(R.id.btnPause);
        Button btnReset = findViewById(R.id.btnReset);

        handler = new Handler();
        updateMillis = new Runnable() {
            @Override
            public void run() {
                if (running) {
                    long time = SystemClock.elapsedRealtime() - chronometer.getBase();
                    int milliseconds = (int) (time % 1000) / 10;
                    tvMillis.setText(String.format(".%02d", milliseconds));
                    handler.postDelayed(this, 10);
                }
            }
        };

        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer cArg) {
                long time = SystemClock.elapsedRealtime() - cArg.getBase();
                int hours = (int) (time / 3600000);
                int minutes = (int) (time - hours * 3600000) / 60000;
                int seconds = (int) (time - hours * 3600000 - minutes * 60000) / 1000;

                String displayTime;
                if (hours > 0) {
                    displayTime = String.format("%d:%02d:%02d", hours, minutes, seconds);
                } else {
                    displayTime = String.format("%02d:%02d", minutes, seconds);
                }
                cArg.setText(displayTime);
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!running) {
                    chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
                    chronometer.start();
                    running = true;
                    handler.post(updateMillis);
                }
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (running) {
                    chronometer.stop();
                    handler.removeCallbacks(updateMillis);
                    pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
                    running = false;
                }
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.setText("00:00"); // Reset display to initial state
                tvMillis.setText(".00");
                pauseOffset = 0;
                if (!running) {
                    chronometer.stop();
                    handler.removeCallbacks(updateMillis);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateMillis);
    }
}