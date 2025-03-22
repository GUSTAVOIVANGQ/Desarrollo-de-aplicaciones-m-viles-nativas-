package com.example.exam1;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * Utility class to help with responsive design decisions
 */
public class ResponsiveUtils {
    
    /**
     * Check if the device is a tablet based on screen size
     * @param context Application context
     * @return true if the device is a tablet
     */
    public static boolean isTablet(Context context) {
        return context.getResources().getBoolean(R.bool.isTablet);
    }
    
    /**
     * Check if the device is in landscape orientation
     * @param context Application context
     * @return true if the device is in landscape orientation
     */
    public static boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == 
                Configuration.ORIENTATION_LANDSCAPE;
    }
    
    /**
     * Get the screen width in dp
     * @param context Application context
     * @return screen width in dp
     */
    public static float getScreenWidthDp(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels / displayMetrics.density;
    }
    
    /**
     * Get the screen height in dp
     * @param context Application context
     * @return screen height in dp
     */
    public static float getScreenHeightDp(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels / displayMetrics.density;
    }
    
    /**
     * Calculate how many columns should be used in a grid based on screen width
     * @param context Application context
     * @param preferredColumnWidthDp Preferred minimum width for each column in dp
     * @return number of columns that fit in the current screen
     */
    public static int calculateOptimalColumnCount(Context context, float preferredColumnWidthDp) {
        float screenWidthDp = getScreenWidthDp(context);
        int columns = Math.max(1, Math.round(screenWidthDp / preferredColumnWidthDp));
        
        // If tablet in landscape, consider more columns for better space utilization
        if (isTablet(context) && isLandscape(context)) {
            columns = Math.max(columns, 3);
        }
        
        return columns;
    }
}
