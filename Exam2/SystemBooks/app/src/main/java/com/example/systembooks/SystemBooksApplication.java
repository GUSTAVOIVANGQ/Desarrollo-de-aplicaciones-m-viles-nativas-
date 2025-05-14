package com.example.systembooks;

import android.app.Application;

import com.example.systembooks.firebase.FirebaseManager;

public class SystemBooksApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase
        FirebaseManager.init(this);
    }
}
