package com.example.systembooks.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.systembooks.R;
import com.example.systembooks.adapter.DashboardActivityAdapter;
import com.example.systembooks.firebase.DashboardActivityRepository;
import com.example.systembooks.models.DashboardActivity;
import com.example.systembooks.util.RoleManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

/**
 * Fragment displaying real-time admin dashboard with user activities
 */
public class AdminDashboardFragment extends Fragment {
    
    private static final String TAG = "AdminDashboardFragment";
    private static final int DEFAULT_ACTIVITIES_LIMIT = 50;
    
    // UI Components
    private RecyclerView recyclerViewActivities;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView textViewEmpty;
    private TextView textViewTitle;
    private TextView textViewSubtitle;
    private ChipGroup chipGroupFilters;
    private Button buttonClearFilters;
    
    // Adapter and data
    private DashboardActivityAdapter activityAdapter;
    private DashboardActivityRepository activityRepository;
    private RoleManager roleManager;
    
    // Current filter
    private String currentFilter = null;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
        
        // Initialize components
        initializeViews(view);
        initializeRepositories();
        
        // Check admin permissions
        if (!roleManager.isAdmin()) {
            redirectToAccessDenied();
            return view;
        }
        
        // Setup components
        setupRecyclerView();
        setupSwipeRefresh();
        setupFilters();
        setupListeners();
        
        // Load initial data
        loadActivities();
        
        return view;
    }
    
    private void initializeViews(View view) {
        recyclerViewActivities = view.findViewById(R.id.recyclerViewDashboardActivities);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshDashboard);
        progressBar = view.findViewById(R.id.progressBarDashboard);
        textViewEmpty = view.findViewById(R.id.textViewDashboardEmpty);
        textViewTitle = view.findViewById(R.id.textViewDashboardTitle);
        textViewSubtitle = view.findViewById(R.id.textViewDashboardSubtitle);
        chipGroupFilters = view.findViewById(R.id.chipGroupDashboardFilters);
        buttonClearFilters = view.findViewById(R.id.buttonClearFilters);
    }
    
    private void initializeRepositories() {
        activityRepository = new DashboardActivityRepository(requireContext());
        roleManager = new RoleManager(requireContext());
    }
    
    private void redirectToAccessDenied() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AccessDeniedFragment())
                    .commit();
        }
    }
    
    private void setupRecyclerView() {
        activityAdapter = new DashboardActivityAdapter(requireContext());
        recyclerViewActivities.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewActivities.setAdapter(activityAdapter);
    }
    
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent
        );
        
        swipeRefreshLayout.setOnRefreshListener(this::refreshActivities);
    }
    
    private void setupFilters() {
        // Add filter chips
        addFilterChip("Todos", null);
        addFilterChip("Registros", DashboardActivity.TYPE_USER_REGISTERED);
        addFilterChip("Inicios de sesión", DashboardActivity.TYPE_USER_LOGIN);
        addFilterChip("Actualizaciones", DashboardActivity.TYPE_USER_UPDATED);
        addFilterChip("Eliminaciones", DashboardActivity.TYPE_USER_DELETED);
        addFilterChip("Cambios de rol", DashboardActivity.TYPE_ROLE_CHANGED);
        addFilterChip("Notificaciones", DashboardActivity.TYPE_NOTIFICATION_SENT);
        addFilterChip("Búsquedas", DashboardActivity.TYPE_BOOK_SEARCHED);
        addFilterChip("Perfiles", DashboardActivity.TYPE_PROFILE_UPDATED);
    }
    
    private void addFilterChip(String text, String filterType) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCheckable(true);
        chip.setChecked(filterType == null); // "Todos" selected by default
        
        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Uncheck other chips
                for (int i = 0; i < chipGroupFilters.getChildCount(); i++) {
                    Chip otherChip = (Chip) chipGroupFilters.getChildAt(i);
                    if (otherChip != chip) {
                        otherChip.setChecked(false);
                    }
                }
                
                currentFilter = filterType;
                loadActivities();
            }
        });
        
        chipGroupFilters.addView(chip);
    }
    
    private void setupListeners() {
        buttonClearFilters.setOnClickListener(v -> clearFilters());
    }
    
    private void clearFilters() {
        currentFilter = null;
        
        // Check "Todos" chip
        if (chipGroupFilters.getChildCount() > 0) {
            Chip firstChip = (Chip) chipGroupFilters.getChildAt(0);
            firstChip.setChecked(true);
        }
        
        loadActivities();
    }
    
    private void loadActivities() {
        showLoading(true);
        
        if (currentFilter == null) {
            // Load all recent activities
            activityRepository.getRecentActivities(DEFAULT_ACTIVITIES_LIMIT, new DashboardActivityRepository.ActivityCallback() {
                @Override
                public void onSuccess(List<DashboardActivity> activities) {
                    handleActivitiesLoaded(activities);
                }
                
                @Override
                public void onError(String errorMessage) {
                    handleLoadError(errorMessage);
                }
            });
        } else {
            // Load activities by type
            activityRepository.getActivitiesByType(currentFilter, DEFAULT_ACTIVITIES_LIMIT, new DashboardActivityRepository.ActivityCallback() {
                @Override
                public void onSuccess(List<DashboardActivity> activities) {
                    handleActivitiesLoaded(activities);
                }
                
                @Override
                public void onError(String errorMessage) {
                    handleLoadError(errorMessage);
                }
            });
        }
    }
    
    private void refreshActivities() {
        loadActivities();
    }
    
    private void handleActivitiesLoaded(List<DashboardActivity> activities) {
        if (getActivity() != null && isAdded()) {
            getActivity().runOnUiThread(() -> {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                
                if (activities.isEmpty()) {
                    showEmptyState(true);
                } else {
                    showEmptyState(false);
                    activityAdapter.updateActivities(activities);
                }
                
                updateSubtitle(activities.size());
            });
        }
    }
    
    private void handleLoadError(String errorMessage) {
        if (getActivity() != null && isAdded()) {
            getActivity().runOnUiThread(() -> {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                
                Toast.makeText(getContext(), "Error al cargar actividades: " + errorMessage, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading activities: " + errorMessage);
                
                showEmptyState(true);
            });
        }
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewActivities.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    private void showEmptyState(boolean show) {
        textViewEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewActivities.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    private void updateSubtitle(int count) {
        String subtitle;
        if (currentFilter == null) {
            subtitle = "Mostrando " + count + " actividades recientes";
        } else {
            subtitle = "Mostrando " + count + " actividades filtradas";
        }
        textViewSubtitle.setText(subtitle);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload activities when fragment becomes visible
        if (activityRepository != null) {
            loadActivities();
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop listening to real-time updates
        if (activityRepository != null) {
            activityRepository.stopListening();
            activityRepository.cleanup();
        }
    }
}
