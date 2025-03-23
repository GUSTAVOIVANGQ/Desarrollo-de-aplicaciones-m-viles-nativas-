package com.example.systembooks.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("user")
    private User user;
    
    @SerializedName("token")
    private String token;
    
    @SerializedName("role")
    private String role;
    
    @SerializedName("message")
    private String message;
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }

    public static class User {
        private Long id;
        private String nombre;
        private String email;

        public Long getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }

        public String getEmail() {
            return email;
        }
    }
}
