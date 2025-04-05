package com.example.filemanager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import java.io.File

class FileViewerActivity : AppCompatActivity() {
    
    private lateinit var textViewerView: View
    private lateinit var imageViewerView: View
    private lateinit var codeViewerView: View
    private lateinit var unsupportedFileView: View
    
    private var filePath: String? = null
    private var file: File? = null
    
    companion object {
        private const val EXTRA_FILE_PATH = "file_path"
        
        fun start(context: Context, filePath: String) {
            val intent = Intent(context, FileViewerActivity::class.java).apply {
                putExtra(EXTRA_FILE_PATH, filePath)
            }
            context.startActivity(intent)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before setting content view
        ThemeManager.applyTheme(this)
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_file_viewer)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Initialize views
        textViewerView = findViewById(R.id.text_viewer_container)
        imageViewerView = findViewById(R.id.image_viewer_container)
        codeViewerView = findViewById(R.id.code_viewer_container)
        unsupportedFileView = findViewById(R.id.unsupported_file_container)
        
        // Get file path from intent
        filePath = intent.getStringExtra(EXTRA_FILE_PATH)
        if (filePath == null) {
            Toast.makeText(this, "Error: No file path provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        file = File(filePath!!)
        if (!file!!.exists() || !file!!.canRead()) {
            Toast.makeText(this, "Error: Cannot read file", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Set activity title to file name
        title = file!!.name
        
        // Load file content based on file type
        loadFileContent()
    }
    
    private fun loadFileContent() {
        // Hide all views initially
        textViewerView.isVisible = false
        imageViewerView.isVisible = false
        codeViewerView.isVisible = false
        unsupportedFileView.isVisible = false
        
        val fileName = file!!.name.lowercase()
        
        when {
            // Text files
            fileName.endsWith(".txt") || fileName.endsWith(".md") -> {
                showTextViewer()
            }
            
            // Code files (JSON, XML)
            fileName.endsWith(".json") || fileName.endsWith(".xml") -> {
                showCodeViewer()
            }
            
            // Image files
            fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
            fileName.endsWith(".png") || fileName.endsWith(".gif") ||
            fileName.endsWith(".bmp") -> {
                showImageViewer()
            }
            
            // Unsupported file types
            else -> {
                showUnsupportedFileView()
            }
        }
    }
    
    private fun showTextViewer() {
        textViewerView.isVisible = true
        
        val textViewFragment = TextViewerFragment.newInstance(filePath!!)
        supportFragmentManager.beginTransaction()
            .replace(R.id.text_viewer_container, textViewFragment)
            .commit()
    }
    
    private fun showCodeViewer() {
        codeViewerView.isVisible = true
        
        val codeViewerFragment = CodeViewerFragment.newInstance(filePath!!)
        supportFragmentManager.beginTransaction()
            .replace(R.id.code_viewer_container, codeViewerFragment)
            .commit()
    }
    
    private fun showImageViewer() {
        imageViewerView.isVisible = true
        
        val imageViewerFragment = ImageViewerFragment.newInstance(filePath!!)
        supportFragmentManager.beginTransaction()
            .replace(R.id.image_viewer_container, imageViewerFragment)
            .commit()
    }
    
    private fun showUnsupportedFileView() {
        unsupportedFileView.isVisible = true
        
        // Set up button to open with other apps
        findViewById<View>(R.id.btn_open_with).setOnClickListener {
            openWithOtherApp()
        }
    }
    
    private fun openWithOtherApp() {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                file!!
            )
            
            val intent = Intent(Intent.ACTION_VIEW)
            val mimeType = getMimeType(file!!.absolutePath)
            intent.setDataAndType(uri, mimeType)
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            
            startActivity(Intent.createChooser(intent, "Open file with..."))
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun getMimeType(filePath: String): String {
        val extension = MimeTypeMap.getFileExtensionFromUrl(filePath)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
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
}
