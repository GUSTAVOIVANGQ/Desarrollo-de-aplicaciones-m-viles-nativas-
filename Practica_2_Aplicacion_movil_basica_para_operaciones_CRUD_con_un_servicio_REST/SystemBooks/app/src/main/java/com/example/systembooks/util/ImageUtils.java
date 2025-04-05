package com.example.systembooks.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.systembooks.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utilidad para gestionar operaciones con imágenes
 */
public class ImageUtils {
    private static final String TAG = "ImageUtils";

    /**
     * Crea un archivo temporal para la imagen capturada con la cámara
     */
    public static File createImageFile(Context context) throws IOException {
        // Crear nombre único para la imagen
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        
        // Crear el archivo
        return File.createTempFile(
                imageFileName,  /* prefijo */
                ".jpg",         /* sufijo */
                storageDir      /* directorio */
        );
    }

    /**
     * Convierte una Uri a un archivo
     */
    public static File uriToFile(Context context, Uri uri) throws IOException {
        File outputFile = createImageFile(context);
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return outputFile;
    }

    /**
     * Convierte un archivo a Uri
     */
    public static Uri fileToUri(Context context, File file) {
        return FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                file);
    }

    /**
     * Convierte un byte array a Bitmap
     */
    public static Bitmap byteArrayToBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    /**
     * Convierte un Bitmap a byte array
     */
    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return stream.toByteArray();
    }

    /**
     * Redimensiona una imagen para reducir su tamaño y mejorar el rendimiento
     */
    public static Bitmap resizeBitmap(Bitmap originalBitmap, int maxWidth, int maxHeight) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        
        float aspectRatio = (float) width / (float) height;
        
        if (width > height) {
            // Landscape image
            width = maxWidth;
            height = Math.round(width / aspectRatio);
        } else {
            // Portrait image
            height = maxHeight;
            width = Math.round(height * aspectRatio);
        }
        
        return Bitmap.createScaledBitmap(originalBitmap, width, height, true);
    }

    /**
     * Carga y redimensiona una imagen desde una Uri
     */
    public static Bitmap loadAndResizeImage(Context context, Uri imageUri, int maxWidth, int maxHeight) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            
            // Primero decodificar con inJustDecodeBounds=true para verificar dimensiones
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            
            // Calcular inSampleSize
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
            
            // Decodificar el bitmap con inSampleSize
            options.inJustDecodeBounds = false;
            inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "Error loading image: " + e.getMessage());
            return null;
        }
    }

    /**
     * Calcula el tamaño de muestra adecuado para cargar una imagen
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            
            // Calcular el mayor valor de inSampleSize que sea potencia de 2 y mantenga
            // la altura y anchura mayores que las requeridas
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        
        return inSampleSize;
    }

    /**
     * Carga una imagen de perfil desde diferentes fuentes (URL, ruta local, base64)
     * @param context Contexto de la aplicación
     * @param imageSource Fuente de la imagen (URL, ruta o base64)
     * @param imageView ImageView donde se cargará la imagen
     */
    public static void loadProfileImage(Context context, String imageSource, ImageView imageView) {
        if (imageSource == null || imageSource.isEmpty()) {
            imageView.setImageResource(R.drawable.default_profile);
            return;
        }

        try {
            // Intentar cargar como URL o ruta de archivo usando Glide
            if (imageSource.startsWith("http") || imageSource.startsWith("https")) {
                // Es una URL - usar Glide para cargar desde red
                Glide.with(context)
                    .load(imageSource)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(imageView);
            } else if (imageSource.startsWith("/")) {
                // Es una ruta de archivo local
                File file = new File(imageSource);
                if (file.exists()) {
                    Glide.with(context)
                        .load(file)
                        .placeholder(R.drawable.default_profile)
                        .error(R.drawable.default_profile)
                        .into(imageView);
                } else {
                    imageView.setImageResource(R.drawable.default_profile);
                }
            } else if (imageSource.startsWith("data:image") || imageSource.startsWith("base64,")) {
                // Es una cadena base64
                String base64Image = imageSource;
                if (imageSource.contains(",")) {
                    base64Image = imageSource.split(",")[1];
                }
                
                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imageView.setImageBitmap(bitmap);
            } else {
                // Intentar como cadena base64 (sin prefijo)
                try {
                    byte[] decodedString = Base64.decode(imageSource, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    imageView.setImageBitmap(bitmap);
                } catch (Exception e) {
                    Log.e(TAG, "Error decoding base64 image: " + e.getMessage());
                    imageView.setImageResource(R.drawable.default_profile);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading profile image: " + e.getMessage());
            imageView.setImageResource(R.drawable.default_profile);
        }
    }
}
