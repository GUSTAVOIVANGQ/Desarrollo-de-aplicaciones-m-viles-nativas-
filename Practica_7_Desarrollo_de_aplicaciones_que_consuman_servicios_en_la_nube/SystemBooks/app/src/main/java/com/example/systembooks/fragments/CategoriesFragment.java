package com.example.systembooks.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.systembooks.R;
import com.example.systembooks.adapters.CategoryAdapter;
import com.example.systembooks.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoriesFragment extends Fragment {

    private RecyclerView recyclerView;
    private CategoryAdapter categoryAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);
        
        recyclerView = view.findViewById(R.id.recyclerview_categories);
        
        setupRecyclerView();
        loadCategories();
        
        return view;
    }
    
    private void setupRecyclerView() {
        categoryAdapter = new CategoryAdapter(requireContext(), new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(categoryAdapter);
    }
    
    private void loadCategories() {
        // This will be replaced with actual API calls in functionality 2
        // For now, populate with static categories
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("Fiction", "fiction"));
        categories.add(new Category("Science Fiction", "science_fiction"));
        categories.add(new Category("Fantasy", "fantasy"));
        categories.add(new Category("Mystery", "mystery"));
        categories.add(new Category("Romance", "romance"));
        categories.add(new Category("Thriller", "thriller"));
        categories.add(new Category("Biography", "biography"));
        categories.add(new Category("History", "history"));
        categories.add(new Category("Children", "children"));
        
        categoryAdapter.updateCategories(categories);
    }
}
