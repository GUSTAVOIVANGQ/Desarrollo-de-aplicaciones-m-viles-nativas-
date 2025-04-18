package com.example.systembooksandmovies.network.interceptors;

import android.content.Context;
import com.example.systembooksandmovies.utils.NetworkUtils;
import java.io.IOException;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class CacheInterceptor implements Interceptor {

    private static final int MAX_AGE = 60 * 60; // 1 hour (seconds)
    private static final int MAX_STALE = 60 * 60 * 24 * 7; // 1 week (offline)
    private Context context;

    public CacheInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        // Modify request based on network availability
        if (!NetworkUtils.isNetworkAvailable(context)) {
            request = request.newBuilder()
                    .cacheControl(new CacheControl.Builder()
                            .maxStale(MAX_STALE, java.util.concurrent.TimeUnit.SECONDS)
                            .build())
                    .build();
        }

        Response originalResponse = chain.proceed(request);

        if (NetworkUtils.isNetworkAvailable(context)) {
            return originalResponse.newBuilder()
                    .header("Cache-Control", "public, max-age=" + MAX_AGE)
                    .build();
        } else {
            return originalResponse;
        }
    }
}