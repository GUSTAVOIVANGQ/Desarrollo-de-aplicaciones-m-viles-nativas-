package com.example.map.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.map.R
import com.example.map.data.CustomPoi
import java.text.SimpleDateFormat
import java.util.*

class PoiAdapter(private val listener: OnPoiClickListener) :
    ListAdapter<CustomPoi, PoiAdapter.PoiViewHolder>(PoiDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_poi, parent, false)
        return PoiViewHolder(view)
    }

    override fun onBindViewHolder(holder: PoiViewHolder, position: Int) {
        holder.bind(getItem(position), listener)
    }

    class PoiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.poiNameTextView)
        private val categoryTextView: TextView = itemView.findViewById(R.id.poiCategoryTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.poiDescriptionTextView)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.poiRatingBar)
        private val imageView: ImageView = itemView.findViewById(R.id.poiThumbnail)
        private val dateTextView: TextView = itemView.findViewById(R.id.poiDateTextView)
        private val editButton: ImageButton = itemView.findViewById(R.id.editPoiButton)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deletePoiButton)
        
        fun bind(poi: CustomPoi, listener: OnPoiClickListener) {
            nameTextView.text = poi.name
            categoryTextView.text = poi.category.displayName
            descriptionTextView.text = poi.description
            ratingBar.rating = poi.rating
            
            // Format date
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dateTextView.text = dateFormat.format(poi.createdAt)
            
            // Load image if available
            poi.imageUri?.let {
                try {
                    imageView.setImageURI(Uri.parse(it))
                    imageView.visibility = View.VISIBLE
                } catch (e: Exception) {
                    imageView.visibility = View.GONE
                }
            } ?: run {
                imageView.visibility = View.GONE
            }
            
            // Set click listeners
            itemView.setOnClickListener { listener.onPoiClick(poi) }
            editButton.setOnClickListener { listener.onPoiEditClick(poi) }
            deleteButton.setOnClickListener { listener.onPoiDeleteClick(poi) }
        }
    }

    class PoiDiffCallback : DiffUtil.ItemCallback<CustomPoi>() {
        override fun areItemsTheSame(oldItem: CustomPoi, newItem: CustomPoi): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CustomPoi, newItem: CustomPoi): Boolean {
            return oldItem == newItem
        }
    }

    interface OnPoiClickListener {
        fun onPoiClick(poi: CustomPoi)
        fun onPoiEditClick(poi: CustomPoi)
        fun onPoiDeleteClick(poi: CustomPoi)
    }
}
