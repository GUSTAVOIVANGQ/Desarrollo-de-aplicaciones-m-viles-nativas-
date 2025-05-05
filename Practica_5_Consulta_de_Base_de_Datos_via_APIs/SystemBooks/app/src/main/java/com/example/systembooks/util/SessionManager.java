package com.example.systembooks.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.systembooks.model.User;
import com.google.gson.Gson;

public class SessionManager {
    private static final String TAG = "SessionManager";
    
    // Variables de SharedPreferences
    private static final String PREF_NAME = "SystemBooksPrefs";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_DATA = "user_data";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;
    
    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }
    
    /**
     * Guarda los datos de sesión del usuario
     */
    public void createLoginSession(String token, User user, String role) {
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.putLong(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_NAME, user.getNombre());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_ROLE, role);
        
        // Guardar el objeto User completo en formato JSON
        Gson gson = new Gson();
        String userJson = gson.toJson(user);
        editor.putString(KEY_USER_DATA, userJson);
        
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
        
        Log.d(TAG, "Sesión de usuario creada para: " + user.getNombre());
    }
    
    /**
     * Obtiene el token de autenticación
     */
    public String getAuthToken() {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null);
    }
    
    /**
     * Obtiene el ID del usuario
     */
    public Long getUserId() {
        return sharedPreferences.getLong(KEY_USER_ID, -1);
    }
    
    /**
     * Obtiene el nombre del usuario
     */
    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, null);
    }
    
    /**
     * Obtiene el email del usuario
     */
    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }
    
    /**
     * Obtiene el rol del usuario
     */
    public String getUserRole() {
        return sharedPreferences.getString(KEY_USER_ROLE, null);
    }
    
    /**
     * Obtiene el objeto User completo
     */
    public User getUser() {
        String userJson = sharedPreferences.getString(KEY_USER_DATA, null);
        if (userJson != null) {
            Gson gson = new Gson();
            return gson.fromJson(userJson, User.class);
        }
        return null;
    }
    
    /**
     * Comprueba si el usuario está logueado
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    /**
     * Cierra la sesión del usuario
     */
    public void logout() {
        editor.clear();
        editor.apply();
        Log.d(TAG, "Sesión cerrada");
    }
}
