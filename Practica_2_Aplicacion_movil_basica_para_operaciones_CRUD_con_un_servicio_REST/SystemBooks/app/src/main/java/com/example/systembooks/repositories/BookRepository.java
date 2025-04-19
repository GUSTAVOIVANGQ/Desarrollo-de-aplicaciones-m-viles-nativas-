package com.example.systembooks.repositories;

import android.content.Context;

import com.example.systembooks.models.Book;
import com.example.systembooks.network.ApiClient;
import com.example.systembooks.network.api.OpenLibraryApi;
import com.example.systembooks.network.models.BookResponse;
import com.example.systembooks.network.models.CategoryResponse;
import com.example.systembooks.network.models.SearchResponse;
import com.example.systembooks.utils.Constants;
import com.example.systembooks.utils.ErrorUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookRepository {

    private Context context;
    private OpenLibraryApi apiService;
    private SearchHistoryRepository searchHistoryRepository;

    public BookRepository(Context context) {
        this.context = context;
        this.apiService = ApiClient.getApiService(context);
        this.searchHistoryRepository = new SearchHistoryRepository(context);
    }

    // Interfaz para manejar respuestas asincrónicas
    public interface BookCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    // Obtener libros destacados
    public void getFeaturedBooks(int limit, BookCallback<List<Book>> callback) {
        apiService.getTrendingBooks(limit).enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Book> books = convertToBooks(response.body().getDocs());
                    callback.onSuccess(books);
                } else {
                    callback.onError(ErrorUtils.getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Buscar libros por consulta
    public void searchBooks(String query, int page, int limit, BookCallback<List<Book>> callback) {
        // Modificado para guardar el historial de búsqueda
        searchBooks(query, page, limit, 0, callback); // userId predeterminado = 0 si no se especifica
    }

    // Buscar libros por consulta con ID de usuario para seguimiento del historial
    public void searchBooks(String query, int page, int limit, long userId, BookCallback<List<Book>> callback) {
        apiService.searchBooks(query, page, limit).enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Guardar la búsqueda en el historial si el usuario está logueado (userId > 0)
                    if (userId > 0) {
                        searchHistoryRepository.saveSearchQuery(userId, query);
                    }
                    
                    List<Book> books = convertToBooks(response.body().getDocs());
                    callback.onSuccess(books);
                } else {
                    callback.onError(ErrorUtils.getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Obtener detalles de un libro
    public void getBookDetails(String bookId, BookCallback<Book> callback) {
        apiService.getBookDetails(bookId).enqueue(new Callback<BookResponse>() {
            @Override
            public void onResponse(Call<BookResponse> call, Response<BookResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Book book = convertToBook(response.body());
                    callback.onSuccess(book);
                } else {
                    callback.onError(ErrorUtils.getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<BookResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Obtener libros por categoría
    public void getBooksByCategory(String category, int limit, int offset, BookCallback<List<Book>> callback) {
        apiService.getBooksInCategory(category, limit, offset).enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(Call<CategoryResponse> call, Response<CategoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Book> books = convertCategoryToBooks(response.body().getWorks());
                    callback.onSuccess(books);
                } else {
                    callback.onError(ErrorUtils.getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<CategoryResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Convertir SearchResponse.BookDoc a Book
    private List<Book> convertToBooks(List<SearchResponse.BookDoc> docs) {
        List<Book> books = new ArrayList<>();
        if (docs != null) {
            for (SearchResponse.BookDoc doc : docs) {
                String id = doc.getKey().replace("/works/", "");
                Book book = new Book(id, doc.getTitle(), doc.getPrimaryAuthor());
                
                // Establecer URL de portada si existe
                if (doc.getCoverId() != null) {
                    book.setCoverUrl(String.format(Constants.COVER_URL, doc.getCoverId()));
                }
                
                if (doc.getFirstPublishYear() != null) {
                    book.setPublishYear(doc.getFirstPublishYear().toString());
                }
                
                books.add(book);
            }
        }
        return books;
    }

    // Convertir BookResponse a Book
    private Book convertToBook(BookResponse response) {
        String id = response.getKey().replace("/works/", "");
        String title = response.getTitle();
        String author = "Desconocido";
        
        // Extraer el primer autor si existe
        if (response.getAuthors() != null && !response.getAuthors().isEmpty()) {
            author = response.getAuthors().get(0).getAuthorKey();
        }
        
        Book book = new Book(id, title, author);
        
        // Establecer descripción
        book.setDescription(response.getDescriptionText());
        
        // Establecer número de páginas
        if (response.getNumberOfPages() != null) {
            book.setPageCount(response.getNumberOfPages());
        }
        
        // Establecer editorial
        if (response.getPublishers() != null && !response.getPublishers().isEmpty()) {
            book.setPublisher(response.getPublishers().get(0).getName());
        }
        
        // Establecer año de publicación
        book.setPublishYear(response.getPublishDate());
        
        // Establecer URL de portada si existe
        if (response.getCovers() != null && !response.getCovers().isEmpty()) {
            book.setCoverUrl(String.format(Constants.COVER_URL, response.getCovers().get(0)));
        }
        
        return book;
    }

    // Convertir CategoryResponse.Work a Book
    private List<Book> convertCategoryToBooks(List<CategoryResponse.Work> works) {
        List<Book> books = new ArrayList<>();
        if (works != null) {
            for (CategoryResponse.Work work : works) {
                String id = work.getKey().replace("/works/", "");
                Book book = new Book(id, work.getTitle(), work.getAuthorName());
                
                // Establecer URL de portada si existe
                if (work.getCoverId() != null) {
                    book.setCoverUrl(String.format(Constants.COVER_URL, work.getCoverId()));
                }
                
                books.add(book);
            }
        }
        return books;
    }
}
