package com.example.systembooks.api;

import com.example.systembooks.model.ApiResponse;
import com.example.systembooks.model.LoginRequest;
import com.example.systembooks.model.LoginResponse;
import com.example.systembooks.model.RegisterRequest;
import com.example.systembooks.model.User;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {
    // Autenticación
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);
    
    @POST("api/auth/register")
    Call<ApiResponse<User>> register(@Body RegisterRequest registerRequest);
    
    // Gestión de usuarios (CRUD)
    @GET("api/users")
    Call<ApiResponse<List<User>>> getAllUsers();
    
    @GET("api/users/{id}")
    Call<ApiResponse<User>> getUserById(@Path("id") Long id);
    
    @PUT("api/users/{id}")
    Call<ApiResponse<User>> updateUser(@Path("id") Long id, @Body User user);
    
    @DELETE("api/users/{id}")
    Call<ApiResponse<Void>> deleteUser(@Path("id") Long id);
    
    // Gestión de imágenes de perfil
    @Multipart
    @POST("api/users/{id}/image")
    Call<ApiResponse<User>> uploadProfileImage(
            @Path("id") Long id,
            @Part MultipartBody.Part image
    );
    
    @GET("api/users/{id}/image")
    Call<ApiResponse<byte[]>> getProfileImage(@Path("id") Long id);
}
