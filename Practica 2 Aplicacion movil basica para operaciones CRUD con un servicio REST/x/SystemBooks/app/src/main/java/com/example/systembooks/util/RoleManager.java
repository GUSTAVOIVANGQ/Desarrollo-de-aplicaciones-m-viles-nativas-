package com.example.systembooks.util;

import android.content.Context;

public class RoleManager {
    
    // Constantes para los diferentes roles
    public static final String ROLE_GUEST = "guest";
    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";
    
    private SessionManager sessionManager;
    
    public RoleManager(Context context) {
        this.sessionManager = new SessionManager(context);
    }
    
    /**
     * Verifica si el usuario actual tiene un rol específico
     * @param role Rol a verificar
     * @return true si el usuario tiene el rol, false en caso contrario
     */
    public boolean hasRole(String role) {
        if (!sessionManager.isLoggedIn()) {
            return ROLE_GUEST.equals(role);
        }
        
        String userRole = sessionManager.getUserRole();
        if (userRole == null) {
            return false;
        }
        
        // Un administrador tiene acceso a todas las funciones
        if (userRole.equals(ROLE_ADMIN)) {
            return true;
        }
        
        // Si no es admin, se compara con el rol específico
        return userRole.equals(role);
    }
    
    /**
     * Verifica si el usuario está autorizado para acceder a funciones admin
     * @return true si es administrador, false en caso contrario
     */
    public boolean isAdmin() {
        return hasRole(ROLE_ADMIN);
    }
    
    /**
     * Verifica si el usuario está autorizado para acceder a funciones de usuario
     * @return true si es usuario o administrador, false en caso contrario
     */
    public boolean isUser() {
        return hasRole(ROLE_USER) || hasRole(ROLE_ADMIN);
    }
    
    /**
     * Verifica si el usuario es invitado (no ha iniciado sesión)
     * @return true si es invitado, false en caso contrario
     */
    public boolean isGuest() {
        return !sessionManager.isLoggedIn();
    }
    
    /**
     * Obtiene el rol actual del usuario
     * @return Rol del usuario o "guest" si no está logueado
     */
    public String getCurrentRole() {
        if (!sessionManager.isLoggedIn()) {
            return ROLE_GUEST;
        }
        
        String role = sessionManager.getUserRole();
        return role != null ? role : ROLE_GUEST;
    }
}
