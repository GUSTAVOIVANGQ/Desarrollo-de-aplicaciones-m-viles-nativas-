package com.example.filemanager

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.chrisbanes.photoview.PhotoView
import java.io.File

class ImageViewerFragment : Fragment() {
    
    private var filePath: String? = null
    private lateinit var photoView: PhotoView
    private var currentRotation = 0
    
    companion object {
        private const val ARG_FILE_PATH = "file_path"
        
        fun newInstance(filePath: String): ImageViewerFragment {
            val fragment = ImageViewerFragment()
            val args = Bundle()
            args.putString(ARG_FILE_PATH, filePath)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            filePath = it.getString(ARG_FILE_PATH)
        }
        setHasOptionsMenu(true)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_image_viewer, container, false)
        photoView = view.findViewById(R.id.photo_view)
        return view
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadImage()
    }
    
    private fun loadImage() {
        if (filePath.isNullOrEmpty()) return
        
        try {
            val file = File(filePath!!)
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            photoView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_image_viewer, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_rotate_left -> {
                rotateImage(-90)
                true
            }
            R.id.action_rotate_right -> {
                rotateImage(90)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun rotateImage(degrees: Int) {
        currentRotation = (currentRotation + degrees) % 360
        photoView.rotation = currentRotation.toFloat()
    }
}
