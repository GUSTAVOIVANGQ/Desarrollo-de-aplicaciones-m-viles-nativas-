package com.example.systembooks.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.systembooks.R;
import com.example.systembooks.adapters.AdminUserDataAdapter;
import com.example.systembooks.models.SearchHistoryItem;
import com.example.systembooks.models.User;
import com.example.systembooks.repositories.SearchHistoryRepository;
import com.example.systembooks.repositories.UserRepository;
import com.example.systembooks.util.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminSearchHistoryFragment extends Fragment {

    private RecyclerView recyclerViewUsers;
    private ProgressBar progressBar;
    private TextView emptyView;
    private AdminUserDataAdapter<SearchHistoryItem> adapter;
    
    private SearchHistoryRepository searchHistoryRepository;
    private UserRepository userRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_search_history, container, false);
        
        recyclerViewUsers = view.findViewById(R.id.recyclerViewUsers);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);
        
        searchHistoryRepository = new SearchHistoryRepository(requireContext());
        userRepository = new UserRepository(requireContext());
        
        setupRecyclerView();
        loadUserSearchHistories();
        
        return view;
    }
    
    private void setupRecyclerView() {
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AdminUserDataAdapter<>(requireContext(), 
                R.layout.item_admin_search_history, 
                R.string.search_history_of);
        recyclerViewUsers.setAdapter(adapter);
    }
    
    private void loadUserSearchHistories() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        
        // Execute in background thread
        new Thread(() -> {
            Map<Long, List<SearchHistoryItem>> allUserSearchHistories = searchHistoryRepository.getAllUsersSearchHistory();
            List<User> users = userRepository.getAllUsers();
            
            // Map of user IDs to their info
            Map<Long, User> userMap = new HashMap<>();
            for (User user : users) {
                userMap.put(user.getId(), user);
            }
            
            requireActivity().runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                
                // Create user data list for adapter
                List<AdminUserDataAdapter.UserData<SearchHistoryItem>> userDataList = new ArrayList<>();
                
                for (Map.Entry<Long, List<SearchHistoryItem>> entry : allUserSearchHistories.entrySet()) {
                    long userId = entry.getKey();
                    List<SearchHistoryItem> histories = entry.getValue();
                    
                    if (histories != null && !histories.isEmpty() && userMap.containsKey(userId)) {
                        User user = userMap.get(userId);
                        userDataList.add(new AdminUserDataAdapter.UserData<>(
                                user.getId(),
                                user.getUsername(),
                                user.getEmail(),
                                histories
                        ));
                    }
                }
                
                if (userDataList.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    adapter.submitList(userDataList);
                }
            });
        }).start();
    }
}