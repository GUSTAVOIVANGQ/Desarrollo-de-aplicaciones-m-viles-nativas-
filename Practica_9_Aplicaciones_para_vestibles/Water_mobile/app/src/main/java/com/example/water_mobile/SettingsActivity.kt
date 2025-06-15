package com.example.water_mobile

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var dailyGoalSeekBar: SeekBar
    private lateinit var dailyGoalText: TextView
    private lateinit var reminderIntervalSeekBar: SeekBar
    private lateinit var reminderIntervalText: TextView
    private lateinit var resetTodayButton: Button
    private lateinit var saveButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        setupToolbar()
        initializeViews()
        loadCurrentSettings()
        setupListeners()
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Configuración"
    }
    
    private fun initializeViews() {
        dailyGoalSeekBar = findViewById(R.id.dailyGoalSeekBar)
        dailyGoalText = findViewById(R.id.dailyGoalText)
        reminderIntervalSeekBar = findViewById(R.id.reminderIntervalSeekBar)
        reminderIntervalText = findViewById(R.id.reminderIntervalText)
        resetTodayButton = findViewById(R.id.resetTodayButton)
        saveButton = findViewById(R.id.saveButton)
    }
    
    private fun loadCurrentSettings() {
        val currentGoal = HydrationManager.getDailyGoal(this)
        val currentInterval = HydrationManager.getReminderInterval(this)
        
        // Configurar SeekBar para meta diaria (1000ml - 5000ml)
        dailyGoalSeekBar.max = 40 // (5000-1000)/100
        dailyGoalSeekBar.progress = (currentGoal - 1000) / 100
        dailyGoalText.text = "${currentGoal}ml"
        
        // Configurar SeekBar para intervalo de recordatorios (1-8 horas)
        reminderIntervalSeekBar.max = 7 // 8-1
        reminderIntervalSeekBar.progress = currentInterval - 1
        reminderIntervalText.text = "${currentInterval}h"
    }
    
    private fun setupListeners() {
        dailyGoalSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val goal = 1000 + (progress * 100)
                dailyGoalText.text = "${goal}ml"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        reminderIntervalSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val interval = progress + 1
                reminderIntervalText.text = "${interval}h"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        saveButton.setOnClickListener {
            saveSettings()
        }
        
        resetTodayButton.setOnClickListener {
            resetTodayIntake()
        }
    }
    
    private fun saveSettings() {
        val newGoal = 1000 + (dailyGoalSeekBar.progress * 100)
        val newInterval = reminderIntervalSeekBar.progress + 1
        
        HydrationManager.setDailyGoal(this, newGoal)
        HydrationManager.setReminderInterval(this, newInterval)
        
        Toast.makeText(this, "Configuración guardada", Toast.LENGTH_SHORT).show()
        finish()
    }
    
    private fun resetTodayIntake() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Reiniciar Consumo")
            .setMessage("¿Estás seguro de que quieres reiniciar el consumo de agua de hoy?")
            .setPositiveButton("Sí") { _, _ ->
                HydrationManager.resetTodayIntake(this)
                Toast.makeText(this, "Consumo de hoy reiniciado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
