package com.example.systembooks.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.systembooks.R;
import com.example.systembooks.adapters.SearchHistoryAdapter;
import com.example.systembooks.fragments.SearchFragment;
import com.example.systembooks.models.SearchHistoryItem;
import com.example.systembooks.repositories.SearchHistoryRepository;
import com.example.systembooks.util.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class SearchHistoryFragment extends Fragment implements SearchHistoryAdapter.OnHistoryItemClickListener {

    private RecyclerView recyclerViewSearchHistory;
    private TextView emptyView;
    private ProgressBar progressBar;
    private FloatingActionButton fabClearHistory;
    
    private SearchHistoryAdapter adapter;
    private SearchHistoryRepository repository;
    private SessionManager sessionManager;
    private List<SearchHistoryItem> historyItems;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_history, container, false);
        
        // Initialize views
        recyclerViewSearchHistory = view.findViewById(R.id.recyclerViewSearchHistory);
        emptyView = view.findViewById(R.id.emptyView);
        progressBar = view.findViewById(R.id.progressBar);
        fabClearHistory = view.findViewById(R.id.fabClearHistory);
        
        // Initialize repository and session manager
        repository = new SearchHistoryRepository(requireContext());
        sessionManager = new SessionManager(requireContext());
        historyItems = new ArrayList<>();
        
        setupRecyclerView();
        setupFabButton();
        
        // Load search history
        loadSearchHistory();
        
        return view;
    }
    
    private void setupRecyclerView() {
        adapter = new SearchHistoryAdapter(requireContext(), historyItems, this);
        recyclerViewSearchHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewSearchHistory.setAdapter(adapter);
    }
    
    private void setupFabButton() {
        fabClearHistory.setOnClickListener(v -> showClearHistoryConfirmationDialog());
    }
    
    private void loadSearchHistory() {
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
        
        // Load search history in a background thread
        new Thread(() -> {
            List<SearchHistoryItem> items = repository.getSearchHistory(userId);
            
            // Update UI on the main thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (items.isEmpty()) {
                        showEmptyState();
                    } else {
                        showSearchHistory(items);
                    }
                });
            }
        }).start();
    }
    
    private void showSearchHistory(List<SearchHistoryItem> items) {
        recyclerViewSearchHistory.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        adapter.updateHistoryItems(items);
    }
    
    private void showEmptyState() {
        recyclerViewSearchHistory.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }
    
    private void showClearHistoryConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.clear_search_history)
                .setMessage(R.string.clear_search_history_confirmation)
                .setPositiveButton(R.string.yes, (dialog, which) -> clearSearchHistory())
                .setNegativeButton(R.string.no, null)
                .show();
    }
    
    private void clearSearchHistory() {
        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);
        
        // Get current user ID
        Long userId = sessionManager.getUserId();
        
        // Clear search history in a background thread
        new Thread(() -> {
            repository.clearSearchHistory(userId);
            
            // Update UI on the main thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    historyItems.clear();
                    adapter.notifyDataSetChanged();
                    showEmptyState();
                });
            }
        }).start();
    }

    @Override
    public void onHistoryItemClick(String query) {
        // Navigate to search fragment with the selected query
        SearchFragment searchFragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString("query", query);
        searchFragment.setArguments(args);
        
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, searchFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}