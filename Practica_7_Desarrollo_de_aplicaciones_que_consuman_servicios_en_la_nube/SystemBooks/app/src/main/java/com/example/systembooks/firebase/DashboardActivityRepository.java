package com.example.systembooks.firebase;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.systembooks.models.DashboardActivity;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Repository for managing dashboard activities in Firebase Firestore
 */
public class DashboardActivityRepository {
    
    private static final String TAG = "DashboardActivityRepo";
    private static final String COLLECTION_ACTIVITIES = "dashboard_activities";
    private static final int MAX_ACTIVITIES = 100; // Máximo número de actividades a mantener
    private static final int CLEANUP_DAYS = 30; // Limpiar actividades después de 30 días
    
    private final FirebaseFirestore firestore;
    private final Context context;
    private ListenerRegistration activitiesListener;
    private ScheduledExecutorService cleanupExecutor;
    
    public interface ActivityCallback {
        void onSuccess(List<DashboardActivity> activities);
        void onError(String errorMessage);
    }
    
    public interface SaveCallback {
        void onSuccess();
        void onError(String errorMessage);
    }
    
    public DashboardActivityRepository(Context context) {
        this.context = context;
        this.firestore = FirebaseManager.getInstance().getFirestore();
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
        
        // Schedule periodic cleanup
        scheduleCleanup();
    }
    
