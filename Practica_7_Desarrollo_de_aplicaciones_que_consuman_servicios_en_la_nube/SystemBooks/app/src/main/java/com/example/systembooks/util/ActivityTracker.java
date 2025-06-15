package com.example.systembooks.util;

import android.content.Context;

import com.example.systembooks.firebase.DashboardActivityRepository;
import com.example.systembooks.util.SessionManager;

/**
 * Helper class to track user activities for the admin dashboard
 */
public class ActivityTracker {
    
    private static ActivityTracker instance;
    private final DashboardActivityRepository activityRepository;
    private final SessionManager sessionManager;
    
    private ActivityTracker(Context context) {
        this.activityRepository = new DashboardActivityRepository(context);
        this.sessionManager = new SessionManager(context);
    }
    
    public static synchronized ActivityTracker getInstance(Context context) {
        if (instance == null) {
            instance = new ActivityTracker(context.getApplicationContext());
        }
        return instance;
    }
      /**
     * Track book search activity
     */
    public void trackBookSearch(String searchQuery) {
        if (sessionManager.isLoggedIn()) {
            String username = getUsernameFromSession();
            String userId = getUserIdFromSession();
            
            if (username != null && userId != null) {
                activityRepository.trackBookSearch(userId, username, searchQuery);
            }
        }
    }
    
    /**
     * Track profile update activity
     */
    public void trackProfileUpdate(String details) {
        if (sessionManager.isLoggedIn()) {
            String username = getUsernameFromSession();
            String userId = getUserIdFromSession();
            
            if (username != null && userId != null) {
                activityRepository.trackUserUpdate(userId, username, details);
            }
        }
    }
    
    /**
     * Track user deletion (called by admin)
     */
    public void trackUserDeletion(String deletedUserId, String deletedUsername) {
        if (sessionManager.isLoggedIn()) {
            String adminId = getUserIdFromSession();
            
            if (adminId != null) {
                activityRepository.trackUserDeletion(deletedUserId, deletedUsername, adminId);
            }
        }
    }
    
    /**
     * Track role change (called by admin)
     */
    public void trackRoleChange(String targetUserId, String targetUsername, String oldRole, String newRole) {
        if (sessionManager.isLoggedIn()) {
            String adminId = getUserIdFromSession();
            
            if (adminId != null) {
                activityRepository.trackRoleChange(targetUserId, targetUsername, oldRole, newRole, adminId);
            }
        }
    }
    
    /**
     * Track notification sent (called by admin)
     */
    public void trackNotificationSent(String title, String recipientInfo) {
        if (sessionManager.isLoggedIn()) {
            String adminId = getUserIdFromSession();
            
            if (adminId != null) {
                activityRepository.trackNotificationSent(adminId, title, recipientInfo);
            }
        }
    }
    
    /**
     * Helper method to get username from session based on auth provider
     */
    private String getUsernameFromSession() {
        if (sessionManager.isFirebaseAuth()) {
            // For Firebase users
            com.example.systembooks.firebase.FirebaseUser firebaseUser = sessionManager.getFirebaseUser();
            return firebaseUser != null ? firebaseUser.getUsername() : null;
        } else {
            // For API users
            return sessionManager.getUserName();
        }
    }
    
    /**
     * Helper method to get user ID from session based on auth provider
     */
    private String getUserIdFromSession() {
        if (sessionManager.isFirebaseAuth()) {
            // For Firebase users
            com.example.systembooks.firebase.FirebaseUser firebaseUser = sessionManager.getFirebaseUser();
            return firebaseUser != null ? firebaseUser.getUid() : null;
        } else {
            // For API users
            Long userId = sessionManager.getUserId();
            return userId != null && userId != -1 ? String.valueOf(userId) : null;
        }
    }
    
    /**
     * Get the dashboard activity repository for direct access
     */
    public DashboardActivityRepository getActivityRepository() {
        return activityRepository;
    }
    
    /**
     * Clean up resources
     */
    public void cleanup() {
        if (activityRepository != null) {
            activityRepository.cleanup();
        }
    }
}
