package com.example.water_mobile

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class StatsActivity : AppCompatActivity() {
    
    private lateinit var todayStatsText: TextView
    private lateinit var weekStatsRecyclerView: RecyclerView
    private lateinit var weekStatsAdapter: WeekStatsAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
        
        setupToolbar()
        initializeViews()
        loadStats()
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Estadísticas"
    }
    
    private fun initializeViews() {
        todayStatsText = findViewById(R.id.todayStatsText)
        weekStatsRecyclerView = findViewById(R.id.weekStatsRecyclerView)
        
        weekStatsAdapter = WeekStatsAdapter()
        weekStatsRecyclerView.layoutManager = LinearLayoutManager(this)
        weekStatsRecyclerView.adapter = weekStatsAdapter
    }
    
    private fun loadStats() {
        // Estadísticas de hoy
        val todayIntake = HydrationManager.getTodayIntake(this)
        val dailyGoal = HydrationManager.getDailyGoal(this)
        val percentage = HydrationManager.getIntakePercentage(this)
        val remaining = HydrationManager.getRemainingWater(this)
        
        val todayStats = buildString {
            appendLine("Consumo actual: ${todayIntake}ml")
            appendLine("Meta diaria: ${dailyGoal}ml")
            appendLine("Progreso: ${(percentage * 100).toInt()}%")
            if (remaining > 0) {
                appendLine("Faltan: ${remaining}ml")
            } else {
                appendLine("¡Meta alcanzada! 🎉")
            }
        }
        
        todayStatsText.text = todayStats
        
        // Estadísticas de la semana
        val weekData = HydrationManager.getWeekData(this)
        weekStatsAdapter.updateData(weekData)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