    /**
     * Save a new activity to Firestore
     */
    public void saveActivity(@NonNull DashboardActivity activity, SaveCallback callback) {
        try {
            CollectionReference activitiesRef = firestore.collection(COLLECTION_ACTIVITIES);
            
            // Generate document ID if not set
            if (activity.getId() == null || activity.getId().isEmpty()) {
                activity.setId(activitiesRef.document().getId());
            }
            
            activitiesRef.document(activity.getId())
                    .set(activity)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Activity saved successfully: " + activity.getDescription());
                        if (callback != null) {
                            callback.onSuccess();
                        }
                        
                        // Check if cleanup is needed
                        performCleanupIfNeeded();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving activity", e);
                        if (callback != null) {
                            callback.onError(e.getMessage());
                        }
                    });
                    
        } catch (Exception e) {
            Log.e(TAG, "Error in saveActivity", e);
            if (callback != null) {
                callback.onError("Error saving activity: " + e.getMessage());
            }
        }
    }
    
    /**
     * Get recent activities with real-time updates
     */
    public void getRecentActivities(int limit, ActivityCallback callback) {
        try {
            Query query = firestore.collection(COLLECTION_ACTIVITIES)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(limit);
                    
            activitiesListener = query.addSnapshotListener((queryDocumentSnapshots, e) -> {
                if (e != null) {
                    Log.e(TAG, "Error listening for activities", e);
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                    return;
                }
                
                if (queryDocumentSnapshots != null) {
                    List<DashboardActivity> activities = new ArrayList<>();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            DashboardActivity activity = document.toObject(DashboardActivity.class);
                            activity.setId(document.getId());
                            activities.add(activity);
                        } catch (Exception ex) {
                            Log.e(TAG, "Error parsing activity document: " + document.getId(), ex);
                        }
                    }
                    
                    Log.d(TAG, "Loaded " + activities.size() + " activities");
                    if (callback != null) {
                        callback.onSuccess(activities);
                    }
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up activities listener", e);
            if (callback != null) {
                callback.onError("Error setting up listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Get activities for a specific user
     */
    public void getUserActivities(String userId, int limit, ActivityCallback callback) {
        try {
            Query query = firestore.collection(COLLECTION_ACTIVITIES)
                    .whereEqualTo("userId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(limit);
                    
            query.get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<DashboardActivity> activities = new ArrayList<>();
                        
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                DashboardActivity activity = document.toObject(DashboardActivity.class);
                                activity.setId(document.getId());
                                activities.add(activity);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing user activity document: " + document.getId(), e);
                            }
                        }
                        
                        if (callback != null) {
                            callback.onSuccess(activities);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting user activities", e);
                        if (callback != null) {
                            callback.onError(e.getMessage());
                        }
                    });
                    
        } catch (Exception e) {
            Log.e(TAG, "Error in getUserActivities", e);
            if (callback != null) {
                callback.onError("Error getting user activities: " + e.getMessage());
            }
        }
    }
    
    /**
     * Get activities by type
     */
    public void getActivitiesByType(String type, int limit, ActivityCallback callback) {
        try {
            Query query = firestore.collection(COLLECTION_ACTIVITIES)
                    .whereEqualTo("type", type)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(limit);
                    
            query.get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<DashboardActivity> activities = new ArrayList<>();
                        
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                DashboardActivity activity = document.toObject(DashboardActivity.class);
                                activity.setId(document.getId());
                                activities.add(activity);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing type activity document: " + document.getId(), e);
                            }
                        }
                        
                        if (callback != null) {
                            callback.onSuccess(activities);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting activities by type", e);
                        if (callback != null) {
                            callback.onError(e.getMessage());
                        }
                    });
                    
        } catch (Exception e) {
            Log.e(TAG, "Error in getActivitiesByType", e);
            if (callback != null) {
                callback.onError("Error getting activities by type: " + e.getMessage());
            }
        }
    }
    
    /**
     * Stop real-time listening
     */
    public void stopListening() {
        if (activitiesListener != null) {
            activitiesListener.remove();
            activitiesListener = null;
        }
    }
    
    /**
     * Clean up old activities
     */
    private void performCleanupIfNeeded() {
        long cutoffTime = System.currentTimeMillis() - (CLEANUP_DAYS * 24 * 60 * 60 * 1000L);
        
        firestore.collection(COLLECTION_ACTIVITIES)
                .whereLessThan("timestamp", cutoffTime)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "Cleaning up " + queryDocumentSnapshots.size() + " old activities");
                        
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            document.getReference().delete();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error during cleanup", e));
    }
    
    /**
     * Schedule periodic cleanup
     */
    private void scheduleCleanup() {
        cleanupExecutor.scheduleAtFixedRate(
                this::performCleanupIfNeeded,
                24, // Initial delay: 24 hours
                24, // Period: every 24 hours
                TimeUnit.HOURS
        );
    }
    
    /**
     * Helper method to track user login
     */
    public void trackUserLogin(String userId, String username, String userRole) {
        DashboardActivity activity = new DashboardActivity(
                DashboardActivity.TYPE_USER_LOGIN,
                userId,
                username,
                username + " ha iniciado sesión"
        );
        activity.setUserRole(userRole);
        saveActivity(activity, null);
    }
    
    /**
     * Helper method to track user registration
     */
    public void trackUserRegistration(String userId, String username, String userRole) {
        DashboardActivity activity = new DashboardActivity(
                DashboardActivity.TYPE_USER_REGISTERED,
                userId,
                username,
                "Nuevo usuario registrado: " + username
        );
        activity.setUserRole(userRole);
        saveActivity(activity, null);
    }
    
    /**
     * Helper method to track user updates
     */
    public void trackUserUpdate(String userId, String username, String details) {
        DashboardActivity activity = new DashboardActivity(
                DashboardActivity.TYPE_USER_UPDATED,
                userId,
                username,
                username + " ha actualizado su perfil",
                details
        );
        saveActivity(activity, null);
    }
    
    /**
     * Helper method to track user deletion
     */
    public void trackUserDeletion(String userId, String username, String adminId) {
        DashboardActivity activity = new DashboardActivity(
                DashboardActivity.TYPE_USER_DELETED,
                adminId,
                "Admin",
                "Usuario eliminado: " + username,
                "ID del usuario eliminado: " + userId
        );
        saveActivity(activity, null);
    }
    
    /**
     * Helper method to track role changes
     */
    public void trackRoleChange(String userId, String username, String oldRole, String newRole, String adminId) {
        DashboardActivity activity = new DashboardActivity(
                DashboardActivity.TYPE_ROLE_CHANGED,
                adminId,
                "Admin",
                "Rol cambiado para " + username + ": " + oldRole + " → " + newRole,
                "Usuario: " + username + " (ID: " + userId + ")"
        );
        saveActivity(activity, null);
    }
    
    /**
     * Helper method to track notifications sent
     */
    public void trackNotificationSent(String adminId, String title, String recipientInfo) {
        DashboardActivity activity = new DashboardActivity(
                DashboardActivity.TYPE_NOTIFICATION_SENT,
                adminId,
                "Admin",
                "Notificación enviada: " + title,
                "Destinatario: " + recipientInfo
        );
        saveActivity(activity, null);
    }
    
    /**
     * Helper method to track book searches
     */
    public void trackBookSearch(String userId, String username, String searchQuery) {
        DashboardActivity activity = new DashboardActivity(
                DashboardActivity.TYPE_BOOK_SEARCHED,
                userId,
                username,
                username + " buscó libros: \"" + searchQuery + "\"",
                "Consulta de búsqueda: " + searchQuery
        );
        saveActivity(activity, null);
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        stopListening();
        if (cleanupExecutor != null && !cleanupExecutor.isShutdown()) {
            cleanupExecutor.shutdown();
        }
    }
}
