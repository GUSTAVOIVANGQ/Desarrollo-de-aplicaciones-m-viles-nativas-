package com.example.exam3.web

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPOutputStream

/**
 * Servicio para descargar contenido web y gestionar el caché
 */
class WebService {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()
    
    private val cache = mutableMapOf<String, WebContent>()
    private val cacheExpirationTime = 5 * 60 * 1000L // 5 minutos
    
    companion object {
        private const val TAG = "WebService"
        private const val USER_AGENT = "BlueWeb-Server/1.0"
    }
    
    /**
     * Descargar contenido web desde una URL
     */
    suspend fun downloadWebContent(urlString: String): WebContent = withContext(Dispatchers.IO) {
        try {
            val normalizedUrl = normalizeUrl(urlString)
            Log.d(TAG, "Descargando: $normalizedUrl")
            
            // Verificar caché primero
            val cachedContent = getCachedContent(normalizedUrl)
            if (cachedContent != null) {
                Log.d(TAG, "Contenido encontrado en caché para: $normalizedUrl")
                return@withContext cachedContent
            }
            
            // Realizar la descarga
            val request = Request.Builder()
                .url(normalizedUrl)
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .addHeader("Accept-Language", "es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3")
                .addHeader("Accept-Encoding", "gzip, deflate")
                .addHeader("DNT", "1")
                .addHeader("Connection", "keep-alive")
                .build()
            
            client.newCall(request).execute().use { response ->
                val webContent = processResponse(normalizedUrl, response)
                
                // Guardar en caché si la descarga fue exitosa
                if (webContent.statusCode == 200) {
                    cacheContent(normalizedUrl, webContent)
                }
                
                return@withContext webContent
            }
            
        } catch (e: MalformedURLException) {
            Log.e(TAG, "URL malformada: $urlString", e)
            createErrorContent(urlString, 400, "URL malformada: ${e.message}")
        } catch (e: IOException) {
            Log.e(TAG, "Error de red al descargar: $urlString", e)
            createErrorContent(urlString, 500, "Error de conexión: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado al descargar: $urlString", e)
            createErrorContent(urlString, 500, "Error interno: ${e.message}")
        }
    }
    
    /**
     * Normalizar URL agregando protocolo si es necesario
     */
    private fun normalizeUrl(url: String): String {
        return when {
            url.startsWith("http://") || url.startsWith("https://") -> url
            url.startsWith("//") -> "https:$url"
            else -> "https://$url"
        }
    }
    
    /**
     * Verificar si hay contenido en caché válido
     */
    private fun getCachedContent(url: String): WebContent? {
        val cached = cache[url] ?: return null
        val currentTime = System.currentTimeMillis()
        
        return if (currentTime - cached.timestamp < cacheExpirationTime) {
            Log.d(TAG, "Caché válido para: $url")
            cached
        } else {
            Log.d(TAG, "Caché expirado para: $url")
            cache.remove(url)
            null
        }
    }
    
    /**
     * Guardar contenido en caché
     */
    private fun cacheContent(url: String, content: WebContent) {
        cache[url] = content
        Log.d(TAG, "Contenido guardado en caché para: $url (${cache.size} elementos en caché)")
    }
    
    /**
     * Procesar la respuesta HTTP
     */
    private fun processResponse(url: String, response: Response): WebContent {
        val statusCode = response.code
        val headers = response.headers.toMultimap().mapValues { it.value.joinToString("; ") }
        
        if (!response.isSuccessful) {
            return createErrorContent(url, statusCode, "HTTP $statusCode: ${response.message}")
        }
        
        val body = response.body ?: return createErrorContent(url, 500, "Respuesta vacía")
        val contentType = body.contentType()
        val mimeType = contentType?.toString() ?: "text/html"
        val charset = contentType?.charset()?.name() ?: "UTF-8"
        
        // Leer el contenido
        val htmlContent = body.string()
        
        // Crear contenido web comprimido
        val compressedHtml = compressContent(htmlContent)
        
        return WebContent(
            url = url,
            htmlContent = compressedHtml,
            mimeType = mimeType,
            encoding = charset,
            statusCode = statusCode,
            headers = headers,
            timestamp = System.currentTimeMillis(),
            compressed = true
        )
    }
    
    /**
     * Comprimir contenido usando GZIP
     */
    private fun compressContent(content: String): String {
        return try {
            val bytes = content.toByteArray(Charsets.UTF_8)
            val outputStream = ByteArrayOutputStream()
            
            GZIPOutputStream(outputStream).use { gzipStream ->
                gzipStream.write(bytes)
            }
            
            val compressedBytes = outputStream.toByteArray()
            val originalSize = bytes.size
            val compressedSize = compressedBytes.size
            val compressionRatio = ((originalSize - compressedSize) * 100.0 / originalSize)
            
            Log.d(TAG, "Compresión: $originalSize bytes -> $compressedSize bytes (${compressionRatio.toInt()}% reducción)")
            
            // Convertir a Base64 para transmisión segura
            android.util.Base64.encodeToString(compressedBytes, android.util.Base64.NO_WRAP)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al comprimir contenido", e)
            content // Devolver contenido original si falla la compresión
        }
    }
    
    /**
     * Crear contenido de error
     */
    private fun createErrorContent(url: String, statusCode: Int, errorMessage: String): WebContent {
        val errorHtml = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Error - BlueWeb</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        max-width: 800px;
                        margin: 0 auto;
                        padding: 20px;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        min-height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                    }
                    .error-container {
                        background: rgba(255, 255, 255, 0.1);
                        padding: 40px;
                        border-radius: 15px;
                        text-align: center;
                        backdrop-filter: blur(10px);
                        box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
                    }
                    h1 {
                        font-size: 4em;
                        margin: 0;
                        color: #ff6b6b;
                    }
                    h2 {
                        margin-top: 0;
                        color: #feca57;
                    }
                    .url {
                        background: rgba(0, 0, 0, 0.2);
                        padding: 10px;
                        border-radius: 5px;
                        font-family: monospace;
                        word-break: break-all;
                        margin: 20px 0;
                    }
                    .timestamp {
                        font-size: 0.9em;
                        opacity: 0.7;
                        margin-top: 20px;
                    }
                </style>
            </head>
            <body>
                <div class="error-container">
                    <h1>$statusCode</h1>
                    <h2>Error al cargar la página</h2>
                    <p><strong>URL:</strong></p>
                    <div class="url">$url</div>
                    <p><strong>Error:</strong> $errorMessage</p>
                    <div class="timestamp">
                        Generado el: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}
                    </div>
                    <p><em>Página servida por BlueWeb a través de Bluetooth</em></p>
                </div>
            </body>
            </html>
        """.trimIndent()
        
        return WebContent(
            url = url,
            htmlContent = errorHtml,
            mimeType = "text/html",
            encoding = "UTF-8",
            statusCode = statusCode,
            headers = mapOf("Content-Type" to "text/html; charset=UTF-8"),
            timestamp = System.currentTimeMillis(),
            compressed = false
        )
    }
    
    /**
     * Limpiar caché
     */
    fun clearCache() {
        cache.clear()
        Log.d(TAG, "Caché limpiado")
    }
    
    /**
     * Obtener estadísticas del caché
     */
    fun getCacheStats(): String {
        return "Elementos en caché: ${cache.size}"
    }
}
