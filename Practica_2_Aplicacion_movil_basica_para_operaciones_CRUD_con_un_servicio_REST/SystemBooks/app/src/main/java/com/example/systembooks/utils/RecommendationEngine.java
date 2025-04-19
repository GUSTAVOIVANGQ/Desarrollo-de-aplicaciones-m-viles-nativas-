package com.example.systembooks.utils;

import android.content.Context;
import android.util.Log;

import com.example.systembooks.models.Book;
import com.example.systembooks.models.FavoriteBook;
import com.example.systembooks.models.SearchHistoryItem;
import com.example.systembooks.repositories.BookRepository;
import com.example.systembooks.repositories.FavoritesRepository;
import com.example.systembooks.repositories.SearchHistoryRepository;
import com.example.systembooks.util.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Engine for generating personalized book recommendations based on user's search history and favorites
 */
public class RecommendationEngine {
    private static final String TAG = "RecommendationEngine";
    private static final int MAX_RECOMMENDATIONS = 10;
    private static final int MAX_HISTORY_ITEMS = 5;
    
    private final Context context;
    private final SessionManager sessionManager;
    private final SearchHistoryRepository searchHistoryRepository;
    private final FavoritesRepository favoritesRepository;
    private final BookRepository bookRepository;
    
    public RecommendationEngine(Context context) {
        this.context = context;
        this.sessionManager = new SessionManager(context);
        this.searchHistoryRepository = new SearchHistoryRepository(context);
        this.favoritesRepository = new FavoritesRepository(context);
        this.bookRepository = new BookRepository(context);
    }
    
    /**
     * Get personalized book recommendations based on user's search history and favorites
     * @param callback Callback to handle the result
     */
    public void getRecommendations(BookRepository.BookCallback<List<Book>> callback) {
        // If user is not logged in, return featured books instead
        if (!sessionManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, returning featured books");
            bookRepository.getFeaturedBooks(MAX_RECOMMENDATIONS, callback);
            return;
        }
        
        // Get user ID
        long userId = sessionManager.getUserId();
        if (userId == -1) {
            Log.d(TAG, "Invalid user ID, returning featured books");
            bookRepository.getFeaturedBooks(MAX_RECOMMENDATIONS, callback);
            return;
        }
        
        // First try to get recommendations based on favorites and search history
        generateRecommendations(userId, callback);
    }
    
    /**
     * Generate personalized recommendations based on favorites and search history
     */
    private void generateRecommendations(long userId, BookRepository.BookCallback<List<Book>> callback) {
        // Step 1: Get user's favorites
        List<FavoriteBook> favorites = favoritesRepository.getFavorites(userId);
        
        // Step 2: Get user's recent search history
        List<SearchHistoryItem> searchHistory = searchHistoryRepository.getSearchHistory(userId);
        
        // If user has no favorites or search history, return featured books
        if ((favorites == null || favorites.isEmpty()) && (searchHistory == null || searchHistory.isEmpty())) {
            Log.d(TAG, "User has no favorites or search history, returning featured books");
            bookRepository.getFeaturedBooks(MAX_RECOMMENDATIONS, callback);
            return;
        }
        
        // Step 3: Extract relevant keywords for recommendation (from titles, authors, search queries)
        Set<String> keywords = extractKeywords(favorites, searchHistory);
        
        if (keywords.isEmpty()) {
            Log.d(TAG, "No keywords extracted, returning featured books");
            bookRepository.getFeaturedBooks(MAX_RECOMMENDATIONS, callback);
            return;
        }
        
        // Step 4: Use extracted keywords to search for recommendations
        searchRecommendations(keywords, callback);
    }
    
