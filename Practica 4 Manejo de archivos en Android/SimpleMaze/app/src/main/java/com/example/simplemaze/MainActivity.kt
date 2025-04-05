package com.example.simplemaze

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var mazeView: MazeView
    private lateinit var scoreTextView: TextView
    private lateinit var levelTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var soundPool: SoundPool
    
    private var moveSound: Int = 0
    private var levelCompleteSound: Int = 0
    private var hitWallSound: Int = 0
    
    private var currentLevel = 1
    private var score = 0
    private var gameStartTimeMillis: Long = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        // Initialize views
        mazeView = findViewById(R.id.mazeView)
        scoreTextView = findViewById(R.id.scoreTextView)
        levelTextView = findViewById(R.id.levelTextView)
        timerTextView = findViewById(R.id.timerTextView)
        
        // Set up sound effects
        setupSoundEffects()
        
        // Configure maze view
        mazeView.setLevel(currentLevel)
        mazeView.setOnMazeCompletedListener { 
            onMazeCompleted() 
        }
        mazeView.setOnMoveListener { 
            onPlayerMove() 
        }
        mazeView.setOnWallHitListener {
            onWallHit()
        }
        
        // Initialize game state
        updateUI()
        gameStartTimeMillis = System.currentTimeMillis()
        
        // Setup controls
        findViewById<Button>(R.id.btnUp).setOnClickListener { mazeView.movePlayer(0, -1) }
        findViewById<Button>(R.id.btnDown).setOnClickListener { mazeView.movePlayer(0, 1) }
        findViewById<Button>(R.id.btnLeft).setOnClickListener { mazeView.movePlayer(-1, 0) }
        findViewById<Button>(R.id.btnRight).setOnClickListener { mazeView.movePlayer(1, 0) }
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    
    private fun setupSoundEffects() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
            
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()
            
        moveSound = soundPool.load(this, R.raw.move_sound, 1)
        levelCompleteSound = soundPool.load(this, R.raw.level_complete, 1)
        hitWallSound = soundPool.load(this, R.raw.wall_hit, 1)
    }
    
    private fun onMazeCompleted() {
        // Play sound
        soundPool.play(levelCompleteSound, 1f, 1f, 1, 0, 1f)
        
        // Update score based on time
        val timeBonus = calculateTimeBonus()
        score += 100 + timeBonus
        
        // Move to next level
        currentLevel++
        if (currentLevel > MazeGenerator.MAX_LEVELS) {
            // Game completed, handle end game
            // For now, just restart from level 1
            currentLevel = 1
        }
        
        // Reset the maze for new level
        mazeView.setLevel(currentLevel)
        
        // Reset timer for new level
        gameStartTimeMillis = System.currentTimeMillis()
        
        // Update UI
        updateUI()
    }
    
    private fun onPlayerMove() {
        soundPool.play(moveSound, 0.5f, 0.5f, 1, 0, 1f)
        score += 1
        updateUI()
    }
    
    private fun onWallHit() {
        soundPool.play(hitWallSound, 0.7f, 0.7f, 1, 0, 1f)
        // Optional: Implement penalty for hitting walls
        // score -= 5
        // updateUI()
    }
    
    private fun calculateTimeBonus(): Int {
        val elapsedTimeSeconds = (System.currentTimeMillis() - gameStartTimeMillis) / 1000
        // Faster completion gives higher bonus
        return (120 - elapsedTimeSeconds).coerceAtLeast(0).toInt()
    }
    
    private fun updateUI() {
        scoreTextView.text = "Score: $score"
        levelTextView.text = "Level: $currentLevel"
        
        // Update timer
        val elapsedTimeSeconds = (System.currentTimeMillis() - gameStartTimeMillis) / 1000
        val minutes = elapsedTimeSeconds / 60
        val seconds = elapsedTimeSeconds % 60
        timerTextView.text = String.format("Time: %02d:%02d", minutes, seconds)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}