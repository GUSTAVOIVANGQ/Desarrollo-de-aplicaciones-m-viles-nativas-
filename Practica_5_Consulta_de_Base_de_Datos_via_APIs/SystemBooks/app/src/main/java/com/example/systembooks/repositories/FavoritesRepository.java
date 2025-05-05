package com.example.systembooks.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.systembooks.database.DatabaseHelper;
import com.example.systembooks.models.Book;
import com.example.systembooks.models.FavoriteBook;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoritesRepository {
    private static final String TAG = "FavoritesRepository";
    private final DatabaseHelper dbHelper;
    
    public FavoritesRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }
    
    public boolean addToFavorites(long userId, Book book) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_USER_ID, userId);
            values.put(DatabaseHelper.COLUMN_BOOK_ID, book.getId());
            values.put(DatabaseHelper.COLUMN_TITLE, book.getTitle());
            values.put(DatabaseHelper.COLUMN_AUTHOR, book.getAuthor());
            values.put(DatabaseHelper.COLUMN_COVER_URL, book.getCoverUrl());
            values.put(DatabaseHelper.COLUMN_DATE_ADDED, new Date().getTime());
            
            long result = db.insert(DatabaseHelper.TABLE_FAVORITES, null, values);
            return result != -1;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error adding book to favorites", e);
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }
    
    public boolean removeFromFavorites(long userId, String bookId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            String whereClause = DatabaseHelper.COLUMN_USER_ID + " = ? AND " + 
                    DatabaseHelper.COLUMN_BOOK_ID + " = ?";
            String[] whereArgs = {String.valueOf(userId), bookId};
            
            int count = db.delete(DatabaseHelper.TABLE_FAVORITES, whereClause, whereArgs);
            return count > 0;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error removing book from favorites", e);
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }
    
    public boolean isFavorite(long userId, String bookId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        
        try {
            String selection = DatabaseHelper.COLUMN_USER_ID + " = ? AND " + 
                    DatabaseHelper.COLUMN_BOOK_ID + " = ?";
            String[] selectionArgs = {String.valueOf(userId), bookId};
            
            cursor = db.query(
                    DatabaseHelper.TABLE_FAVORITES,
                    new String[]{DatabaseHelper.COLUMN_ID},
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );
            
            return cursor.getCount() > 0;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error checking if book is favorite", e);
            return false;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }
    
    public List<FavoriteBook> getFavorites(long userId) {
        List<FavoriteBook> favorites = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        
        try {
            String selection = DatabaseHelper.COLUMN_USER_ID + " = ?";
            String[] selectionArgs = {String.valueOf(userId)};
            String orderBy = DatabaseHelper.COLUMN_DATE_ADDED + " DESC";
            
            cursor = db.query(
                    DatabaseHelper.TABLE_FAVORITES,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    orderBy
            );
            
            int idColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
            int userIdColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID);
            int bookIdColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_BOOK_ID);
            int titleColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TITLE);
            int authorColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_AUTHOR);
            int coverUrlColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_COVER_URL);
            int dateAddedColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE_ADDED);
            
            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumnIndex);
                long favoriteUserId = cursor.getLong(userIdColumnIndex);
                String bookId = cursor.getString(bookIdColumnIndex);
                String title = cursor.getString(titleColumnIndex);
                String author = cursor.getString(authorColumnIndex);
                String coverUrl = cursor.getString(coverUrlColumnIndex);
                long timestamp = cursor.getLong(dateAddedColumnIndex);
                
                FavoriteBook favoriteBook = new FavoriteBook(
                        id, favoriteUserId, bookId, title, author, coverUrl, new Date(timestamp)
                );
                favorites.add(favoriteBook);
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "Error getting favorites", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return favorites;
    }
    
    /**
     * Get all favorites for all users (admin function)
     * @return Map of user IDs to lists of their favorite books
     */
    public Map<Long, List<FavoriteBook>> getAllUsersFavorites() {
        Map<Long, List<FavoriteBook>> allFavorites = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        
        try {
            // No selection needed - we want all favorites
            String orderBy = DatabaseHelper.COLUMN_USER_ID + " ASC, " 
                           + DatabaseHelper.COLUMN_DATE_ADDED + " DESC";
            
            cursor = db.query(
                    DatabaseHelper.TABLE_FAVORITES,
                    null,
                    null,
                    null,
                    null,
                    null,
                    orderBy
            );
            
            int idColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
            int userIdColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID);
            int bookIdColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_BOOK_ID);
            int titleColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TITLE);
            int authorColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_AUTHOR);
            int coverUrlColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_COVER_URL);
            int dateAddedColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE_ADDED);
            
            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumnIndex);
                long userId = cursor.getLong(userIdColumnIndex);
                String bookId = cursor.getString(bookIdColumnIndex);
                String title = cursor.getString(titleColumnIndex);
                String author = cursor.getString(authorColumnIndex);
                String coverUrl = cursor.getString(coverUrlColumnIndex);
                long timestamp = cursor.getLong(dateAddedColumnIndex);
                
                FavoriteBook favoriteBook = new FavoriteBook(
                        id, userId, bookId, title, author, coverUrl, new Date(timestamp)
                );
                
                // Add to the appropriate list in the map
                if (!allFavorites.containsKey(userId)) {
                    allFavorites.put(userId, new ArrayList<>());
                }
                allFavorites.get(userId).add(favoriteBook);
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "Error getting all users favorites", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return allFavorites;
    }
}