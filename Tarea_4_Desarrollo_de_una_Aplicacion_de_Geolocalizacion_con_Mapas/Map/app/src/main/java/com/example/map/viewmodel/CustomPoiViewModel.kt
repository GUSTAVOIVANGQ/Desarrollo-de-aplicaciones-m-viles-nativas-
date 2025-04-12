package com.example.map.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.map.data.AppDatabase
import com.example.map.data.CustomPoi
import com.example.map.data.CustomPoiRepository
import com.example.map.data.PoiCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class CustomPoiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CustomPoiRepository
    private val searchQuery = MutableStateFlow("")
    private val filterCategory = MutableStateFlow<PoiCategory?>(null)
    private val _filterTrigger = MutableStateFlow<String?>(null)

    val allPois: LiveData<List<CustomPoi>>
    
    init {
        val customPoiDao = AppDatabase.getDatabase(application).customPoiDao()
        repository = CustomPoiRepository(customPoiDao)
        allPois = repository.allPois.asLiveData()
    }

    val filteredPois = _filterTrigger
        .flatMapLatest { query ->
            when {
                !query.isNullOrEmpty() -> repository.searchPois(query)
                filterCategory.value != null -> repository.getPoisByCategory(filterCategory.value!!)
                else -> repository.allPois
            }
        }.asLiveData()

    fun insert(poi: CustomPoi) = viewModelScope.launch {
        repository.insert(poi)
    }

    fun update(poi: CustomPoi) = viewModelScope.launch {
        repository.update(poi)
    }

    fun delete(poi: CustomPoi) = viewModelScope.launch {
        repository.delete(poi)
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
        _filterTrigger.value = query  // Update the trigger instead
    }

    fun setFilterCategory(category: PoiCategory?) {
        filterCategory.value = category
    }

    // Fix return type by explicitly specifying it as LiveData<List<CustomPoi>>
    fun getNearbyPois(latitude: Double, longitude: Double, radiusInKm: Double = 1.0): LiveData<List<CustomPoi>> = 
        repository.getNearbyPois(latitude, longitude, radiusInKm).asLiveData()

    fun clearFilters() {
        searchQuery.value = ""
        filterCategory.value = null
        _filterTrigger.value = null  // Update the trigger instead
    }

    suspend fun getPoiById(id: Long): CustomPoi? {
        return repository.getPoiById(id)
    }
}
