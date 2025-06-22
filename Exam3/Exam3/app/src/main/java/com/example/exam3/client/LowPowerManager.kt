package com.example.exam3.client

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Clase para manejar el modo bajo consumo
 */
class LowPowerManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("blueweb_settings", Context.MODE_PRIVATE)
    
    companion object {
        private const val TAG = "LowPowerManager"
        private const val LOW_POWER_MODE_KEY = "low_power_mode"
        private const val REMOVE_IMAGES_KEY = "remove_images"
        private const val REMOVE_VIDEOS_KEY = "remove_videos"
        private const val REMOVE_ADS_KEY = "remove_ads"
        private const val COMPRESS_TEXT_KEY = "compress_text"
    }
    
    /**
     * Verificar si el modo bajo consumo está activado
     */
    fun isLowPowerModeEnabled(): Boolean {
        return prefs.getBoolean(LOW_POWER_MODE_KEY, false)
    }
    
    /**
     * Activar/desactivar modo bajo consumo
     */
    fun setLowPowerMode(enabled: Boolean) {
        prefs.edit().putBoolean(LOW_POWER_MODE_KEY, enabled).apply()
        Log.d(TAG, "Modo bajo consumo: ${if (enabled) "activado" else "desactivado"}")
    }
    
    /**
     * Configurar opciones específicas del modo bajo consumo
     */
    fun setRemoveImages(remove: Boolean) {
        prefs.edit().putBoolean(REMOVE_IMAGES_KEY, remove).apply()
    }
    
    fun setRemoveVideos(remove: Boolean) {
        prefs.edit().putBoolean(REMOVE_VIDEOS_KEY, remove).apply()
    }
    
    fun setRemoveAds(remove: Boolean) {
        prefs.edit().putBoolean(REMOVE_ADS_KEY, remove).apply()
    }
    
    fun setCompressText(compress: Boolean) {
        prefs.edit().putBoolean(COMPRESS_TEXT_KEY, compress).apply()
    }
    
    /**
     * Obtener configuraciones del modo bajo consumo
     */
    fun shouldRemoveImages(): Boolean {
        return isLowPowerModeEnabled() && prefs.getBoolean(REMOVE_IMAGES_KEY, true)
    }
    
    fun shouldRemoveVideos(): Boolean {
        return isLowPowerModeEnabled() && prefs.getBoolean(REMOVE_VIDEOS_KEY, true)
    }
    
    fun shouldRemoveAds(): Boolean {
        return isLowPowerModeEnabled() && prefs.getBoolean(REMOVE_ADS_KEY, true)
    }
    
    fun shouldCompressText(): Boolean {
        return isLowPowerModeEnabled() && prefs.getBoolean(COMPRESS_TEXT_KEY, false)
    }
    
    /**
     * Procesar HTML para modo bajo consumo
     */
    fun processHtmlForLowPower(html: String): String {
        if (!isLowPowerModeEnabled()) {
            return html
        }
        
        var processedHtml = html
        
        try {
            // Remover imágenes
            if (shouldRemoveImages()) {
                processedHtml = removeImages(processedHtml)
            }
            
            // Remover videos
            if (shouldRemoveVideos()) {
                processedHtml = removeVideos(processedHtml)
            }
            
            // Remover anuncios comunes
            if (shouldRemoveAds()) {
                processedHtml = removeAds(processedHtml)
            }
            
            // Comprimir texto (remover espacios extra)
            if (shouldCompressText()) {
                processedHtml = compressText(processedHtml)
            }
            
            // Agregar estilo de modo bajo consumo
            processedHtml = addLowPowerStyles(processedHtml)
            
            val originalSize = html.length
            val processedSize = processedHtml.length
            val reduction = ((originalSize - processedSize) * 100 / originalSize)
            
            Log.d(TAG, "HTML procesado para modo bajo consumo: ${originalSize}B -> ${processedSize}B (${reduction}% reducción)")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error procesando HTML para modo bajo consumo", e)
            return html // Devolver HTML original si hay error
        }
        
        return processedHtml
    }
    
    /**
     * Remover imágenes del HTML
     */
    private fun removeImages(html: String): String {
        return html
            .replace(Regex("<img[^>]*>", RegexOption.IGNORE_CASE), 
                "<div style='border:1px dashed #ccc; padding:10px; text-align:center; color:#666; font-size:12px;'>📷 Imagen removida (modo bajo consumo)</div>")
            .replace(Regex("<picture[^>]*>.*?</picture>", RegexOption.IGNORE_CASE), 
                "<div style='border:1px dashed #ccc; padding:10px; text-align:center; color:#666; font-size:12px;'>🖼️ Imagen removida (modo bajo consumo)</div>")
    }
    
    /**
     * Remover videos del HTML
     */
    private fun removeVideos(html: String): String {
        return html
            .replace(Regex("<video[^>]*>.*?</video>", RegexOption.IGNORE_CASE), 
                "<div style='border:1px dashed #ccc; padding:10px; text-align:center; color:#666; font-size:12px;'>🎥 Video removido (modo bajo consumo)</div>")
            .replace(Regex("<iframe[^>]*youtube[^>]*>.*?</iframe>", RegexOption.IGNORE_CASE), 
                "<div style='border:1px dashed #ccc; padding:10px; text-align:center; color:#666; font-size:12px;'>📺 Video de YouTube removido (modo bajo consumo)</div>")
            .replace(Regex("<embed[^>]*>", RegexOption.IGNORE_CASE), 
                "<div style='border:1px dashed #ccc; padding:10px; text-align:center; color:#666; font-size:12px;'>🎬 Contenido multimedia removido (modo bajo consumo)</div>")
    }
    
    /**
     * Remover anuncios comunes
     */
    private fun removeAds(html: String): String {
        return html
            .replace(Regex("<div[^>]*class=['\"][^'\"]*ad[^'\"]*['\"][^>]*>.*?</div>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<div[^>]*id=['\"][^'\"]*ad[^'\"]*['\"][^>]*>.*?</div>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<script[^>]*googletag[^>]*>.*?</script>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<script[^>]*doubleclick[^>]*>.*?</script>", RegexOption.IGNORE_CASE), "")
    }
    
    /**
     * Comprimir texto removiendo espacios extra
     */
    private fun compressText(html: String): String {
        return html
            .replace(Regex("\\s+"), " ")
            .replace(Regex(">\\s+<"), "><")
            .trim()
    }
    
    /**
     * Agregar estilos CSS para modo bajo consumo
     */
    private fun addLowPowerStyles(html: String): String {
        val lowPowerStyles = """
            <style>
            /* Estilos para modo bajo consumo */
            body {
                font-family: Arial, sans-serif !important;
                line-height: 1.4 !important;
                color: #333 !important;
                background: #f9f9f9 !important;
                margin: 0 !important;
                padding: 10px !important;
            }
            * {
                box-shadow: none !important;
                text-shadow: none !important;
                border-radius: 3px !important;
            }
            .blueweb-low-power-banner {
                background: #4CAF50;
                color: white;
                padding: 8px;
                text-align: center;
                font-size: 12px;
                position: fixed;
                top: 0;
                left: 0;
                right: 0;
                z-index: 9999;
                box-shadow: 0 2px 4px rgba(0,0,0,0.2);
            }
            body {
                padding-top: 40px !important;
            }
            </style>
        """.trimIndent()
        
        val banner = """
            <div class="blueweb-low-power-banner">
                🔋 Modo Bajo Consumo Activado - Contenido optimizado para Bluetooth
            </div>
        """.trimIndent()
        
        // Insertar estilos en el head o al principio del body
        return if (html.contains("<head>", ignoreCase = true)) {
            html.replace(Regex("<head>", RegexOption.IGNORE_CASE), "<head>$lowPowerStyles")
                .replace(Regex("<body[^>]*>", RegexOption.IGNORE_CASE)) { matchResult ->
                    "${matchResult.value}$banner"
                }
        } else {
            "$lowPowerStyles$banner$html"
        }
    }
    
    /**
     * Obtener configuración actual como string
     */
    fun getCurrentSettings(): String {
        return if (isLowPowerModeEnabled()) {
            val settings = mutableListOf<String>()
            if (shouldRemoveImages()) settings.add("Sin imágenes")
            if (shouldRemoveVideos()) settings.add("Sin videos")
            if (shouldRemoveAds()) settings.add("Sin anuncios")
            if (shouldCompressText()) settings.add("Texto comprimido")
            
            "Modo bajo consumo: ${settings.joinToString(", ")}"
        } else {
            "Modo normal"
        }
    }
}
