package com.example.systembooksandmovies.network;

import android.content.Context;

import com.example.systembooksandmovies.network.api.OpenLibraryApi;
import com.example.systembooksandmovies.utils.Constants;
import com.example.systembooksandmovies.utils.NetworkUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final int CACHE_SIZE = Constants.CACHE_SIZE_MB * 1024 * 1024;
    private static OpenLibraryApi apiService;
    private static Retrofit retrofit;

    public static OpenLibraryApi getApiService(Context context) {
        if (apiService == null) {
            apiService = getClient(context).create(OpenLibraryApi.class);
        }
        return apiService;
    }

    private static Retrofit getClient(Context context) {
        if (retrofit == null) {
            // Crear caché
            File httpCacheDirectory = new File(context.getCacheDir(), "http-cache");
            Cache cache = new Cache(httpCacheDirectory, CACHE_SIZE);

            // Configurar cliente OkHttp con caché e interceptores
            OkHttpClient client = new OkHttpClient.Builder()
                    .addNetworkInterceptor(new CacheInterceptor())
                    .addInterceptor(new OfflineCacheInterceptor(context))
                    .cache(cache)
                    .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
                    .build();

            // Crear Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    /**
     * Interceptor para forzar el uso de caché cuando estamos offline
     */
    private static class OfflineCacheInterceptor implements Interceptor {
        private Context context;

        public OfflineCacheInterceptor(Context context) {
            this.context = context;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            if (!NetworkUtils.isNetworkAvailable(context) && Constants.ENABLE_OFFLINE_CACHE) {
                CacheControl cacheControl = new CacheControl.Builder()
                        .maxStale(Constants.CACHE_MAX_STALE, TimeUnit.SECONDS)
                        .build();

                request = request.newBuilder()
                        .removeHeader("Pragma")
                        .removeHeader("Cache-Control")
                        .cacheControl(cacheControl)
                        .build();
            }

            return chain.proceed(request);
        }
    }

    /**
     * Interceptor para configurar la caché en las respuestas de la API
     */
    private static class CacheInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response response = chain.proceed(chain.request());

            CacheControl cacheControl = new CacheControl.Builder()
                    .maxAge(Constants.CACHE_MAX_AGE, TimeUnit.SECONDS)
                    .build();

            return response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", cacheControl.toString())
                    .build();
        }
    }
}
