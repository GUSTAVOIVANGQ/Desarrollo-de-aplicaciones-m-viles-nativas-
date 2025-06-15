package com.example.water_mobile

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GroupDetailActivity : AppCompatActivity() {
    
    private lateinit var groupMembersRecyclerView: RecyclerView
    private lateinit var membersAdapter: GroupMembersAdapter
    private lateinit var addMemberButton: Button
    private lateinit var noMembersText: TextView
    private lateinit var loadingProgressBar: ProgressBar
    
    private var groupId: String = ""
    private var groupName: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_detail)
        
        // Obtener datos del intent
        groupId = intent.getStringExtra("groupId") ?: ""
        groupName = intent.getStringExtra("groupName") ?: "Grupo"
        
        setupToolbar()
        initializeViews()
        setupListeners()
        loadGroupMembers()
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = groupName
    }
    
    private fun initializeViews() {
        groupMembersRecyclerView = findViewById(R.id.groupMembersRecyclerView)
        addMemberButton = findViewById(R.id.addMemberButton)
        noMembersText = findViewById(R.id.noMembersText)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        
        membersAdapter = GroupMembersAdapter()
        groupMembersRecyclerView.layoutManager = LinearLayoutManager(this)
        groupMembersRecyclerView.adapter = membersAdapter
    }
    
    private fun setupListeners() {
        addMemberButton.setOnClickListener {
            showAddMemberDialog()
        }
    }
    
    private fun loadGroupMembers() {
        showLoading(true)
        
        GroupManager.loadGroupMembers(groupId) { success, message ->
            showLoading(false)
            
            if (success) {
                // Para simplificar, simulamos algunos miembros
                val members = listOf(
                    FriendData("1", "Usuario 1", "usuario1@example.com", true, System.currentTimeMillis()),
                    FriendData("2", "Usuario 2", "usuario2@example.com", false, System.currentTimeMillis() - 300000),
                    FriendData("3", "Usuario 3", "usuario3@example.com", true, System.currentTimeMillis())
                )
                updateMembersUI(members)
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                updateMembersUI(emptyList())
            }
        }
    }
    
    private fun updateMembersUI(members: List<FriendData>) {
        if (members.isEmpty()) {
            groupMembersRecyclerView.visibility = View.GONE
            noMembersText.visibility = View.VISIBLE
        } else {
            groupMembersRecyclerView.visibility = View.VISIBLE
            noMembersText.visibility = View.GONE
            membersAdapter.updateMembers(members)
        }
    }
    
    private fun showLoading(show: Boolean) {
        if (show) {
            loadingProgressBar.visibility = View.VISIBLE
            groupMembersRecyclerView.visibility = View.GONE
            noMembersText.visibility = View.GONE
        } else {
            loadingProgressBar.visibility = View.GONE
        }
    }
    
    private fun showAddMemberDialog() {
        val editText = EditText(this)
        editText.hint = "Email del usuario"
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Añadir Miembro")
            .setMessage("Ingresa el email del usuario que quieres añadir al grupo")
            .setView(editText)
            .setPositiveButton("Añadir") { _, _ ->
                val email = editText.text.toString().trim()
                if (email.isNotEmpty()) {
                    addMember(email)
                } else {
                    Toast.makeText(this, "Por favor ingresa un email", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun addMember(email: String) {
        showLoading(true)
        
        GroupManager.addMemberToGroup(groupId, email) { success, message ->
            showLoading(false)
            
            if (success) {
                Toast.makeText(this, "Miembro añadido exitosamente", Toast.LENGTH_SHORT).show()
                loadGroupMembers()
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
