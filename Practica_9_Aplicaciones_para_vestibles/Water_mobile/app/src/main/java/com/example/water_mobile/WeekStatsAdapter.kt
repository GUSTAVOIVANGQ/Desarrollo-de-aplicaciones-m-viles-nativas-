package com.example.water_mobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WeekStatsAdapter : RecyclerView.Adapter<WeekStatsAdapter.StatsViewHolder>() {
    
    private var weekData = listOf<DayData>()
    
    fun updateData(newData: List<DayData>) {
        weekData = newData
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day_stats, parent, false)
        return StatsViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: StatsViewHolder, position: Int) {
        holder.bind(weekData[position])
    }
    
    override fun getItemCount() = weekData.size
    
    class StatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayText: TextView = itemView.findViewById(R.id.dayText)
        private val intakeText: TextView = itemView.findViewById(R.id.intakeText)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        private val percentageText: TextView = itemView.findViewById(R.id.percentageText)
        
        fun bind(dayData: DayData) {
            dayText.text = dayData.day
            intakeText.text = "${dayData.intake}ml / ${dayData.goal}ml"
            
            val percentage = (dayData.intake.toFloat() / dayData.goal.toFloat()).coerceAtMost(1f)
            progressBar.progress = (percentage * 100).toInt()
            percentageText.text = "${(percentage * 100).toInt()}%"
            
            // Cambiar color según si se alcanzó la meta
            val context = itemView.context
            if (dayData.intake >= dayData.goal) {
                progressBar.progressTintList = context.getColorStateList(R.color.online_green)
                percentageText.setTextColor(context.getColor(R.color.online_green))
            } else {
                progressBar.progressTintList = context.getColorStateList(R.color.water_blue)
                percentageText.setTextColor(context.getColor(R.color.water_blue))
            }
        }
    }
}
