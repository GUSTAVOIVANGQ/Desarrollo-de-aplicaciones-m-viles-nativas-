package com.example.systembooks.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.systembooks.R;
import com.example.systembooks.adapter.FirebaseUserAdapter;
import com.example.systembooks.adapter.UserAdapter;
import com.example.systembooks.firebase.FirebaseUser;
import com.example.systembooks.firebase.FirebaseUserRepository;
import com.example.systembooks.model.User;
import com.example.systembooks.repository.ApiRepository;
import com.example.systembooks.util.RoleManager;
import com.example.systembooks.util.ActivityTracker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class UserManagementFragment extends Fragment implements UserAdapter.OnUserClickListener, FirebaseUserAdapter.OnFirebaseUserClickListener {

    private static final String TAG = "UserManagementFragment";
    
    // UI Components
    private RecyclerView recyclerViewUsers;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddUser;
    private Button buttonToggleSource;
    private TextInputEditText editTextSearch;

    // Adapters
    private UserAdapter userAdapter;
    private FirebaseUserAdapter firebaseUserAdapter;
      // Repositories
    private ApiRepository apiRepository;
    private FirebaseUserRepository firebaseUserRepository;
    private RoleManager roleManager;
    private ActivityTracker activityTracker;
    
    // Data
    private List<User> apiUserList;
    private List<FirebaseUser> firebaseUserList;
    
    // State
    private boolean isShowingFirebaseUsers = false;    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_management, container, false);

        // Initialize UI components
        initializeViews(view);
        
        // Initialize repositories
        initializeRepositories();
        
        // Check admin permissions
        if (!roleManager.isAdmin()) {
            redirectToAccessDenied();
            return view;
        }

        // Setup components
        setupRecyclerView();
        setupListeners();

        // Load initial data (API users by default)
        loadApiUsers();

        return view;
    }
    
    private void initializeViews(View view) {
        recyclerViewUsers = view.findViewById(R.id.recyclerViewUsers);
        progressBar = view.findViewById(R.id.progressBarUsers);
        fabAddUser = view.findViewById(R.id.fabAddUser);
        buttonToggleSource = view.findViewById(R.id.buttonToggleSource);
        editTextSearch = view.findViewById(R.id.editTextSearch);
    }
      private void initializeRepositories() {
        apiRepository = new ApiRepository(requireContext());
        firebaseUserRepository = new FirebaseUserRepository(requireContext());
        roleManager = new RoleManager(requireContext());
        activityTracker = ActivityTracker.getInstance(requireContext());
        apiUserList = new ArrayList<>();
        firebaseUserList = new ArrayList<>();
    }
    
    private void redirectToAccessDenied() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AccessDeniedFragment())
                    .commit();
        }
    }
    
    private void setupListeners() {
        // Toggle button listener
        buttonToggleSource.setOnClickListener(v -> toggleUserSource());
        
        // FAB listener
        fabAddUser.setOnClickListener(v -> navigateToRegisterFragment());
        
        // Search listener
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (isShowingFirebaseUsers) {
                    searchFirebaseUsers(query);
                } else {
                    searchApiUsers(query);
                }
            }
        });
    }
    
    private void toggleUserSource() {
        isShowingFirebaseUsers = !isShowingFirebaseUsers;
        
        if (isShowingFirebaseUsers) {
            buttonToggleSource.setText("Firebase Users");
            setupFirebaseAdapter();
            loadFirebaseUsers();
        } else {
            buttonToggleSource.setText("API Users");
            setupApiAdapter();
            loadApiUsers();
        }
        
        // Clear search when switching
        editTextSearch.setText("");
    }    private void setupRecyclerView() {
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        setupApiAdapter(); // Start with API adapter by default
    }
    
    private void setupApiAdapter() {
        userAdapter = new UserAdapter(getContext(), apiUserList, this);
        recyclerViewUsers.setAdapter(userAdapter);
    }
    
    private void setupFirebaseAdapter() {
        firebaseUserAdapter = new FirebaseUserAdapter(getContext(), firebaseUserList, this);
        recyclerViewUsers.setAdapter(firebaseUserAdapter);
    }    private void loadApiUsers() {
        progressBar.setVisibility(View.VISIBLE);

        apiRepository.getAllUsers(new ApiRepository.ApiCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        apiUserList = result;
                        if (userAdapter != null) {
                            userAdapter.updateUsers(apiUserList);
                        }

                        if (apiUserList.isEmpty()) {
                            Toast.makeText(getContext(), "No API users found", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error loading API users: " + errorMessage, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error loading API users: " + errorMessage);
                    });
                }
            }
        });
    }
    
    private void loadFirebaseUsers() {
        progressBar.setVisibility(View.VISIBLE);

        firebaseUserRepository.getAllUsers(new FirebaseUserRepository.FirebaseCallback<List<FirebaseUser>>() {
            @Override
            public void onSuccess(List<FirebaseUser> result) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        firebaseUserList = result;
                        if (firebaseUserAdapter != null) {
                            firebaseUserAdapter.updateUsers(firebaseUserList);
                        }

                        if (firebaseUserList.isEmpty()) {
                            Toast.makeText(getContext(), "No Firebase users found", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error loading Firebase users: " + errorMessage, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error loading Firebase users: " + errorMessage);
                    });
                }
            }
        });
    }
    
    private void searchApiUsers(String query) {
        if (query.isEmpty()) {
            if (userAdapter != null) {
                userAdapter.updateUsers(apiUserList);
            }
            return;
        }

        List<User> filteredUsers = new ArrayList<>();
        for (User user : apiUserList) {
            if ((user.getName() != null && user.getName().toLowerCase().contains(query.toLowerCase())) ||
                (user.getEmail() != null && user.getEmail().toLowerCase().contains(query.toLowerCase()))) {
                filteredUsers.add(user);
            }
        }

        if (userAdapter != null) {
            userAdapter.updateUsers(filteredUsers);
        }
    }
    
    private void searchFirebaseUsers(String query) {
        if (query.isEmpty()) {
            if (firebaseUserAdapter != null) {
                firebaseUserAdapter.updateUsers(firebaseUserList);
            }
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        firebaseUserRepository.searchUsers(query, new FirebaseUserRepository.FirebaseCallback<List<FirebaseUser>>() {
            @Override
            public void onSuccess(List<FirebaseUser> result) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        if (firebaseUserAdapter != null) {
                            firebaseUserAdapter.updateUsers(result);
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Search error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }    // API User Adapter Callbacks
    @Override
    public void onEditClick(User user) {
        navigateToEditUserFragment(user);
    }

    @Override
    public void onDeleteClick(User user) {
        showDeleteApiUserConfirmationDialog(user);
    }

    // Firebase User Adapter Callbacks
    @Override
    public void onEditClick(FirebaseUser user) {
        navigateToEditFirebaseUserFragment(user);
    }

    @Override
    public void onDeleteClick(FirebaseUser user) {
        showDeleteFirebaseUserConfirmationDialog(user);
    }

    @Override
    public void onRoleClick(FirebaseUser user) {
        showChangeRoleDialog(user);
    }    private void navigateToRegisterFragment() {
        if (getActivity() != null) {
            if (isShowingFirebaseUsers) {
                // Navigate to Firebase register
                com.example.systembooks.firebase.FirebaseRegisterFragment registerFragment = 
                        new com.example.systembooks.firebase.FirebaseRegisterFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, registerFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            } else {
                // Navigate to API register
                RegisterFragment registerFragment = new RegisterFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, registerFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        }
    }

    private void navigateToEditUserFragment(User user) {
        if (getActivity() != null) {
            Bundle bundle = new Bundle();
            bundle.putLong("userId", user.getId());
            
            ProfileFragment profileFragment = new ProfileFragment();
            profileFragment.setArguments(bundle);
            
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, profileFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }
    
    private void navigateToEditFirebaseUserFragment(FirebaseUser user) {
        if (getActivity() != null) {
            Bundle bundle = new Bundle();
            bundle.putString("userId", user.getUid());
            bundle.putBoolean("isFirebaseUser", true);
            
            com.example.systembooks.firebase.FirebaseProfileFragment profileFragment = 
                    new com.example.systembooks.firebase.FirebaseProfileFragment();
            profileFragment.setArguments(bundle);
            
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, profileFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }    private void showDeleteApiUserConfirmationDialog(User user) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete API User")
                .setMessage("Are you sure you want to delete " + user.getName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> deleteApiUser(user))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
    
    private void showDeleteFirebaseUserConfirmationDialog(FirebaseUser user) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Firebase User")
                .setMessage("Are you sure you want to delete " + user.getUsername() + "?\n\nNote: This will only remove the user from Firestore, not from Firebase Authentication.")
                .setPositiveButton("Yes", (dialog, which) -> deleteFirebaseUser(user))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
    
    private void showChangeRoleDialog(FirebaseUser user) {
        String currentRole = user.getRole();
        String newRole = FirebaseUser.ROLE_ADMIN.equals(currentRole) ? 
                FirebaseUser.ROLE_USER : FirebaseUser.ROLE_ADMIN;
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Change User Role")
                .setMessage("Change " + user.getUsername() + "'s role from " + currentRole + " to " + newRole + "?")
                .setPositiveButton("Yes", (dialog, which) -> changeUserRole(user, newRole))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteApiUser(User user) {
        progressBar.setVisibility(View.VISIBLE);

        apiRepository.deleteUser(user.getId(), new ApiRepository.ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (getActivity() != null && isAdded()) {                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "API User deleted successfully", Toast.LENGTH_SHORT).show();
                        
                        // Track user deletion activity
                        activityTracker.trackUserDeletion(String.valueOf(user.getId()), user.getUsername());
                        
                        // Reload API users
                        loadApiUsers();
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error deleting API user", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error deleting API user: " + errorMessage);
                    });
                }
            }
        });
    }
    
    private void deleteFirebaseUser(FirebaseUser user) {
        progressBar.setVisibility(View.VISIBLE);

        firebaseUserRepository.deleteUser(user.getUid(), new FirebaseUserRepository.FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (getActivity() != null && isAdded()) {                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Firebase user deleted successfully", Toast.LENGTH_SHORT).show();
                        
                        // Track user deletion activity
                        activityTracker.trackUserDeletion(user.getUid(), user.getUsername());
                        
                        // Reload Firebase users
                        loadFirebaseUsers();
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error deleting Firebase user: " + errorMessage, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error deleting Firebase user: " + errorMessage);
                    });
                }
            }
        });
    }
    
    private void changeUserRole(FirebaseUser user, String newRole) {
        progressBar.setVisibility(View.VISIBLE);

        firebaseUserRepository.updateUserRole(user.getUid(), newRole, new FirebaseUserRepository.FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (getActivity() != null && isAdded()) {                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "User role updated successfully", Toast.LENGTH_SHORT).show();
                        
                        // Track role change activity
                        String oldRole = user.getRole();
                        activityTracker.trackRoleChange(user.getUid(), user.getUsername(), oldRole, newRole);
                        
                        // Reload Firebase users to reflect changes
                        loadFirebaseUsers();
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error updating user role: " + errorMessage, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error updating user role: " + errorMessage);
                    });
                }
            }
        });
    }
}
