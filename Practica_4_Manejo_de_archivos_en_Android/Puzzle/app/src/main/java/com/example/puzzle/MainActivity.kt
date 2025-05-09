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
        
        // Stop light sensor monitoring to save battery
        themeManager.stopLightMonitoring()
    }

    override fun onResume() {
        super.onResume()
        // Resume the game timer if a game was in progress
        if (::gameBoard.isInitialized && !gameBoard.isSolved()) {
            isGameActive = true
            handler.post(timerRunnable)
        }
        
        // Start light sensor monitoring if auto mode is enabled
        themeManager.startLightMonitoring()
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
            R.id.action_light_sensor -> {
                showLightSensorDialog()
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
        // Create a custom view with radio buttons for themes and a checkbox for auto mode
        val view = layoutInflater.inflate(R.layout.dialog_theme_settings, null)
        val radioGuinda = view.findViewById<android.widget.RadioButton>(R.id.radio_theme_guinda)
        val radioAzul = view.findViewById<android.widget.RadioButton>(R.id.radio_theme_azul)
        val checkboxAutoMode = view.findViewById<android.widget.CheckBox>(R.id.checkbox_auto_dark_mode)
        val sensorInfoText = view.findViewById<TextView>(R.id.sensor_info_text)
        
        // Set current values
        val currentTheme = themeManager.getCurrentTheme()
        when (currentTheme) {
            ThemeManager.THEME_GUINDA -> radioGuinda.isChecked = true
            ThemeManager.THEME_AZUL -> radioAzul.isChecked = true
        }
        
        // Set auto dark mode checkbox
        checkboxAutoMode.isChecked = themeManager.isAutoDarkModeEnabled()
        
        // Show current light sensor value if available
        val luxReading = themeManager.getLastLuxReading()
        val lightLevel = themeManager.getCurrentLightLevel().toString()
        if (luxReading >= 0) {
            sensorInfoText.text = "Current light: $luxReading lux ($lightLevel)"
            sensorInfoText.visibility = View.VISIBLE
        } else {
            sensorInfoText.visibility = View.GONE
        }
        
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_theme)
            .setView(view)
            .setPositiveButton(R.string.btn_apply) { _, _ ->
                // Apply theme change if needed
                val newTheme = if (radioGuinda.isChecked) {
                    ThemeManager.THEME_GUINDA
                } else {
                    ThemeManager.THEME_AZUL
                }
                
                if (newTheme != currentTheme) {
                    themeManager.setTheme(newTheme)
                    
                    // Show toast informing the user of the theme change
                    val themeName = if (radioGuinda.isChecked) getString(R.string.theme_guinda) else getString(R.string.theme_azul)
                    Toast.makeText(
                        this,
                        getString(R.string.theme_changed, themeName),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                
                // Apply auto dark mode setting
                val autoModeEnabled = checkboxAutoMode.isChecked
                if (autoModeEnabled != themeManager.isAutoDarkModeEnabled()) {
                    themeManager.setAutoDarkModeEnabled(autoModeEnabled)
                    
                    // Show toast about auto mode
                    val message = if (autoModeEnabled) {
                        "Auto dark mode enabled based on ambient light"
                    } else {
                        "Auto dark mode disabled"
                    }
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
                
                // Recreate the activity to apply the new theme
                recreate()
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .create()
            
        dialog.show()
    }
    
    /**
     * Shows a dialog for configuring the light sensor settings
     */
    private fun showLightSensorDialog() {
        // Create a custom view with light sensor settings
        val view = layoutInflater.inflate(R.layout.dialog_light_sensor_settings, null)
        
        // Get reference to UI elements
        val enableSensorCheckbox = view.findViewById<android.widget.CheckBox>(R.id.checkbox_enable_light_sensor)
        val autoDarkModeCheckbox = view.findViewById<android.widget.CheckBox>(R.id.checkbox_auto_dark_mode)
        val lightThresholdSeekbar = view.findViewById<android.widget.SeekBar>(R.id.seekbar_light_threshold)
        val thresholdValueText = view.findViewById<TextView>(R.id.text_threshold_value)
        val currentLuxText = view.findViewById<TextView>(R.id.text_current_lux)
        val currentModeText = view.findViewById<TextView>(R.id.text_current_mode)
        val settingsLayout = view.findViewById<android.view.View>(R.id.light_sensor_settings)
        
        // Set initial values
        val isAutoModeEnabled = themeManager.isAutoDarkModeEnabled()
        enableSensorCheckbox.isChecked = isAutoModeEnabled
        autoDarkModeCheckbox.isChecked = isAutoModeEnabled
        settingsLayout.visibility = if (isAutoModeEnabled) View.VISIBLE else View.GONE
        
        // Get the current threshold value (if implemented)
        // Default to 50 for now - in a real app you would save this in preferences
        val currentThreshold = 50
        lightThresholdSeekbar.progress = currentThreshold
        thresholdValueText.text = getString(R.string.light_threshold_value, currentThreshold)
        
        // Get the current light level
        val luxReading = themeManager.getLastLuxReading()
        currentLuxText.text = if (luxReading >= 0) "$luxReading lux" else "Sensor unavailable"
        
        // Set the current mode text
        currentModeText.text = if (themeManager.isDarkModeEnabled()) {
            getString(R.string.mode_dark)
        } else {
            getString(R.string.mode_light)
        }
        
        // Set up checkbox listener for enabling/disabling all settings
        enableSensorCheckbox.setOnCheckedChangeListener { _, isChecked ->
            settingsLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        
        // Set up seekbar listener
        lightThresholdSeekbar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                thresholdValueText.text = getString(R.string.light_threshold_value, progress)
            }
            
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
        
        // Set up handler to update readings
        val handler = Handler(Looper.getMainLooper())
        var sensorUpdateRunnable: Runnable? = null
        
        // Create a dialog builder
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.menu_light_sensor)
            .setView(view)
            .setPositiveButton(R.string.btn_apply) { _, _ ->
                // Save settings
                val enabled = enableSensorCheckbox.isChecked
                themeManager.setAutoDarkModeEnabled(enabled)
                
                // TODO: Save threshold value in a shared preference
                
                Toast.makeText(
                    this, 
                    if (enabled) R.string.msg_auto_mode_enabled else R.string.msg_auto_mode_disabled, 
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .create()
        
        // Create a runnable to update sensor readings while dialog is visible
        sensorUpdateRunnable = object : Runnable {
            override fun run() {
                if (dialog.isShowing) {
                    // Update lux reading
                    val luxReading = themeManager.getLastLuxReading()
                    if (luxReading >= 0) {
                        currentLuxText.text = "$luxReading lux"
                    }
                    
                    // Update current mode
                    currentModeText.text = if (themeManager.isDarkModeEnabled()) {
                        getString(R.string.mode_dark)
                    } else {
                        getString(R.string.mode_light)
                    }
                    
                    // Schedule next update
                    handler.postDelayed(this, 1000)
                }
            }
        }
        
        // Start monitoring when the dialog is shown
        dialog.setOnShowListener {
            // Start sensor monitoring
            themeManager.startLightMonitoring()
            
            // Start updating the UI
            handler.post(sensorUpdateRunnable!!)
        }
        
        // Stop monitoring when the dialog is dismissed
        dialog.setOnDismissListener {
            handler.removeCallbacks(sensorUpdateRunnable!!)
            
            // If auto mode isn't enabled, stop monitoring
            if (!themeManager.isAutoDarkModeEnabled()) {
                themeManager.stopLightMonitoring()
            }
        }
        
        dialog.show()
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