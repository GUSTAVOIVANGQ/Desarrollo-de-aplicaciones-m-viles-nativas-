package com.example.water_mobile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class GroupsActivity : AppCompatActivity() {
    
    private lateinit var groupsRecyclerView: RecyclerView
    private lateinit var groupsAdapter: GroupsAdapter
    private lateinit var noGroupsText: TextView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var fab: FloatingActionButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)
        
        setupToolbar()
        initializeViews()
        setupListeners()
        loadGroups()
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Mis Grupos"
    }
    
    private fun initializeViews() {
        groupsRecyclerView = findViewById(R.id.groupsRecyclerView)
        noGroupsText = findViewById(R.id.noGroupsText)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        fab = findViewById(R.id.fab)
        
        groupsAdapter = GroupsAdapter { group ->
            // Abrir detalle del grupo
            val intent = Intent(this, GroupDetailActivity::class.java)
            intent.putExtra("groupId", group.id)
            intent.putExtra("groupName", group.name)
            startActivity(intent)
        }
        
        groupsRecyclerView.layoutManager = LinearLayoutManager(this)
        groupsRecyclerView.adapter = groupsAdapter
    }
    
    private fun setupListeners() {
        fab.setOnClickListener {
            showCreateGroupDialog()
        }
    }
    
    private fun loadGroups() {
        showLoading(true)
        
        GroupManager.loadUserGroups { success, message ->
            showLoading(false)
            
            if (success) {
                val groups = GroupManager.currentUserGroups
                updateGroupsUI(groups)
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                updateGroupsUI(emptyList())
            }
        }
    }
    
    private fun updateGroupsUI(groups: List<GroupData>) {
        if (groups.isEmpty()) {
            groupsRecyclerView.visibility = View.GONE
            noGroupsText.visibility = View.VISIBLE
        } else {
            groupsRecyclerView.visibility = View.VISIBLE
            noGroupsText.visibility = View.GONE
            groupsAdapter.updateGroups(groups)
        }
    }
    
    private fun showLoading(show: Boolean) {
        if (show) {
            loadingProgressBar.visibility = View.VISIBLE
            groupsRecyclerView.visibility = View.GONE
            noGroupsText.visibility = View.GONE
        } else {
            loadingProgressBar.visibility = View.GONE
        }
    }
    
    private fun showCreateGroupDialog() {
        val editText = EditText(this)
        editText.hint = "Nombre del grupo"
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Crear Nuevo Grupo")
            .setView(editText)
            .setPositiveButton("Crear") { _, _ ->
                val groupName = editText.text.toString().trim()
                if (groupName.isNotEmpty()) {
                    createGroup(groupName)
                } else {
                    Toast.makeText(this, "Por favor ingresa un nombre", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun createGroup(groupName: String) {
        showLoading(true)
        
        GroupManager.createGroup(groupName) { success, message ->
            showLoading(false)
            
            if (success) {
                Toast.makeText(this, "Grupo creado exitosamente", Toast.LENGTH_SHORT).show()
                loadGroups()
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        loadGroups()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
