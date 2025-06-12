package com.example.water.presentation

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.water.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class HydrationFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "HydrationFCM"
        private const val CHANNEL_ID = "HYDRATION_CHANNEL"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "Mensaje recibido de: ${remoteMessage.from}")

        // Si el mensaje contiene datos
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Datos del mensaje: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        // Si el mensaje contiene una notificación
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Título: ${notification.title}")
            Log.d(TAG, "Cuerpo: ${notification.body}")

            showNotification(
                title = notification.title ?: "💧 Recordatorio de Hidratación",
                body = notification.body ?: "¡Es hora de beber agua!",
                data = remoteMessage.data
            )
        }

        // Si no hay notificación pero sí datos, crear una notificación personalizada
        if (remoteMessage.notification == null && remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "💧 Recordatorio de Hidratación"
            val body = remoteMessage.data["body"] ?: "¡Es hora de beber agua!"
            showNotification(title, body, remoteMessage.data)
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"]

        when (type) {
            "hydration_reminder" -> {
                // Recordatorio normal de hidratación
                val customMessage = data["message"] ?: getRandomHydrationMessage()
                showHydrationNotification(customMessage)
            }
            "goal_update" -> {
                // Actualizar meta diaria desde el administrador
                val newGoal = data["new_goal"]?.toIntOrNull()
                if (newGoal != null) {
                    HydrationManager.setDailyGoal(this, newGoal)
                    showNotification(
                        "Meta Actualizada",
                        "Tu nueva meta diaria es ${newGoal}ml",
                        data
                    )
                }
            }
            "motivational" -> {
                // Mensaje motivacional
                val message = data["message"] ?: "¡Sigue así! Tu salud es importante."
                showNotification("💪 Motivación", message, data)
            }
            "admin_broadcast" -> {
                // Mensaje del administrador
                val adminMessage = data["message"] ?: "Mensaje del administrador"
                showNotification("📢 Mensaje Importante", adminMessage, data)
            }
        }
    }

    private fun showHydrationNotification(message: String) {
        val currentIntake = HydrationManager.getTodayIntake(this)
        val dailyGoal = HydrationManager.getDailyGoal(this)
        val remaining = HydrationManager.getRemainingWater(this)

        val detailedMessage = if (remaining > 0) {
            "$message\n\nProgreso: ${currentIntake}ml / ${dailyGoal}ml\nFaltan: ${remaining}ml"
        } else {
            "$message\n\n¡Meta alcanzada! ${currentIntake}ml / ${dailyGoal}ml"
        }

        showNotification(
            title = "💧 Hora de Hidratarse",
            body = detailedMessage,
            data = mapOf("type" to "hydration_reminder")
        )
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Pasar datos extra si es necesario
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Crear acciones rápidas para la notificación
        val quickAction250 = createQuickAction("Bebí 250ml", 250)
        val quickAction500 = createQuickAction("Bebí 500ml", 500)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_drop) // Necesitarás agregar este ícono
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .addAction(quickAction250)
            .addAction(quickAction500)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = Random.nextInt(1000, 9999)
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun createQuickAction(title: String, amount: Int): NotificationCompat.Action {
        val intent = Intent(this, QuickActionReceiver::class.java).apply {
            action = "ADD_WATER"
            putExtra("amount", amount)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this, amount, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Action.Builder(
            R.drawable.ic_water_drop,
            title,
            pendingIntent
        ).build()
    }

    private fun getRandomHydrationMessage(): String {
        val messages = arrayOf(
            "¡Es hora de beber agua! 💧",
            "Tu cuerpo necesita hidratación 🌊",
            "Recuerda mantenerte hidratado 💙",
            "Un vaso de agua te hará sentir mejor ✨",
            "¡Dale a tu cuerpo el agua que necesita! 🏃‍♂️",
            "Hidratarse es cuidarse 💚",
            "¡Tu salud es importante, bebe agua! 🌟",
            "El agua es vida, ¡no la olvides! 🌱"
        )
        return messages.random()
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Token FCM actualizado: $token")

        // Aquí puedes enviar el token a tu servidor si es necesario
        sendTokenToServer(token)

        // Suscribirse al tópico de recordatorios de hidratación
        com.google.firebase.messaging.FirebaseMessaging.getInstance()
            .subscribeToTopic("hydration_reminders")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Suscrito al tópico hydration_reminders")
                } else {
                    Log.e(TAG, "Error al suscribirse al tópico", task.exception)
                }
            }
    }    private fun sendTokenToServer(token: String) {
        // Aquí puedes implementar el envío del token a tu servidor
        // Por ejemplo, usando Firebase Firestore o tu API REST
        Log.d(TAG, "Enviando token al servidor: $token")

        // Implementación con Firestore:
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        
        val userId = auth.currentUser?.uid ?: "anonymous_user"
        
        val tokenData = hashMapOf(
            "token" to token,
            "timestamp" to com.google.firebase.Timestamp.now(),
            "deviceType" to "wearable",
            "userId" to userId
        )

        firestore.collection("device_tokens")
            .document(token)
            .set(tokenData)
            .addOnSuccessListener {
                Log.d(TAG, "Token guardado en Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al guardar token", e)
            }
    }
}