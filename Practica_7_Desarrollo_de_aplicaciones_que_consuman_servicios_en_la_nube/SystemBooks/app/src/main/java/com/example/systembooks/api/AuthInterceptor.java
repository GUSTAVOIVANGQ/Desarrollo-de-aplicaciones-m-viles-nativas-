package com.example.systembooks.api;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private String authToken;
    
    public AuthInterceptor(String token) {
        this.authToken = token;
    }
    
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        
        // Si no hay token, procede sin modificar la solicitud
        if (authToken == null || authToken.isEmpty()) {
            return chain.proceed(originalRequest);
        }
        
        // Añadir el token en el encabezado de la solicitud
        Request newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + authToken)
                .build();
        
        return chain.proceed(newRequest);
    }
    
    // Permite actualizar el token si cambia durante la sesión
    public void setAuthToken(String token) {
        this.authToken = token;
    }
}
