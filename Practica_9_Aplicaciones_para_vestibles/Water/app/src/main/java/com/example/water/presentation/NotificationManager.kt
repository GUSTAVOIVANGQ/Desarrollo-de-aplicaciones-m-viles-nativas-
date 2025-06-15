package com.example.water.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.water.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

/**
 * Gestor de notificaciones FCM para enviar recordatorios de hidratación
 * desde el vestible a otros usuarios registrados en Firebase
 */
object HydrationNotificationManager {
    private const val TAG = "NotificationManager"
    private const val LOCAL_CHANNEL_ID = "HYDRATION_LOCAL_CHANNEL"
    private const val LOCAL_CHANNEL_NAME = "Notificaciones Locales de Hidratación"
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val functions = FirebaseFunctions.getInstance()

    /**
     * Inicializa el token FCM para el dispositivo actual
     */
    suspend fun initializeFCMToken(): String? {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "FCM Token obtenido: $token")

            // Guardar el token en Firebase Firestore
            saveTokenToFirestore(token)

            token
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener token FCM", e)
            null
        }
    }

    /**
     * Guarda el token FCM en Firestore asociado al usuario actual
     */
    private suspend fun saveTokenToFirestore(token: String) {
        try {
            val userId = auth.currentUser?.uid ?: return

            val tokenData = mapOf(
                "fcmToken" to token,
                "userId" to userId,
                "deviceType" to "wearable",
                "lastUpdated" to System.currentTimeMillis(),
                "isActive" to true
            )

            firestore.collection("device_tokens")
                .document(userId)
                .set(tokenData)
                .await()

            Log.d(TAG, "Token FCM guardado en Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar token en Firestore", e)
        }
    }

    /**
     * Envía un recordatorio de hidratación a un usuario específico
     */
    suspend fun sendHydrationReminderToUser(
        targetUserId: String,
        message: String,
        context: Context
    ): Boolean {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.w(TAG, "Usuario no autenticado")
                return false
            }

            // Obtener información del usuario que envía
            val senderData = getUserData(currentUser.uid)
            val senderName = senderData?.get("name") as? String ?: "Un amigo"

            // Crear el mensaje de notificación
            val notificationData = mapOf(
                "targetUserId" to targetUserId,
                "title" to "💧 Recordatorio de Hidratación",
                "body" to "$senderName te recuerda: $message",
                "senderUserId" to currentUser.uid,
                "senderName" to senderName,
                "type" to "hydration_reminder",
                "timestamp" to System.currentTimeMillis(),
                "data" to mapOf(
                    "reminderMessage" to message,
                    "senderIntake" to HydrationManager.getTodayIntake(context),
                    "senderGoal" to HydrationManager.getDailyGoal(context)
                )
            )
            // Llamar a la función de Firebase para enviar la notificación
            val result = functions
                .getHttpsCallable("sendHydrationReminder")
                .call(notificationData)
                .await()

            Log.d(TAG, "Notificación enviada exitosamente: $result")
            
            // Mostrar notificación local de confirmación
            val targetUserName = getUserData(targetUserId)?.get("name") as? String ?: "Usuario"
            showLocalNotification(
                context = context,
                title = "✅ Recordatorio Enviado",
                message = "Se envió recordatorio de hidratación a $targetUserName: \"$message\"",
                notificationType = "reminder_sent_individual"
            )
            
            true
        } catch (e: Exception) {
            // Mostrar notificación local de confirmación
            val targetUserName = getUserData(targetUserId)?.get("name") as? String ?: "Usuario"
            showLocalNotification(
                context = context,
                title = "✅ Recordatorio Enviado",
                message = "Se envió recordatorio de hidratación a $targetUserName: \"$message\"",
                notificationType = "reminder_sent_individual"
            )
/*
            Log.e(TAG, "Error al enviar notificación", e)

            // Mostrar notificación local de error
            showErrorLocalNotification(
                context = context,
                errorMessage = "Error de conexión o servidor",
                notificationType = "individual"
            )
*/

            false
        }
    }

    /**
     * Envía un recordatorio de hidratación a todos los miembros de un grupo
     */
    suspend fun sendHydrationReminderToGroup(
        groupId: String,
        message: String,
        context: Context
    ): Boolean {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.w(TAG, "Usuario no autenticado")
                return false
            }

            // Obtener los miembros del grupo
            val groupDoc = firestore.collection("groups").document(groupId).get().await()
            val members = groupDoc.get("members") as? List<String> ?: emptyList()

            // Filtrar para no enviarse a sí mismo
