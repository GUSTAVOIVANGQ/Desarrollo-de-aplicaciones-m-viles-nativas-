package com.example.exam3.web

import android.util.Log
import com.google.gson.Gson
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream

/**
 * Clase para representar el contenido web que se intercambia
 */
data class WebContent(
    val url: String,
    val htmlContent: String,
    val mimeType: String = "text/html",
    val encoding: String = "UTF-8",
    val statusCode: Int = 200,
    val headers: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis(),
    val compressed: Boolean = false
) {
    
    companion object {
        private val gson = Gson()
        private const val TAG = "WebContent"
        
        /**
         * Convertir a JSON
         */
        fun toJson(webContent: WebContent): String {
            return gson.toJson(webContent)
        }
        
        /**
         * Convertir desde JSON
         */
        fun fromJson(json: String): WebContent? {
            return try {
                gson.fromJson(json, WebContent::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Error al deserializar WebContent", e)
                null
            }
        }
    }
    
    /**
     * Convertir a JSON
     */
    fun toJson(): String = toJson(this)
    
    /**
     * Obtener el contenido HTML descomprimido
     */
    fun getDecompressedContent(): String {
        return if (compressed) {
            try {
                decompressContent(htmlContent)
            } catch (e: Exception) {
                Log.e(TAG, "Error al descomprimir contenido", e)
                htmlContent // Devolver contenido original si falla
            }
        } else {
            htmlContent
        }
    }
    
    /**
     * Descomprimir contenido GZIP desde Base64
     */
    private fun decompressContent(compressedBase64: String): String {
        return try {
            // Decodificar desde Base64
            val compressedBytes = android.util.Base64.decode(compressedBase64, android.util.Base64.NO_WRAP)
            
            // Descomprimir GZIP
            val inputStream = ByteArrayInputStream(compressedBytes)
            val outputStream = ByteArrayOutputStream()
            
            GZIPInputStream(inputStream).use { gzipStream ->
                val buffer = ByteArray(1024)
                var length: Int
                while (gzipStream.read(buffer).also { length = it } != -1) {
                    outputStream.write(buffer, 0, length)
                }
            }
            
            val decompressedBytes = outputStream.toByteArray()
            val result = String(decompressedBytes, Charsets.UTF_8)
            
            Log.d(TAG, "Descompresión exitosa: ${compressedBytes.size} bytes -> ${decompressedBytes.size} bytes")
            result
            
        } catch (e: Exception) {
            Log.e(TAG, "Error en descompresión", e)
            compressedBase64 // Devolver contenido original si falla
        }
    }
    
    /**
     * Obtener información resumida del contenido
     */
    fun getSummary(): String {
        val sizeInfo = if (compressed) {
            "comprimido"
        } else {
            "${htmlContent.length} chars"
        }
        
        return "WebContent(url=$url, status=$statusCode, type=$mimeType, size=$sizeInfo)"
    }
}
