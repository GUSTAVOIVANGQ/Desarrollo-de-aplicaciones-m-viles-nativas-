package com.example.systembooks.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.systembooks.R;
import com.example.systembooks.adapters.BookAdapter;
import com.example.systembooks.models.Book;
import com.example.systembooks.repositories.BookRepository;
import com.example.systembooks.repositories.SearchHistoryRepository;
import com.example.systembooks.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class SearchResultsActivity extends AppCompatActivity {

    public static final String EXTRA_QUERY = "extra_query";
    public static final String EXTRA_SEARCH_QUERY = "extra_search_query";

    private RecyclerView recyclerViewResults;
    private ProgressBar progressBar;
    private TextView emptyResultsView;
    
    private BookAdapter adapter;
    private BookRepository bookRepository;
    private SearchHistoryRepository historyRepository;
    private SessionManager sessionManager;
    
    private String searchQuery;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        
        // Get search query from intent
        searchQuery = getIntent().getStringExtra(EXTRA_QUERY);
        
        // Set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.search_results_for, searchQuery));
        }
        
        // Initialize views
        recyclerViewResults = findViewById(R.id.recyclerViewResults);
        progressBar = findViewById(R.id.progressBar);
        emptyResultsView = findViewById(R.id.emptyResultsView);
        
        // Initialize repositories and session manager
        bookRepository = new BookRepository(this);
        historyRepository = new SearchHistoryRepository(this);
        sessionManager = new SessionManager(this);
        
        // Set up RecyclerView
        adapter = new BookAdapter(this, new ArrayList<>());
        recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewResults.setAdapter(adapter);
        
        // Perform search
        if (searchQuery != null && !searchQuery.isEmpty()) {
            performSearch(searchQuery);
            
            // Save to search history if user is logged in
            Long userId = sessionManager.getUserId();
            if (userId != -1) {
                saveSearchToHistory(userId, searchQuery);
            }
        } else {
            showEmptyResults("No search query provided");
        }
    }
    
    private void performSearch(String query) {
        progressBar.setVisibility(View.VISIBLE);
        emptyResultsView.setVisibility(View.GONE);
        
        // Updated to pass correct parameters to the searchBooks method
        bookRepository.searchBooks(query, 1, 20, new BookRepository.BookCallback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> books) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (books.isEmpty()) {
                        showEmptyResults("No results found for \"" + query + "\"");
                    } else {
                        adapter.updateBooks(books);
                        recyclerViewResults.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    showEmptyResults("Error: " + errorMessage);
                });
            }
        });
    }
    
    private void showEmptyResults(String message) {
        recyclerViewResults.setVisibility(View.GONE);
        emptyResultsView.setVisibility(View.VISIBLE);
        emptyResultsView.setText(message);
    }
    
    private void saveSearchToHistory(Long userId, String query) {
        // Updated to use the correct method name from SearchHistoryRepository
        new Thread(() -> historyRepository.saveSearchQuery(userId, query)).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
