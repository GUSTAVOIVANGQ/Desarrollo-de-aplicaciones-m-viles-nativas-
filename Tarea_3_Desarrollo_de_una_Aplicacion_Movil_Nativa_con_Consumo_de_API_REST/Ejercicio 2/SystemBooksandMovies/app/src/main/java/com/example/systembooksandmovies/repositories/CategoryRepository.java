package com.example.systembooksandmovies.repositories;

import android.content.Context;

import com.example.systembooksandmovies.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {

    private Context context;
    
    public CategoryRepository(Context context) {
        this.context = context;
    }
    
    // Interfaz para manejar respuestas asincrónicas
    public interface CategoryCallback {
        void onSuccess(List<Category> categories);
        void onError(String message);
    }
    
    // Obtener categorías disponibles
    // Por ahora usamos datos estáticos, en el futuro podríamos obtenerlos de la API
    public void getCategories(CategoryCallback callback) {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("Ficción", "fiction"));
        categories.add(new Category("Ciencia Ficción", "science_fiction"));
        categories.add(new Category("Fantasía", "fantasy"));
        categories.add(new Category("Misterio", "mystery"));
        categories.add(new Category("Romance", "romance"));
        categories.add(new Category("Thriller", "thriller"));
        categories.add(new Category("Biografía", "biography"));
        categories.add(new Category("Historia", "history"));
        categories.add(new Category("Infantil", "children"));
        categories.add(new Category("Poesía", "poetry"));
        categories.add(new Category("Drama", "drama"));
        categories.add(new Category("Comedia", "comedy"));
        
        callback.onSuccess(categories);
    }
}