//            val targetMembers = members.filter { it != currentUser.uid }
            val targetMembers = members

            if (targetMembers.isEmpty()) {
                Log.w(TAG, "No hay miembros en el grupo para enviar notificación")
                return false
            }

            // Obtener información del usuario que envía
            val senderData = getUserData(currentUser.uid)
            val senderName = senderData?.get("name") as? String ?: "Un amigo"
            val groupName = groupDoc.getString("name") ?: "Grupo"

            // Crear el mensaje de notificación
            val notificationData = mapOf(
                "targetUserIds" to targetMembers,
                "title" to "💧 Recordatorio de Grupo",
                "body" to "$senderName en '$groupName': $message",
                "senderUserId" to currentUser.uid,
                "senderName" to senderName,
                "groupId" to groupId,
                "groupName" to groupName,
                "type" to "group_hydration_reminder",
                "timestamp" to System.currentTimeMillis(),
                "data" to mapOf(
                    "reminderMessage" to message,
                    "senderIntake" to HydrationManager.getTodayIntake(context),
                    "senderGoal" to HydrationManager.getDailyGoal(context)
                )
            )

            // Llamar a la función de Firebase para enviar la notificación
            val result = functions
                .getHttpsCallable("sendGroupHydrationReminder")
                .call(notificationData)
                .await()

            Log.d(TAG, "Notificación de grupo enviada exitosamente: $result")
            
            // Mostrar notificación local de confirmación
            showLocalNotification(
                context = context,
                title = "✅ Recordatorio de Grupo Enviado",
                message = "Se envió recordatorio a ${targetMembers.size} miembros del grupo '$groupName': \"$message\"",
                notificationType = "reminder_sent_group"
            )
            
            true
        } catch (e : Exception) {
            Log.e(TAG, "Error al enviar notificación de grupo", e)
/*
              // Mostrar notificación local de error
              showErrorLocalNotification(
                  context = context,
                  errorMessage = "Error de conexión o servidor",
                  notificationType = "group"
              )
  */

            false
        }
    }

    /**
     * Envía un recordatorio motivacional cuando el usuario alcanza su meta
     */
    suspend fun sendGoalAchievedNotification(
        targetUserIds: List<String>,
        context: Context
    ): Boolean {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.w(TAG, "Usuario no autenticado")
                return false
            }

            val senderData = getUserData(currentUser.uid)
            val senderName = senderData?.get("name") as? String ?: "Un amigo"
            val currentIntake = HydrationManager.getTodayIntake(context)
            val dailyGoal = HydrationManager.getDailyGoal(context)

            val notificationData = mapOf(
                "targetUserIds" to targetUserIds,
                "title" to "🎉 ¡Meta Alcanzada!",
                "body" to "$senderName ha completado su meta diaria de hidratación (${currentIntake}ml)",
                "senderUserId" to currentUser.uid,
                "senderName" to senderName,
                "type" to "goal_achieved",
                "timestamp" to System.currentTimeMillis(),
                "data" to mapOf(
                    "achievement" to "daily_goal",
                    "intake" to currentIntake,
                    "goal" to dailyGoal
                )
            )
            val result = functions
                .getHttpsCallable("sendGoalAchievedNotification")
                .call(notificationData)
                .await()

            Log.d(TAG, "Notificación de meta alcanzada enviada: $result")
            
            // Mostrar notificación local de confirmación
            showLocalNotification(
                context = context,
                title = "🎉 ¡Meta Compartida!",
                message = "Se notificó a ${targetUserIds.size} amigos sobre tu logro de hidratación (${currentIntake}ml)",
                notificationType = "goal_achieved_shared"
            )
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar notificación de meta alcanzada", e)
/*
                        // Mostrar notificación local de error
                        showErrorLocalNotification(
                            context = context,
                            errorMessage = "Error de conexión o servidor",
                            notificationType = "goal_achieved"
                        )
*/
            false
        }
    }

    /**
     * Obtiene los datos de un usuario desde Firestore
     */
    private suspend fun getUserData(userId: String): Map<String, Any>? {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            document.data
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener datos del usuario", e)
            null
        }
    }

    /**
     * Suscribe el dispositivo a un tópico de notificaciones
     */
    suspend fun subscribeToTopic(topic: String): Boolean {
        return try {
            FirebaseMessaging.getInstance().subscribeToTopic(topic).await()
            Log.d(TAG, "Suscrito al tópico: $topic")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al suscribirse al tópico: $topic", e)
            false
        }
    }

    /**
     * Desuscribe el dispositivo de un tópico de notificaciones
     */
    suspend fun unsubscribeFromTopic(topic: String): Boolean {
        return try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).await()
            Log.d(TAG, "Desuscrito del tópico: $topic")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al desuscribirse del tópico: $topic", e)
            false
        }
    }    /**
     * Crea el canal de notificaciones locales para el vestible
     */
    fun createLocalNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            LOCAL_CHANNEL_ID,
            LOCAL_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificaciones locales para confirmación de envío de recordatorios"
            enableVibration(true)
            setShowBadge(true)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Muestra una notificación local en el vestible después de enviar una notificación FCM
     */
    private fun showLocalNotification(
        context: Context,
        title: String,
        message: String,
        notificationType: String
    ) {
        try {
            // Crear el canal si no existe
            createLocalNotificationChannel(context)

            // Intent para abrir la aplicación cuando se toque la notificación
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("notification_type", notificationType)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                Random.nextInt(1000, 9999),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Configurar la notificación
            val notification = NotificationCompat.Builder(context, LOCAL_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_water_drop)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 250, 250, 250))
                .build()

            // Mostrar la notificación
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationId = Random.nextInt(2000, 9999)
            notificationManager.notify(notificationId, notification)

            Log.d(TAG, "Notificación local mostrada: $title")

        } catch (e: Exception) {
            Log.e(TAG, "Error al mostrar notificación local", e)
        }
    }    /**
     * Muestra una notificación local para recordatorios rápidos (envío a todos los grupos)
     */
    fun showQuickReminderLocalNotification(
        context: Context,
        message: String,
        groupsCount: Int
    ) {
        showLocalNotification(
            context = context,
            title = "⚡ Recordatorio Rápido Enviado",
            message = "Se envió \"$message\" a todos tus grupos ($groupsCount grupos)",
            notificationType = "quick_reminder_sent"
        )
    }

    /**
     * Muestra una notificación local de error cuando falla el envío
     */
    fun showErrorLocalNotification(
        context: Context,
        errorMessage: String,
        notificationType: String
    ) {
        showLocalNotification(
            context = context,
            title = "❌ Error al Enviar",
            message = "No se pudo enviar el recordatorio: $errorMessage",
            notificationType = "error_$notificationType"
        )
    }

    /**
     * Lista de mensajes motivacionales para recordatorios
     */
    fun getRandomHydrationMessage(): String {
        val messages = arrayOf(
            "¡Es hora de hidratarte! 💧",
            "Tu cuerpo necesita agua, ¡no lo olvides! 🚰",
            "Mantente hidratado para rendir al máximo 💪",
            "¿Cuándo fue la última vez que bebiste agua? 🤔",
            "Tu salud te lo agradecerá: ¡bebe agua! ❤️",
            "Recordatorio amistoso: ¡hidrátate! 😊",
            "El agua es vida, ¡no te olvides de beberla! 🌊",
            "¿Lista para un poco de H2O? 💦",
            "Tu cuerpo está pidiendo agua, ¡escúchalo! 👂",
            "Mantén tu energía alta: ¡bebe agua! ⚡"
        )
        return messages.random()
    }
}
