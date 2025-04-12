package com.example.map.data

// DEMO VERSION: Room database disabled
/*
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
*/
import java.util.Date

// DEMO VERSION: Room annotations commented out
// @Entity(tableName = "custom_pois")
// @TypeConverters(DateConverter::class, CategoryConverter::class)
data class CustomPoi(
    // @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val category: PoiCategory,
    val rating: Float,
    val createdAt: Date = Date(),
    val imageUri: String? = null
)

// DEMO VERSION: Room annotations commented out
class DateConverter {
    // @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    // @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

class CategoryConverter {
    // @TypeConverter
    fun fromCategory(category: PoiCategory): String {
        return category.name
    }

    // @TypeConverter
    fun toCategory(value: String): PoiCategory {
        return PoiCategory.valueOf(value)
    }
}
