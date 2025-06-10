package com.example.puzzle.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.RadioGroup
import android.widget.RadioButton
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.puzzle.R
import com.example.puzzle.models.GameState
import com.example.puzzle.util.ThemeManager

/**
 * Actividad para gestionar la configuración del modo multijugador y establecer conexiones Bluetooth.
 */
class MultiplayerActivity : AppCompatActivity(), GameSyncManager.OnConnectionEventListener {
      companion object {
        private const val TAG = "MultiplayerActivity"
        private const val REQUEST_ENABLE_BT = 1
        private const val REQUEST_PERMISSIONS = 2
          // Permisos necesarios para Bluetooth en Android 12+
        private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }
      // Elementos de UI
    private lateinit var statusText: TextView
    private lateinit var deviceListView: ListView
    private lateinit var scanButton: Button
    private lateinit var hostButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var gameModeRadioGroup: RadioGroup
    private lateinit var cooperativeRadioButton: RadioButton
    private lateinit var competitiveRadioButton: RadioButton
    
    // Bluetooth y sincronización
    private lateinit var gameSyncManager: GameSyncManager
    private val devicesList = ArrayList<BluetoothDevice>()
    private lateinit var deviceListAdapter: ArrayAdapter<String>
    private lateinit var bluetoothAdapter: BluetoothAdapter
    
    // Gestión de temas
    private lateinit var themeManager: ThemeManager
    
