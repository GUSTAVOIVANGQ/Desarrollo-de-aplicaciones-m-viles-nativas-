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
        Log.d(TAG, "New FCM token: " + token);
        
        // Save the token to Firestore (to be used later for sending notifications to this device)
        saveTokenToFirestore(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if the message contains data
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data: " + remoteMessage.getData());
        }

        // Check if the message contains a notification
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message notification body: " + remoteMessage.getNotification().getBody());
            sendNotification(
                remoteMessage.getNotification().getTitle(),
                remoteMessage.getNotification().getBody()
            );
        }
    }

    /**
     * Create and show a notification with the given title and body
     * @param title Notification title
     * @param body Notification body text
     */
    private void sendNotification(String title, String body) {
        // Create intent to open the app when notification is tapped
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        // Set notification sound
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        
        // Build the notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        // Get the notification manager
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android Oreo+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESC);
            notificationManager.createNotificationChannel(channel);
        }

        // Show the notification
        notificationManager.notify(0, notificationBuilder.build());
    }    /**
     * Save FCM token to user's document in Firestore
     * @param token The FCM token
     */
    private void saveTokenToFirestore(String token) {
        String userId = FirebaseManager.getInstance().getAuth().getUid();
        if (userId != null) {
            // Guardar el token en la colección específica para tokens FCM
            NotificationHelper notificationHelper = new NotificationHelper(this);
            notificationHelper.storeTokenForUser(userId, token);
            
            // También actualizar el token en el documento del usuario para referencia rápida
            FirebaseManager.getInstance().getFirestore()
                    .collection("users")
                    .document(userId)
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Token saved to Firestore"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error saving token", e));
        }
    }
}
