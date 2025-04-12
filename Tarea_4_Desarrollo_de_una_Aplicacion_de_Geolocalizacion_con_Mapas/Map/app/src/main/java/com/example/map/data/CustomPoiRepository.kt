package com.example.map.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlin.math.*

class CustomPoiRepository(private val customPoiDao: CustomPoiDao) {
    
    val allPois: Flow<List<CustomPoi>> = customPoiDao.getAllPois()
    
    suspend fun insert(poi: CustomPoi): Long {
        return customPoiDao.insert(poi)
    }
    
    suspend fun update(poi: CustomPoi) {
        customPoiDao.update(poi)
    }
    
    suspend fun delete(poi: CustomPoi) {
        customPoiDao.delete(poi)
    }
    
    suspend fun getPoiById(id: Long): CustomPoi? {
        return customPoiDao.getPoiById(id).firstOrNull()
    }
    
    fun getPoisByCategory(category: PoiCategory): Flow<List<CustomPoi>> {
        return customPoiDao.getPoisByCategory(category)
    }
    
    fun searchPois(query: String): Flow<List<CustomPoi>> {
        return customPoiDao.searchPois(query)
    }
    
    // Add missing getNearbyPois method using getPoisInArea with calculated bounding box
    fun getNearbyPois(latitude: Double, longitude: Double, radiusInKm: Double): Flow<List<CustomPoi>> {
        // Calculate rough bounding box based on radius
        // 1 degree of latitude = ~111 kilometers
        val latDelta = radiusInKm / 111.0
        val lonDelta = radiusInKm / (111.0 * cos(Math.toRadians(latitude)))
        
        val minLat = latitude - latDelta
        val maxLat = latitude + latDelta
        val minLon = longitude - lonDelta
        val maxLon = longitude + lonDelta
        
        // Get POIs within this bounding box
        return customPoiDao.getPoisInArea(minLat, maxLat, minLon, maxLon)
            .map { pois -> 
                pois.filter { poi ->
                    // Further filter by actual distance using Haversine formula
                    calculateDistance(latitude, longitude, poi.latitude, poi.longitude) <= radiusInKm
                }
            }
    }
    
    // Haversine formula to calculate distance between two points on Earth
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in kilometers
        
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        
        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lonDistance / 2) * sin(lonDistance / 2)
                
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return r * c
    }
}
