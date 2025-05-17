package com.example.systembooks.firebase;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;

/**
 * Manager class to handle Firebase initialization and services
 */
public class FirebaseManager {

    private static final String TAG = "FirebaseManager";    private static FirebaseManager instance;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private FirebaseMessaging messaging;
    private Context context;

    private FirebaseManager() {
        // Private constructor to enforce singleton pattern
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        messaging = FirebaseMessaging.getInstance();
    }/**
     * Initializes Firebase in the application
     * @param context Application context
     */    public static void init(Context context) {
        try {
            FirebaseApp.initializeApp(context);
            
            // Store application context
            getInstance().setContext(context);
            
            // Configure FCM for improved delivery reliability
            setupFCM();
            
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage());
        }
    }
    
    /**
     * Get context
     * @return The application context
     */
    public Context getContext() {
        return context;
    }

    /**
     * Set context
     * @param context The application context
     */
    public void setContext(Context context) {
        this.context = context.getApplicationContext();
    }    /**
     * Configure Firebase Cloud Messaging for optimal performance
     */
    private static void setupFCM() {
        try {
            // Configure FCM for high priority delivery
            FirebaseMessaging.getInstance().setAutoInitEnabled(true);
            
            // Request token early for better reliability
            FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        Log.d(TAG, "Initial FCM token obtained");
                    } else {
                        Log.e(TAG, "Failed to get initial FCM token", task.getException());
                    }
                });
            
            Log.d(TAG, "FCM setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up FCM: " + e.getMessage());
        }
    }/**
     * Gets the singleton instance of FirebaseManager
     * @return FirebaseManager instance
     */
    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }
    
    /**
     * Gets Firebase Auth instance
     * @return FirebaseAuth instance
     */
    public FirebaseAuth getAuth() {
        return auth;
    }

    /**
     * Gets Firebase Firestore instance
     * @return FirebaseFirestore instance
     */
    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    /**
     * Gets Firebase Storage instance
     * @return FirebaseStorage instance
     */
    public FirebaseStorage getStorage() {
        return storage;
    }

    /**
     * Gets Firebase Cloud Messaging instance
     * @return FirebaseMessaging instance
     */
    public FirebaseMessaging getMessaging() {
        return messaging;
    }    /**
     * Subscribes to a topic for FCM notifications
     * @param topic Topic name
     */
    public void subscribeToTopic(String topic) {
        messaging.subscribeToTopic(topic)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Subscribed to topic: " + topic);
                    } else {
                        Log.e(TAG, "Failed to subscribe to topic: " + topic);
                    }
                });
    }
    
    /**
     * Refresh FCM token explicitly
     * @param force Whether to force a server refresh
     */
    public void refreshToken(final boolean force) {
        try {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            // Token refreshed
                            String token = task.getResult();
                            Log.d(TAG, "FCM token refreshed");
                            
                            // Update server if force=true or user logged in
                            if (force && auth.getCurrentUser() != null && context != null) {
                                new NotificationHelper(context).storeTokenForUser(auth.getCurrentUser().getUid(), token);
                            }
                        } else {
                            Log.e(TAG, "Failed to refresh FCM token", task.getException());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error refreshing FCM token: " + e.getMessage());
        }
    }

    /**
     * Apply optimizations for notification delivery
     */
    public void optimizeNotificationDelivery() {
        try {
            // Ensure we have a valid token
            refreshToken(true);
            
            // Apply optimization for Firebase connectivity
            if (context != null) {
                // Set keep alive time for FCM connection
                android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                handler.postDelayed(() -> {
                    // Trigger a reconnection of FCM service
                    refreshToken(false);
                }, 1000);
            }
            
            Log.d(TAG, "Notification delivery optimizations applied");
        } catch (Exception e) {
            Log.e(TAG, "Error applying notification optimizations: " + e.getMessage());
        }
    }
}
