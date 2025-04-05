package com.example.puzzle.storage

import android.content.Context
import com.example.puzzle.models.GameBoard
import com.example.puzzle.models.GameState
import com.example.puzzle.models.TileState
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Implementation of GameStateStorage for saving and loading games in JSON format.
 */
class JsonGameStateStorage : GameStateStorage {
    
    private val saveDirectory = "puzzle_saves/json"
    
    override fun saveGame(context: Context, gameState: GameState, fileName: String?): File {
        // Create the save directory if it doesn't exist
        val directory = File(context.filesDir, saveDirectory)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        
        // Determine the file name to use
        val actualFileName = fileName ?: generateFileName(gameState)
        val file = File(directory, "$actualFileName.${getFileExtension()}")
        
        // Create JSON object with game state data
        val jsonObject = JSONObject()
        jsonObject.put("title", gameState.gameTitle)
        jsonObject.put("difficulty", gameState.difficulty.toString())
        jsonObject.put("gridSize", gameState.gridSize)
        jsonObject.put("moveCount", gameState.moveCount)
        jsonObject.put("elapsedTimeSeconds", gameState.elapsedTimeSeconds)
        jsonObject.put("score", gameState.score)
        jsonObject.put("timestamp", gameState.timestamp)
        jsonObject.put("saveDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date(gameState.timestamp)))
        
        // Add tiles array
        val tilesArray = JSONArray()
        for (tile in gameState.tiles) {
            val tileObject = JSONObject()
            tileObject.put("number", tile.number)
            tileObject.put("currentRow", tile.currentRow)
            tileObject.put("currentCol", tile.currentCol)
            tileObject.put("correctRow", tile.correctRow)
            tileObject.put("correctCol", tile.correctCol)
            tilesArray.put(tileObject)
        }
        jsonObject.put("tiles", tilesArray)
        
        // Write the JSON to the file
        FileWriter(file).use { writer ->
            writer.write(jsonObject.toString(2)) // Use 2 spaces for indentation
        }
        
        return file
    }
    
    override fun loadGame(context: Context, file: File): GameState {
        val tilesList = mutableListOf<TileState>()
        
        // Read the entire file content
        val content = BufferedReader(FileReader(file)).use { reader ->
            reader.readText()
        }
        
        // Parse the JSON
        val jsonObject = JSONObject(content)
        
        // Extract metadata
        val gameTitle = jsonObject.getString("title")
        val difficultyStr = jsonObject.getString("difficulty")
        val difficulty = if (difficultyStr == "EASY") GameBoard.GameDifficulty.EASY else GameBoard.GameDifficulty.HARD
        val gridSize = jsonObject.getInt("gridSize")
        val moveCount = jsonObject.getInt("moveCount")
        val elapsedTimeSeconds = jsonObject.getInt("elapsedTimeSeconds")
        val score = jsonObject.getInt("score")
        val timestamp = jsonObject.getLong("timestamp")
        
        // Extract tiles
        val tilesArray = jsonObject.getJSONArray("tiles")
        for (i in 0 until tilesArray.length()) {
            val tileObject = tilesArray.getJSONObject(i)
            tilesList.add(
                TileState(
                    number = tileObject.getInt("number"),
                    currentRow = tileObject.getInt("currentRow"),
                    currentCol = tileObject.getInt("currentCol"),
                    correctRow = tileObject.getInt("correctRow"),
                    correctCol = tileObject.getInt("correctCol")
                )
            )
        }
        
        return GameState(
            tiles = tilesList,
            difficulty = difficulty,
            gridSize = gridSize,
            moveCount = moveCount,
            elapsedTimeSeconds = elapsedTimeSeconds,
            score = score,
            timestamp = timestamp,
            gameTitle = gameTitle
        )
    }
    
    override fun getSavedGames(context: Context): List<File> {
        val directory = File(context.filesDir, saveDirectory)
        if (!directory.exists()) {
            return emptyList()
        }
        
        return directory.listFiles { file ->
            file.isFile && file.name.endsWith(".${getFileExtension()}")
        }?.toList() ?: emptyList()
    }
    
    override fun getFileExtension(): String {
        return "json"
    }
    
    override fun getFormatName(): String {
        return "JSON"
    }
    
    private fun generateFileName(gameState: GameState): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val dateStr = dateFormat.format(Date(gameState.timestamp))
        val difficulty = if (gameState.difficulty == GameBoard.GameDifficulty.EASY) "easy" else "hard"
        return "puzzle_${difficulty}_${dateStr}"
    }
}
