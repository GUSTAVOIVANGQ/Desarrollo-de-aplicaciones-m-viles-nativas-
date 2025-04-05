package com.example.systembooks.model;

import java.io.Serializable;

public class User implements Serializable {
    private Long id;
    private String name;
    private String email;
    private String password;
    private String role;
    private String profileImage;

    // Constructor vacío necesario para Gson
    public User() {
    }

    // Constructor para creación de usuarios
    public User(Long id, String name, String email, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    /**
     * Método de compatibilidad para mantener el código existente funcionando
     * @return el nombre del usuario
     */
    public String getNombre() {
        return name;
    }

    /**
     * Método de compatibilidad para mantener el código existente funcionando
     * @param nombre el nombre del usuario a establecer
     */
    public void setNombre(String nombre) {
        this.name = nombre;
    }

    /**
     * Método de compatibilidad para acceder a la imagen del perfil
     * @return la imagen del perfil del usuario
     */
    public String getImagen() {
        return profileImage;
    }

    /**
     * Método de compatibilidad para establecer la imagen del perfil
     * @param imagen la imagen del perfil a establecer
     */
    public void setImagen(String imagen) {
        this.profileImage = imagen;
    }
}
