package com.example.helloandroid.api;

import android.content.Context;
import android.util.Log;

import com.example.helloandroid.util.ServerConfig;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static Retrofit retrofit = null;

    public static void resetClient() {
        retrofit = null;
    }

    public static Retrofit getClient(Context context) {
        String baseUrl = ServerConfig.getServerUrl(context);
        Log.d(TAG, "Configurando cliente Retrofit con URL: " + baseUrl);
        
        if (retrofit == null || !retrofit.baseUrl().toString().equals(baseUrl)) {
            // Agregar interceptor de logging para depuración
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            // Configurar cliente OkHttp con timeout más largo (dispositivos físicos pueden necesitarlo)
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();
            
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
            
            Log.d(TAG, "Retrofit configurado exitosamente con URL: " + baseUrl);
        }
        return retrofit;
    }
}
