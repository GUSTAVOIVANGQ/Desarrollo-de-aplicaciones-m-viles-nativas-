package com.example.systembooks.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "systembooks.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_SEARCH_HISTORY = "search_history";
    public static final String TABLE_FAVORITES = "favorites";

    // Common column names
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USER_ID = "user_id";

    // Search history columns
    public static final String COLUMN_QUERY = "query";
    public static final String COLUMN_SEARCH_DATE = "search_date";

    // Favorites columns
    public static final String COLUMN_BOOK_ID = "book_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_AUTHOR = "author";
    public static final String COLUMN_COVER_URL = "cover_url";
    public static final String COLUMN_DATE_ADDED = "date_added";

    // Create table statements
    private static final String CREATE_TABLE_SEARCH_HISTORY =
            "CREATE TABLE " + TABLE_SEARCH_HISTORY + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID + " INTEGER NOT NULL, " +
                    COLUMN_QUERY + " TEXT NOT NULL, " +
                    COLUMN_SEARCH_DATE + " INTEGER NOT NULL);";

    private static final String CREATE_TABLE_FAVORITES =
            "CREATE TABLE " + TABLE_FAVORITES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID + " INTEGER NOT NULL, " +
                    COLUMN_BOOK_ID + " TEXT NOT NULL, " +
                    COLUMN_TITLE + " TEXT NOT NULL, " +
                    COLUMN_AUTHOR + " TEXT NOT NULL, " +
                    COLUMN_COVER_URL + " TEXT, " +
                    COLUMN_DATE_ADDED + " INTEGER NOT NULL, " +
                    "UNIQUE(" + COLUMN_USER_ID + ", " + COLUMN_BOOK_ID + "));";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SEARCH_HISTORY);
        db.execSQL(CREATE_TABLE_FAVORITES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if they exist and create fresh ones
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEARCH_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        onCreate(db);
    }
}