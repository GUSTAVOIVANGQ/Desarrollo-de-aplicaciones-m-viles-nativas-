package com.example.filemanager

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.OnBackPressedCallback
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: View
    private lateinit var breadcrumbChipGroup: ChipGroup
    private lateinit var fileAdapter: FileAdapter
    
    private var currentDirectory: File? = null
    
    // Variables for copy/cut operations
    private var clipboardFile: File? = null
    private var isCut = false
    private var sortOrder = SortOrder.BY_NAME
    
    private lateinit var permissionsManager: PermissionsManager
    
    companion object {
        private const val EXTRA_DIRECTORY_PATH = "directory_path"
        
        fun startWithDirectory(context: Context, directoryPath: String) {
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra(EXTRA_DIRECTORY_PATH, directoryPath)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before setting content view
        ThemeManager.applyTheme(this)
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Initialize and set up toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerView)
        emptyView = findViewById(R.id.emptyView)
        breadcrumbChipGroup = findViewById(R.id.breadcrumbChipGroup)
        
        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        
        // Initialize adapter with long click listener
        fileAdapter = FileAdapter(emptyList(), 
            onItemClick = { fileItem ->
                onFileItemClick(fileItem)
            },
            onItemLongClick = { fileItem, view ->
                selectedFile = fileItem.file
                showContextMenuForFile(view, fileItem.file)
                true // Return true to indicate we've handled the long press
            }
        )
        recyclerView.adapter = fileAdapter
        
        // Register for context menu
        registerForContextMenu(recyclerView)
        
        // Set up floating action button
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            // Create a new folder in the current directory
            showCreateFolderDialog()
        }
        
        // Initialize permissions manager
        permissionsManager = PermissionsManager(this)
        
        // Check if we were launched with a specific directory
        val directoryPath = intent.getStringExtra(EXTRA_DIRECTORY_PATH)
        if (directoryPath != null) {
            val directory = File(directoryPath)
            if (directory.exists() && directory.isDirectory) {
                currentDirectory = directory
            }
        }

        // Check and request permissions
        checkPermissions()
    }
    
    // Variable to store the currently selected file
    private var selectedFile: File? = null
    
    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        // We're not using this method anymore, using the showContextMenuForFile method instead
    }
    
    private fun showContextMenuForFile(view: View, file: File) {
        val popup = android.widget.PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.context_menu_file, popup.menu)
        
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_copy -> {
                    clipboardFile = file
                    isCut = false
                    invalidateOptionsMenu() // Update options menu to show paste
                    Toast.makeText(this, "File copied to clipboard", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_cut -> {
                    clipboardFile = file
                    isCut = true
                    invalidateOptionsMenu() // Update options menu to show paste
                    Toast.makeText(this, "File cut to clipboard", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_rename -> {
                    showRenameDialog(file)
                    true
                }
                R.id.action_delete -> {
                    showDeleteConfirmation(file)
                    true
                }
                R.id.action_share -> {
                    FileOperationsManager.shareFile(this, file)
                    true
                }
                R.id.action_properties -> {
                    showFileProperties(file)
                    true
                }
                else -> false
            }
        }
        
        popup.show()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // Show paste option only if there's a file in clipboard
        menu.findItem(R.id.action_paste).isVisible = clipboardFile != null
        
        // Update the checked sort order
        when (sortOrder) {
            SortOrder.BY_NAME -> menu.findItem(R.id.sort_by_name).isChecked = true
            SortOrder.BY_DATE -> menu.findItem(R.id.sort_by_date).isChecked = true
            SortOrder.BY_SIZE -> menu.findItem(R.id.sort_by_size).isChecked = true
            SortOrder.BY_TYPE -> menu.findItem(R.id.sort_by_type).isChecked = true
        }
        return super.onPrepareOptionsMenu(menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> {
                startActivity(Intent(this, SearchActivity::class.java))
                return true
            }
            R.id.action_paste -> {
                // Check write permission first
                if (!PermissionsManager.hasWritePermission(this)) {
                    Toast.makeText(this, "Write permission is required to paste files", Toast.LENGTH_SHORT).show()
                    return true
                }
                
                currentDirectory?.let { dir ->
                    clipboardFile?.let { file ->
                        val result = if (isCut) {
                            FileOperationsManager.moveFile(this, file, dir)
                        } else {
                            FileOperationsManager.copyFile(this, file, dir)
                        }
                        
                        if (result.success) {
                            Toast.makeText(
                                this,
                                result.message,
                                Toast.LENGTH_SHORT
                            ).show()
                            
                            // Clear clipboard if it was a cut operation
                            if (isCut) {
                                clipboardFile = null
                                invalidateOptionsMenu()
                            }
                            
                            // Refresh the directory
                            loadDirectory(dir)
                        } else {
                            Toast.makeText(
                                this,
                                result.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                return true
            }
            R.id.action_new_folder -> {
                showCreateFolderDialog()
                return true
            }
            R.id.sort_by_name -> {
                sortOrder = SortOrder.BY_NAME
                currentDirectory?.let { loadDirectory(it) }
                return true
            }
            R.id.sort_by_date -> {
                sortOrder = SortOrder.BY_DATE
                currentDirectory?.let { loadDirectory(it) }
                return true
            }
            R.id.sort_by_size -> {
                sortOrder = SortOrder.BY_SIZE
                currentDirectory?.let { loadDirectory(it) }
                return true
            }
            R.id.sort_by_type -> {
                sortOrder = SortOrder.BY_TYPE
                currentDirectory?.let { loadDirectory(it) }
                return true
            }
            R.id.theme_ipn -> {
                setAppTheme(ThemeManager.Theme.IPN)
                return true
            }
            R.id.theme_escom -> {
                setAppTheme(ThemeManager.Theme.ESCOM)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    
    private fun setAppTheme(theme: ThemeManager.Theme) {
        ThemeManager.setTheme(this, theme)
        Toast.makeText(this, R.string.theme_changed, Toast.LENGTH_SHORT).show()
        
        // Recreate activity to apply new theme
        recreate()
    }
    
    private fun checkPermissions() {
        permissionsManager.checkAndRequestPermissions(
            onPermissionsGranted = {
                loadInitialDirectory()
            },
            onPermissionsDenied = {
                // Show a dialog explaining why permissions are needed
                MaterialAlertDialogBuilder(this)
                    .setTitle("Permissions Required")
                    .setMessage("This app requires storage permissions to function. Without these permissions, you won't be able to access and manage files.")
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
    
    private fun loadInitialDirectory() {
        // Start with external storage if available, or internal storage
        currentDirectory = FileUtils.getExternalStorage() ?: FileUtils.getInternalStorage(this)
        loadDirectory(currentDirectory!!)
    }
    
    private fun loadDirectory(directory: File) {
        currentDirectory = directory
        
        // Load files
        val files = FileUtils.getFilesList(directory)
        
        // Apply sorting
        val sortedFiles = when (sortOrder) {
            SortOrder.BY_NAME -> files.sortedWith(compareBy<FileItem> { !it.isDirectory }.thenBy { it.name.lowercase() })
            SortOrder.BY_DATE -> files.sortedWith(compareBy<FileItem> { !it.isDirectory }.thenByDescending { it.file.lastModified() })
            SortOrder.BY_SIZE -> files.sortedWith(compareBy<FileItem> { !it.isDirectory }.thenByDescending { it.file.length() })
            SortOrder.BY_TYPE -> files.sortedWith(compareBy<FileItem> { !it.isDirectory }.thenBy { it.file.extension.lowercase() })
        }
        
        fileAdapter.updateFiles(sortedFiles)
        
        // Update empty view visibility
        emptyView.isVisible = files.isEmpty()
        
        // Update breadcrumb navigation
        updateBreadcrumbs(directory.absolutePath)
        
        // Update toolbar title
        supportActionBar?.title = directory.name.takeIf { it.isNotEmpty() } ?: "Root"
    }
    
    private fun updateBreadcrumbs(path: String) {
        breadcrumbChipGroup.removeAllViews()
        
        // Add root chip
        val rootChip = Chip(this).apply {
            text = "Root"
            isCheckable = false
            isClickable = true
            setOnClickListener {
                loadInitialDirectory()
            }
        }
        breadcrumbChipGroup.addView(rootChip)
        
        // Get path segments
        val segments = FileUtils.getPathSegments(path)
        
        // Add chip for each segment
        segments.forEachIndexed { index, segment ->
            val chip = Chip(this).apply {
                text = File(segment).name
                isCheckable = false
                isClickable = true
                setOnClickListener {
                    loadDirectory(File(segment))
                }
            }
            breadcrumbChipGroup.addView(chip)
        }
    }
    
    private fun onFileItemClick(fileItem: FileItem) {
        if (fileItem.isDirectory) {
            // Navigate to the directory
            loadDirectory(fileItem.file)
        } else {
            // Open the file with FileViewerActivity
            FileViewerActivity.start(this, fileItem.file.absolutePath)
        }
    }
    
    private fun showCreateFolderDialog() {
        // Check write permission first
        if (!PermissionsManager.hasWritePermission(this)) {
            Toast.makeText(this, "Write permission is required to create folders", Toast.LENGTH_SHORT).show()
            return
        }
        
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_folder, null)
        val editText = dialogView.findViewById<EditText>(R.id.editTextFolderName)
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Create Folder")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val folderName = editText.text.toString()
                if (folderName.isNotEmpty()) {
                    currentDirectory?.let { dir ->
                        val result = FileOperationsManager.createDirectory(this, dir, folderName)
                        if (result.success) {
                            loadDirectory(dir) // Refresh
                            Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Folder name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showRenameDialog(file: File) {
        // Check write permission first
        if (!PermissionsManager.hasWritePermission(this)) {
            Toast.makeText(this, "Write permission is required to rename files", Toast.LENGTH_SHORT).show()
            return
        }
        
        val dialogView = layoutInflater.inflate(R.layout.dialog_rename, null)
        val editText = dialogView.findViewById<EditText>(R.id.editTextNewName)
        
        // Get file name without extension if it's a file
        val nameWithoutExtension = if (file.isFile) {
            val lastDot = file.name.lastIndexOf('.')
            if (lastDot > 0) file.name.substring(0, lastDot) else file.name
        } else {
            file.name
        }
        
        editText.setText(nameWithoutExtension)
        editText.setSelection(nameWithoutExtension.length)
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Rename")
            .setView(dialogView)
            .setPositiveButton("Rename") { _, _ ->
                val newName = editText.text.toString()
                if (newName.isNotEmpty()) {
                    val result = FileOperationsManager.rename(this, file, newName)
                    if (result.success) {
                        currentDirectory?.let { loadDirectory(it) } // Refresh
                        Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "File name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showDeleteConfirmation(file: File) {
        // Check write permission first
        if (!PermissionsManager.hasWritePermission(this)) {
            Toast.makeText(this, "Write permission is required to delete files", Toast.LENGTH_SHORT).show()
            return
        }
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete ${if (file.isDirectory) "Folder" else "File"}")
            .setMessage("Are you sure you want to delete ${file.name}?")
            .setPositiveButton("Delete") { _, _ ->
                val result = FileOperationsManager.delete(this, file)
                if (result.success) {
                    currentDirectory?.let { loadDirectory(it) } // Refresh
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showFileProperties(file: File) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_file_properties, null)
        
        // Fill in the file properties
        dialogView.findViewById<TextView>(R.id.file_name).text = file.name
        dialogView.findViewById<TextView>(R.id.file_path).text = file.absolutePath
        
        val size = FileOperationsManager.getSize(this, file)
        dialogView.findViewById<TextView>(R.id.file_size).text = formatSize(size)
        
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val lastModified = sdf.format(Date(file.lastModified()))
        dialogView.findViewById<TextView>(R.id.file_modified).text = lastModified
        
        dialogView.findViewById<TextView>(R.id.file_type).text = when {
            file.isDirectory -> "Directory"
            else -> getFileType(file.name)
        }
        
        val permissions = StringBuilder()
        if (file.canRead()) permissions.append("Read ")
        if (file.canWrite()) permissions.append("Write ")
        if (file.canExecute()) permissions.append("Execute")
        dialogView.findViewById<TextView>(R.id.file_permissions).text = permissions.toString()
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Properties")
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun getFileType(fileName: String): String {
        val lastDot = fileName.lastIndexOf('.')
        if (lastDot > 0) {
            val extension = fileName.substring(lastDot + 1).lowercase()
            return when (extension) {
                "txt" -> "Text File"
                "pdf" -> "PDF Document"
                "doc", "docx" -> "Word Document"
                "jpg", "jpeg", "png", "gif" -> "Image"
                "mp3", "wav" -> "Audio"
                "mp4", "3gp" -> "Video"
                "xml" -> "XML File"
                "json" -> "JSON File"
                else -> "$extension File"
            }
        }
        return "File"
    }
    
    private fun formatSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }

    override fun onBackPressed() {
        // This method is now deprecated
        currentDirectory?.let { directory ->
            val parent = FileUtils.getParentDirectory(directory)
            if (parent != null) {
                loadDirectory(parent)
                return
            }
        }
        super.onBackPressed()
    }
    
    // Enum for sort order
    private enum class SortOrder {
        BY_NAME, BY_DATE, BY_SIZE, BY_TYPE
    }
}