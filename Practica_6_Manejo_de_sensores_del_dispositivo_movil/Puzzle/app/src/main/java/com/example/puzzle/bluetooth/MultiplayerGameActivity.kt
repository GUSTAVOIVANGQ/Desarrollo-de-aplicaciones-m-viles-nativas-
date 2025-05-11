package com.example.puzzle.bluetooth

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.puzzle.R
import com.example.puzzle.models.GameBoard
import com.example.puzzle.models.GameState
import com.example.puzzle.util.ThemeManager
import com.example.puzzle.views.PuzzleView

/**
 * Actividad para jugar en modo multijugador a través de Bluetooth.
 * Soporta tanto el modo cooperativo como el competitivo.
 */
class MultiplayerGameActivity : AppCompatActivity(), GameSyncManager.OnGameEventListener, GameSyncManager.OnConnectionEventListener {
    
    // UI elements
    private lateinit var localPuzzleView: PuzzleView
    private lateinit var remotePuzzleView: PuzzleView
    private lateinit var localMovesText: TextView
    private lateinit var localTimeText: TextView
    private lateinit var localScoreText: TextView
    private lateinit var remoteMovesText: TextView
    private lateinit var remoteTimeText: TextView
    private lateinit var remoteScoreText: TextView
    private lateinit var statusText: TextView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var readyButton: Button
    private lateinit var chatLayout: ConstraintLayout
    private lateinit var competitiveLayout: ConstraintLayout
    
    // Game elements
    private lateinit var localGameBoard: GameBoard
    private var remoteGameBoard: GameBoard? = null
    private var isLocalPlayerReady = false
    private var isRemotePlayerReady = false
    private var isGameStarted = false
    private var isCompetitiveMode = false
    private var deviceName = ""
    private var elapsedTimeSeconds = 0
    
    // Bluetooth and synchronization
    private lateinit var gameSyncManager: GameSyncManager
    
    // Theme management
    private lateinit var themeManager: ThemeManager
    
