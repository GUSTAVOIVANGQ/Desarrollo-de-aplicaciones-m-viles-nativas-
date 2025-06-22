package com.example.exam3.bluetooth

import java.util.*

/**
 * Constantes para la comunicación Bluetooth
 */
object BluetoothConstants {
    // UUID para el servicio Bluetooth (debe ser el mismo en servidor y cliente)
    val SERVICE_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    
    // Nombre del servicio
    const val SERVICE_NAME = "BlueWebNavigator"
      // Tipos de mensajes
    const val MESSAGE_TYPE_REQUEST = "REQUEST"
    const val MESSAGE_TYPE_RESPONSE = "RESPONSE"
    const val MESSAGE_TYPE_WEB_CONTENT = "WEB_CONTENT"
    const val MESSAGE_TYPE_HELLO = "HELLO"
    const val MESSAGE_TYPE_ACK = "ACK"
    
    // Estados de conexión
    const val STATE_NONE = 0
    const val STATE_LISTENING = 1
    const val STATE_CONNECTING = 2
    const val STATE_CONNECTED = 3
    
    // Códigos de mensaje para el Handler
    const val MESSAGE_STATE_CHANGE = 1
    const val MESSAGE_READ = 2
    const val MESSAGE_WRITE = 3
    const val MESSAGE_DEVICE_NAME = 4
    const val MESSAGE_TOAST = 5
    
    // Claves para Bundle
    const val DEVICE_NAME = "device_name"
    const val TOAST = "toast"
}
