package com.example.exam1;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {
    
    private static final String PREFERENCES_FILE = "theme_pref";
    private static final String KEY_THEME = "app_theme";
    
    // Theme constants
    public static final int THEME_GUINDA = 0;
    public static final int THEME_ESCOM = 1;
    
    // Get current theme
    public static int getTheme(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_THEME, THEME_GUINDA); // Default to Guinda theme
    }
    
    // Save theme selection
    public static void setTheme(Context context, int themeId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_THEME, themeId);
        editor.apply();
    }
    
    // Apply theme to activity
    public static void applyTheme(AppCompatActivity activity) {
        int themeId = getTheme(activity);
        switch (themeId) {
            case THEME_ESCOM:
                activity.setTheme(R.style.Theme_Exam1_Escom);
                break;
            case THEME_GUINDA:
            default:
                activity.setTheme(R.style.Theme_Exam1_Guinda);
                break;
        }
    }
}
