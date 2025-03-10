package com.example.helloandroid;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.helloandroid.api.ApiClient;
import com.example.helloandroid.api.ApiService;
import com.example.helloandroid.model.HelloResponse;
import com.example.helloandroid.util.DateTimeUtil;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextView messageTextView;
    private TextView timestampTextView;
    private TextView errorTextView;
    private ProgressBar progressBar;
    private Button refreshButton;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Configurar insets para el sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        messageTextView = findViewById(R.id.messageTextView);
        timestampTextView = findViewById(R.id.timestampTextView);
        errorTextView = findViewById(R.id.errorTextView);
        progressBar = findViewById(R.id.progressBar);
        refreshButton = findViewById(R.id.refreshButton);

        // Inicializar API Service
        apiService = ApiClient.getClient().create(ApiService.class);

        // Configurar botón de actualización
        refreshButton.setOnClickListener(v -> fetchDataFromApi());

        // Cargar datos al iniciar
        fetchDataFromApi();
    }

    private void fetchDataFromApi() {
        showLoading();
        
        apiService.getHello().enqueue(new Callback<HelloResponse>() {
            @Override
            public void onResponse(Call<HelloResponse> call, Response<HelloResponse> response) {
                hideLoading();
                
                if (response.isSuccessful() && response.body() != null) {
                    HelloResponse helloResponse = response.body();
                    updateUI(helloResponse);
                } else {
                    showError(getString(R.string.error_server));
                }
            }

            @Override
            public void onFailure(Call<HelloResponse> call, Throwable t) {
                hideLoading();
                
                if (t instanceof IOException) {
                    showError(getString(R.string.error_network));
                } else {
                    showError(getString(R.string.error_unknown));
                }
            }
        });
    }

    private void updateUI(HelloResponse response) {
        errorTextView.setVisibility(View.GONE);
        messageTextView.setVisibility(View.VISIBLE);
        timestampTextView.setVisibility(View.VISIBLE);
        
        messageTextView.setText(response.getMessage());
        String formattedDate = DateTimeUtil.formatTimestamp(response.getTimestamp());
        timestampTextView.setText(getString(R.string.timestamp_format, formattedDate));
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        messageTextView.setVisibility(View.GONE);
        timestampTextView.setVisibility(View.GONE);
        errorTextView.setVisibility(View.GONE);
        refreshButton.setEnabled(false);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        refreshButton.setEnabled(true);
    }

    private void showError(String errorMessage) {
        messageTextView.setVisibility(View.GONE);
        timestampTextView.setVisibility(View.GONE);
        errorTextView.setVisibility(View.VISIBLE);
        errorTextView.setText(errorMessage);
    }
}