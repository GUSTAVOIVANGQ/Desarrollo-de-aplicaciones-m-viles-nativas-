package com.example.exam3.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Clase para manejar el cliente Bluetooth
 */
class BluetoothClientManager(
    private val bluetoothAdapter: BluetoothAdapter,
    private val handler: Handler,
    private val context: Context
) {
    
    private var connectThread: ConnectThread? = null
    private var connectedThread: ConnectedThread? = null
    private var state = BluetoothConstants.STATE_NONE
    private val executor: ExecutorService = Executors.newCachedThreadPool()
    
    // Lista de dispositivos descubiertos
    private val discoveredDevices = mutableSetOf<BluetoothDevice>()
    
    /**
     * Conectar a un dispositivo específico
     */
    fun connect(device: BluetoothDevice) {
        // Cancelar cualquier thread que esté intentando hacer una conexión
        if (state == BluetoothConstants.STATE_CONNECTING) {
            connectThread?.cancel()
            connectThread = null
        }
        
        // Cancelar cualquier thread que esté corriendo una conexión
        connectedThread?.cancel()
        connectedThread = null
        
        // Iniciar el thread para conectar con el dispositivo dado
        connectThread = ConnectThread(device)
        connectThread?.start()
        
        setState(BluetoothConstants.STATE_CONNECTING)
    }
    
    /**
     * Desconectar del dispositivo
     */
    fun disconnect() {
        connectThread?.cancel()
        connectThread = null
        
        connectedThread?.cancel()
        connectedThread = null
        
        setState(BluetoothConstants.STATE_NONE)
    }
      /**
     * Manejar una conexión establecida
     */
    private fun connected(socket: BluetoothSocket, device: BluetoothDevice) {
        // Cancelar el thread que completó la conexión
        connectThread?.cancel()
        connectThread = null
        
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
        
        // Enviar mensaje de saludo
        val helloMessage = BluetoothMessage.createHelloMessage("Cliente BlueWeb")
        write(helloMessage.toJson())
    }
    
    /**
     * Escribir datos al dispositivo conectado
     */
    fun write(data: String) {
        val thread = connectedThread
        thread?.write(data.toByteArray())
    }    /**
     * Obtener lista de dispositivos emparejados que podrían ser servidores
     */
    fun getPairedDevices(): Set<BluetoothDevice>? {
        return try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                bluetoothAdapter.bondedDevices
            } else {
                null
            }
        } catch (e: SecurityException) {
            null
        }
    }
    
    /**
     * Iniciar discovery de dispositivos Bluetooth
     */
    fun startDiscovery(): Boolean {
        return try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Limpiar la lista de dispositivos descubiertos
                discoveredDevices.clear()
                
                // Cancelar discovery anterior si existe
                if (bluetoothAdapter.isDiscovering) {
                    bluetoothAdapter.cancelDiscovery()
                }
                
                // Iniciar discovery
                bluetoothAdapter.startDiscovery()
            } else {
                false
            }
        } catch (e: SecurityException) {
            false
        }
    }
    
    /**
     * Detener discovery de dispositivos
     */
    fun stopDiscovery(): Boolean {
        return try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                bluetoothAdapter.cancelDiscovery()
            } else {
                false
            }
        } catch (e: SecurityException) {
            false
        }
    }
    
    /**
     * Agregar dispositivo descubierto a la lista
     */
    fun addDiscoveredDevice(device: BluetoothDevice) {
        discoveredDevices.add(device)
    }
    
    /**
     * Obtener lista de dispositivos descubiertos
     */
    fun getDiscoveredDevices(): Set<BluetoothDevice> {
        return discoveredDevices.toSet()
    }
    
    /**
     * Obtener lista combinada de dispositivos (emparejados + descubiertos)
     */
    fun getAllAvailableDevices(): Set<BluetoothDevice> {
        val allDevices = mutableSetOf<BluetoothDevice>()
        
        // Agregar dispositivos emparejados
        val pairedDevices = getPairedDevices()
        pairedDevices?.let { allDevices.addAll(it) }
        
        // Agregar dispositivos descubiertos
        allDevices.addAll(discoveredDevices)
        
        return allDevices
    }
    
    /**
     * Establecer el estado actual
     */
    private fun setState(newState: Int) {
        state = newState
        handler.obtainMessage(BluetoothConstants.MESSAGE_STATE_CHANGE, newState, -1).sendToTarget()
    }
    
    /**
     * Obtener el estado actual
     */
    fun getState(): Int = state
      /**
     * Thread para conectar a un dispositivo
     */
    private inner class ConnectThread(private val device: BluetoothDevice) : Thread() {
        private val socket: BluetoothSocket?
        
        init {
            var tmp: BluetoothSocket? = null
            try {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Método estándar
                    tmp = device.createRfcommSocketToServiceRecord(BluetoothConstants.SERVICE_UUID)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                // Intentar método alternativo si falla el estándar
                tmp = createAlternativeSocket(device)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
            socket = tmp
        }
        
        /**
         * Crear socket alternativo usando reflexión (método de respaldo)
         */
        private fun createAlternativeSocket(device: BluetoothDevice): BluetoothSocket? {
            return try {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Usar reflexión para acceder al método createRfcommSocket
                    val method = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                    method.invoke(device, 1) as BluetoothSocket
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
          
        override fun run() {
            name = "ConnectThread"
            
            // Cancelar el discovery porque ralentiza la conexión
            try {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    bluetoothAdapter.cancelDiscovery()
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
            
            try {
                // Conectar al socket. Esto es una llamada bloqueante
                socket?.connect()
            } catch (e: IOException) {
                // No se pudo conectar; cerrar el socket e informar el fallo
                try {
                    socket?.close()
                } catch (e2: IOException) {
                    e2.printStackTrace()
                }
                connectionFailed()
                return
            } catch (e: SecurityException) {
                connectionFailed()
                return
            }
            
            // Reset del ConnectThread porque hemos terminado
            synchronized(this@BluetoothClientManager) {
                connectThread = null
            }
            
            // Iniciar el connected thread
            socket?.let { connected(it, device) }
        }
        
        fun cancel() {
            try {
                socket?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        
        private fun connectionFailed() {
            // Enviar un mensaje de fallo de vuelta a la Activity
            val msg = handler.obtainMessage(BluetoothConstants.MESSAGE_TOAST)
            val bundle = Bundle().apply {
                putString(BluetoothConstants.TOAST, "No se pudo conectar al dispositivo")
            }
            msg.data = bundle
            handler.sendMessage(msg)
            
            setState(BluetoothConstants.STATE_NONE)
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
            
            setState(BluetoothConstants.STATE_NONE)
        }
    }
}
