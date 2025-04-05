package com.example.filemanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.lang.Exception

class TextViewerFragment : Fragment() {
    
    private var filePath: String? = null
    private lateinit var textView: TextView
    private lateinit var scrollView: ScrollView
    
    companion object {
        private const val ARG_FILE_PATH = "file_path"
        
        fun newInstance(filePath: String): TextViewerFragment {
            val fragment = TextViewerFragment()
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
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_text_viewer, container, false)
        textView = view.findViewById(R.id.text_content)
        scrollView = view.findViewById(R.id.scroll_view)
        return view
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadTextFile()
    }
    
    private fun loadTextFile() {
        if (filePath.isNullOrEmpty()) return
        
        try {
            val file = File(filePath!!)
            val content = StringBuilder()
            
            BufferedReader(FileReader(file)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    content.append(line)
                    content.append('\n')
                }
            }
            
            textView.text = content.toString()
        } catch (e: Exception) {
            textView.text = "Error loading file: ${e.message}"
        }
    }
}
