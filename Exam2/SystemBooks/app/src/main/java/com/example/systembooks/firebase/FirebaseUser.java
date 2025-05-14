package com.example.systembooks.firebase;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Firebase User model with role information
 */
public class FirebaseUser {
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    @DocumentId
    private String uid;
    private String username;
    private String email;
    private String role;
    private String photoUrl;
    private Long createdAt;

    // Default constructor required for Firestore
    public FirebaseUser() {
    }

    public FirebaseUser(String uid, String username, String email, String role) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.role = role;
        this.createdAt = System.currentTimeMillis();
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("email", email);
        result.put("role", role);
        result.put("createdAt", createdAt != null ? createdAt : System.currentTimeMillis());
        if (photoUrl != null) {
            result.put("photoUrl", photoUrl);
        }
        return result;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    @Exclude
    public boolean isAdmin() {
        return ROLE_ADMIN.equals(role);
    }
}
