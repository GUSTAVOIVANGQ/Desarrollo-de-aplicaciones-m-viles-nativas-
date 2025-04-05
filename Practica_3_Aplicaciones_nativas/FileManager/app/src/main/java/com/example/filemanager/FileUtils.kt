package com.example.filemanager

import android.content.Context
import android.os.Environment
import java.io.File

object FileUtils {
    
    // Get the list of files in a directory
    fun getFilesList(directory: File): List<FileItem> {
        val files = directory.listFiles() ?: return emptyList()
        
        // First directories, then files, both alphabetically
        return files
            .filter { it.canRead() }
            .map { FileItem(it) }
            .sortedWith(compareBy<FileItem> { !it.isDirectory }.thenBy { it.name.lowercase() })
    }
    
    // Get internal storage root directory
    fun getInternalStorage(context: Context): File {
        return context.filesDir
    }
    
    // Get external storage root directory (if available)
    fun getExternalStorage(): File? {
        return if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            Environment.getExternalStorageDirectory()
        } else {
            null
        }
    }
    
    // Get parent directory
    fun getParentDirectory(currentDirectory: File): File? {
        val parent = currentDirectory.parentFile
        return if (parent != null && parent.canRead()) parent else null
    }
    
    // Create path segments for breadcrumb navigation
    fun getPathSegments(path: String): List<String> {
        val segments = mutableListOf<String>()
        var currentPath = ""
        
        path.split(File.separator).filter { it.isNotEmpty() }.forEach { segment ->
            currentPath += "${File.separator}$segment"
            segments.add(currentPath)
        }
        
        return segments
    }
}
