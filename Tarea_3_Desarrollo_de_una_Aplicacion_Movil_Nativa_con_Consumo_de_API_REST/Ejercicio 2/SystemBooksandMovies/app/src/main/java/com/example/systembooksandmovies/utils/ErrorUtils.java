package com.example.systembooksandmovies.utils;

import android.content.Context;

import java.io.IOException;
import java.net.SocketTimeoutException;

import retrofit2.Response;

/**
 * Clase de utilidad para manejo de errores en las peticiones a la API
 */
public class ErrorUtils {

    /**
     * Obtiene un mensaje de error descriptivo basado en la respuesta HTTP
     * @param response La respuesta HTTP
     * @return Un mensaje de error descriptivo
     */
    public static String getErrorMessage(Response<?> response) {
        if (response == null) return Constants.ERROR_GENERIC;
        
        switch (response.code()) {
            case 400:
                return "Solicitud incorrecta. Por favor, revise los datos.";
            case 401:
                return "No autorizado. Por favor, inicie sesión nuevamente.";
            case 403:
                return "Acceso prohibido.";
            case 404:
                return "No se encontró el recurso solicitado.";
            case 429:
                return "Demasiadas solicitudes. Intente de nuevo más tarde.";
            case 500:
            case 501:
            case 502:
            case 503:
                return Constants.ERROR_SERVER;
            default:
                return Constants.ERROR_GENERIC;
        }
    }
    
    /**
     * Obtiene un mensaje de error descriptivo basado en la excepción
     * @param throwable La excepción que ocurrió
     * @param context Contexto de la aplicación para verificar conectividad
     * @return Un mensaje de error descriptivo
     */
    public static String getErrorMessage(Throwable throwable, Context context) {
        if (throwable instanceof SocketTimeoutException) {
            return "Tiempo de espera agotado. La conexión al servidor es lenta.";
        } else if (throwable instanceof IOException) {
            return NetworkUtils.isNetworkAvailable(context) 
                    ? Constants.ERROR_SERVER 
                    : Constants.ERROR_NETWORK;
        } else {
            return Constants.ERROR_GENERIC;
        }
    }
}
