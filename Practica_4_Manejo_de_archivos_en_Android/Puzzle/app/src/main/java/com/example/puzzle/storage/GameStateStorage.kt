package com.example.puzzle.storage

import android.content.Context
import com.example.puzzle.models.GameState
import java.io.File

/**
 * Interface for saving and loading game states in different formats.
 */
interface GameStateStorage {
    
    /**
     * Save a game state to a file.
     * 
     * @param context The application context used to get the app's files directory
     * @param gameState The game state to save
     * @param fileName Optional filename to use. If null, a default name will be generated
     * @return The File object for the saved file
     */
    fun saveGame(context: Context, gameState: GameState, fileName: String? = null): File
    
    /**
     * Load a game state from a file.
     * 
     * @param context The application context used to get the app's files directory
     * @param file The file to load from
     * @return The loaded GameState object
     */
    fun loadGame(context: Context, file: File): GameState
    
    /**
     * Get a list of all saved games in this format.
     * 
     * @param context The application context used to get the app's files directory
     * @return A list of File objects representing saved games
     */
    fun getSavedGames(context: Context): List<File>
    
    /**
     * Gets the file extension for this storage format (e.g., "txt", "xml", "json").
     */
    fun getFileExtension(): String
    
    /**
     * Gets a human-readable name for this storage format.
     */
    fun getFormatName(): String
}

/**
 * Enum representing the different storage formats available.
 */
enum class StorageFormat {
    TEXT,
    XML,
    JSON
}
