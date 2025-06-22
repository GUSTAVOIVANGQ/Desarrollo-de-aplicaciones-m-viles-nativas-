package com.example.exam3.client

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Clase para representar un marcador
 */
data class Bookmark(
    val url: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val folder: String = "Predeterminado",
    val description: String = ""
)

class BookmarkManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("blueweb_bookmarks", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val BOOKMARKS_KEY = "bookmarks"
    }
    
    /**
     * Agregar un marcador
     */
    fun addBookmark(url: String, title: String, folder: String = "Predeterminado", description: String = ""): Boolean {
        val currentBookmarks = getBookmarks().toMutableList()
        
        // Verificar si ya existe
        if (currentBookmarks.any { it.url == url }) {
            return false // Ya existe
        }
        
        // Agregar nuevo marcador
        currentBookmarks.add(Bookmark(url, title, System.currentTimeMillis(), folder, description))
        
        // Guardar
        saveBookmarks(currentBookmarks)
        return true
    }
    
    /**
     * Obtener todos los marcadores
     */
    fun getBookmarks(): List<Bookmark> {
        return try {
            val bookmarksJson = prefs.getString(BOOKMARKS_KEY, "[]") ?: "[]"
            val type = object : TypeToken<List<Bookmark>>() {}.type
            gson.fromJson(bookmarksJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Obtener marcadores por carpeta
     */
    fun getBookmarksByFolder(folder: String): List<Bookmark> {
        return getBookmarks().filter { it.folder == folder }
    }
    
    /**
     * Obtener todas las carpetas
     */
    fun getFolders(): List<String> {
        return getBookmarks().map { it.folder }.distinct().sorted()
    }
    
    /**
     * Verificar si una URL está marcada
     */
    fun isBookmarked(url: String): Boolean {
        return getBookmarks().any { it.url == url }
    }
    
    /**
     * Remover un marcador
     */
    fun removeBookmark(url: String): Boolean {
        val currentBookmarks = getBookmarks().toMutableList()
        val sizeBefore = currentBookmarks.size
        currentBookmarks.removeAll { it.url == url }
        
        if (currentBookmarks.size < sizeBefore) {
            saveBookmarks(currentBookmarks)
            return true
        }
        return false
    }
    
    /**
     * Buscar marcadores
     */
    fun searchBookmarks(query: String): List<Bookmark> {
        val searchQuery = query.lowercase()
        return getBookmarks().filter {
            it.url.lowercase().contains(searchQuery) ||
            it.title.lowercase().contains(searchQuery) ||
            it.description.lowercase().contains(searchQuery)
        }
    }
    
    /**
     * Actualizar un marcador
     */
    fun updateBookmark(url: String, newTitle: String? = null, newFolder: String? = null, newDescription: String? = null): Boolean {
        val currentBookmarks = getBookmarks().toMutableList()
        val index = currentBookmarks.indexOfFirst { it.url == url }
        
        if (index >= 0) {
            val bookmark = currentBookmarks[index]
            currentBookmarks[index] = bookmark.copy(
                title = newTitle ?: bookmark.title,
                folder = newFolder ?: bookmark.folder,
                description = newDescription ?: bookmark.description
            )
            saveBookmarks(currentBookmarks)
            return true
        }
        return false
    }
    
    /**
     * Limpiar todos los marcadores
     */
    fun clearBookmarks() {
        prefs.edit().remove(BOOKMARKS_KEY).apply()
    }
    
    /**
     * Exportar marcadores como JSON
     */
    fun exportBookmarks(): String {
        return gson.toJson(getBookmarks())
    }
    
    /**
     * Importar marcadores desde JSON
     */
    fun importBookmarks(json: String): Boolean {
        return try {
            val type = object : TypeToken<List<Bookmark>>() {}.type
            val importedBookmarks: List<Bookmark> = gson.fromJson(json, type)
            val currentBookmarks = getBookmarks().toMutableList()
            
            // Agregar marcadores que no existan
            var addedCount = 0
            importedBookmarks.forEach { bookmark ->
                if (!currentBookmarks.any { it.url == bookmark.url }) {
                    currentBookmarks.add(bookmark)
                    addedCount++
                }
            }
            
            if (addedCount > 0) {
                saveBookmarks(currentBookmarks)
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Guardar marcadores
     */
    private fun saveBookmarks(bookmarks: List<Bookmark>) {
        val bookmarksJson = gson.toJson(bookmarks)
        prefs.edit().putString(BOOKMARKS_KEY, bookmarksJson).apply()
    }
    
    /**
     * Obtener estadísticas de marcadores
     */
    fun getBookmarkStats(): String {
        val bookmarks = getBookmarks()
        val totalBookmarks = bookmarks.size
        val totalFolders = getFolders().size
        
        return "Marcadores: $totalBookmarks en $totalFolders carpetas"
    }
}
