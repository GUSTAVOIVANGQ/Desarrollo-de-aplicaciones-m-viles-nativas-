package com.example.systembooks.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.systembooks.R;
import com.example.systembooks.activities.BookDetailActivity;
import com.example.systembooks.adapters.BookAdapter;
import com.example.systembooks.models.Book;
import com.example.systembooks.models.FavoriteBook;
import com.example.systembooks.repositories.FavoritesRepository;
import com.example.systembooks.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment implements BookAdapter.OnBookClickListener {

    private RecyclerView recyclerViewFavorites;
    private TextView emptyView;
    private ProgressBar progressBar;
    
    private BookAdapter bookAdapter;
    private FavoritesRepository repository;
    private SessionManager sessionManager;
    private List<Book> favoriteBooks;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        
        // Initialize views
        recyclerViewFavorites = view.findViewById(R.id.recyclerViewFavorites);
        emptyView = view.findViewById(R.id.emptyView);
        progressBar = view.findViewById(R.id.progressBar);
        
        // Initialize repository and session manager
        repository = new FavoritesRepository(requireContext());
        sessionManager = new SessionManager(requireContext());
        favoriteBooks = new ArrayList<>();
        
        setupRecyclerView();
        
        // Load favorite books
        loadFavoriteBooks();
        
        return view;
    }
    
    private void setupRecyclerView() {
        bookAdapter = new BookAdapter(requireContext(), favoriteBooks, this);
        recyclerViewFavorites.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerViewFavorites.setAdapter(bookAdapter);
    }
    
    private void loadFavoriteBooks() {
        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);
        
        // Get current user ID
        Long userId = sessionManager.getUserId();
        
        // If no user is logged in, show empty state
        if (userId == -1) {
            progressBar.setVisibility(View.GONE);
            showEmptyState();
            return;
        }
        
        // Load favorite books in a background thread
        new Thread(() -> {
            List<FavoriteBook> favorites = repository.getFavorites(userId);
            List<Book> books = convertToBooks(favorites);
            
            // Update UI on the main thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (books.isEmpty()) {
                        showEmptyState();
                    } else {
                        showFavoriteBooks(books);
                    }
                });
            }
        }).start();
    }
    
    private List<Book> convertToBooks(List<FavoriteBook> favorites) {
        List<Book> books = new ArrayList<>();
        for (FavoriteBook favorite : favorites) {
            Book book = new Book(favorite.getBookId(), favorite.getTitle(), favorite.getAuthor());
            book.setCoverUrl(favorite.getCoverUrl());
            books.add(book);
        }
        return books;
    }
    
    private void showFavoriteBooks(List<Book> books) {
        recyclerViewFavorites.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        favoriteBooks.clear();
        favoriteBooks.addAll(books);
        bookAdapter.notifyDataSetChanged();
    }
    
    private void showEmptyState() {
        recyclerViewFavorites.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }
    
    @Override
    public void onBookClick(Book book) {
        // Navigate to book detail activity
        Intent intent = new Intent(requireContext(), BookDetailActivity.class);
        intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.getId());
        startActivity(intent);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload favorites when returning to this fragment
        // (in case a book has been added or removed from favorites)
        loadFavoriteBooks();
    }
}