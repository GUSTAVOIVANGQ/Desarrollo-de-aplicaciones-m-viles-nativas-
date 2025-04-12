package com.example.map

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.map.data.CustomPoi
import com.example.map.data.PoiCategory
import com.example.map.viewmodel.CustomPoiViewModel
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddEditPoiActivity : AppCompatActivity() {

    private lateinit var nameEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var categorySpinner: Spinner
    private lateinit var ratingBar: RatingBar
    private lateinit var imageView: ImageView
    private lateinit var takePhotoButton: Button
    private lateinit var selectPhotoButton: Button
    private lateinit var saveButton: Button
    
    private lateinit var viewModel: CustomPoiViewModel
    private var poiId: Long = 0
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var imageUri: Uri? = null
    
    private val categories = PoiCategory.values()
    
    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageView.setImageURI(imageUri)
        }
    }
    
    private val selectPicture = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            // Copy the selected image to our app's private storage
            val selectedImageUri = copyImageToPrivateStorage(it)
            imageUri = selectedImageUri
            imageView.setImageURI(selectedImageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_poi)
        
        // Setup ActionBar
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Añadir Punto de Interés"
        }
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[CustomPoiViewModel::class.java]
        
        // Get location data from intent
        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)
        poiId = intent.getLongExtra("poi_id", 0)
        
        // Initialize UI components
        nameEditText = findViewById(R.id.nameEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        categorySpinner = findViewById(R.id.categorySpinner)
        ratingBar = findViewById(R.id.ratingBar)
        imageView = findViewById(R.id.poiImageView)
        takePhotoButton = findViewById(R.id.takePhotoButton)
        selectPhotoButton = findViewById(R.id.selectPhotoButton)
        saveButton = findViewById(R.id.saveButton)
        
        // Setup category spinner
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories.map { it.displayName }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
        
        // Setup photo buttons
        takePhotoButton.setOnClickListener {
            imageUri = createImageFileUri()
            takePicture.launch(imageUri!!)
        }
        
        selectPhotoButton.setOnClickListener {
            selectPicture.launch("image/*")
        }
        
        // If editing an existing POI, load its data
        if (poiId > 0) {
            supportActionBar?.title = "Editar Punto de Interés"
            loadPoiData()
        }
        
        // Setup save button
        saveButton.setOnClickListener {
            saveCustomPoi()
        }
    }
    
    private fun loadPoiData() {
        // Load POI data for editing
        viewModel.viewModelScope.launch {
            // Replace direct repository access with ViewModel method
            val poi = viewModel.getPoiById(poiId)
            poi?.let {
                nameEditText.setText(it.name)
                descriptionEditText.setText(it.description)
                
                val categoryIndex = categories.indexOf(it.category)
                if (categoryIndex >= 0) {
                    categorySpinner.setSelection(categoryIndex)
                }
                
                ratingBar.rating = it.rating
                
                it.imageUri?.let { uri ->
                    imageUri = Uri.parse(uri)
                    imageView.setImageURI(imageUri)
                }
                
                latitude = it.latitude
                longitude = it.longitude
            }
        }
    }
    
    private fun saveCustomPoi() {
        val name = nameEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        val category = categories[categorySpinner.selectedItemPosition]
        val rating = ratingBar.rating
        
        if (name.isEmpty()) {
            nameEditText.error = "El nombre es obligatorio"
            return
        }
        
        val poi = CustomPoi(
            id = if (poiId > 0) poiId else 0,
            name = name,
            description = description,
            latitude = latitude,
            longitude = longitude,
            category = category,
            imageUri = imageUri?.toString(),
            rating = rating
        )
        
        if (poiId > 0) {
            viewModel.update(poi)
            Toast.makeText(this, "Punto de interés actualizado", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.insert(poi)
            Toast.makeText(this, "Punto de interés guardado", Toast.LENGTH_SHORT).show()
        }
        
        finish()
    }
    
    private fun createImageFileUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "POI_${timeStamp}"
        val storageDir = File(getExternalFilesDir(null), "PoiImages")
        
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        
        val imageFile = File(storageDir, "$imageFileName.jpg")
        return FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            imageFile
        )
    }
    
    private fun copyImageToPrivateStorage(uri: Uri): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "POI_${timeStamp}"
        val storageDir = File(getExternalFilesDir(null), "PoiImages")
        
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        
        val imageFile = File(storageDir, "$imageFileName.jpg")
        
        contentResolver.openInputStream(uri)?.use { input ->
            imageFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        
        return FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            imageFile
        )
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
