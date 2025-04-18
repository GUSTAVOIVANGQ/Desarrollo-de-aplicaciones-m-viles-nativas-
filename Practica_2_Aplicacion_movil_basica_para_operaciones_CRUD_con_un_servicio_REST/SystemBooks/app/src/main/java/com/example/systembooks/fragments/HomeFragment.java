package com.example.systembooks.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.systembooks.R;
import com.example.systembooks.adapters.BookAdapter;
import com.example.systembooks.models.Book;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        recyclerView = view.findViewById(R.id.recyclerview_books);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        
        setupRecyclerView();
        setupSwipeRefresh();
        
        // Load featured books
        loadFeaturedBooks();
        
        return view;
    }
    
    private void setupRecyclerView() {
        bookAdapter = new BookAdapter(requireContext(), new ArrayList<>());
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerView.setAdapter(bookAdapter);
    }
    
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadFeaturedBooks);
    }
    
    private void loadFeaturedBooks() {
        // This will be replaced with actual API calls in functionality 2
        // Simulate loading for now
        swipeRefreshLayout.setRefreshing(true);
        
        // Dummy data for UI testing
        List<Book> dummyBooks = new ArrayList<>();
        // Add some dummy books
        dummyBooks.add(new Book("OL1234567M", "Cien Años de Soledad", "Gabriel García Márquez"));
        dummyBooks.add(new Book("OL7654321M", "El Principito", "Antoine de Saint-Exupéry"));
        dummyBooks.add(new Book("OL2468135M", "Don Quijote de la Mancha", "Miguel de Cervantes"));
        dummyBooks.add(new Book("OL1357924M", "Harry Potter y la Piedra Filosofal", "J.K. Rowling"));
        dummyBooks.add(new Book("OL8642975M", "El Señor de los Anillos", "J.R.R. Tolkien"));
        dummyBooks.add(new Book("OL9753124M", "1984", "George Orwell"));
        
        // Opcional: Agregar imágenes de portada para estos libros de ejemplo
        dummyBooks.get(0).setCoverUrl("https://covers.openlibrary.org/b/id/8302553-M.jpg");
        dummyBooks.get(1).setCoverUrl("https://covers.openlibrary.org/b/id/12224315-M.jpg");
        dummyBooks.get(2).setCoverUrl("https://covers.openlibrary.org/b/id/8127804-M.jpg");
        dummyBooks.get(3).setCoverUrl("https://covers.openlibrary.org/b/id/10523521-M.jpg");
        dummyBooks.get(4).setCoverUrl("https://covers.openlibrary.org/b/id/12559555-M.jpg");
        dummyBooks.get(5).setCoverUrl("https://covers.openlibrary.org/b/id/12083218-M.jpg");
        
        // Update the adapter after a delay to simulate network call
        recyclerView.postDelayed(() -> {
            bookAdapter.updateBooks(dummyBooks);
            swipeRefreshLayout.setRefreshing(false);
        }, 1500);
    }
}
