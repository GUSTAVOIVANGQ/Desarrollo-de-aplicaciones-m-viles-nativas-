package com.example.exam3.client

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.exam3.ClientActivity
import com.example.exam3.R

/**
 * Servicio para manejar notificaciones de BlueWeb
 */
class NotificationService(private val context: Context) {
    
    companion object {
        private const val CHANNEL_ID = "blueweb_channel"
        private const val CHANNEL_NAME = "BlueWeb Navegador"
        private const val CHANNEL_DESCRIPTION = "Notificaciones del navegador BlueWeb"
        
        const val NOTIFICATION_CONNECTION_ID = 1
        const val NOTIFICATION_DOWNLOAD_ID = 2
        const val NOTIFICATION_ERROR_ID = 3
        const val NOTIFICATION_LOW_POWER_ID = 4
    }
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Crear canal de notificaciones (Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                lightColor = ContextCompat.getColor(context, R.color.azul_primary)
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 200, 300, 400)
            }
            
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Mostrar notificación de estado de conexión
     */
    fun showConnectionNotification(isConnected: Boolean, deviceName: String? = null) {
        val title = if (isConnected) "🔗 Conectado por Bluetooth" else "❌ Desconectado"
        val content = if (isConnected) {
            "Conectado con: ${deviceName ?: "Dispositivo desconocido"}"
        } else {
            "Conexión Bluetooth perdida"
        }
        
        val intent = Intent(context, ClientActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(context, if (isConnected) R.color.success else R.color.error))
            .build()
        
        notificationManager.notify(NOTIFICATION_CONNECTION_ID, notification)
    }
    
    /**
     * Mostrar notificación de descarga de página
     */
    fun showDownloadNotification(url: String, isSuccess: Boolean, statusCode: Int = 200) {
        val title = if (isSuccess) "📄 Página descargada" else "❌ Error de descarga"
        val content = if (isSuccess) {
            "Página cargada exitosamente"
        } else {
            "Error $statusCode al cargar la página"
        }
        
        val domain = try {
            java.net.URL(url).host
        } catch (e: Exception) {
            "sitio web"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText("$domain - $content")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setTimeoutAfter(5000) // Auto-dismiss después de 5 segundos
            .setColor(ContextCompat.getColor(context, if (isSuccess) R.color.success else R.color.error))
            .build()
        
        notificationManager.notify(NOTIFICATION_DOWNLOAD_ID, notification)
    }
    
    /**
     * Mostrar notificación de progreso de descarga
     */
    fun showDownloadProgressNotification(url: String, progress: Int) {
        val domain = try {
            java.net.URL(url).host
        } catch (e: Exception) {
            "sitio web"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("📥 Descargando página...")
            .setContentText("$domain - $progress%")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setProgress(100, progress, progress == 0)
            .setOngoing(true)
            .setColor(ContextCompat.getColor(context, R.color.info))
            .build()
        
        notificationManager.notify(NOTIFICATION_DOWNLOAD_ID, notification)
    }
    
    /**
     * Mostrar notificación de error
     */
    fun showErrorNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⚠️ $title")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(context, R.color.error))
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .build()
        
        notificationManager.notify(NOTIFICATION_ERROR_ID, notification)
    }
    
    /**
     * Mostrar notificación de modo bajo consumo
     */
    fun showLowPowerModeNotification(enabled: Boolean) {
        val title = if (enabled) "🔋 Modo Bajo Consumo Activado" else "🔌 Modo Normal Activado"
        val content = if (enabled) {
            "Contenido optimizado para Bluetooth"
        } else {
            "Contenido completo activado"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setTimeoutAfter(3000)
            .setColor(ContextCompat.getColor(context, if (enabled) R.color.warning else R.color.success))
            .build()
        
        notificationManager.notify(NOTIFICATION_LOW_POWER_ID, notification)
    }
    
    /**
     * Mostrar notificación persistente mientras está conectado
     */
    fun showPersistentConnectionNotification(deviceName: String): Notification {
        val intent = Intent(context, ClientActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🌐 BlueWeb Navegador")
            .setContentText("Conectado con: $deviceName")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setColor(ContextCompat.getColor(context, R.color.azul_primary))
            .build()
    }
    
    /**
     * Cancelar una notificación específica
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
    
    /**
     * Cancelar todas las notificaciones
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
    
    /**
     * Verificar si las notificaciones están habilitadas
     */
    fun areNotificationsEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationManager.areNotificationsEnabled()
        } else {
            true
        }
    }
}
