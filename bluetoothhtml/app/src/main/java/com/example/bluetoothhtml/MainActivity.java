package com.example.bluetoothhtml;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebView;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "BluetoothHTML";
    private static final UUID SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSIONS = 2;
    private static final int REQUEST_DISCOVERABLE = 3;

    private BluetoothAdapter bluetoothAdapter;
    private EditText urlInput;
    private Button serverButton, clientButton, refreshButton;
    private ListView devicesList;
    private WebView webView;
    private TextView statusText;

    private ArrayList<BluetoothDevice> discoveredDevices = new ArrayList<>();
    private ArrayList<String> deviceNames = new ArrayList<>();
    private ArrayAdapter<String> devicesAdapter;
    private AcceptThread acceptThread;
    private boolean isServerRunning = false;

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupBluetooth();
        setupListeners();
        requestPermissions();
    }

    private void initViews() {
        urlInput = findViewById(R.id.urlInput);
        serverButton = findViewById(R.id.serverButton);
        clientButton = findViewById(R.id.clientButton);
        refreshButton = findViewById(R.id.refreshButton);
        devicesList = findViewById(R.id.devicesList);
        webView = findViewById(R.id.webView);
        statusText = findViewById(R.id.statusText);

        devicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceNames);
        devicesList.setAdapter(devicesAdapter);

        webView.getSettings().setJavaScriptEnabled(true);
        urlInput.setText("www.google.com");
    }

    private void setupBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth no soportado en este dispositivo", Toast.LENGTH_LONG).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void setupListeners() {
        serverButton.setOnClickListener(v -> {
            if (!isServerRunning) {
                startServer();
            } else {
                stopServer();
            }
        });

        clientButton.setOnClickListener(v -> startClient());
        refreshButton.setOnClickListener(v -> refreshDevices());

        devicesList.setOnItemClickListener((parent, view, position, id) -> {
            if (position < discoveredDevices.size()) {
                BluetoothDevice device = discoveredDevices.get(position);
                String url = urlInput.getText().toString().trim();
                if (!url.isEmpty()) {
                    sendUrlToDevice(device, url);
                } else {
                    Toast.makeText(this, "Ingresa una URL primero", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Registrar receivers para Bluetooth
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothReceiver, filter);
    }

    private void requestPermissions() {
        ArrayList<String> permissionsNeeded = new ArrayList<>();

        // Permisos básicos siempre necesarios
        String[] basicPermissions = {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.INTERNET
        };

        // Permisos para Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            String[] newPermissions = {
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE
            };
            for (String permission : newPermissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsNeeded.add(permission);
                }
            }
        }

        for (String permission : basicPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]), REQUEST_PERMISSIONS);
        } else {
            loadPairedDevices();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                statusText.setText("Permisos concedidos");
                loadPairedDevices();
            } else {
                statusText.setText("Permisos necesarios para funcionar");
                Toast.makeText(this, "La aplicación necesita permisos de Bluetooth para funcionar", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadPairedDevices() {
        if (!checkBluetoothPermissions()) return;

        discoveredDevices.clear();
        deviceNames.clear();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                discoveredDevices.add(device);
                String deviceName = device.getName();
                if (deviceName == null) deviceName = "Dispositivo desconocido";
                deviceNames.add(deviceName + "\n" + device.getAddress() + " (Emparejado)");
            }
        }

        devicesAdapter.notifyDataSetChanged();
        statusText.setText("Dispositivos emparejados cargados: " + pairedDevices.size());
    }

    private void startServer() {
        if (!checkBluetoothPermissions()) return;

        // Hacer dispositivo descubrible
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE);

        if (acceptThread == null || !acceptThread.isAlive()) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }

        isServerRunning = true;
        serverButton.setText("Detener Servidor");
        statusText.setText("Servidor iniciado - Dispositivo visible por 5 minutos");
    }

    private void stopServer() {
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
        isServerRunning = false;
        serverButton.setText("Ser Servidor");
        statusText.setText("Servidor detenido");
    }

    private void startClient() {
        statusText.setText("Modo cliente activado - Selecciona un dispositivo");
        loadPairedDevices();
    }

    private void refreshDevices() {
        if (!checkBluetoothPermissions()) return;

        statusText.setText("Buscando dispositivos...");

        // Cancelar descubrimiento anterior
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        // Cargar dispositivos emparejados primero
        loadPairedDevices();

        // Iniciar nuevo descubrimiento
        if (bluetoothAdapter.startDiscovery()) {
            refreshButton.setEnabled(false);
        } else {
            statusText.setText("Error iniciando búsqueda");
        }
    }

    private void sendUrlToDevice(BluetoothDevice device, String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        final String urlToSend = url + "\n";

        statusText.setText("Conectando a " + device.getName() + "...");

        new Thread(() -> {
            BluetoothSocket socket = null;
            try {
                // Cancelar descubrimiento para mejorar conexión
                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                }

                // Intentar múltiples métodos de conexión
                socket = createBluetoothSocket(device);

                mainHandler.post(() -> statusText.setText("Estableciendo conexión..."));
                socket.connect();

                mainHandler.post(() -> statusText.setText("Conectado! Enviando URL..."));

                // Usar streams directos para mejor control
                OutputStream outputStream = socket.getOutputStream();
                InputStream inputStream = socket.getInputStream();

                // Enviar URL con terminador claro
                //final String urlToSend = url + "\n";
                outputStream.write(urlToSend.getBytes("UTF-8"));
                outputStream.flush();

                mainHandler.post(() -> statusText.setText("URL enviada, esperando respuesta..."));

                // Recibir HTML con timeout
                StringBuilder htmlBuilder = new StringBuilder();
                byte[] buffer = new byte[1024];
                int bytesRead;
                long startTime = System.currentTimeMillis();
                boolean responseReceived = false;

                while (System.currentTimeMillis() - startTime < 30000) { // 30 segundos timeout
                    if (inputStream.available() > 0) {
                        bytesRead = inputStream.read(buffer);
                        if (bytesRead > 0) {
                            String chunk = new String(buffer, 0, bytesRead, "UTF-8");
                            htmlBuilder.append(chunk);
                            responseReceived = true;

                            // Si el chunk contiene un indicador de fin, salir
                            if (chunk.contains("</html>") || chunk.contains("ERROR:")) {
                                break;
                            }
                        }
                    } else if (responseReceived) {
                        // Si ya recibimos algo y no hay más datos, esperar un poco más
                        Thread.sleep(100);
                    } else {
                        Thread.sleep(50);
                    }
                }

                String html = htmlBuilder.toString().trim();

                mainHandler.post(() -> {
                    if (html.isEmpty()) {
                        statusText.setText("No se recibió respuesta del servidor");
                        Toast.makeText(this, "El servidor no respondió", Toast.LENGTH_LONG).show();
                    } else if (html.startsWith("ERROR:")) {
                        statusText.setText("Error del servidor");
                        Toast.makeText(this, html, Toast.LENGTH_LONG).show();
                    } else {
                        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
                        statusText.setText("HTML recibido y mostrado correctamente");
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error enviando URL", e);
                mainHandler.post(() -> {
                    statusText.setText("Error de conexión con " + device.getName());
                    String errorMsg = "Error: " + e.getMessage();
                    if (e.getMessage().contains("Service discovery failed")) {
                        errorMsg = "Error: El dispositivo no está ejecutando el servidor";
                    } else if (e.getMessage().contains("Connection refused")) {
                        errorMsg = "Error: Conexión rechazada - Verifica que el servidor esté activo";
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                });
            } finally {
                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error cerrando socket", e);
                }
            }
        }).start();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        BluetoothSocket socket = null;

        try {
            // Método 1: Conexión insegura estándar
            socket = device.createInsecureRfcommSocketToServiceRecord(SERVICE_UUID);
            return socket;
        } catch (Exception e1) {
            Log.w(TAG, "Método 1 falló, intentando método 2", e1);

            try {
                // Método 2: Conexión segura estándar
                socket = device.createRfcommSocketToServiceRecord(SERVICE_UUID);
                return socket;
            } catch (Exception e2) {
                Log.w(TAG, "Método 2 falló, intentando método 3", e2);

                // Método 3: Conexión por reflexión (más compatible)
                try {
                    java.lang.reflect.Method m = device.getClass().getMethod(
                            "createInsecureRfcommSocket", new Class[]{int.class});
                    socket = (BluetoothSocket) m.invoke(device, 1);
                    return socket;
                } catch (Exception e3) {
                    Log.e(TAG, "Todos los métodos de conexión fallaron");
                    throw new IOException("No se pudo crear socket Bluetooth");
                }
            }
        }
    }

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && !discoveredDevices.contains(device)) {
                    discoveredDevices.add(device);
                    String deviceName = device.getName();
                    if (deviceName == null) deviceName = "Dispositivo desconocido";
                    deviceNames.add(deviceName + "\n" + device.getAddress() + " (Descubierto)");
                    devicesAdapter.notifyDataSetChanged();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                refreshButton.setEnabled(true);
                statusText.setText("Búsqueda completada - " + discoveredDevices.size() + " dispositivos encontrados");
            }
        }
    };

    private class AcceptThread extends Thread {
        private BluetoothServerSocket serverSocket;
        private boolean shouldRun = true;

        public AcceptThread() {
            try {
                if (checkBluetoothPermissions()) {
                    serverSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
                            "BluetoothHTML", SERVICE_UUID);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error creando server socket", e);
            }
        }

        public void run() {
            while (shouldRun) {
                BluetoothSocket socket = null;
                try {
                    mainHandler.post(() -> statusText.setText("Servidor esperando conexiones..."));
                    socket = serverSocket.accept();

                    if (socket != null) {
                        handleClientConnection(socket);
                    }
                } catch (IOException e) {
                    if (shouldRun) {
                        Log.e(TAG, "Error aceptando conexión", e);
                        mainHandler.post(() -> statusText.setText("Error en servidor"));
                    }
                    break;
                }
            }
        }

        public void cancel() {
            shouldRun = false;
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error cerrando server socket", e);
            }
        }
    }

    private void handleClientConnection(BluetoothSocket socket) {
        try {
            mainHandler.post(() -> statusText.setText("Cliente conectado, procesando solicitud..."));

            // Usar streams directos para mejor control
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            // Leer URL del cliente
            StringBuilder urlBuilder = new StringBuilder();
            byte[] buffer = new byte[1024];
            int bytesRead;

            // Leer hasta encontrar nueva línea o timeout
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 10000) { // 10 segundos timeout
                if (inputStream.available() > 0) {
                    bytesRead = inputStream.read(buffer);
                    if (bytesRead > 0) {
                        String chunk = new String(buffer, 0, bytesRead, "UTF-8");
                        urlBuilder.append(chunk);

                        // Si encontramos nueva línea, tenemos la URL completa
                        if (chunk.contains("\n")) {
                            break;
                        }
                    }
                } else {
                    Thread.sleep(50);
                }
            }

            String url = urlBuilder.toString().trim();

            if (url.isEmpty()) {
                String errorMsg = "ERROR: No se recibió URL del cliente\n";
                outputStream.write(errorMsg.getBytes("UTF-8"));
                outputStream.flush();
                mainHandler.post(() -> statusText.setText("Error: URL vacía del cliente"));
                return;
            }

            mainHandler.post(() -> statusText.setText("Descargando: " + url));

            String html = downloadHtml(url);

            if (html != null && !html.isEmpty() && !html.startsWith("ERROR:")) {
                // Enviar HTML con indicador de finalización
                outputStream.write(html.getBytes("UTF-8"));
                outputStream.flush();

                // Pequeña pausa para asegurar que se envíe todo
                Thread.sleep(500);

                mainHandler.post(() -> statusText.setText("HTML enviado al cliente correctamente"));
            } else {
                String errorMsg = html != null ? html : "ERROR: No se pudo descargar la página";
                outputStream.write(errorMsg.getBytes("UTF-8"));
                outputStream.flush();
                mainHandler.post(() -> statusText.setText("Error enviado al cliente"));
            }

        } catch (Exception e) {
            Log.e(TAG, "Error manejando cliente", e);
            mainHandler.post(() -> statusText.setText("Error procesando solicitud del cliente"));

            try {
                String errorMsg = "ERROR: Error interno del servidor\n";
                socket.getOutputStream().write(errorMsg.getBytes("UTF-8"));
                socket.getOutputStream().flush();
            } catch (Exception e2) {
                Log.e(TAG, "Error enviando mensaje de error", e2);
            }
        } finally {
            try {
                Thread.sleep(1000); // Dar tiempo para que se complete la transferencia
                socket.close();
            } catch (Exception e) {
                Log.e(TAG, "Error cerrando socket cliente", e);
            }
        }
    }

    private String downloadHtml(String urlString) {
        try {
            // Validar URL
            if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
                urlString = "https://" + urlString;
            }

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Configurar conexión
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(20000); // 20 segundos
            connection.setReadTimeout(20000);
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36");
            connection.setRequestProperty("Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language", "es-ES,es;q=0.8,en;q=0.6");
            connection.setRequestProperty("Connection", "close");

            // Seguir redirecciones manualmente si es necesario
            connection.setInstanceFollowRedirects(true);

            int responseCode = connection.getResponseCode();

            // Manejar códigos de respuesta
            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                    responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                    responseCode == HttpURLConnection.HTTP_SEE_OTHER) {

                String newUrl = connection.getHeaderField("Location");
                connection.disconnect();
                return downloadHtml(newUrl); // Recursión para seguir redirección
            }

            if (responseCode != HttpURLConnection.HTTP_OK) {
                connection.disconnect();
                return "ERROR: Código de respuesta HTTP " + responseCode + " - " + connection.getResponseMessage();
            }

            // Leer contenido
            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            StringBuilder html = new StringBuilder();
            String line;
            int maxLines = 1000; // Limitar número de líneas para evitar memoria excesiva
            int lineCount = 0;

            while ((line = reader.readLine()) != null && lineCount < maxLines) {
                html.append(line).append("\n");
                lineCount++;
            }

            reader.close();
            inputStream.close();
            connection.disconnect();

            String result = html.toString();

            // Verificar que el HTML sea válido
            if (result.length() < 50) {
                return "ERROR: Respuesta muy corta, posible error del servidor";
            }

            // Añadir meta tag para viewport si no existe (mejor visualización móvil)
            if (!result.toLowerCase().contains("viewport")) {
                result = result.replaceFirst("(?i)<head>",
                        "<head>\n<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
            }

            return result;

        } catch (java.net.MalformedURLException e) {
            Log.e(TAG, "URL malformada", e);
            return "ERROR: URL inválida - " + e.getMessage();
        } catch (java.net.ConnectException e) {
            Log.e(TAG, "Error de conexión", e);
            return "ERROR: No se pudo conectar al servidor - Verifica tu conexión a internet";
        } catch (java.net.SocketTimeoutException e) {
            Log.e(TAG, "Timeout de conexión", e);
            return "ERROR: Tiempo de espera agotado - El servidor tardó demasiado en responder";
        } catch (java.net.UnknownHostException e) {
            Log.e(TAG, "Host desconocido", e);
            return "ERROR: No se pudo encontrar el sitio web - Verifica la URL";
        } catch (javax.net.ssl.SSLException e) {
            Log.e(TAG, "Error SSL", e);
            // Intentar con HTTP si HTTPS falla
            if (urlString.startsWith("https://")) {
                String httpUrl = urlString.replace("https://", "http://");
                return downloadHtml(httpUrl);
            }
            return "ERROR: Error de seguridad SSL - " + e.getMessage();
        } catch (Exception e) {
            Log.e(TAG, "Error descargando HTML", e);
            return "ERROR: Error general descargando página - " + e.getMessage();
        }
    }

    private boolean checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                statusText.setText("Bluetooth activado");
                loadPairedDevices();
            } else {
                statusText.setText("Bluetooth necesario para funcionar");
            }
        } else if (requestCode == REQUEST_DISCOVERABLE) {
            if (resultCode > 0) {
                statusText.setText("Dispositivo visible por " + resultCode + " segundos");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopServer();

        try {
            unregisterReceiver(bluetoothReceiver);
        } catch (Exception e) {
            // Receiver ya desregistrado
        }

        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            if (checkBluetoothPermissions()) {
                bluetoothAdapter.cancelDiscovery();
            }
        }
    }
}