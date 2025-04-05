package com.example.puzzle.util

import android.content.Context
import android.content.SharedPreferences
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
            else -> false
        }
    }
}
