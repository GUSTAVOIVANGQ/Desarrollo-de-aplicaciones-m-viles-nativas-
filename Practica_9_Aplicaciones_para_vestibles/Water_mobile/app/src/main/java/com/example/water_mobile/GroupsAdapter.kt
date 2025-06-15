package com.example.water_mobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GroupsAdapter(private val onGroupClick: (GroupData) -> Unit) : RecyclerView.Adapter<GroupsAdapter.GroupViewHolder>() {
    
    private var groups = listOf<GroupData>()
    
    fun updateGroups(newGroups: List<GroupData>) {
        groups = newGroups
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(groups[position])
    }
    
    override fun getItemCount() = groups.size
    
    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val groupName: TextView = itemView.findViewById(R.id.groupName)
        private val memberCount: TextView = itemView.findViewById(R.id.memberCount)
        
        fun bind(group: GroupData) {
            groupName.text = group.name
            memberCount.text = "${group.members.size} miembros"
            
            itemView.setOnClickListener {
                onGroupClick(group)
            }
        }
    }
}
