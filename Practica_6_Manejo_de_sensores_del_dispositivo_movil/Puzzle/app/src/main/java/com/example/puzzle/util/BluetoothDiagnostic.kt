package com.example.puzzle.util

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat

/**
 * Clase de utilidad para diagnosticar problemas de Bluetooth
 */
object BluetoothDiagnostic {
    private const val TAG = "BluetoothDiagnostic"
    
    /**
     * Realiza un diagnóstico completo del estado de Bluetooth y permisos
     */
    fun performDiagnostic(context: Context): DiagnosticResult {
        Log.d(TAG, "=== INICIO DIAGNÓSTICO BLUETOOTH ===")
        
        val result = DiagnosticResult()
        
        // 1. Verificar soporte de Bluetooth
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val bluetoothAdapter = bluetoothManager?.adapter
        
        if (bluetoothAdapter == null) {
            result.bluetoothSupported = false
            result.addError("Bluetooth no está soportado en este dispositivo")
            Log.e(TAG, "Bluetooth no soportado")
            return result
        }
        
        result.bluetoothSupported = true
        Log.d(TAG, "✓ Bluetooth soportado")
        
        // 2. Verificar si Bluetooth está habilitado
        try {
            result.bluetoothEnabled = bluetoothAdapter.isEnabled
            Log.d(TAG, "✓ Bluetooth habilitado: ${result.bluetoothEnabled}")
        } catch (e: SecurityException) {
            result.addError("Error de seguridad al verificar estado de Bluetooth: ${e.message}")
            Log.e(TAG, "Error de seguridad al verificar estado de Bluetooth", e)
        }
        
        // 3. Verificar versión de Android
        result.androidVersion = Build.VERSION.SDK_INT
        Log.d(TAG, "✓ Versión Android: ${result.androidVersion} (${Build.VERSION.RELEASE})")
        
        // 4. Verificar permisos
        checkPermissions(context, result)
        
        // 5. Verificar capacidad de descubrimiento
        if (result.bluetoothEnabled) {
            checkDiscoveryCapability(bluetoothAdapter, result)
        }
        
        Log.d(TAG, "=== FIN DIAGNÓSTICO BLUETOOTH ===")
        Log.d(TAG, "Resumen: ${result.getSummary()}")
        
        return result
    }
    
    private fun checkPermissions(context: Context, result: DiagnosticResult) {
        Log.d(TAG, "--- Verificando permisos ---")
        
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
        
        for (permission in requiredPermissions) {
            val granted = ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            result.permissions[permission] = granted
            
            if (granted) {
                Log.d(TAG, "✓ $permission: CONCEDIDO")
            } else {
                Log.w(TAG, "✗ $permission: DENEGADO")
                result.addError("Permiso requerido no concedido: $permission")
            }
        }
    }
    
    private fun checkDiscoveryCapability(bluetoothAdapter: BluetoothAdapter, result: DiagnosticResult) {
        Log.d(TAG, "--- Verificando capacidad de descubrimiento ---")
        
        try {
            result.discoverySupported = bluetoothAdapter.isDiscovering != null
            Log.d(TAG, "✓ Descubrimiento soportado: ${result.discoverySupported}")
            
            if (bluetoothAdapter.isDiscovering) {
                Log.d(TAG, "⚠ Descubrimiento ya está en progreso")
                result.addWarning("Descubrimiento ya está en progreso")
            }
            
        } catch (e: SecurityException) {
            result.addError("Error de seguridad al verificar descubrimiento: ${e.message}")
            Log.e(TAG, "Error de seguridad al verificar descubrimiento", e)
        } catch (e: Exception) {
            result.addError("Error inesperado al verificar descubrimiento: ${e.message}")
            Log.e(TAG, "Error inesperado al verificar descubrimiento", e)
        }
    }
    
    /**
     * Clase para almacenar los resultados del diagnóstico
     */
    class DiagnosticResult {
        var bluetoothSupported = false
        var bluetoothEnabled = false
        var discoverySupported = false
        var androidVersion = 0
        val permissions = mutableMapOf<String, Boolean>()
        private val errors = mutableListOf<String>()
        private val warnings = mutableListOf<String>()
        
        fun addError(error: String) {
            errors.add(error)
        }
        
        fun addWarning(warning: String) {
            warnings.add(warning)
        }
        
        fun hasErrors(): Boolean = errors.isNotEmpty()
        
        fun getErrors(): List<String> = errors.toList()
        
        fun getWarnings(): List<String> = warnings.toList()
        
        fun areAllPermissionsGranted(): Boolean = permissions.values.all { it }
        
        fun getSummary(): String {
            return buildString {
                appendLine("Diagnóstico Bluetooth:")
                appendLine("- Soportado: $bluetoothSupported")
                appendLine("- Habilitado: $bluetoothEnabled")
                appendLine("- Descubrimiento soportado: $discoverySupported")
                appendLine("- Android: $androidVersion")
                appendLine("- Permisos concedidos: ${permissions.count { it.value }}/${permissions.size}")
                
                if (errors.isNotEmpty()) {
                    appendLine("Errores:")
                    errors.forEach { appendLine("  • $it") }
                }
                
                if (warnings.isNotEmpty()) {
                    appendLine("Advertencias:")
                    warnings.forEach { appendLine("  • $it") }
                }
            }
        }
        
        fun isReadyForDiscovery(): Boolean {
            return bluetoothSupported && 
                   bluetoothEnabled && 
                   discoverySupported && 
                   areAllPermissionsGranted() && 
                   !hasErrors()
        }
    }
}
