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

    private static final String TAG = "FirebaseManager";
    private static FirebaseManager instance;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private FirebaseMessaging messaging;

    private FirebaseManager() {
        // Private constructor to enforce singleton pattern
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        messaging = FirebaseMessaging.getInstance();
    }

    /**
     * Initializes Firebase in the application
     * @param context Application context
     */
    public static void init(Context context) {
        try {
            FirebaseApp.initializeApp(context);
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage());
        }
    }

    /**
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
    }

    /**
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
}
