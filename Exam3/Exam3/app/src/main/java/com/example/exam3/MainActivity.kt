package com.example.exam3

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    
    private lateinit var btnServer: MaterialButton
    private lateinit var btnClient: MaterialButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        // Configurar window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        initializeViews()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        btnServer = findViewById(R.id.btn_server)
        btnClient = findViewById(R.id.btn_client)
    }
    
    private fun setupClickListeners() {
        btnServer.setOnClickListener {
            // Navegar a la actividad del servidor
            val intent = Intent(this, ServerActivity::class.java)
            startActivity(intent)
        }
        
        btnClient.setOnClickListener {
            // Navegar a la actividad del cliente
            val intent = Intent(this, ClientActivity::class.java)
            startActivity(intent)
        }
    }
}