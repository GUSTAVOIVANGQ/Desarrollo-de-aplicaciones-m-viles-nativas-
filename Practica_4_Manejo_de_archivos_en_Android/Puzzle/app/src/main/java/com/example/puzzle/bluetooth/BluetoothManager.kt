package com.example.puzzle.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

/**
 * Esta clase gestiona las comunicaciones Bluetooth para el modo multijugador.
 */
class BluetoothManager(private val context: Context) {

    companion object {
        private const val TAG = "BluetoothManager"
        private const val APP_NAME = "PuzzleGame"
        private val MY_UUID = UUID.fromString("8989063a-c9af-463a-b3f1-f21d9b2b827b")
        
        // Constantes para los estados de conexión
        const val STATE_NONE = 0
        const val STATE_LISTEN = 1
        const val STATE_CONNECTING = 2
        const val STATE_CONNECTED = 3
        
        // Constantes para los tipos de mensajes
        const val MESSAGE_STATE_CHANGE = 1
        const val MESSAGE_READ = 2
        const val MESSAGE_WRITE = 3
        const val MESSAGE_DEVICE_NAME = 4
        const val MESSAGE_TOAST = 5
        
        // Constantes para los tipos de juego
        const val GAME_MODE_COOPERATIVE = 0
        const val GAME_MODE_COMPETITIVE = 1
    }
    
    // BluetoothAdapter representa el adaptador Bluetooth del dispositivo
    private var bluetoothAdapter: BluetoothAdapter? = null
    
    // Estado actual de la conexión
    private var state = STATE_NONE
    
    // Hilos para las diferentes partes de la conexión
    private var serverThread: ServerThread? = null
    private var clientThread: ClientThread? = null
    private var connectedThread: ConnectedThread? = null
    
    // Listener para eventos de Bluetooth
    private var listener: BluetoothEventListener? = null
    
    // Modo de juego actual
    private var gameMode = GAME_MODE_COOPERATIVE
    
    init {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }
    
    /**
     * Establece el listener para eventos de Bluetooth
     */
    fun setListener(listener: BluetoothEventListener) {
        this.listener = listener
    }
    
    /**
     * Configura el modo de juego (cooperativo o competitivo)
     */
    fun setGameMode(mode: Int) {
        gameMode = mode
    }
    
