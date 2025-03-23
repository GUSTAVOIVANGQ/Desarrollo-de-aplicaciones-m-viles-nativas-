package com.example.systembooks.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.systembooks.api.ApiClient;
import com.example.systembooks.api.ApiService;
import com.example.systembooks.model.ApiResponse;
import com.example.systembooks.model.LoginRequest;
import com.example.systembooks.model.LoginResponse;
import com.example.systembooks.model.RegisterRequest;
import com.example.systembooks.model.User;
import com.example.systembooks.util.SessionManager;

import java.io.File;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiRepository {
    private static final String TAG = "ApiRepository";
    
    private final Context context;
    private final ApiService apiService;
    private final ApiService authenticatedApiService;
    private final SessionManager sessionManager;
    
    public ApiRepository(Context context) {
        this.context = context;
        this.apiService = ApiClient.getClient().create(ApiService.class);
        this.authenticatedApiService = ApiClient.getAuthenticatedClient(context).create(ApiService.class);
        this.sessionManager = new SessionManager(context);
    }

    public void getUserById(Long userId, ApiCallback<User> apiCallback) {
    }

    public void uploadProfileImage(Long userId, File imageFile, ApiCallback<User> apiCallback) {
    }

    public void updateUser(User currentUser, ApiCallback<User> apiCallback) {
    }

    public void getAllUsers(ApiCallback<List<User>> apiCallback) {
    }

    public void deleteUser(Long id, ApiCallback<Void> apiCallback) {
    }

    public void logout() {

    }

    /**
     * Interface para manejar callbacks de respuesta
     */
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }
    
    /**
     * Login de usuario
     */
    public void login(String email, String password, final ApiCallback<LoginResponse> callback) {
        // Log para depuración
        Log.d(TAG, "Intentando login con email: " + email);
        
        // No encriptamos la contraseña - el servidor se encargará de la comparación con BCrypt
        LoginRequest loginRequest = new LoginRequest(email, password);
        
        // Crear solicitud
        apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    Log.d(TAG, "Login exitoso, token: " + loginResponse.getToken());
                    
                    // Convertir LoginResponse.User a User para la sesión
                    User user = convertResponseUserToUser(loginResponse.getUser());
                    
                    // Guardar token y datos de sesión
                    sessionManager.createLoginSession(
                            loginResponse.getToken(),
                            user,
                            loginResponse.getRole()
                    );
                    callback.onSuccess(loginResponse);
                } else {
                    String errorMsg;
                    try {
                        errorMsg = response.errorBody() != null ? 
                            response.errorBody().string() : "Error desconocido";
                    } catch (Exception e) {
                        errorMsg = "Error al procesar la respuesta: " + e.getMessage();
                    }
                    Log.e(TAG, "Error en login: " + errorMsg);
                    callback.onError("Login failed: " + errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error en login: " + t.getMessage());
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Método para convertir LoginResponse.User a User
     */
    private User convertResponseUserToUser(LoginResponse.User responseUser) {
        if (responseUser == null) {
            return null;
        }
        
        User user = new User();
        user.setId(responseUser.getId());
        user.setName(responseUser.getNombre());
        user.setEmail(responseUser.getEmail());
        return user;
    }
    
    /**
     * Registro de usuario
     */
    public void register(String name, String email, String password, final ApiCallback<User> callback) {
        // Usar la contraseña en texto plano - el servidor se encargará de encriptarla con BCrypt
        RegisterRequest registerRequest = new RegisterRequest(name, email, password);
        
        // Enviar solicitud al servidor
        apiService.register(registerRequest).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<User>> call, @NonNull Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    User user = response.body().getData();
                    callback.onSuccess(user);
                } else {
                    callback.onError("Registration failed: " + (response.errorBody() != null ? 
                            response.errorBody().toString() : "Unknown error"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}