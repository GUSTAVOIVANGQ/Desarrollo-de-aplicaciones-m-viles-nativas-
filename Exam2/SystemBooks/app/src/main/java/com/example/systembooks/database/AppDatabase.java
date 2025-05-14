package com.example.systembooks.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * SQLite database helper class for managing search history and favorite books
 */
public class AppDatabase extends SQLiteOpenHelper {

    private static final String TAG = "AppDatabase";
    private static final String DATABASE_NAME = "systembooks.db";
    private static final int DATABASE_VERSION = 1;

    // Singleton instance
    private static AppDatabase instance;

    // Get singleton instance
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new AppDatabase(context.getApplicationContext());
        }
        return instance;
    }

    // Private constructor to enforce singleton pattern
    private AppDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create search history table
        db.execSQL(SearchHistoryContract.CREATE_TABLE);
        
        // Create favorites table
        db.execSQL(FavoritesContract.CREATE_TABLE);
        
        Log.i(TAG, "Database tables created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // If we need to upgrade the database in the future, we would handle that here
        // For now, we'll just drop tables and recreate them
        db.execSQL("DROP TABLE IF EXISTS " + SearchHistoryContract.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FavoritesContract.TABLE_NAME);
        onCreate(db);
    }
}