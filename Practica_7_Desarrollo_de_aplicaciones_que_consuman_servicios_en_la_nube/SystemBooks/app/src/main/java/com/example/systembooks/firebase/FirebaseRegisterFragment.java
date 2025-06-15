package com.example.systembooks.firebase;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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

public class FirebaseRegisterFragment extends Fragment {
    
    private static final String TAG = "FirebaseRegisterFragment";
    
    private TextInputLayout layoutName, layoutEmail, layoutPassword, layoutConfirmPassword;
    private TextInputEditText editTextName, editTextEmail, editTextPassword, editTextConfirmPassword;
    private EditText editTextAdminPassword;
    private CheckBox checkBoxAdmin;
    private Button buttonRegister;
    private ProgressBar progressBar;
    private TextView textViewLoginLink;
    
    private FirebaseAuthRepository authRepository;
    private SessionManager sessionManager;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_firebase_register, container, false);
        
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
        layoutName = view.findViewById(R.id.layoutFbRegisterName);
        layoutEmail = view.findViewById(R.id.layoutFbRegisterEmail);
        layoutPassword = view.findViewById(R.id.layoutFbRegisterPassword);
        layoutConfirmPassword = view.findViewById(R.id.layoutFbRegisterConfirmPassword);
        
        editTextName = view.findViewById(R.id.editTextFbRegisterName);
        editTextEmail = view.findViewById(R.id.editTextFbRegisterEmail);
        editTextPassword = view.findViewById(R.id.editTextFbRegisterPassword);
        editTextConfirmPassword = view.findViewById(R.id.editTextFbRegisterConfirmPassword);
        editTextAdminPassword = view.findViewById(R.id.editTextFbAdminPassword);
        
        checkBoxAdmin = view.findViewById(R.id.checkBoxAdmin);
        buttonRegister = view.findViewById(R.id.buttonFbRegister);
        progressBar = view.findViewById(R.id.progressBarFbRegister);
        textViewLoginLink = view.findViewById(R.id.textViewFbLoginLink);
    }
    
    private void setupListeners() {
        // Mostrar/ocultar campo de contraseña de admin
        checkBoxAdmin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editTextAdminPassword.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        
        // Botón de registro
        buttonRegister.setOnClickListener(v -> attemptRegister());
        
        // Enlace para ir a login
        textViewLoginLink.setOnClickListener(v -> navigateToLoginFragment());
    }
    
    private void attemptRegister() {
        // Reset errors
        layoutName.setError(null);
        layoutEmail.setError(null);
        layoutPassword.setError(null);
        layoutConfirmPassword.setError(null);
        
        // Obtener valores de campos
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String adminPassword = null;
        
        // Validar campos
        boolean cancel = false;
        View focusView = null;
        
        // Verificar nombre
        if (TextUtils.isEmpty(name)) {
            layoutName.setError(getString(R.string.error_field_required));
            focusView = editTextName;
            cancel = true;
        }
        
        // Verificar email
        if (TextUtils.isEmpty(email)) {
            layoutEmail.setError(getString(R.string.error_field_required));
            focusView = editTextEmail;
            cancel = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError(getString(R.string.error_invalid_email));
            focusView = editTextEmail;
            cancel = true;
        }
        
        // Verificar contraseña
        if (TextUtils.isEmpty(password)) {
            layoutPassword.setError(getString(R.string.error_field_required));
            focusView = editTextPassword;
            cancel = true;
        } else if (password.length() < 6) {
            layoutPassword.setError(getString(R.string.error_invalid_password));
            focusView = editTextPassword;
            cancel = true;
        }
        
        // Verificar confirmación de contraseña
        if (TextUtils.isEmpty(confirmPassword)) {
            layoutConfirmPassword.setError(getString(R.string.error_field_required));
            focusView = editTextConfirmPassword;
            cancel = true;
        } else if (!confirmPassword.equals(password)) {
            layoutConfirmPassword.setError(getString(R.string.error_passwords_not_match));
            focusView = editTextConfirmPassword;
            cancel = true;
        }
        
        // Verificar si es registro de administrador
        if (checkBoxAdmin.isChecked()) {
            adminPassword = editTextAdminPassword.getText().toString().trim();
            if (TextUtils.isEmpty(adminPassword)) {
                editTextAdminPassword.setError(getString(R.string.error_field_required));
                focusView = editTextAdminPassword;
                cancel = true;
            }
        }
        
        if (cancel) {
            // Hay errores, enfocar el primer campo con error
            if (focusView != null) {
                focusView.requestFocus();
            }
        } else {
            // Mostrar progreso
            progressBar.setVisibility(View.VISIBLE);
            
            // Realizar registro
            authRepository.registerUser(name, email, password, adminPassword, 
                    new FirebaseAuthRepository.FirebaseCallback<FirebaseUser>() {
                @Override
                public void onSuccess(FirebaseUser result) {
                    progressBar.setVisibility(View.GONE);
                    
                    // Guardar datos de sesión
                    sessionManager.createFirebaseLoginSession(result);
                    
                    // Notificar éxito
                    Toast.makeText(getContext(), R.string.success_register_firebase, Toast.LENGTH_SHORT).show();
                    
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
    
    private void navigateToLoginFragment() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new FirebaseLoginFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }
}
