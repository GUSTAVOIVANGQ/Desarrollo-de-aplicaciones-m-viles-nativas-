package com.example.systembooks.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.systembooks.models.ImgbbResponse;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Helper class for uploading images to imgbb service
 */
public class ImgbbUploader {
    private static final String TAG = "ImgbbUploader";    private static final String BASE_URL = "https://api.imgbb.com/";
    // IMPORTANTE: Reemplaza esta API key con tu propia API key de imgbb.com
    // Para obtener una API key gratuita:
    // 1. Ve a https://api.imgbb.com/
    // 2. Crea una cuenta gratuita
    // 3. Obtén tu API key personal
    private static final String API_KEY = "c2eec4f113845fc033e9896ebfecff93"; // Reemplaza con tu API key real
    
    private final ImgbbService service;
    private final Context context;
    
    public interface ImgbbService {
        @FormUrlEncoded
        @POST("1/upload")
        Call<ImgbbResponse> uploadImage(
            @Query("key") String apiKey,
            @Field("image") String imageBase64
        );
    }
    
    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onError(String errorMessage);
    }
    
    public ImgbbUploader(Context context) {
        this.context = context;
        
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
                
        this.service = retrofit.create(ImgbbService.class);
    }
    
    /**
     * Upload an image from URI to imgbb
     * @param imageUri The URI of the image to upload
     * @param callback Callback for success/error handling
     */
    public void uploadImage(Uri imageUri, UploadCallback callback) {
        try {
            // Convert URI to Base64
            String base64Image = convertUriToBase64(imageUri);
            
            if (base64Image == null) {
                callback.onError("Error al procesar la imagen");
                return;
            }
            
            // Make API call
            Call<ImgbbResponse> call = service.uploadImage(API_KEY, base64Image);
            call.enqueue(new Callback<ImgbbResponse>() {
                @Override
                public void onResponse(@NonNull Call<ImgbbResponse> call, @NonNull Response<ImgbbResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ImgbbResponse imgbbResponse = response.body();
                        if (imgbbResponse.isSuccess() && imgbbResponse.getData() != null) {
                            String imageUrl = imgbbResponse.getData().getUrl();
                            Log.d(TAG, "Image uploaded successfully: " + imageUrl);
                            callback.onSuccess(imageUrl);
                        } else {
                            Log.e(TAG, "Upload failed: " + imgbbResponse.getError());
                            callback.onError("Error en la respuesta del servidor");
                        }
                    } else {
                        Log.e(TAG, "HTTP Error: " + response.code() + " - " + response.message());
                        callback.onError("Error de conexión: " + response.message());
                    }
                }
                
                @Override
                public void onFailure(@NonNull Call<ImgbbResponse> call, @NonNull Throwable t) {
                    Log.e(TAG, "Network error: " + t.getMessage());
                    callback.onError("Error de red: " + t.getMessage());
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error uploading image: " + e.getMessage());
            callback.onError("Error inesperado: " + e.getMessage());
        }
    }
    
    /**
     * Convert URI to Base64 string
     * @param uri The image URI
     * @return Base64 encoded string or null if error
     */
    private String convertUriToBase64(Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            
            // Decode and compress the image
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            
            if (bitmap == null) return null;
            
            // Resize bitmap if too large (max 1MB for imgbb free tier)
            bitmap = resizeBitmap(bitmap, 1024, 1024);
            
            // Convert to Base64
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
            
        } catch (Exception e) {
            Log.e(TAG, "Error converting URI to Base64: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Resize bitmap to fit within specified dimensions
     * @param bitmap Original bitmap
     * @param maxWidth Maximum width
     * @param maxHeight Maximum height
     * @return Resized bitmap
     */
    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap;
        }
        
        float aspectRatio = (float) width / height;
        
        if (width > height) {
            width = maxWidth;
            height = (int) (width / aspectRatio);
        } else {
            height = maxHeight;
            width = (int) (height * aspectRatio);
        }
        
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }
}
