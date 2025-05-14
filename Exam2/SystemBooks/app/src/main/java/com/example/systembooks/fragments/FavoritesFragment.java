package com.example.systembooks.fragments;

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

public class FavoritesFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView emptyView;
    private ProgressBar progressBar;

    private BookAdapter adapter;
    private FavoritesRepository favoritesRepository;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        
        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewFavorites);
        emptyView = view.findViewById(R.id.emptyView);
        progressBar = view.findViewById(R.id.progressBar);
        
        // Setup RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new BookAdapter(getContext(), new ArrayList<>());
        recyclerView.setAdapter(adapter);
        
        // Initialize repositories and session manager
        favoritesRepository = new FavoritesRepository(getContext());
        sessionManager = new SessionManager(getContext());
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavoriteBooks();
    }

    private void loadFavoriteBooks() {
        if (!sessionManager.isLoggedIn()) {
            showEmptyView();
            return;
        }
        
        showLoading();
        
        new Thread(() -> {
            final List<FavoriteBook> favorites = favoritesRepository.getFavorites(sessionManager.getUserId());
            
            // Convert FavoriteBook objects to Book objects
            final List<Book> favoriteBooks = new ArrayList<>();
            for (FavoriteBook favorite : favorites) {
                Book book = new Book(favorite.getBookId(), favorite.getTitle(), favorite.getAuthor());
                book.setCoverUrl(favorite.getCoverUrl());
                favoriteBooks.add(book);
            }
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (favoriteBooks != null && !favoriteBooks.isEmpty()) {
                        adapter.updateBooks(favoriteBooks);
                        showContent();
                    } else {
                        showEmptyView();
                    }
                });
            }
        }).start();
    }
    
    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
    }
    
    private void showContent() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }
    
    private void showEmptyView() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }
}