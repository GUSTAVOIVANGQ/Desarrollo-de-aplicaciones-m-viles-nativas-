package com.example.water.presentation

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

object HydrationManager {
    private const val PREFS_NAME = "hydration_prefs"
    private const val KEY_DAILY_GOAL = "daily_goal"
    private const val KEY_REMINDER_INTERVAL = "reminder_interval"
    private const val KEY_TODAY_INTAKE = "today_intake"
    private const val KEY_LAST_DATE = "last_date"
    private const val DEFAULT_DAILY_GOAL = 2000 // 2 litros por defecto
    private const val DEFAULT_REMINDER_INTERVAL = 2 // cada 2 horas

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun getTodayString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    fun getDailyGoal(context: Context): Int {
        return getPrefs(context).getInt(KEY_DAILY_GOAL, DEFAULT_DAILY_GOAL)
    }

    fun setDailyGoal(context: Context, goal: Int): Int {
        getPrefs(context).edit().putInt(KEY_DAILY_GOAL, goal).apply()
        return goal
    }

    fun getReminderInterval(context: Context): Int {
        return getPrefs(context).getInt(KEY_REMINDER_INTERVAL, DEFAULT_REMINDER_INTERVAL)
    }

    fun setReminderInterval(context: Context, interval: Int): Int {
        getPrefs(context).edit().putInt(KEY_REMINDER_INTERVAL, interval).apply()
        return interval
    }

    fun getTodayIntake(context: Context): Int {
        val prefs = getPrefs(context)
        val today = getTodayString()
        val lastDate = prefs.getString(KEY_LAST_DATE, "")

        // Si es un nuevo día, resetear el contador
        if (today != lastDate) {
            prefs.edit()
                .putInt(KEY_TODAY_INTAKE, 0)
                .putString(KEY_LAST_DATE, today)
                .apply()
            return 0
        }

        return prefs.getInt(KEY_TODAY_INTAKE, 0)
    }

    fun addWater(context: Context, amount: Int): Int {
        val currentIntake = getTodayIntake(context)
        val newIntake = currentIntake + amount

        getPrefs(context).edit()
            .putInt(KEY_TODAY_INTAKE, newIntake)
            .putString(KEY_LAST_DATE, getTodayString())
            .apply()

        // Guardar en historial
        saveToHistory(context, getTodayString(), newIntake)

        return newIntake
    }

    private fun saveToHistory(context: Context, date: String, intake: Int) {
        val prefs = getPrefs(context)
        prefs.edit().putInt("history_$date", intake).apply()
    }

    fun getWeekData(context: Context): List<DayData> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("E", Locale.getDefault())
        val prefs = getPrefs(context)
        val dailyGoal = getDailyGoal(context)
        val weekData = mutableListOf<DayData>()

        // Obtener los últimos 7 días
        for (i in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_MONTH, -i)

            val dateString = dateFormat.format(calendar.time)
            val dayString = dayFormat.format(calendar.time)
            val intake = prefs.getInt("history_$dateString", 0)

            weekData.add(DayData(dayString, intake, dailyGoal))
        }

        return weekData
    }

    fun resetTodayIntake(context: Context) {
        getPrefs(context).edit()
            .putInt(KEY_TODAY_INTAKE, 0)
            .putString(KEY_LAST_DATE, getTodayString())
            .apply()
    }

    fun getIntakePercentage(context: Context): Float {
        val intake = getTodayIntake(context)
        val goal = getDailyGoal(context)
        return (intake.toFloat() / goal.toFloat()).coerceAtMost(1f)
    }

    fun isGoalReached(context: Context): Boolean {
        return getTodayIntake(context) >= getDailyGoal(context)
    }

    fun getRemainingWater(context: Context): Int {
        val remaining = getDailyGoal(context) - getTodayIntake(context)
        return if (remaining > 0) remaining else 0
    }
}