package com.example.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import com.example.map.data.CustomPoi
import com.example.map.data.PoiCategory
import com.example.map.viewmodel.CustomPoiViewModel
import com.google.android.gms.location.*
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    
    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        // Normal mode: Update location every 10 seconds
        private const val LOCATION_UPDATE_INTERVAL = 10000L
        private const val FASTEST_INTERVAL = 5000L
        
        // Battery saving mode: Update location every 30 seconds
        private const val BATTERY_SAVING_UPDATE_INTERVAL = 30000L
        private const val BATTERY_SAVING_FASTEST_INTERVAL = 15000L
        
        // Minimum distance for update (10 meters)
        private const val MIN_DISTANCE_CHANGE = 10f
        
        // Key for passing location data between activities
        const val EXTRA_LATITUDE = "extra_latitude"
        const val EXTRA_LONGITUDE = "extra_longitude"
        const val EXTRA_ACCURACY = "extra_accuracy"
    }
    
    private lateinit var webView: WebView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var lastKnownLocation: Location? = null
    private var isActiveTracking = true
    
    // Custom POI components
    private lateinit var customPoiViewModel: CustomPoiViewModel
    private lateinit var addPoiBtn: FloatingActionButton
    private lateinit var viewPoiListBtn: FloatingActionButton
    
    // Register the permissions callback
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        
        if (allGranted) {
            // All permissions granted, proceed with map setup
            Toast.makeText(this, "Permisos de ubicación concedidos", Toast.LENGTH_SHORT).show()
            setupLocationUpdates()
        } else {
            // Permissions denied, show a message explaining why the app needs these permissions
            Toast.makeText(this, 
                "Se necesitan permisos de ubicación para mostrar tu posición en el mapa", 
                Toast.LENGTH_LONG).show()
            // Show error in the WebView
            showErrorInMap("No se puede mostrar el mapa sin permisos de ubicación")
        }
    }

    private lateinit var openStreetMapBtn: Button
    private lateinit var googleMapsBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Keep screen on for map navigation
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Initialize WebView with optimized settings
        webView = findViewById(R.id.webView)
        setupOptimizedWebView()
        
        // Initialize navigation buttons
        openStreetMapBtn = findViewById(R.id.openStreetMapBtn)
        googleMapsBtn = findViewById(R.id.googleMapsBtn)
        setupNavigation()
        
        // Initialize custom POI buttons
        addPoiBtn = findViewById(R.id.addPoiBtn)
        viewPoiListBtn = findViewById(R.id.viewPoiListBtn)
        setupCustomPoiButtons()
        
        // Initialize ViewModel for Custom POIs
        customPoiViewModel = ViewModelProvider(this)[CustomPoiViewModel::class.java]
        
        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        // Check location permissions when the activity starts
        checkLocationPermissions()
        
        // Setup long press JavaScript interface for custom POIs
        setupCustomPoiJavaScriptInterface()
    }
    
    private fun setupNavigation() {
        // Set click listener for Google Maps button
        googleMapsBtn.setOnClickListener {
            // Launch Google Maps Activity and pass location data if available
            lastKnownLocation?.let { location ->
                val intent = Intent(this, GoogleMapsActivity::class.java).apply {
                    putExtra(EXTRA_LATITUDE, location.latitude)
                    putExtra(EXTRA_LONGITUDE, location.longitude)
                    putExtra(EXTRA_ACCURACY, location.accuracy)
                }
                startActivity(intent)
            } ?: run {
                // If no location is available yet
                val intent = Intent(this, GoogleMapsActivity::class.java)
                startActivity(intent)
                Toast.makeText(this, "Espere mientras se obtiene su ubicación", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupOptimizedWebView() {
        webView.settings.apply {
            // Enable JavaScript
            javaScriptEnabled = true
            
            // Optimize WebView performance
            domStorageEnabled = true
            setGeolocationEnabled(true)
            cacheMode = WebSettings.LOAD_DEFAULT // Better for performance than NO_CACHE
            
            // Modern caching strategy
            databaseEnabled = true
            
            // Other performance optimizations
            loadsImagesAutomatically = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            
            // Set text zoom to normal
            textZoom = 100
            
            // Enable wide viewport
            useWideViewPort = true
            loadWithOverviewMode = true
        }
        
        // Hardware acceleration at the view level
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        
        // Set WebView clients for better performance
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // When page is loaded, get initial location if permissions are granted
                if (hasLocationPermissions() && lastKnownLocation == null) {
                    getLastKnownLocation()
                }
                
                // Setup long press detection for adding POIs
                setupLongPressDetection()
            }
        }
        
        // Load the HTML file from assets
        webView.loadUrl("file:///android_asset/map.html")
    }
    
    private fun setupCustomPoiButtons() {
        // Add POI button - opens POI creation dialog at current location
        addPoiBtn.setOnClickListener {
            lastKnownLocation?.let { location ->
                showAddPoiDialog(location.latitude, location.longitude)
            } ?: run {
                Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
            }
        }
        
        // View all POIs list button
        viewPoiListBtn.setOnClickListener {
            val intent = Intent(this, PoiListActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun setupCustomPoiJavaScriptInterface() {
        // Add JavaScript interface to handle POI operations from WebView
        webView.addJavascriptInterface(MapJavaScriptInterface(this), "AndroidMapInterface")
    }
    
    private fun setupLongPressDetection() {
        // Inject JavaScript to capture long press events on the map
        val longPressJs = """
            var longPressTimer;
            var startX, startY;
            
            map.on('mousedown', function(e) {
                startX = e.originalEvent.clientX;
                startY = e.originalEvent.clientY;
                
                longPressTimer = setTimeout(function() {
                    var latlng = map.mouseEventToLatLng(e.originalEvent);
                    AndroidMapInterface.onMapLongPress(latlng.lat, latlng.lng);
                }, 800);
            });
            
            map.on('mouseup mousemove', function(e) {
                clearTimeout(longPressTimer);
                
                // Check if it's a small movement (to prevent canceling long press on small movements)
                var dx = Math.abs(e.originalEvent.clientX - startX);
                var dy = Math.abs(e.originalEvent.clientY - startY);
                if (dx > 10 || dy > 10) {
                    clearTimeout(longPressTimer);
                }
            });
            
            // For touch devices
            map.on('touchstart', function(e) {
                if (e.originalEvent.touches.length === 1) {
                    startX = e.originalEvent.touches[0].clientX;
                    startY = e.originalEvent.touches[0].clientY;
                    
                    longPressTimer = setTimeout(function() {
                        var latlng = map.mouseEventToLatLng(e.originalEvent.touches[0]);
                        AndroidMapInterface.onMapLongPress(latlng.lat, latlng.lng);
                    }, 800);
                }
            });
            
            map.on('touchend touchmove', function(e) {
                clearTimeout(longPressTimer);
                
                if (e.originalEvent.changedTouches.length === 1) {
                    // Check if it's a small movement
                    var dx = Math.abs(e.originalEvent.changedTouches[0].clientX - startX);
                    var dy = Math.abs(e.originalEvent.changedTouches[0].clientY - startY);
                    if (dx > 10 || dy > 10) {
                        clearTimeout(longPressTimer);
                    }
                }
            });
        """.trimIndent()
        
        webView.evaluateJavascript(longPressJs, null)
    }
    
    private fun showAddPoiDialog(latitude: Double, longitude: Double) {
        // Create a dialog for adding a new POI
        val intent = Intent(this, AddEditPoiActivity::class.java).apply {
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
        }
        startActivity(intent)
    }
    
    // JavaScript interface for WebView to Android communication
    inner class MapJavaScriptInterface(private val context: Context) {
        @JavascriptInterface
        fun onMapLongPress(latitude: Double, longitude: Double) {
            runOnUiThread {
                showAddPoiDialog(latitude, longitude)
            }
        }
    }
    
    private fun updateMapWithCustomPois() {
        if (!this::customPoiViewModel.isInitialized) return
        
        val currentLocation = lastKnownLocation ?: return
        
        // Observe nearby POIs within 1km radius
        customPoiViewModel.getNearbyPois(
            currentLocation.latitude, 
            currentLocation.longitude, 
            1.0
        ).observe(this, { poisList: List<CustomPoi> ->
            if (poisList.isNotEmpty()) {
                val jsBuilder = StringBuilder()
                jsBuilder.append("var customPois = [];\n")
                jsBuilder.append("function clearCustomPois() { ")
                jsBuilder.append("  for(var i=0; i<customPois.length; i++) { ")
                jsBuilder.append("    customPois[i].remove(); ")
                jsBuilder.append("  } ")
                jsBuilder.append("  customPois = []; ")
                jsBuilder.append("}\n")
                jsBuilder.append("clearCustomPois();\n")
                
                for (poi in poisList) {
                    val iconColor = when (poi.category) {
                        PoiCategory.FAVORITE -> "#FF0000" // Red
                        PoiCategory.TO_VISIT -> "#0000FF" // Blue
                        PoiCategory.RECOMMENDED -> "#FFFF00" // Yellow
                        PoiCategory.RESTAURANT -> "#FF8C00" // Dark Orange
                        PoiCategory.SHOPPING -> "#800080" // Purple
                        PoiCategory.CULTURAL -> "#4B0082" // Indigo
                        PoiCategory.OUTDOOR -> "#008000" // Green
                        else -> "#808080" // Gray
                    }
                    
                    jsBuilder.append("var marker = L.circleMarker([${poi.latitude}, ${poi.longitude}], {")
                    jsBuilder.append("  radius: 8,")
                    jsBuilder.append("  fillColor: '$iconColor',")
                    jsBuilder.append("  color: '#000',")
                    jsBuilder.append("  weight: 1,")
                    jsBuilder.append("  opacity: 1,")
                    jsBuilder.append("  fillOpacity: 0.8")
                    jsBuilder.append("}).addTo(map);\n")
                    
                    // Add popup with POI information
                    val popupContent = "<b>${poi.name}</b><br>${poi.description}"
                    jsBuilder.append("marker.bindPopup('$popupContent');\n")
                    
                    // Add to array for later removal
                    jsBuilder.append("customPois.push(marker);\n")
                }
                
                webView.evaluateJavascript(jsBuilder.toString(), null)
            }
        })
    }
    
    override fun onResume() {
        super.onResume()
        // Switch to active tracking mode
        toggleTrackingMode(true)
        
        // Update custom POIs on the map
        updateMapWithCustomPois()
    }
    
    override fun onPause() {
        super.onPause()
        // Switch to battery saving mode when not in foreground
        toggleTrackingMode(false)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Remove location updates to prevent memory leaks
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        
        // Clear WebView cache and history to free memory
        webView.clearCache(true)
        webView.clearHistory()
    }
    
    private fun checkLocationPermissions() {
        if (hasLocationPermissions()) {
            // Permissions already granted, proceed with map setup
            Toast.makeText(this, "Permisos de ubicación ya concedidos", Toast.LENGTH_SHORT).show()
            setupLocationUpdates()
        } else {
            // Request location permissions
            requestLocationPermissions()
        }
    }
    
    private fun hasLocationPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun requestLocationPermissions() {
        // Check if we should show the rationale for any permission
        val shouldShowRationale = REQUIRED_PERMISSIONS.any {
            ActivityCompat.shouldShowRequestPermissionRationale(this, it)
        }
        
        if (shouldShowRationale) {
            // Show explanation to the user about why we need these permissions
            Toast.makeText(this, 
                "La aplicación necesita acceso a tu ubicación para mostrarte en el mapa", 
                Toast.LENGTH_LONG).show()
        }
        
        // Request the permissions
        requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
    }
    
    @SuppressLint("MissingPermission")
    private fun setupLocationUpdates() {
        // Create location request with appropriate intervals based on battery optimization
        val locationRequest = if (isActiveTracking) {
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL)
                .setMinUpdateIntervalMillis(FASTEST_INTERVAL)
                .setMinUpdateDistanceMeters(MIN_DISTANCE_CHANGE) // Only update if moved 10 meters
                .setWaitForAccurateLocation(true)
                .build()
        } else {
            LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, BATTERY_SAVING_UPDATE_INTERVAL)
                .setMinUpdateIntervalMillis(BATTERY_SAVING_FASTEST_INTERVAL)
                .setMinUpdateDistanceMeters(MIN_DISTANCE_CHANGE * 2) // Only update if moved 20 meters in battery saving mode
                .setWaitForAccurateLocation(false)
                .build()
        }
        
        // Create location callback with significant change filter
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    // Only update map if the location has changed significantly
                    if (shouldUpdateLocation(location)) {
                        lastKnownLocation = location
                        updateMapWithLocation(location)
                    }
                }
            }
        }
        
        // Request location updates
        if (hasLocationPermissions()) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
            
            // Get initial location
            getLastKnownLocation()
        }
    }
    
    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation() {
        if (hasLocationPermissions()) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        lastKnownLocation = location
                        updateMapWithLocation(location)
                    } else {
                        showErrorInMap("Obteniendo ubicación actual...")
                    }
                }
                .addOnFailureListener { e ->
                    showErrorInMap("Error al obtener la ubicación: ${e.message}")
                }
        }
    }
    
    // Determine if we should update the map based on the change in location
    private fun shouldUpdateLocation(newLocation: Location): Boolean {
        val lastLocation = lastKnownLocation ?: return true
        
        // Calculate distance between last known location and new location
        val distance = lastLocation.distanceTo(newLocation)
        
        // Calculate time difference in milliseconds
        val timeDiff = newLocation.time - lastLocation.time
        
        // Update if moved more than 10 meters or if more than 30 seconds have passed
        return distance > MIN_DISTANCE_CHANGE || timeDiff > 30000
    }
    
    private fun updateMapWithLocation(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        val accuracy = location.accuracy
        
        // Call JavaScript function to update the map with enhanced parameters
        webView.evaluateJavascript(
            "javascript:showMap($latitude, $longitude, $accuracy)",
            null
        )
    }
    
    private fun showErrorInMap(message: String) {
        // Call JavaScript function to show error
        webView.evaluateJavascript(
            "javascript:showError('$message')",
            null
        )
    }
    
    // Toggle between active tracking and battery saving modes
    fun toggleTrackingMode(isActive: Boolean) {
        if (this.isActiveTracking != isActive) {
            this.isActiveTracking = isActive
            
            // Remove existing updates
            locationCallback?.let {
                fusedLocationClient.removeLocationUpdates(it)
            }
            
            // Setup with new tracking mode
            setupLocationUpdates()
            
            val message = if (isActive) 
                "Modo de seguimiento activo" 
            else 
                "Modo de ahorro de batería"
            
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}