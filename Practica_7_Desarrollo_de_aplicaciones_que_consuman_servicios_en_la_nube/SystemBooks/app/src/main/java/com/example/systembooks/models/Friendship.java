package com.example.systembooks.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class for friendships
 */
public class Friendship {
    @DocumentId
    private String id;
    private String user1Id;
    private String user1Name;
    private String user1Email;
    private String user1PhotoUrl;
    private String user2Id;
    private String user2Name;
    private String user2Email;
    private String user2PhotoUrl;
    private Long createdAt;

    // Constructor vacío requerido por Firestore
    public Friendship() {
    }

    public Friendship(String user1Id, String user1Name, String user1Email, String user1PhotoUrl,
                     String user2Id, String user2Name, String user2Email, String user2PhotoUrl) {
        this.user1Id = user1Id;
        this.user1Name = user1Name;
        this.user1Email = user1Email;
        this.user1PhotoUrl = user1PhotoUrl;
        this.user2Id = user2Id;
        this.user2Name = user2Name;
        this.user2Email = user2Email;
        this.user2PhotoUrl = user2PhotoUrl;
        this.createdAt = System.currentTimeMillis();
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("user1Id", user1Id);
        result.put("user1Name", user1Name);
        result.put("user1Email", user1Email);
        result.put("user1PhotoUrl", user1PhotoUrl);
        result.put("user2Id", user2Id);
        result.put("user2Name", user2Name);
        result.put("user2Email", user2Email);
        result.put("user2PhotoUrl", user2PhotoUrl);
        result.put("createdAt", createdAt);
        return result;
    }

    /**
     * Get the friend's information based on the current user ID
     * @param currentUserId The current user's ID
     * @return Friend's data as a map
     */
    @Exclude
    public Map<String, String> getFriendInfo(String currentUserId) {
        Map<String, String> friendInfo = new HashMap<>();
        
        if (user1Id.equals(currentUserId)) {
            // Current user is user1, so friend is user2
            friendInfo.put("id", user2Id);
            friendInfo.put("name", user2Name);
            friendInfo.put("email", user2Email);
            friendInfo.put("photoUrl", user2PhotoUrl);
        } else {
            // Current user is user2, so friend is user1
            friendInfo.put("id", user1Id);
            friendInfo.put("name", user1Name);
            friendInfo.put("email", user1Email);
            friendInfo.put("photoUrl", user1PhotoUrl);
        }
        
        return friendInfo;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(String user1Id) {
        this.user1Id = user1Id;
    }

    public String getUser1Name() {
        return user1Name;
    }

    public void setUser1Name(String user1Name) {
        this.user1Name = user1Name;
    }

    public String getUser1Email() {
        return user1Email;
    }

    public void setUser1Email(String user1Email) {
        this.user1Email = user1Email;
    }

    public String getUser1PhotoUrl() {
        return user1PhotoUrl;
    }

    public void setUser1PhotoUrl(String user1PhotoUrl) {
        this.user1PhotoUrl = user1PhotoUrl;
    }

    public String getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(String user2Id) {
        this.user2Id = user2Id;
    }

    public String getUser2Name() {
        return user2Name;
    }

    public void setUser2Name(String user2Name) {
        this.user2Name = user2Name;
    }

    public String getUser2Email() {
        return user2Email;
    }

    public void setUser2Email(String user2Email) {
        this.user2Email = user2Email;
    }

    public String getUser2PhotoUrl() {
        return user2PhotoUrl;
    }

    public void setUser2PhotoUrl(String user2PhotoUrl) {
        this.user2PhotoUrl = user2PhotoUrl;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}
