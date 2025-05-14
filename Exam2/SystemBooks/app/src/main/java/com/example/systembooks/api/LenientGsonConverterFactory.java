package com.example.systembooks.api;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Una fábrica de conversores personalizada para Retrofit que maneja de forma más tolerante 
 * las respuestas JSON del servidor que podrían no estar perfectamente formateadas.
 */
public class LenientGsonConverterFactory extends Converter.Factory {

    private final Gson gson;

    private LenientGsonConverterFactory(Gson gson) {
        this.gson = gson;
    }

    public static LenientGsonConverterFactory create() {
        return create(new Gson());
    }

    public static LenientGsonConverterFactory create(Gson gson) {
        if (gson == null) throw new NullPointerException("gson == null");
        return new LenientGsonConverterFactory(gson);
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new LenientGsonResponseBodyConverter<>(gson, adapter);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations,
                                                         Annotation[] methodAnnotations, Retrofit retrofit) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new GsonRequestBodyConverter<>(gson, adapter);
    }

    /**
     * Conversor personalizado para manejar las respuestas del servidor de manera más tolerante.
     */
    private static final class LenientGsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
        private final Gson gson;
        private final TypeAdapter<T> adapter;

        LenientGsonResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
            this.gson = gson;
            this.adapter = adapter;
        }

        @Override
        public T convert(ResponseBody value) throws IOException {
            String responseString = value.string();
            MediaType contentType = value.contentType();
            
            try {
                // Usar JsonReader con modo permisivo
                JsonReader jsonReader = gson.newJsonReader(new StringReader(responseString));
                jsonReader.setLenient(true);
                
                // Verificar si el primer token es un valor primitivo (como una cadena)
                if (jsonReader.peek() == JsonToken.STRING) {
                    // Si es una cadena, intentamos leer todo el cuerpo como una cadena
                    return null;
                }
                
                try {
                    T result = adapter.read(jsonReader);
                    if (jsonReader.peek() != JsonToken.END_DOCUMENT) {
                        throw new JsonIOException("JSON document was not fully consumed.");
                    }
                    return result;
                } finally {
                    value.close();
                }
            } catch (JsonIOException | JsonSyntaxException e) {
                // Si hay un error al analizar el JSON, devolver null 
                // y dejar que ApiRepository maneje el error
                return null;
            }
        }
    }

    /**
     * Conversor para solicitudes estándar de Gson.
     */
    private static final class GsonRequestBodyConverter<T> implements Converter<T, RequestBody> {
        private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8");

        private final Gson gson;
        private final TypeAdapter<T> adapter;

        GsonRequestBodyConverter(Gson gson, TypeAdapter<T> adapter) {
            this.gson = gson;
            this.adapter = adapter;
        }

        @Override
        public RequestBody convert(T value) throws IOException {
            Buffer buffer = new Buffer();
            try {
                // Create an OutputStreamWriter to wrap the OutputStream
                OutputStreamWriter writer = new OutputStreamWriter(buffer.outputStream(), StandardCharsets.UTF_8);
                // Now pass the Writer to newJsonWriter
                adapter.write(gson.newJsonWriter(writer), value);
                writer.flush(); // Ensure data is written to buffer
                return RequestBody.create(MEDIA_TYPE, buffer.readByteString());
            } catch (JsonIOException e) {
                throw new IOException(e);
            }
        }
    }
}

