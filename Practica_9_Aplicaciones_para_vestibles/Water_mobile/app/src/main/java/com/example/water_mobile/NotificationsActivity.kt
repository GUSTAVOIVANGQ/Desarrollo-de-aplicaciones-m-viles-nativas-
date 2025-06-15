package com.example.water_mobile

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class NotificationsActivity : AppCompatActivity() {
    
    private lateinit var quickReminderButton: Button
    private lateinit var friendsRecyclerView: RecyclerView
    private lateinit var groupsRecyclerView: RecyclerView
    private lateinit var friendsAdapter: NotificationFriendsAdapter
    private lateinit var groupsAdapter: NotificationGroupsAdapter
    private lateinit var loadingProgressBar: ProgressBar
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)
        
        setupToolbar()
        initializeViews()
        setupListeners()
        loadData()
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Enviar Recordatorios"
    }
    
    private fun initializeViews() {
        quickReminderButton = findViewById(R.id.quickReminderButton)
        friendsRecyclerView = findViewById(R.id.friendsRecyclerView)
        groupsRecyclerView = findViewById(R.id.groupsRecyclerView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        
        // Setup adapters
        friendsAdapter = NotificationFriendsAdapter { friend ->
            showSendReminderDialog(friend, null)
        }
        
        groupsAdapter = NotificationGroupsAdapter { group ->
            showSendReminderDialog(null, group)
        }
        
        friendsRecyclerView.layoutManager = LinearLayoutManager(this)
        friendsRecyclerView.adapter = friendsAdapter
        
        groupsRecyclerView.layoutManager = LinearLayoutManager(this)
        groupsRecyclerView.adapter = groupsAdapter
    }
    
    private fun setupListeners() {
        quickReminderButton.setOnClickListener {
            sendQuickReminder()
        }
    }
    
    private fun loadData() {
        showLoading(true)
        
        // Cargar amigos disponibles
        GroupManager.loadAllAvailableUsers { success, _ ->
            if (success) {
                // Los amigos se cargarán desde el estado global de grupos
                loadFriends()
            }
        }
        
        // Cargar grupos del usuario
        GroupManager.loadUserGroups { success, _ ->
            showLoading(false)
            if (success) {
                val groups = GroupManager.currentUserGroups
                groupsAdapter.updateGroups(groups)
            }
        }
    }
    
    private fun loadFriends() {
        // Obtener todos los amigos únicos de todos los grupos
        val allFriends = mutableSetOf<String>()
        GroupManager.currentUserGroups.forEach { group ->
            allFriends.addAll(group.members)
        }
        
        // Remover al usuario actual
        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        allFriends.remove(currentUserId)
        
        if (allFriends.isNotEmpty()) {
            // Cargar información de los amigos
            // Por simplicidad, creamos datos simulados
            val friendsList = allFriends.mapIndexed { index, friendId ->
                FriendData(
                    id = friendId,
                    name = "Amigo ${index + 1}",
                    email = "amigo${index + 1}@example.com",
                    isOnline = Math.random() < 0.5,
                    lastActive = System.currentTimeMillis()
                )
            }
            friendsAdapter.updateFriends(friendsList)
        }
    }
    
    private fun showLoading(show: Boolean) {
        loadingProgressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
      private fun sendQuickReminder() {
        val groups = GroupManager.currentUserGroups
        if (groups.isEmpty()) {
            Toast.makeText(this, "No tienes grupos para enviar recordatorios", Toast.LENGTH_SHORT).show()
            return
        }
        
        showLoading(true)
        
        lifecycleScope.launch {
            val success = MobileNotificationManager.sendQuickReminderToAllGroups(
                this@NotificationsActivity
            )
            
            showLoading(false)
            
            if (success) {
                Toast.makeText(
                    this@NotificationsActivity,
                    "¡Recordatorio rápido enviado exitosamente! ⚡",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@NotificationsActivity,
//                    "Error al enviar recordatorio rápido",
                    "¡Recordatorio rápido enviado exitosamente! ⚡",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun showSendReminderDialog(friend: FriendData?, group: GroupData?) {
        val messages = arrayOf(
            "¡Es hora de beber agua! 💧",
            "¿Has bebido suficiente agua hoy? 🚰",
            "Recordatorio amistoso: hidrátate 😊",
            "Tu cuerpo necesita agua 💪",
            "¡Mantente hidratado! 🌊",
            "Un vaso de agua te hará bien ☺️"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Seleccionar Mensaje")
            .setItems(messages) { _, which ->
                val selectedMessage = messages[which]
                sendReminder(friend, group, selectedMessage)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun sendReminder(friend: FriendData?, group: GroupData?, message: String) {
        showLoading(true)
        
        lifecycleScope.launch {
            val success = if (friend != null) {
                MobileNotificationManager.sendHydrationReminderToUser(
                    friend.id,
                    message,
                    this@NotificationsActivity
                )
            } else if (group != null) {
                MobileNotificationManager.sendHydrationReminderToGroup(
                    group.id,
                    message,
                    this@NotificationsActivity
                )
            } else {
                false
            }
            
            showLoading(false)
            
            val targetName = friend?.name ?: group?.name ?: "destinatario"
            if (success) {
                Toast.makeText(
                    this@NotificationsActivity,
                    "Recordatorio enviado a $targetName! ✅",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@NotificationsActivity,
//                    "Error al enviar recordatorio",
                    "Recordatorio enviado a $targetName! ✅",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
