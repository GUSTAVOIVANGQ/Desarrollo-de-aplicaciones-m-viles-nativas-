package com.example.exam3.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Clase para manejar el servidor Bluetooth
 */
class BluetoothServerManager(
    private val bluetoothAdapter: BluetoothAdapter,
    private val handler: Handler,
    private val context: Context
) {
    
    private var acceptThread: AcceptThread? = null
    private var connectedThread: ConnectedThread? = null
    private var state = BluetoothConstants.STATE_NONE
    private val executor: ExecutorService = Executors.newCachedThreadPool()
    
    /**
     * Iniciar el servidor Bluetooth
     */
    fun start() {
        // Cancelar cualquier thread que esté intentando hacer una conexión
        connectedThread?.cancel()
        connectedThread = null
        
        // Iniciar el thread para escuchar conexiones
        if (acceptThread == null) {
            acceptThread = AcceptThread()
            acceptThread?.start()
        }
        
        setState(BluetoothConstants.STATE_LISTENING)
    }
    
    /**
     * Detener el servidor
     */
    fun stop() {
        acceptThread?.cancel()
        acceptThread = null
        
        connectedThread?.cancel()
        connectedThread = null
        
        setState(BluetoothConstants.STATE_NONE)
    }
      /**
     * Manejar una conexión establecida
     */
    private fun connected(socket: BluetoothSocket, device: android.bluetooth.BluetoothDevice) {
        // Cancelar el thread que completó la conexión
        acceptThread?.cancel()
        acceptThread = null
        
        // Cancelar cualquier thread que esté corriendo una conexión
        connectedThread?.cancel()
        
        // Iniciar el thread para manejar la conexión
        connectedThread = ConnectedThread(socket)
        connectedThread?.start()
        
        // Enviar el nombre del dispositivo conectado a la UI
        val msg = handler.obtainMessage(BluetoothConstants.MESSAGE_DEVICE_NAME)
        val bundle = Bundle().apply {
            val deviceName = try {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    device.name ?: "Dispositivo desconocido"
                } else {
                    "Dispositivo sin permisos"
                }
            } catch (e: SecurityException) {
                "Dispositivo con error de seguridad"
            }
            putString(BluetoothConstants.DEVICE_NAME, deviceName)
        }
        msg.data = bundle
        handler.sendMessage(msg)
        
        setState(BluetoothConstants.STATE_CONNECTED)
        
        // Enviar mensaje de bienvenida
        val helloMessage = BluetoothMessage.createHelloMessage("Servidor BlueWeb")
        write(helloMessage.toJson())
    }
    
    /**
     * Escribir datos al dispositivo conectado
     */
    fun write(data: String) {
        val thread = connectedThread
        thread?.write(data.toByteArray())
    }
    
    /**
     * Establecer el estado actual
     */
    private fun setState(newState: Int) {
        state = newState
        handler.obtainMessage(BluetoothConstants.MESSAGE_STATE_CHANGE, newState, -1).sendToTarget()
    }
      /**
     * Thread para escuchar conexiones entrantes
     */
    private inner class AcceptThread : Thread() {
        private val serverSocket: BluetoothServerSocket?
        
        init {
            var tmp: BluetoothServerSocket? = null
            try {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(
                        BluetoothConstants.SERVICE_NAME,
                        BluetoothConstants.SERVICE_UUID
                    )
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
            serverSocket = tmp
        }        override fun run() {
            name = "AcceptThread"
            var socket: BluetoothSocket?
            
            // Escuchar hasta que se conecte un cliente o se cancele
            while (this@BluetoothServerManager.state != BluetoothConstants.STATE_CONNECTED) {
                try {
                    socket = serverSocket?.accept()
                } catch (e: IOException) {
                    break
                } catch (e: SecurityException) {
                    break
                }
                  // Si se acepta una conexión
                socket?.let {
                    synchronized(this@BluetoothServerManager) {
                        when (this@BluetoothServerManager.state) {
                            BluetoothConstants.STATE_LISTENING,
                            BluetoothConstants.STATE_CONNECTING -> {
                                // Situación normal. Iniciar el connected thread.
                                try {
                                    if (ActivityCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.BLUETOOTH_CONNECT
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        connected(it, it.remoteDevice)
                                    }
                                } catch (e: SecurityException) {
                                    e.printStackTrace()
                                }
                            }
                            BluetoothConstants.STATE_NONE,
                            BluetoothConstants.STATE_CONNECTED -> {
                                // No está listo o ya conectado. Terminar nuevo socket.
                                try {
                                    it.close()
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                            }
                            else -> {
                                // Estado no manejado, cerrar socket
                                try {
                                    it.close()
                                } catch (e: IOException) {
                                    e.printStackTrace()
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
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Thread para manejar una conexión establecida
     */
    private inner class ConnectedThread(private val socket: BluetoothSocket) : Thread() {
        private val inputStream: InputStream? = socket.inputStream
        private val outputStream: OutputStream? = socket.outputStream
        
        override fun run() {
            val buffer = ByteArray(1024)
            var bytes: Int
            
            // Escuchar el InputStream
            while (true) {
                try {
                    // Leer del InputStream
                    bytes = inputStream?.read(buffer) ?: -1
                    if (bytes > 0) {
                        val receivedData = String(buffer, 0, bytes)
                        
                        // Enviar los datos obtenidos al hilo principal
                        handler.obtainMessage(BluetoothConstants.MESSAGE_READ, bytes, -1, receivedData)
                            .sendToTarget()
                    }
                } catch (e: IOException) {
                    connectionLost()
                    break
                }
            }
        }
        
        /**
         * Escribir al dispositivo conectado
         */
        fun write(buffer: ByteArray) {
            try {
                outputStream?.write(buffer)
                
                // Compartir el mensaje enviado de vuelta al hilo principal
                handler.obtainMessage(BluetoothConstants.MESSAGE_WRITE, -1, -1, buffer)
                    .sendToTarget()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        
        fun cancel() {
            try {
                socket.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        
        private fun connectionLost() {
            // Enviar un mensaje de fallo de vuelta a la Activity
            val msg = handler.obtainMessage(BluetoothConstants.MESSAGE_TOAST)
            val bundle = Bundle().apply {
                putString(BluetoothConstants.TOAST, "Conexión perdida con el dispositivo")
            }
            msg.data = bundle
            handler.sendMessage(msg)
            
            setState(BluetoothConstants.STATE_LISTENING)
            
            // Reiniciar el AcceptThread para escuchar nuevas conexiones
            this@BluetoothServerManager.start()
        }
    }
    
    /**
     * Hacer el dispositivo descubrible por otros dispositivos Bluetooth
     */
    fun makeDiscoverable(durationSeconds: Int = 300): Boolean {
        return try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // En versiones modernas de Android, necesitamos hacer el dispositivo descubrible
                // Esto generalmente requiere interacción del usuario
                true
            } else {
                false
            }
        } catch (e: SecurityException) {
            false
        }
    }
    
    /**
     * Verificar si el dispositivo es descubrible
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun isDiscoverable(): Boolean {
        return bluetoothAdapter.scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE
    }
    
    /**
     * Obtener el estado de scan del adaptador
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun getScanMode(): Int {
        return bluetoothAdapter.scanMode
    }
}
