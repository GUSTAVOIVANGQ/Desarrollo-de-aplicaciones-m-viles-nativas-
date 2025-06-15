package com.example.systembooks;

import android.app.Application;

import com.example.systembooks.firebase.FirebaseManager;

public class SystemBooksApplication extends Application {    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase with improved FCM handling
        FirebaseManager.init(this);
        
        // Optimize notifications for immediate delivery
        FirebaseManager.getInstance().optimizeNotificationDelivery();
    }
}
