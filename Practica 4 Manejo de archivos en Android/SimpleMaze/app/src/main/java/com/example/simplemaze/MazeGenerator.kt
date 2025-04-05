package com.example.simplemaze

import kotlin.random.Random

object MazeGenerator {
    const val WALL = 0
    const val PATH = 1
    const val MAX_LEVELS = 5
    
    private val levelSizes = mapOf(
        1 to Pair(11, 11),   // Level 1: 11x11 maze
        2 to Pair(15, 15),   // Level 2: 15x15 maze
        3 to Pair(19, 19),   // Level 3: 19x19 maze
        4 to Pair(23, 23),   // Level 4: 23x23 maze
        5 to Pair(27, 27)    // Level 5: 27x27 maze
    )
    
    fun generateMaze(level: Int): Array<IntArray> {
        val size = levelSizes[level.coerceIn(1, MAX_LEVELS)] ?: Pair(11, 11)
        val width = size.first
        val height = size.second
        
        // Create a maze filled with walls
        val maze = Array(height) { IntArray(width) { WALL } }
        
        // Start from cell (1,1)
        generateMazeRecursive(maze, 1, 1, Random.Default)
        
        // Ensure the exit is open
        maze[height - 2][width - 2] = PATH
        
        return maze
    }
    
    private fun generateMazeRecursive(maze: Array<IntArray>, x: Int, y: Int, random: Random) {
        // Mark current cell as path
        maze[y][x] = PATH
        
        // Define possible directions to move: up, right, down, left
        val directions = mutableListOf(0, 1, 2, 3)
        directions.shuffle(random)
        
        // Try each direction
        for (direction in directions) {
            val dx = when (direction) {
                0 -> 0    // Up
                1 -> 2    // Right
                2 -> 0    // Down
                3 -> -2   // Left
                else -> 0
            }
            
            val dy = when (direction) {
                0 -> -2   // Up
                1 -> 0    // Right
                2 -> 2    // Down
                3 -> 0    // Left
                else -> 0
            }
            
            val nx = x + dx
            val ny = y + dy
            
            // Check if the next cell is valid
            if (nx in 1 until maze[0].size - 1 && ny in 1 until maze.size - 1 && maze[ny][nx] == WALL) {
                // Carve a path by marking the cell between current and next as PATH
                maze[y + dy/2][x + dx/2] = PATH
                // Continue generating from the next cell
                generateMazeRecursive(maze, nx, ny, random)
            }
        }
    }
}
