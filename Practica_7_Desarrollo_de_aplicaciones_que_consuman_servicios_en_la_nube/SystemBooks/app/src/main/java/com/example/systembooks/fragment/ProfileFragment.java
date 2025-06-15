package com.example.systembooks.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.systembooks.R;
import com.example.systembooks.model.User;
import com.example.systembooks.repository.ApiRepository;
import com.example.systembooks.util.ImageUtils;
import com.example.systembooks.util.SessionManager;
import com.example.systembooks.util.RoleManager;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;

    private CircleImageView imageViewProfile;
    private TextInputEditText editTextName;
    private TextInputEditText editTextEmail;
    private Button buttonChangePhoto;
    private Button buttonSaveProfile;
    private ProgressBar progressBar;

    private ApiRepository apiRepository;
    private SessionManager sessionManager;
    private RoleManager roleManager;
    private User currentUser;
    private File photoFile;
    private Uri photoUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Inicializar componentes de UI
        imageViewProfile = view.findViewById(R.id.imageViewProfile);
        editTextName = view.findViewById(R.id.editTextName);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        buttonChangePhoto = view.findViewById(R.id.buttonChangePhoto);
        buttonSaveProfile = view.findViewById(R.id.buttonSaveProfile);
        progressBar = view.findViewById(R.id.progressBarProfile);

        // Inicializar repository, session manager y role manager
        apiRepository = new ApiRepository(requireContext());
        sessionManager = new SessionManager(requireContext());
        roleManager = new RoleManager(requireContext());
        
        // Comprobar si el usuario tiene permisos para acceder a esta pantalla
        if (!roleManager.isUser() && !roleManager.isAdmin()) {
            // Si no tiene permisos, redirigir a la pantalla de acceso denegado
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AccessDeniedFragment())
                        .commit();
                return view;
            }
        }

        // Cargar datos del usuario
        loadUserData();

        // Configurar listener para cambiar foto
        buttonChangePhoto.setOnClickListener(v -> showPhotoSourceDialog());

        // Configurar listener para guardar perfil
        buttonSaveProfile.setOnClickListener(v -> saveUserProfile());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Cargar datos del usuario desde la sesión
        if (currentUser != null) {
            editTextName.setText(currentUser.getNombre());
            editTextEmail.setText(currentUser.getEmail());
            
            // Cargar imagen de perfil
            if (!TextUtils.isEmpty(currentUser.getImagen())) {
                ImageUtils.loadProfileImage(requireContext(), currentUser.getImagen(), imageViewProfile);
            } else {
                imageViewProfile.setImageResource(R.drawable.default_profile);
            }
        }
    }

    private void loadUserData() {
        progressBar.setVisibility(View.VISIBLE);

        // Obtener usuario de sesión
        currentUser = sessionManager.getUser();

        if (currentUser != null) {
            // Cargar datos básicos
            editTextName.setText(currentUser.getNombre());
            editTextEmail.setText(currentUser.getEmail());

            // Cargar imagen de perfil
            if (!TextUtils.isEmpty(currentUser.getImagen())) {
                ImageUtils.loadProfileImage(requireContext(), currentUser.getImagen(), imageViewProfile);
            } else {
                imageViewProfile.setImageResource(R.drawable.default_profile);
            }
            
            progressBar.setVisibility(View.GONE);
        } else {
            // Si no hay usuario en sesión, obtener datos desde API
            apiRepository.getUserById(sessionManager.getUserId(), new ApiRepository.ApiCallback<User>() {
                @Override
                public void onSuccess(User result) {
                    currentUser = result;
                    
                    // Actualizar UI con los datos obtenidos
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            editTextName.setText(result.getNombre());
                            editTextEmail.setText(result.getEmail());
                            
                            if (!TextUtils.isEmpty(result.getImagen())) {
                                ImageUtils.loadProfileImage(requireContext(), result.getImagen(), imageViewProfile);
                            } else {
                                imageViewProfile.setImageResource(R.drawable.default_profile);
                            }
                            
                            progressBar.setVisibility(View.GONE);
                        });
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Error al cargar datos del usuario: " + errorMessage);
                        });
                    }
                }
            });
        }
    }

    private void showPhotoSourceDialog() {
        String[] options = {
                getString(R.string.camera),
                getString(R.string.gallery),
                getString(R.string.cancel)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.choose_photo_source)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        dispatchTakePictureIntent();
                    } else if (which == 1) {
                        dispatchPickImageIntent();
                    }
                    dialog.dismiss();
                })
                .show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            try {
                photoFile = ImageUtils.createImageFile(requireContext());
                
                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(requireContext(),
                            requireContext().getPackageName() + ".fileprovider",
                            photoFile);
                    
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error creando archivo para la foto: " + e.getMessage());
                Toast.makeText(getContext(), "Error al preparar la cámara", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "No se encontró aplicación para tomar fotos", Toast.LENGTH_SHORT).show();
        }
    }

    private void dispatchPickImageIntent() {
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageIntent.setType("image/*");
        startActivityForResult(pickImageIntent, REQUEST_PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Foto tomada con cámara
                processAndUploadImage(photoUri);
            } else if (requestCode == REQUEST_PICK_IMAGE && data != null) {
                // Foto seleccionada de galería
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    processAndUploadImage(selectedImageUri);
                }
            }
        }
    }

    private void processAndUploadImage(Uri imageUri) {
        progressBar.setVisibility(View.VISIBLE);
        
        try {
            // Cargar y redimensionar la imagen
            Bitmap bitmap = ImageUtils.loadAndResizeImage(requireContext(), imageUri, 500, 500);
            
            if (bitmap != null) {
                // Mostrar la imagen en el ImageView
                imageViewProfile.setImageBitmap(bitmap);
                
                // Convertir Uri a File para subir
                File imageFile = ImageUtils.uriToFile(requireContext(), imageUri);
                
                // Subir la imagen al servidor
                apiRepository.uploadProfileImage(sessionManager.getUserId(), imageFile, new ApiRepository.ApiCallback<User>() {
                    @Override
                    public void onSuccess(User result) {
                        if (getActivity() != null && isAdded()) {
                            getActivity().runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getContext(), R.string.success_photo_update, Toast.LENGTH_SHORT).show();
                                
                                // Actualizar usuario en sesión
                                currentUser = result;
                                sessionManager.createLoginSession(
                                        sessionManager.getAuthToken(),
                                        result,
                                        sessionManager.getUserRole()
                                );
                            });
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        if (getActivity() != null && isAdded()) {
                            getActivity().runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getContext(), R.string.error_photo_update, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Error al subir imagen: " + errorMessage);
                            });
                        }
                    }
                });
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), R.string.error_photo_update, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), R.string.error_photo_update, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error procesando imagen: " + e.getMessage());
        }
    }

    private void saveUserProfile() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        
        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(getContext(), R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        
        // Actualizar datos del usuario
        currentUser.setNombre(name);
        currentUser.setEmail(email);
        
        apiRepository.updateUser(currentUser, new ApiRepository.ApiCallback<User>() {
            @Override
            public void onSuccess(User result) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), R.string.success_profile_update, Toast.LENGTH_SHORT).show();
                        
                        // Actualizar usuario en sesión
                        sessionManager.createLoginSession(
                                sessionManager.getAuthToken(),
                                result,
                                sessionManager.getUserRole()
                        );
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), R.string.error_profile_update, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error al actualizar perfil: " + errorMessage);
                    });
                }
            }
        });
    }
}
