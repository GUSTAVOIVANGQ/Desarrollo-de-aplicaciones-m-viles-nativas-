package com.example.systembooks.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.systembooks.MainActivity;
import com.example.systembooks.R;
import com.example.systembooks.model.LoginResponse;
import com.example.systembooks.repository.ApiRepository;
import com.example.systembooks.util.RoleManager;
import com.google.android.material.textfield.TextInputEditText;

public class LoginFragment extends Fragment {
    
    private static final String TAG = "LoginFragment";
    
    private TextInputEditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private ProgressBar progressBar;
    private TextView textViewRegisterLink;
    
    private ApiRepository apiRepository;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        
        // Inicializar componentes de la UI
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        buttonLogin = view.findViewById(R.id.buttonLogin);
        progressBar = view.findViewById(R.id.progressBar);
        textViewRegisterLink = view.findViewById(R.id.textViewRegisterLink);
        
        // Inicializar ApiRepository
        apiRepository = new ApiRepository(requireContext());
        
        // Configurar listener del botón de login
        buttonLogin.setOnClickListener(v -> attemptLogin());
        
        // Configurar el enlace para ir a la pantalla de registro
        textViewRegisterLink.setOnClickListener(v -> navigateToRegister());
        
        return view;
    }
    
    private void attemptLogin() {
        // Obtener valores de los campos
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        
        // Validar que los campos no estén vacíos
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Mostrar progreso
        progressBar.setVisibility(View.VISIBLE);
        
        // Llamar al repositorio para realizar el login
        apiRepository.login(email, password, new ApiRepository.ApiCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse result) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), R.string.success_login, Toast.LENGTH_SHORT).show();
                
                // Cargar el fragmento adecuado según el rol del usuario
                if (getActivity() != null) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    String role = result.getRole();
                    Log.d(TAG, "Login successful with role: " + role);
                    mainActivity.showMenuItemsByRole(role);
                    
                    // Navegar al fragmento adecuado según el rol
                    Fragment fragmentToLoad;
                    if (role.equals(RoleManager.ROLE_ADMIN)) {
                        fragmentToLoad = new UserManagementFragment();
                        Toast.makeText(getContext(), R.string.admin_welcome, Toast.LENGTH_SHORT).show();
                    } else if (role.equals(RoleManager.ROLE_USER)) {
                        fragmentToLoad = new ProfileFragment();
                    } else {
                        // If we get an unexpected role, default to profile view
                        fragmentToLoad = new ProfileFragment();
                        Log.w(TAG, "Unexpected role received: " + role);
                    }
                    
                    mainActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, fragmentToLoad)
                            .commit();
                }
            }
            
            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error en login: " + errorMessage);
            }
        });
    }
    
    private void navigateToRegister() {
        // Reemplazar este fragmento con el fragmento de registro
        if (getActivity() != null) {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new RegisterFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }
}
