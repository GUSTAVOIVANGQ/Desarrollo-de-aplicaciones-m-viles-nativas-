package com.example.helloandroid;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import com.example.helloandroid.util.ServerConfig;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    
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
        initApiService();

        // Configurar botón de actualización
        refreshButton.setOnClickListener(v -> fetchDataFromApi());

        // Cargar datos al iniciar
        fetchDataFromApi();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 1, Menu.NONE, R.string.config_server)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(Menu.NONE, 2, Menu.NONE, R.string.device_info)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(Menu.NONE, 3, Menu.NONE, R.string.enter_ip_manually)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            showServerConfigDialog();
            return true;
        } else if (item.getItemId() == 2) {
            showDeviceInfoDialog();
            return true;
        } else if (item.getItemId() == 3) {
            showManualIpDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void showManualIpDialog() {
        // Creamos un dialogo simple para ingresar manualmente la IP
        EditText input = new EditText(this);
        input.setHint("172.100.90.248"); // IP sugerida como ejemplo
        
        new AlertDialog.Builder(this)
            .setTitle(R.string.enter_ip_manually)
            .setView(input)
            .setPositiveButton(R.string.save, (dialog, which) -> {
                String ip = input.getText().toString().trim();
                if (!TextUtils.isEmpty(ip)) {
                    // Asegurar que la URL tenga el formato correcto
                    String url = "http://" + ip + ":8088/";
                    ServerConfig.saveServerUrl(MainActivity.this, url);
                    ServerConfig.resetServerList(); // Regenerar la lista de servidores
                    ApiClient.resetClient();
                    initApiService();
                    fetchDataFromApi();
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
    
    private void initApiService() {
        Retrofit retrofit = ApiClient.getClient(this);
        apiService = retrofit.create(ApiService.class);
        
        // Mostrar la URL actual en un toast para verificación
        String baseUrl = retrofit.baseUrl().toString();
        Toast.makeText(this, "URL del servidor: " + baseUrl, Toast.LENGTH_SHORT).show();
    }
    
    private void showDeviceInfoDialog() {
        String deviceInfo = ServerConfig.getDeviceInfo();
        String currentUrl = ServerConfig.getServerUrl(this);
        
        // Crear un mensaje más detallado
        StringBuilder message = new StringBuilder();
        message.append(deviceInfo).append("\n\n");
        message.append("URL del servidor actual:\n").append(currentUrl).append("\n\n");
        message.append("Dirección IP del dispositivo: 172.100.86.140\n\n");
        message.append("IPs detectadas en la red:\n");
        
        // Obtener las IPs detectadas (eliminar el http:// y el puerto)
        String[] servers = ServerConfig.getPredefinedServers(this);
        for (String server : servers) {
            String ip = server.replace("http://", "").replace(":8088/", "");
            message.append("• ").append(ip).append("\n");
        }
        
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.device_info)
                .setMessage(message.toString())
                .setPositiveButton(android.R.string.ok, null)
                .setNeutralButton(R.string.copy_info, (dialogInterface, i) -> {
                    // Copiar la información al portapapeles
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Información de red", message.toString());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(MainActivity.this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
                })
                .show();
    }
    
    private void showServerConfigDialog() {
        // Forzar regeneración de la lista de servidores
        ServerConfig.resetServerList();
        
        View view = getLayoutInflater().inflate(R.layout.dialog_server_config, null);
        EditText editTextServerUrl = view.findViewById(R.id.editTextServerUrl);
        Spinner spinnerPredefinedServers = view.findViewById(R.id.spinnerPredefinedServers);
        
        String currentUrl = ServerConfig.getServerUrl(this);
        editTextServerUrl.setText(currentUrl);
        
        // Obtener servidores predefinidos dinámicamente (incluyendo IPs detectadas)
        String[] predefinedServers = ServerConfig.getPredefinedServers(this);
        
        // Ordenar los servidores para poner primero la red 172.100.x.x
        Arrays.sort(predefinedServers, (a, b) -> {
            boolean aIs172 = a.contains("172.100");
            boolean bIs172 = b.contains("172.100");
            if (aIs172 && !bIs172) return -1;
            if (!aIs172 && bIs172) return 1;
            return 0;
        });
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, predefinedServers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPredefinedServers.setAdapter(adapter);
        
        spinnerPredefinedServers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editTextServerUrl.setText(predefinedServers[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });
        
        new AlertDialog.Builder(this)
                .setTitle(R.string.config_server)
                .setView(view)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String newUrl = editTextServerUrl.getText().toString().trim();
                    if (!newUrl.endsWith("/")) {
                        newUrl = newUrl + "/";
                    }
                    
                    // Si no empieza con http://, añadirlo
                    if (!newUrl.startsWith("http://") && !newUrl.startsWith("https://")) {
                        newUrl = "http://" + newUrl;
                    }
                    
                    ServerConfig.saveServerUrl(MainActivity.this, newUrl);
                    ApiClient.resetClient();
                    initApiService();
                    fetchDataFromApi();
                    Toast.makeText(MainActivity.this, 
                            getString(R.string.server_url_updated, newUrl), 
                            Toast.LENGTH_LONG).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // El resto del MainActivity sigue igual...
    
    private void fetchDataFromApi() {
        showLoading();
        
        String baseUrl = ServerConfig.getServerUrl(this);
        Log.d(TAG, "Iniciando petición a la API: " + baseUrl);
        
        apiService.getHello().enqueue(new Callback<HelloResponse>() {
            @Override
            public void onResponse(Call<HelloResponse> call, Response<HelloResponse> response) {
                hideLoading();
                
                Log.d(TAG, "Respuesta recibida. Código: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    HelloResponse helloResponse = response.body();
                    Log.d(TAG, "Respuesta exitosa: " + helloResponse.getMessage());
                    updateUI(helloResponse);
                } else {
                    String errorMessage = "Error del servidor: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMessage += " - " + response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error al leer errorBody", e);
                    }
                    Log.e(TAG, errorMessage);
                    showError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<HelloResponse> call, Throwable t) {
                hideLoading();
                
                String errorMessage;
                if (t instanceof UnknownHostException) {
                    errorMessage = "Error: Host no encontrado. Verifica la URL y tu conexión a internet.";
                } else if (t instanceof SocketTimeoutException) {
                    errorMessage = "Error: Tiempo de espera agotado (" + t.getMessage() + "). El servidor tarda demasiado en responder.";
                } else if (t instanceof ConnectException) {
                    errorMessage = "Error: No se puede conectar al servidor. Verifica que esté ejecutándose.";
                } else if (t instanceof IOException) {
                    errorMessage = "Error de red: " + t.getMessage();
                } else {
                    errorMessage = "Error desconocido: " + t.getMessage();
                }
                
                Log.e(TAG, "Error en la petición", t);
                showError(errorMessage);
                
                // Sugerencia para configurar el servidor
                Toast.makeText(MainActivity.this, 
                        "Error de conexión. Prueba otra URL de servidor en el menú.", 
                        Toast.LENGTH_LONG).show();
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