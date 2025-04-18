package com.example.systembooks.utils;

/**
 * Clase que contiene todas las constantes utilizadas en la aplicación
 */
public class Constants {
    
    // API URLs
    public static final String BASE_URL = "https://openlibrary.org/";
    public static final String COVER_URL = "https://covers.openlibrary.org/b/id/%d-L.jpg";
    public static final String AUTHOR_URL = "https://openlibrary.org/authors/%s";
    
    // API Search Parameters
    public static final String PARAM_QUERY = "q";
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_LIMIT = "limit";
    public static final String PARAM_OFFSET = "offset";
    
    // API Path Parameters
    public static final String PATH_WORKS = "works";
    public static final String PATH_AUTHORS = "authors";
    public static final String PATH_SUBJECTS = "subjects";
    public static final String PATH_SEARCH = "search";
    
    // Pagination defaults
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int DEFAULT_FIRST_PAGE = 1;
    
    // Network timeouts (in seconds)
    public static final int CONNECT_TIMEOUT = 30;
    public static final int READ_TIMEOUT = 30;
    public static final int WRITE_TIMEOUT = 30;
    
    // Cache settings
    public static final int CACHE_SIZE_MB = 10; // 10 MB
    public static final int CACHE_MAX_AGE = 60 * 60; // 1 hora (en segundos)
    public static final int CACHE_MAX_STALE = 60 * 60 * 24 * 7; // 1 semana (en segundos)
    
    // Intent keys
    public static final String EXTRA_BOOK_ID = "extra_book_id";
    public static final String EXTRA_CATEGORY_SLUG = "extra_category_slug";
    public static final String EXTRA_CATEGORY_NAME = "extra_category_name";
    public static final String EXTRA_SEARCH_QUERY = "extra_search_query";
    
    // SharedPreferences
    public static final String PREF_FILE_NAME = "app_preferences";
    public static final String PREF_RECENT_SEARCHES = "recent_searches";
    public static final int MAX_RECENT_SEARCHES = 10;
    
    // Error messages
    public static final String ERROR_NETWORK = "Error de red. Por favor, compruebe su conexión a Internet.";
    public static final String ERROR_SERVER = "Error del servidor. Por favor, inténtelo de nuevo más tarde.";
    public static final String ERROR_GENERIC = "Algo salió mal. Por favor, inténtelo de nuevo.";
    
    // Feature flags
    public static final boolean ENABLE_OFFLINE_CACHE = true;
    public static final boolean ENABLE_RECENT_SEARCHES = true;
}
