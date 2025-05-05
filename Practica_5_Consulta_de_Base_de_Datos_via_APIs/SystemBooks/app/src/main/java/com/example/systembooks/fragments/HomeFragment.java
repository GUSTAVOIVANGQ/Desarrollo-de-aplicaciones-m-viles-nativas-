package com.example.systembooks.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.systembooks.R;
import com.example.systembooks.activities.BookDetailActivity;
import com.example.systembooks.adapters.BookAdapter;
import com.example.systembooks.models.Book;
import com.example.systembooks.repositories.BookRepository;
import com.example.systembooks.util.SessionManager;
import com.example.systembooks.utils.RecommendationEngine;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView titleTextView;
    private View loadingView;
    private View errorView;
    
    private RecommendationEngine recommendationEngine;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerview_books);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        titleTextView = view.findViewById(R.id.text_title); // Make sure this ID exists in your layout
        loadingView = view.findViewById(R.id.loading_view); // Make sure this ID exists in your layout
        errorView = view.findViewById(R.id.error_view); // Make sure this ID exists in your layout
        
        // Initialize repositories and utilities
        recommendationEngine = new RecommendationEngine(requireContext());
        sessionManager = new SessionManager(requireContext());
        
        setupRecyclerView();
        setupSwipeRefresh();
        updateTitle();
        
        // Load personalized recommendations
        loadRecommendedBooks();
        
        return view;
    }
    
    private void setupRecyclerView() {
        bookAdapter = new BookAdapter(requireContext(), new ArrayList<>(), book -> {
            // Handle book click - navigate to book detail screen
            if (getActivity() != null) {
                // Navigate to book detail activity/fragment
                Intent intent = new Intent(getActivity(), BookDetailActivity.class);
                intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.getId());
                startActivity(intent);
            }
        });
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerView.setAdapter(bookAdapter);
    }
    
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadRecommendedBooks);
        swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary, 
            R.color.colorAccent, 
            R.color.colorPrimaryDark
        );
    }
    
    private void updateTitle() {
        if (titleTextView != null) {
            if (sessionManager.isLoggedIn()) {
                titleTextView.setText(R.string.recommended_books);
            } else {
                titleTextView.setText(R.string.featured_books);
            }
        }
    }
    
    private void loadRecommendedBooks() {
        showLoading();
        
        recommendationEngine.getRecommendations(new BookRepository.BookCallback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> books) {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (books != null && !books.isEmpty()) {
                            bookAdapter.updateBooks(books);
                            showContent();
                        } else {
                            showError("No se encontraron libros para recomendar");
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.e(TAG, "Error loading recommendations: " + errorMessage);
                        showError(getString(R.string.error_loading_recommendations));
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    });
                }
            }
        });
    }
    
    private void showLoading() {
        if (swipeRefreshLayout != null && !swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }
        
        if (loadingView != null) {
            loadingView.setVisibility(View.VISIBLE);
        }
        
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
        
        if (errorView != null) {
            errorView.setVisibility(View.GONE);
        }
    }
    
    private void showContent() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
        
        if (loadingView != null) {
            loadingView.setVisibility(View.GONE);
        }
        
        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
        }
        
        if (errorView != null) {
            errorView.setVisibility(View.GONE);
        }
    }
    
    private void showError(String message) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
        
        if (loadingView != null) {
            loadingView.setVisibility(View.GONE);
        }
        
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
        
        if (errorView != null) {
            errorView.setVisibility(View.VISIBLE);
            
            // Update error message text if there's a TextView in the error view
            View errorMessageView = errorView.findViewById(R.id.text_error_message);
            if (errorMessageView instanceof TextView) {
                ((TextView) errorMessageView).setText(message);
            }
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh recommendations when returning to this fragment
        // This ensures recommendations are updated if user added new favorites
        updateTitle();
    }
}