    // Timer for game
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timerRunnable: Runnable
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before calling super.onCreate
        themeManager = ThemeManager.getInstance(this)
        themeManager.applyTheme(this)
        
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiplayer_game)
        
        // Set up action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Get intent extras
        deviceName = intent.getStringExtra("deviceName") ?: "Oponente"
        isCompetitiveMode = intent.getBooleanExtra("gameMode", false)
        
        // Initialize UI elements
        localPuzzleView = findViewById(R.id.localPuzzleView)
        localMovesText = findViewById(R.id.localMovesText)
        localTimeText = findViewById(R.id.localTimeText)
        localScoreText = findViewById(R.id.localScoreText)
        statusText = findViewById(R.id.statusText)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        readyButton = findViewById(R.id.readyButton)
        chatLayout = findViewById(R.id.chatLayout)
        
        // Initialize elements for competitive mode
        competitiveLayout = findViewById(R.id.competitiveLayout)
        if (isCompetitiveMode) {
            remotePuzzleView = findViewById(R.id.remotePuzzleView)
            remoteMovesText = findViewById(R.id.remoteMovesText)
            remoteTimeText = findViewById(R.id.remoteTimeText)
            remoteScoreText = findViewById(R.id.remoteScoreText)
            
            // Show the remote player's view for competitive mode
            competitiveLayout.visibility = View.VISIBLE
        } else {
            competitiveLayout.visibility = View.GONE
        }
        
        // Set up title
        supportActionBar?.title = if (isCompetitiveMode) {
            getString(R.string.competitive_mode_title)
        } else {
            getString(R.string.cooperative_mode_title)
        }
        
        // Initialize Bluetooth game sync
        gameSyncManager = GameSyncManager(this, if (isCompetitiveMode) 
            BluetoothManager.GAME_MODE_COMPETITIVE else BluetoothManager.GAME_MODE_COOPERATIVE)
        gameSyncManager.setOnGameEventListener(this)
        gameSyncManager.setOnConnectionEventListener(this)
        
        // Set up timer runnable
        timerRunnable = object : Runnable {
            override fun run() {
                if (isGameStarted) {
                    elapsedTimeSeconds++
                    updateTimeDisplay()
                    handler.postDelayed(this, 1000)
                }
            }
        }
        
        // Setup button listeners
        readyButton.setOnClickListener {
            isLocalPlayerReady = !isLocalPlayerReady
            readyButton.text = if (isLocalPlayerReady) {
                getString(R.string.btn_not_ready)
            } else {
                getString(R.string.btn_ready)
            }
            gameSyncManager.setLocalPlayerReady(isLocalPlayerReady)
            updateStatusText()
        }
        
        sendButton.setOnClickListener {
            val message = messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                gameSyncManager.sendChat(message)
                displayChatMessage("Tú", message)
                messageInput.text.clear()
            }
        }
        
        // Set up puzzle view
        setupLocalGameBoard()
        localPuzzleView.onTileMoved = { isSolved ->
            if (isSolved) {
                handleGameSolved()
            } else {
                updateLocalStats()
                
                // Sync game state if it's cooperative mode
                if (!isCompetitiveMode) {
                    val gameState = GameState.fromGameBoard(localGameBoard, elapsedTimeSeconds, "Shared Game")
                    gameSyncManager.syncGameState(gameState)
                }
            }
        }
        
        // Initialize UI state
        updateStatusText()
    }
    
    /**
     * Sets up the initial local game board
     */
    private fun setupLocalGameBoard() {
        val difficulty = GameBoard.GameDifficulty.EASY  // Por defecto, usar dificultad fácil para multijugador
        val gridSize = 3  // 3x3 para dificultad fácil
        localGameBoard = GameBoard(gridSize, difficulty)
        localPuzzleView.setGameBoard(localGameBoard)
        updateLocalStats()
    }
    
    /**
     * Updates the status text based on game state
     */
    private fun updateStatusText() {
        statusText.text = when {
            !isLocalPlayerReady && !isRemotePlayerReady -> getString(R.string.waiting_for_players)
            isLocalPlayerReady && !isRemotePlayerReady -> getString(R.string.waiting_for_opponent)
            !isLocalPlayerReady && isRemotePlayerReady -> getString(R.string.opponent_ready)
            isGameStarted -> getString(R.string.game_in_progress)
            else -> getString(R.string.both_ready)
        }
    }
    
    /**
     * Updates local game statistics on UI
     */
    private fun updateLocalStats() {
        localMovesText.text = getString(R.string.moves_format, localGameBoard.moveCount)
        localScoreText.text = getString(R.string.score_format, localGameBoard.calculateScore())
    }
    
    /**
     * Updates remote game statistics on UI (for competitive mode)
     */
    private fun updateRemoteStats(moveCount: Int, score: Int) {
        if (isCompetitiveMode) {
            remoteMovesText.text = getString(R.string.moves_format, moveCount)
            remoteScoreText.text = getString(R.string.score_format, score)
        }
    }
    
    /**
     * Updates the game timer display
     */
    private fun updateTimeDisplay() {
        val minutes = elapsedTimeSeconds / 60
        val seconds = elapsedTimeSeconds % 60
        val timeString = String.format("%02d:%02d", minutes, seconds)
        
        localTimeText.text = getString(R.string.time_format, timeString)
        
        if (isCompetitiveMode) {
            remoteTimeText.text = getString(R.string.time_format, timeString)
        }
    }
    
    /**
     * Shows a chat message in the UI
     */
    private fun displayChatMessage(sender: String, message: String) {
        val formattedMessage = "[$sender]: $message"
        
        // In a real app, you would add this to a RecyclerView or ListView
        // For simplicity, just show a toast here
        runOnUiThread {
            Toast.makeText(this, formattedMessage, Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Handle actions when the puzzle is solved
     */
    private fun handleGameSolved() {
        // Stop the game timer
        isGameStarted = false
        handler.removeCallbacks(timerRunnable)
        
        // Calculate final score
        val finalScore = localGameBoard.calculateScore()
        
        // Notify the remote player
        gameSyncManager.sendGameOver(true, finalScore)
        
        // Show completion dialog
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_solved)
            .setMessage(getString(R.string.dialog_message_solved, 
                localGameBoard.moveCount, formatTime(elapsedTimeSeconds), finalScore))
            .setPositiveButton(R.string.btn_exit) { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }
    
    /**
     * Starts the game
     */
    private fun startGame() {
        if (!isGameStarted) {
            isGameStarted = true
            elapsedTimeSeconds = 0
            
            if (isCompetitiveMode) {
                // In competitive mode, both players start with identical boards
                setupLocalGameBoard()
                gameSyncManager.startGame(
                    GameState.fromGameBoard(localGameBoard, 0, "Multiplayer Game")
                )
            } else {
                // In cooperative mode, we may want to use the host's board
                // or create a new one depending on your requirements
                setupLocalGameBoard()
                gameSyncManager.startGame(
                    GameState.fromGameBoard(localGameBoard, 0, "Cooperative Game")
                )
            }
            
            // Start the timer
            handler.post(timerRunnable)
            
            // Update UI
            readyButton.visibility = View.GONE
            updateStatusText()
        }
    }
    
    /**
     * Formats time in seconds to a readable string
     */
    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
    
    /**
     * Apply remote move to the local game board (for cooperative mode)
     */
    private fun applyRemoteMove(row: Int, col: Int) {
        if (!isCompetitiveMode) {
            // Only apply remote moves in cooperative mode
            localGameBoard.moveTile(row, col)
            localPuzzleView.invalidate()
            updateLocalStats()
        }
    }
    
    // GameSyncManager.OnGameEventListener implementation
    override fun onRemoteMove(row: Int, col: Int, result: Boolean) {
        runOnUiThread {
            if (isCompetitiveMode) {
                // In competitive mode, update opponent's puzzle view
                remoteGameBoard?.moveTile(row, col)
                remotePuzzleView.invalidate()
            } else {
                // In cooperative mode, apply the move to our board
                applyRemoteMove(row, col)
            }
        }
    }
    
    override fun onRemoteStateUpdated(gameState: GameState) {
        runOnUiThread {
            if (isCompetitiveMode) {
                // In competitive mode, update opponent's state
                remoteGameBoard = gameState.toGameBoard()
                remotePuzzleView.setGameBoard(remoteGameBoard!!)
                updateRemoteStats(gameState.moveCount, gameState.score)
            } else {
                // In cooperative mode, update our state
                localGameBoard = gameState.toGameBoard()
                localPuzzleView.setGameBoard(localGameBoard)
                elapsedTimeSeconds = gameState.elapsedTimeSeconds
                updateLocalStats()
                updateTimeDisplay()
            }
        }
    }
    
    override fun onChatMessageReceived(text: String) {
        displayChatMessage(deviceName, text)
    }
    
    override fun onRemotePlayerReady(ready: Boolean) {
        runOnUiThread {
            isRemotePlayerReady = ready
            updateStatusText()
        }
    }
    
    override fun onBothPlayersReady() {
        runOnUiThread {
            // We can now start the game
            AlertDialog.Builder(this)
                .setTitle(R.string.both_players_ready)
                .setMessage(R.string.start_game_question)
                .setPositiveButton(R.string.btn_start) { _, _ -> startGame() }
                .setNegativeButton(R.string.btn_wait, null)
                .create()
                .show()
        }
    }
    
    override fun onGameStarted(initialState: GameState) {
        runOnUiThread {
            // Set up the game with the received initial state
            localGameBoard = initialState.toGameBoard()
            localPuzzleView.setGameBoard(localGameBoard)
            
            if (isCompetitiveMode) {
                // In competitive mode, also set up the opponent's board
                remoteGameBoard = initialState.toGameBoard()
                remotePuzzleView.setGameBoard(remoteGameBoard!!)
            }
            
            isGameStarted = true
            elapsedTimeSeconds = 0
            
            // Start the timer
            handler.post(timerRunnable)
            
            // Update UI
            readyButton.visibility = View.GONE
            updateStatusText()
            updateLocalStats()
            
            Toast.makeText(this, R.string.game_started, Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onRemoteGameOver(win: Boolean, score: Int) {
        runOnUiThread {
            // Opponent has solved the puzzle
            if (isCompetitiveMode) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.opponent_won)
                    .setMessage(getString(R.string.opponent_score, score))
                    .setPositiveButton(R.string.btn_exit) { _, _ -> finish() }
                    .setCancelable(false)
                    .create()
                    .show()
            } else {
                // Cooperative mode: show that the game was solved together
                isGameStarted = false
                handler.removeCallbacks(timerRunnable)
                
                AlertDialog.Builder(this)
                    .setTitle(R.string.puzzle_solved_together)
                    .setMessage(R.string.cooperative_win_message)
                    .setPositiveButton(R.string.btn_exit) { _, _ -> finish() }
                    .setCancelable(false)
                    .create()
                    .show()
            }
        }
    }
    
    // GameSyncManager.OnConnectionEventListener implementation
    override fun onConnecting() {
        runOnUiThread {
            statusText.text = getString(R.string.status_connecting)
        }
    }
    
    override fun onConnected(deviceName: String) {
        // Already connected, nothing to do here
    }
    
    override fun onDisconnected() {
        runOnUiThread {
            Toast.makeText(this, R.string.connection_lost, Toast.LENGTH_LONG).show()
            
            // Stop the game
            isGameStarted = false
            handler.removeCallbacks(timerRunnable)
            
            // Return to the previous activity
            finish()
        }
    }
    
    override fun onError(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onBluetoothPermissionRequired() {
        runOnUiThread {
            Toast.makeText(this, R.string.bluetooth_permission_required, Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Stop the game timer
        handler.removeCallbacks(timerRunnable)
        
        // Stop the bluetooth connection
        if (::gameSyncManager.isInitialized) {
            gameSyncManager.stop()
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            // Show confirmation dialog to prevent accidental exits
            if (isGameStarted) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.confirm_exit)
                    .setMessage(R.string.confirm_exit_message)
                    .setPositiveButton(R.string.btn_exit) { _, _ -> finish() }
                    .setNegativeButton(R.string.btn_cancel, null)
                    .create()
                    .show()
            } else {
                finish()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
