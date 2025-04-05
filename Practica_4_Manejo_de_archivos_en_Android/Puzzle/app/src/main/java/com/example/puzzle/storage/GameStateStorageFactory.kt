package com.example.puzzle.storage

/**
 * Factory class for creating GameStateStorage instances.
 */
object GameStateStorageFactory {
    
    /**
     * Gets a GameStateStorage implementation for the given format.
     */
    fun getStorage(format: StorageFormat): GameStateStorage {
        return when (format) {
            StorageFormat.TEXT -> TextGameStateStorage()
            StorageFormat.XML -> XmlGameStateStorage()
            StorageFormat.JSON -> JsonGameStateStorage()
        }
    }
    
    /**
     * Gets all available storage implementations.
     */
    fun getAllStorages(): List<GameStateStorage> {
        return listOf(
            TextGameStateStorage(),
            XmlGameStateStorage(),
            JsonGameStateStorage()
        )
    }
    
    /**
     * Determines the format of a saved game file based on its extension.
     */
    fun getFormatForFile(fileName: String): StorageFormat? {
        return when {
            fileName.lowercase().endsWith(".txt") -> StorageFormat.TEXT
            fileName.lowercase().endsWith(".xml") -> StorageFormat.XML
            fileName.lowercase().endsWith(".json") -> StorageFormat.JSON
            else -> null
        }
    }
}
