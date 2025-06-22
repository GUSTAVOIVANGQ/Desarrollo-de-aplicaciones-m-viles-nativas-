package com.example.exam3.client

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Clase para manejar el historial de navegación
 */
data class HistoryItem(
    val url: String,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val favicon: String? = null
)

class HistoryManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("blueweb_history", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val maxHistoryItems = 100
    
    companion object {
        private const val HISTORY_KEY = "history_items"
    }
    
    /**
     * Agregar un elemento al historial
     */
    fun addToHistory(url: String, title: String) {
        val currentHistory = getHistory().toMutableList()
        
        // Remover elemento existente si ya está en el historial
        currentHistory.removeAll { it.url == url }
        
        // Agregar nuevo elemento al principio
        currentHistory.add(0, HistoryItem(url, title))
        
        // Limitar el tamaño del historial
        if (currentHistory.size > maxHistoryItems) {
            currentHistory.removeAt(currentHistory.size - 1)
        }
        
        // Guardar historial actualizado
        saveHistory(currentHistory)
    }
    
    /**
     * Obtener el historial completo
     */
    fun getHistory(): List<HistoryItem> {
        return try {
            val historyJson = prefs.getString(HISTORY_KEY, "[]") ?: "[]"
            val type = object : TypeToken<List<HistoryItem>>() {}.type
            gson.fromJson(historyJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Obtener historial reciente (últimos N elementos)
     */
    fun getRecentHistory(limit: Int = 10): List<HistoryItem> {
        return getHistory().take(limit)
    }
    
    /**
     * Buscar en el historial
     */
    fun searchHistory(query: String): List<HistoryItem> {
        val searchQuery = query.lowercase()
        return getHistory().filter {
            it.url.lowercase().contains(searchQuery) || 
            it.title.lowercase().contains(searchQuery)
        }
    }
    
    /**
     * Eliminar un elemento del historial
     */
    fun removeFromHistory(url: String) {
        val currentHistory = getHistory().toMutableList()
        currentHistory.removeAll { it.url == url }
        saveHistory(currentHistory)
    }
    
    /**
     * Limpiar todo el historial
     */
    fun clearHistory() {
        prefs.edit().remove(HISTORY_KEY).apply()
    }
    
    /**
     * Guardar historial en SharedPreferences
     */
    private fun saveHistory(history: List<HistoryItem>) {
        val historyJson = gson.toJson(history)
        prefs.edit().putString(HISTORY_KEY, historyJson).apply()
    }
    
    /**
     * Obtener estadísticas del historial
     */
    fun getHistoryStats(): String {
        val history = getHistory()
        val totalItems = history.size
        val uniqueDomains = history.map { 
            try {
                java.net.URL(it.url).host
            } catch (e: Exception) {
                "desconocido"
            }
        }.distinct().size
        
        return "Historial: $totalItems páginas, $uniqueDomains dominios"
    }
}
