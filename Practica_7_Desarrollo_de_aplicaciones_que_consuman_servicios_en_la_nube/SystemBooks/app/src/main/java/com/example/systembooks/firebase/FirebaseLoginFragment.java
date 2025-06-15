package com.example.systembooks.firebase;

import android.os.Bundle;
import android.text.TextUtils;
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

import com.example.systembooks.MainActivity;
import com.example.systembooks.R;
import com.example.systembooks.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class FirebaseLoginFragment extends Fragment {
    
    private static final String TAG = "FirebaseLoginFragment";
    
    private TextInputLayout layoutEmail, layoutPassword;
    private TextInputEditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private ProgressBar progressBar;
    private TextView textViewRegisterLink;
    private TextView textViewApiLoginLink;
    
    private FirebaseAuthRepository authRepository;
    private SessionManager sessionManager;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_firebase_login, container, false);
        
        // Inicializar componentes de la UI
        initializeViews(view);
        
        // Inicializar repositorios
        authRepository = new FirebaseAuthRepository(requireContext());
        sessionManager = new SessionManager(requireContext());
        
        // Configurar listeners
        setupListeners();
        
        return view;
    }
    
    private void initializeViews(View view) {
        layoutEmail = view.findViewById(R.id.layoutFbLoginEmail);
        layoutPassword = view.findViewById(R.id.layoutFbLoginPassword);
        
        editTextEmail = view.findViewById(R.id.editTextFbLoginEmail);
        editTextPassword = view.findViewById(R.id.editTextFbLoginPassword);
        
        buttonLogin = view.findViewById(R.id.buttonFbLogin);
        progressBar = view.findViewById(R.id.progressBarFbLogin);
        textViewRegisterLink = view.findViewById(R.id.textViewFbRegisterLink);
        textViewApiLoginLink = view.findViewById(R.id.textViewApiLoginLink);
    }
    
    private void setupListeners() {
        // Botón de login
        buttonLogin.setOnClickListener(v -> attemptLogin());
        
        // Enlace para ir a registro
        textViewRegisterLink.setOnClickListener(v -> navigateToRegisterFragment());
        
        // Enlace para ir a login con API
        textViewApiLoginLink.setOnClickListener(v -> navigateToApiLoginFragment());
    }
    
    private void attemptLogin() {
        // Reset errors
        layoutEmail.setError(null);
        layoutPassword.setError(null);
        
        // Obtener valores de campos
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        
        // Validar campos
        boolean cancel = false;
        View focusView = null;
        
        // Verificar email
        if (TextUtils.isEmpty(email)) {
            layoutEmail.setError(getString(R.string.error_field_required));
            focusView = editTextEmail;
            cancel = true;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError(getString(R.string.error_invalid_email));
            focusView = editTextEmail;
            cancel = true;
        }
        
        // Verificar contraseña
        if (TextUtils.isEmpty(password)) {
            layoutPassword.setError(getString(R.string.error_field_required));
            focusView = editTextPassword;
            cancel = true;
        }
        
        if (cancel) {
            // Hay errores, enfocar el primer campo con error
            if (focusView != null) {
                focusView.requestFocus();
            }
        } else {
            // Mostrar progreso
            progressBar.setVisibility(View.VISIBLE);
            
            // Realizar login
            authRepository.loginUser(email, password, 
                    new FirebaseAuthRepository.FirebaseCallback<FirebaseUser>() {
                @Override
                public void onSuccess(FirebaseUser result) {
                    progressBar.setVisibility(View.GONE);
                    
                    // Guardar datos de sesión
                    sessionManager.createFirebaseLoginSession(result);
                    
                    // Notificar éxito
                    Toast.makeText(getContext(), R.string.success_login_firebase, Toast.LENGTH_SHORT).show();
                    
                    // Redirigir a la actividad principal
                    if (getActivity() instanceof MainActivity) {
                        MainActivity activity = (MainActivity) getActivity();
                        activity.showMenuItemsByRole(result.getRole());
                        activity.loadHomeFragment();
                    }
                }
                
                @Override
                public void onError(String errorMessage) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    
    private void navigateToRegisterFragment() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new FirebaseRegisterFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }
    
    private void navigateToApiLoginFragment() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new com.example.systembooks.fragment.LoginFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }
}
