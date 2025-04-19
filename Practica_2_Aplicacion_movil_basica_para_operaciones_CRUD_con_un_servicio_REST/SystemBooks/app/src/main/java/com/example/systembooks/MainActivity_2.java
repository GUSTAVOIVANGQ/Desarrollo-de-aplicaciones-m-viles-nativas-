package com.example.systembooks;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.systembooks.fragments.FavoritesFragment;
import com.example.systembooks.fragments.HomeFragment;
import com.example.systembooks.fragments.SearchFragment;
import com.example.systembooks.fragments.CategoriesFragment;
import com.example.systembooks.fragments.SearchHistoryFragment;
import com.example.systembooks.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity_2 extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_2);
        
        sessionManager = new SessionManager(this);
        
        setupWindowInsets();
        setupBottomNavigation();
        
        // Load home fragment by default
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }
    
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    
    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (itemId == R.id.nav_search) {
                fragment = new SearchFragment();
            } else if (itemId == R.id.nav_categories) {
                fragment = new CategoriesFragment();
            } else if (itemId == R.id.nav_history) {
                if (sessionManager.isLoggedIn()) {
                    fragment = new SearchHistoryFragment();
                } else {
                    // Show login message or redirect to login
                    showLoginRequired();
                    return false;
                }
            } else if (itemId == R.id.nav_favorites) {
                if (sessionManager.isLoggedIn()) {
                    fragment = new FavoritesFragment();
                } else {
                    // Show login message or redirect to login
                    showLoginRequired();
                    return false;
                }
            }
            
            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }
    
    private void showLoginRequired() {
        // Show message that login is required to access this feature
        android.widget.Toast.makeText(this, R.string.login_required, Toast.LENGTH_SHORT).show();
        
        // Keep current selection
        int currentItemId = bottomNavigationView.getSelectedItemId();
        bottomNavigationView.setSelectedItemId(currentItemId);
    }
    
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}