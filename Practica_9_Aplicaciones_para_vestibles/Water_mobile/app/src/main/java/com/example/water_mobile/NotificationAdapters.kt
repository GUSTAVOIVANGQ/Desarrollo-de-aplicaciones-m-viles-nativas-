package com.example.water_mobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotificationFriendsAdapter(private val onFriendClick: (FriendData) -> Unit) : RecyclerView.Adapter<NotificationFriendsAdapter.FriendViewHolder>() {
    
    private var friends = listOf<FriendData>()
    
    fun updateFriends(newFriends: List<FriendData>) {
        friends = newFriends
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification_friend, parent, false)
        return FriendViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(friends[position])
    }
    
    override fun getItemCount() = friends.size
    
    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val friendName: TextView = itemView.findViewById(R.id.friendName)
        private val statusIcon: ImageView = itemView.findViewById(R.id.statusIcon)
        private val sendButton: TextView = itemView.findViewById(R.id.sendButton)
        
        fun bind(friend: FriendData) {
            friendName.text = friend.name
            
            if (friend.isOnline) {
                statusIcon.setImageResource(R.drawable.ic_online)
                statusIcon.setColorFilter(itemView.context.getColor(R.color.online_green))
            } else {
                statusIcon.setImageResource(R.drawable.ic_offline)
                statusIcon.setColorFilter(itemView.context.getColor(R.color.offline_gray))
            }
            
            sendButton.setOnClickListener {
                onFriendClick(friend)
            }
        }
    }
}

class NotificationGroupsAdapter(private val onGroupClick: (GroupData) -> Unit) : RecyclerView.Adapter<NotificationGroupsAdapter.GroupViewHolder>() {
    
    private var groups = listOf<GroupData>()
    
    fun updateGroups(newGroups: List<GroupData>) {
        groups = newGroups
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification_group, parent, false)
        return GroupViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(groups[position])
    }
    
    override fun getItemCount() = groups.size
    
    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val groupName: TextView = itemView.findViewById(R.id.groupName)
        private val memberCount: TextView = itemView.findViewById(R.id.memberCount)
        private val sendButton: TextView = itemView.findViewById(R.id.sendButton)
        
        fun bind(group: GroupData) {
            groupName.text = group.name
            memberCount.text = "${group.members.size} miembros"
            
            sendButton.setOnClickListener {
                onGroupClick(group)
            }
        }
    }
}
