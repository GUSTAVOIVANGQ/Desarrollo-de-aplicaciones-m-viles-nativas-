package com.example.water_mobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GroupMembersAdapter : RecyclerView.Adapter<GroupMembersAdapter.MemberViewHolder>() {
    
    private var members = listOf<FriendData>()
    
    fun updateMembers(newMembers: List<FriendData>) {
        members = newMembers
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group_member, parent, false)
        return MemberViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(members[position])
    }
    
    override fun getItemCount() = members.size
    
    class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val memberName: TextView = itemView.findViewById(R.id.memberName)
        private val memberEmail: TextView = itemView.findViewById(R.id.memberEmail)
        private val statusIcon: ImageView = itemView.findViewById(R.id.statusIcon)
        private val statusText: TextView = itemView.findViewById(R.id.statusText)
        
        fun bind(member: FriendData) {
            memberName.text = member.name
            memberEmail.text = member.email
            
            if (member.isOnline) {
                statusIcon.setImageResource(R.drawable.ic_online)
                statusIcon.setColorFilter(itemView.context.getColor(R.color.online_green))
                statusText.text = "En línea"
                statusText.setTextColor(itemView.context.getColor(R.color.online_green))
            } else {
                statusIcon.setImageResource(R.drawable.ic_offline)
                statusIcon.setColorFilter(itemView.context.getColor(R.color.offline_gray))
                statusText.text = "Desconectado"
                statusText.setTextColor(itemView.context.getColor(R.color.offline_gray))
            }
        }
    }
}
