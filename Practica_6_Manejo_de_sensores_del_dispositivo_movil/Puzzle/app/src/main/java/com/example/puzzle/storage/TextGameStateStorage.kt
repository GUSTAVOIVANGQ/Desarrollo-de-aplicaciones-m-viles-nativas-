package com.example.puzzle.storage

import android.content.Context
import com.example.puzzle.models.GameBoard
import com.example.puzzle.models.GameState
import com.example.puzzle.models.TileState
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Implementation of GameStateStorage for saving and loading games in plain text format.
 */
class TextGameStateStorage : GameStateStorage {
    
    private val saveDirectory = "puzzle_saves/text"
    
    override fun saveGame(context: Context, gameState: GameState, fileName: String?): File {
        // Create the save directory if it doesn't exist
        val directory = File(context.filesDir, saveDirectory)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        
        // Determine the file name to use
        val actualFileName = fileName ?: generateFileName(gameState)
        val file = File(directory, "$actualFileName.${getFileExtension()}")
        
        // Write the game state to the file
        PrintWriter(FileWriter(file)).use { writer ->
            // Write header with metadata
            writer.println("# Puzzle Game Save - Text Format")
            writer.println("# Saved: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(gameState.timestamp))}")
            writer.println("TITLE=${gameState.gameTitle}")
            writer.println("DIFFICULTY=${gameState.difficulty}")
            writer.println("GRID_SIZE=${gameState.gridSize}")
            writer.println("MOVE_COUNT=${gameState.moveCount}")
            writer.println("TIME_SECONDS=${gameState.elapsedTimeSeconds}")
            writer.println("SCORE=${gameState.score}")
            writer.println("TIMESTAMP=${gameState.timestamp}")
            
            // Write tile data
            writer.println("# Tile data format: number,currentRow,currentCol,correctRow,correctCol")
            writer.println("TILES_BEGIN")
            for (tile in gameState.tiles) {
                writer.println("${tile.number},${tile.currentRow},${tile.currentCol},${tile.correctRow},${tile.correctCol}")
            }
            writer.println("TILES_END")
        }
        
        return file
    }
    
    override fun loadGame(context: Context, file: File): GameState {
        val tilesList = mutableListOf<TileState>()
        var difficulty = GameBoard.GameDifficulty.EASY
        var gridSize = 3
        var moveCount = 0
        var elapsedTimeSeconds = 0
        var score = 0
        var timestamp = System.currentTimeMillis()
        var gameTitle = "Untitled Game"
        
        BufferedReader(FileReader(file)).use { reader ->
            var line: String?
            var readingTiles = false
            
            while (reader.readLine().also { line = it } != null) {
                val currentLine = line ?: continue
                
                // Skip comments and empty lines
                if (currentLine.startsWith("#") || currentLine.trim().isEmpty()) {
                    continue
                }
                
                // Check if we're in the tiles section
                if (currentLine == "TILES_BEGIN") {
                    readingTiles = true
                    continue
                } else if (currentLine == "TILES_END") {
                    readingTiles = false
                    continue
                }
                
                if (readingTiles) {
                    // Parse tile data
                    val parts = currentLine.split(",")
                    if (parts.size == 5) {
                        tilesList.add(
                            TileState(
                                number = parts[0].toInt(),
                                currentRow = parts[1].toInt(),
                                currentCol = parts[2].toInt(),
                                correctRow = parts[3].toInt(),
                                correctCol = parts[4].toInt()
                            )
                        )
                    }
                } else {
                    // Parse metadata
                    if (currentLine.startsWith("TITLE=")) {
                        gameTitle = currentLine.substring("TITLE=".length)
                    } else if (currentLine.startsWith("DIFFICULTY=")) {
                        difficulty = if (currentLine.substring("DIFFICULTY=".length) == "EASY") {
                            GameBoard.GameDifficulty.EASY
                        } else {
                            GameBoard.GameDifficulty.HARD
                        }
                    } else if (currentLine.startsWith("GRID_SIZE=")) {
                        gridSize = currentLine.substring("GRID_SIZE=".length).toInt()
                    } else if (currentLine.startsWith("MOVE_COUNT=")) {
                        moveCount = currentLine.substring("MOVE_COUNT=".length).toInt()
                    } else if (currentLine.startsWith("TIME_SECONDS=")) {
                        elapsedTimeSeconds = currentLine.substring("TIME_SECONDS=".length).toInt()
                    } else if (currentLine.startsWith("SCORE=")) {
                        score = currentLine.substring("SCORE=".length).toInt()
                    } else if (currentLine.startsWith("TIMESTAMP=")) {
                        timestamp = currentLine.substring("TIMESTAMP=".length).toLong()
                    }
                }
            }
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
        return "txt"
    }
    
    override fun getFormatName(): String {
        return "Plain Text"
    }
    
    private fun generateFileName(gameState: GameState): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val dateStr = dateFormat.format(Date(gameState.timestamp))
        val difficulty = if (gameState.difficulty == GameBoard.GameDifficulty.EASY) "easy" else "hard"
        return "puzzle_${difficulty}_${dateStr}"
    }
}
