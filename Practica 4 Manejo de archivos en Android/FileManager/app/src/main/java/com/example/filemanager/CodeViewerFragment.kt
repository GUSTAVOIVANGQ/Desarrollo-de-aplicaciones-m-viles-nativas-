package com.example.filemanager

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.regex.Pattern

class CodeViewerFragment : Fragment() {
    
    private var filePath: String? = null
    private lateinit var codeView: TextView
    private lateinit var scrollView: ScrollView
    
    companion object {
        private const val ARG_FILE_PATH = "file_path"
        
        fun newInstance(filePath: String): CodeViewerFragment {
            val fragment = CodeViewerFragment()
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
        val view = inflater.inflate(R.layout.fragment_code_viewer, container, false)
        codeView = view.findViewById(R.id.code_content)
        scrollView = view.findViewById(R.id.scroll_view)
        return view
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadCodeFile()
    }
    
    private fun loadCodeFile() {
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
            
            val rawContent = content.toString()
            
            // Apply syntax highlighting based on file type
            when {
                filePath!!.endsWith(".json") -> {
                    highlightJson(rawContent)
                }
                filePath!!.endsWith(".xml") -> {
                    highlightXml(rawContent)
                }
                else -> {
                    codeView.text = rawContent
                }
            }
        } catch (e: Exception) {
            codeView.text = "Error loading file: ${e.message}"
        }
    }
    
    private fun highlightJson(json: String) {
        try {
            val formatted = formatJson(json)
            val spannableBuilder = SpannableStringBuilder(formatted)
            
            // Highlight keys
            val keyPattern = Pattern.compile("\"(.*?)\"\\s*:")
            val keyMatcher = keyPattern.matcher(formatted)
            while (keyMatcher.find()) {
                val keyColor = ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark)
                spannableBuilder.setSpan(
                    ForegroundColorSpan(keyColor),
                    keyMatcher.start(), keyMatcher.end() - 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            
            // Highlight string values
            val valuePattern = Pattern.compile(":[ \t]*\"(.*?)\"")
            val valueMatcher = valuePattern.matcher(formatted)
            while (valueMatcher.find()) {
                val valueColor = ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
                val start = valueMatcher.start() + valueMatcher.group(0)!!.indexOf('"')
                val end = valueMatcher.end()
                spannableBuilder.setSpan(
                    ForegroundColorSpan(valueColor),
                    start, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            
            // Highlight numbers
            val numberPattern = Pattern.compile(":[ \t]*(-?\\d+(\\.\\d+)?)")
            val numberMatcher = numberPattern.matcher(formatted)
            while (numberMatcher.find()) {
                val numColor = ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark)
                val start = valueMatcher.start() + valueMatcher.group(0)!!.indexOf(numberMatcher.group(1)!!)
                val end = start + numberMatcher.group(1)!!.length
                if (start < end && end <= spannableBuilder.length) {
                    spannableBuilder.setSpan(
                        ForegroundColorSpan(numColor),
                        start, end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
            
            codeView.text = spannableBuilder
        } catch (e: Exception) {
            codeView.text = json // Fallback to unformatted JSON
        }
    }
    
    private fun formatJson(json: String): String {
        try {
            val trimmed = json.trim()
            return when {
                trimmed.startsWith("{") -> {
                    val jsonObject = JSONObject(trimmed)
                    jsonObject.toString(4)
                }
                trimmed.startsWith("[") -> {
                    val jsonArray = JSONArray(trimmed)
                    jsonArray.toString(4)
                }
                else -> {
                    val obj = JSONTokener(trimmed).nextValue()
                    when (obj) {
                        is JSONObject -> obj.toString(4)
                        is JSONArray -> obj.toString(4)
                        else -> json
                    }
                }
            }
        } catch (e: Exception) {
            return json
        }
    }
    
    private fun highlightXml(xml: String) {
        val spannableBuilder = SpannableStringBuilder(xml)
        
        // Highlight tags
        val tagPattern = Pattern.compile("</?[a-zA-Z][a-zA-Z0-9:_.-]*[^>]*>")
        val tagMatcher = tagPattern.matcher(xml)
        while (tagMatcher.find()) {
            val tagColor = ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark)
            spannableBuilder.setSpan(
                ForegroundColorSpan(tagColor),
                tagMatcher.start(), tagMatcher.end(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        
        // Highlight attributes
        val attrPattern = Pattern.compile("\\s+([a-zA-Z][a-zA-Z0-9:_.-]*)=")
        val attrMatcher = attrPattern.matcher(xml)
        while (attrMatcher.find()) {
            val attrColor = ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
            spannableBuilder.setSpan(
                ForegroundColorSpan(attrColor),
                attrMatcher.start(1), attrMatcher.end(1),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        
        // Highlight attribute values
        val valuePattern = Pattern.compile("=\\s*\"([^\"]*)\"")
        val valueMatcher = valuePattern.matcher(xml)
        while (valueMatcher.find()) {
            val valueColor = ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark)
            spannableBuilder.setSpan(
                ForegroundColorSpan(valueColor),
                valueMatcher.start(1), valueMatcher.end(1),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        
        codeView.text = spannableBuilder
    }
}
