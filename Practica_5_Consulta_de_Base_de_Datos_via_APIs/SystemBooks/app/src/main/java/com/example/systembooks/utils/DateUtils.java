package com.example.systembooks.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for formatting dates
 */
public class DateUtils {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private static final SimpleDateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    
    /**
     * Format a Date object to a readable string
     * @param date The date to format
     * @return Formatted date string
     */
    public static String formatDate(Date date) {
        if (date == null) return "";
        return DATE_FORMAT.format(date);
    }
    
    /**
     * Format a Date object to a short readable string (without time)
     * @param date The date to format
     * @return Formatted date string without time
     */
    public static String formatShortDate(Date date) {
        if (date == null) return "";
        return SHORT_DATE_FORMAT.format(date);
    }
}