package com.example.systembooks.utils;

import android.content.Context;
import android.util.Log;

import com.example.systembooks.firebase.FirebaseAuthRepository;
import com.example.systembooks.firebase.FirebaseManager;
import com.example.systembooks.firebase.NotificationHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for testing notification delivery
 * This is a helper class to diagnose and test notification delivery issues
 */
public class NotificationTestUtil {
    private static final String TAG = "NotificationTestUtil";
    private final Context context;

    public NotificationTestUtil(Context context) {
        this.context = context;
    }

    /**
     * Run a full diagnostic on the notification system
     * @param callback Callback for the result
     */
    public void runDiagnostic(final DiagnosticCallback callback) {
        Log.d(TAG, "Starting notification system diagnostic");
        
        // Step 1: Force token refresh to get a fresh FCM token
        FirebaseManager.getInstance().refreshToken(true);
        
        // Step 2: Check if the user is logged in
        String userId = FirebaseManager.getInstance().getAuth().getUid();
        if (userId == null) {
            callback.onResult("ERROR: User not logged in. Please log in to test notifications.");
            return;
        }
        
        // Step 3: Force optimizations for notification delivery
        FirebaseManager.getInstance().optimizeNotificationDelivery();
        
        // Step 4: Send a test notification to the current user
        Map<String, String> data = new HashMap<>();
        data.put("testId", String.valueOf(System.currentTimeMillis()));
        data.put("priority", "high");
        
        new NotificationHelper(context).sendNotificationToUser(userId, 
                "Test Notification", 
                "This is a test notification sent at " + new java.util.Date(),
                data,
                new FirebaseAuthRepository.FirebaseCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Log.d(TAG, "Test notification sent successfully");
                        callback.onResult("Test notification sent successfully. " +
                                "You should receive it within a few seconds.\n" +
                                "If not, check the following:\n" +
                                "1. Firebase console for errors\n" +
                                "2. Device notification permissions\n" +
                                "3. Battery optimization settings");
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Error sending test notification: " + errorMessage);
                        callback.onResult("ERROR: Failed to send test notification: " + errorMessage);
                    }
                });
    }
    
    /**
     * Check the device settings that might affect notification delivery
     * @return A string with diagnostic information
     */
    public String checkDeviceSettings() {
        StringBuilder result = new StringBuilder();
        
        // Check if notification channel exists
        boolean hasNotificationPermission = hasNotificationPermission();
        result.append("Notification Permission: ").append(hasNotificationPermission ? "GRANTED" : "DENIED").append("\n");
        
        // Check if battery optimization is disabled for the app
        boolean isBatteryOptimizationDisabled = isBatteryOptimizationDisabled();
        result.append("Battery Optimization: ").append(isBatteryOptimizationDisabled ? "DISABLED" : "ENABLED").append("\n");
        
        // Get the current FCM token status
        String tokenStatus = getTokenStatus();
        result.append("FCM Token Status: ").append(tokenStatus).append("\n");
        
        return result.toString();
    }
    
    /**
     * Check if notification permissions are granted
     * @return true if granted, false otherwise
     */
    private boolean hasNotificationPermission() {
        try {
            android.app.NotificationManager notificationManager = 
                (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                android.app.NotificationChannel channel = notificationManager.getNotificationChannel("SystemBooksChannel");
                return channel != null && 
                       channel.getImportance() != android.app.NotificationManager.IMPORTANCE_NONE;
            } else {
                return androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking notification permission", e);
            return false;
        }
    }
    
    /**
     * Check if battery optimization is disabled for the app
     * @return true if disabled, false otherwise
     */
    private boolean isBatteryOptimizationDisabled() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                android.os.PowerManager powerManager = (android.os.PowerManager) context.getSystemService(Context.POWER_SERVICE);
                return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
            }
            return true; // On older versions, battery optimization isn't an issue
        } catch (Exception e) {
            Log.e(TAG, "Error checking battery optimization", e);
            return false;
        }
    }
    
    /**
     * Get the current FCM token status
     * @return String with token status
     */
    private String getTokenStatus() {
        try {
            // Check if token is stored in shared preferences
            android.content.SharedPreferences prefs = 
                context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE);
            String pendingToken = prefs.getString("pending_fcm_token", null);
            long timestamp = prefs.getLong("token_timestamp", 0);
            
            if (pendingToken != null) {
                return "Pending token found (stored " + getTimeAgo(timestamp) + " ago)";
            }
            
            String userId = FirebaseManager.getInstance().getAuth().getUid();
            if (userId == null) {
                return "No user logged in, token may not be registered properly";
            }
            
            return "Token should be registered for current user";
        } catch (Exception e) {
            Log.e(TAG, "Error getting token status", e);
            return "Error: " + e.getMessage();
        }
    }
    
    /**
     * Convert a timestamp to a human-readable time ago string
     * @param timestamp The timestamp in milliseconds
     * @return A string representing time ago
     */
    private String getTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        
        if (diff < 60 * 1000) {
            return (diff / 1000) + " seconds";
        } else if (diff < 60 * 60 * 1000) {
            return (diff / (60 * 1000)) + " minutes";
        } else if (diff < 24 * 60 * 60 * 1000) {
            return (diff / (60 * 60 * 1000)) + " hours";
        } else {
            return (diff / (24 * 60 * 60 * 1000)) + " days";
        }
    }
    
    /**
     * Callback interface for diagnostic results
     */
    public interface DiagnosticCallback {
        void onResult(String result);
    }
}
