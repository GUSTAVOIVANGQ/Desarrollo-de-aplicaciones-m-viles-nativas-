package com.example.systembooks.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.systembooks.R;
import com.example.systembooks.adapter.UserAdapter;
import com.example.systembooks.model.User;
import com.example.systembooks.repository.ApiRepository;
import com.example.systembooks.util.RoleManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class UserManagementFragment extends Fragment implements UserAdapter.OnUserClickListener {

    private static final String TAG = "UserManagementFragment";

    private RecyclerView recyclerViewUsers;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddUser;

    private UserAdapter userAdapter;
    private ApiRepository apiRepository;
    private RoleManager roleManager;
    private List<User> userList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_management, container, false);

        // Inicializar componentes de UI
        recyclerViewUsers = view.findViewById(R.id.recyclerViewUsers);
        progressBar = view.findViewById(R.id.progressBarUsers);
        fabAddUser = view.findViewById(R.id.fabAddUser);

        // Inicializar repository y role manager
        apiRepository = new ApiRepository(requireContext());
        roleManager = new RoleManager(requireContext());
        userList = new ArrayList<>();
        
        // Verificar si el usuario es administrador
        if (!roleManager.isAdmin()) {
            // Si no es administrador, redirigir a la pantalla de acceso denegado
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AccessDeniedFragment())
                        .commit();
                return view;
            }
        }

        // Configurar RecyclerView
        setupRecyclerView();

        // Configurar FAB para agregar usuario
        fabAddUser.setOnClickListener(v -> navigateToRegisterFragment());

        // Cargar usuarios
        loadUsers();

        return view;
    }

    private void setupRecyclerView() {
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        userAdapter = new UserAdapter(getContext(), userList, this);
        recyclerViewUsers.setAdapter(userAdapter);
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);

        apiRepository.getAllUsers(new ApiRepository.ApiCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        userList = result;
                        userAdapter.updateUsers(userList);

                        if (userList.isEmpty()) {
                            Toast.makeText(getContext(), R.string.no_users_found, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error al cargar usuarios: " + errorMessage);
                    });
                }
            }
        });
    }

    @Override
    public void onEditClick(User user) {
        navigateToEditUserFragment(user);
    }

    @Override
    public void onDeleteClick(User user) {
        showDeleteConfirmationDialog(user);
    }

    private void navigateToRegisterFragment() {
        if (getActivity() != null) {
            RegisterFragment registerFragment = new RegisterFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, registerFragment);
            transaction.addToBackStack(null);
            transaction.commit();
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

    private void showDeleteConfirmationDialog(User user) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_user)
                .setMessage(R.string.delete_confirmation)
                .setPositiveButton(R.string.yes, (dialog, which) -> deleteUser(user))
                .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteUser(User user) {
        progressBar.setVisibility(View.VISIBLE);

        apiRepository.deleteUser(user.getId(), new ApiRepository.ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), R.string.user_deleted, Toast.LENGTH_SHORT).show();
                        
                        // Recargar la lista de usuarios
                        loadUsers();
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), R.string.error_deleting_user, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error al eliminar usuario: " + errorMessage);
                    });
                }
            }
        });
    }
}
