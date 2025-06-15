package com.example.systembooks.firebase;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for handling Firebase User CRUD operations
 */
public class FirebaseUserRepository {
    private static final String TAG = "FirebaseUserRepository";
    private static final String USERS_COLLECTION = "users";

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final Context context;

    public FirebaseUserRepository(Context context) {
        this.context = context;
        this.db = FirebaseManager.getInstance().getFirestore();
        this.auth = FirebaseManager.getInstance().getAuth();
    }

    /**
     * Get all users from Firebase Firestore
     * @param callback Callback to handle the result
     */
    public void getAllUsers(FirebaseCallback<List<FirebaseUser>> callback) {
        db.collection(USERS_COLLECTION)
                .orderBy("username", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<FirebaseUser> users = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                FirebaseUser user = document.toObject(FirebaseUser.class);
                                users.add(user);
                            }
                            Log.d(TAG, "Successfully loaded " + users.size() + " users from Firebase");
                            callback.onSuccess(users);
                        } else {
                            Log.e(TAG, "Error getting users from Firebase", task.getException());
                            callback.onError("Error loading users: " + 
                                    (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        }
                    }
                });
    }

    /**
     * Get a specific user by UID
     * @param uid User ID
     * @param callback Callback to handle the result
     */
    public void getUserById(String uid, FirebaseCallback<FirebaseUser> callback) {
        db.collection(USERS_COLLECTION).document(uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                FirebaseUser user = document.toObject(FirebaseUser.class);
                                callback.onSuccess(user);
                            } else {
                                callback.onError("User not found");
                            }
                        } else {
                            Log.e(TAG, "Error getting user", task.getException());
                            callback.onError("Error getting user: " + 
                                    (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        }
                    }
                });
    }

    /**
     * Update a user in Firestore
     * @param user The user to update
     * @param callback Callback to handle the result
     */
    public void updateUser(FirebaseUser user, FirebaseCallback<Void> callback) {
        if (user.getUid() == null || user.getUid().isEmpty()) {
            callback.onError("User ID is required for update");
            return;
        }

        db.collection(USERS_COLLECTION).document(user.getUid())
                .set(user.toMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User updated successfully");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user", e);
                    callback.onError("Error updating user: " + e.getMessage());
                });
    }

    /**
     * Delete a user from Firestore
     * Note: This only deletes the user document from Firestore, not from Firebase Auth
     * @param uid User ID to delete
     * @param callback Callback to handle the result
     */
    public void deleteUser(String uid, FirebaseCallback<Void> callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onError("User ID is required for deletion");
            return;
        }

        db.collection(USERS_COLLECTION).document(uid)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User deleted successfully from Firestore");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting user", e);
                    callback.onError("Error deleting user: " + e.getMessage());
                });
    }

    /**
     * Update user role
     * @param uid User ID
     * @param newRole New role for the user
     * @param callback Callback to handle the result
     */
    public void updateUserRole(String uid, String newRole, FirebaseCallback<Void> callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onError("User ID is required");
            return;
        }

        if (!FirebaseUser.ROLE_ADMIN.equals(newRole) && !FirebaseUser.ROLE_USER.equals(newRole)) {
            callback.onError("Invalid role. Must be ROLE_ADMIN or ROLE_USER");
            return;
        }

        db.collection(USERS_COLLECTION).document(uid)
                .update("role", newRole)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User role updated successfully");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user role", e);
                    callback.onError("Error updating user role: " + e.getMessage());
                });
    }

    /**
     * Update user profile photo URL
     * @param uid User ID
     * @param photoUrl New photo URL
     * @param callback Callback to handle the result
     */
    public void updateUserPhoto(String uid, String photoUrl, FirebaseCallback<Void> callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onError("User ID is required");
            return;
        }

        db.collection(USERS_COLLECTION).document(uid)
                .update("photoUrl", photoUrl)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User photo updated successfully");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user photo", e);
                    callback.onError("Error updating user photo: " + e.getMessage());
                });
    }

    /**
     * Search users by username or email
     * @param searchQuery Search query
     * @param callback Callback to handle the result
     */
    public void searchUsers(String searchQuery, FirebaseCallback<List<FirebaseUser>> callback) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            getAllUsers(callback);
            return;
        }

        String searchLower = searchQuery.toLowerCase();
        
        db.collection(USERS_COLLECTION)
                .orderBy("username")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<FirebaseUser> users = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                FirebaseUser user = document.toObject(FirebaseUser.class);
                                
                                // Filter by username or email
                                if ((user.getUsername() != null && user.getUsername().toLowerCase().contains(searchLower)) ||
                                    (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchLower))) {
                                    users.add(user);
                                }
                            }
                            Log.d(TAG, "Search completed. Found " + users.size() + " users");
                            callback.onSuccess(users);
                        } else {
                            Log.e(TAG, "Error searching users", task.getException());
                            callback.onError("Error searching users: " + 
                                    (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        }
                    }
                });
    }

    /**
     * Get users by role
     * @param role User role (ROLE_ADMIN or ROLE_USER)
     * @param callback Callback to handle the result
     */
    public void getUsersByRole(String role, FirebaseCallback<List<FirebaseUser>> callback) {
        db.collection(USERS_COLLECTION)
                .whereEqualTo("role", role)
                .orderBy("username", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<FirebaseUser> users = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                FirebaseUser user = document.toObject(FirebaseUser.class);
                                users.add(user);
                            }
                            Log.d(TAG, "Successfully loaded " + users.size() + " users with role: " + role);
                            callback.onSuccess(users);
                        } else {
                            Log.e(TAG, "Error getting users by role", task.getException());
                            callback.onError("Error loading users by role: " + 
                                    (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        }
                    }
                });
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
