package com.example.filemanager

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

/**
 * Manages theme settings for the application
 */
object ThemeManager {
    private const val PREFS_NAME = "theme_preferences"
    private const val PREF_THEME = "selected_theme"
    
    enum class Theme(val themeResId: Int) {
        IPN(R.style.Theme_FileManager_IPN),
        ESCOM(R.style.Theme_FileManager_ESCOM)
    }
    
    /**
     * Apply the saved theme to an activity
     */
    fun applyTheme(activity: AppCompatActivity) {
        val theme = getTheme(activity)
        activity.setTheme(theme.themeResId)
    }
    
    /**
     * Get the currently saved theme
     */
    fun getTheme(context: Context): Theme {
        val preferences = getPreferences(context)
        val themeName = preferences.getString(PREF_THEME, Theme.IPN.name)
        return try {
            Theme.valueOf(themeName ?: Theme.IPN.name)
        } catch (e: IllegalArgumentException) {
            Theme.IPN
        }
    }
    
    /**
     * Save and apply a new theme
     */
    fun setTheme(context: Context, theme: Theme) {
        val preferences = getPreferences(context)
        preferences.edit().putString(PREF_THEME, theme.name).apply()
    }
    
    /**
     * Get the theme shared preferences
     */
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
