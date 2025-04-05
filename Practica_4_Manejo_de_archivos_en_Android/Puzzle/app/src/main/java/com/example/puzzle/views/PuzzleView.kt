package com.example.puzzle.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.core.content.withStyledAttributes
import com.example.puzzle.R
import com.example.puzzle.models.GameBoard
import com.example.puzzle.models.Tile

/**
 * Custom view for rendering the sliding puzzle game board.
 */
class PuzzleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    // Game board reference
    private var gameBoard: GameBoard? = null
    
    // Callback for when a tile is moved
    var onTileMoved: ((Boolean) -> Unit)? = null
    
    // Paint objects for drawing
    private val backgroundPaint = Paint().apply {
        color = Color.LTGRAY
        style = Paint.Style.FILL
    }
    
    private val tilePaint = Paint().apply {
        style = Paint.Style.FILL
    }
    
    private val correctTilePaint = Paint().apply {
        style = Paint.Style.FILL
    }
    
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        textSize = 50f
    }
    
    private val borderPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    
    // For sound effects
    private var toneGenerator: ToneGenerator? = null
    private var soundEnabled = true
    
    init {
        // Initialize colors from the theme
        context.withStyledAttributes(attrs, R.styleable.PuzzleTheme) {
            tilePaint.color = getColor(R.styleable.PuzzleTheme_puzzleTileColor, 
                resources.getColor(R.color.escom_tile, context.theme))
                
            correctTilePaint.color = getColor(R.styleable.PuzzleTheme_puzzleTileCorrectColor, 
                resources.getColor(R.color.escom_tile_correct, context.theme))
        }
        
        // Initialize sound if enabled
        if (soundEnabled) {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        }
    }
    
    /**
     * Sets the game board to be displayed.
     */
    fun setGameBoard(board: GameBoard) {
        gameBoard = board
        invalidate() // Redraw the view
    }
    
    /**
     * Enable or disable sound effects
     */
    fun setSoundEnabled(enabled: Boolean) {
        if (enabled != soundEnabled) {
            soundEnabled = enabled
            
            if (soundEnabled && toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            } else if (!soundEnabled && toneGenerator != null) {
                toneGenerator?.release()
                toneGenerator = null
            }
        }
    }
    
    /**
     * Update the tile colors based on the current theme
     */
    fun updateThemeColors() {
        context.withStyledAttributes(null, R.styleable.PuzzleTheme) {
            tilePaint.color = getColor(R.styleable.PuzzleTheme_puzzleTileColor, 
                resources.getColor(R.color.escom_tile, context.theme))
                
            correctTilePaint.color = getColor(R.styleable.PuzzleTheme_puzzleTileCorrectColor, 
                resources.getColor(R.color.escom_tile_correct, context.theme))
        }
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        gameBoard?.let { board ->
            val tileSize = calculateTileSize(board.gridSize)
            
            // Draw background
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
            
            // Draw tiles
            for (tile in board.getAllTiles()) {
                if (!tile.isEmpty()) {
                    val rect = getTileRect(tile, tileSize)
                    
                    // Choose the paint color based on whether the tile is in the correct position
                    val paint = if (tile.isInCorrectPosition()) correctTilePaint else tilePaint
                    
                    // Draw tile background
                    canvas.drawRoundRect(rect, 8f, 8f, paint)
                    canvas.drawRoundRect(rect, 8f, 8f, borderPaint)
                    
                    // Draw tile number
                    val xPos = rect.left + tileSize / 2
                    val yPos = rect.top + tileSize / 2 - (textPaint.descent() + textPaint.ascent()) / 2
                    canvas.drawText(tile.number.toString(), xPos, yPos, textPaint)
                }
            }
        }
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        invalidate()
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            gameBoard?.let { board ->
                val tileSize = calculateTileSize(board.gridSize)
                val row = (event.y / tileSize).toInt()
                val col = (event.x / tileSize).toInt()
                
                // Check if row and col are within the grid bounds
                if (row >= 0 && row < board.gridSize && col >= 0 && col < board.gridSize) {
                    val tileMoved = board.moveTile(row, col)
                    
                    if (tileMoved) {
                        // Play sound effect if enabled
                        if (soundEnabled) {
                            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
                        }
                        
                        // Animate the tile movement (in a real app, this would be more sophisticated)
                        invalidate()
                        
                        // Check if the puzzle is solved
                        val isSolved = board.isSolved()
                        onTileMoved?.invoke(isSolved)
                    }
                    
                    return true
                }
            }
        }
        
        return super.onTouchEvent(event)
    }
    
    /**
     * Calculates the size of each tile based on the grid size and view dimensions.
     */
    private fun calculateTileSize(gridSize: Int): Float {
        val minDimension = minOf(width, height)
        return minDimension.toFloat() / gridSize
    }
    
    /**
     * Gets the rectangle for drawing a specific tile.
     */
    private fun getTileRect(tile: Tile, tileSize: Float): RectF {
        val left = tile.currentCol * tileSize
        val top = tile.currentRow * tileSize
        
        return RectF(left, top, left + tileSize, top + tileSize)
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        toneGenerator?.release()
        toneGenerator = null
    }
}
