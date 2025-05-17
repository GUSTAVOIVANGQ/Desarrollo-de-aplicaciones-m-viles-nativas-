package com.example.pushnotificationapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.pushnotificationapp.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity"
    private lateinit var db: FirebaseFirestore

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notificaciones permitidas", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notificaciones denegadas", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance()

        askNotificationPermission()
        getFCMToken()

        binding.sendNotificationButton.setOnClickListener {
            val title = binding.titleEditText.text.toString().trim()
            val message = binding.messageEditText.text.toString().trim()

            if (title.isEmpty() || message.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendNotificationToAll(title, message)
        }
    }

    private fun askNotificationPermission() {
        // Solo se requiere para Android 13 y superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Obtener el token
            val token = task.result
            Log.d(TAG, "FCM Token: $token")
            binding.tokenTextView.text = "Token de FCM: $token"

            // Guardar el token en Firestore
            saveTokenToFirestore(token)
        }
    }

    private fun saveTokenToFirestore(token: String) {
        val tokenData = hashMapOf(
            "token" to token,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("tokens")
            .document(token)
            .set(tokenData)
            .addOnSuccessListener {
                Log.d(TAG, "Token guardado exitosamente en Firestore")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error al guardar token", e)
            }
    }

    private fun sendNotificationToAll(title: String, message: String) {
        // Mostrar estado de carga
        binding.progressBar.visibility = View.VISIBLE
        binding.statusTextView.text = "Enviando notificación..."
        binding.sendNotificationButton.isEnabled = false

        // Utilizar corrutinas para operaciones asíncronas
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Obtener todos los tokens de Firestore
                val tokensSnapshot = db.collection("tokens").get().await()
                val tokens = tokensSnapshot.documents.mapNotNull { it.getString("token") }

                if (tokens.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        binding.statusTextView.text = "No hay dispositivos registrados"
                        binding.sendNotificationButton.isEnabled = true
                    }
                    return@launch
                }

                // Usar FCMHelper para enviar notificaciones
                val success = FCMHelper.sendNotificationToTokens(tokens, title, message)

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE

                    if (success) {
                        binding.statusTextView.text = "Notificación enviada a ${tokens.size} dispositivos"
                        // Limpiar los campos
                        binding.titleEditText.text.clear()
                        binding.messageEditText.text.clear()

                        // Mostrar notificación local para demostración
                        NotificationHelper.showNotification(
                            this@MainActivity,
                            title,
                            message
                        )

                        Toast.makeText(
                            this@MainActivity,
                            "Notificación enviada exitosamente",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        binding.statusTextView.text = "Error al enviar notificación"
                        Toast.makeText(
                            this@MainActivity,
                            "Error al enviar notificación",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    binding.sendNotificationButton.isEnabled = true
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar notificación", e)
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.statusTextView.text = "Error: ${e.message}"
                    binding.sendNotificationButton.isEnabled = true
                    Toast.makeText(
                        this@MainActivity,
                        "Error al enviar notificación: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}