package com.example.systembooks.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.systembooks.database.DatabaseHelper;
import com.example.systembooks.models.SearchHistoryItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SearchHistoryRepository {
    private final DatabaseHelper dbHelper;
    
    public SearchHistoryRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }
    
    public void saveSearchQuery(long userId, String query) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_USER_ID, userId);
            values.put(DatabaseHelper.COLUMN_QUERY, query);
            values.put(DatabaseHelper.COLUMN_SEARCH_DATE, new Date().getTime());
            
            db.insert(DatabaseHelper.TABLE_SEARCH_HISTORY, null, values);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }
    
    public List<SearchHistoryItem> getSearchHistory(long userId) {
        List<SearchHistoryItem> historyItems = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = DatabaseHelper.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        String orderBy = DatabaseHelper.COLUMN_SEARCH_DATE + " DESC";
        
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_SEARCH_HISTORY,
                null,
                selection,
                selectionArgs,
                null,
                null,
                orderBy
        );
        
        try {
            int idColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
            int userIdColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID);
            int queryColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_QUERY);
            int dateColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_SEARCH_DATE);
            
            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumnIndex);
                long historyUserId = cursor.getLong(userIdColumnIndex);
                String query = cursor.getString(queryColumnIndex);
                long timestamp = cursor.getLong(dateColumnIndex);
                
                SearchHistoryItem item = new SearchHistoryItem(
                        id, historyUserId, query, new Date(timestamp)
                );
                historyItems.add(item);
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return historyItems;
    }
    // Method to clear search history for a specific user
    // This method returns true if the history was cleared successfully, false otherwise
    // Note: The method should be fixed to return a boolean value
    public boolean clearSearchHistory(long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            String whereClause = DatabaseHelper.COLUMN_USER_ID + " = ?";
            String[] whereArgs = {String.valueOf(userId)};
            
            db.delete(DatabaseHelper.TABLE_SEARCH_HISTORY, whereClause, whereArgs);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return false;
    }
}