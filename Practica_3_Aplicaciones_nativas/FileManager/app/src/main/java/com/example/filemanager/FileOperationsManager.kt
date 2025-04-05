package com.example.filemanager

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest

object FileOperationsManager {
    
    private const val TAG = "FileOperationsManager"
    
    /**
     * Result class to provide detailed information about file operations
     */
    data class OperationResult(
        val success: Boolean,
        val message: String,
        val errorType: ErrorType = ErrorType.NONE
    )
    
    /**
     * Types of errors that can occur during file operations
     */
    enum class ErrorType {
        NONE,
        PERMISSION_DENIED,
        FILE_EXISTS,
        FILE_NOT_FOUND,
        INVALID_OPERATION,
        IO_ERROR,
        SECURITY_ERROR,
        UNKNOWN
    }
    
    /**
     * Creates a new directory at the specified path with permission checks
     */
    fun createDirectory(context: Context, parentDir: File, dirName: String): OperationResult {
        // Validate inputs
        if (!isValidFileName(dirName)) {
            return OperationResult(
                false, 
                "Invalid directory name. Names cannot contain: \\ / : * ? \" < > |", 
                ErrorType.INVALID_OPERATION
            )
        }
        
        // Check write permission
        if (!PermissionsManager.hasWritePermission(context)) {
            return OperationResult(
                false, 
                "Permission denied. Write permission is required.", 
                ErrorType.PERMISSION_DENIED
            )
        }
        
        val newDir = File(parentDir, dirName)
        
        // Check if directory already exists
        if (newDir.exists()) {
            return OperationResult(
                false, 
                "A folder with this name already exists.", 
                ErrorType.FILE_EXISTS
            )
        }
        
        // Check if parent directory exists and is accessible
        if (!parentDir.exists() || !parentDir.isDirectory || !parentDir.canWrite()) {
            return OperationResult(
                false, 
                "Cannot create directory: parent folder is not writable.", 
                ErrorType.PERMISSION_DENIED
            )
        }
        
        return try {
            val success = newDir.mkdir()
            if (success) {
                OperationResult(true, "Directory created successfully.")
            } else {
                OperationResult(
                    false, 
                    "Failed to create directory for unknown reason.", 
                    ErrorType.UNKNOWN
                )
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security error creating directory: ${e.message}")
            OperationResult(
                false, 
                "Security error: ${e.message}", 
                ErrorType.SECURITY_ERROR
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating directory: ${e.message}")
            OperationResult(
                false, 
                "Error: ${e.message}", 
                ErrorType.UNKNOWN
            )
        }
    }
    
    /**
     * Deletes a file or directory with permission checks and validation
     */
    fun delete(context: Context, file: File): OperationResult {
        // Check if file exists
        if (!file.exists()) {
            return OperationResult(
                false, 
                "File or folder does not exist.", 
                ErrorType.FILE_NOT_FOUND
            )
        }
        
        // Check write permission
        if (!PermissionsManager.hasWritePermission(context)) {
            return OperationResult(
                false, 
                "Permission denied. Write permission is required to delete files.", 
                ErrorType.PERMISSION_DENIED
            )
        }
        
        // Check special system directories that shouldn't be deleted
        if (isProtectedSystemDirectory(file)) {
            return OperationResult(
                false, 
                "This is a protected system directory and cannot be deleted.", 
                ErrorType.SECURITY_ERROR
            )
        }
        
        // Special handling for Android 10+ with scoped storage
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && 
            !Environment.isExternalStorageManager() && 
            isInPublicExternalStorage(file)) {
            
            return deleteWithMediaStore(context, file)
        }
        
        return try {
            if (file.isDirectory) {
                // Delete directory contents first
                val contents = file.listFiles()
                if (contents != null) {
                    for (child in contents) {
                        val result = delete(context, child)
                        if (!result.success) {
                            return result // Propagate the error
                        }
                    }
                }
            }
            
            val success = file.delete()
            if (success) {
                OperationResult(true, "Deleted successfully.")
            } else {
                OperationResult(
                    false, 
                    "Failed to delete. The file might be in use or protected.", 
                    ErrorType.UNKNOWN
                )
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security error deleting file: ${e.message}")
            OperationResult(
                false, 
                "Security error: ${e.message}", 
                ErrorType.SECURITY_ERROR
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file: ${e.message}")
            OperationResult(
                false, 
                "Error: ${e.message}", 
                ErrorType.UNKNOWN
            )
        }
    }
    
    /**
     * Delete file using MediaStore API for Android 10+ (API 29+)
     */
    private fun deleteWithMediaStore(context: Context, file: File): OperationResult {
        try {
            val contentResolver = context.contentResolver
            val uri = getMediaStoreUri(context, file)
            
            if (uri != null) {
                val deletedRows = contentResolver.delete(uri, null, null)
                return if (deletedRows > 0) {
                    OperationResult(true, "Deleted successfully.")
                } else {
                    OperationResult(
                        false, 
                        "Failed to delete file through MediaStore.", 
                        ErrorType.UNKNOWN
                    )
                }
            } else {
                return OperationResult(
                    false, 
                    "File not found in MediaStore.", 
                    ErrorType.FILE_NOT_FOUND
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting with MediaStore: ${e.message}")
            return OperationResult(
                false, 
                "Error: ${e.message}", 
                ErrorType.UNKNOWN
            )
        }
    }
    
    /**
     * Renames a file or directory with permission and validation checks
     */
    fun rename(context: Context, file: File, newName: String): OperationResult {
        // Validate inputs
        if (!isValidFileName(newName)) {
            return OperationResult(
                false, 
                "Invalid file name. Names cannot contain: \\ / : * ? \" < > |", 
                ErrorType.INVALID_OPERATION
            )
        }
        
        // Check if file exists
        if (!file.exists()) {
            return OperationResult(
                false, 
                "File does not exist.", 
                ErrorType.FILE_NOT_FOUND
            )
        }
        
        // Check write permission
        if (!PermissionsManager.hasWritePermission(context)) {
            return OperationResult(
                false, 
                "Permission denied. Write permission is required to rename files.", 
                ErrorType.PERMISSION_DENIED
            )
        }
        
        // Get proper file extension
        val extension = if (file.isFile) {
            val lastDot = file.name.lastIndexOf('.')
            if (lastDot > 0) file.name.substring(lastDot) else ""
        } else ""
        
        // Create new file name with extension if needed
        val newFileName = if (file.isFile && extension.isNotEmpty()) {
            if (newName.endsWith(extension)) newName else "$newName$extension"
        } else {
            newName
        }
        
        // Create target file
        val newFile = File(file.parent, newFileName)
        
        // Check if new file name already exists
        if (newFile.exists()) {
            return OperationResult(
                false, 
                "A file with this name already exists.", 
                ErrorType.FILE_EXISTS
            )
        }
        
        // Special handling for Android 10+ with scoped storage
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && 
            !Environment.isExternalStorageManager() && 
            isInPublicExternalStorage(file)) {
            
            return renameWithMediaStore(context, file, newFileName)
        }
        
        return try {
            val success = file.renameTo(newFile)
            if (success) {
                OperationResult(true, "Renamed successfully.")
            } else {
                OperationResult(
                    false, 
                    "Failed to rename. The file might be in use or protected.", 
                    ErrorType.UNKNOWN
                )
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security error renaming file: ${e.message}")
            OperationResult(
                false, 
                "Security error: ${e.message}", 
                ErrorType.SECURITY_ERROR
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error renaming file: ${e.message}")
            OperationResult(
                false, 
                "Error: ${e.message}", 
                ErrorType.UNKNOWN
            )
        }
    }
    
    /**
     * Rename file using MediaStore API for Android 10+ (API 29+)
     */
    private fun renameWithMediaStore(context: Context, file: File, newFileName: String): OperationResult {
        try {
            val contentResolver = context.contentResolver
            val uri = getMediaStoreUri(context, file)
            
            if (uri != null) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, newFileName)
                }
                
                val updated = contentResolver.update(uri, values, null, null)
                return if (updated > 0) {
                    OperationResult(true, "Renamed successfully.")
                } else {
                    OperationResult(
                        false, 
                        "Failed to rename file through MediaStore.", 
                        ErrorType.UNKNOWN
                    )
                }
            } else {
                return OperationResult(
                    false, 
                    "File not found in MediaStore.", 
                    ErrorType.FILE_NOT_FOUND
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error renaming with MediaStore: ${e.message}")
            return OperationResult(
                false, 
                "Error: ${e.message}", 
                ErrorType.UNKNOWN
            )
        }
    }
    
    /**
     * Copies a file to a destination directory with permission checks
     */
    fun copyFile(context: Context, sourceFile: File, destDir: File): OperationResult {
        // Check source file existence
        if (!sourceFile.exists()) {
            return OperationResult(
                false, 
                "Source file does not exist.", 
                ErrorType.FILE_NOT_FOUND
            )
        }
        
        // Check destination directory
        if (!destDir.exists() || !destDir.isDirectory) {
            return OperationResult(
                false, 
                "Destination is not a valid directory.", 
                ErrorType.INVALID_OPERATION
            )
        }
        
        // Check if source and destination are the same
        if (sourceFile.parentFile?.canonicalPath == destDir.canonicalPath) {
            return OperationResult(
                false, 
                "Source and destination directories are the same.", 
                ErrorType.INVALID_OPERATION
            )
        }
        
        // Check read permission
        if (!PermissionsManager.hasReadPermission(context)) {
            return OperationResult(
                false, 
                "Permission denied. Read permission is required for the source file.", 
                ErrorType.PERMISSION_DENIED
            )
        }
        
        // Check write permission for destination
        if (!PermissionsManager.hasWritePermission(context)) {
            return OperationResult(
                false, 
                "Permission denied. Write permission is required for the destination directory.", 
                ErrorType.PERMISSION_DENIED
            )
        }
        
        // Create destination file
        val destFile = File(destDir, sourceFile.name)
        
        // Check if destination file already exists
        if (destFile.exists()) {
            return OperationResult(
                false, 
                "A file with the same name already exists in the destination directory.", 
                ErrorType.FILE_EXISTS
            )
        }
        
        try {
            if (sourceFile.isDirectory) {
                // For directories, create a new directory and copy contents
                val newDir = File(destDir, sourceFile.name)
                
                if (!newDir.exists() && !newDir.mkdir()) {
                    return OperationResult(
                        false, 
                        "Failed to create destination directory.", 
                        ErrorType.IO_ERROR
                    )
                }
                
                // Copy each file in the directory
                val files = sourceFile.listFiles()
                if (files != null) {
                    for (childFile in files) {
                        val result = copyFile(context, childFile, newDir)
                        if (!result.success) {
                            return result // Propagate the error
                        }
                    }
                }
                
                return OperationResult(true, "Directory copied successfully.")
            } else {
                // Calculate available space in destination
                val requiredSpace = sourceFile.length()
                val availableSpace = destDir.freeSpace
                
                if (requiredSpace > availableSpace) {
                    return OperationResult(
                        false, 
                        "Not enough space available in the destination directory.", 
                        ErrorType.IO_ERROR
                    )
                }
                
                // For files, copy the file content
                FileInputStream(sourceFile).use { input ->
                    FileOutputStream(destFile).use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var length: Int
                        var totalCopied: Long = 0
                        val fileSize = sourceFile.length()
                        
                        while (input.read(buffer).also { length = it } > 0) {
                            output.write(buffer, 0, length)
                            totalCopied += length
                            
                            // Verify integrity for large files by checking progress
                            if (fileSize > 10 * 1024 * 1024 && totalCopied % (5 * 1024 * 1024) < DEFAULT_BUFFER_SIZE) { // Every ~5MB
                                if (Thread.interrupted()) {
                                    output.close()
                                    destFile.delete()
                                    return OperationResult(
                                        false, 
                                        "Operation cancelled.", 
                                        ErrorType.UNKNOWN
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Verify file integrity with size comparison
                if (sourceFile.length() != destFile.length()) {
                    destFile.delete() // Clean up the incomplete file
                    return OperationResult(
                        false, 
                        "File copy verification failed. Source and destination file sizes don't match.", 
                        ErrorType.IO_ERROR
                    )
                }
                
                return OperationResult(true, "File copied successfully.")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security error copying file: ${e.message}")
            // Clean up the potentially partially copied file
            if (destFile.exists()) {
                destFile.delete()
            }
            return OperationResult(
                false, 
                "Security error: ${e.message}", 
                ErrorType.SECURITY_ERROR
            )
        } catch (e: IOException) {
            Log.e(TAG, "IO error copying file: ${e.message}")
            // Clean up the potentially partially copied file
            if (destFile.exists()) {
                destFile.delete()
            }
            return OperationResult(
                false, 
                "I/O error: ${e.message}", 
                ErrorType.IO_ERROR
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error copying file: ${e.message}")
            // Clean up the potentially partially copied file
            if (destFile.exists()) {
                destFile.delete()
            }
            return OperationResult(
                false, 
                "Error: ${e.message}", 
                ErrorType.UNKNOWN
            )
        }
    }
    
    /**
     * Moves a file to a destination directory with permission checks
     */
    fun moveFile(context: Context, sourceFile: File, destDir: File): OperationResult {
        // Check source file existence
        if (!sourceFile.exists()) {
            return OperationResult(
                false, 
                "Source file does not exist.", 
                ErrorType.FILE_NOT_FOUND
            )
        }
        
        // Check destination directory
        if (!destDir.exists() || !destDir.isDirectory) {
            return OperationResult(
                false, 
                "Destination is not a valid directory.", 
                ErrorType.INVALID_OPERATION
            )
        }
        
        // Check if source and destination are the same
        if (sourceFile.parentFile?.canonicalPath == destDir.canonicalPath) {
            return OperationResult(
                false, 
                "Source and destination directories are the same.", 
                ErrorType.INVALID_OPERATION
            )
        }
        
        // Create destination file
        val destFile = File(destDir, sourceFile.name)
        
        // Check if destination file already exists
        if (destFile.exists()) {
            return OperationResult(
                false, 
                "A file with the same name already exists in the destination directory.", 
                ErrorType.FILE_EXISTS
            )
        }
        
        // Try direct move first (works if on same filesystem)
        try {
            if (sourceFile.renameTo(destFile)) {
                return OperationResult(true, "File moved successfully.")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security error moving file: ${e.message}")
            return OperationResult(
                false, 
                "Security error: ${e.message}", 
                ErrorType.SECURITY_ERROR
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error trying to move file directly: ${e.message}")
            // Continue to try copy+delete method
        }
        
        // If direct rename fails, copy and then delete original
        val copyResult = copyFile(context, sourceFile, destDir)
        if (!copyResult.success) {
            return copyResult
        }
        
        // If copy was successful, delete the original
        val deleteResult = delete(context, sourceFile)
        if (!deleteResult.success) {
            // If deletion failed, we should inform but still consider the move partially successful
            return OperationResult(
                true, 
                "File copied to destination but could not delete the original: ${deleteResult.message}", 
                ErrorType.NONE
            )
        }
        
        return OperationResult(true, "File moved successfully.")
    }
    
    /**
     * Gets the size of a file or directory with permission check
     */
    fun getSize(context: Context, file: File): Long {
        if (!file.exists()) {
            return 0
        }
        
        // Check read permission
        if (!PermissionsManager.hasReadPermission(context)) {
            return 0
        }
        
        return try {
            if (file.isFile) {
                file.length()
            } else {
                var size = 0L
                file.listFiles()?.forEach { child ->
                    size += getSize(context, child)
                }
                size
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security error getting size: ${e.message}")
            0
        } catch (e: Exception) {
            Log.e(TAG, "Error getting size: ${e.message}")
             0
        }
    }
    
    /**
     * Share a file with other apps securely using FileProvider
     */
    fun shareFile(context: Context, file: File) {
        try {
            if (!file.exists()) {
                Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show()
                return
            }
            
            if (!file.canRead()) {
                Toast.makeText(context, "Cannot read file", Toast.LENGTH_SHORT).show()
                return
            }
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            
            val intent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                type = getMimeType(file)
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(android.content.Intent.createChooser(intent, "Share file via"))
        } catch (e: SecurityException) {
            Log.e(TAG, "Security error sharing file: ${e.message}")
            Toast.makeText(context, "Security error sharing file: ${e.message}", Toast.LENGTH_SHORT).show()
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid file for FileProvider: ${e.message}")
            Toast.makeText(context, "Cannot share this file: ${e.message}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing file: ${e.message}")
            Toast.makeText(context, "Error sharing file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Get file MIME type
     */
    fun getMimeType(file: File): String {
        return if (file.isDirectory) {
            "resource/folder"
        } else {
            val extension = MimeTypeMap.getFileExtensionFromUrl(file.absolutePath)
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: when (extension) {
                "txt" -> "text/plain"
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "pdf" -> "application/pdf"
                "doc", "docx" -> "application/msword"
                "mp3" -> "audio/mp3"
                "mp4" -> "video/mp4"
                "json" -> "application/json"
                "xml" -> "application/xml"
                else -> "*/*"
            }
        }
    }
    
    /**
     * Checks if a filename is valid (does not contain invalid characters)
     */
    private fun isValidFileName(fileName: String): Boolean {
        return !fileName.isEmpty() && 
               !fileName.contains("/") && 
               !fileName.contains("\\") && 
               !fileName.contains(":") && 
               !fileName.contains("*") && 
               !fileName.contains("?") && 
               !fileName.contains("\"") && 
               !fileName.contains("<") && 
               !fileName.contains(">") && 
               !fileName.contains("|") &&
               fileName.length <= 255
    }
    
    /**
     * Checks if a directory is a protected system directory
     */
    private fun isProtectedSystemDirectory(file: File): Boolean {
        val systemDirectories = listOf(
            Environment.getRootDirectory().absolutePath,
            "/system",
            "/sys",
            "/proc",
            "/dev",
            "/etc",
            "/vendor",
            "/product"
        )
        
        val path = file.absolutePath
        return systemDirectories.any { path.startsWith(it) }
    }
    
    /**
     * Checks if file is in public external storage
     */
    private fun isInPublicExternalStorage(file: File): Boolean {
        val externalDir = Environment.getExternalStorageDirectory()
        return file.absolutePath.startsWith(externalDir.absolutePath)
    }
    
    /**
     * Gets MediaStore Uri for a file
     */
    private fun getMediaStoreUri(context: Context, file: File): Uri? {
        val contentResolver = context.contentResolver
        val projection = arrayOf(MediaStore.MediaColumns._ID)
        val selection = "${MediaStore.MediaColumns.DATA} = ?"
        val selectionArgs = arrayOf(file.absolutePath)
        
        val queryUri = when {
            file.name.lowercase().endsWith(".jpg") || 
            file.name.lowercase().endsWith(".jpeg") || 
            file.name.lowercase().endsWith(".png") || 
            file.name.lowercase().endsWith(".gif") -> {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            file.name.lowercase().endsWith(".mp4") || 
            file.name.lowercase().endsWith(".3gp") || 
            file.name.lowercase().endsWith(".mkv") -> {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
            file.name.lowercase().endsWith(".mp3") || 
            file.name.lowercase().endsWith(".wav") || 
            file.name.lowercase().endsWith(".ogg") -> {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
                } else {
                    MediaStore.Files.getContentUri("external")
                }
            }
        }
        
        try {
            contentResolver.query(
                queryUri,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                    return Uri.withAppendedPath(queryUri, id.toString())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying MediaStore: ${e.message}")
        }
        
        return null
    }
}
