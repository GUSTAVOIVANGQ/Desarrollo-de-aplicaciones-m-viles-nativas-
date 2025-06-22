package com.example.exam3.bluetooth

import com.google.gson.Gson

/**
 * Clase para representar mensajes intercambiados entre servidor y cliente
 */
data class BluetoothMessage(
    val type: String,
    val data: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    
    companion object {
        private val gson = Gson()
        
        /**
         * Crear un mensaje de saludo
         */
        fun createHelloMessage(deviceName: String): BluetoothMessage {
            return BluetoothMessage(
                type = BluetoothConstants.MESSAGE_TYPE_HELLO,
                data = deviceName
            )
        }
        
        /**
         * Crear un mensaje de acknowledgment
         */
        fun createAckMessage(message: String = "OK"): BluetoothMessage {
            return BluetoothMessage(
                type = BluetoothConstants.MESSAGE_TYPE_ACK,
                data = message
            )
        }
        
        /**
         * Crear un mensaje de solicitud de URL
         */
        fun createRequestMessage(url: String): BluetoothMessage {
            return BluetoothMessage(
                type = BluetoothConstants.MESSAGE_TYPE_REQUEST,
                data = url
            )
        }
          /**
         * Crear un mensaje de respuesta con HTML
         */
        fun createResponseMessage(htmlContent: String): BluetoothMessage {
            return BluetoothMessage(
                type = BluetoothConstants.MESSAGE_TYPE_RESPONSE,
                data = htmlContent
            )
        }
        
        /**
         * Crear un mensaje de respuesta con WebContent completo
         */
        fun createWebContentResponseMessage(webContent: com.example.exam3.web.WebContent): BluetoothMessage {
            return BluetoothMessage(
                type = BluetoothConstants.MESSAGE_TYPE_WEB_CONTENT,
                data = webContent.toJson()
            )
        }
        
        /**
         * Convertir el mensaje a JSON para envío
         */
        fun toJson(message: BluetoothMessage): String {
            return gson.toJson(message)
        }
        
        /**
         * Convertir JSON a mensaje
         */
        fun fromJson(json: String): BluetoothMessage? {
            return try {
                gson.fromJson(json, BluetoothMessage::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Convertir a JSON
     */
    fun toJson(): String = toJson(this)
}
