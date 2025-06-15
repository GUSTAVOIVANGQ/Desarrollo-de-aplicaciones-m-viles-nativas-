package com.example.systembooks.firebase;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository class for handling Firebase Authentication operations
 */
public class FirebaseAuthRepository {
    private static final String TAG = "FirebaseAuthRepository";
    private static final String USERS_COLLECTION = "users";
    private static final String ADMIN_PASSWORD = "Admin1234!"; // Master password for admin role

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final Context context;
    private final DashboardActivityRepository activityRepository;

    public FirebaseAuthRepository(Context context) {
        this.context = context;
        this.auth = FirebaseManager.getInstance().getAuth();
        this.db = FirebaseManager.getInstance().getFirestore();
        this.activityRepository = new DashboardActivityRepository(context);
    }

    /**
     * Register a new user with Firebase Authentication
     * @param username Username for the new user
     * @param email Email for the new user
     * @param password Password for the new user
     * @param adminPassword Optional admin password to register as admin
     * @param callback Callback to handle the result
     */
    public void registerUser(String username, String email, String password, String adminPassword, FirebaseCallback<com.example.systembooks.firebase.FirebaseUser> callback) {
        // Create user with Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // User created successfully
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            if (firebaseUser != null) {
                                // Determine user role
                                String role = com.example.systembooks.firebase.FirebaseUser.ROLE_USER;
                                if (adminPassword != null && adminPassword.equals(ADMIN_PASSWORD)) {
                                    role = com.example.systembooks.firebase.FirebaseUser.ROLE_ADMIN;
                                }                                // Create user document in Firestore
                                saveUserToFirestore(firebaseUser.getUid(), username, email, role, new FirebaseCallback<com.example.systembooks.firebase.FirebaseUser>() {
                                    @Override
                                    public void onSuccess(com.example.systembooks.firebase.FirebaseUser result) {
                                        // Track user registration activity
                                        activityRepository.trackUserRegistration(result.getUid(), result.getUsername(), result.getRole());
                                        callback.onSuccess(result);
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        callback.onError(errorMessage);
                                    }
                                });
                            } else {
                                callback.onError("Failed to get user after registration");
                            }
                        } else {
                            // Failed to create user
                            callback.onError(task.getException() != null ? 
                                    task.getException().getMessage() : "Registration failed");
                        }
                    }
                });
    }

    /**
     * Login a user with Firebase Authentication
     * @param email User's email
     * @param password User's password
     * @param callback Callback to handle the result
     */
    public void loginUser(String email, String password, FirebaseCallback<com.example.systembooks.firebase.FirebaseUser> callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login successful
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            if (firebaseUser != null) {                                // Get user data from Firestore
                                getUserFromFirestore(firebaseUser.getUid(), new FirebaseCallback<com.example.systembooks.firebase.FirebaseUser>() {
                                    @Override
                                    public void onSuccess(com.example.systembooks.firebase.FirebaseUser result) {
                                        // Update FCM token after successful login
                                        processFcmTokenAfterLogin(firebaseUser.getUid());
                                        
                                        // Track user login activity
                                        activityRepository.trackUserLogin(result.getUid(), result.getUsername(), result.getRole());
                                        
                                        callback.onSuccess(result);
                                    }
                                    
                                    @Override
                                    public void onError(String errorMessage) {
                                        callback.onError(errorMessage);
                                    }
                                });
                            } else {
                                callback.onError("Failed to get user after login");
                            }
                        } else {
                            // Login failed
                            callback.onError(task.getException() != null ? 
                                    task.getException().getMessage() : "Login failed");
                        }
                    }
                });
    }
    
    /**
     * Process FCM token after successful login
     * @param userId User ID
     */
    private void processFcmTokenAfterLogin(String userId) {
        try {
            // Process any pending token saved before login
            new NotificationHelper(context).processPendingToken(userId);
            
            // Also request a fresh token to ensure it's up to date
            FirebaseManager.getInstance().refreshToken(false);
            
            Log.d(TAG, "FCM token processing triggered after login");
        } catch (Exception e) {
            Log.e(TAG, "Error processing FCM token after login", e);
        }
    }

    /**
     * Save user data to Firestore
     * @param uid User ID
     * @param username Username
     * @param email Email
     * @param role User role
     * @param callback Callback to handle the result
     */
    private void saveUserToFirestore(String uid, String username, String email, String role, 
                                    FirebaseCallback<com.example.systembooks.firebase.FirebaseUser> callback) {
        com.example.systembooks.firebase.FirebaseUser user = 
                new com.example.systembooks.firebase.FirebaseUser(uid, username, email, role);
        
        db.collection(USERS_COLLECTION).document(uid)
                .set(user.toMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User document saved successfully");
                    callback.onSuccess(user);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user document", e);
                    callback.onError("Error saving user data: " + e.getMessage());
                });
    }

    /**
     * Get user data from Firestore
     * @param uid User ID
     * @param callback Callback to handle the result
     */
    public void getUserFromFirestore(String uid, FirebaseCallback<com.example.systembooks.firebase.FirebaseUser> callback) {
        db.collection(USERS_COLLECTION).document(uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                com.example.systembooks.firebase.FirebaseUser user = 
                                        document.toObject(com.example.systembooks.firebase.FirebaseUser.class);
                                callback.onSuccess(user);
                            } else {
                                Log.d(TAG, "No user document found");
                                callback.onError("User data not found");
                            }
                        } else {
                            Log.e(TAG, "get user document failed", task.getException());
                            callback.onError("Error getting user data: " + 
                                    (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        }
                    }
                });
    }

    /**
     * Check if a user is currently logged in
     * @return true if a user is logged in, false otherwise
     */
    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    /**
     * Get current logged in user ID
     * @return User ID or null if no user is logged in
     */
    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    /**
     * Sign out the current user
     */
    public void signOut() {
        auth.signOut();
    }

    /**
     * Interface for Firebase callbacks
     * @param <T> Type of result
     */
    public interface FirebaseCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }
}
