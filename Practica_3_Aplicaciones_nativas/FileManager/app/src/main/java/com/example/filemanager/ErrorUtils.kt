package com.example.filemanager

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.IOException
import java.security.GeneralSecurityException

/**
 * Utility class for handling errors and displaying appropriate messages
 */
object ErrorUtils {
    
    private const val TAG = "ErrorUtils"
    
    /**
     * Shows a toast message for an error, with optional logging
     */
    fun showErrorToast(context: Context, message: String, exception: Exception? = null) {
        if (exception != null) {
            Log.e(TAG, message, exception)
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Shows an appropriate error dialog based on exception type
     */
    fun showErrorDialog(context: Context, exception: Exception, operation: String) {
        val title: String
        val message: String
        
        when (exception) {
            is SecurityException -> {
                title = "Permission Error"
                message = "You don't have permission to $operation. Please check your app permissions in settings."
                Log.e(TAG, "Security error during $operation", exception)
            }
            is IOException -> {
                title = "Storage Error"
                message = "There was a problem accessing the file system: ${exception.localizedMessage}"
                Log.e(TAG, "IO error during $operation", exception)
            }
            is OutOfMemoryError -> {
                title = "Out of Memory"
                message = "The file is too large to process with the available memory."
                Log.e(TAG, "OOM error during $operation", exception)
            }
            is IllegalArgumentException -> {
                title = "Invalid Operation"
                message = "The requested operation couldn't be completed: ${exception.localizedMessage}"
                Log.e(TAG, "Invalid operation during $operation", exception)
            }
            is GeneralSecurityException -> {
                title = "Security Error"
                message = "A security issue occurred while trying to $operation."
                Log.e(TAG, "Security error during $operation", exception)
            }
            else -> {
                title = "Error"
                message = "An unexpected error occurred: ${exception.localizedMessage ?: exception.javaClass.simpleName}"
                Log.e(TAG, "Unexpected error during $operation", exception)
            }
        }
        
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
    
    /**
     * Shows an error dialog for file operations based on result
     */
    fun showOperationResultDialog(
        context: Context, 
        result: FileOperationsManager.OperationResult,
        operation: String
    ) {
        if (result.success) {
            // Operation was successful, just show a toast
            Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
            return
        }
        
        val title = when (result.errorType) {
            FileOperationsManager.ErrorType.PERMISSION_DENIED -> "Permission Error"
            FileOperationsManager.ErrorType.FILE_EXISTS -> "File Already Exists"
            FileOperationsManager.ErrorType.FILE_NOT_FOUND -> "File Not Found"
            FileOperationsManager.ErrorType.INVALID_OPERATION -> "Invalid Operation"
            FileOperationsManager.ErrorType.IO_ERROR -> "I/O Error"
            FileOperationsManager.ErrorType.SECURITY_ERROR -> "Security Error"
            else -> "Operation Failed"
        }
        
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage("${result.message}\n\nError occurred while trying to $operation.")
            .setPositiveButton("OK", null)
            .show()
    }
}
