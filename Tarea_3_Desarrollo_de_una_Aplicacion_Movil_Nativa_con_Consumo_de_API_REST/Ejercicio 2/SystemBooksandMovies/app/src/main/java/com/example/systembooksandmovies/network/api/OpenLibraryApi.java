package com.example.systembooksandmovies.network.api;

import com.example.systembooksandmovies.network.models.BookResponse;
import com.example.systembooksandmovies.network.models.SearchResponse;
import com.example.systembooksandmovies.network.models.CategoryResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OpenLibraryApi {
    
    // Buscar libros por consulta
    @GET("search.json")
    Call<SearchResponse> searchBooks(
            @Query("q") String query,
            @Query("page") int page,
            @Query("limit") int limit
    );
    
    // Obtener detalles de un libro por ID
    @GET("works/{workId}.json")
    Call<BookResponse> getBookDetails(
            @Path("workId") String workId
    );
    
    // Obtener libros por categoría/tema
    @GET("subjects/{subject}.json")
    Call<CategoryResponse> getBooksInCategory(
            @Path("subject") String subject,
            @Query("limit") int limit,
            @Query("offset") int offset
    );
    
    // Obtener libros destacados
    @GET("trending/daily.json")
    Call<SearchResponse> getTrendingBooks(
            @Query("limit") int limit
    );
}
