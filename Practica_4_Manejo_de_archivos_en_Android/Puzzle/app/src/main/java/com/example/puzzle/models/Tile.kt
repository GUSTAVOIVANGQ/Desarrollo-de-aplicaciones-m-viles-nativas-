package com.example.puzzle.models

/**
 * Represents a tile in the puzzle game.
 * 
 * @property number The number on the tile (0 represents the empty tile)
 * @property currentRow The current row position of the tile
 * @property currentCol The current column position of the tile
 * @property correctRow The correct row position of the tile in the solved state
 * @property correctCol The correct column position of the tile in the solved state
 */
data class Tile(
    val number: Int,
    var currentRow: Int,
    var currentCol: Int,
    val correctRow: Int,
    val correctCol: Int
) {
    /**
     * Checks if the tile is in its correct position.
     */
    fun isInCorrectPosition(): Boolean {
        return currentRow == correctRow && currentCol == correctCol
    }
    
    /**
     * Checks if this tile is the empty tile.
     */
    fun isEmpty(): Boolean {
        return number == 0
    }
    
    /**
     * Checks if this tile is adjacent to the given position.
     */
    fun isAdjacentTo(row: Int, col: Int): Boolean {
        return (currentRow == row && Math.abs(currentCol - col) == 1) ||
                (currentCol == col && Math.abs(currentRow - row) == 1)
    }
}
