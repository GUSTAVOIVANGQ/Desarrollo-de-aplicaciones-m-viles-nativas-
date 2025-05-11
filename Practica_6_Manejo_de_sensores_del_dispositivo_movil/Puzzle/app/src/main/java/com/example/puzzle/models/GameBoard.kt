package com.example.puzzle.models

import kotlin.random.Random

/**
 * Manages the game board state and logic for the sliding puzzle.
 * 
 * @property gridSize The size of the grid (e.g., 3 for a 3x3 puzzle, 4 for a 4x4 puzzle)
 * @property difficulty The difficulty level of the game
 * @property moveCount The number of moves made in the current game
 * @property startTimeMillis The start time of the game in milliseconds
 */
class GameBoard(val gridSize: Int, val difficulty: GameDifficulty) {
    
    enum class GameDifficulty {
        EASY, // 3x3 grid
        HARD  // 4x4 grid
    }
    
    private val tiles: MutableList<Tile> = mutableListOf()
    private var emptyTileRow: Int = gridSize - 1
    private var emptyTileCol: Int = gridSize - 1
    
    var moveCount: Int = 0
    var startTimeMillis: Long = System.currentTimeMillis()
    
    init {
        initializeTiles()
        shuffleTiles()
    }
    
    /**
     * Initializes the tiles in the solved position.
     */
    private fun initializeTiles() {
        tiles.clear()
        
        // Create all tiles in their correct positions
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val number = row * gridSize + col + 1
                
                // The last tile is the empty tile (represented by 0)
                if (row == gridSize - 1 && col == gridSize - 1) {
                    tiles.add(Tile(0, row, col, row, col))
                } else {
                    tiles.add(Tile(number, row, col, row, col))
                }
            }
        }
    }
    
    /**
     * Shuffles the tiles to create a random, solvable puzzle.
     */
    private fun shuffleTiles() {
        // Number of random moves to perform based on difficulty
        val shuffleMoves = when (difficulty) {
            GameDifficulty.EASY -> gridSize * gridSize * 20
            GameDifficulty.HARD -> gridSize * gridSize * 40
        }
        
        // Make a series of random valid moves to ensure the puzzle is solvable
        repeat(shuffleMoves) {
            // Get all possible moves
            val possibleMoves = getPossibleMoves()
            if (possibleMoves.isNotEmpty()) {
                // Select a random move
                val randomMove = possibleMoves[Random.nextInt(possibleMoves.size)]
                // Perform the move
                moveTile(randomMove.currentRow, randomMove.currentCol, false)
            }
        }
        
        // Reset move count after shuffling
        moveCount = 0
        startTimeMillis = System.currentTimeMillis()
    }
    
    /**
     * Gets all tiles that can be moved into the empty slot.
     */
    private fun getPossibleMoves(): List<Tile> {
        return tiles.filter { tile ->
            !tile.isEmpty() && tile.isAdjacentTo(emptyTileRow, emptyTileCol)
        }
    }
    
    /**
     * Attempts to move a tile at the specified position.
     * Returns true if the move was successful.
     */
    fun moveTile(row: Int, col: Int, countMove: Boolean = true): Boolean {
        val selectedTile = getTileAt(row, col) ?: return false
        
        // Check if the selected tile is adjacent to the empty tile
        if (!selectedTile.isAdjacentTo(emptyTileRow, emptyTileCol)) {
            return false
        }
        
        // Swap the selected tile with the empty tile
        val emptyTile = getTileAt(emptyTileRow, emptyTileCol)!!
        
        // Update positions
        selectedTile.currentRow = emptyTileRow
        selectedTile.currentCol = emptyTileCol
        emptyTile.currentRow = row
        emptyTile.currentCol = col
        
        // Update empty tile position reference
        emptyTileRow = row
        emptyTileCol = col
        
        // Increment move count if this is a player move
        if (countMove) {
            moveCount++
        }
        
        return true
    }
    
    /**
     * Gets the tile at the specified position.
     */
    fun getTileAt(row: Int, col: Int): Tile? {
        return tiles.find { it.currentRow == row && it.currentCol == col }
    }
    
    /**
     * Restores the game board from a saved state
     */
    fun restoreFromState(tiles: List<TileState>, moveCount: Int, elapsedTimeSeconds: Int) {
        this.tiles.clear()
        
        // Add all tiles from the saved state
        tiles.forEach { tileState ->
            this.tiles.add(
                Tile(
                    number = tileState.number,
                    currentRow = tileState.currentRow,
                    currentCol = tileState.currentCol,
                    correctRow = tileState.correctRow,
                    correctCol = tileState.correctCol
                )
            )
            
            // Find the empty tile and update its position reference
            if (tileState.number == 0) {
                emptyTileRow = tileState.currentRow
                emptyTileCol = tileState.currentCol
            }
        }
        
        // Restore move count
        this.moveCount = moveCount
        
        // Adjust the start time based on elapsed time
        this.startTimeMillis = System.currentTimeMillis() - (elapsedTimeSeconds * 1000)
    }
    
    /**
     * Returns all tiles in the game board.
     */
    fun getAllTiles(): List<Tile> {
        return tiles
    }
    
    /**
     * Checks if the puzzle is solved.
     */
    fun isSolved(): Boolean {
        return tiles.all { it.isInCorrectPosition() }
    }
    
    /**
     * Calculates the current score based on moves and time.
     */
    fun calculateScore(): Int {
        val timeTakenSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000
        val baseScore = gridSize * gridSize * 100
        val moveScore = baseScore - (moveCount * 5)
        val timeScore = baseScore - (timeTakenSeconds.toInt() * 2)
        
        // Combine the scores with weights
        return ((moveScore * 0.7) + (timeScore * 0.3)).toInt().coerceAtLeast(0)
    }
    
    /**
     * Resets the game with the same settings.
     */
    fun resetGame() {
        initializeTiles()
        shuffleTiles()
    }
}
