package com.example.map.data

import android.content.Context

// DEMO VERSION: Room database disabled
// Mock implementation for demo purposes
/*
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [CustomPoi::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class, CategoryConverter::class)
*/
class AppDatabase /* : RoomDatabase() */ {

    // Mock implementation
    fun customPoiDao(): CustomPoiDao = MockCustomPoiDao()

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Mock implementation - no actual database is created
                val instance = AppDatabase()
                INSTANCE = instance
                instance
            }
        }
    }
}
