package com.example.map

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.map.adapter.PoiAdapter
import com.example.map.data.CustomPoi
import com.example.map.data.PoiCategory
import com.example.map.viewmodel.CustomPoiViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class PoiListActivity : AppCompatActivity(), PoiAdapter.OnPoiClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var poiAdapter: PoiAdapter
    private lateinit var chipGroup: ChipGroup
    private lateinit var viewModel: CustomPoiViewModel
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poi_list)
        
        // Setup ActionBar
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Mis Puntos de Interés"
        }
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[CustomPoiViewModel::class.java]
        
        // Initialize RecyclerView
        recyclerView = findViewById(R.id.poiRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        poiAdapter = PoiAdapter(this)
        recyclerView.adapter = poiAdapter
        
        // Initialize chip group for category filtering
        chipGroup = findViewById(R.id.categoryChipGroup)
        setupCategoryChips()
        
        // Observe POIs
        viewModel.filteredPois.observe(this) { pois ->
            poiAdapter.submitList(pois)
            updateEmptyView(pois)
        }
    }

    private fun setupCategoryChips() {
        // Add a "All" chip first
        val allChip = Chip(this).apply {
            text = "Todos"
            isCheckable = true
            isChecked = true
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewModel.clearFilters()
                    uncheckOtherChips(this)
                }
            }
        }
        chipGroup.addView(allChip)
        
        // Add a chip for each category
        for (category in PoiCategory.values()) {
            val chip = Chip(this).apply {
                text = category.displayName
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        viewModel.setFilterCategory(category)
                        uncheckOtherChips(this)
                    }
                }
            }
            chipGroup.addView(chip)
        }
    }
    
    private fun uncheckOtherChips(selectedChip: Chip) {
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as? Chip
            if (chip != selectedChip) {
                chip?.isChecked = false
            }
        }
    }
    
    private fun updateEmptyView(pois: List<CustomPoi>) {
        val emptyView = findViewById<android.view.View>(R.id.emptyView)
        if (pois.isEmpty()) {
            emptyView.visibility = android.view.View.VISIBLE
            recyclerView.visibility = android.view.View.GONE
        } else {
            emptyView.visibility = android.view.View.GONE
            recyclerView.visibility = android.view.View.VISIBLE
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.poi_list_menu, menu)
        
        // Configure search view
        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    viewModel.setSearchQuery(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    viewModel.clearFilters()
                } else if (newText.length >= 2) {
                    viewModel.setSearchQuery(newText)
                }
                return true
            }
        })
        
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_add_poi -> {
                // Start AddEditPoiActivity for adding a new POI
                val intent = Intent(this, AddEditPoiActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onPoiClick(poi: CustomPoi) {
        // Open the POI details
        val intent = Intent(this, PoiDetailActivity::class.java).apply {
            putExtra("poi_id", poi.id)
        }
        startActivity(intent)
    }
    
    override fun onPoiEditClick(poi: CustomPoi) {
        // Edit the POI
        val intent = Intent(this, AddEditPoiActivity::class.java).apply {
            putExtra("poi_id", poi.id)
            putExtra("latitude", poi.latitude)
            putExtra("longitude", poi.longitude)
        }
        startActivity(intent)
    }
    
    override fun onPoiDeleteClick(poi: CustomPoi) {
        // Delete POI with confirmation dialog
        android.app.AlertDialog.Builder(this)
            .setTitle("Eliminar punto de interés")
            .setMessage("¿Estás seguro de que quieres eliminar '${poi.name}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.delete(poi)
                android.widget.Toast.makeText(this, "Punto de interés eliminado", android.widget.Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
