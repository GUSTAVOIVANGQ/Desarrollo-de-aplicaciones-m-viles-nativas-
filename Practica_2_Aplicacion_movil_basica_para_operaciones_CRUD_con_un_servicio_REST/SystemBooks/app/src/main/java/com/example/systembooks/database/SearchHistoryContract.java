package com.example.systembooks.database;

import android.provider.BaseColumns;

/**
 * Contract class for the search history table in the database.
 */
public final class SearchHistoryContract {
    
    // To prevent someone from accidentally instantiating the contract class
    private SearchHistoryContract() {}
    
    // Table name
    public static final String TABLE_NAME = "search_history";
    
    // Column names
    public static class Columns implements BaseColumns {
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_QUERY = "query";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }
    
    // SQL to create the table
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Columns.COLUMN_USER_ID + " INTEGER NOT NULL, " +
                    Columns.COLUMN_QUERY + " TEXT NOT NULL, " +
                    Columns.COLUMN_TIMESTAMP + " INTEGER NOT NULL)";
}