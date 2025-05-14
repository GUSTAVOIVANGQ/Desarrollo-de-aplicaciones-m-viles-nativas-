package com.example.systembooks.firebase;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for managing Firebase notifications
 */
public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static final String NOTIFICATIONS_COLLECTION = "notifications";
    private static final String TOKENS_COLLECTION = "fcm_tokens";

    private final Context context;
    private final FirebaseFirestore db;

    public NotificationHelper(Context context) {
        this.context = context;
        this.db = FirebaseManager.getInstance().getFirestore();
    }

    /**
     * Get the current FCM token
     * @param callback Callback to receive the token
     */
    public void getDeviceToken(FirebaseAuthRepository.FirebaseCallback<String> callback) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String token = task.getResult();
                            // Store token in Firestore if user is logged in
                            String userId = FirebaseManager.getInstance().getAuth().getUid();
                            if (userId != null) {
                                storeTokenForUser(userId, token);
                            }
                            callback.onSuccess(token);
                        } else {
                            callback.onError("Failed to get FCM token");
                        }
                    }
                });
    }    /**
     * Store the FCM token for a user
     * @param userId User ID
     * @param token FCM token
     */
    public void storeTokenForUser(String userId, String token) {
        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("token", token);
        tokenData.put("userId", userId);
        tokenData.put("timestamp", System.currentTimeMillis());

        db.collection(TOKENS_COLLECTION)
                .document(userId)
                .set(tokenData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Token stored successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error storing token", e));
    }

    /**
     * Send a notification to specific user
     * @param userId User ID to send notification to
     * @param title Notification title
     * @param body Notification body
     * @param data Additional data to send with the notification
     * @param callback Callback for result
     */
    public void sendNotificationToUser(String userId, String title, String body,
                                      Map<String, String> data,
                                      FirebaseAuthRepository.FirebaseCallback<Void> callback) {
        // Create notification data
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("body", body);
        notification.put("targetUserId", userId);
        notification.put("timestamp", System.currentTimeMillis());
        
        if (data != null) {
            notification.put("data", data);
        }

        // Store notification in Firestore (will trigger Cloud Function to send FCM)
        db.collection(NOTIFICATIONS_COLLECTION)
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Notification document added");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding notification document", e);
                    callback.onError("Failed to send notification: " + e.getMessage());
                });
    }

    /**
     * Send a notification to all users
     * @param title Notification title
     * @param body Notification body
     * @param data Additional data to send with the notification
     * @param callback Callback for result
     */
    public void sendNotificationToAllUsers(String title, String body,
                                         Map<String, String> data,
                                         FirebaseAuthRepository.FirebaseCallback<Void> callback) {
        // Create notification data
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("body", body);
        notification.put("toAll", true);
        notification.put("timestamp", System.currentTimeMillis());
        
        if (data != null) {
            notification.put("data", data);
        }

        // Store notification in Firestore (will trigger Cloud Function to send FCM)
        db.collection(NOTIFICATIONS_COLLECTION)
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Mass notification document added");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding mass notification document", e);
                    callback.onError("Failed to send notification: " + e.getMessage());
                });
    }

    /**
     * Subscribe to a topic for receiving topic-based notifications
     * @param topic Topic name
     * @param callback Callback for result
     */
    public void subscribeToTopic(String topic, FirebaseAuthRepository.FirebaseCallback<Void> callback) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Subscribed to topic: " + topic);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to subscribe to topic: " + topic, e);
                    callback.onError("Failed to subscribe: " + e.getMessage());
                });
    }

    /**
     * Unsubscribe from a topic
     * @param topic Topic name
     * @param callback Callback for result
     */
    public void unsubscribeFromTopic(String topic, FirebaseAuthRepository.FirebaseCallback<Void> callback) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Unsubscribed from topic: " + topic);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to unsubscribe from topic: " + topic, e);
                    callback.onError("Failed to unsubscribe: " + e.getMessage());
                });
    }
}
