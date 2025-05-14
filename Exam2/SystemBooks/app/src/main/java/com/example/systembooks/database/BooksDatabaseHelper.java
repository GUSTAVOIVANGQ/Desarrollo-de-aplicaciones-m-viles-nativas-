package com.example.systembooks.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper class for managing the local SQLite database used to store
 * search history and favorite books.
 */
public class BooksDatabaseHelper extends SQLiteOpenHelper {
    
    // Database Info
    private static final String DATABASE_NAME = "BooksDatabase";
    private static final int DATABASE_VERSION = 1;
    
    // Singleton instance
    private static BooksDatabaseHelper instance;
    
    public static synchronized BooksDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new BooksDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }
    
    private BooksDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables
        db.execSQL(SearchHistoryContract.CREATE_TABLE);
        db.execSQL(FavoritesContract.CREATE_TABLE);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop old tables and recreate
        db.execSQL("DROP TABLE IF EXISTS " + SearchHistoryContract.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FavoritesContract.TABLE_NAME);
        onCreate(db);
    }
}