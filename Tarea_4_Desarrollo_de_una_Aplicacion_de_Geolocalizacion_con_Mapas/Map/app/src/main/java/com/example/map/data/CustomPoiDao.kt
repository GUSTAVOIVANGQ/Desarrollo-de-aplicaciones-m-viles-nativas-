package com.example.map.data

// DEMO VERSION: Room database disabled
/*
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
*/
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.Date

// DEMO VERSION: Room annotations commented out
// @Dao
interface CustomPoiDao {
    // @Insert
    suspend fun insert(poi: CustomPoi): Long
    
    // @Update
    suspend fun update(poi: CustomPoi)
    
    // @Delete
    suspend fun delete(poi: CustomPoi)
    
    // @Query("SELECT * FROM custom_pois ORDER BY createdAt DESC")
    fun getAllPois(): Flow<List<CustomPoi>>
    
    // @Query("SELECT * FROM custom_pois WHERE id = :poiId")
    fun getPoiById(poiId: Long): Flow<CustomPoi?>
    
    // @Query("SELECT * FROM custom_pois WHERE category = :category ORDER BY createdAt DESC")
    fun getPoisByCategory(category: PoiCategory): Flow<List<CustomPoi>>
    
    // @Query("SELECT * FROM custom_pois WHERE name LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchPois(query: String): Flow<List<CustomPoi>>
    
    // @Query("SELECT * FROM custom_pois WHERE (latitude BETWEEN :minLat AND :maxLat) AND (longitude BETWEEN :minLon AND :maxLon)")
    fun getPoisInArea(minLat: Double, maxLat: Double, minLon: Double, maxLon: Double): Flow<List<CustomPoi>>
}

// Mock implementation for demo purpose
class MockCustomPoiDao : CustomPoiDao {
    private val pois = MutableStateFlow<List<CustomPoi>>(createSamplePois())
    
    override suspend fun insert(poi: CustomPoi): Long {
        val newId = (pois.value.maxOfOrNull { it.id } ?: 0) + 1
        val newPoi = poi.copy(id = newId)
        pois.value = pois.value + newPoi
        return newId
    }
    
    override suspend fun update(poi: CustomPoi) {
        pois.value = pois.value.map { if (it.id == poi.id) poi else it }
    }
    
    override suspend fun delete(poi: CustomPoi) {
        pois.value = pois.value.filter { it.id != poi.id }
    }
    
    override fun getAllPois(): Flow<List<CustomPoi>> = pois
    
    override fun getPoiById(poiId: Long): Flow<CustomPoi?> {
        return pois.map { poiList -> poiList.find { it.id == poiId } }
    }
    
    override fun getPoisByCategory(category: PoiCategory): Flow<List<CustomPoi>> {
        return pois.map { poiList -> poiList.filter { it.category == category } }
    }
    
    override fun searchPois(query: String): Flow<List<CustomPoi>> {
        return pois.map { poiList -> 
            poiList.filter { it.name.contains(query, ignoreCase = true) }
        }
    }
    
    override fun getPoisInArea(minLat: Double, maxLat: Double, minLon: Double, maxLon: Double): Flow<List<CustomPoi>> {
        return pois.map { poiList ->
            poiList.filter { 
                it.latitude in minLat..maxLat && it.longitude in minLon..maxLon 
            }
        }
    }
    
    // Create sample POIs for demo - updated with correct enum values
    private fun createSamplePois(): List<CustomPoi> {
        return listOf(
            CustomPoi(
                id = 1,
                name = "Museo del Prado",
                description = "Uno de los museos de arte más importantes del mundo",
                latitude = 40.4139,
                longitude = -3.6921,
                category = PoiCategory.CULTURAL,
                rating = 5.0f
            ),
            CustomPoi(
                id = 2,
                name = "Parque del Retiro",
                description = "Hermoso parque en el centro de Madrid",
                latitude = 40.4153,
                longitude = -3.6844,
                category = PoiCategory.OUTDOOR,
                rating = 4.5f
            ),
            CustomPoi(
                id = 3,
                name = "Restaurante Botín",
                description = "El restaurante más antiguo del mundo según el libro Guinness",
                latitude = 40.4136,
                longitude = -3.7093,
                category = PoiCategory.RESTAURANT,
                rating = 4.8f
            )
        )
    }
}
