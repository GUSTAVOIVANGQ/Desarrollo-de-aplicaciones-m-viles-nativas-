package com.example.water_mobile

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MobileFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MobileFCM"
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
                notification.title ?: "Recordatorio de Hidratación",
                notification.body ?: "¡Es hora de beber agua!",
                remoteMessage.data
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
                val senderName = data["senderName"] ?: "Un amigo"
                val reminderMessage = data["reminderMessage"] ?: "¡Es hora de hidratarte!"
                val message = "$senderName te recuerda: $reminderMessage"
                showHydrationNotification(message, data)
            }
            "group_hydration_reminder" -> {
                val senderName = data["senderName"] ?: "Un amigo"
                val groupName = data["groupName"] ?: "el grupo"
                val reminderMessage = data["reminderMessage"] ?: "¡Es hora de hidratarse!"
                val message = "$senderName en $groupName: $reminderMessage"
                showHydrationNotification(message, data)
            }
            "goal_achieved" -> {
                val senderName = data["senderName"] ?: "Un amigo"
                val intake = data["intake"] ?: "0"
                val goal = data["goal"] ?: "2000"
                val message = "🎉 $senderName ha completado su meta diaria! (${intake}ml / ${goal}ml)"
                showGoalAchievedNotification(message, data)
            }
            "scheduled_reminder" -> {
                showScheduledNotification(data)
            }
        }
    }

    private fun showHydrationNotification(message: String, data: Map<String, String>) {
        showNotification(
            title = "💧 Recordatorio de Hidratación",
            body = message,
            data = data,
            isHighPriority = true
        )
    }

    private fun showGoalAchievedNotification(message: String, data: Map<String, String>) {
        showNotification(
            title = "🎉 ¡Meta Alcanzada!",
            body = message,
            data = data,
            isHighPriority = true
        )
    }

    private fun showScheduledNotification(data: Map<String, String>) {
        val messages = arrayOf(
            "💧 ¡Es hora de beber agua!",
            "🚰 No olvides hidratarte",
            "💪 Tu cuerpo necesita agua",
            "⏰ Recordatorio de hidratación",
            "🌊 Mantente hidratado"
        )
        
        val randomMessage = messages.random()
        
        showNotification(
            title = "💧 Recordatorio Automático",
            body = randomMessage,
            data = data,
            isHighPriority = false
        )
    }

    private fun showNotification(
        title: String, 
        body: String, 
        data: Map<String, String>,
        isHighPriority: Boolean = true
    ) {
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

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Usa el ícono por defecto
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(if (isHighPriority) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 250, 500))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Token FCM actualizado: $token")

        // Enviar el token al servidor
        sendTokenToServer(token)

        // Suscribirse a tópicos
        com.google.firebase.messaging.FirebaseMessaging.getInstance()
            .subscribeToTopic("hydration_reminders")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Suscrito a hydration_reminders")
                } else {
                    Log.w(TAG, "Error al suscribirse a hydration_reminders", task.exception)
                }
            }
    }

    private fun sendTokenToServer(token: String) {
        Log.d(TAG, "Enviando token al servidor: $token")

        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        
        val userId = auth.currentUser?.uid ?: "anonymous_user_mobile"
        
        val tokenData = hashMapOf(
            "fcmToken" to token,
            "userId" to userId,
            "deviceType" to "mobile",
            "lastUpdated" to System.currentTimeMillis(),
            "isActive" to true
        )

        firestore.collection("device_tokens")
            .document("${userId}_mobile")
            .set(tokenData)
            .addOnSuccessListener {
                Log.d(TAG, "Token guardado exitosamente en Firestore")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error al guardar token en Firestore", e)
            }
    }
}
