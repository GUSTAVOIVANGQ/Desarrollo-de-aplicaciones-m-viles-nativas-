package com.example.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.webkit.GeolocationPermissions
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
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class GoogleMapsActivity : AppCompatActivity() {
    
    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        private const val LOCATION_UPDATE_INTERVAL = 10000L
        private const val FASTEST_INTERVAL = 5000L
    }
    
    private lateinit var webView: WebView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var lastKnownLocation: Location? = null
    private var openStreetMapBtn: Button? = null
    private var googleMapsBtn: Button? = null
    
    // Register permissions callback
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "Permisos de ubicación concedidos", Toast.LENGTH_SHORT).show()
            setupLocationUpdates()
        } else {
            Toast.makeText(this, 
                "Se necesitan permisos de ubicación para mostrar tu posición en el mapa", 
                Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        try {
            // Set content view
            setContentView(R.layout.activity_google_maps)
            
            // Initialize WebView first - this is essential
            webView = findViewById(R.id.webView)
            if (webView == null) {
                throw RuntimeException("WebView not found in layout")
            }
            
            // Setup WebView with JavaScript enabled
            setupWebView()
            
            // Initialize optional navigation buttons - with null safety
            try {
                openStreetMapBtn = findViewById(R.id.openStreetMapBtn)
                googleMapsBtn = findViewById(R.id.googleMapsBtn)
                
                // Only set up navigation if buttons are found
                if (openStreetMapBtn != null) {
                    setupNavigation()
                }
            } catch (e: Exception) {
                Log.w("GoogleMapsActivity", "Navigation buttons not found or initialized: ${e.message}")
                // Continue without navigation - it's not critical
            }
            
            // Initialize location services
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            
            // Get location data from intent if available
            val latitude = intent.getDoubleExtra(MainActivity.EXTRA_LATITUDE, 0.0)
            val longitude = intent.getDoubleExtra(MainActivity.EXTRA_LONGITUDE, 0.0)
            
            // Load map - either with provided coordinates or use default coordinates
            if (latitude != 0.0 && longitude != 0.0) {
                loadGoogleMaps(latitude, longitude)
            } else {
                // Default to a location in Mexico City for demo
                loadGoogleMaps(19.4326, -99.1332)
            }
            
            // Setup location updates if permissions are available
            if (hasLocationPermissions()) {
                setupLocationUpdates()
            } else {
                requestLocationPermissions()
            }
            
        } catch (e: Exception) {
            Log.e("GoogleMapsActivity", "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error initializing map activity", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun setupNavigation() {
        // Set click listener for OpenStreetMap button with null safety
        openStreetMapBtn?.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
    
    private fun hasLocationPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun requestLocationPermissions() {
        requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
    }
    
    @SuppressLint("MissingPermission")
    private fun setupLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL)
            .setMinUpdateIntervalMillis(FASTEST_INTERVAL)
            .setWaitForAccurateLocation(true)
            .build()
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    lastKnownLocation = location
                    loadGoogleMaps(location.latitude, location.longitude)
                }
            }
        }
        
        if (hasLocationPermissions()) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
            
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
                        loadGoogleMaps(location.latitude, location.longitude)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("GoogleMapsActivity", "Error getting location: ${e.message}")
                    // Load default location in case of error
                    loadGoogleMaps(19.4326, -99.1332) // Mexico City coordinates
                }
        }
    }
    
    // The simplified function for loading Google Maps
    private fun loadGoogleMaps(latitude: Double, longitude: Double) {
        try {
            val googleMapsUrl = "https://www.google.com/maps?q=$latitude,$longitude&z=15"
            Log.d("GoogleMapsActivity", "Loading map with URL: $googleMapsUrl")
            webView.loadUrl(googleMapsUrl)
        } catch (e: Exception) {
            Log.e("GoogleMapsActivity", "Error loading Google Maps: ${e.message}", e)
            Toast.makeText(this, "Error loading map", Toast.LENGTH_SHORT).show()
        }
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            // Enable JavaScript - required for Google Maps to work
            javaScriptEnabled = true
            
            // Optimize WebView performance
            domStorageEnabled = true
            setGeolocationEnabled(true)
            cacheMode = WebSettings.LOAD_DEFAULT
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
        
        // Hardware acceleration
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        
        // Set up WebChromeClient for geolocation permissions
        webView.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(
                origin: String,
                callback: GeolocationPermissions.Callback
            ) {
                callback.invoke(origin, true, false)
            }
        }
        
        // Set up WebViewClient
        webView.webViewClient = WebViewClient()
    }
    
    override fun onResume() {
        super.onResume()
    }
    
    override fun onPause() {
        super.onPause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        webView.clearCache(true)
        webView.clearHistory()
    }
    
    // Simple JavaScript interface for the WebView
    inner class MapJavaScriptInterface(private val context: Context) {
        @JavascriptInterface
        fun onMapError(errorMessage: String) {
            runOnUiThread {
                Toast.makeText(context, "Map error: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
