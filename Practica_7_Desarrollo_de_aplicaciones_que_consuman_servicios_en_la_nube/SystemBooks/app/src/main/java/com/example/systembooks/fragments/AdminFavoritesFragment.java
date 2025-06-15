package com.example.systembooks.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.systembooks.R;
import com.example.systembooks.adapters.AdminUserDataAdapter;
import com.example.systembooks.models.FavoriteBook;
import com.example.systembooks.models.User;
import com.example.systembooks.repositories.FavoritesRepository;
import com.example.systembooks.repositories.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminFavoritesFragment extends Fragment {

    private RecyclerView recyclerViewUsers;
    private ProgressBar progressBar;
    private TextView emptyView;
    private AdminUserDataAdapter<FavoriteBook> adapter;
    
    private FavoritesRepository favoritesRepository;
    private UserRepository userRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_favorites, container, false);
        
        recyclerViewUsers = view.findViewById(R.id.recyclerViewUsers);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);
        
        favoritesRepository = new FavoritesRepository(requireContext());
        userRepository = new UserRepository(requireContext());
        
        setupRecyclerView();
        loadUserFavorites();
        
        return view;
    }
    
    private void setupRecyclerView() {
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AdminUserDataAdapter<>(requireContext(), 
                R.layout.item_admin_favorite_book, 
                R.string.favorites_of);
        recyclerViewUsers.setAdapter(adapter);
    }
    
    private void loadUserFavorites() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        
        // Execute in background thread
        new Thread(() -> {
            Map<Long, List<FavoriteBook>> allUserFavorites = favoritesRepository.getAllUsersFavorites();
            List<User> users = userRepository.getAllUsers();
            
            // Map of user IDs to their info
            Map<Long, User> userMap = new HashMap<>();
            for (User user : users) {
                userMap.put(user.getId(), user);
            }
            
            requireActivity().runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                
                // Create user data list for adapter
                List<AdminUserDataAdapter.UserData<FavoriteBook>> userDataList = new ArrayList<>();
                
                for (Map.Entry<Long, List<FavoriteBook>> entry : allUserFavorites.entrySet()) {
                    long userId = entry.getKey();
                    List<FavoriteBook> favorites = entry.getValue();
                    
                    if (favorites != null && !favorites.isEmpty() && userMap.containsKey(userId)) {
                        User user = userMap.get(userId);
                        userDataList.add(new AdminUserDataAdapter.UserData<>(
                                user.getId(),
                                user.getUsername(),
                                user.getEmail(),
                                favorites
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