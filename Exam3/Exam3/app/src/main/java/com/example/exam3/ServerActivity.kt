package com.example.exam3

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.exam3.bluetooth.BluetoothConstants
import com.example.exam3.bluetooth.BluetoothMessage
import com.example.exam3.bluetooth.BluetoothServerManager
import com.example.exam3.web.WebContent
import com.example.exam3.web.WebService
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class ServerActivity : AppCompatActivity() {
      private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothServerManager: BluetoothServerManager
    private lateinit var webService: WebService
    private lateinit var tvServerStatus: TextView
    private lateinit var tvServerLogs: TextView
    private lateinit var btnStartServer: MaterialButton
    private lateinit var btnStopServer: MaterialButton
    private lateinit var btnClearLogs: MaterialButton
    private lateinit var btnClearCache: MaterialButton
    private lateinit var cardStatus: MaterialCardView
    
    private var isServerRunning = false
    private var connectedDeviceName: String? = null
    
    // Handler para recibir mensajes del BluetoothServerManager
    private val bluetoothHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                BluetoothConstants.MESSAGE_STATE_CHANGE -> {
                    when (msg.arg1) {
                        BluetoothConstants.STATE_CONNECTED -> {
                            logMessage("Cliente conectado exitosamente")
                            updateUIState()
                        }
                        BluetoothConstants.STATE_CONNECTING -> {
                            logMessage("Conectando con cliente...")
                        }
                        BluetoothConstants.STATE_LISTENING -> {
                            logMessage("Escuchando conexiones...")
                        }
                        BluetoothConstants.STATE_NONE -> {
                            logMessage("No conectado")
                            connectedDeviceName = null
                            updateUIState()
                        }
                    }
                }
                BluetoothConstants.MESSAGE_READ -> {
                    val receivedData = msg.obj as String
                    handleReceivedMessage(receivedData)
                }
                BluetoothConstants.MESSAGE_WRITE -> {
                    val writtenData = msg.obj as ByteArray
                    logMessage("Enviado: ${String(writtenData)}")
                }
                BluetoothConstants.MESSAGE_DEVICE_NAME -> {
                    connectedDeviceName = msg.data.getString(BluetoothConstants.DEVICE_NAME)
                    logMessage("Conectado con: $connectedDeviceName")
                    showToast("Conectado con $connectedDeviceName")
                }
                BluetoothConstants.MESSAGE_TOAST -> {
                    val toastMessage = msg.data.getString(BluetoothConstants.TOAST)
                    toastMessage?.let { 
                        showToast(it)
                        logMessage("Error: $it")
                    }
                }
            }
        }
    }
      // Permisos necesarios para Bluetooth
    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
      private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            logMessage("Permisos otorgados correctamente")
            initializeBluetooth()
            // Si ya tenemos el servidor corriendo, intentar hacer descubrible
            if (isServerRunning) {
                makeDeviceDiscoverable()
            }
        } else {
            val deniedPermissions = permissions.filter { !it.value }.keys
            logMessage("Permisos denegados: ${deniedPermissions.joinToString(", ")}")
            showToast("Se requieren todos los permisos para funcionar correctamente")
        }
    }
    
    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            initializeBluetooth()
        } else {
            showToast(getString(R.string.bluetooth_not_enabled))
            finish()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Aplicar tema del servidor (Guinda)
        setTheme(R.style.Theme_BlueWeb_Server)
        
        setContentView(R.layout.activity_server)
        
        // Configurar window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
          initializeViews()
        initializeWebService()
        setupClickListeners()
        checkPermissionsAndInitialize()
    }
      private fun initializeViews() {
        tvServerStatus = findViewById(R.id.tv_server_status)
        tvServerLogs = findViewById(R.id.tv_server_logs)
        btnStartServer = findViewById(R.id.btn_start_server)
        btnStopServer = findViewById(R.id.btn_stop_server)
        btnClearLogs = findViewById(R.id.btn_clear_logs)
        btnClearCache = findViewById(R.id.btn_clear_cache)
        cardStatus = findViewById(R.id.card_status)
        
        updateUIState()
    }
    
    private fun initializeWebService() {
        webService = WebService()
        logMessage("Servicio web inicializado")
    }
    
    private fun setupClickListeners() {
        btnStartServer.setOnClickListener {
            startBluetoothServer()
        }
          btnStopServer.setOnClickListener {
            stopBluetoothServer()
        }
          btnClearLogs.setOnClickListener {
            tvServerLogs.text = ""
            logMessage("Logs limpiados")
        }
        
        btnClearCache.setOnClickListener {
            webService.clearCache()
            logMessage("Caché limpiado")
            showToast("Caché limpiado")
        }
    }
    
    private fun checkPermissionsAndInitialize() {
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isNotEmpty()) {
            requestPermissionsLauncher.launch(missingPermissions.toTypedArray())
        } else {
            initializeBluetooth()
        }
    }
    
    private fun initializeBluetooth() {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        
        if (!::bluetoothAdapter.isInitialized) {
            showToast(getString(R.string.bluetooth_not_supported))
            finish()
            return
        }
          if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                == PackageManager.PERMISSION_GRANTED) {
                enableBluetoothLauncher.launch(enableBtIntent)
            }        } else {
            // Inicializar el BluetoothServerManager
            bluetoothServerManager = BluetoothServerManager(bluetoothAdapter, bluetoothHandler, this)
            logMessage("Bluetooth inicializado correctamente")
        }
    }
      /**
     * Iniciar el servidor Bluetooth
     */
    private fun startBluetoothServer() {
        if (!::bluetoothAdapter.isInitialized || !bluetoothAdapter.isEnabled) {
            showToast("Bluetooth no está disponible")
            return
        }
          
        if (!::bluetoothServerManager.isInitialized) {
            bluetoothServerManager = BluetoothServerManager(bluetoothAdapter, bluetoothHandler, this)
        }
        
        // Verificar estado de permisos
        checkAndLogPermissions()
        
        // Hacer el dispositivo descubrible
        makeDeviceDiscoverable()
        
        // Iniciar el servidor
        bluetoothServerManager.start()
        isServerRunning = true
        updateUIState()
        logMessage("Servidor Bluetooth iniciado")
        logMessage("Esperando conexiones de clientes...")
        showToast("Servidor iniciado - Esperando conexiones")
    }    /**
     * Hacer el dispositivo descubrible para que los clientes lo puedan encontrar
     */
    private fun makeDeviceDiscoverable() {
        try {
            // Intentar hacer el dispositivo descubrible directamente
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300) // 5 minutos
            }
            
            // Verificar si tenemos permisos en Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val hasAdvertisePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED
                val hasConnectPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                
                if (!hasAdvertisePermission || !hasConnectPermission) {
                    logMessage("Permisos insuficientes para hacer el dispositivo descubrible")
                    logMessage("BLUETOOTH_ADVERTISE: $hasAdvertisePermission, BLUETOOTH_CONNECT: $hasConnectPermission")
                    
                    // Solicitar permisos faltantes
                    val missingPermissions = mutableListOf<String>()
                    if (!hasAdvertisePermission) missingPermissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
                    if (!hasConnectPermission) missingPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
                    
                    logMessage("Solicitando permisos: ${missingPermissions.joinToString(", ")}")
                    requestPermissionsLauncher.launch(missingPermissions.toTypedArray())
                    return
                }
            }
            
            // Intentar iniciar la actividad para hacer descubrible
            logMessage("Solicitando hacer el dispositivo descubrible por 300 segundos...")
            startActivityForResult(discoverableIntent, REQUEST_ENABLE_DISCOVERABLE)
            
        } catch (e: SecurityException) {
            logMessage("Error de seguridad al hacer el dispositivo descubrible: ${e.message}")
            // Como alternativa, informar al usuario que debe hacer esto manualmente
            showDiscoverableInstructions()
        } catch (e: Exception) {
            logMessage("Error inesperado al hacer el dispositivo descubrible: ${e.message}")
            showDiscoverableInstructions()
        }
    }
    
    /**
     * Mostrar instrucciones para hacer el dispositivo descubrible manualmente
     */
    private fun showDiscoverableInstructions() {
        val instructions = """
            Para que el cliente pueda conectar, necesitas hacer este dispositivo visible:
            
            1. Ve a Configuración → Bluetooth
            2. Toca en el nombre de tu dispositivo
            3. Activa "Visible para otros dispositivos" o "Descubrible"
            4. O mantén presionado el icono de Bluetooth en configuración rápida
            
            El servidor ya está escuchando conexiones.
        """.trimIndent()
        
        logMessage("INSTRUCCIONES MANUALES:")
        logMessage(instructions)
        
        AlertDialog.Builder(this)
            .setTitle("Hacer dispositivo visible")
            .setMessage(instructions)
            .setPositiveButton("Entendido") { _, _ ->
                logMessage("Usuario confirmó las instrucciones")
            }
            .setNegativeButton("Ir a Configuración") { _, _ ->
                try {
                    val settingsIntent = Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)
                    startActivity(settingsIntent)
                } catch (e: Exception) {
                    logMessage("No se pudo abrir configuración de Bluetooth")
                }
            }
            .show()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        when (requestCode) {
            REQUEST_ENABLE_DISCOVERABLE -> {
                if (resultCode != RESULT_CANCELED) {
                    logMessage("Dispositivo configurado como descubrible por $resultCode segundos")
                    showToast("Dispositivo visible para clientes por $resultCode segundos")
                } else {
                    logMessage("Usuario canceló la configuración de descubrible")
                    showToast("Nota: El dispositivo podría no ser visible para otros clientes")
                }
            }
        }
    }
    
    companion object {
        private const val REQUEST_ENABLE_DISCOVERABLE = 1001
    }
    
    /**
     * Detener el servidor Bluetooth
     */
    private fun stopBluetoothServer() {
        if (::bluetoothServerManager.isInitialized) {
            bluetoothServerManager.stop()
        }
        isServerRunning = false
        connectedDeviceName = null
        updateUIState()
        logMessage("Servidor Bluetooth detenido")
        showToast("Servidor detenido")
    }
    
    /**
     * Manejar mensajes recibidos del cliente
     */
    private fun handleReceivedMessage(data: String) {
        logMessage("Recibido: $data")
        
        val message = BluetoothMessage.fromJson(data)
        if (message != null) {
            when (message.type) {
                BluetoothConstants.MESSAGE_TYPE_HELLO -> {
                    logMessage("Saludo recibido de: ${message.data}")
                    // Responder con ACK
                    val ackMessage = BluetoothMessage.createAckMessage("Servidor listo")
                    bluetoothServerManager.write(ackMessage.toJson())
                }                BluetoothConstants.MESSAGE_TYPE_REQUEST -> {
                    val requestedUrl = message.data
                    logMessage("Solicitud de URL: $requestedUrl")
                    
                    // Descargar contenido web de forma asíncrona
                    lifecycleScope.launch {
                        try {
                            logMessage("Descargando: $requestedUrl...")
                            val webContent = webService.downloadWebContent(requestedUrl)
                            
                            logMessage("Descarga completada: ${webContent.statusCode} - ${webContent.getSummary()}")
                            
                            // Enviar respuesta completa con WebContent
                            val responseMessage = BluetoothMessage.createWebContentResponseMessage(webContent)
                            bluetoothServerManager.write(responseMessage.toJson())
                            
                            logMessage("WebContent enviado al cliente (${webContent.url})")
                            logMessage("Caché: ${webService.getCacheStats()}")
                            
                        } catch (e: Exception) {
                            logMessage("Error al procesar solicitud: ${e.message}")
                            
                            // Enviar respuesta de error
                            val errorHtml = createErrorHtml(requestedUrl, "Error del servidor: ${e.message}")
                            val errorMessage = BluetoothMessage.createResponseMessage(errorHtml)
                            bluetoothServerManager.write(errorMessage.toJson())
                        }
                    }
                }
                BluetoothConstants.MESSAGE_TYPE_ACK -> {
                    logMessage("ACK recibido: ${message.data}")
                }
            }
        } else {
            logMessage("Mensaje no válido recibido: $data")
        }
    }
      private fun updateUIState() {
        if (isServerRunning) {
            if (connectedDeviceName != null) {
                tvServerStatus.text = "Conectado con: $connectedDeviceName"
                cardStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.success))
            } else {
                tvServerStatus.text = getString(R.string.server_waiting)
                cardStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.info))
            }
            btnStartServer.isEnabled = false
            btnStopServer.isEnabled = true
        } else {
            tvServerStatus.text = "Servidor detenido"
            btnStartServer.isEnabled = true
            btnStopServer.isEnabled = false
            cardStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.warning))
        }
    }
    
    private fun logMessage(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        val logEntry = "[$timestamp] $message\n"
        tvServerLogs.append(logEntry)
    }
      private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Crear HTML de error simple para casos de emergencia
     */
    private fun createErrorHtml(url: String, errorMessage: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Error - BlueWeb</title>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; padding: 20px; }
                    .error { color: red; border: 1px solid red; padding: 10px; }
                </style>
            </head>
            <body>
                <h1>Error del Servidor</h1>
                <div class="error">
                    <p><strong>URL:</strong> $url</p>
                    <p><strong>Error:</strong> $errorMessage</p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
      /**
     * Verificar y mostrar el estado de todos los permisos Bluetooth
     */
    private fun checkAndLogPermissions() {
        logMessage("=== Estado de Permisos ===")
        
        // Verificar permisos básicos
        val bluetoothPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
        val bluetoothAdminPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
        val locationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        
        logMessage("BLUETOOTH: $bluetoothPermission")
        logMessage("BLUETOOTH_ADMIN: $bluetoothAdminPermission")
        logMessage("ACCESS_FINE_LOCATION: $locationPermission")
        
        // Verificar permisos de Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val connectPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            val advertisePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED
            val scanPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
            
            logMessage("BLUETOOTH_CONNECT: $connectPermission")
            logMessage("BLUETOOTH_ADVERTISE: $advertisePermission")
            logMessage("BLUETOOTH_SCAN: $scanPermission")
        }
        
        // Estado del adaptador Bluetooth
        if (::bluetoothAdapter.isInitialized) {
            logMessage("Bluetooth habilitado: ${bluetoothAdapter.isEnabled}")
            if (bluetoothAdapter.isEnabled) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                        val scanMode = bluetoothAdapter.scanMode
                        logMessage("Modo de escaneo: ${getScanModeString(scanMode)}")
                    }
                } catch (e: SecurityException) {
                    logMessage("No se puede verificar el modo de escaneo: ${e.message}")
                }
            }
        }
        
        logMessage("=========================")
    }
    
    /**
     * Convertir el modo de escaneo a string legible
     */
    private fun getScanModeString(scanMode: Int): String {
        return when (scanMode) {
            BluetoothAdapter.SCAN_MODE_NONE -> "No descubrible"
            BluetoothAdapter.SCAN_MODE_CONNECTABLE -> "Conectable pero no descubrible"
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE -> "Conectable y descubrible"
            else -> "Modo desconocido ($scanMode)"
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (isServerRunning) {
            stopBluetoothServer()
        }
    }
}
