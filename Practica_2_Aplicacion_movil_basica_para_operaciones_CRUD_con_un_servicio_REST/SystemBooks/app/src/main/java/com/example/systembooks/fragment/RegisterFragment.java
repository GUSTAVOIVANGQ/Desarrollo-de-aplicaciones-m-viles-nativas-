package com.example.systembooks.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
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

import com.example.systembooks.R;
import com.example.systembooks.model.User;
import com.example.systembooks.repository.ApiRepository;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterFragment extends Fragment {
    
    private static final String TAG = "RegisterFragment";
    
    private TextInputEditText editTextName, editTextEmail, editTextPassword, editTextConfirmPassword;
    private Button buttonRegister;
    private ProgressBar progressBar;
    private TextView textViewLoginLink;
    
    private ApiRepository apiRepository;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        
        // Inicializar componentes de la UI
        editTextName = view.findViewById(R.id.editTextName);
        editTextEmail = view.findViewById(R.id.editTextRegisterEmail);
        editTextPassword = view.findViewById(R.id.editTextRegisterPassword);
        editTextConfirmPassword = view.findViewById(R.id.editTextConfirmPassword);
        buttonRegister = view.findViewById(R.id.buttonRegister);
        progressBar = view.findViewById(R.id.progressBarRegister);
        textViewLoginLink = view.findViewById(R.id.textViewLoginLink);
        
        // Inicializar ApiRepository
        apiRepository = new ApiRepository(requireContext());
        
        // Configurar listener del botón de registro
        buttonRegister.setOnClickListener(v -> attemptRegister());
        
        // Configurar el enlace para ir a la pantalla de login
        textViewLoginLink.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        
        return view;
    }
    
    private void attemptRegister() {
        // Obtener valores de los campos
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        
        // Validar que los campos no estén vacíos
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || 
                TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(getContext(), R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validar formato de email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), R.string.error_invalid_email, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validar que las contraseñas coincidan
        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), R.string.error_passwords_not_match, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Mostrar progreso
        progressBar.setVisibility(View.VISIBLE);
        
        // Llamar al repositorio para realizar el registro
        apiRepository.register(name, email, password, new ApiRepository.ApiCallback<User>() {
            @Override
            public void onSuccess(User result) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), R.string.success_register, Toast.LENGTH_SHORT).show();
                
                // Volver a la pantalla de login
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
            
            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error en registro: " + errorMessage);
            }
        });
    }
}
