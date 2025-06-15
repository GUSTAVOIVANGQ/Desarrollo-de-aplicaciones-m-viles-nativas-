package com.example.systembooks.models;

import com.example.systembooks.R;
import java.io.Serializable;

/**
 * Model class representing an activity/event in the admin dashboard
 */
public class DashboardActivity implements Serializable {
    
    public static final String TYPE_USER_REGISTERED = "USER_REGISTERED";
    public static final String TYPE_USER_LOGIN = "USER_LOGIN";
    public static final String TYPE_USER_UPDATED = "USER_UPDATED";
    public static final String TYPE_USER_DELETED = "USER_DELETED";
    public static final String TYPE_ROLE_CHANGED = "ROLE_CHANGED";
    public static final String TYPE_NOTIFICATION_SENT = "NOTIFICATION_SENT";
    public static final String TYPE_BOOK_SEARCHED = "BOOK_SEARCHED";
    public static final String TYPE_PROFILE_UPDATED = "PROFILE_UPDATED";
    
    private String id;
    private String type;
    private String userId;
    private String username;
    private String description;
    private String details;
    private long timestamp;
    private String userRole;
    private String additionalData;

    // Default constructor for Firestore
    public DashboardActivity() {
    }

    public DashboardActivity(String type, String userId, String username, String description) {
        this.type = type;
        this.userId = userId;
        this.username = username;
        this.description = description;
        this.timestamp = System.currentTimeMillis();
    }

    public DashboardActivity(String type, String userId, String username, String description, String details) {
        this(type, userId, username, description);
        this.details = details;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(String additionalData) {
        this.additionalData = additionalData;
    }

    /**
     * Get formatted time ago string
     */
    public String getTimeAgo() {
        long diff = System.currentTimeMillis() - timestamp;
        
        if (diff < 60000) {
            return "hace " + (diff / 1000) + " segundos";
        } else if (diff < 3600000) {
            return "hace " + (diff / 60000) + " minutos";
        } else if (diff < 86400000) {
            return "hace " + (diff / 3600000) + " horas";
        } else {
            return "hace " + (diff / 86400000) + " días";
        }
    }    /**
     * Get icon resource ID based on activity type
     */
    public int getIconResource() {
        switch (type) {
            case TYPE_USER_REGISTERED:
                return R.drawable.ic_register;
            case TYPE_USER_LOGIN:
                return R.drawable.ic_login;
            case TYPE_USER_UPDATED:
                return android.R.drawable.ic_menu_edit;
            case TYPE_USER_DELETED:
                return android.R.drawable.ic_menu_delete;
            case TYPE_ROLE_CHANGED:
                return R.drawable.ic_admin;
            case TYPE_NOTIFICATION_SENT:
                return R.drawable.ic_notifications;
            case TYPE_BOOK_SEARCHED:
                return R.drawable.ic_book;
            case TYPE_PROFILE_UPDATED:
                return R.drawable.ic_profile;
            default:
                return android.R.drawable.ic_dialog_info;
        }
    }

    /**
     * Get color resource ID based on activity type
     */
    public int getColorResource() {
        switch (type) {
            case TYPE_USER_REGISTERED:
                return android.R.color.holo_green_dark;
            case TYPE_USER_LOGIN:
                return android.R.color.holo_blue_dark;
            case TYPE_USER_UPDATED:
                return android.R.color.holo_orange_dark;
            case TYPE_USER_DELETED:
                return android.R.color.holo_red_dark;
            case TYPE_ROLE_CHANGED:
                return android.R.color.holo_purple;
            case TYPE_NOTIFICATION_SENT:
                return R.color.colorPrimary;
            case TYPE_BOOK_SEARCHED:
                return android.R.color.holo_blue_light;
            case TYPE_PROFILE_UPDATED:
                return R.color.colorAccent;
            default:
                return android.R.color.darker_gray;
        }
    }
}
