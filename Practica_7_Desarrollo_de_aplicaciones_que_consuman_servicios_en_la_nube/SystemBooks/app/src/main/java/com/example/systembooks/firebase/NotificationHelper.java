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
    private final FirebaseFirestore db;    public NotificationHelper(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        
        this.context = context;
        
        try {
            // Verificar que FirebaseManager esté correctamente inicializado
            if (!FirebaseManager.isInitialized()) {
                Log.e(TAG, "FirebaseManager is not properly initialized");
                throw new IllegalStateException("FirebaseManager not properly initialized");
            }
            
            FirebaseManager manager = FirebaseManager.getInstance();
            this.db = manager.getFirestore();
            
            if (this.db == null) {
                Log.e(TAG, "Firestore instance is null");
                throw new IllegalStateException("Firestore not initialized");
            }
            
            Log.d(TAG, "NotificationHelper initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing NotificationHelper", e);
            throw new RuntimeException("Failed to initialize NotificationHelper: " + e.getMessage(), e);
        }
    }/**
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
                            Log.d(TAG, "Received FCM token: " + token.substring(0, Math.min(token.length(), 10)) + "...");
                            
                            // Store token in Firestore if user is logged in
                            String userId = FirebaseManager.getInstance().getAuth().getUid();
                            if (userId != null) {
                                storeTokenForUser(userId, token);
                            } else {
                                // Store token locally for later registration when user logs in
                                storeTokenLocally(token);
                            }
                            callback.onSuccess(token);
                        } else {
                            Log.e(TAG, "Failed to get FCM token", task.getException());
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
        if (userId == null || token == null || token.isEmpty()) {
            Log.e(TAG, "Invalid userId or token");
            return;
        }
        
        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("token", token);
        tokenData.put("userId", userId);
        tokenData.put("timestamp", System.currentTimeMillis());
        tokenData.put("deviceInfo", getDeviceInfo());
        tokenData.put("appVersion", getAppVersion());
        tokenData.put("isHighPriority", true); // Mark as high priority for immediate delivery

        // Update token in fcm_tokens collection with retry if it fails
        storeTokenWithRetry(userId, token, tokenData, 0);
    }
    
    /**
     * Store token with retry mechanism
     * @param userId User ID
     * @param token FCM token
     * @param tokenData Token data to store
     * @param retryCount Current retry count
     */
    private void storeTokenWithRetry(String userId, String token, Map<String, Object> tokenData, int retryCount) {
        final int MAX_RETRIES = 3;
        
        db.collection(TOKENS_COLLECTION)
                .document(userId)
                .set(tokenData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Token stored successfully in tokens collection");
                    
                    // Also update the token in the user document for quick reference
                    db.collection("users")
                            .document(userId)
                            .update("fcmToken", token)
                            .addOnSuccessListener(unused -> Log.d(TAG, "Token updated in user document"))
                            .addOnFailureListener(e -> Log.e(TAG, "Error updating token in user document", e));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error storing token", e);
                    
                    // Try again if we haven't exceeded the maximum number of retries
                    if (retryCount < MAX_RETRIES) {
                        Log.d(TAG, "Retrying token storage (" + (retryCount + 1) + "/" + MAX_RETRIES + ")");
                        // Wait a bit before retrying (exponential backoff)
                        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                        handler.postDelayed(() -> storeTokenWithRetry(userId, token, tokenData, retryCount + 1), 
                                (long) (1000 * Math.pow(2, retryCount))); // Exponential backoff: 1s, 2s, 4s
                    } else {
                        // Store locally as backup
                        storeTokenLocally(token);
                    }
                });
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
    }

    /**
     * Get app version information
     * @return App version string
     */
    private String getAppVersion() {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            Log.e(TAG, "Error getting app version", e);
            return "unknown";
        }
    }

    /**
     * Store token locally for later use when user logs in
     * @param token FCM token
     */
    private void storeTokenLocally(String token) {
        try {
            context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("pending_fcm_token", token)
                .putLong("token_timestamp", System.currentTimeMillis())
                .apply();
            Log.d(TAG, "Token stored locally for later use when user logs in");
        } catch (Exception e) {
            Log.e(TAG, "Error storing token locally", e);
        }
    }

    /**
     * Process any pending tokens for the newly logged in user
     * @param userId User ID
     */
    public void processPendingToken(String userId) {
        try {
            if (userId == null) {
                Log.e(TAG, "Cannot process pending token: User ID is null");
                return;
            }

            android.content.SharedPreferences prefs = context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE);
            String pendingToken = prefs.getString("pending_fcm_token", null);
            long timestamp = prefs.getLong("token_timestamp", 0);

            // Check if we have a recent pending token (less than 30 days old)
            if (pendingToken != null && System.currentTimeMillis() - timestamp < 30 * 24 * 60 * 60 * 1000) {
                Log.d(TAG, "Processing pending FCM token for user: " + userId);
                storeTokenForUser(userId, pendingToken);
                
                // Clear the pending token after processing
                prefs.edit().remove("pending_fcm_token").apply();
            } else {
                // No valid pending token, request a new one
                getDeviceToken(new FirebaseAuthRepository.FirebaseCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        Log.d(TAG, "New token generated and stored for user: " + userId);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Error getting new token for user: " + errorMessage);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing pending token", e);
        }
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
