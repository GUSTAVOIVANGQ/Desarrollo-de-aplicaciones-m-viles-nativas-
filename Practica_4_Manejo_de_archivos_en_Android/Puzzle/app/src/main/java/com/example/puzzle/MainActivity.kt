package com.example.puzzle

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.puzzle.models.GameBoard
import com.example.puzzle.models.GameState
import com.example.puzzle.storage.GameStateStorage
import com.example.puzzle.storage.GameStateStorageFactory
import com.example.puzzle.storage.StorageFormat
import com.example.puzzle.util.ThemeManager
import com.example.puzzle.views.PuzzleView
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var puzzleView: PuzzleView
    private lateinit var movesTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var scoreTextView: TextView
    private lateinit var newGameButton: Button
    private lateinit var difficultyButton: Button

    private lateinit var gameBoard: GameBoard
    private var currentDifficulty = GameBoard.GameDifficulty.EASY
    private var elapsedTimeSeconds = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timerRunnable: Runnable
    private var isGameActive = false
    
    // Game state persistence
    private var currentStorageFormat = StorageFormat.TEXT
    private var currentStorage: GameStateStorage = GameStateStorageFactory.getStorage(StorageFormat.TEXT)
    
    // Theme management
    private lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply the theme before calling super.onCreate or setContentView
        themeManager = ThemeManager.getInstance(this)
        themeManager.applyTheme(this)
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI elements
        puzzleView = findViewById(R.id.puzzleView)
        movesTextView = findViewById(R.id.movesTextView)
        timeTextView = findViewById(R.id.timeTextView)
        scoreTextView = findViewById(R.id.scoreTextView)
        newGameButton = findViewById(R.id.newGameButton)
        difficultyButton = findViewById(R.id.difficultyButton)

        // Set up button listeners
        newGameButton.setOnClickListener { startNewGame() }
        difficultyButton.setOnClickListener { showDifficultyDialog() }

        // Set up tile move callback
        puzzleView.onTileMoved = { isSolved ->
            updateGameStats()
            if (isSolved) {
                handleGameSolved()
            }
        }

        // Create timer runnable
        timerRunnable = object : Runnable {
            override fun run() {
                if (isGameActive) {
                    elapsedTimeSeconds++
                    updateTimeDisplay()
                    handler.postDelayed(this, 1000)
                }
            }
        }

        // Start the game
        startNewGame()
    }

    private fun startNewGame() {
        // Stop any existing timer
        handler.removeCallbacks(timerRunnable)
        
        // Reset time and start new timer
        elapsedTimeSeconds = 0
        
        // Create new game board with the current difficulty
        val gridSize = if (currentDifficulty == GameBoard.GameDifficulty.EASY) 3 else 4
        gameBoard = GameBoard(gridSize, currentDifficulty)
        
        // Set game board to puzzle view
        puzzleView.setGameBoard(gameBoard)
        
        // Update UI
        updateGameStats()
        
        // Start the game timer
        isGameActive = true
        handler.post(timerRunnable)
    }

    private fun updateGameStats() {
        // Update moves text
        movesTextView.text = "Moves: ${gameBoard.moveCount}"
        
        // Update score text
        val currentScore = gameBoard.calculateScore()
        scoreTextView.text = "Score: $currentScore"
        
        // Time is updated by the timer runnable
    }

    private fun updateTimeDisplay() {
        val minutes = elapsedTimeSeconds / 60
        val seconds = elapsedTimeSeconds % 60
        timeTextView.text = "Time: ${String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)}"
        
        // Update score as time changes
        val currentScore = gameBoard.calculateScore()
        scoreTextView.text = "Score: $currentScore"
    }

    private fun handleGameSolved() {
        // Stop the timer
        isGameActive = false
        handler.removeCallbacks(timerRunnable)
        
        // Calculate final score
        val finalScore = gameBoard.calculateScore()
        
        // Show completion dialog
        AlertDialog.Builder(this)
            .setTitle("Puzzle Solved!")
            .setMessage("Congratulations! You solved the puzzle!\n\nMoves: ${gameBoard.moveCount}\nTime: ${formatTime(elapsedTimeSeconds)}\nScore: $finalScore")
            .setPositiveButton("New Game") { _, _ -> startNewGame() }
            .setNegativeButton("Change Difficulty") { _, _ -> showDifficultyDialog() }
            .setCancelable(false)
            .show()
    }

    private fun showDifficultyDialog() {
        val difficulties = arrayOf("Easy (3x3)", "Hard (4x4)")
        val currentSelection = if (currentDifficulty == GameBoard.GameDifficulty.EASY) 0 else 1
        
        AlertDialog.Builder(this)
            .setTitle("Select Difficulty")
            .setSingleChoiceItems(difficulties, currentSelection) { dialog, which ->
                currentDifficulty = if (which == 0) {
                    GameBoard.GameDifficulty.EASY
                } else {
                    GameBoard.GameDifficulty.HARD
                }
                dialog.dismiss()
                startNewGame()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
    }

    override fun onPause() {
        super.onPause()
        // Pause the game timer when the app is paused
        isGameActive = false
        handler.removeCallbacks(timerRunnable)
    }

    override fun onResume() {
        super.onResume()
        // Resume the game timer if a game was in progress
        if (::gameBoard.isInitialized && !gameBoard.isSolved()) {
            isGameActive = true
            handler.post(timerRunnable)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_new_game -> {
                startNewGame()
                true
            }
            R.id.action_difficulty -> {
                showDifficultyDialog()
                true
            }
            R.id.action_save_game -> {
                showSaveGameDialog()
                true
            }
            R.id.action_load_game -> {
                showLoadGameDialog()
                true
            }
            R.id.action_saved_games -> {
                showSavedGamesDialog()
                true
            }
            R.id.action_save_format -> {
                showSaveFormatDialog()
                true
            }
            R.id.action_theme -> {
                showThemeDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    // Game state persistence methods
    
    /**
     * Shows a dialog for saving the current game state.
     */
    private fun showSaveGameDialog() {
        // Pause the game while saving
        val wasActive = isGameActive
        isGameActive = false
        handler.removeCallbacks(timerRunnable)
        
        // Create an edit text for the game name
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.hint = getString(R.string.dialog_hint_game_name)
        
        // Suggest a default name based on date, time, and difficulty
        val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
        val diffText = if (currentDifficulty == GameBoard.GameDifficulty.EASY) "Easy" else "Hard"
        input.setText("Puzzle $diffText - ${dateFormat.format(Date())}")
        
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_save_game)
            .setView(input)
            .setPositiveButton(R.string.btn_save) { _, _ ->
                // Create a GameState from the current board
                val gameState = GameState.fromGameBoard(
                    gameBoard = gameBoard,
                    elapsedTimeSeconds = elapsedTimeSeconds,
                    gameTitle = input.text.toString()
                )
                
                // Save the game
                val savedFile = currentStorage.saveGame(this, gameState)
                
                Toast.makeText(this, getString(R.string.msg_game_saved), Toast.LENGTH_SHORT).show()
                
                // Resume game if it was active before
                if (wasActive) {
                    isGameActive = true
                    handler.post(timerRunnable)
                }
            }
            .setNegativeButton(R.string.btn_cancel) { _, _ ->
                // Resume game if it was active before
                if (wasActive) {
                    isGameActive = true
                    handler.post(timerRunnable)
                }
            }
            .show()
    }
    
    /**
     * Shows a dialog for loading a saved game state.
     */
    private fun showLoadGameDialog() {
        // Get all saved games for the current format
        val savedGames = currentStorage.getSavedGames(this)
        
        if (savedGames.isEmpty()) {
            Toast.makeText(this, getString(R.string.msg_no_saved_games), Toast.LENGTH_SHORT).show()
            return
        }
        
        // Pause the game while loading
        isGameActive = false
        handler.removeCallbacks(timerRunnable)
        
        // Create an array of items to show in the list
        val items = savedGames.map { file ->
            try {
                // Try to load enough of the game state to show the title and info
                val gameState = currentStorage.loadGame(this, file)
                val diffText = if (gameState.difficulty == GameBoard.GameDifficulty.EASY) "Easy" else "Hard"
                val dateFormat = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())
                val dateStr = dateFormat.format(Date(gameState.timestamp))
                
                getString(
                    R.string.format_saved_game_item,
                    gameState.gameTitle,
                    diffText,
                    gameState.moveCount,
                    gameState.score,
                    dateStr
                )
            } catch (e: Exception) {
                // If we can't load the file info, just show the filename
                "Error loading info: ${file.name}"
            }
        }.toTypedArray()
        
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_load_game)
            .setItems(items) { _, which ->
                try {
                    val gameState = currentStorage.loadGame(this, savedGames[which])
                    
                    // Load the game state
                    elapsedTimeSeconds = gameState.elapsedTimeSeconds
                    gameBoard = gameState.toGameBoard()
                    currentDifficulty = gameState.difficulty
                    
                    // Update the UI
                    puzzleView.setGameBoard(gameBoard)
                    updateGameStats()
                    updateTimeDisplay()
                    
                    // Start the timer if the game isn't already solved
                    if (!gameBoard.isSolved()) {
                        isGameActive = true
                        handler.post(timerRunnable)
                    }
                    
                    Toast.makeText(this, getString(R.string.msg_game_loaded), Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error loading game: ${e.message}", Toast.LENGTH_LONG).show()
                    
                    // Start a new game if loading fails
                    startNewGame()
                }
            }
            .setNegativeButton(R.string.btn_cancel) { _, _ ->
                // Resume the game if it wasn't solved
                if (::gameBoard.isInitialized && !gameBoard.isSolved()) {
                    isGameActive = true
                    handler.post(timerRunnable)
                }
            }
            .show()
    }
    
    /**
     * Shows a dialog for managing saved games across all formats.
     */
    private fun showSavedGamesDialog() {
        // Get all saved games for all formats
        val allSavedGames = mutableListOf<Pair<File, StorageFormat>>()
        
        GameStateStorageFactory.getAllStorages().forEach { storage ->
            val format = when (storage.getFileExtension()) {
                "txt" -> StorageFormat.TEXT
                "xml" -> StorageFormat.XML
                "json" -> StorageFormat.JSON
                else -> StorageFormat.TEXT
            }
            
            storage.getSavedGames(this).forEach { file ->
                allSavedGames.add(Pair(file, format))
            }
        }
        
        if (allSavedGames.isEmpty()) {
            Toast.makeText(this, getString(R.string.msg_no_saved_games), Toast.LENGTH_SHORT).show()
            return
        }
        
        // Pause the game while in the dialog
        val wasActive = isGameActive
        isGameActive = false
        handler.removeCallbacks(timerRunnable)
        
        // Inflate a custom view with a ListView
        val view = layoutInflater.inflate(R.layout.dialog_saved_games, null)
        val listView = view.findViewById<ListView>(R.id.saved_games_list)
        
        // Create an array of items to show in the list
        val items = allSavedGames.map { (file, format) ->
            try {
                // Try to load enough of the game state to show title and info
                val storage = GameStateStorageFactory.getStorage(format)
                val gameState = storage.loadGame(this, file)
                val diffText = if (gameState.difficulty == GameBoard.GameDifficulty.EASY) "Easy" else "Hard"
                val dateFormat = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())
                val dateStr = dateFormat.format(Date(gameState.timestamp))
                
                "${gameState.gameTitle} (${storage.getFormatName()})\n$diffText • Moves: ${gameState.moveCount} • Score: ${gameState.score}\nSaved: $dateStr"
            } catch (e: Exception) {
                // If we can't load the file info, just show the filename
                "Error loading info: ${file.name}"
            }
        }.toTypedArray()
        
        // Set up the adapter
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        listView.adapter = adapter
        
        val dialog = AlertDialog.Builder(this)
            .setTitle("Saved Games")
            .setView(view)
            .setNegativeButton("Close") { _, _ ->
                // Resume game if it was active before
                if (wasActive) {
                    isGameActive = true
                    handler.post(timerRunnable)
                }
            }
            .create()
        
        // Set up item click handling
        listView.setOnItemClickListener { _, _, position, _ ->
            val (file, format) = allSavedGames[position]
            val storage = GameStateStorageFactory.getStorage(format)
            
            // Show options for this save file
            AlertDialog.Builder(this)
                .setTitle("Options")
                .setItems(arrayOf("Load", "View Content", "Delete")) { _, which ->
                    when (which) {
                        0 -> { // Load
                            dialog.dismiss()
                            try {
                                val gameState = storage.loadGame(this, file)
                                
                                // Load the game state
                                elapsedTimeSeconds = gameState.elapsedTimeSeconds
                                gameBoard = gameState.toGameBoard()
                                currentDifficulty = gameState.difficulty
                                
                                // Update the UI
                                puzzleView.setGameBoard(gameBoard)
                                updateGameStats()
                                updateTimeDisplay()
                                
                                // Start the timer if the game isn't already solved
                                if (!gameBoard.isSolved()) {
                                    isGameActive = true
                                    handler.post(timerRunnable)
                                }
                                
                                Toast.makeText(this, getString(R.string.msg_game_loaded), Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(this, "Error loading game: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                        1 -> { // View Content
                            try {
                                // Read the file content
                                val content = BufferedReader(FileReader(file)).use { reader ->
                                    reader.readText()
                                }
                                
                                // Show the content in a dialog
                                AlertDialog.Builder(this)
                                    .setTitle(R.string.dialog_title_file_content)
                                    .setMessage(content)
                                    .setPositiveButton("OK", null)
                                    .show()
                            } catch (e: Exception) {
                                Toast.makeText(this, "Error reading file: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                        2 -> { // Delete
                            AlertDialog.Builder(this)
                                .setTitle("Confirm Delete")
                                .setMessage("Are you sure you want to delete this saved game?")
                                .setPositiveButton("Delete") { _, _ ->
                                    if (file.delete()) {
                                        Toast.makeText(this, getString(R.string.msg_game_deleted), Toast.LENGTH_SHORT).show()
                                        
                                        // Remove the item from the list and update the adapter
                                        allSavedGames.removeAt(position)
                                        val newItems = allSavedGames.map { (file, format) ->
                                            try {
                                                val storage = GameStateStorageFactory.getStorage(format)
                                                val gameState = storage.loadGame(this, file)
                                                val diffText = if (gameState.difficulty == GameBoard.GameDifficulty.EASY) "Easy" else "Hard"
                                                val dateFormat = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())
                                                val dateStr = dateFormat.format(Date(gameState.timestamp))
                                                
                                                "${gameState.gameTitle} (${storage.getFormatName()})\n$diffText • Moves: ${gameState.moveCount} • Score: ${gameState.score}\nSaved: $dateStr"
                                            } catch (e: Exception) {
                                                "Error loading info: ${file.name}"
                                            }
                                        }.toTypedArray()
                                        
                                        adapter.clear()
                                        adapter.addAll(*newItems)
                                        adapter.notifyDataSetChanged()
                                        
                                        // If no items left, close the dialog
                                        if (allSavedGames.isEmpty()) {
                                            dialog.dismiss()
                                            Toast.makeText(this, getString(R.string.msg_no_saved_games), Toast.LENGTH_SHORT).show()
                                            
                                            // Resume game if it was active before
                                            if (wasActive) {
                                                isGameActive = true
                                                handler.post(timerRunnable)
                                            }
                                        }
                                    } else {
                                        Toast.makeText(this, "Failed to delete file", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .setNegativeButton("Cancel", null)
                                .show()
                        }
                    }
                }
                .show()
        }
        
        dialog.show()
    }
    
    /**
     * Shows a dialog for selecting the save format.
     */
    private fun showSaveFormatDialog() {
        val formats = arrayOf("Plain Text (.txt)", "XML (.xml)", "JSON (.json)")
        val currentSelection = when(currentStorageFormat) {
            StorageFormat.TEXT -> 0
            StorageFormat.XML -> 1
            StorageFormat.JSON -> 2
        }
        
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_save_format)
            .setSingleChoiceItems(formats, currentSelection) { dialog, which ->
                currentStorageFormat = when (which) {
                    0 -> StorageFormat.TEXT
                    1 -> StorageFormat.XML
                    2 -> StorageFormat.JSON
                    else -> StorageFormat.TEXT
                }
                currentStorage = GameStateStorageFactory.getStorage(currentStorageFormat)
                dialog.dismiss()
                
                // Show a toast with the selected format
                Toast.makeText(this, "Save format set to: ${formats[which]}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }
    
    /**
     * Shows a dialog for selecting the application theme
     */
    private fun showThemeDialog() {
        val themes = arrayOf(
            getString(R.string.theme_guinda),
            getString(R.string.theme_azul)
        )
        
        val currentTheme = themeManager.getCurrentTheme()
        val currentSelection = when (currentTheme) {
            ThemeManager.THEME_GUINDA -> 0
            ThemeManager.THEME_AZUL -> 1
            else -> 1
        }
        
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_theme)
            .setSingleChoiceItems(themes, currentSelection) { dialog, which ->
                val newTheme = when (which) {
                    0 -> ThemeManager.THEME_GUINDA
                    1 -> ThemeManager.THEME_AZUL
                    else -> ThemeManager.THEME_AZUL
                }
                
                if (newTheme != currentTheme) {
                    themeManager.setTheme(newTheme)
                    
                    // Show toast informing the user of the theme change
                    val themeName = if (which == 0) getString(R.string.theme_guinda) else getString(R.string.theme_azul)
                    Toast.makeText(
                        this,
                        getString(R.string.theme_changed, themeName),
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Recreate the activity to apply the new theme
                    recreate()
                }
                
                dialog.dismiss()
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }
    
    /**
     * Handle configuration changes like orientation or theme changes
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        
        // Check if the night mode configuration has changed
        val currentNightMode = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
        
        // Update the UI based on the new configuration
        updateUIForConfiguration(currentNightMode == Configuration.UI_MODE_NIGHT_YES)
    }
    
    /**
     * Update UI elements for the current configuration
     */
    private fun updateUIForConfiguration(isNightMode: Boolean) {
        // Update puzzle view colors for the new theme
        puzzleView.updateThemeColors()
        
        // Get the background color from the theme
        val typedValue = TypedValue()
        theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true)
        val backgroundColor = typedValue.data
        
        // Find the main layout and set its background
        val mainLayout = findViewById<View>(R.id.main)
        mainLayout.setBackgroundColor(backgroundColor)
        
        // We could also update other UI elements that need special handling
        // for the current theme or night mode here
    }
}