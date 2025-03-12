package com.example.helloandroid.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class ServerConfig {
    private static final String TAG = "ServerConfig";
    private static final String PREFS_NAME = "ServerConfigPrefs";
    private static final String KEY_SERVER_URL = "server_url";
    
    // Cambiamos el default a la red corporativa que estás usando
    private static final String DEFAULT_SERVER_URL = "http://192.168.8.71:8088/";

    // Lista estática de servidores predefinidos para facilitar la selección
    private static final String[] STATIC_PREDEFINED_SERVERS = {
        // URLs para redes corporativas - parece que estás en una red 172.100.x.x
        "http://172.100.90.248:8088/",   // La IP original que intentabas
        "http://172.100.86.1:8088/",     // Posible gateway de tu red actual
        "http://172.100.86.248:8088/",   // Similar a tu red pero diferente host
        
        // Para emulador y redes domésticas (probablemente no funcionan en tu red corporativa)
        "http://10.0.2.2:8088/",         // Emulador Android -> localhost
        "http://192.168.1.100:8088/"     // Red doméstica típica
    };
    
    // Variable para almacenar los servidores predefinidos (incluirá también IPs autodetectadas)
    private static String[] dynamicPredefinedServers = null;

    public static String getServerUrl(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL);
    }

    public static void saveServerUrl(Context context, String serverUrl) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_SERVER_URL, serverUrl);
        editor.apply();
    }
    
    public static String[] getPredefinedServers(Context context) {
        if (dynamicPredefinedServers == null) {
            // Detectar IPs de la red local
            List<String> detectedIps = getLocalIpAddressesWithSubnets();
            
            // Construir la lista completa
            List<String> allServers = new ArrayList<>();
            
            // Agregar primero la IP donde está ejecutándose el servidor actualmente (si existe)
            String currentServerUrl = getServerUrl(context);
            allServers.add(currentServerUrl);
            
            // Agregar IPs detectadas automáticamente en la misma subred
            for (String ip : detectedIps) {
                String serverUrl = "http://" + ip + ":8088/";
                if (!allServers.contains(serverUrl)) {
                    allServers.add(serverUrl);
                }
            }
            
            // Agregar IPs estáticas predefinidas
            for (String server : STATIC_PREDEFINED_SERVERS) {
                if (!allServers.contains(server)) {
                    allServers.add(server);
                }
            }
            
            // Convertir a array y guardar
            dynamicPredefinedServers = allServers.toArray(new String[0]);
        }
        
        return dynamicPredefinedServers;
    }
    
    // Método mejorado para detectar IPs locales y generar posibles IPs en la misma subred
    private static List<String> getLocalIpAddressesWithSubnets() {
        List<String> ipAddresses = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                // Skip loopback and disabled interfaces
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) {
                        String ip = addr.getHostAddress();
                        
                        // Log la interfaz de red y su IP
                        Log.d(TAG, "Interfaz: " + networkInterface.getDisplayName() + " - IP: " + ip);
                        
                        // Añadir la IP local
                        ipAddresses.add(ip);
                        
                        // Añadir otras IPs posibles en la misma subred
                        String[] parts = ip.split("\\.");
                        if (parts.length == 4) {
                            // Priorizar las IPs en la red 172.100.x.x ya que parece ser tu red corporativa
                            if (parts[0].equals("172") && parts[1].equals("100")) {
                                // Añadir varias IPs en la red 172.100.x.x
                                for (int i = 1; i <= 10; i++) {
                                    ipAddresses.add("172.100." + parts[2] + "." + i); // IPs bajas (posibles gateways)
                                }
                                // Añadir servidores en la misma subred pero diferente octeto
                                ipAddresses.add("172.100." + parts[2] + ".248");
                                ipAddresses.add("172.100." + parts[2] + ".249");
                                ipAddresses.add("172.100." + parts[2] + ".250");
                                
                                // También probar otras subredes cercanas
                                int subnet = Integer.parseInt(parts[2]);
                                ipAddresses.add("172.100." + (subnet-1) + ".248");
                                ipAddresses.add("172.100." + (subnet+1) + ".248");
                                
                                // La IP original que intentabas usar
                                ipAddresses.add("172.100.90.248");
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            Log.e(TAG, "Error obteniendo direcciones IP locales", e);
        }
        
        return ipAddresses;
    }
    
    public static String getDeviceInfo() {
        return "Modelo: " + Build.MODEL + "\n" +
               "Android: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")\n" +
               "Fabricante: " + Build.MANUFACTURER;
    }
    
    // Reset the cached servers list to force regeneration
    public static void resetServerList() {
        dynamicPredefinedServers = null;
    }
}
