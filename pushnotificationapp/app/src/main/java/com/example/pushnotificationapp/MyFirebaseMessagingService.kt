package com.example.pushnotificationapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "FCM_Service"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo token FCM: $token")
        // Guardar en Firestore
        saveTokenToFirestore(token)
    }

    private fun saveTokenToFirestore(token: String) {
        val db = FirebaseFirestore.getInstance()
        val tokenData = hashMapOf(
            "token" to token,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("tokens")
            .document(token)
            .set(tokenData)
            .addOnSuccessListener {
                Log.d(TAG, "Token guardado exitosamente en Firestore")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error al guardar token", e)
            }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "From: ${remoteMessage.from}")

        // Verificar si el mensaje contiene datos
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            // Podemos obtener título y mensaje desde los datos
            val title = remoteMessage.data["title"] ?: "Nueva notificación"
            val message = remoteMessage.data["message"] ?: "Tienes una nueva notificación"

            NotificationHelper.showNotification(
                this,
                title,
                message
            )
        }

        // Verificar si el mensaje contiene una notificación
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            it.body?.let { body ->
                NotificationHelper.showNotification(
                    this,
                    it.title ?: "Nueva notificación",
                    body
                )
            }
        }
    }
}