    /**
     * Search for books using extracted keywords
     */
    private void searchRecommendations(Set<String> keywords, BookRepository.BookCallback<List<Book>> callback) {
        Log.d(TAG, "Searching recommendations with keywords: " + keywords);
        
        // Keep track of all recommendations
        final List<Book> allRecommendations = new ArrayList<>();
        final Set<String> bookIds = new HashSet<>(); // To avoid duplicates
        
        // Count number of completed searches
        final CountDownLatch latch = new CountDownLatch(Math.min(3, keywords.size()));
        final AtomicReference<String> errorRef = new AtomicReference<>(null);
        
        // Use up to 3 keywords for search to avoid overloading
        int count = 0;
        for (String keyword : keywords) {
            if (count >= 3) break;
            
            bookRepository.searchBooks(keyword, 1, 5, new BookRepository.BookCallback<List<Book>>() {
                @Override
                public void onSuccess(List<Book> books) {
                    synchronized (allRecommendations) {
                        if (books != null && !books.isEmpty()) {
                            // Add non-duplicate books
                            for (Book book : books) {
                                if (!bookIds.contains(book.getId())) {
                                    allRecommendations.add(book);
                                    bookIds.add(book.getId());
                                }
                            }
                        }
                    }
                    latch.countDown();
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e(TAG, "Error searching for recommendations: " + errorMessage);
                    errorRef.set(errorMessage);
                    latch.countDown();
                }
            });
            
            count++;
        }
        
        // Wait for all searches to complete (with timeout)
        new Thread(() -> {
            try {
                // Wait for all searches to complete (with 10 second timeout)
                boolean completed = latch.await(10, java.util.concurrent.TimeUnit.SECONDS);
                
                if (!completed) {
                    Log.w(TAG, "Recommendation search timed out");
                }
                
                // If we have recommendations, return them
                synchronized (allRecommendations) {
                    if (!allRecommendations.isEmpty()) {
                        // Limit number of recommendations
                        List<Book> finalRecommendations = allRecommendations.size() > MAX_RECOMMENDATIONS 
                            ? allRecommendations.subList(0, MAX_RECOMMENDATIONS) 
                            : allRecommendations;
                            
                        callback.onSuccess(finalRecommendations);
                    } else if (errorRef.get() != null) {
                        // If there was an error, return it
                        callback.onError(errorRef.get());
                    } else {
                        // If no recommendations found, fallback to featured books
                        bookRepository.getFeaturedBooks(MAX_RECOMMENDATIONS, callback);
                    }
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "Recommendation search interrupted", e);
                callback.onError("Recommendation search interrupted: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Extract keywords from favorites and search history for recommendation
     */
    private Set<String> extractKeywords(List<FavoriteBook> favorites, List<SearchHistoryItem> searchHistory) {
        Set<String> keywords = new HashSet<>();
        
        // Extract keywords from favorites (titles and authors)
        if (favorites != null) {
            for (FavoriteBook favorite : favorites) {
                // Extract title keywords (skip common words)
                String title = favorite.getTitle();
                if (title != null && !title.isEmpty()) {
                    String[] titleWords = title.split("\\s+");
                    for (String word : titleWords) {
                        if (isRelevantKeyword(word)) {
                            keywords.add(word);
                        }
                    }
                }
                
                // Add author directly as a keyword
                String author = favorite.getAuthor();
                if (author != null && !author.isEmpty() && !author.equalsIgnoreCase("Unknown") && 
                    !author.equalsIgnoreCase("Desconocido")) {
                    keywords.add(author);
                }
            }
        }
        
        // Extract keywords from search history
        if (searchHistory != null) {
            int count = 0;
            for (SearchHistoryItem historyItem : searchHistory) {
                if (count >= MAX_HISTORY_ITEMS) break;
                
                String query = historyItem.getQuery();
                if (query != null && !query.isEmpty()) {
                    // If query looks like it might be a specific title or author name,
                    // add it as a whole phrase
                    if (query.length() > 5 && query.split("\\s+").length >= 2) {
                        keywords.add(query);
                    } else {
                        // Otherwise, split and add individual keywords
                        String[] queryWords = query.split("\\s+");
                        for (String word : queryWords) {
                            if (isRelevantKeyword(word)) {
                                keywords.add(word);
                            }
                        }
                    }
                }
                count++;
            }
        }
        
        return keywords;
    }
    
    /**
     * Check if a word is relevant enough to use as a keyword
     * Excludes common words, short words, etc.
     */
    private boolean isRelevantKeyword(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        
        // Normalize word
        word = word.toLowerCase().trim().replaceAll("[^a-záéíóúüñ]", "");
        
        // Skip if too short
        if (word.length() < 3) {
            return false;
        }
        
        // Skip common words in English and Spanish that wouldn't help with book recommendations
        Set<String> commonWords = getCommonWords();
        return !commonWords.contains(word);
    }
    
    /**
     * Get a set of common words to exclude from keywords
     */
    private Set<String> getCommonWords() {
        Set<String> commonWords = new HashSet<>();
        
        // English common words
        commonWords.add("the");
        commonWords.add("and");
        commonWords.add("for");
        commonWords.add("this");
        commonWords.add("that");
        commonWords.add("with");
        commonWords.add("from");
        commonWords.add("have");
        commonWords.add("has");
        commonWords.add("had");
        commonWords.add("not");
        commonWords.add("are");
        commonWords.add("was");
        commonWords.add("were");
        commonWords.add("been");
        commonWords.add("you");
        commonWords.add("your");
        commonWords.add("their");
        commonWords.add("they");
        commonWords.add("will");
        commonWords.add("would");
        
        // Spanish common words
        commonWords.add("los");
        commonWords.add("las");
        commonWords.add("del");
        commonWords.add("por");
        commonWords.add("para");
        commonWords.add("con");
        commonWords.add("que");
        commonWords.add("una");
        commonWords.add("uno");
        commonWords.add("esto");
        commonWords.add("esta");
        commonWords.add("estos");
        commonWords.add("estas");
        commonWords.add("como");
        commonWords.add("pero");
        commonWords.add("más");
        commonWords.add("mas");
        commonWords.add("ese");
        commonWords.add("esa");
        commonWords.add("esos");
        commonWords.add("esas");
        
        return commonWords;
    }
}