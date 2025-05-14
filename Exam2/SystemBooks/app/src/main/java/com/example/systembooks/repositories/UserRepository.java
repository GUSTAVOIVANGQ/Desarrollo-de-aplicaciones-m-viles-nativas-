package com.example.systembooks.repositories;

import android.content.Context;
import android.util.Log;

import com.example.systembooks.models.User;
import com.example.systembooks.repository.ApiRepository;
import com.example.systembooks.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for handling user data operations
 */
public class UserRepository {
    private static final String TAG = "UserRepository";
    private final Context context;
    private final ApiRepository apiRepository;
    private final SessionManager sessionManager;

    public UserRepository(Context context) {
        this.context = context;
        this.apiRepository = new ApiRepository(context);
        this.sessionManager = new SessionManager(context);
    }

    /**
     * Get all users from the system (admin function)
     * @return List of all users
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        
        try {
            // In a real application, this would fetch users from the API
            // For now, we'll just include the current user for demonstration
            long currentUserId = sessionManager.getUserId();
            String currentUsername = sessionManager.getUserName();
            String currentEmail = sessionManager.getUserEmail();
            
            if (currentUserId > 0 && currentUsername != null) {
                User currentUser = new User(currentUserId, currentUsername, currentEmail);
                users.add(currentUser);
            }
            
            // Add some sample users for testing the admin view
            users.add(new User(1, "admin", "admin@example.com"));
            users.add(new User(2, "user1", "user1@example.com"));
            users.add(new User(3, "user2", "user2@example.com"));
            
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving users", e);
        }
        
        return users;
    }
    
    /**
     * Get user by ID
     * @param userId User ID
     * @return User object or null if not found
     */
    public User getUserById(long userId) {
        List<User> users = getAllUsers();
        
        for (User user : users) {
            if (user.getId() == userId) {
                return user;
            }
        }
        
        return null;
    }
}