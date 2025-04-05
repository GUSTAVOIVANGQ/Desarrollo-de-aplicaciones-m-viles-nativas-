package com.example.systembooks.util;

import org.apache.commons.codec.digest.DigestUtils;

public class PasswordUtils {
    
    /**
     * Encripta una contraseña usando SHA-256
     * @param password Contraseña en texto plano
     * @return Contraseña encriptada
     */
    public static String encryptPassword(String password) {
        return DigestUtils.sha256Hex(password);
    }
}
