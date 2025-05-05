package com.example.systembooks.models;

import java.io.Serializable;

/**
 * User model class for admin views
 */
public class User implements Serializable {
    private long id;
    private String username;
    private String email;
    private String role;

    public User(long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public User(long id, String username, String email, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
}