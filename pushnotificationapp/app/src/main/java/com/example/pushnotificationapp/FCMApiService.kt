package com.example.pushnotificationapp

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface FCMApiService {

    @POST("fcm/send")
    suspend fun sendNotification(
        @Header("Authorization") authHeader: String,
        @Body notificationRequest: FCMNotificationRequest
    ): Response<FCMResponse>

    companion object {
        private const val BASE_URL = "https://fcm.googleapis.com/"

        fun create(): FCMApiService {
            val logger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(FCMApiService::class.java)
        }
    }
}
