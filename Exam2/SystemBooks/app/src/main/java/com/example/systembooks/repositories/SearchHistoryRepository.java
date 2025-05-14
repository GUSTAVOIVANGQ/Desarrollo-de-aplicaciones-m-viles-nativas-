package com.example.systembooks.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.systembooks.database.DatabaseHelper;
import com.example.systembooks.models.SearchHistoryItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchHistoryRepository {
    private static final String TAG = "SearchHistoryRepository";
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
    
    /**
     * Get search history for all users (admin function)
     * @return Map of user IDs to lists of their search history items
     */
    public Map<Long, List<SearchHistoryItem>> getAllUsersSearchHistory() {
        Map<Long, List<SearchHistoryItem>> allHistory = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        
        try {
            String orderBy = DatabaseHelper.COLUMN_USER_ID + " ASC, " 
                           + DatabaseHelper.COLUMN_SEARCH_DATE + " DESC";
            
            cursor = db.query(
                    DatabaseHelper.TABLE_SEARCH_HISTORY,
                    null,
                    null,
                    null,
                    null,
                    null,
                    orderBy
            );
            
            int idColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
            int userIdColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID);
            int queryColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_QUERY);
            int dateColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_SEARCH_DATE);
            
            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumnIndex);
                long userId = cursor.getLong(userIdColumnIndex);
                String query = cursor.getString(queryColumnIndex);
                long timestamp = cursor.getLong(dateColumnIndex);
                
                SearchHistoryItem item = new SearchHistoryItem(
                        id, userId, query, new Date(timestamp)
                );
                
                if (!allHistory.containsKey(userId)) {
                    allHistory.put(userId, new ArrayList<>());
                }
                allHistory.get(userId).add(item);
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "Error getting all users search history", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return allHistory;
    }
}