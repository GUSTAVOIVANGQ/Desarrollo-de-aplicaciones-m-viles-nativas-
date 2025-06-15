package com.example.systembooks.firebase;

import android.app.Activity;
import android.content.Intent;
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

import com.bumptech.glide.Glide;
import com.example.systembooks.R;
import com.example.systembooks.fragment.AccessDeniedFragment;
import com.example.systembooks.util.SessionManager;
import com.example.systembooks.util.RoleManager;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.systembooks.util.ImgbbUploader;
import com.example.systembooks.util.ActivityTracker;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class FirebaseProfileFragment extends Fragment {

    private static final String TAG = "FirebaseProfileFragment";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;

    private CircleImageView imageViewProfile;
    private TextInputEditText editTextName;
    private TextInputEditText editTextEmail;
    private Button buttonChangePhoto;
    private Button buttonSaveProfile;
    private ProgressBar progressBar;    private FirebaseAuthRepository authRepository;
    private SessionManager sessionManager;
    private RoleManager roleManager;
    private FirebaseUser currentUser;    private File photoFile;
    private Uri photoUri;
    private ImgbbUploader imgbbUploader;
    private ActivityTracker activityTracker;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Inicializar componentes de UI
        imageViewProfile = view.findViewById(R.id.imageViewProfile);
        editTextName = view.findViewById(R.id.editTextName);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        buttonChangePhoto = view.findViewById(R.id.buttonChangePhoto);
        buttonSaveProfile = view.findViewById(R.id.buttonSaveProfile);
        progressBar = view.findViewById(R.id.progressBarProfile);        // Inicializar respository, session manager y role manager
        authRepository = new FirebaseAuthRepository(requireContext());
        sessionManager = new SessionManager(requireContext());
        roleManager = new RoleManager(requireContext());
        imgbbUploader = new ImgbbUploader(requireContext());
        activityTracker = ActivityTracker.getInstance(requireContext());
        
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

    private void loadUserData() {
        progressBar.setVisibility(View.VISIBLE);

        // Obtener usuario de Firebase
        currentUser = sessionManager.getFirebaseUser();

        if (currentUser != null) {
            // Cargar datos básicos
            editTextName.setText(currentUser.getUsername());
            editTextEmail.setText(currentUser.getEmail());

            // Cargar imagen de perfil
            if (!TextUtils.isEmpty(currentUser.getPhotoUrl())) {
                Glide.with(this)
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(imageViewProfile);
            } else {
                imageViewProfile.setImageResource(R.drawable.default_profile);
            }
            
            progressBar.setVisibility(View.GONE);
        } else {
            // Si no hay usuario en sesión, obtener datos desde Firebase
            String uid = authRepository.getCurrentUserId();
            if (uid != null) {
                authRepository.getUserFromFirestore(uid, new FirebaseAuthRepository.FirebaseCallback<FirebaseUser>() {
                    @Override
                    public void onSuccess(FirebaseUser result) {
                        currentUser = result;
                        
                        // Actualizar UI con los datos obtenidos
                        if (getActivity() != null && isAdded()) {
                            getActivity().runOnUiThread(() -> {
                                editTextName.setText(result.getUsername());
                                editTextEmail.setText(result.getEmail());
                                
                                if (!TextUtils.isEmpty(result.getPhotoUrl())) {
                                    Glide.with(FirebaseProfileFragment.this)
                                        .load(result.getPhotoUrl())
                                        .placeholder(R.drawable.default_profile)
                                        .error(R.drawable.default_profile)
                                        .into(imageViewProfile);
                                } else {
                                    imageViewProfile.setImageResource(R.drawable.default_profile);
                                }
                                
                                // Actualizar usuario en sesión
                                sessionManager.createFirebaseLoginSession(result);
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
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            }
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
                photoFile = createImageFile();
                
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

    private File createImageFile() throws IOException {
        // Create an image file name
        String fileName = "JPEG_" + UUID.randomUUID().toString();
        File storageDir = requireActivity().getExternalFilesDir(null);
        return File.createTempFile(fileName, ".jpg", storageDir);
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
                uploadProfileImage(photoUri);
            } else if (requestCode == REQUEST_PICK_IMAGE && data != null) {
                // Foto seleccionada de galería
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    uploadProfileImage(selectedImageUri);
                }
            }
        }
    }    private void uploadProfileImage(Uri imageUri) {
        if (imageUri == null) {
            Toast.makeText(getContext(), "Error: imagen no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        
        // Mostrar mensaje de carga
        Toast.makeText(getContext(), R.string.uploading_image, Toast.LENGTH_SHORT).show();

        // Verificar que el usuario esté autenticado
        String uid = authRepository.getCurrentUserId();
        if (uid == null) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Error: usuario no identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Subir imagen a imgbb
        imgbbUploader.uploadImage(imageUri, new ImgbbUploader.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                Log.d(TAG, "Image uploaded successfully to imgbb: " + imageUrl);
                updateProfileWithImageUrl(imageUrl);
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error al subir la imagen: " + errorMessage, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error uploading image to imgbb: " + errorMessage);
                    });
                }
            }
        });
    }

    private void updateProfileWithImageUrl(String imageUrl) {
        if (currentUser == null || TextUtils.isEmpty(authRepository.getCurrentUserId())) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        // Actualizar URL de la foto en Firestore
        FirebaseFirestore db = FirebaseManager.getInstance().getFirestore();
        String uid = authRepository.getCurrentUserId();
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("photoUrl", imageUrl);

        db.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                // Actualizar objeto usuario local
                currentUser.setPhotoUrl(imageUrl);
                sessionManager.createFirebaseLoginSession(currentUser);

                // Actualizar UI
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                          // Mostrar imagen actualizada
                        Glide.with(FirebaseProfileFragment.this)
                            .load(imageUrl)
                            .placeholder(R.drawable.default_profile)
                            .error(R.drawable.default_profile)
                            .into(imageViewProfile);
                        
                        // Track profile update activity
                        activityTracker.trackProfileUpdate("Foto de perfil actualizada");
                        
                        Toast.makeText(getContext(), R.string.success_photo_update, Toast.LENGTH_SHORT).show();
                    });
                }
            })
            .addOnFailureListener(e -> {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), R.string.error_photo_update, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error updating profile with image URL", e);
                    });
                }
            });
    }

    private void saveUserProfile() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(getContext(), R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        
        // Solo se puede cambiar el nombre de usuario, no el email (requiere reautenticación)
        if (!name.equals(currentUser.getUsername())) {
            updateUsername(name);
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "No changes detected", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUsername(String newUsername) {
        if (currentUser == null || TextUtils.isEmpty(authRepository.getCurrentUserId())) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        // Actualizar username en Firestore
        FirebaseFirestore db = FirebaseManager.getInstance().getFirestore();
        String uid = authRepository.getCurrentUserId();
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", newUsername);

        db.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                // Actualizar objeto usuario local
                currentUser.setUsername(newUsername);
                sessionManager.createFirebaseLoginSession(currentUser);                // Actualizar UI
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        
                        // Track profile update activity
                        activityTracker.trackProfileUpdate("Nombre de usuario actualizado: " + newUsername);
                        
                        Toast.makeText(getContext(), R.string.success_profile_update, Toast.LENGTH_SHORT).show();
                    });
                }
            })
            .addOnFailureListener(e -> {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), R.string.error_profile_update, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error updating username", e);
                    });
                }
            });
    }
}
