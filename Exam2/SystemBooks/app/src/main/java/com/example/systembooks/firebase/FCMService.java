package com.example.systembooks.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.systembooks.MainActivity;
import com.example.systembooks.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Service to handle Firebase Cloud Messaging notifications
 */
public class FCMService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "SystemBooksChannel";
    private static final String CHANNEL_NAME = "SystemBooks Notifications";
    private static final String CHANNEL_DESC = "Notifications from SystemBooks app";    
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM token received: " + token.substring(0, Math.min(token.length(), 10)) + "...");
        
        // Save the token to Firestore with priority set to urgent
        saveTokenToFirestore(token);
        
        // Also update the token in the FirebaseManager for immediate use
        if (FirebaseManager.getInstance().getContext() != null) {
            Log.d(TAG, "Updating token in FirebaseManager");
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        Log.d(TAG, "FCM message received from: " + remoteMessage.getFrom());
        
        // Obtener timestamp de recepción para debugging
        long receivedTime = System.currentTimeMillis();
        
        // Información de depuración adicional
        String messageId = remoteMessage.getMessageId();
        String notificationId = remoteMessage.getData().get("notificationId");
        String notificationType = remoteMessage.getData().get("type");
        Log.d(TAG, "Message ID: " + messageId + ", Notification ID: " + notificationId + ", Type: " + notificationType);
        
        // Check if the message contains data
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data: " + remoteMessage.getData());
        }

        // Check if the message contains a notification
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Message notification - Title: " + title + ", Body: " + body);
            
            // Customize notification based on type
            if ("friend_request".equals(notificationType)) {
                sendFriendRequestNotification(title, body, remoteMessage.getData());
            } else if ("friend_request_accepted".equals(notificationType)) {
                sendFriendRequestAcceptedNotification(title, body, remoteMessage.getData());
            } else {
                // Mostrar la notificación normal
                sendNotification(title, body);
            }
            
            // Enviar confirmación a Firestore para seguimiento (opcional)
            if (notificationId != null) {
                sendDeliveryReceipt(notificationId, receivedTime);
            }
        } else if (remoteMessage.getData().size() > 0) {
            // Si no hay notificación pero sí hay datos, crear una notificación
            String title = remoteMessage.getData().getOrDefault("title", "SystemBooks");
            String body = remoteMessage.getData().getOrDefault("body", "Nueva notificación");
            Log.d(TAG, "Created notification from data - Title: " + title + ", Body: " + body);
            
            // Customize notification based on type
            if ("friend_request".equals(notificationType)) {
                sendFriendRequestNotification(title, body, remoteMessage.getData());
            } else if ("friend_request_accepted".equals(notificationType)) {
                sendFriendRequestAcceptedNotification(title, body, remoteMessage.getData());
            } else {
                sendNotification(title, body);
            }
        }
    }
    
    /**
     * Send receipt of notification delivery for tracking
     * @param notificationId The ID of the notification in Firestore
     * @param receivedTime The time the notification was received
     */
    private void sendDeliveryReceipt(String notificationId, long receivedTime) {
        try {
            String userId = FirebaseManager.getInstance().getAuth().getUid();
            if (userId == null) return;
            
            Map<String, Object> receipt = new HashMap<>();
            receipt.put("notificationId", notificationId);
            receipt.put("userId", userId);
            receipt.put("receivedAt", receivedTime);
            receipt.put("deviceInfo", getDeviceInfo());
            
            FirebaseManager.getInstance().getFirestore()
                .collection("notification_receipts")
                .add(receipt)
                .addOnSuccessListener(ref -> Log.d(TAG, "Notification receipt sent"))
                .addOnFailureListener(e -> Log.e(TAG, "Error sending notification receipt", e));
        } catch (Exception e) {
            Log.e(TAG, "Error creating delivery receipt", e);
        }
    }
    
    /**
     * Get device information for debugging
     * @return Map with device info
     */
    private Map<String, Object> getDeviceInfo() {
        Map<String, Object> deviceInfo = new HashMap<>();
        deviceInfo.put("manufacturer", android.os.Build.MANUFACTURER);
        deviceInfo.put("model", android.os.Build.MODEL);
        deviceInfo.put("androidVersion", android.os.Build.VERSION.RELEASE);
        deviceInfo.put("sdkVersion", android.os.Build.VERSION.SDK_INT);
        return deviceInfo;
    }    /**
     * Create and show a notification with the given title and body
     * @param title Notification title
     * @param body Notification body text
     */
    private void sendNotification(String title, String body) {
        Log.d(TAG, "Showing notification - Title: " + title + ", Body: " + body);
        
        // Create intent to open the app when notification is tapped
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        // Add a unique ID to avoid PendingIntent collision
        int notificationId = (int) System.currentTimeMillis();
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Set notification sound
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        
        // Build the notification with high priority
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_HIGH) // Alta prioridad
                        .setContentIntent(pendingIntent)
                        .setVibrate(new long[]{0, 250, 250, 250}); // Vibración

        // Get the notification manager
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android Oreo+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH); // Importancia alta
            channel.setDescription(CHANNEL_DESC);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 250, 250, 250});
            channel.enableLights(true);
            channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }

        // Show the notification with unique ID para evitar sobrescribir notificaciones anteriores
        notificationManager.notify(notificationId, notificationBuilder.build());
    }/**
     * Save FCM token to user's document in Firestore
     * @param token The FCM token
     */    private void saveTokenToFirestore(String token) {
        String userId = FirebaseManager.getInstance().getAuth().getUid();
        if (userId != null) {
            Log.d(TAG, "Saving FCM token for user: " + userId);
            
            // Guardar el token en la colección específica para tokens FCM
            NotificationHelper notificationHelper = new NotificationHelper(this);
            notificationHelper.storeTokenForUser(userId, token);
        } else {
            Log.w(TAG, "Cannot save FCM token: User not logged in");
            
            // Guardar el token localmente para asignarlo cuando el usuario inicie sesión
            try {
                getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("pending_fcm_token", token)
                    .putLong("token_timestamp", System.currentTimeMillis())
                    .apply();
                Log.d(TAG, "Token saved locally for later registration");
            } catch (Exception e) {
                Log.e(TAG, "Error saving token locally", e);
            }
        }
    }

    /**
     * Send a friend request notification
     * @param title Notification title
     * @param body Notification body text
     * @param data Additional data from FCM
     */
    private void sendFriendRequestNotification(String title, String body, Map<String, String> data) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("open_friends", true);
        intent.putExtra("notification_type", "friend_request");

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android Oreo+
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

        // Build the notification with high priority and friend request styling
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_friends)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setVibrate(new long[]{0, 250, 250, 250});

        // Show the notification with unique ID
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    /**
     * Send a friend request accepted notification
     * @param title Notification title
     * @param body Notification body text
     * @param data Additional data from FCM
     */
    private void sendFriendRequestAcceptedNotification(String title, String body, Map<String, String> data) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("open_friends", true);
        intent.putExtra("notification_type", "friend_request_accepted");

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android Oreo+
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

        // Build the notification with high priority and acceptance styling
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_friends)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setVibrate(new long[]{0, 250, 250, 250});

        // Show the notification with unique ID
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}
