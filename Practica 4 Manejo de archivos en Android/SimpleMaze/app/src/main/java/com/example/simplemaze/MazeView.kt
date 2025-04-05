package com.example.simplemaze

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.min

class MazeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val wallPaint = Paint().apply {
        color = Color.DKGRAY
        style = Paint.Style.FILL
    }
    
    private val pathPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    
    private val playerPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.player_color)
        style = Paint.Style.FILL
    }
    
    private val exitPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.exit_color)
        style = Paint.Style.FILL
    }
    
    private var maze: Array<IntArray> = emptyArray()
    private var playerX = 1
    private var playerY = 1
    private var exitX = 0
    private var exitY = 0
    private var cellSize = 0f
    
    private var onMazeCompletedListener: (() -> Unit)? = null
    private var onMoveListener: (() -> Unit)? = null
    private var onWallHitListener: (() -> Unit)? = null
    
    fun setOnMazeCompletedListener(listener: () -> Unit) {
        onMazeCompletedListener = listener
    }
    
    fun setOnMoveListener(listener: () -> Unit) {
        onMoveListener = listener
    }
    
    fun setOnWallHitListener(listener: () -> Unit) {
        onWallHitListener = listener
    }
    
    fun setLevel(level: Int) {
        maze = MazeGenerator.generateMaze(level)
        
        // Set starting position (usually 1,1)
        playerX = 1
        playerY = 1
        
        // Set exit position (bottom-right corner for most mazes)
        exitX = maze[0].size - 2
        exitY = maze.size - 2
        
        invalidate()
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateCellSize()
    }
    
    private fun calculateCellSize() {
        if (maze.isEmpty()) return
        
        val mazeSizeX = maze[0].size
        val mazeSizeY = maze.size
        
        cellSize = min(
            width.toFloat() / mazeSizeX,
            height.toFloat() / mazeSizeY
        )
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (maze.isEmpty()) return
        
        // Draw the maze
        for (y in maze.indices) {
            for (x in maze[y].indices) {
                val left = x * cellSize
                val top = y * cellSize
                val right = left + cellSize
                val bottom = top + cellSize
                
                val rect = RectF(left, top, right, bottom)
                
                when (maze[y][x]) {
                    MazeGenerator.WALL -> canvas.drawRect(rect, wallPaint)
                    MazeGenerator.PATH -> canvas.drawRect(rect, pathPaint)
                }
                
                // Draw exit
                if (x == exitX && y == exitY) {
                    canvas.drawRect(rect, exitPaint)
                }
            }
        }
        
        // Draw player
        val playerLeft = playerX * cellSize
        val playerTop = playerY * cellSize
        val playerSize = cellSize * 0.8f
        val playerMargin = (cellSize - playerSize) / 2
        
        canvas.drawOval(
            playerLeft + playerMargin,
            playerTop + playerMargin,
            playerLeft + playerSize + playerMargin,
            playerTop + playerSize + playerMargin,
            playerPaint
        )
        
        // Add visual effects (glow around player)
        playerPaint.setShadowLayer(cellSize * 0.15f, 0f, 0f, Color.YELLOW)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) return super.onTouchEvent(event)
        
        // Calculate which cell was touched
        val x = (event.x / cellSize).toInt()
        val y = (event.y / cellSize).toInt()
        
        // Calculate if we should move horizontally or vertically
        val diffX = x - playerX
        val diffY = y - playerY
        
        if (kotlin.math.abs(diffX) > kotlin.math.abs(diffY)) {
            // Move horizontally
            val dx = if (diffX > 0) 1 else -1
            movePlayer(dx, 0)
        } else {
            // Move vertically
            val dy = if (diffY > 0) 1 else -1
            movePlayer(0, dy)
        }
        
        return true
    }
    
    fun movePlayer(dx: Int, dy: Int) {
        val newX = playerX + dx
        val newY = playerY + dy
        
        // Check if the new position is valid
        if (newX >= 0 && newX < maze[0].size && newY >= 0 && newY < maze.size) {
            if (maze[newY][newX] == MazeGenerator.PATH) {
                playerX = newX
                playerY = newY
                onMoveListener?.invoke()
                
                // Check if player reached the exit
                if (playerX == exitX && playerY == exitY) {
                    onMazeCompletedListener?.invoke()
                }
                
                invalidate()
            } else {
                // Hit a wall
                onWallHitListener?.invoke()
            }
        }
    }
}
