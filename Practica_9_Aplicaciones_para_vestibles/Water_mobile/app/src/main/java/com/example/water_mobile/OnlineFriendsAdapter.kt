package com.example.water_mobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OnlineFriendsAdapter : RecyclerView.Adapter<OnlineFriendsAdapter.FriendViewHolder>() {
    
    private var friends = listOf<FriendData>()
    
    fun updateFriends(newFriends: List<FriendData>) {
        friends = newFriends
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_online_friend, parent, false)
        return FriendViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(friends[position])
    }
    
    override fun getItemCount() = friends.size
    
    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.friendName)
        private val statusIcon: ImageView = itemView.findViewById(R.id.statusIcon)
        private val statusText: TextView = itemView.findViewById(R.id.statusText)
        
        fun bind(friend: FriendData) {
            nameText.text = friend.name
            
            if (friend.isOnline) {
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
