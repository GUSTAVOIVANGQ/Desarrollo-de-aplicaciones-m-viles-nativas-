package com.example.systembooks.ui.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.systembooks.R;
import com.example.systembooks.firebase.FirebaseAuthRepository;
import com.example.systembooks.firebase.FirebaseManager;
import com.example.systembooks.firebase.NotificationHelper;
import com.example.systembooks.utils.NotificationTestUtil;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Fragment to test notification delivery and manage notification settings
 */
public class NotificationTestFragment extends Fragment {
    private static final String TAG = "NotificationTestFragment";
    private TextView statusText;
    private Button testButton;
    private Button optimizeButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notification_test, container, false);
        
        statusText = root.findViewById(R.id.text_notification_status);
        testButton = root.findViewById(R.id.button_test_notification);
        optimizeButton = root.findViewById(R.id.button_optimize_notifications);
        
        setupButtons();
        checkInitialStatus();
        
        return root;
    }
    
    private void setupButtons() {
        testButton.setOnClickListener(v -> {
            // Run notification test
            testNotification();
        });
        
        optimizeButton.setOnClickListener(v -> {
            // Apply optimizations
            FirebaseManager.getInstance().optimizeNotificationDelivery();
            Toast.makeText(requireContext(), "Optimización de notificaciones aplicada", Toast.LENGTH_SHORT).show();
            updateStatus("Optimizaciones aplicadas. Intentando mejorar la entrega de notificaciones...");
        });
    }
    
    private void checkInitialStatus() {
        // Display device settings and notification status
        NotificationTestUtil testUtil = new NotificationTestUtil(requireContext());
        String deviceSettings = testUtil.checkDeviceSettings();
        updateStatus("Estado del sistema de notificaciones:\n\n" + deviceSettings);
    }
    
    private void testNotification() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesión para probar las notificaciones", Toast.LENGTH_SHORT).show();
            updateStatus("ERROR: Necesitas iniciar sesión para probar las notificaciones");
            return;
        }
        
        updateStatus("Enviando notificación de prueba...");
        
        NotificationTestUtil testUtil = new NotificationTestUtil(requireContext());
        testUtil.runDiagnostic(result -> {
            updateStatus(result);
        });
    }
    
    private void updateStatus(String text) {
        if (statusText != null) {
            statusText.setText(text);
        }
        Log.d(TAG, "Status update: " + text);
    }
}
