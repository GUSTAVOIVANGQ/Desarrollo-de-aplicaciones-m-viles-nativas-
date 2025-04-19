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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
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
        Log.d(TAG, "Fetching user with ID: " + userId);
        
        // Use authenticated service for secured endpoints
        authenticatedApiService.getUserById(userId).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<User>> call, @NonNull Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    User user = response.body().getData();
                    Log.d(TAG, "User fetched successfully: " + user.getId());
                    apiCallback.onSuccess(user);
                } else {
                    String errorMsg;
                    try {
                        errorMsg = response.errorBody() != null ? 
                            response.errorBody().string() : "Failed to fetch user";
                    } catch (Exception e) {
                        errorMsg = "Error processing response: " + e.getMessage();
                    }
                    Log.e(TAG, "Error fetching user: " + errorMsg);
                    apiCallback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error when fetching user: " + t.getMessage());
                apiCallback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void uploadProfileImage(Long userId, File imageFile, ApiCallback<User> apiCallback) {
        if (userId == null || imageFile == null) {
            apiCallback.onError("Invalid parameters: userId or imageFile is null");
            return;
        }
        
        if (!imageFile.exists()) {
            apiCallback.onError("Image file does not exist");
            return;
        }
        
        Log.d(TAG, "Uploading profile image for user with ID: " + userId);
        
        try {
            // Create request body for file
            RequestBody requestFile = RequestBody.create(
                    MediaType.parse("image/*"),
                    imageFile
            );
            
            // MultipartBody.Part is used to send the file in the request
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                    "image", 
                    imageFile.getName(), 
                    requestFile
            );
            
            // Make API call
            authenticatedApiService.uploadProfileImage(userId, imagePart).enqueue(new Callback<ApiResponse<User>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<User>> call, @NonNull Response<ApiResponse<User>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        User updatedUser = response.body().getData();
                        Log.d(TAG, "Profile image uploaded successfully for user: " + updatedUser.getId());
                        
                        // Update session with new user data if it's the current user
                        User sessionUser = sessionManager.getUser();
                        if (sessionUser != null && sessionUser.getId().equals(updatedUser.getId())) {
                            sessionManager.createLoginSession(
                                    sessionManager.getAuthToken(),
                                    updatedUser,
                                    sessionManager.getUserRole()
                            );
                        }
                        
                        apiCallback.onSuccess(updatedUser);
                    } else {
                        String errorMsg;
                        try {
                            errorMsg = response.errorBody() != null ? 
                                response.errorBody().string() : "Failed to upload profile image";
                        } catch (Exception e) {
                            errorMsg = "Error processing response: " + e.getMessage();
                        }
                        Log.e(TAG, "Error uploading profile image: " + errorMsg);
                        apiCallback.onError(errorMsg);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                    Log.e(TAG, "Network error when uploading profile image: " + t.getMessage());
                    apiCallback.onError("Network error: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception during profile image upload: " + e.getMessage());
            apiCallback.onError("Error preparing image for upload: " + e.getMessage());
        }
    }

    public void updateUser(User currentUser, ApiCallback<User> apiCallback) {
        if (currentUser == null || currentUser.getId() == null) {
            apiCallback.onError("Invalid user data: user or user ID is null");
            return;
        }
        
        Log.d(TAG, "Updating user with ID: " + currentUser.getId());
        
        // Use authenticated service for secured endpoints
        authenticatedApiService.updateUser(currentUser.getId(), currentUser).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<User>> call, @NonNull Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    User updatedUser = response.body().getData();
                    Log.d(TAG, "User updated successfully: " + updatedUser.getId());
                    
                    // Update the session with new user data
                    User sessionUser = sessionManager.getUser();
                    if (sessionUser != null && sessionUser.getId().equals(updatedUser.getId())) {
                        sessionManager.createLoginSession(
                                sessionManager.getAuthToken(),
                                updatedUser,
                                sessionManager.getUserRole()
                        );
                    }
                    
                    apiCallback.onSuccess(updatedUser);
                } else {
                    String errorMsg;
                    try {
                        errorMsg = response.errorBody() != null ? 
                            response.errorBody().string() : "Failed to update user";
                    } catch (Exception e) {
                        errorMsg = "Error processing response: " + e.getMessage();
                    }
                    Log.e(TAG, "Error updating user: " + errorMsg);
                    apiCallback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error when updating user: " + t.getMessage());
                apiCallback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getAllUsers(ApiCallback<List<User>> apiCallback) {
        Log.d(TAG, "Fetching all users");

        // Usar Retrofit directamente en lugar de HttpUrlConnection
        authenticatedApiService.getAllUsers().enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<User>>> call, @NonNull Response<ApiResponse<List<User>>> response) {
                // Log para depuración
                Log.d(TAG, "Response received: isSuccessful=" + response.isSuccessful() 
                      + ", code=" + response.code()
                      + ", message=" + response.message()
                      + ", url=" + call.request().url());
                
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<List<User>> apiResponse = response.body();
                        
                        // Verificar el token en la petición actual
                        String requestToken = call.request().header("Authorization");
                        Log.d(TAG, "Auth token used: " + (requestToken != null ? requestToken : "No token"));
                        
                        // Verificar que la respuesta contenga datos
                        if (apiResponse != null && apiResponse.getData() != null) {
                            List<User> users = apiResponse.getData();
                            Log.d(TAG, "Users fetched successfully: " + users.size() + " users");
                            apiCallback.onSuccess(users);
                        } else {
                            Log.e(TAG, "Response body or data is null");
                            apiCallback.onError("No data received from server");
                        }
                    } else {
                        // Si no es exitoso, intentar recuperar el cuerpo del error
                        String errorBody = "";
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                            Log.e(TAG, "Error response body: " + errorBody);
                        }
                        
                        // Verificar si es un problema de token
                        if (response.code() == 401 || response.code() == 403 || errorBody.contains("login")) {
                            Log.e(TAG, "Authentication failed. Token may be invalid or expired.");
                            
                            // Intento de actualizar token - depende de tu lógica de autenticación
                            // Por ahora, notificamos el error de autenticación
                            apiCallback.onError("Authentication failed. Please login again.");
                        } else {
                            apiCallback.onError("Server error: " + response.code() + " " + response.message());
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing response: " + e.getMessage(), e);
                    apiCallback.onError("Error processing response: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<User>>> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error when fetching users: " + t.getMessage());
                apiCallback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    // Helper method to get UI thread handler
    private android.os.Handler getHandler() {
        try {
            return new android.os.Handler(android.os.Looper.getMainLooper());
        } catch (Exception e) {
            Log.e(TAG, "Error creating handler: " + e.getMessage());
            return null;
        }
    }

    public void deleteUser(Long id, ApiCallback<Void> apiCallback) {
        if (id == null) {
            apiCallback.onError("Invalid user ID: ID is null");
            return;
        }
        
        Log.d(TAG, "Deleting user with ID: " + id);
        
        // Use authenticated service for secured endpoints
        authenticatedApiService.deleteUser(id).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "User deleted successfully: " + id);
                    apiCallback.onSuccess(null);
                } else {
                    String errorMsg;
                    try {
                        errorMsg = response.errorBody() != null ? 
                            response.errorBody().string() : "Failed to delete user";
                    } catch (Exception e) {
                        errorMsg = "Error processing response: " + e.getMessage();
                    }
                    Log.e(TAG, "Error deleting user: " + errorMsg);
                    apiCallback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error when deleting user: " + t.getMessage());
                apiCallback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void logout() {

    }

    /**
     * Get the profile image of a user
     * @param userId User ID
     * @param apiCallback Callback to handle the response
     */
    public void getProfileImage(Long userId, ApiCallback<byte[]> apiCallback) {
        if (userId == null) {
            apiCallback.onError("Invalid user ID: ID is null");
            return;
        }
        
        Log.d(TAG, "Fetching profile image for user with ID: " + userId);
        
        // Use authenticated service for secured endpoints
        authenticatedApiService.getProfileImage(userId).enqueue(new Callback<ApiResponse<byte[]>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<byte[]>> call, @NonNull Response<ApiResponse<byte[]>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    byte[] imageData = response.body().getData();
                    Log.d(TAG, "Profile image fetched successfully for user ID: " + userId + 
                          ", size: " + imageData.length + " bytes");
                    apiCallback.onSuccess(imageData);
                } else {
                    String errorMsg;
                    try {
                        errorMsg = response.errorBody() != null ? 
                            response.errorBody().string() : "Failed to fetch profile image";
                    } catch (Exception e) {
                        errorMsg = "Error processing response: " + e.getMessage();
                    }
                    Log.e(TAG, "Error fetching profile image: " + errorMsg);
                    apiCallback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<byte[]>> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error when fetching profile image: " + t.getMessage());
                apiCallback.onError("Network error: " + t.getMessage());
            }
        });
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