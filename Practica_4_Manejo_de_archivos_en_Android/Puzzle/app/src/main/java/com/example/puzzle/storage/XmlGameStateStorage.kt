package com.example.puzzle.storage

import android.content.Context
import com.example.puzzle.models.GameBoard
import com.example.puzzle.models.GameState
import com.example.puzzle.models.TileState
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Implementation of GameStateStorage for saving and loading games in XML format.
 */
class XmlGameStateStorage : GameStateStorage {
    
    private val saveDirectory = "puzzle_saves/xml"
    
    override fun saveGame(context: Context, gameState: GameState, fileName: String?): File {
        // Create the save directory if it doesn't exist
        val directory = File(context.filesDir, saveDirectory)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        
        // Determine the file name to use
        val actualFileName = fileName ?: generateFileName(gameState)
        val file = File(directory, "$actualFileName.${getFileExtension()}")
        
        // Create an XML serializer
        val serializer = XmlPullParserFactory.newInstance().newSerializer()
        FileOutputStream(file).use { fos ->
            serializer.setOutput(fos, "UTF-8")
            serializer.startDocument("UTF-8", true)
            
            // Root element
            serializer.startTag("", "puzzle-save")
            
            // Metadata
            addTag(serializer, "title", gameState.gameTitle)
            addTag(serializer, "difficulty", gameState.difficulty.toString())
            addTag(serializer, "grid-size", gameState.gridSize.toString())
            addTag(serializer, "move-count", gameState.moveCount.toString())
            addTag(serializer, "time-seconds", gameState.elapsedTimeSeconds.toString())
            addTag(serializer, "score", gameState.score.toString())
            addTag(serializer, "timestamp", gameState.timestamp.toString())
            addTag(serializer, "save-date", 
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date(gameState.timestamp)))
            
            // Tiles
            serializer.startTag("", "tiles")
            for (tile in gameState.tiles) {
                serializer.startTag("", "tile")
                addTag(serializer, "number", tile.number.toString())
                addTag(serializer, "current-row", tile.currentRow.toString())
                addTag(serializer, "current-col", tile.currentCol.toString())
                addTag(serializer, "correct-row", tile.correctRow.toString())
                addTag(serializer, "correct-col", tile.correctCol.toString())
                serializer.endTag("", "tile")
            }
            serializer.endTag("", "tiles")
            
            // End of root element
            serializer.endTag("", "puzzle-save")
            serializer.endDocument()
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
        
        // Create a parser
        val parser = XmlPullParserFactory.newInstance().newPullParser()
        FileInputStream(file).use { fis ->
            parser.setInput(fis, null)
            
            var eventType = parser.eventType
            var currentTag = ""
            var inTilesSection = false
            var currentTile = TileState(0, 0, 0, 0, 0)
            var tileNumber = 0
            var currentRow = 0
            var currentCol = 0
            var correctRow = 0
            var correctCol = 0
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                        
                        if (currentTag == "tiles") {
                            inTilesSection = true
                        } else if (currentTag == "tile" && inTilesSection) {
                            // Reset tile values for a new tile
                            tileNumber = 0
                            currentRow = 0
                            currentCol = 0
                            correctRow = 0
                            correctCol = 0
                        }
                    }
                    XmlPullParser.TEXT -> {
                        val text = parser.text
                        
                        if (!inTilesSection) {
                            // Parse metadata
                            when (currentTag) {
                                "title" -> gameTitle = text
                                "difficulty" -> difficulty = if (text == "EASY") GameBoard.GameDifficulty.EASY else GameBoard.GameDifficulty.HARD
                                "grid-size" -> gridSize = text.toInt()
                                "move-count" -> moveCount = text.toInt()
                                "time-seconds" -> elapsedTimeSeconds = text.toInt()
                                "score" -> score = text.toInt()
                                "timestamp" -> timestamp = text.toLong()
                            }
                        } else {
                            // Parse tile data
                            when (currentTag) {
                                "number" -> tileNumber = text.toInt()
                                "current-row" -> currentRow = text.toInt()
                                "current-col" -> currentCol = text.toInt()
                                "correct-row" -> correctRow = text.toInt()
                                "correct-col" -> correctCol = text.toInt()
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "tile" && inTilesSection) {
                            // Add the completed tile to our list
                            tilesList.add(
                                TileState(
                                    number = tileNumber,
                                    currentRow = currentRow,
                                    currentCol = currentCol,
                                    correctRow = correctRow,
                                    correctCol = correctCol
                                )
                            )
                        } else if (parser.name == "tiles") {
                            inTilesSection = false
                        }
                        currentTag = ""
                    }
                }
                eventType = parser.next()
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
        return "xml"
    }
    
    override fun getFormatName(): String {
        return "XML"
    }
    
    private fun generateFileName(gameState: GameState): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val dateStr = dateFormat.format(Date(gameState.timestamp))
        val difficulty = if (gameState.difficulty == GameBoard.GameDifficulty.EASY) "easy" else "hard"
        return "puzzle_${difficulty}_${dateStr}"
    }
    
    private fun addTag(serializer: XmlSerializer, name: String, value: String) {
        serializer.startTag("", name)
        serializer.text(value)
        serializer.endTag("", name)
    }
}
