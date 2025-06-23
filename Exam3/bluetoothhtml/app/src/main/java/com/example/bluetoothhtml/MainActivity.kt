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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
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
    private static final UUID SERVICE_UUID = UUID.fromString("12345678-1234-1234-1234-123456789012");
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSIONS = 2;

    private BluetoothAdapter bluetoothAdapter;
    private EditText urlInput;
    private Button serverButton, clientButton, sendButton;
    private ListView devicesList;
    private WebView webView;
    private TextView statusText;

    private ArrayList<BluetoothDevice> discoveredDevices = new ArrayList<>();
    private ArrayAdapter<String> devicesAdapter;
    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private BluetoothDevice selectedDevice;

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
        sendButton = findViewById(R.id.sendButton);
        devicesList = findViewById(R.id.devicesList);
        webView = findViewById(R.id.webView);
        statusText = findViewById(R.id.statusText);

        devicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        devicesList.setAdapter(devicesAdapter);

        webView.getSettings().setJavaScriptEnabled(true);
    }

    private void setupBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth no soportado", Toast.LENGTH_LONG).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void setupListeners() {
        serverButton.setOnClickListener(v -> startServer());
        clientButton.setOnClickListener(v -> startClient());
        sendButton.setOnClickListener(v -> sendUrl());

        devicesList.setOnItemClickListener((parent, view, position, id) -> {
        selectedDevice = discoveredDevices.get(position);
        statusText.setText("Dispositivo seleccionado: " + selectedDevice.getName());
    });

        // Receiver para dispositivos descubiertos
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(deviceFoundReceiver, filter);
    }

    private void requestPermissions() {
        String[] permissions = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET
        };

        boolean needsPermission = false;
        for (String permission : permissions) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            needsPermission = true;
            break;
        }
    }

        if (needsPermission) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
        }
    }

    private void startServer() {
        statusText.setText("Iniciando servidor...");
        serverButton.setEnabled(false);

        // Hacer el dispositivo descubrible
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(discoverableIntent);

        acceptThread = new AcceptThread();
        acceptThread.start();
    }

    private void startClient() {
        statusText.setText("Buscando servidores...");
        clientButton.setEnabled(false);

        discoveredDevices.clear();
        devicesAdapter.clear();

        // Mostrar dispositivos emparejados
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
        discoveredDevices.add(device);
        devicesAdapter.add(device.getName() + "\n" + device.getAddress());
    }

        // Iniciar descubrimiento
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
    }

    private void sendUrl() {
        if (selectedDevice == null) {
            Toast.makeText(this, "Selecciona un dispositivo primero", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = urlInput.getText().toString().trim();
        if (url.isEmpty()) {
            Toast.makeText(this, "Ingresa una URL", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        connectThread = new ConnectThread(selectedDevice, url);
        connectThread.start();
    }

    private final BroadcastReceiver deviceFoundReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && !discoveredDevices.contains(device)) {
                    discoveredDevices.add(device);
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    devicesAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
        }
    };

    private class AcceptThread extends Thread {
        private BluetoothServerSocket serverSocket;

        public AcceptThread() {
            try {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("BluetoothHTML", SERVICE_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Error creating server socket", e);
            }
        }

        public void run() {
            BluetoothSocket socket = null;
            while (true) {
                try {
                    mainHandler.post(() -> statusText.setText("Esperando conexión..."));
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Error accepting connection", e);
                    break;
                }

                if (socket != null) {
                    handleServerConnection(socket);
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing server socket", e);
                    }
                    break;
                }
            }
        }

        public void cancel() {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error closing server socket", e);
            }
        }
    }

    private void handleServerConnection(BluetoothSocket socket) {
        try {
            mainHandler.post(() -> statusText.setText("Cliente conectado"));

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            String url = reader.readLine();
            mainHandler.post(() -> statusText.setText("Descargando: " + url));

            String html = downloadHtml(url);

            if (html != null) {
                writer.println(html);
                mainHandler.post(() -> statusText.setText("HTML enviado exitosamente"));
            } else {
                writer.println("ERROR: No se pudo descargar la página");
                mainHandler.post(() -> statusText.setText("Error descargando página"));
            }

            socket.close();
        } catch (IOException e) {
            Log.e(TAG, "Error handling server connection", e);
        }

        mainHandler.post(() -> serverButton.setEnabled(true));
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket socket;
        private BluetoothDevice device;
        private String url;

        public ConnectThread(BluetoothDevice device, String url) {
        this.device = device;
        this.url = url;

        try {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            socket = device.createRfcommSocketToServiceRecord(SERVICE_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Error creating client socket", e);
        }
    }

        public void run() {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            bluetoothAdapter.cancelDiscovery();

            try {
                mainHandler.post(() -> statusText.setText("Conectando al servidor..."));
                socket.connect();

                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                writer.println(url);
                mainHandler.post(() -> statusText.setText("URL enviada, esperando HTML..."));

                StringBuilder htmlBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    htmlBuilder.append(line).append("\n");
                }

                String html = htmlBuilder.toString();
                mainHandler.post(() -> {
                    webView.loadData(html, "text/html", "UTF-8");
                    statusText.setText("HTML recibido y mostrado");
                });

                socket.close();

            } catch (IOException e) {
                Log.e(TAG, "Error connecting to server", e);
                mainHandler.post(() -> statusText.setText("Error conectando al servidor"));
            }

            mainHandler.post(() -> clientButton.setEnabled(true));
        }

        public void cancel() {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error closing client socket", e);
            }
        }
    }

    private String downloadHtml(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder html = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                html.append(line).append("\n");
            }

            reader.close();
            connection.disconnect();

            return html.toString();

        } catch (Exception e) {
            Log.e(TAG, "Error downloading HTML", e);
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (acceptThread != null) {
            acceptThread.cancel();
        }

        if (connectThread != null) {
            connectThread.cancel();
        }

        try {
            unregisterReceiver(deviceFoundReceiver);
        } catch (Exception e) {
            // Receiver ya fue desregistrado
        }
    }
}