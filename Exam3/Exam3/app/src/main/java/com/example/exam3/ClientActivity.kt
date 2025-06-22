package com.example.exam3

import android.Manifest
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
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.exam3.bluetooth.BluetoothClientManager
import com.example.exam3.bluetooth.BluetoothConstants
import com.example.exam3.bluetooth.BluetoothMessage
import com.example.exam3.web.WebContent
import com.example.exam3.web.WebService
import com.example.exam3.client.HistoryManager
import com.example.exam3.client.BookmarkManager
import com.example.exam3.client.LowPowerManager
import com.example.exam3.client.NotificationService
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClientActivity : AppCompatActivity() {
    
    // MODO SIMULACIÓN - Cambiar a false para usar Bluetooth real
    private val SIMULATION_MODE = true
    
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothClientManager: BluetoothClientManager
    private lateinit var tvClientStatus: TextView
    private lateinit var etUrl: EditText
    private lateinit var btnScan: MaterialButton
    private lateinit var btnConnect: MaterialButton
    private lateinit var btnDisconnect: MaterialButton
    private lateinit var btnGo: MaterialButton
    private lateinit var btnBack: MaterialButton
    private lateinit var btnForward: MaterialButton
    private lateinit var btnRefresh: MaterialButton
    private lateinit var btnBookmark: MaterialButton
    private lateinit var btnMenu: MaterialButton
    private lateinit var webView: WebView
    private lateinit var cardStatus: MaterialCardView
    private lateinit var cardNavigation: MaterialCardView
      // Nuevos managers para funcionalidades avanzadas
    private lateinit var historyManager: HistoryManager
    private lateinit var bookmarkManager: BookmarkManager
    private lateinit var lowPowerManager: LowPowerManager
    private lateinit var notificationService: NotificationService
    
    // WebService para modo simulación
    private lateinit var webService: WebService
    
    private var isConnected = false
    private var isConnecting = false
    private var isScanning = false
    private var connectedDeviceName: String? = null
    private var availableDevices: List<BluetoothDevice> = emptyList()
    private var currentUrl: String = ""
    
    // Handler para recibir mensajes del BluetoothClientManager
    private val bluetoothHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                BluetoothConstants.MESSAGE_STATE_CHANGE -> {
                    when (msg.arg1) {                BluetoothConstants.STATE_CONNECTED -> {
                    isConnected = true
                    isConnecting = false
                    updateUIState()
                    notificationService.showConnectionNotification(true, connectedDeviceName)
                }
                        BluetoothConstants.STATE_CONNECTING -> {
                            isConnecting = true
                            isConnected = false
                            updateUIState()
                        }                BluetoothConstants.STATE_NONE -> {
                    isConnected = false
                    isConnecting = false
                    connectedDeviceName = null
                    updateUIState()
                    notificationService.showConnectionNotification(false)
                }
                    }
                }
                BluetoothConstants.MESSAGE_READ -> {
                    val receivedData = msg.obj as String
                    handleReceivedMessage(receivedData)
                }
                BluetoothConstants.MESSAGE_WRITE -> {
                    val writtenData = msg.obj as ByteArray
                    // Log del mensaje enviado si es necesario
                }
                BluetoothConstants.MESSAGE_DEVICE_NAME -> {
                    connectedDeviceName = msg.data.getString(BluetoothConstants.DEVICE_NAME)
                    showToast("Conectado con $connectedDeviceName")
                }
                BluetoothConstants.MESSAGE_TOAST -> {
                    val toastMessage = msg.data.getString(BluetoothConstants.TOAST)
                    toastMessage?.let { showToast(it) }
                }
            }
        }
    }
    
    // Permisos necesarios para Bluetooth
    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
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
            showToast("Permisos otorgados correctamente")
            initializeBluetooth()
        } else {
            val deniedPermissions = permissions.filter { !it.value }.keys
            showToast("Permisos denegados: ${deniedPermissions.joinToString(", ")}")
            showToast("Se requieren todos los permisos para conectar por Bluetooth")
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
        
        // Aplicar tema del cliente (Azul)
        setTheme(R.style.Theme_BlueWeb_Client)
        
        setContentView(R.layout.activity_client)
        
        // Configurar window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets        }
          
        initializeViews()
        initializeManagers()
        setupWebView()
        setupClickListeners()
          // Mostrar indicador de modo simulación
        if (SIMULATION_MODE) {
            title = "BlueWeb Cliente (SIMULADO)"
            // Agregar URL de ejemplo
            etUrl.hint = "Ej: google.com, youtube.com, wikipedia.org"
        }
        
        checkPermissionsAndInitialize()
    }
    
    override fun onResume() {
        super.onResume()
        registerDiscoveryReceiver()
    }
    
    override fun onPause() {
        super.onPause()
        unregisterDiscoveryReceiver()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Detener discovery si está en progreso
        if (::bluetoothClientManager.isInitialized) {
            bluetoothClientManager.stopDiscovery()
            bluetoothClientManager.disconnect()
        }
        
        // Cancelar notificaciones
        if (::notificationService.isInitialized) {
            notificationService.cancelAllNotifications()
        }
    }
    
    private fun initializeViews() {
        tvClientStatus = findViewById(R.id.tv_client_status)
        etUrl = findViewById(R.id.et_url)
        btnScan = findViewById(R.id.btn_scan)
        btnConnect = findViewById(R.id.btn_connect)
        btnDisconnect = findViewById(R.id.btn_disconnect)
        btnGo = findViewById(R.id.btn_go)
        btnBack = findViewById(R.id.btn_back)
        btnForward = findViewById(R.id.btn_forward)
        btnRefresh = findViewById(R.id.btn_refresh)
        btnBookmark = findViewById(R.id.btn_bookmark)
        btnMenu = findViewById(R.id.btn_menu)
        webView = findViewById(R.id.webview)
        cardStatus = findViewById(R.id.card_status)
        cardNavigation = findViewById(R.id.card_navigation)
        
        updateUIState()
    }
      private fun initializeManagers() {
        historyManager = HistoryManager(this)
        bookmarkManager = BookmarkManager(this)
        lowPowerManager = LowPowerManager(this)
        notificationService = NotificationService(this)
        webService = WebService()
    }
      private fun setupWebView() {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (SIMULATION_MODE && url != null) {
                    // Obtener el título de la página
                    val title = view?.title ?: url
                    // Actualizar historial con el título real
                    historyManager.addToHistory(url, title)
                }
            }
            
            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                if (SIMULATION_MODE) {
                    showToast("❌ Error de conexión: $description")
                    notificationService.showErrorNotification("Error de conexión", description ?: "Error desconocido")
                }
            }
        }
        
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        
        // Configuraciones adicionales para modo simulación
        if (SIMULATION_MODE) {
            webView.settings.userAgentString = "BlueWeb-Client/1.0 (Simulation Mode)"
            webView.settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
        }
    }
    
    private fun setupClickListeners() {
        btnScan.setOnClickListener {
            scanForServers()
        }
        
        btnConnect.setOnClickListener {
            connectToServer()
        }
        
        btnDisconnect.setOnClickListener {
            disconnectFromServer()
        }
        
        btnGo.setOnClickListener {
            navigateToUrl()
        }
        
        btnBack.setOnClickListener {
            if (webView.canGoBack()) {
                webView.goBack()
            }
        }
        
        btnForward.setOnClickListener {
            if (webView.canGoForward()) {
                webView.goForward()
            }
        }
          btnRefresh.setOnClickListener {
            navigateToUrl()
        }
        
        btnBookmark.setOnClickListener {
            toggleBookmark()
        }
        
        btnMenu.setOnClickListener {
            showMenuDialog()
        }
    }
      private fun checkPermissionsAndInitialize() {
        if (SIMULATION_MODE) {
            // En modo simulación, saltar verificación de Bluetooth
            showToast("🔄 MODO SIMULACIÓN ACTIVADO")
            simulateSuccessfulConnection()
            return
        }
        
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isNotEmpty()) {
            requestPermissionsLauncher.launch(missingPermissions.toTypedArray())
        } else {
            initializeBluetooth()        }
    }
    
    /**
     * Simular conexión exitosa sin usar Bluetooth real
     */
    private fun simulateSuccessfulConnection() {
        // Simular que encontramos un servidor
        isConnected = true
        isConnecting = false
        connectedDeviceName = "Servidor"
        
        // Actualizar UI para mostrar conexión exitosa
        updateUIState()
        
        // Mostrar notificación de conexión
        notificationService.showConnectionNotification(true, connectedDeviceName)
        
        showToast("✅ Conectado exitosamente al servidor (SIMULADO)")
        showToast("🌐 Listo para navegar por la web")
    }
    
    /**
     * Simular búsqueda de servidores
     */
    private fun simulateServerScan() {
        if (isScanning) {
            isScanning = false
            updateScanButton()
            showToast("🔍 Búsqueda detenida")
            return
        }
        
        isScanning = true
        updateScanButton()
        showToast("🔍 Buscando servidores... (SIMULADO)")
        
        // Simular delay de búsqueda
        Handler(Looper.getMainLooper()).postDelayed({
            isScanning = false
            updateScanButton()
            
            // Simular que se encontró un servidor
            showToast("📡 Servidor encontrado: Dispositivo Simulado")
            
            // Mostrar opción de conexión automática
            AlertDialog.Builder(this)
                .setTitle("Servidor Encontrado")
                .setMessage("Se encontró un servidor BlueWeb.\n\n🔗 Dispositivo Simulado\n\n¿Desea conectarse?")
                .setPositiveButton("Conectar") { _, _ ->
                    simulateSuccessfulConnection()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }, 2000) // 2 segundos de delay
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
            // Inicializar el BluetoothClientManager
            bluetoothClientManager = BluetoothClientManager(bluetoothAdapter, bluetoothHandler, this)
        }
    }    /**
     * Buscar servidores disponibles
     */
    private fun scanForServers() {
        if (SIMULATION_MODE) {
            simulateServerScan()
            return
        }
        
        if (!::bluetoothAdapter.isInitialized || !bluetoothAdapter.isEnabled) {
            showToast("Bluetooth no está disponible")
            return
        }
        
        if (!::bluetoothClientManager.isInitialized) {
            bluetoothClientManager = BluetoothClientManager(bluetoothAdapter, bluetoothHandler, this)
        }
        
        if (isScanning) {
            // Detener scan si ya está en progreso
            bluetoothClientManager.stopDiscovery()
            return
        }
        
        // Verificar permisos antes de empezar
        checkAndLogPermissions()
        
        // Primero mostrar dispositivos emparejados
        val pairedDevices = bluetoothClientManager.getPairedDevices()
        if (!pairedDevices.isNullOrEmpty()) {
            availableDevices = pairedDevices.toList()
            showToast("Encontrados ${availableDevices.size} dispositivos emparejados")
            showDeviceSelectionDialog()
        } else {
            showToast("No hay dispositivos emparejados. Buscando dispositivos cercanos...")
            // Iniciar discovery para encontrar dispositivos cercanos
            if (bluetoothClientManager.startDiscovery()) {
                isScanning = true
                updateScanButton()
            } else {
                showToast("No se pudo iniciar la búsqueda. Verificar permisos.")
                checkAndLogPermissions()
            }
        }
    }
    
    /**
     * Actualizar la lista de dispositivos disponibles
     */
    private fun updateDevicesList() {
        val allDevices = bluetoothClientManager.getAllAvailableDevices()
        availableDevices = allDevices.toList()
    }
    
    /**
     * Actualizar el botón de scan según el estado
     */
    private fun updateScanButton() {
        btnScan.text = if (isScanning) "Detener Búsqueda" else "Buscar Servidores"
        btnScan.isEnabled = true
    }
    
    /**
     * Mostrar diálogo para seleccionar dispositivo servidor
     */
    private fun showDeviceSelectionDialog() {
        if (availableDevices.isEmpty()) {
            showToast("No se encontraron dispositivos. Asegúrate de que el servidor esté encendido y visible.")
            return
        }
        
        val deviceNames = availableDevices.map { device ->
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                    == PackageManager.PERMISSION_GRANTED) {
                    "${device.name ?: "Dispositivo desconocido"} (${device.address})"
                } else {
                    "Dispositivo (${device.address})"
                }
            } catch (e: SecurityException) {
                "Dispositivo (${device.address})"
            }
        }.toTypedArray()
        
        AlertDialog.Builder(this)
            .setTitle("Seleccionar Servidor")
            .setItems(deviceNames) { _, which ->
                val selectedDevice = availableDevices[which]
                connectToDevice(selectedDevice)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Conectar a un dispositivo específico
     */
    private fun connectToDevice(device: BluetoothDevice) {
        if (isConnecting || isConnected) {
            showToast("Ya existe una conexión o intento de conexión")
            return
        }
        
        try {
            val deviceName = if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                == PackageManager.PERMISSION_GRANTED) {
                device.name ?: "Dispositivo desconocido"
            } else {
                "Dispositivo seleccionado"
            }
            
            showToast("Conectando a $deviceName...")
            bluetoothClientManager.connect(device)
            
        } catch (e: SecurityException) {
            showToast("Error de permisos al conectar")
        }
    }
      /**
     * Método legacy para compatibilidad
     */
    private fun connectToServer() {
        if (SIMULATION_MODE) {
            if (isConnected) {
                showToast("Ya estás conectado al servidor")
                return
            }
            simulateSuccessfulConnection()
            return
        }
        
        if (availableDevices.isEmpty()) {
            scanForServers()
        } else {
            showDeviceSelectionDialog()
        }
    }
    
    private fun disconnectFromServer() {
        if (SIMULATION_MODE) {
            isConnected = false
            isConnecting = false
            connectedDeviceName = null
            updateUIState()
            notificationService.showConnectionNotification(false)
            showToast("🔌 Desconectado del servidor simulado")
            return
        }
        
        if (::bluetoothClientManager.isInitialized) {
            bluetoothClientManager.disconnect()
        }
        isConnected = false
        isConnecting = false
        connectedDeviceName = null
        updateUIState()
        showToast("Desconectado del servidor")
    }
    
    /**
     * Manejar mensajes recibidos del servidor
     */
    private fun handleReceivedMessage(data: String) {
        val message = BluetoothMessage.fromJson(data)
        if (message != null) {
            when (message.type) {
                BluetoothConstants.MESSAGE_TYPE_HELLO -> {
                    // Responder con ACK
                    val ackMessage = BluetoothMessage.createAckMessage("Cliente listo")
                    bluetoothClientManager.write(ackMessage.toJson())
                }                BluetoothConstants.MESSAGE_TYPE_RESPONSE -> {
                    // Respuesta HTML simple (compatibilidad hacia atrás)
                    webView.loadDataWithBaseURL(null, message.data, "text/html", "utf-8", null)
                    showToast("Página cargada")
                }
                BluetoothConstants.MESSAGE_TYPE_WEB_CONTENT -> {
                    // Respuesta WebContent completa
                    val webContent = WebContent.fromJson(message.data)
                    if (webContent != null) {
                        loadWebContent(webContent)
                    } else {
                        showToast("Error al procesar contenido web")
                    }
                }
                BluetoothConstants.MESSAGE_TYPE_ACK -> {
                    // ACK recibido del servidor
                    showToast("Servidor confirmó conexión")
                }
            }
        }
    }      private fun navigateToUrl() {
        val url = etUrl.text.toString().trim()
        if (url.isEmpty()) {
            showToast("Ingresa una URL")
            return
        }
        
        if (!isConnected) {
            showToast("Conecta al servidor primero")
            return
        }
        
        if (SIMULATION_MODE) {
            // En modo simulación, cargar la página directamente
            simulateWebNavigation(url)
            return
        }
          // Enviar solicitud de URL al servidor por Bluetooth (modo normal)
        val requestMessage = BluetoothMessage.createRequestMessage(url)
        bluetoothClientManager.write(requestMessage.toJson())
        showToast("Solicitando página al servidor...")
    }
      /**
     * Simular navegación web cargando la página directamente
     */
    private fun simulateWebNavigation(url: String) {
        var finalUrl = url
        
        // Normalizar URL (agregar https:// si no tiene protocolo)
        if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
            finalUrl = "https://$finalUrl"
        }
        
        currentUrl = finalUrl
        
        showToast("🔄 Solicitando página al servidor simulado...")
        
        // Usar corrutinas para simular la descarga
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Simular delay de red
                withContext(Dispatchers.IO) {
                    Thread.sleep(1000) // 1 segundo de delay
                }
                
                // Mostrar progreso
                showToast("📡 Recibiendo contenido del servidor...")
                
                // Agregar al historial
                historyManager.addToHistory(finalUrl, finalUrl)
                
                // Actualizar marcador si ya existe
                updateBookmarkButton()
                
                // Mostrar notificación de descarga exitosa
                notificationService.showDownloadNotification(finalUrl, true)
                
                // Cargar la página directamente en el WebView
                webView.loadUrl(finalUrl)
                
                // Simular mensaje de éxito del servidor
                Handler(Looper.getMainLooper()).postDelayed({
                    showToast("✅ Página cargada desde servidor BlueWeb")
                }, 2000)
                
            } catch (e: Exception) {
                showToast("❌ Error al cargar la página: ${e.message}")
                notificationService.showErrorNotification("Error de carga", "No se pudo cargar la página: $finalUrl")
            }
        }
    }
      private fun updateUIState() {
        when {
            isConnecting -> {
                tvClientStatus.text = getString(R.string.client_connecting)
                cardStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.warning))
                btnScan.isEnabled = false
                btnConnect.isEnabled = false
                btnDisconnect.isEnabled = true
                cardNavigation.alpha = 0.5f
                setNavigationButtonsEnabled(false)
            }
            isConnected -> {
                val statusText = if (connectedDeviceName != null) {
                    "Conectado con: $connectedDeviceName"
                } else {
                    getString(R.string.client_connected)
                }
                tvClientStatus.text = statusText
                cardStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.success))
                btnScan.isEnabled = false
                btnConnect.isEnabled = false
                btnDisconnect.isEnabled = true
                cardNavigation.alpha = 1.0f
                setNavigationButtonsEnabled(true)
            }
            else -> {
                tvClientStatus.text = getString(R.string.client_disconnected)
                cardStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.error))
                btnScan.isEnabled = true
                btnConnect.isEnabled = true
                btnDisconnect.isEnabled = false
                cardNavigation.alpha = 0.5f
                setNavigationButtonsEnabled(false)
            }
        }
    }
      private fun setNavigationButtonsEnabled(enabled: Boolean) {
        btnGo.isEnabled = enabled
        btnBack.isEnabled = enabled && webView.canGoBack()
        btnForward.isEnabled = enabled && webView.canGoForward()
        btnRefresh.isEnabled = enabled
        btnBookmark.isEnabled = enabled
        btnMenu.isEnabled = enabled
        
        if (enabled) {
            updateBookmarkButton()
        }
    }
      private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
      /**
     * Cargar WebContent en el WebView
     */
    private fun loadWebContent(webContent: WebContent) {
        try {
            // Obtener contenido descomprimido
            var htmlContent = webContent.getDecompressedContent()
            
            // Aplicar modo bajo consumo si está activado
            htmlContent = lowPowerManager.processHtmlForLowPower(htmlContent)
            
            // Cargar en WebView con la URL base para recursos relativos
            webView.loadDataWithBaseURL(
                webContent.url,
                htmlContent,
                webContent.mimeType,
                webContent.encoding,
                null
            )
            
            // Actualizar la URL actual y el campo de texto
            currentUrl = webContent.url
            etUrl.setText(webContent.url)
            
            // Agregar al historial
            val title = extractTitle(htmlContent) ?: "Página web"
            historyManager.addToHistory(webContent.url, title)
            
            // Actualizar estado del botón de marcador
            updateBookmarkButton()
            
            // Mostrar notificación de descarga exitosa
            notificationService.showDownloadNotification(webContent.url, webContent.statusCode == 200, webContent.statusCode)
            
            // Mostrar información del contenido
            val statusMessage = when {
                webContent.statusCode == 200 && webContent.compressed -> "Página cargada (comprimida)"
                webContent.statusCode == 200 && lowPowerManager.isLowPowerModeEnabled() -> "Página cargada (modo bajo consumo)"
                webContent.statusCode == 200 -> "Página cargada"
                else -> "Error ${webContent.statusCode}"
            }
            
            showToast(statusMessage)
            
        } catch (e: Exception) {
            showToast("Error al cargar contenido: ${e.message}")
            notificationService.showErrorNotification("Error de carga", "No se pudo cargar la página: ${e.message}")
        }
    }
    
    /**
     * Extraer título de HTML
     */
    private fun extractTitle(html: String): String? {
        return try {
            val titleRegex = Regex("<title[^>]*>([^<]+)</title>", RegexOption.IGNORE_CASE)
            val match = titleRegex.find(html)
            match?.groupValues?.get(1)?.trim()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Alternar marcador para la página actual
     */
    private fun toggleBookmark() {
        if (currentUrl.isEmpty()) {
            showToast("No hay página para marcar")
            return
        }
        
        val title = webView.title?.let { extractTitle(it) } ?: currentUrl
        
        if (bookmarkManager.isBookmarked(currentUrl)) {
            if (bookmarkManager.removeBookmark(currentUrl)) {
                showToast(getString(R.string.bookmark_removed))
                updateBookmarkButton()
            }
        } else {
            if (bookmarkManager.addBookmark(currentUrl, title)) {
                showToast(getString(R.string.bookmark_added))
                updateBookmarkButton()
            } else {
                showToast(getString(R.string.bookmark_exists))
            }
        }
    }
    
    /**
     * Actualizar el estado del botón de marcador
     */
    private fun updateBookmarkButton() {
        if (currentUrl.isNotEmpty() && bookmarkManager.isBookmarked(currentUrl)) {
            btnBookmark.text = "★"
            btnBookmark.setTextColor(ContextCompat.getColor(this, R.color.warning))
        } else {
            btnBookmark.text = "☆"
            btnBookmark.setTextColor(ContextCompat.getColor(this, R.color.azul_on_surface))
        }
    }
      /**
     * Mostrar menú de opciones
     */
    private fun showMenuDialog() {
        val options = if (SIMULATION_MODE) {
            arrayOf(
                "🌐 Páginas Populares",
                "📖 Marcadores",
                "📜 Historial", 
                "⚙️ Configuración",
                if (lowPowerManager.isLowPowerModeEnabled()) "🔋 Desactivar Modo Bajo Consumo" else "🔋 Activar Modo Bajo Consumo"
            )
        } else {
            arrayOf(
                "📖 Marcadores",
                "📜 Historial",
                "⚙️ Configuración",
                if (lowPowerManager.isLowPowerModeEnabled()) "🔋 Desactivar Modo Bajo Consumo" else "🔋 Activar Modo Bajo Consumo"
            )
        }
        
        AlertDialog.Builder(this)
            .setTitle("Menú BlueWeb")
            .setItems(options) { _, which ->
                if (SIMULATION_MODE) {
                    when (which) {
                        0 -> showPopularSitesDialog()
                        1 -> showBookmarksDialog()
                        2 -> showHistoryDialog()
                        3 -> showSettingsDialog()
                        4 -> toggleLowPowerMode()
                    }
                } else {
                    when (which) {
                        0 -> showBookmarksDialog()
                        1 -> showHistoryDialog()
                        2 -> showSettingsDialog()
                        3 -> toggleLowPowerMode()
                    }
                }            }
            .show()
    }
    
    /**
     * Mostrar diálogo con páginas populares para pruebas
     */
    private fun showPopularSitesDialog() {
        val popularSites = arrayOf(
            "🔍 Google - google.com",
            "📺 YouTube - youtube.com", 
            "📖 Wikipedia - wikipedia.org",
            "📰 BBC News - bbc.com",
            "🛒 Amazon - amazon.com",
            "🐦 Twitter - twitter.com",
            "💼 LinkedIn - linkedin.com",
            "📱 GitHub - github.com",
            "🌤️ Weather - weather.com",
            "📧 Gmail - gmail.com"
        )
        
        val urls = arrayOf(
            "google.com",
            "youtube.com",
            "wikipedia.org", 
            "bbc.com",
            "amazon.com",
            "twitter.com",
            "linkedin.com",
            "github.com",
            "weather.com",
            "gmail.com"
        )
        
        AlertDialog.Builder(this)
            .setTitle("🌐 Páginas Populares")
            .setItems(popularSites) { _, which ->
                etUrl.setText(urls[which])
                navigateToUrl()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Mostrar diálogo de marcadores
     */
    private fun showBookmarksDialog() {
        val bookmarks = bookmarkManager.getBookmarks()
        if (bookmarks.isEmpty()) {
            showToast(getString(R.string.no_bookmarks))
            return
        }
        
        val bookmarkTitles = bookmarks.map { "${it.title} (${it.url})" }.toTypedArray()
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.bookmarks_title))
            .setItems(bookmarkTitles) { _, which ->
                val selectedBookmark = bookmarks[which]
                etUrl.setText(selectedBookmark.url)
                navigateToUrl()
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }
    
    /**
     * Mostrar diálogo de historial
     */
    private fun showHistoryDialog() {
        val history = historyManager.getRecentHistory(20)
        if (history.isEmpty()) {
            showToast(getString(R.string.no_history))
            return
        }
        
        val historyTitles = history.map { "${it.title} (${it.url})" }.toTypedArray()
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.history_title))
            .setItems(historyTitles) { _, which ->
                val selectedHistory = history[which]
                etUrl.setText(selectedHistory.url)
                navigateToUrl()
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }
    
    /**
     * Mostrar diálogo de configuración
     */
    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.settings_title))
            .setMessage("Configuración actual:\n\n${lowPowerManager.getCurrentSettings()}\n\n${historyManager.getHistoryStats()}\n${bookmarkManager.getBookmarkStats()}")
            .setPositiveButton("Cerrar", null)
            .show()
    }
    
    /**
     * Alternar modo bajo consumo
     */
    private fun toggleLowPowerMode() {
        val newState = !lowPowerManager.isLowPowerModeEnabled()
        lowPowerManager.setLowPowerMode(newState)
        
        // Configurar opciones predeterminadas cuando se activa
        if (newState) {
            lowPowerManager.setRemoveImages(true)
            lowPowerManager.setRemoveVideos(true)
            lowPowerManager.setRemoveAds(true)
        }
        
        notificationService.showLowPowerModeNotification(newState)
        
        val message = if (newState) {
            getString(R.string.low_power_enabled)
        } else {
            getString(R.string.low_power_disabled)
        }
        showToast(message)
        
        // Recargar página actual si hay una cargada
        if (currentUrl.isNotEmpty()) {
            navigateToUrl()
        }
    }
      /**
     * Registrar BroadcastReceiver para el discovery de dispositivos Bluetooth
     */
    private fun registerDiscoveryReceiver() {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND).apply {
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        registerReceiver(discoveryReceiver, filter)
    }
    
    /**
     * Anular el registro del BroadcastReceiver
     */
    private fun unregisterDiscoveryReceiver() {
        try {
            unregisterReceiver(discoveryReceiver)
        } catch (e: Exception) {
            // Ignorar excepción si el receiver no estaba registrado
        }
    }
    
    // Receiver para manejar eventos de discovery de Bluetooth
    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Ignorar intents no válidos
            if (context == null || intent == null) return
            
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Un dispositivo fue encontrado
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        // Actualizar lista de dispositivos disponibles
                        updateDevicesList()
                        
                        // Mostrar notificación o actualización de UI si es necesario
                        if (isScanning) {
                            showToast("Dispositivo encontrado: ${it.name ?: "Desconocido"}")
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    // El discovery ha comenzado
                    isScanning = true
                    updateScanButton()
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    // El discovery ha terminado
                    isScanning = false
                    updateScanButton()
                }
            }
        }
    }
    
    /**
     * Verificar y mostrar el estado de todos los permisos Bluetooth
     */
    private fun checkAndLogPermissions() {
        showToast("=== Estado de Permisos ===")
        
        // Verificar permisos básicos
        val bluetoothPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
        val bluetoothAdminPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
        val locationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        
        showToast("BLUETOOTH: $bluetoothPermission")
        showToast("BLUETOOTH_ADMIN: $bluetoothAdminPermission") 
        showToast("ACCESS_FINE_LOCATION: $locationPermission")
        
        // Verificar permisos de Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val connectPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            val scanPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
            
            showToast("BLUETOOTH_CONNECT: $connectPermission")
            showToast("BLUETOOTH_SCAN: $scanPermission")
        }
        
        showToast("=========================")
    }
}
