package com.example.systembooks.database;

import android.provider.BaseColumns;

/**
 * Contract class for the favorites table in the database.
 */
public final class FavoritesContract {
    
    // To prevent someone from accidentally instantiating the contract class
    private FavoritesContract() {}
    
    // Table name
    public static final String TABLE_NAME = "favorites";
    
    // Column names
    public static class Columns implements BaseColumns {
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_BOOK_ID = "book_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_COVER_URL = "cover_url";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }
    
    // SQL to create the table
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Columns.COLUMN_USER_ID + " INTEGER NOT NULL, " +
                    Columns.COLUMN_BOOK_ID + " TEXT NOT NULL, " +
                    Columns.COLUMN_TITLE + " TEXT NOT NULL, " +
                    Columns.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                    Columns.COLUMN_COVER_URL + " TEXT, " +
                    Columns.COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
                    "UNIQUE(" + Columns.COLUMN_USER_ID + ", " + Columns.COLUMN_BOOK_ID + "))";
}