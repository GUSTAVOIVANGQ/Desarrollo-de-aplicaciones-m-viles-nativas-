package com.example.puzzle.bluetooth

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.example.puzzle.models.GameBoard
import com.example.puzzle.models.GameState
import com.example.puzzle.models.TileState
import com.google.gson.Gson
import org.json.JSONObject

/**
 * Esta clase maneja la sincronización del estado del juego entre dos dispositivos en modo multijugador.
 */
class GameSyncManager(
    private val context: Context, 
    private var gameMode: Int = BluetoothManager.GAME_MODE_COOPERATIVE
) : BluetoothManager.BluetoothEventListener {
    companion object {
        private const val TAG = "GameSyncManager"
        
        // Tipos de mensajes que se pueden enviar
        private const val MSG_TYPE_MOVE = "move"
        private const val MSG_TYPE_GAME_STATE = "gameState"
        private const val MSG_TYPE_CHAT = "chat"
        private const val MSG_TYPE_READY = "ready"
        private const val MSG_TYPE_START = "start"
        private const val MSG_TYPE_GAME_OVER = "gameOver"
    }
    
    // Instancia de BluetoothManager
    private val bluetoothManager = BluetoothManager(context)
    private val gson = Gson()
    
    // Listeners para eventos del juego
    private var onGameEventListener: OnGameEventListener? = null
    private var onConnectionEventListener: OnConnectionEventListener? = null
    
    // Estado del juego remoto (para modo competitivo)
    private var remoteGameState: GameState? = null
    
    // Estados de la partida
    private var isRemotePlayerReady = false
    private var isLocalPlayerReady = false
    private var isGameStarted = false
    
    // Handler para procesar mensajes en el hilo principal
    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                BluetoothManager.MESSAGE_STATE_CHANGE -> {
                    when (msg.arg1) {
                        BluetoothManager.STATE_CONNECTED -> {
                            resetGameState()
                            onConnectionEventListener?.onConnected(msg.obj as String)
                        }
                        BluetoothManager.STATE_CONNECTING -> {
                            onConnectionEventListener?.onConnecting()
                        }
                        BluetoothManager.STATE_LISTEN, BluetoothManager.STATE_NONE -> {
                            onConnectionEventListener?.onDisconnected()
                        }
                    }
                }
                BluetoothManager.MESSAGE_DEVICE_NAME -> {
                    onConnectionEventListener?.onConnected(msg.obj as String)
                }
                BluetoothManager.MESSAGE_TOAST -> {
                    onConnectionEventListener?.onError(msg.obj as String)
                }
            }
        }
    }
    
    init {
        bluetoothManager.setListener(this)
    }
    
    /**
     * Obtiene el administrador de Bluetooth subyacente
     */
    fun getBluetoothManager(): BluetoothManager {
        return bluetoothManager
    }
    
    /**
     * Configura el listener para eventos del juego
     */
    fun setOnGameEventListener(listener: OnGameEventListener) {
        onGameEventListener = listener
    }
    
    /**
     * Configura el listener para eventos de conexión
     */
    fun setOnConnectionEventListener(listener: OnConnectionEventListener) {
        onConnectionEventListener = listener
    }
    
    /**
     * Configura el modo de juego
     */
    fun setGameMode(mode: Int) {
        gameMode = mode
        bluetoothManager.setGameMode(mode)
    }
    
    /**
     * Inicia el modo servidor (espera conexiones)
     */
    fun startServer() {
        resetGameState()
        bluetoothManager.startServer()
    }
    
    /**
     * Marca al jugador local como listo para empezar
     */
    fun setLocalPlayerReady(ready: Boolean) {
        isLocalPlayerReady = ready
        sendReadyStatus(ready)
    }
    
    /**
     * Envía el estado de listo al otro jugador
     */
    private fun sendReadyStatus(ready: Boolean) {
        val message = JSONObject().apply {
            put("type", MSG_TYPE_READY)
            put("ready", ready)
        }
        sendMessage(message.toString())
    }
    
    /**
     * Envía la señal para iniciar el juego
     */
    fun startGame(initialGameState: GameState) {
        isGameStarted = true
        
        val message = JSONObject().apply {
            put("type", MSG_TYPE_START)
            put("gameState", gson.toJson(initialGameState))
        }
        sendMessage(message.toString())
        
        // Notificar al listener que el juego ha comenzado
        onGameEventListener?.onGameStarted(initialGameState)
    }
    
    /**
     * Envía un mensaje de chat al otro jugador
     */
    fun sendChat(text: String) {
        val message = JSONObject().apply {
            put("type", MSG_TYPE_CHAT)
            put("text", text)
        }
        sendMessage(message.toString())
    }
    
    /**
     * Sincroniza un movimiento con el otro dispositivo
     */
    fun syncMove(row: Int, col: Int, moveResult: Boolean) {
        val message = JSONObject().apply {
            put("type", MSG_TYPE_MOVE)
            put("row", row)
            put("col", col)
            put("result", moveResult)
        }
        sendMessage(message.toString())
    }
    
    /**
     * Sincroniza el estado completo del juego con el otro dispositivo
     */
    fun syncGameState(gameState: GameState) {
        val message = JSONObject().apply {
            put("type", MSG_TYPE_GAME_STATE)
            put("state", gson.toJson(gameState))
        }
        sendMessage(message.toString())
    }
    
    /**
     * Envía una notificación de que el juego ha terminado
     */
    fun sendGameOver(win: Boolean, score: Int) {
        val message = JSONObject().apply {
            put("type", MSG_TYPE_GAME_OVER)
            put("win", win)
            put("score", score)
        }
        sendMessage(message.toString())
    }
    
    /**
     * Envía un mensaje a través de Bluetooth
     */
    private fun sendMessage(message: String) {
        if (bluetoothManager.isBluetoothEnabled()) {
            bluetoothManager.write(message.toByteArray())
        }
    }
    
    /**
     * Limpia el estado del juego
     */
    private fun resetGameState() {
        isRemotePlayerReady = false
        isLocalPlayerReady = false
        isGameStarted = false
        remoteGameState = null
    }
    
    /**
     * Detiene la sincronización y cierra la conexión
     */
    fun stop() {
        bluetoothManager.stop()
        resetGameState()
    }
    
    /**
     * Procesa los mensajes recibidos de Bluetooth
     */
    private fun processReceivedMessage(message: String) {
        try {
            val jsonMessage = JSONObject(message)
            when (jsonMessage.getString("type")) {
                MSG_TYPE_MOVE -> {
                    val row = jsonMessage.getInt("row")
                    val col = jsonMessage.getInt("col")
                    val result = jsonMessage.getBoolean("result")
                    onGameEventListener?.onRemoteMove(row, col, result)
                }
                MSG_TYPE_GAME_STATE -> {
                    val stateJson = jsonMessage.getString("state")
                    val gameState = gson.fromJson(stateJson, GameState::class.java)
                    remoteGameState = gameState
                    onGameEventListener?.onRemoteStateUpdated(gameState)
                }
                MSG_TYPE_CHAT -> {
                    val text = jsonMessage.getString("text")
                    onGameEventListener?.onChatMessageReceived(text)
                }
                MSG_TYPE_READY -> {
                    isRemotePlayerReady = jsonMessage.getBoolean("ready")
                    onGameEventListener?.onRemotePlayerReady(isRemotePlayerReady)
                    
                    // Si ambos jugadores están listos, notificar
                    if (isRemotePlayerReady && isLocalPlayerReady) {
                        onGameEventListener?.onBothPlayersReady()
                    }
                }
                MSG_TYPE_START -> {
                    isGameStarted = true
                    val stateJson = jsonMessage.getString("gameState")
                    val initialState = gson.fromJson(stateJson, GameState::class.java)
                    onGameEventListener?.onGameStarted(initialState)
                }
                MSG_TYPE_GAME_OVER -> {
                    val win = jsonMessage.getBoolean("win")
                    val score = jsonMessage.getInt("score")
                    onGameEventListener?.onRemoteGameOver(win, score)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al procesar mensaje recibido", e)
        }
    }
    
    // Implementación de BluetoothEventListener
    override fun onConnectionStateChanged(state: Int) {
        handler.obtainMessage(BluetoothManager.MESSAGE_STATE_CHANGE, state, -1).sendToTarget()
    }
    
    override fun onDeviceConnected(deviceName: String) {
        handler.obtainMessage(BluetoothManager.MESSAGE_DEVICE_NAME, deviceName).sendToTarget()
    }
    
    override fun onConnectionFailed() {
        onConnectionEventListener?.onError("Falló la conexión")
    }
    
    override fun onConnectionLost() {
        onConnectionEventListener?.onError("Conexión perdida")
    }
    
    override fun onMessageReceived(buffer: ByteArray) {
        val message = String(buffer)
        processReceivedMessage(message)
    }
    
    override fun onMessageSent(buffer: ByteArray) {
        // No necesitamos hacer nada cuando se envía un mensaje
    }
    
    override fun onBluetoothPermissionRequired() {
        onConnectionEventListener?.onBluetoothPermissionRequired()
    }
    
    /**
     * Interfaz para eventos de juego multijugador
     */
    interface OnGameEventListener {
        fun onRemoteMove(row: Int, col: Int, result: Boolean)
        fun onRemoteStateUpdated(gameState: GameState)
        fun onChatMessageReceived(text: String)
        fun onRemotePlayerReady(ready: Boolean)
        fun onBothPlayersReady()
        fun onGameStarted(initialState: GameState)
        fun onRemoteGameOver(win: Boolean, score: Int)
    }
    
    /**
     * Interfaz para eventos de conexión
     */
    interface OnConnectionEventListener {
        fun onConnecting()
        fun onConnected(deviceName: String)
        fun onDisconnected()
        fun onError(message: String)
        fun onBluetoothPermissionRequired()
    }
}
