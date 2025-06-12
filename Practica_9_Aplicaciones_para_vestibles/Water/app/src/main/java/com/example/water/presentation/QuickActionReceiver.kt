package com.example.water.presentation

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.water.R

class QuickActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "ADD_WATER" -> {
                val amount = intent.getIntExtra("amount", 0)
                if (amount > 0) {
                    handleAddWater(context, amount)
                }
            }
        }
    }

    private fun handleAddWater(context: Context, amount: Int) {
        // Agregar agua al registro
        val newTotal = HydrationManager.addWater(context, amount)
        val dailyGoal = HydrationManager.getDailyGoal(context)
        val remaining = HydrationManager.getRemainingWater(context)

        // Mostrar toast de confirmación
        val message = if (remaining > 0) {
            "✅ +${amount}ml agregados. Total: ${newTotal}ml"
        } else {
            "🎉 ¡Meta alcanzada! Total: ${newTotal}ml"
        }

        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

        // Mostrar notificación de progreso actualizada
        showProgressNotification(context, newTotal, dailyGoal, remaining, amount)
    }

    private fun showProgressNotification(
        context: Context,
        currentIntake: Int,
        dailyGoal: Int,
        remaining: Int,
        lastAmount: Int
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val title = if (remaining > 0) {
            "💧 Progreso Actualizado"
        } else {
            "🎉 ¡Meta Alcanzada!"
        }

        val body = if (remaining > 0) {
            "Agregaste ${lastAmount}ml\nProgreso: ${currentIntake}ml / ${dailyGoal}ml\nFaltan: ${remaining}ml"
        } else {
            "¡Excelente! Has completado tu meta diaria\nTotal: ${currentIntake}ml / ${dailyGoal}ml"
        }
        val notification = NotificationCompat.Builder(context, "HYDRATION_CHANNEL")
            .setSmallIcon(com.example.water.R.drawable.ic_water_drop)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(9999, notification)
    }
}