package com.example.filemanager

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Manages all permission-related operations for accessing storage
 */
class PermissionsManager(private val activity: AppCompatActivity) {

    private var onPermissionsGrantedCallback: (() -> Unit)? = null
    private var onPermissionsDeniedCallback: (() -> Unit)? = null

    // Permission request launcher for standard permissions
    private val requestPermissionLauncher: ActivityResultLauncher<Array<String>> = 
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                onPermissionsGrantedCallback?.invoke()
            } else {
                // Show rationale dialog if permissions denied
                showPermissionRationaleDialog()
            }
        }
    
    // Launcher for MANAGE_EXTERNAL_STORAGE permission (Android 11+)
    private val manageStorageLauncher: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    onPermissionsGrantedCallback?.invoke()
                } else {
                    onPermissionsDeniedCallback?.invoke()
                }
            }
        }
    
    /**
     * Checks and requests all necessary permissions based on Android version
     */
    fun checkAndRequestPermissions(
        onPermissionsGranted: () -> Unit,
        onPermissionsDenied: () -> Unit
    ) {
        this.onPermissionsGrantedCallback = onPermissionsGranted
        this.onPermissionsDeniedCallback = onPermissionsDenied
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11+ (API 30+), we need MANAGE_EXTERNAL_STORAGE permission
            if (Environment.isExternalStorageManager()) {
                onPermissionsGranted()
            } else {
                showManageStorageRationaleDialog()
            }
        } else {
            // For Android 10 and below
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
                )
            } else {
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
            
            if (permissions.all {
                    ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
                }) {
                onPermissionsGranted()
            } else if (permissions.any {
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
                }) {
                // We should show rationale before requesting permissions
                showPermissionRationaleDialog()
            } else {
                // First time asking or "Never ask again" was checked
                requestPermissionLauncher.launch(permissions)
            }
        }
    }
    
    /**
     * Shows a dialog explaining why we need MANAGE_EXTERNAL_STORAGE permission 
     */
    private fun showManageStorageRationaleDialog() {
        MaterialAlertDialogBuilder(activity)
            .setTitle("Storage Access Required")
            .setMessage("This app needs full storage access permission to manage your files. You will be redirected to the settings screen to grant this permission.")
            .setPositiveButton("Go to Settings") { _, _ ->
                openManageStorageSettings()
            }
            .setNegativeButton("Cancel") { _, _ ->
                onPermissionsDeniedCallback?.invoke()
            }
            .setCancelable(false)
            .show()
    }
    
    /**
     * Shows dialog explaining why we need storage permissions
     */
    private fun showPermissionRationaleDialog() {
        MaterialAlertDialogBuilder(activity)
            .setTitle("Storage Permissions Required")
            .setMessage("This file manager requires storage permissions to access and manage your files. Without these permissions, the app cannot function properly.")
            .setPositiveButton("Request Again") { _, _ ->
                val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_AUDIO
                    )
                } else {
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }
                requestPermissionLauncher.launch(permissions)
            }
            .setNegativeButton("Cancel") { _, _ ->
                onPermissionsDeniedCallback?.invoke()
            }
            .setCancelable(false)
            .show()
    }
    
    /**
     * Opens settings screen for MANAGE_EXTERNAL_STORAGE permission
     */
    private fun openManageStorageSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${activity.packageName}")
            }
            manageStorageLauncher.launch(intent)
        }
    }
    
    /**
     * Opens app settings to allow the user to enable permissions
     */
    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${activity.packageName}")
        }
        activity.startActivity(intent)
    }
    
    companion object {
        /**
         * Check if a file operation requires write permission and if that permission is granted
         */
        fun hasWritePermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
        
        /**
         * Check if a read operation is allowed
         */
        fun hasReadPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
    }
}
