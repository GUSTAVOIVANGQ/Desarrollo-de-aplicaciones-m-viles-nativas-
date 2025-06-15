package com.example.systembooks.firebase;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.systembooks.R;
import com.example.systembooks.firebase.FirebaseManager;
import com.example.systembooks.util.LocalNotificationHelper;
import com.example.systembooks.util.RoleManager;
import com.example.systembooks.util.SessionManager;
import com.example.systembooks.util.ActivityTracker;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationAdminFragment extends Fragment {

    private static final String TAG = "NotificationAdmin";
    
    private EditText editTextTitle;
    private EditText editTextMessage;
    private Spinner spinnerTargetType;
    private RecyclerView recyclerViewUsers;
    private Button buttonSendNotification;
    private ProgressBar progressBar;
      private NotificationHelper notificationHelper;
    private LocalNotificationHelper localNotificationHelper;    private RoleManager roleManager;
    private SessionManager sessionManager;
    private UserSelectionAdapter userAdapter;
    private ActivityTracker activityTracker;
    private List<FirebaseUserItem> userList = new ArrayList<>();
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification_admin, container, false);
        
        // Inicializar vistas
        editTextTitle = view.findViewById(R.id.editTextNotificationTitle);
        editTextMessage = view.findViewById(R.id.editTextNotificationMessage);
        spinnerTargetType = view.findViewById(R.id.spinnerTargetType);
        recyclerViewUsers = view.findViewById(R.id.recyclerViewUsers);
        buttonSendNotification = view.findViewById(R.id.buttonSendNotification);
        progressBar = view.findViewById(R.id.progressBarNotification);        // Inicializar helpers y managers
        try {
            // Verificar que FirebaseManager esté inicializado antes de crear NotificationHelper
            if (!FirebaseManager.isInitialized()) {
                Log.e(TAG, "FirebaseManager not properly initialized");
                Toast.makeText(getContext(), "Error: Firebase no inicializado correctamente", Toast.LENGTH_LONG).show();
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
                return view;
            }
            
            notificationHelper = new NotificationHelper(requireContext());
            localNotificationHelper = new LocalNotificationHelper(requireContext());
            roleManager = new RoleManager(requireContext());
            sessionManager = new SessionManager(requireContext());
            activityTracker = ActivityTracker.getInstance(requireContext());
            
            Log.d(TAG, "All components initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing components", e);
            Toast.makeText(getContext(), "Error inicializando componentes: " + e.getMessage(), Toast.LENGTH_LONG).show();
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
            return view;
        }
        
        // Verificar si el usuario tiene permisos de administrador
        if (!roleManager.isAdmin()) {
            Toast.makeText(getContext(), R.string.admin_required, Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
            return view;
        }
        
        // Configurar spinner de tipo de destinatario
        setupTargetTypeSpinner();
        
        // Configurar RecyclerView de usuarios
        setupUserRecyclerView();
        
        // Cargar lista de usuarios
        loadUsers();
        
        // Configurar listener del botón para enviar notificación
        buttonSendNotification.setOnClickListener(v -> sendNotification());
        
        return view;
    }
    
    private void setupTargetTypeSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.notification_target_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTargetType.setAdapter(adapter);
        
        spinnerTargetType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                // Mostrar u ocultar la lista de usuarios según la selección
                if (position == 1) { // Opción "Usuario específico"
                    recyclerViewUsers.setVisibility(View.VISIBLE);
                } else {
                    recyclerViewUsers.setVisibility(View.GONE);
                }
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                recyclerViewUsers.setVisibility(View.GONE);
            }
        });
    }
    
    private void setupUserRecyclerView() {
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        userAdapter = new UserSelectionAdapter(userList);
        recyclerViewUsers.setAdapter(userAdapter);
        // Inicialmente oculto hasta que se seleccione "Usuario específico"
        recyclerViewUsers.setVisibility(View.GONE);
    }
    
    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        
        FirebaseFirestore db = FirebaseManager.getInstance().getFirestore();
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    
                    if (task.isSuccessful()) {
                        userList.clear();
                        
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String uid = document.getId();
                            String username = document.getString("username");
                            String email = document.getString("email");
                            
                            FirebaseUserItem user = new FirebaseUserItem(uid, username, email);
                            userList.add(user);
                        }
                        
                        userAdapter.notifyDataSetChanged();
                        
                        if (userList.isEmpty()) {
                            Toast.makeText(getContext(), R.string.no_users_found, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Error getting users", task.getException());
                        Toast.makeText(getContext(), R.string.error_loading_users, Toast.LENGTH_SHORT).show();
                    }
                });
    }
      
    private void sendNotification() {
        // Validar campos
        if (TextUtils.isEmpty(editTextTitle.getText())) {
            editTextTitle.setError(getString(R.string.error_field_required));
            editTextTitle.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(editTextMessage.getText())) {
            editTextMessage.setError(getString(R.string.error_field_required));
            editTextMessage.requestFocus();
            return;
        }
        
        String title = editTextTitle.getText().toString().trim();
        String message = editTextMessage.getText().toString().trim();
        int targetType = spinnerTargetType.getSelectedItemPosition();
          progressBar.setVisibility(View.VISIBLE);
        
        // Verificar que los helpers estén inicializados
        if (localNotificationHelper == null) {
            Log.e(TAG, "LocalNotificationHelper is null");
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Error: Servicio de notificaciones locales no disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Mostrar notificación local inmediatamente
        //localNotificationHelper.sendLocalNotification(title, message);
        
        // Los datos adicionales para Firebase
        Map<String, String> data = new HashMap<>();
        data.put("type", "admin_notification");
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        // Determinar el tipo de envío según la selección del spinner
        if (targetType == 0) { // Todos los usuarios
            // Mostrar notificación local inmediatamente
            try {
                localNotificationHelper.sendLocalNotification(title, message);
                Log.d(TAG, "Local notification sent successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error sending local notification", e);
            }
            sendToAllUsers(title, message, data);
        } else { // Usuario específico
            FirebaseUserItem selectedUser = userAdapter.getSelectedUser();
            if (selectedUser != null) {
                // Mostrar notificación local también para usuario específico
                try {
                    localNotificationHelper.sendLocalNotification(title, message);
                    Log.d(TAG, "Local notification sent successfully");
                } catch (Exception e) {
                    Log.e(TAG, "Error sending local notification", e);
                }
                sendToSpecificUser(selectedUser.getUid(), title, message, data);
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Seleccione un usuario para enviar por Firebase.", Toast.LENGTH_SHORT).show();
            }
        }
    }    
      private void sendToAllUsers(String title, String message, Map<String, String> data) {
        // La notificación local ya se envió en sendNotification()
        
        // Verificar que notificationHelper esté inicializado
        if (notificationHelper == null) {
            Log.e(TAG, "NotificationHelper is null - cannot send Firebase notification");
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Error: Servicio de notificaciones no disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Ahora enviamos por Firebase para los dispositivos que no están activos
        notificationHelper.sendNotificationToAllUsers(title, message, data, new FirebaseAuthRepository.FirebaseCallback<Void>() {@Override
            public void onSuccess(Void result) {
                progressBar.setVisibility(View.GONE);
                //Toast.makeText(getContext(), "Notificación enviada local y remotamente", Toast.LENGTH_SHORT).show();
                Toast.makeText(getContext(), "Notificación enviada exitosamente", Toast.LENGTH_SHORT).show();
                
                // Track notification sent activity
                activityTracker.trackNotificationSent(title, "Todos los usuarios");
                
                clearFields();
            }
            
            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                // Notificamos que la notificación local se envió correctamente aunque Firebase fallara
                Toast.makeText(getContext(), "Notificación local enviada. Error en Firebase: " + errorMessage, Toast.LENGTH_LONG).show();
                clearFields();
            }
        });
    }    
      private void sendToSpecificUser(String userId, String title, String message, Map<String, String> data) {
        // La notificación local ya se envió en sendNotification()
        
        // Verificar que notificationHelper esté inicializado
        if (notificationHelper == null) {
            Log.e(TAG, "NotificationHelper is null - cannot send Firebase notification");
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Error: Servicio de notificaciones no disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Luego enviamos también por Firebase para los dispositivos que no están activos
        notificationHelper.sendNotificationToUser(userId, title, message, data, new FirebaseAuthRepository.FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Notificación enviada local y remotamente", Toast.LENGTH_SHORT).show();
                
                // Track notification sent activity - get username from userId
                String recipientInfo = "Usuario específico (ID: " + userId + ")";
                activityTracker.trackNotificationSent(title, recipientInfo);
                
                clearFields();
            }
            
            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                // Notificamos que la notificación local se envió correctamente aunque Firebase fallara
                Toast.makeText(getContext(), "Notificación local enviada. Error en Firebase: " + errorMessage, Toast.LENGTH_LONG).show();
                clearFields();
            }
        });
    }
    
    private void clearFields() {
        editTextTitle.setText("");
        editTextMessage.setText("");
        spinnerTargetType.setSelection(0);
    }
    
    /**
     * Clase interna para representar elementos de usuario en la lista
     */
    private static class FirebaseUserItem {
        private final String uid;
        private final String username;
        private final String email;
        private boolean selected;
        
        public FirebaseUserItem(String uid, String username, String email) {
            this.uid = uid;
            this.username = username;
            this.email = email;
            this.selected = false;
        }
        
        public String getUid() {
            return uid;
        }
        
        public String getUsername() {
            return username;
        }
        
        public String getEmail() {
            return email;
        }
        
        public boolean isSelected() {
            return selected;
        }
        
        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }
    
    /**
     * Adaptador para la lista de selección de usuarios
     */
    private class UserSelectionAdapter extends RecyclerView.Adapter<UserSelectionAdapter.UserViewHolder> {
        
        private final List<FirebaseUserItem> users;
        private int selectedPosition = RecyclerView.NO_POSITION;
        
        UserSelectionAdapter(List<FirebaseUserItem> users) {
            this.users = users;
        }
        
        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_selection, parent, false);
            return new UserViewHolder(itemView);
        }
        
        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            FirebaseUserItem user = users.get(position);
            holder.bindUser(user, position);
        }
        
        @Override
        public int getItemCount() {
            return users.size();
        }
        
        public FirebaseUserItem getSelectedUser() {
            if (selectedPosition != RecyclerView.NO_POSITION && selectedPosition < users.size()) {
                return users.get(selectedPosition);
            }
            return null;
        }
        
        class UserViewHolder extends RecyclerView.ViewHolder {
            private final androidx.appcompat.widget.AppCompatRadioButton radioButton;
            private final androidx.appcompat.widget.AppCompatTextView textViewUsername;
            private final androidx.appcompat.widget.AppCompatTextView textViewEmail;
            
            UserViewHolder(View view) {
                super(view);
                radioButton = view.findViewById(R.id.radioButtonSelectUser);
                textViewUsername = view.findViewById(R.id.textViewUsername);
                textViewEmail = view.findViewById(R.id.textViewEmail);
                
                view.setOnClickListener(v -> {
                    int clickedPosition = getAdapterPosition();
                    if (clickedPosition != RecyclerView.NO_POSITION) {
                        updateSelection(clickedPosition);
                    }
                });
                
                radioButton.setOnClickListener(v -> {
                    int clickedPosition = getAdapterPosition();
                    if (clickedPosition != RecyclerView.NO_POSITION) {
                        updateSelection(clickedPosition);
                    }
                });
            }
            
            void bindUser(FirebaseUserItem user, int position) {
                textViewUsername.setText(user.getUsername());
                textViewEmail.setText(user.getEmail());
                radioButton.setChecked(position == selectedPosition);
            }
            
            void updateSelection(int position) {
                if (selectedPosition != position) {
                    int oldPosition = selectedPosition;
                    selectedPosition = position;
                    
                    if (oldPosition != RecyclerView.NO_POSITION) {
                        notifyItemChanged(oldPosition);
                    }
                    notifyItemChanged(selectedPosition);
                }
            }
        }
    }
}
