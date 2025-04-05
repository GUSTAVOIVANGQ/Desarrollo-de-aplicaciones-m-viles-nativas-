package com.example.filemanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FileAdapter(
    private var fileItems: List<FileItem>,
    private val onItemClick: (FileItem) -> Unit,
    private val onItemLongClick: (FileItem, View) -> Boolean
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileIcon: ImageView = itemView.findViewById(R.id.file_icon)
        val fileName: TextView = itemView.findViewById(R.id.file_name)
        val fileDetails: TextView = itemView.findViewById(R.id.file_details)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.file_item, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val fileItem = fileItems[position]
        
        // Set file name
        holder.fileName.text = fileItem.name
        
        // Set file details (size and last modified)
        holder.fileDetails.text = if (fileItem.isDirectory) {
            "Directory | ${fileItem.lastModified}"
        } else {
            "${fileItem.size} | ${fileItem.lastModified}"
        }
        
        // Set icon based on file type
        holder.fileIcon.setImageResource(
            if (fileItem.isDirectory) R.drawable.ic_folder
            else when {
                fileItem.name.endsWith(".txt") || fileItem.name.endsWith(".md") -> R.drawable.ic_text
                fileItem.name.endsWith(".jpg") || fileItem.name.endsWith(".png") || 
                fileItem.name.endsWith(".jpeg") || fileItem.name.endsWith(".gif") -> R.drawable.ic_image
                fileItem.name.endsWith(".mp3") || fileItem.name.endsWith(".wav") -> R.drawable.ic_audio
                fileItem.name.endsWith(".mp4") || fileItem.name.endsWith(".3gp") -> R.drawable.ic_video
                fileItem.name.endsWith(".pdf") -> R.drawable.ic_pdf
                fileItem.name.endsWith(".doc") || fileItem.name.endsWith(".docx") -> R.drawable.ic_doc
                fileItem.name.endsWith(".xml") || fileItem.name.endsWith(".json") -> R.drawable.ic_code
                else -> R.drawable.ic_file
            }
        )
        
        // Set click listener
        holder.itemView.setOnClickListener {
            onItemClick(fileItem)
        }
        
        // Set long click listener
        holder.itemView.setOnLongClickListener { view ->
            onItemLongClick(fileItem, view)
        }
    }

    override fun getItemCount() = fileItems.size

    fun updateFiles(newFiles: List<FileItem>) {
        fileItems = newFiles
        notifyDataSetChanged()
    }
}
