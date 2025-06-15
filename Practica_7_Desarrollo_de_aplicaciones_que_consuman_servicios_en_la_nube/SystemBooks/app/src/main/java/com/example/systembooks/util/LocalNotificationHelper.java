package com.example.systembooks.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.systembooks.MainActivity;
import com.example.systembooks.R;

/**
 * Helper class for sending local notifications without requiring Firebase or internet connection
 */
public class LocalNotificationHelper {
    private static final String TAG = "LocalNotificationHelper";
    private static final String CHANNEL_ID = "SystemBooksLocalChannel";
    private static final String CHANNEL_NAME = "Local Notifications";
    private static final String CHANNEL_DESC = "Local notifications from SystemBooks app";

    private final Context context;
    private final NotificationManager notificationManager;
    
    public LocalNotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        // Create notification channel for Android Oreo+
        createNotificationChannel();
    }
    
    /**
     * Creates the notification channel for Android Oreo+
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESC);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 250, 250, 250});
            channel.enableLights(true);
            channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Send a local notification
     * @param title Notification title
     * @param message Notification body
     */
    public void sendLocalNotification(String title, String message) {
        Log.d(TAG, "Sending local notification - Title: " + title + ", Message: " + message);
        
        int notificationId = (int) System.currentTimeMillis();
        sendLocalNotification(title, message, notificationId, null);
    }
    
    /**
     * Send a local notification with custom data
     * @param title Notification title
     * @param message Notification body
     * @param notificationId Unique ID for the notification
     * @param intentExtras Additional data to include in the intent
     */
    public void sendLocalNotification(String title, String message, int notificationId, Intent intentExtras) {
        // Create intent to open the app when notification is tapped
        Intent intent = intentExtras != null ? intentExtras : new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Set notification sound
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        
        // Build the notification with high priority
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setVibrate(new long[]{0, 250, 250, 250});

        // Show the notification
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
    
    /**
     * Cancel a specific notification
     * @param notificationId ID of the notification to cancel
     */
    public void cancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
    }
    
    /**
     * Cancel all notifications from this app
     */
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }
}
