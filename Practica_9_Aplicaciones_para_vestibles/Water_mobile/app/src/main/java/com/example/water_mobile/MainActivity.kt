package com.example.water_mobile

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    // UI Components
    private lateinit var waterProgress: ProgressBar
    private lateinit var currentIntakeText: TextView
    private lateinit var percentageText: TextView
    private lateinit var weekStatsText: TextView
    private lateinit var noOnlineFriendsText: TextView
    private lateinit var onlineFriendsRecyclerView: RecyclerView
    private lateinit var onlineFriendsAdapter: OnlineFriendsAdapter
    
    @RequiresApi(Build.VERSION_CODES.O)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            createNotificationChannel()
            initializeFCM()
        } else {
            Toast.makeText(this, "Permiso de notificación denegado", Toast.LENGTH_SHORT).show()
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        initializeViews()
        setupClickListeners()
        
        // Solicitar permisos de notificación
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            createNotificationChannel()
            initializeFCM()
        }
        
        // Autenticarse de forma anónima en Firebase
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        createUserProfile(userId)
                        Toast.makeText(this, "Dispositivo móvil registrado", Toast.LENGTH_SHORT).show()
                        
                        // Cargar datos después de autenticarse
                        loadInitialData()
                    } else {
                        Toast.makeText(this, "Error al registrar dispositivo", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            loadInitialData()
        }
        
        // Inicializar notificaciones
        MobileNotificationManager.createLocalNotificationChannel(this)
        
        // Actualizar UI inicial
        updateWaterIntakeUI()
        updateWeekStatsUI()
    }
    
    private fun initializeViews() {
        waterProgress = findViewById(R.id.waterProgress)
        currentIntakeText = findViewById(R.id.currentIntakeText)
        percentageText = findViewById(R.id.percentageText)
        weekStatsText = findViewById(R.id.weekStatsText)
        noOnlineFriendsText = findViewById(R.id.noOnlineFriendsText)
        
        // Setup RecyclerView for online friends
        onlineFriendsRecyclerView = findViewById(R.id.onlineFriendsRecyclerView)
        onlineFriendsAdapter = OnlineFriendsAdapter()
        onlineFriendsRecyclerView.layoutManager = LinearLayoutManager(this)
        onlineFriendsRecyclerView.adapter = onlineFriendsAdapter
    }
    
    private fun setupClickListeners() {
        // Water intake buttons
        findViewById<Button>(R.id.add250Button).setOnClickListener {
            addWater(250)
        }
        
        findViewById<Button>(R.id.add500Button).setOnClickListener {
            addWater(500)
        }
        
        findViewById<Button>(R.id.add750Button).setOnClickListener {
            addWater(750)
        }
        
        // Settings button
        findViewById<ImageView>(R.id.settingsButton).setOnClickListener {
            startActivity(android.content.Intent(this, SettingsActivity::class.java))
        }
        
        // Groups card
        findViewById<CardView>(R.id.groupsCard).setOnClickListener {
            startActivity(android.content.Intent(this, GroupsActivity::class.java))
        }
        
        // Notifications card
        findViewById<CardView>(R.id.notificationsCard).setOnClickListener {
            startActivity(android.content.Intent(this, NotificationsActivity::class.java))
        }
        
        // Stats card
        findViewById<CardView>(R.id.statsCard).setOnClickListener {
            startActivity(android.content.Intent(this, StatsActivity::class.java))
        }
    }
    
    private fun addWater(amount: Int) {
        val newIntake = HydrationManager.addWater(this, amount)
        updateWaterIntakeUI()
        
        // Mostrar mensaje
        Toast.makeText(this, "Añadidos ${amount}ml. Total: ${newIntake}ml", Toast.LENGTH_SHORT).show()
        
        // Verificar si se alcanzó la meta
        if (HydrationManager.isGoalReached(this)) {
            Toast.makeText(this, "¡Felicidades! Has alcanzado tu meta diaria 🎉", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun updateWaterIntakeUI() {
        val intake = HydrationManager.getTodayIntake(this)
        val goal = HydrationManager.getDailyGoal(this)
        val percentage = HydrationManager.getIntakePercentage(this)
        
        waterProgress.progress = (percentage * 100).toInt()
        currentIntakeText.text = "${intake}ml / ${goal}ml"
        percentageText.text = "${(percentage * 100).toInt()}%"
    }
    
    private fun updateWeekStatsUI() {
        val weekData = HydrationManager.getWeekData(this)
        val completedDays = weekData.count { it.intake >= it.goal }
        weekStatsText.text = "Días completados esta semana: $completedDays/7"
    }
    
    private fun loadInitialData() {
        // Cargar grupos del usuario
        GroupManager.loadUserGroups { success, message ->
            if (success) {
                // Iniciar listener de amigos online
                GroupManager.startOnlineFriendsListener()
                
                // Observar cambios en amigos online
                lifecycleScope.launch {
                    GroupManager.onlineFriends.collect { friends ->
                        updateOnlineFriendsUI(friends)
                    }
                }
                
                // Actualizar estado online del usuario
                lifecycleScope.launch {
                    GroupManager.updateUserOnlineStatus(true)
                }
            }
        }
    }
    
    private fun updateOnlineFriendsUI(friends: List<FriendData>) {
        if (friends.isEmpty()) {
            onlineFriendsRecyclerView.visibility = RecyclerView.GONE
            noOnlineFriendsText.visibility = TextView.VISIBLE
        } else {
            onlineFriendsRecyclerView.visibility = RecyclerView.VISIBLE
            noOnlineFriendsText.visibility = TextView.GONE
            onlineFriendsAdapter.updateFriends(friends)
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "HYDRATION_CHANNEL",
            "Recordatorios de Hidratación",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificaciones para recordar beber agua"
        }
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    
    private fun initializeFCM() {
        lifecycleScope.launch {
            try {
                val token = MobileNotificationManager.initializeFCMToken()
                if (token != null) {
                    // Suscribirse a tópicos
                    MobileNotificationManager.subscribeToTopic("hydration_reminders")
                    Toast.makeText(this@MainActivity, "Conectado a notificaciones de hidratación", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error al conectar notificaciones", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun createUserProfile(userId: String?) {
        if (userId == null) return
        
        val userProfile = hashMapOf(
            "name" to "Usuario Móvil",
            "email" to "mobile@example.com",
            "isActive" to true,
            "deviceType" to "mobile",
            "createdAt" to System.currentTimeMillis(),
            "lastActive" to System.currentTimeMillis(),
            "dailyIntake" to 0,
            "dailyGoal" to 2000
        )
        
        firestore.collection("users").document(userId)
            .set(userProfile)
            .addOnSuccessListener {
                // Perfil creado exitosamente
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al crear perfil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    
    override fun onResume() {
        super.onResume()
        updateWaterIntakeUI()
        updateWeekStatsUI()
        
        // Actualizar estado online
        lifecycleScope.launch {
            GroupManager.updateUserOnlineStatus(true)
        }
    }
    
    override fun onPause() {
        super.onPause()
        
        // Actualizar estado offline
        lifecycleScope.launch {
            GroupManager.updateUserOnlineStatus(false)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        GroupManager.stopOnlineFriendsListener()
    }
}