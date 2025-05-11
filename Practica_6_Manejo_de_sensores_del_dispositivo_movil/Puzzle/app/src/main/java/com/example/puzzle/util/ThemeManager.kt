package com.example.puzzle.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.puzzle.R

/**
 * Manages application themes and their persistence
 */
class ThemeManager(private val context: Context) {

    companion object {
        const val THEME_GUINDA = "guinda"
        const val THEME_AZUL = "azul"
        
        private const val PREFS_NAME = "puzzle_settings"
        private const val KEY_THEME = "theme"
        private const val KEY_AUTO_DARK_MODE = "auto_dark_mode"
        
        // Singleton instance
        @Volatile
        private var INSTANCE: ThemeManager? = null
        
        fun getInstance(context: Context): ThemeManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThemeManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val preferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // Light sensor manager for auto dark mode
    private val lightSensorManager: LightSensorManager by lazy { LightSensorManager.getInstance(context) }
    
    init {
        // Set up the light sensor callback
        lightSensorManager.setOnLightLevelChangedListener { lightLevel ->
            if (isAutoDarkModeEnabled()) {
                // Change to dark mode in low light conditions
                when (lightLevel) {
                    LightSensorManager.LightLevel.DARK, LightSensorManager.LightLevel.DIM -> {
                        setDarkMode(true)
                    }
                    LightSensorManager.LightLevel.NORMAL, 
                    LightSensorManager.LightLevel.BRIGHT,
                    LightSensorManager.LightLevel.VERY_BRIGHT -> {
                        setDarkMode(false)
                    }
                }
            }
        }
        
        // Initialize auto dark mode if enabled
        if (isAutoDarkModeEnabled()) {
            lightSensorManager.setAutoModeEnabled(true)
        }
    }
    
    /**
     * Get the current theme
     */
    fun getCurrentTheme(): String {
        return preferences.getString(KEY_THEME, THEME_AZUL) ?: THEME_AZUL
    }
    
    /**
     * Apply the saved theme to an activity
     */
    fun applyTheme(activity: AppCompatActivity) {
        when (getCurrentTheme()) {
            THEME_GUINDA -> activity.setTheme(R.style.Theme_Puzzle_Guinda)
            THEME_AZUL -> activity.setTheme(R.style.Theme_Puzzle_Azul)
            else -> activity.setTheme(R.style.Theme_Puzzle_Azul)
        }
    }
    
    /**
     * Change the theme and save the preference
     */
    fun setTheme(themeName: String) {
        preferences.edit().putString(KEY_THEME, themeName).apply()
    }
    
    /**
     * Get the display name of the current theme
     */
    fun getCurrentThemeName(): String {
        return when (getCurrentTheme()) {
            THEME_GUINDA -> context.getString(R.string.theme_guinda)
            THEME_AZUL -> context.getString(R.string.theme_azul)
            else -> context.getString(R.string.theme_azul)
        }
    }
    
    /**
     * Toggle between light and dark mode
     */
    fun toggleDarkMode(isNightMode: Boolean) {
        val mode = if (isNightMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
    
    /**
     * Check if dark mode is currently enabled
     */
    fun isDarkModeEnabled(): Boolean {
        return when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO -> false
            else -> {
                // Check the current configuration
                val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                currentNightMode == Configuration.UI_MODE_NIGHT_YES
            }
        }
    }
    
    /**
     * Set the dark mode state directly
     */
    fun setDarkMode(isNightMode: Boolean) {
        val mode = if (isNightMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
    
    /**
     * Enable or disable automatic dark mode based on ambient light
     */
    fun setAutoDarkModeEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_AUTO_DARK_MODE, enabled).apply()
        lightSensorManager.setAutoModeEnabled(enabled)
        
        // If disabling auto mode, respect the current manually set mode
        if (!enabled) {
            // Do nothing - keep the current dark/light mode setting
        } else {
            // Start immediate monitoring to set the correct theme based on current light
            lightSensorManager.startMonitoring()
        }
    }
    
    /**
     * Check if automatic dark mode is enabled
     */
    fun isAutoDarkModeEnabled(): Boolean {
        return preferences.getBoolean(KEY_AUTO_DARK_MODE, false)
    }
    
    /**
     * Get the current light level from the sensor
     */
    fun getCurrentLightLevel(): LightSensorManager.LightLevel {
        return lightSensorManager.getCurrentLightLevel()
    }
    
    /**
     * Get the last light reading in lux
     */
    fun getLastLuxReading(): Float {
        return lightSensorManager.getLastLuxReading()
    }
    
    /**
     * Start monitoring ambient light (should be called in onResume)
     */
    fun startLightMonitoring() {
        if (isAutoDarkModeEnabled()) {
            lightSensorManager.startMonitoring()
        }
    }
    
    /**
     * Stop monitoring ambient light (should be called in onPause)
     */
    fun stopLightMonitoring() {
        lightSensorManager.stopMonitoring()
    }
}
