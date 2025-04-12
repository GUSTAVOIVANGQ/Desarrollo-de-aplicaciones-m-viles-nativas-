package com.example.map

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.map.viewmodel.CustomPoiViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PoiDetailActivity : AppCompatActivity() {

    private lateinit var nameTextView: TextView
    private lateinit var categoryTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var imageView: ImageView
    private lateinit var coordinatesTextView: TextView
    private lateinit var showOnMapButton: Button
    private lateinit var editButton: Button
    private lateinit var deleteButton: Button
    
    private lateinit var viewModel: CustomPoiViewModel
    private var poiId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poi_detail)
        
        // Setup ActionBar
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Detalle del Punto de Interés"
        }
        
        // Get POI ID from intent
        poiId = intent.getLongExtra("poi_id", 0)
        if (poiId == 0L) {
            finish()
            return
        }
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[CustomPoiViewModel::class.java]
        
        // Initialize UI components
        nameTextView = findViewById(R.id.poiNameTextView)
        categoryTextView = findViewById(R.id.poiCategoryTextView)
        descriptionTextView = findViewById(R.id.poiDescriptionTextView)
        dateTextView = findViewById(R.id.poiDateTextView)
        ratingBar = findViewById(R.id.poiRatingBar)
        imageView = findViewById(R.id.poiImageView)
        coordinatesTextView = findViewById(R.id.poiCoordinatesTextView)
        showOnMapButton = findViewById(R.id.showOnMapButton)
        editButton = findViewById(R.id.editButton)
        deleteButton = findViewById(R.id.deleteButton)
        
        // Load POI data
        loadPoiData()
        
        // Setup button click listeners
        setupClickListeners()
    }
    
    private fun loadPoiData() {
        viewModel.viewModelScope.launch {
            val poi = viewModel.getPoiById(poiId)
            poi?.let {
                nameTextView.text = it.name
                categoryTextView.text = it.category.displayName
                descriptionTextView.text = it.description
                ratingBar.rating = it.rating
                
                // Format date
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                dateTextView.text = dateFormat.format(it.createdAt)
                
                // Show coordinates
                coordinatesTextView.text = "Lat: ${it.latitude}, Long: ${it.longitude}"
                
                // Load image if available
                it.imageUri?.let { uri ->
                    try {
                        imageView.setImageURI(Uri.parse(uri))
                        imageView.visibility = View.VISIBLE
                    } catch (e: Exception) {
                        imageView.visibility = View.GONE
                    }
                } ?: run {
                    imageView.visibility = View.GONE
                }
                
                // Store coordinates for map button
                showOnMapButton.tag = LatLng(it.latitude, it.longitude)
            }
        }
    }
    
    private fun setupClickListeners() {
        showOnMapButton.setOnClickListener {
            val latLng = it.tag as? LatLng
            latLng?.let { coordinates ->
                val intent = Intent(this, GoogleMapsActivity::class.java).apply {
                    putExtra("show_poi", true)
                    putExtra("poi_id", poiId)
                    putExtra("poi_latitude", coordinates.latitude)
                    putExtra("poi_longitude", coordinates.longitude)
                }
                startActivity(intent)
            }
        }
        
        editButton.setOnClickListener {
            val intent = Intent(this, AddEditPoiActivity::class.java).apply {
                putExtra("poi_id", poiId)
            }
            startActivity(intent)
            finish()
        }
        
        deleteButton.setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("Eliminar punto de interés")
                .setMessage("¿Estás seguro de que quieres eliminar este punto de interés?")
                .setPositiveButton("Eliminar") { _, _ ->
                    viewModel.viewModelScope.launch {
                        val poi = viewModel.getPoiById(poiId)
                        poi?.let {
                            viewModel.delete(it)
                            android.widget.Toast.makeText(
                                this@PoiDetailActivity,
                                "Punto de interés eliminado",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
