package com.example.systembooks.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.systembooks.R;
import com.example.systembooks.activities.SearchResultsActivity;
import com.example.systembooks.adapters.SearchHistoryAdapter;
import com.example.systembooks.models.SearchHistoryItem;
import com.example.systembooks.repositories.SearchHistoryRepository;
import com.example.systembooks.util.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class SearchHistoryFragment extends Fragment implements SearchHistoryAdapter.OnHistoryItemClickListener {

    private RecyclerView recyclerView;
    private TextView emptyView;
    private ProgressBar progressBar;
    private FloatingActionButton fabClearHistory;

    private SearchHistoryAdapter adapter;
    private SearchHistoryRepository historyRepository;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_history, container, false);
        
        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewSearchHistory);
        emptyView = view.findViewById(R.id.emptyView);
        progressBar = view.findViewById(R.id.progressBar);
        fabClearHistory = view.findViewById(R.id.fabClearHistory);
        
        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SearchHistoryAdapter(getContext(), new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
        
        // Initialize repositories and session manager
        historyRepository = new SearchHistoryRepository(getContext());
        sessionManager = new SessionManager(getContext());
        
        // Setup clear history button
        fabClearHistory.setOnClickListener(v -> showClearHistoryConfirmationDialog());
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSearchHistory();
    }

    private void loadSearchHistory() {
        if (!sessionManager.isLoggedIn()) {
            showEmptyView();
            return;
        }
        
        showLoading();
        
        new Thread(() -> {
            final List<SearchHistoryItem> historyItems = historyRepository.getSearchHistory(sessionManager.getUserId());
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (historyItems != null && !historyItems.isEmpty()) {
                        adapter.updateHistoryItems(historyItems);
                        showContent();
                    } else {
                        showEmptyView();
                    }
                });
            }
        }).start();
    }
    
    private void clearSearchHistory() {
        if (!sessionManager.isLoggedIn()) return;
        
        showLoading();
        
        new Thread(() -> {
            final boolean success = historyRepository.clearSearchHistory(sessionManager.getUserId());
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (success) {
                        adapter.updateHistoryItems(new ArrayList<>());
                        showEmptyView();
                        Toast.makeText(getContext(), "Historial de búsqueda borrado", Toast.LENGTH_SHORT).show();
                    } else {
                        showContent();
                        Toast.makeText(getContext(), "Error al borrar el historial", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }
    
    private void showClearHistoryConfirmationDialog() {
        if (getContext() == null) return;
        
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.clear_search_history)
                .setMessage(R.string.clear_search_history_confirmation)
                .setPositiveButton(R.string.yes, (dialog, which) -> clearSearchHistory())
                .setNegativeButton(R.string.no, null)
                .show();
    }
    
    @Override
    public void onHistoryItemClick(String query) {
        // Execute the search query when a history item is clicked
        Intent intent = new Intent(getActivity(), SearchResultsActivity.class);
        intent.putExtra(SearchResultsActivity.EXTRA_SEARCH_QUERY, query);
        startActivity(intent);
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
        fabClearHistory.setVisibility(View.VISIBLE);
    }
    
    private void showEmptyView() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        fabClearHistory.setVisibility(View.GONE);
    }
}