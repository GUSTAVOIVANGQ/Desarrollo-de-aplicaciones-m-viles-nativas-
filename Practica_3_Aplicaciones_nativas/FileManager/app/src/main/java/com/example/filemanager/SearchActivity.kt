package com.example.filemanager

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import java.io.File
import java.io.IOException

class SearchActivity : AppCompatActivity() {
    
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var noResultsText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var fileAdapter: FileAdapter
    private lateinit var permissionsManager: PermissionsManager
    
    private var searchJob: Job? = null
    private var rootDirectory: File? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before setting content view
        ThemeManager.applyTheme(this)
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Search Files"
        
        // Initialize views
        searchView = findViewById(R.id.search_view)
        recyclerView = findViewById(R.id.recyclerView)
        noResultsText = findViewById(R.id.no_results_text)
        progressBar = findViewById(R.id.progress_bar)
        
        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        
        // Initialize permissions manager
        permissionsManager = PermissionsManager(this)
        
        // Initialize adapter
        fileAdapter = FileAdapter(
            emptyList(),
            onItemClick = { fileItem ->
                onFileItemClick(fileItem)
            },
            onItemLongClick = { _, _ -> false }
        )
        recyclerView.adapter = fileAdapter
        
        // Check permissions first before accessing storage
        checkPermissions()
    }
    
    private fun checkPermissions() {
        permissionsManager.checkAndRequestPermissions(
            onPermissionsGranted = {
                // Get root directory to start search from
                rootDirectory = FileUtils.getExternalStorage() ?: FileUtils.getInternalStorage(this)
                
                // Set up search view
                setupSearchView()
            },
            onPermissionsDenied = {
                // Show a dialog explaining why permissions are needed
                MaterialAlertDialogBuilder(this)
                    .setTitle("Permissions Required")
                    .setMessage("Storage permissions are required to search files.")
                    .setPositiveButton("Settings") { _, _ ->
                        permissionsManager.openAppSettings()
                    }
                    .setNegativeButton("Exit") { _, _ ->
                        finish()
                    }
                    .setCancelable(false)
                    .show()
            }
        )
    }
    
    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (query.isNotEmpty()) {
                    performSearch(query)
                }
                return true
            }
            
            override fun onQueryTextChange(newText: String): Boolean {
                // Only search when query is at least 3 characters
                if (newText.length >= 3) {
                    performSearch(newText)
                } else if (newText.isEmpty()) {
                    clearSearchResults()
                }
                return true
            }
        })
        
        // Expand the search view by default
        searchView.isIconified = false
        searchView.requestFocus()
    }
    
    private fun performSearch(query: String) {
        // Verify read permissions first
        if (!PermissionsManager.hasReadPermission(this)) {
            Toast.makeText(this, "Storage permissions required to search files", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Cancel previous search if it's running
        searchJob?.cancel()
        
        // Reset views
        progressBar.isVisible = true
        noResultsText.isVisible = false
        recyclerView.isVisible = false
        
        // Start new search
        searchJob = lifecycleScope.launch(Dispatchers.IO) {
            try {
                val results = searchFiles(rootDirectory!!, query.lowercase())
                
                withContext(Dispatchers.Main) {
                    progressBar.isVisible = false
                    
                    if (results.isEmpty()) {
                        noResultsText.isVisible = true
                        recyclerView.isVisible = false
                    } else {
                        noResultsText.isVisible = false
                        recyclerView.isVisible = true
                        fileAdapter.updateFiles(results)
                    }
                }
            } catch (e: SecurityException) {
                withContext(Dispatchers.Main) {
                    progressBar.isVisible = false
                    Toast.makeText(this@SearchActivity, 
                        "Permission error: ${e.message}", 
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.isVisible = false
                    Toast.makeText(this@SearchActivity, 
                        "Error searching files: ${e.message}", 
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun clearSearchResults() {
        searchJob?.cancel()
        progressBar.isVisible = false
        noResultsText.isVisible = false
        fileAdapter.updateFiles(emptyList())
    }
    
    private suspend fun searchFiles(directory: File, query: String): List<FileItem> {
        val results = mutableListOf<FileItem>()

        // Fix: Check if the current coroutine is still active
        if (!coroutineContext.isActive) return results

        try {
            if (!directory.exists() || !directory.isDirectory || !directory.canRead()) {
                return results
            }
            
            val files = directory.listFiles() ?: return results

            for (file in files) {
                // Check if search was cancelled
                // Fix: Check if the current coroutine is still active
                if (!coroutineContext.isActive) return results

                try {
                    // Check if file name contains query
                    if (file.name.lowercase().contains(query)) {
                        results.add(FileItem(file))
                    }
                    
                    // Search in subdirectories (only if we have read access)
                    if (file.isDirectory && file.canRead()) {
                        // Add a small delay to prevent UI freezing and allow cancellation
                        if (results.size % 50 == 0) {
                            withContext(Dispatchers.Main) {
                                // Update progress if needed
                            }
                        }
                        
                        results.addAll(searchFiles(file, query))
                    }
                } catch (e: SecurityException) {
                    // Skip this file if we don't have permission
                    continue
                } catch (e: IOException) {
                    // Skip this file if we can't read it
                    continue
                }
            }
        } catch (e: Exception) {
            // Log error but continue with what we have
            e.printStackTrace()
        }
        
        return results
    }
    
    private fun onFileItemClick(fileItem: FileItem) {
        if (fileItem.isDirectory) {
            // Open the directory in MainActivity
            MainActivity.startWithDirectory(this, fileItem.file.absolutePath)
            finish()
        } else {
            // Open the file with FileViewerActivity
            FileViewerActivity.start(this, fileItem.file.absolutePath)
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Make sure to cancel any ongoing search operation
        searchJob?.cancel()
    }
}
