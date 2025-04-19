package com.example.systembooks.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.systembooks.R;
import com.example.systembooks.adapters.BookAdapter;
import com.example.systembooks.models.Book;
import com.example.systembooks.repositories.BookRepository;
import com.example.systembooks.repositories.SearchHistoryRepository;
import com.example.systembooks.util.SessionManager;
import com.example.systembooks.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private SearchView searchView;
    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private View loadingView;
    private View emptyView;
    private View errorView;
    private TextView errorMessage;
    private Button retryButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BookRepository bookRepository;
    private String currentQuery = "";
    
    // Añadir repositorio de historial y gestor de sesión
    private SearchHistoryRepository searchHistoryRepository;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        
        // Inicializar vistas
        searchView = view.findViewById(R.id.search_view);
        recyclerView = view.findViewById(R.id.search_results);
        loadingView = view.findViewById(R.id.loading_view);
        emptyView = view.findViewById(R.id.empty_view);
        errorView = view.findViewById(R.id.error_view);
        errorMessage = errorView.findViewById(R.id.error_message);
        retryButton = errorView.findViewById(R.id.retry_button);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        
        // Inicializar repositorio
        bookRepository = new BookRepository(requireContext());
        
        // Inicializar repositorio de historial y gestor de sesión
        searchHistoryRepository = new SearchHistoryRepository(requireContext());
        sessionManager = new SessionManager(requireContext());
        
        setupRecyclerView();
        setupSearchView();
        setupSwipeRefresh();
        setupErrorView();
        
        return view;
    }
    
    private void setupRecyclerView() {
        bookAdapter = new BookAdapter(requireContext(), new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(bookAdapter);
    }
    
    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query;
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Opcional: implementar sugerencias mientras el usuario escribe
                return false;
            }
        });
    }
    
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
        
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (!currentQuery.isEmpty()) {
                performSearch(currentQuery);
            } else {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
    
    private void setupErrorView() {
        retryButton.setOnClickListener(v -> {
            if (!currentQuery.isEmpty()) {
                performSearch(currentQuery);
            }
        });
    }

    private void performSearch(String query) {
        // Verificar conectividad a internet
        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            showErrorView(getString(R.string.error_network));
            return;
        }
        
        showLoadingView();
        
        bookRepository.searchBooks(query, 1, 20, new BookRepository.BookCallback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> result) {
                swipeRefreshLayout.setRefreshing(false);
                
                // Guardar la búsqueda en el historial si el usuario está logueado
                if (sessionManager.isLoggedIn()) {
                    // Ejecutar en un hilo separado para no bloquear la UI
                    new Thread(() -> {
                        long userId = sessionManager.getUserId();
                        searchHistoryRepository.saveSearchQuery(userId, query);
                    }).start();
                }
                
                if (result.isEmpty()) {
                    showEmptyView();
                } else {
                    bookAdapter.updateBooks(result);
                    showResultsView();
                }
            }

            @Override
            public void onError(String message) {
                swipeRefreshLayout.setRefreshing(false);
                showErrorView(message);
            }
        });
    }
    
    private void showLoadingView() {
        loadingView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
    }
    
    private void showResultsView() {
        loadingView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
    }
    
    private void showEmptyView() {
        loadingView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
    }
    
    private void showErrorView(String message) {
        loadingView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        errorMessage.setText(message);
    }
}