    // Launcher para solicitar activar Bluetooth
    private val bluetoothEnableLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            setupBluetooth()
        } else {
            Toast.makeText(this, R.string.bluetooth_not_enabled, Toast.LENGTH_SHORT).show()
            finish()
        }
    }
      // Receiver para detectar dispositivos Bluetooth
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "BroadcastReceiver onReceive: ${intent.action}")
            
            when(intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    Log.d(TAG, "Dispositivo Bluetooth encontrado")
                    
                    // Dispositivo encontrado
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    
                    device?.let {
                        Log.d(TAG, "Procesando dispositivo: ${it.address}")
                        
                        // Comprobar permisos antes de acceder al nombre
                        val deviceName = try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                                    it.name ?: "Dispositivo desconocido"
                                } else {
                                    Log.w(TAG, "Permiso BLUETOOTH_CONNECT no concedido")
                                    "Dispositivo desconocido"
                                }
                            } else {
                                it.name ?: "Dispositivo desconocido"
                            }
                        } catch (e: SecurityException) {
                            Log.e(TAG, "Error de seguridad al acceder al nombre del dispositivo", e)
                            "Dispositivo desconocido"
                        }
                        
                        if (!devicesList.contains(it)) {
                            devicesList.add(it)
                            val displayText = "$deviceName\n${it.address}"
                            deviceListAdapter.add(displayText)
                            deviceListAdapter.notifyDataSetChanged()
                            
                            // Hacer visible la lista de dispositivos
                            deviceListView.visibility = View.VISIBLE
                            
                            Log.d(TAG, "Dispositivo añadido: $deviceName (${it.address})")
                        } else {
                            Log.d(TAG, "Dispositivo ya existe en la lista: ${it.address}")
                        }
                    } ?: Log.w(TAG, "Dispositivo es null en ACTION_FOUND")
                }
                
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d(TAG, "Descubrimiento de dispositivos iniciado")
                    progressBar.visibility = View.VISIBLE
                    scanButton.isEnabled = false
                    updateConnectionStatus(getString(R.string.status_scanning))
                }
                
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d(TAG, "Descubrimiento de dispositivos finalizado. Dispositivos encontrados: ${devicesList.size}")
                    
                    progressBar.visibility = View.GONE
                    scanButton.isEnabled = true
                    scanButton.text = getString(R.string.btn_scan_again)
                    
                    if (devicesList.isEmpty()) {
                        Toast.makeText(context, R.string.no_devices_found, Toast.LENGTH_SHORT).show()
                        deviceListView.visibility = View.GONE
                        updateConnectionStatus(getString(R.string.status_not_connected))
                    } else {
                        updateConnectionStatus("${devicesList.size} dispositivos encontrados")
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplicar tema antes de crear la actividad
        themeManager = ThemeManager.getInstance(this)
        themeManager.applyTheme(this)
        
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiplayer)
        
        // Configurar la barra de acción
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.multiplayer_title)        // Inicializar elementos de UI
        statusText = findViewById(R.id.statusTextView)
        deviceListView = findViewById(R.id.deviceListView)
        scanButton = findViewById(R.id.scanButton)
        hostButton = findViewById(R.id.hostButton)
        progressBar = findViewById(R.id.progressBar)
        gameModeRadioGroup = findViewById(R.id.gameModeRadioGroup)
        cooperativeRadioButton = findViewById(R.id.cooperativeRadioButton)
        competitiveRadioButton = findViewById(R.id.competitiveRadioButton)
        
        // Establecer modo cooperativo por defecto
        cooperativeRadioButton.isChecked = true

        // Configurar el adaptador para la lista de dispositivos
        deviceListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ArrayList())
        deviceListView.adapter = deviceListAdapter
        
        // Inicializar GameSyncManager
        gameSyncManager = GameSyncManager(this)
        gameSyncManager.setOnConnectionEventListener(this)
        
        // Configurar listener para clicks en dispositivos
        deviceListView.setOnItemClickListener { _, _, position, _ ->
            // Cancelar descubrimiento de dispositivos porque consume recursos
            if (::bluetoothAdapter.isInitialized && bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
            }
            
            // Obtener el dispositivo seleccionado
            val device = devicesList[position]
            
            // Preguntar si está seguro de conectarse
            AlertDialog.Builder(this)
                .setTitle(R.string.connect_to_device)
                .setMessage(getString(R.string.confirm_connect_to_device, device.name ?: "Dispositivo desconocido"))
                .setPositiveButton(R.string.btn_connect) { _, _ ->
                    connectToDevice(device)
                }
                .setNegativeButton(R.string.btn_cancel, null)
                .create()
                .show()
        }
        
        // Configurar botón de escaneo
        scanButton.setOnClickListener {
            if (checkPermissions()) {
                scanForDevices()
            }
        }
        
        // Configurar botón de host
        hostButton.setOnClickListener {
            startHosting()
        }        // Configurar toggle de modo de juego
        gameModeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.competitiveRadioButton -> com.example.puzzle.bluetooth.BluetoothManager.GAME_MODE_COMPETITIVE
                else -> com.example.puzzle.bluetooth.BluetoothManager.GAME_MODE_COOPERATIVE
            }
            gameSyncManager.setGameMode(mode)
        }
        
        // Verificar permisos y Bluetooth
        if (checkPermissions()) {
            initializeBluetooth()
        }
    }
    
    /**
     * Inicializa el adaptador Bluetooth y verifica disponibilidad
     */
    private fun initializeBluetooth() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        
        // Comprobar si el dispositivo soporta Bluetooth
        if (!::bluetoothAdapter.isInitialized) {
            Toast.makeText(this, R.string.bluetooth_not_supported, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Comprobar si Bluetooth está activado
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    bluetoothEnableLauncher.launch(enableBtIntent)
                } else {
                    Toast.makeText(this, R.string.bluetooth_permission_required, Toast.LENGTH_SHORT).show()
                    checkPermissions()
                }
            } else {
                bluetoothEnableLauncher.launch(enableBtIntent)
            }
        } else {
            setupBluetooth()
        }
    }
      /**
     * Configura Bluetooth una vez confirmado que está disponible y activo
     */
    private fun setupBluetooth() {
        Log.d(TAG, "Configurando Bluetooth...")
        
        // Verificar permisos de nuevo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Permiso BLUETOOTH_CONNECT no concedido en setupBluetooth")
                return
            }
        }
        
        // Registrar BroadcastReceiver para descubrimiento de dispositivos
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        
        try {
            registerReceiver(receiver, filter)
            Log.d(TAG, "BroadcastReceiver registrado exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar BroadcastReceiver", e)
        }
        
        // Actualizar UI inicial
        updateConnectionStatus(getString(R.string.status_not_connected))
        scanButton.isEnabled = true
        hostButton.isEnabled = true
        
        Log.d(TAG, "Bluetooth configurado correctamente")
    }
      /**
     * Inicia el escaneo de dispositivos Bluetooth
     */
    private fun scanForDevices() {
        Log.d(TAG, "Iniciando escaneo de dispositivos Bluetooth...")
        
        // Limpiar lista anterior
        devicesList.clear()
        deviceListAdapter.clear()
        deviceListAdapter.notifyDataSetChanged()
        deviceListView.visibility = View.GONE
        
        // Cancelar cualquier descubrimiento en progreso
        if (::bluetoothAdapter.isInitialized) {
            try {
                if (bluetoothAdapter.isDiscovering) {
                    Log.d(TAG, "Cancelando descubrimiento anterior...")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                            bluetoothAdapter.cancelDiscovery()
                        }
                    } else {
                        bluetoothAdapter.cancelDiscovery()
                    }
                }
                
                // Verificar permisos antes de iniciar descubrimiento
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "Permiso BLUETOOTH_SCAN no concedido")
                        Toast.makeText(this, R.string.bluetooth_permission_required, Toast.LENGTH_SHORT).show()
                        return
                    }
                }
                
                // Verificar que Bluetooth esté habilitado
                if (!bluetoothAdapter.isEnabled) {
                    Log.e(TAG, "Bluetooth no está habilitado")
                    Toast.makeText(this, R.string.bluetooth_not_enabled, Toast.LENGTH_SHORT).show()
                    return
                }
                
                Log.d(TAG, "Iniciando startDiscovery()...")
                
                // Iniciar descubrimiento
                val discoveryStarted = bluetoothAdapter.startDiscovery()
                
                if (discoveryStarted) {
                    Log.d(TAG, "Descubrimiento iniciado exitosamente")
                    progressBar.visibility = View.VISIBLE
                    scanButton.isEnabled = false
                    scanButton.text = "Scanning..."
                    updateConnectionStatus(getString(R.string.status_scanning))                } else {
                    Log.e(TAG, "Error: startDiscovery() retornó false")
                    Toast.makeText(this, "Failed to start device discovery", Toast.LENGTH_SHORT).show()
                    updateConnectionStatus(getString(R.string.status_not_connected))
                }
                
            } catch (e: SecurityException) {
                Log.e(TAG, "Error de permisos durante el escaneo", e)
                Toast.makeText(this, R.string.bluetooth_permission_required, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "Error inesperado durante el escaneo", e)
                Toast.makeText(this, "Error starting Bluetooth discovery: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e(TAG, "bluetoothAdapter no está inicializado")
            Toast.makeText(this, "Bluetooth adapter not initialized", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Inicia el modo host (servidor)
     */
    private fun startHosting() {
        gameSyncManager.startServer()
        updateConnectionStatus(getString(R.string.status_listening))
        
        // Hacer que el dispositivo sea visible para otros
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.bluetooth_permission_required, Toast.LENGTH_SHORT).show()
                return
            }
        }
        
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300) // 5 minutos
        startActivity(discoverableIntent)
    }
    
    /**
     * Conecta con un dispositivo Bluetooth específico
     */
    private fun connectToDevice(device: BluetoothDevice) {
        updateConnectionStatus(getString(R.string.status_connecting))
        progressBar.visibility = View.VISIBLE
        
        // Cancelar cualquier descubrimiento para mejorar la conexión
        if (::bluetoothAdapter.isInitialized && bluetoothAdapter.isDiscovering) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
            }
            bluetoothAdapter.cancelDiscovery()
        }
        
        // Conectar al dispositivo
        gameSyncManager.getBluetoothManager().connect(device)
    }
    
    /**
     * Actualiza el estado de conexión en la UI
     */
    private fun updateConnectionStatus(status: String) {
        statusText.text = status
    }
    
    /**
     * Inicia la actividad de juego multijugador después de establecer conexión
     */
    private fun startMultiplayerGame(deviceName: String) {        // Pasar datos del juego multijugador a MultiplayerGameActivity
        val intent = Intent(this, MultiplayerGameActivity::class.java)
        intent.putExtra("deviceName", deviceName)
        intent.putExtra("gameMode", competitiveRadioButton.isChecked)
        startActivity(intent)
    }
    
    /**
     * Verifica los permisos necesarios para Bluetooth
     */
    private fun checkPermissions(): Boolean {
        val permissionsNeeded = REQUIRED_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        
        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded, REQUEST_PERMISSIONS)
            return false
        }
        
        return true
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Todos los permisos concedidos
                initializeBluetooth()
            } else {
                Toast.makeText(this, R.string.bluetooth_permission_required, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    
    /**
     * Implementaciones de GameSyncManager.OnConnectionEventListener
     */
    override fun onConnecting() {
        updateConnectionStatus(getString(R.string.status_connecting))
        progressBar.visibility = View.VISIBLE
    }
    
    override fun onConnected(deviceName: String) {
        updateConnectionStatus(getString(R.string.status_connected, deviceName))
        progressBar.visibility = View.GONE
        
        // Mostrar diálogo para iniciar el juego
        AlertDialog.Builder(this)
            .setTitle(R.string.connection_established)
            .setMessage(getString(R.string.start_multiplayer_game_question, deviceName))
            .setPositiveButton(R.string.btn_start_game) { _, _ ->
                startMultiplayerGame(deviceName)
            }
            .setNegativeButton(R.string.btn_cancel) { _, _ ->
                gameSyncManager.getBluetoothManager().stop()
                updateConnectionStatus(getString(R.string.status_not_connected))
            }
            .setCancelable(false)
            .create()
            .show()
    }
    
    override fun onDisconnected() {
        updateConnectionStatus(getString(R.string.status_not_connected))
        progressBar.visibility = View.GONE
    }
    
    override fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        progressBar.visibility = View.GONE
        updateConnectionStatus(getString(R.string.status_not_connected))
    }
    
    override fun onBluetoothPermissionRequired() {
        Toast.makeText(this, R.string.bluetooth_permission_required, Toast.LENGTH_SHORT).show()
        checkPermissions()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Cancelar descubrimiento si está activo
        if (::bluetoothAdapter.isInitialized && bluetoothAdapter.isDiscovering) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                    bluetoothAdapter.cancelDiscovery()
                }
            } else {
                bluetoothAdapter.cancelDiscovery()
            }
        }
        
        // Detener GameSyncManager
        if (::gameSyncManager.isInitialized) {
            gameSyncManager.stop()
        }
        
        // Desregistrar BroadcastReceiver
        try {
            unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            // Receptor ya desregistrado
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
