package com.example.puzzle.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Manages the device's light sensor and provides brightness adaptation functionality
 */
class LightSensorManager(private val context: Context) : SensorEventListener, DefaultLifecycleObserver {

    companion object {
        private const val TAG = "LightSensorManager"
        
        // Threshold values for light levels in lux
        private const val DARK_LUX_THRESHOLD = 10f
        private const val DIM_LUX_THRESHOLD = 50f
        private const val NORMAL_LUX_THRESHOLD = 200f
        private const val BRIGHT_LUX_THRESHOLD = 1000f
        
        // Hysteresis value to prevent rapid toggling
        private const val LUX_HYSTERESIS = 5f
        
        // Singleton instance
        @Volatile
        private var INSTANCE: LightSensorManager? = null
        
        fun getInstance(context: Context): LightSensorManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LightSensorManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private var sensorManager: SensorManager? = null
    private var lightSensor: Sensor? = null
    private var isRegistered = false
    
    // Current light level category
    enum class LightLevel { DARK, DIM, NORMAL, BRIGHT, VERY_BRIGHT }
    
    // Callbacks
    private var onLightLevelChanged: ((LightLevel) -> Unit)? = null
    private var currentLightLevel: LightLevel = LightLevel.NORMAL
    private var lastLuxReading = -1f
    
    private var autoModeEnabled = false
    
    init {
        // Get the system's sensor service
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        // Get the light sensor
        lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
        
        if (lightSensor == null) {
            Log.w(TAG, "Light sensor not available on this device")
        }
    }
    
    /**
     * Start monitoring light changes
     */
    fun startMonitoring() {
        if (lightSensor != null && !isRegistered) {
            sensorManager?.registerListener(
                this,
                lightSensor,
                SensorManager.SENSOR_DELAY_UI
            )
            isRegistered = true
            Log.d(TAG, "Light sensor monitoring started")
        }
    }
    
    /**
     * Stop monitoring light changes
     */
    fun stopMonitoring() {
        if (isRegistered) {
            sensorManager?.unregisterListener(this)
            isRegistered = false
            Log.d(TAG, "Light sensor monitoring stopped")
        }
    }
    
    /**
     * Set a callback to receive light level change events
     */
    fun setOnLightLevelChangedListener(listener: (LightLevel) -> Unit) {
        onLightLevelChanged = listener
    }
    
    /**
     * Enable or disable automatic mode switching based on ambient light
     */
    fun setAutoModeEnabled(enabled: Boolean) {
        autoModeEnabled = enabled
        
        if (enabled) {
            startMonitoring()
        } else {
            stopMonitoring()
        }
    }
    
    /**
     * Check if auto mode is currently enabled
     */
    fun isAutoModeEnabled(): Boolean = autoModeEnabled
    
    /**
     * Get the current light level category
     */
    fun getCurrentLightLevel(): LightLevel = currentLightLevel
    
    /**
     * Get the last measured light value in lux
     */
    fun getLastLuxReading(): Float = lastLuxReading
    
    // SensorEventListener implementation
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_LIGHT) {
            val luxValue = event.values[0]
            lastLuxReading = luxValue
            
            // Determine the new light level
            val newLightLevel = when {
                luxValue < DARK_LUX_THRESHOLD -> LightLevel.DARK
                luxValue < DIM_LUX_THRESHOLD -> LightLevel.DIM
                luxValue < NORMAL_LUX_THRESHOLD -> LightLevel.NORMAL
                luxValue < BRIGHT_LUX_THRESHOLD -> LightLevel.BRIGHT
                else -> LightLevel.VERY_BRIGHT
            }
            
            // Only notify if the light level category has changed
            if (currentLightLevel != newLightLevel) {
                currentLightLevel = newLightLevel
                onLightLevelChanged?.invoke(newLightLevel)
                Log.d(TAG, "Light level changed to: $newLightLevel (${luxValue} lux)")
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Not needed for this implementation, but required by the interface
    }
    
    // DefaultLifecycleObserver implementation - automatically manage sensor registration
    override fun onResume(owner: LifecycleOwner) {
        if (autoModeEnabled) {
            startMonitoring()
        }
    }
    
    override fun onPause(owner: LifecycleOwner) {
        stopMonitoring()
    }
}
