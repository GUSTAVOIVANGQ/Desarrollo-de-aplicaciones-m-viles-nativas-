package com.example.pushnotificationapp

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object FCMHelper {
    private const val TAG = "FCMHelper"

    // Esto debería ser reemplazado por tu clave de servidor real de Firebase
    // IMPORTANTE: En una aplicación de producción, esta clave nunca debería estar en el cliente
    // Esto es solo para fines de demostración
    private const val SERVER_KEY = "AIzaSyBvI4LbiyBC8s6ReHlTFsLxmoOj33zw4oQ"

    private val fcmApi = FCMApiService.create()

    suspend fun sendNotificationToTokens(
        tokens: List<String>,
        title: String,
        message: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            if (tokens.isEmpty()) {
                Log.e(TAG, "Lista de tokens vacía")
                return@withContext false
            }

            val authHeader = "key=$SERVER_KEY"

            val notificationData = NotificationData(
                title = title,
                body = message
            )

            val data = mapOf(
                "click_action" to "FLUTTER_NOTIFICATION_CLICK",
                "title" to title,
                "message" to message
            )

            val request = if (tokens.size == 1) {
                FCMNotificationRequest(
                    to = tokens[0],
                    notification = notificationData,
                    data = data
                )
            } else {
                FCMNotificationRequest(
                    registration_ids = tokens,
                    notification = notificationData,
                    data = data
                )
            }

            val response = fcmApi.sendNotification(authHeader, request)

            if (response.isSuccessful) {
                val fcmResponse = response.body()
                Log.d(TAG, "FCM Response: ${fcmResponse?.success} success, ${fcmResponse?.failure} failure")
                return@withContext true
            } else {
                Log.e(TAG, "Error: ${response.code()} - ${response.message()}")
                return@withContext false
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar notificación", e)
            return@withContext false
        }
    }
}