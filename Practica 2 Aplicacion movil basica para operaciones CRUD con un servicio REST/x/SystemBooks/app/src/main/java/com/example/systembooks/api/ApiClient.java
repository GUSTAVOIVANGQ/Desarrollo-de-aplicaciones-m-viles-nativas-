package com.example.systembooks.api;

import android.content.Context;

import com.example.systembooks.util.SessionManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // Asegúrate de que esta sea la IP correcta de tu servidor
    private static final String BASE_URL = "http://192.168.8.71:8088/"; 
    private static Retrofit retrofit = null;
    private static AuthInterceptor authInterceptor;
    
    // Obtener cliente Retrofit sin autenticación (para login/registro)
    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)  // Aumentar timeout
                    .readTimeout(30, TimeUnit.SECONDS)     // Aumentar timeout
                    .writeTimeout(30, TimeUnit.SECONDS)    // Aumentar timeout
                    .build();
            
            Gson gson = new GsonBuilder()
                    .setLenient() // Para manejo más flexible de JSON
                    .create();
            
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
        }
        return retrofit;
    }
    
    // Obtener cliente Retrofit con autenticación
    public static Retrofit getAuthenticatedClient(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        String token = sessionManager.getAuthToken();
        
        if (authInterceptor == null) {
            authInterceptor = new AuthInterceptor(token);
        } else {
            authInterceptor.setAuthToken(token);
        }
        
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(authInterceptor)
                .build();
        
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }
}