    /**
     * Verifica si el dispositivo soporta Bluetooth
     */
    fun isBluetoothSupported(): Boolean {
        return bluetoothAdapter != null
    }
      /**
     * Verifica si el Bluetooth está habilitado
     */
    fun isBluetoothEnabled(): Boolean {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    listener?.onBluetoothPermissionRequired()
                    return false
                }
            }
            return bluetoothAdapter?.isEnabled == true
        } catch (e: SecurityException) {
            Log.e(TAG, "Error de permisos al verificar el estado de Bluetooth", e)
            listener?.onBluetoothPermissionRequired()
            return false
        }
    }
      /**
     * Inicia el modo servidor (espera conexiones)
     */
    fun startServer() {
        // Verificar permisos de Bluetooth antes de proceder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            listener?.onBluetoothPermissionRequired()
            return
        }
        
        // Cancelar cualquier hilo intentando establecer una conexión
        if (clientThread != null) {
            clientThread?.cancel()
            clientThread = null
        }
        
        // Cancelar cualquier hilo actualmente conectado
        if (connectedThread != null) {
            connectedThread?.cancel()
            connectedThread = null
        }
        
        // Iniciar el hilo de servidor
        if (serverThread == null) {
            serverThread = ServerThread()
            serverThread?.start()
        }
        
        setState(STATE_LISTEN)
    }
      /**
     * Conecta a un dispositivo como cliente
     */
    fun connect(device: BluetoothDevice) {
        // Verificar permisos de Bluetooth antes de proceder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            listener?.onBluetoothPermissionRequired()
            return
        }
        
        // Cancelar cualquier hilo intentando establecer una conexión
        if (state == STATE_CONNECTING) {
            if (clientThread != null) {
                clientThread?.cancel()
                clientThread = null
            }
        }
        
        // Cancelar cualquier hilo actualmente conectado
        if (connectedThread != null) {
            connectedThread?.cancel()
            connectedThread = null
        }
        
        // Iniciar el hilo para conectar con el dispositivo
        clientThread = ClientThread(device)
        clientThread?.start()
        
        setState(STATE_CONNECTING)
    }
    
    /**
     * Gestiona el estado de una conexión establecida
     */    @Synchronized
    fun connected(socket: BluetoothSocket, device: BluetoothDevice) {
        // Cancelar los hilos que completaron la conexión
        if (clientThread != null) {
            clientThread?.cancel()
            clientThread = null
        }
        
        // Cancelar el hilo actualmente conectado
        if (connectedThread != null) {
            connectedThread?.cancel()
            connectedThread = null
        }
        
        // Cancelar el hilo de aceptación
        if (serverThread != null) {
            serverThread?.cancel()
            serverThread = null
        }
        
        // Iniciar el hilo para manejar la conexión
        connectedThread = ConnectedThread(socket)
        connectedThread?.start()
        
        // Notificar al listener sobre el nombre del dispositivo conectado
        try {
            // Verificar permisos antes de acceder al nombre del dispositivo
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    val deviceName = device.name ?: "Dispositivo desconocido"
                    listener?.onDeviceConnected(deviceName)
                } else {
                    listener?.onDeviceConnected("Dispositivo desconocido")
                    listener?.onBluetoothPermissionRequired()
                }
            } else {
                val deviceName = device.name ?: "Dispositivo desconocido"
                listener?.onDeviceConnected(deviceName)
            }
        } catch (e: SecurityException) {
            listener?.onDeviceConnected("Dispositivo desconocido")
            Log.e(TAG, "Error de seguridad al acceder al nombre del dispositivo", e)
        }
        
        setState(STATE_CONNECTED)
    }
    
    /**
     * Detiene todos los hilos
     */
    @Synchronized
    fun stop() {
        if (clientThread != null) {
            clientThread?.cancel()
            clientThread = null
        }
        
        if (connectedThread != null) {
            connectedThread?.cancel()
            connectedThread = null
        }
        
        if (serverThread != null) {
            serverThread?.cancel()
            serverThread = null
        }
        
        setState(STATE_NONE)
    }
      /**
     * Escribe a los dispositivos conectados de forma asíncrona
     */
    fun write(out: ByteArray) {
        var connThread: ConnectedThread? = null
        
        // Sincronizar copia del ConnectedThread
        synchronized(this) {
            if (state != STATE_CONNECTED) return
            connThread = connectedThread
        }
        
        // Realizar escritura asíncrona
        connThread?.write(out)
    }
    
    /**
     * Obtiene el estado actual de la conexión
     */
    @Synchronized
    fun getState(): Int {
        return state
    }
    
    /**
     * Cambia el estado de la conexión y notifica al listener
     */
    @Synchronized
    private fun setState(state: Int) {
        this.state = state
        listener?.onConnectionStateChanged(state)
    }
    
    /**
     * Notifica una falla de conexión
     */
    private fun connectionFailed() {
        setState(STATE_LISTEN)
        listener?.onConnectionFailed()
    }
    
    /**
     * Notifica que la conexión fue perdida
     */
    private fun connectionLost() {
        setState(STATE_LISTEN)
        listener?.onConnectionLost()
    }
    
    /**
     * Hilo que escucha conexiones entrantes
     */    private inner class ServerThread : Thread() {
        private val serverSocket: BluetoothServerSocket?
        
        init {
            var tmp: BluetoothServerSocket? = null
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "Permiso BLUETOOTH_CONNECT no otorgado")
                        listener?.onBluetoothPermissionRequired()
                        tmp = null
                    } else {
                        tmp = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID)
                    }
                } else {
                    tmp = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID)
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error al crear ServerSocket", e)
            } catch (e: SecurityException) {
                Log.e(TAG, "Error de permisos al crear ServerSocket", e)
                listener?.onBluetoothPermissionRequired()
            }
            
            serverSocket = tmp
        }

        override fun run() {
            // Establecer el nombre del hilo
            currentThread().name = "ServerThread"
            var socket: BluetoothSocket?
            var continueRunning = true

            while (this@BluetoothManager.state != STATE_CONNECTED && continueRunning) {
                socket = null
                try {
                    socket = serverSocket?.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "Error al aceptar conexión", e)
                    continueRunning = false
                    continue
                } catch (e: SecurityException) {
                    Log.e(TAG, "Error de permisos al aceptar conexión", e)
                    listener?.onBluetoothPermissionRequired()
                    continueRunning = false
                    continue
                }

                // Si una conexión fue aceptada
                if (socket != null) {
                    synchronized(this@BluetoothManager) {
                        when (state) {
                            Thread.State.NEW, Thread.State.RUNNABLE -> {
                                // Situación normal, iniciar la conexión
                                var permissionDenied = false;
                                try {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                            listener?.onBluetoothPermissionRequired()
                                            socket.close()
                                            permissionDenied = true
                                        }
                                    }
                                    if (!permissionDenied) {
                                        connected(socket, socket.remoteDevice)
                                    } else {
                                        Log.e(TAG, "Permiso BLUETOOTH_CONNECT no otorgado")
                                        listener?.onBluetoothPermissionRequired()
                                        socket.close()
                                    }
                                }catch (e: SecurityException) {
                                    Log.e(TAG, "Error de permisos al conectar", e)
                                    listener?.onBluetoothPermissionRequired()
                                    try { socket.close() } catch (e: IOException) {}
                                }
                            }
                            Thread.State.TERMINATED -> {
                                // No está listo o ya está conectado. Cerrar el socket
                                try {
                                    socket.close()
                                } catch (e: IOException) {
                                    Log.e(TAG, "Error al cerrar un socket no deseado", e)
                                }
                            }
                            else -> {
                                // Estado no esperado. Cerrar el socket por seguridad
                                try {
                                    Log.w(TAG, "Estado inesperado: $state. Cerrando socket.")
                                    socket.close()
                                } catch (e: IOException) {
                                    Log.e(TAG, "Error al cerrar socket en estado inesperado", e)
                                }
                            }
                        }
                    }
                }
            }
        }

        fun cancel() {
            try {
                serverSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error al cerrar ServerSocket", e)
            }
        }
    }
    
    /**
     * Hilo que conecta con un dispositivo
     */    private inner class ClientThread(private val device: BluetoothDevice) : Thread() {
        private val socket: BluetoothSocket?
        
        init {
            var tmp: BluetoothSocket? = null
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "Permiso BLUETOOTH_CONNECT no otorgado")
                        listener?.onBluetoothPermissionRequired()
                        tmp = null
                    } else {
                        tmp = device.createRfcommSocketToServiceRecord(MY_UUID)
                    }
                } else {
                    tmp = device.createRfcommSocketToServiceRecord(MY_UUID)
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error al crear ClientSocket", e)
            } catch (e: SecurityException) {
                Log.e(TAG, "Error de permisos al crear ClientSocket", e)
                listener?.onBluetoothPermissionRequired()
            }
            
            socket = tmp
        }          override fun run() {
            // Establecer el nombre del hilo
            currentThread().name = "ClientThread"
            
            // Siempre cancelar descubrimiento porque ralentiza la conexión
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                        bluetoothAdapter?.cancelDiscovery()
                    } else {
                        // No podemos cancelar el descubrimiento, pero podemos continuar
                        Log.w(TAG, "No se puede cancelar el descubrimiento por falta de permiso BLUETOOTH_SCAN")
                    }
                } else {
                    bluetoothAdapter?.cancelDiscovery()
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "Error de permisos al cancelar descubrimiento", e)
            }
            
            // Verificar si el socket es null
            if (socket == null) {
                Log.e(TAG, "Socket es null, no se puede continuar con la conexión")
                connectionFailed()
                return
            }
            
            // Hacer una conexión al socket
            try {
                socket.connect()
            } catch (e: IOException) {
                // Cerrar el socket
                try {
                    socket.close()
                } catch (e2: IOException) {
                    Log.e(TAG, "Error al cerrar ClientSocket", e2)
                }
                connectionFailed()
                return
            } catch (e: SecurityException) {
                Log.e(TAG, "Error de permisos al conectar", e)
                try {
                    socket.close()
                } catch (e2: IOException) { }
                connectionFailed()
                listener?.onBluetoothPermissionRequired()
                return
            }
            
            // Reiniciar el hilo de conexión porque lo hemos terminado
            synchronized(this@BluetoothManager) {
                clientThread = null
            }
            
            // Iniciar el hilo conectado
            connected(socket, device)
        }
        
        fun cancel() {
            try {
                socket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error al cerrar ClientSocket", e)
            }
        }
    }
    
    /**
     * Hilo para manejar la conexión establecida
     */
    private inner class ConnectedThread(private val socket: BluetoothSocket) : Thread() {
        private val inputStream: InputStream?
        private val outputStream: OutputStream?
        
        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null
            
            try {
                tmpIn = socket.inputStream
                tmpOut = socket.outputStream
            } catch (e: IOException) {
                Log.e(TAG, "Error al crear streams de socket", e)
            }
            
            inputStream = tmpIn
            outputStream = tmpOut
        }
          override fun run() {
            val buffer = ByteArray(1024)
            var bytes: Int
            
            // Verificar que los streams existan antes de iniciar la escucha
            if (inputStream == null) {
                Log.e(TAG, "InputStream es nulo, no se puede iniciar la comunicación")
                connectionLost()
                return
            }
            
            // Mantener escuchando el InputStream
            while (true) {
                try {
                    // Leer del InputStream
                    bytes = inputStream.read(buffer)
                    if (bytes > 0) {
                        // Enviar los bytes obtenidos al listener
                        listener?.onMessageReceived(buffer.copyOf(bytes))
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Error durante lectura de socket", e)
                    connectionLost()
                    break
                } catch (e: SecurityException) {
                    Log.e(TAG, "Error de permisos durante lectura de socket", e)
                    listener?.onBluetoothPermissionRequired()
                    connectionLost()
                    break
                }
            }
        }
          /**
         * Escribe en el stream de salida conectado
         */
        fun write(buffer: ByteArray) {
            if (outputStream == null) {
                Log.e(TAG, "OutputStream es nulo, no se puede enviar el mensaje")
                return
            }
            
            try {
                outputStream.write(buffer)
                listener?.onMessageSent(buffer)
            } catch (e: IOException) {
                Log.e(TAG, "Error durante escritura de socket", e)
                connectionLost()
            } catch (e: SecurityException) {
                Log.e(TAG, "Error de permisos durante escritura de socket", e)
                listener?.onBluetoothPermissionRequired()
            }
        }
          fun cancel() {
            try {
                socket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error al cerrar socket conectado", e)
            } catch (e: SecurityException) {
                Log.e(TAG, "Error de permisos al cerrar socket conectado", e)
                listener?.onBluetoothPermissionRequired()
            }
        }
    }
    
    /**
     * Interfaz para escuchar eventos de Bluetooth
     */
    interface BluetoothEventListener {
        fun onConnectionStateChanged(state: Int)
        fun onDeviceConnected(deviceName: String)
        fun onConnectionFailed()
        fun onConnectionLost()
        fun onMessageReceived(buffer: ByteArray)
        fun onMessageSent(buffer: ByteArray)
        fun onBluetoothPermissionRequired()
    }
}
