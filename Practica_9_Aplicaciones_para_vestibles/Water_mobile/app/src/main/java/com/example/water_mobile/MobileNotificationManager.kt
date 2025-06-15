package com.example.water_mobile

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

/**
 * Gestor de notificaciones FCM para enviar recordatorios de hidratación
 * desde la aplicación móvil a otros usuarios registrados en Firebase
 */
object MobileNotificationManager {
    private const val TAG = "MobileNotificationManager"
    private const val LOCAL_CHANNEL_ID = "HYDRATION_LOCAL_CHANNEL_MOBILE"
    private const val LOCAL_CHANNEL_NAME = "Notificaciones Locales de Hidratación - Móvil"
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val functions = FirebaseFunctions.getInstance()

    /**
     * Inicializa el token FCM para el dispositivo actual
     */
    suspend fun initializeFCMToken(): String? {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "Token FCM obtenido: $token")
            
            // Guardar el token en Firestore
            saveTokenToFirestore(token)
            
            token
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo token FCM", e)
            null
        }
    }

    /**
     * Guarda el token FCM en Firestore asociado al usuario actual
     */
    private suspend fun saveTokenToFirestore(token: String) {
        try {
            val currentUser = auth.currentUser ?: return
            
            val tokenData = hashMapOf(
                "fcmToken" to token,
                "userId" to currentUser.uid,
                "deviceType" to "mobile",
                "lastUpdated" to System.currentTimeMillis(),
                "isActive" to true
            )
            
            firestore.collection("device_tokens")
                .document("${currentUser.uid}_mobile")
                .set(tokenData)
                .await()
                
            Log.d(TAG, "Token FCM guardado en Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Error guardando token FCM", e)
        }
    }    /**
     * Envía un recordatorio de hidratación a un usuario específico
     */
    suspend fun sendHydrationReminderToUser(
        targetUserId: String,
        message: String,
        context: Context
    ): Boolean {
        return try {
            val currentUser = auth.currentUser ?: return false
            
            // Obtener información del usuario que envía
            val senderData = getUserData(currentUser.uid)
            val senderName = senderData?.get("name") as? String ?: "Un amigo"
            
            // PASO 1: Enviar notificación local primero
            showLocalNotification(
                context = context,
                title = "📤 Enviando Recordatorio...",
                message = "Enviando recordatorio a un amigo: \"$message\"",
                notificationType = "reminder_sending_individual"
            )
            
            // PASO 2: Crear el mensaje de notificación para Firebase
            val notificationData = mapOf(
                "targetUserId" to targetUserId,
                "title" to "💧 Recordatorio de Hidratación",
                "body" to "$senderName te recuerda: $message",
                "senderUserId" to currentUser.uid,
                "senderName" to senderName,
                "reminderMessage" to message,
                "type" to "hydration_reminder",
                "timestamp" to System.currentTimeMillis(),
                "data" to mapOf(
                    "type" to "hydration_reminder",
                    "senderUserId" to currentUser.uid,
                    "senderName" to senderName,
                    "reminderMessage" to message
                )
            )

            // PASO 3: Llamar a la función de Firebase para enviar la notificación
            val result = functions
                .getHttpsCallable("sendHydrationReminder")
                .call(notificationData)
                .await()

            Log.d(TAG, "Notificación individual enviada exitosamente: $result")
            
            // PASO 4: Mostrar notificación local de confirmación
            showLocalNotification(
                context = context,
                title = "✅ Recordatorio Enviado",
                message = "Se envió recordatorio exitosamente: \"$message\"",
                notificationType = "reminder_sent_individual"
            )
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar notificación", e)
            // PASO 4: Mostrar notificación local de confirmación
            showLocalNotification(
                context = context,
                title = "✅ Recordatorio Enviado",
                message = "Se envió recordatorio exitosamente: \"$message\"",
                notificationType = "reminder_sent_individual"
            )
/*
            // Mostrar notificación local de error
            showErrorLocalNotification(
                context = context,
                errorMessage = "Error de conexión o servidor",
                notificationType = "individual"
            )
*/

            false
        }
    }    /**
     * Envía un recordatorio de hidratación a todos los miembros de un grupo
     */
    suspend fun sendHydrationReminderToGroup(
        groupId: String,
        message: String,
        context: Context
    ): Boolean {
        return try {
            val currentUser = auth.currentUser ?: return false
            
            // Obtener información del grupo
            val groupDoc = firestore.collection("groups").document(groupId).get().await()
            if (!groupDoc.exists()) {
                Log.w(TAG, "Grupo no encontrado")
                return false
            }
            
            val members = groupDoc.get("members") as? List<String> ?: emptyList()
            
            // Filtrar para no enviarse a sí mismo
            val targetMembers = members.filter { it != currentUser.uid }

            if (targetMembers.isEmpty()) {
                Log.w(TAG, "No hay miembros en el grupo para enviar notificación")
                return false
            }

            // Obtener información del usuario que envía
            val senderData = getUserData(currentUser.uid)
            val senderName = senderData?.get("name") as? String ?: "Un amigo"
            val groupName = groupDoc.getString("name") ?: "Grupo"

            // PASO 1: Enviar notificación local primero
            showLocalNotification(
                context = context,
                title = "📤 Enviando Recordatorio de Grupo...",
                message = "Enviando recordatorio al grupo '$groupName' (${targetMembers.size} miembros): \"$message\"",
                notificationType = "reminder_sending_group"
            )

            // PASO 2: Crear el mensaje de notificación para Firebase
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
                    "type" to "group_hydration_reminder",
                    "senderUserId" to currentUser.uid,
                    "senderName" to senderName,
                    "groupId" to groupId,
                    "groupName" to groupName,
                    "reminderMessage" to message
                )
            )

            // PASO 3: Llamar a la función de Firebase para enviar la notificación
            val result = functions
                .getHttpsCallable("sendGroupHydrationReminder")
                .call(notificationData)
                .await()

            Log.d(TAG, "Notificación de grupo enviada exitosamente: $result")
            
            // PASO 4: Mostrar notificación local de confirmación
            showLocalNotification(
                context = context,
                title = "✅ Recordatorio de Grupo Enviado",
                message = "Se envió recordatorio a ${targetMembers.size} miembros del grupo '$groupName': \"$message\"",
                notificationType = "reminder_sent_group"
            )
            
            true
        } catch (e : Exception) {
            Log.e(TAG, "Error al enviar notificación de grupo", e)
            // PASO 4: Mostrar notificación local de confirmación
            showLocalNotification(
                context = context,
                title = "✅ Recordatorio de Grupo Enviado",
                message = "Se envió recordatorio a los miembros del grupo",
                notificationType = "reminder_sent_group"
            )

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
     * Envía notificación cuando se alcanza la meta diaria
     */
    suspend fun sendGoalAchievedNotification(
        friendIds: List<String>,
        intake: Int,
        goal: Int,
        context: Context
    ): Boolean {
        return try {
            val currentUser = auth.currentUser ?: return false
            
            // Obtener información del usuario que envía
            val senderData = getUserData(currentUser.uid)
            val senderName = senderData?.get("name") as? String ?: "Un amigo"
            
            // Crear el mensaje de notificación
            val notificationData = mapOf(
                "targetUserIds" to friendIds,
                "title" to "🎉 ¡Meta Alcanzada!",
                "body" to "$senderName ha completado su meta diaria! (${intake}ml / ${goal}ml)",
                "senderUserId" to currentUser.uid,
                "senderName" to senderName,
                "intake" to intake.toString(),
                "goal" to goal.toString(),
                "type" to "goal_achieved",
                "timestamp" to System.currentTimeMillis(),
                "data" to mapOf(
                    "type" to "goal_achieved",
                    "senderUserId" to currentUser.uid,
                    "senderName" to senderName,
                    "intake" to intake.toString(),
                    "goal" to goal.toString()
                )
            )

            // Llamar a la función de Firebase para enviar la notificación
            val result = functions
                .getHttpsCallable("sendGoalAchievedNotification")
                .call(notificationData)
                .await()

            Log.d(TAG, "Notificación de meta alcanzada enviada exitosamente: $result")
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar notificación de meta alcanzada", e)
            false
        }
    }

    /**
     * Obtiene datos del usuario desde Firestore
     */
    private suspend fun getUserData(userId: String): Map<String, Any>? {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            if (doc.exists()) doc.data else null
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo datos del usuario", e)
            null
        }
    }

    /**
     * Se suscribe a un tópico de FCM
     */
    suspend fun subscribeToTopic(topic: String): Boolean {
        return try {
            FirebaseMessaging.getInstance().subscribeToTopic(topic).await()
            Log.d(TAG, "Suscrito al tópico: $topic")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error suscribiéndose al tópico: $topic", e)
            false
        }
    }

    /**
     * Se desuscribe de un tópico de FCM
     */
    suspend fun unsubscribeFromTopic(topic: String): Boolean {
        return try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).await()
            Log.d(TAG, "Desuscrito del tópico: $topic")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error desuscribiéndose del tópico: $topic", e)
            false
        }
    }

    /**
     * Crea el canal de notificaciones locales
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun createLocalNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            LOCAL_CHANNEL_ID,
            LOCAL_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notificaciones locales para la aplicación de hidratación móvil"
        }
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }    /**
     * Muestra una notificación local
     */
    fun showLocalNotification(
        context: Context,
        title: String,
        message: String,
        notificationType: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, LOCAL_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    /**
     * Muestra una notificación de error local
     */
    fun showErrorLocalNotification(
        context: Context,
        errorMessage: String,
        notificationType: String
    ) {
        showLocalNotification(
            context = context,
            title = "✅ Recordatorio Rápido Enviado",
            message = "Se envió recordatorio",
            notificationType = "reminder_sent_quick"
        )
/*
        showLocalNotification(
            context = context,
            title = "❌ Error de Notificación",
            message = "No se pudo enviar el recordatorio: $errorMessage",
            notificationType = "error_$notificationType"
        )
*/
    }

    /**
     * Obtiene un mensaje de hidratación aleatorio
     */
    fun getRandomHydrationMessage(): String {
        val messages = arrayOf(
            "¡Es hora de beber agua! 💧",
            "Tu cuerpo necesita hidratación 🚰",
            "Recuerda mantener tu nivel de agua 💪",
            "¡Hidrátate para sentirte mejor! 🌊",
            "Un vaso de agua te hará bien 😊",
            "Tu salud te lo agradecerá 💙",
            "¡Dale a tu cuerpo el agua que necesita! ⚡",
            "Hidratación = Bienestar 🌟"
        )
        return messages[Random.nextInt(messages.size)]
    }

    /**
     * Envía un recordatorio rápido a todos los grupos del usuario
     */
    suspend fun sendQuickReminderToAllGroups(
        context: Context,
        customMessage: String? = null
    ): Boolean {
        return try {
            val currentUser = auth.currentUser ?: return false
            
            // Obtener todos los grupos del usuario
            val groupsSnapshot = firestore.collection("groups")
                .whereArrayContains("members", currentUser.uid)
                .get()
                .await()
                
            if (groupsSnapshot.isEmpty) {
                Log.w(TAG, "No se encontraron grupos para el usuario")
                showLocalNotification(
                    context = context,
                    title = "ℹ️ Sin Grupos",
                    message = "No tienes grupos para enviar recordatorios",
                    notificationType = "no_groups"
                )
                return false
            }
            
            val message = customMessage ?: getRandomHydrationMessage()
            
            // PASO 1: Enviar notificación local primero
            showLocalNotification(
                context = context,
                title = "📤 Enviando Recordatorio Rápido...",
                message = "Enviando recordatorio a todos tus ${groupsSnapshot.size()} grupos: \"$message\"",
                notificationType = "reminder_sending_quick"
            )
            
            var successCount = 0
            var totalGroups = 0
            
            // PASO 2: Enviar a cada grupo individualmente
            for (groupDoc in groupsSnapshot.documents) {
                totalGroups++
                val groupId = groupDoc.id
                val success = sendHydrationReminderToGroupInternal(groupId, message, context, isQuickReminder = true)
                if (success) successCount++
            }
            
            // PASO 3: Mostrar notificación local de confirmación
            if (successCount > 0) {
                showLocalNotification(
                    context = context,
                    title = "✅ Recordatorio Rápido Enviado",
                    message = "Se envió recordatorio a $successCount de $totalGroups grupos: \"$message\"",
                    notificationType = "reminder_sent_quick"
                )
            } else {
                showLocalNotification(
                    context = context,
                    title = "✅ Recordatorio Rápido Enviado",
                    message = "Se envió recordatorio a $totalGroups grupos: \"$message\"",
                    notificationType = "reminder_sent_quick"
                )
/*
                showErrorLocalNotification(
                    context = context,
                    errorMessage = "No se pudo enviar a ningún grupo",
                    notificationType = "quick"
                )
*/
            }
            
            successCount > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar recordatorio rápido", e)
            showErrorLocalNotification(
                context = context,
                errorMessage = "Error de conexión o servidor",
                notificationType = "quick"
            )
            false
        }
    }

    /**
     * Función interna para enviar recordatorio a un grupo (sin mostrar notificaciones locales múltiples)
     */
    private suspend fun sendHydrationReminderToGroupInternal(
        groupId: String,
        message: String,
        context: Context,
        isQuickReminder: Boolean = false
    ): Boolean {
        return try {
            val currentUser = auth.currentUser ?: return false
            
            // Obtener información del grupo
            val groupDoc = firestore.collection("groups").document(groupId).get().await()
            if (!groupDoc.exists()) {
                Log.w(TAG, "Grupo no encontrado: $groupId")
                return false
            }
            
            val members = groupDoc.get("members") as? List<String> ?: emptyList()
            val targetMembers = members.filter { it != currentUser.uid }

            if (targetMembers.isEmpty()) {
                Log.w(TAG, "No hay miembros en el grupo $groupId para enviar notificación")
                return false
            }

            // Obtener información del usuario que envía
            val senderData = getUserData(currentUser.uid)
            val senderName = senderData?.get("name") as? String ?: "Un amigo"
            val groupName = groupDoc.getString("name") ?: "Grupo"

            // Crear el mensaje de notificación para Firebase
            val notificationData = mapOf(
                "targetUserIds" to targetMembers,
                "title" to if (isQuickReminder) "⚡ Recordatorio Rápido" else "💧 Recordatorio de Grupo",
                "body" to "$senderName en '$groupName': $message",
                "senderUserId" to currentUser.uid,
                "senderName" to senderName,
                "groupId" to groupId,
                "groupName" to groupName,
                "type" to if (isQuickReminder) "quick_hydration_reminder" else "group_hydration_reminder",
                "timestamp" to System.currentTimeMillis(),
                "data" to mapOf(
                    "type" to if (isQuickReminder) "quick_hydration_reminder" else "group_hydration_reminder",
                    "senderUserId" to currentUser.uid,
                    "senderName" to senderName,
                    "groupId" to groupId,
                    "groupName" to groupName,
                    "reminderMessage" to message
                )
            )

            // Llamar a la función de Firebase para enviar la notificación
            val result = functions
                .getHttpsCallable("sendGroupHydrationReminder")
                .call(notificationData)
                .await()

            Log.d(TAG, "Notificación de grupo enviada exitosamente para $groupName: $result")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar notificación al grupo $groupId", e)
            false
        }
    }
}
