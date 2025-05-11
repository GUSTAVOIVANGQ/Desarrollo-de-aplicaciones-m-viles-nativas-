package com.example.puzzle.models

import java.io.Serializable

/**
 * Represents the complete state of a game that can be saved and loaded.
 */
data class GameState(
    val tiles: List<TileState>, // Current state of all tiles
    val difficulty: GameBoard.GameDifficulty, // Current difficulty
    val gridSize: Int, // Size of the grid (3x3 or 4x4)
    val moveCount: Int, // Number of moves made
    val elapsedTimeSeconds: Int, // Time elapsed in seconds
    val score: Int, // Current score
    val timestamp: Long = System.currentTimeMillis(), // When the game was saved
    val gameTitle: String // User-defined or auto-generated name for the saved game
) : Serializable {
    
    /**
     * Creates a GameState from the current state of a GameBoard
     */
    companion object {
        fun fromGameBoard(
            gameBoard: GameBoard, 
            elapsedTimeSeconds: Int, 
            gameTitle: String
        ): GameState {
            val tileStates = gameBoard.getAllTiles().map { tile ->
                TileState(
                    number = tile.number,
                    currentRow = tile.currentRow,
                    currentCol = tile.currentCol,
                    correctRow = tile.correctRow,
                    correctCol = tile.correctCol
                )
            }
            
            return GameState(
                tiles = tileStates,
                difficulty = gameBoard.difficulty,
                gridSize = gameBoard.gridSize,
                moveCount = gameBoard.moveCount,
                elapsedTimeSeconds = elapsedTimeSeconds,
                score = gameBoard.calculateScore(),
                gameTitle = gameTitle
            )
        }
    }
    
    /**
     * Recreates a GameBoard from this saved state
     */
    fun toGameBoard(): GameBoard {
        // Create a new GameBoard
        val gameBoard = GameBoard(gridSize, difficulty)
        
        // We need to override the default board created with our saved state
        gameBoard.restoreFromState(
            tiles = tiles,
            moveCount = moveCount,
            elapsedTimeSeconds = elapsedTimeSeconds
        )
        
        return gameBoard
    }
}

/**
 * Represents the state of a single tile. Simplified version of the Tile class for storage.
 */
data class TileState(
    val number: Int,
    val currentRow: Int,
    val currentCol: Int,
    val correctRow: Int,
    val correctCol: Int
) : Serializable
