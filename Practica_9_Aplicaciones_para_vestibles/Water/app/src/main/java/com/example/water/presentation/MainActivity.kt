/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.water.presentation

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.*
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    
    companion object {
        private var instance: MainActivity? = null
        
        fun getInstance(): MainActivity? = instance
    }
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            createNotificationChannel()
        }
    }
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    // Variables para notificaciones locales de prueba
    private var notificationHandler: Handler? = null
    private var notificationRunnable: Runnable? = null
    private var isNotificationActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Asignar la instancia
        instance = this
        
        // Solicitar permisos de notificación
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            createNotificationChannel()
        }

        // Autenticarse de forma anónima en Firebase
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Usuario anónimo creado
                        val userId = auth.currentUser?.uid
                        createUserProfile(userId)
                        Toast.makeText(this, "Dispositivo registrado", Toast.LENGTH_SHORT).show()
                    } else {
                        // Error
                        Toast.makeText(this, "Error al registrar dispositivo", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Configurar Firebase Messaging
        FirebaseMessaging.getInstance().subscribeToTopic("hydration_reminders")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Conectado a notificaciones", Toast.LENGTH_SHORT).show()
                }
            }

        setContent {
            WearApp()
        }
    }
    
    private fun createUserProfile(userId: String?) {
        if (userId == null) return
        
        val deviceName = android.os.Build.MODEL
        val user = hashMapOf(
            "name" to "Usuario de $deviceName",
            "email" to "$userId@anonymous.wear",
            "deviceType" to "wearable",
            "createdAt" to com.google.firebase.Timestamp.now(),
            "lastActive" to com.google.firebase.Timestamp.now(),
            "dailyGoal" to HydrationManager.getDailyGoal(this),
            "dailyIntake" to HydrationManager.getTodayIntake(this)
        )
        
        firestore.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                updateDeviceToken(userId)
            }
    }
    
    private fun updateDeviceToken(userId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                
                val tokenData = hashMapOf(
                    "token" to token,
                    "userId" to userId,
                    "deviceType" to "wearable",
                    "timestamp" to com.google.firebase.Timestamp.now()
                )
                
                firestore.collection("device_tokens")
                    .document(token)
                    .set(tokenData)
            }
        }
    }

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
    
    // Funciones para notificaciones locales de prueba
    fun startLocalNotifications() {
        if (isNotificationActive) return
        
        isNotificationActive = true
        notificationHandler = Handler(Looper.getMainLooper())
        
        notificationRunnable = object : Runnable {
            override fun run() {
                if (isNotificationActive) {
                    sendLocalNotification()
                    notificationHandler?.postDelayed(this, 30000) // 30 segundos
                }
            }
        }
        
        notificationHandler?.post(notificationRunnable!!)
        Toast.makeText(this, "🔔 Notificaciones iniciadas (cada 30s)", Toast.LENGTH_SHORT).show()
    }
    
    fun stopLocalNotifications() {
        isNotificationActive = false
        notificationRunnable?.let { runnable ->
            notificationHandler?.removeCallbacks(runnable)
        }
        notificationHandler = null
        notificationRunnable = null
        Toast.makeText(this, "🔕 Notificaciones detenidas", Toast.LENGTH_SHORT).show()
    }
    
    private fun sendLocalNotification() {
        val currentIntake = HydrationManager.getTodayIntake(this)
        val dailyGoal = HydrationManager.getDailyGoal(this)
        val remaining = HydrationManager.getRemainingWater(this)
        
        val testMessages = arrayOf(
            "💧 ¡Hora de hidratarte!",
            "🚰 Recuerda beber agua",
            "💦 Tu cuerpo necesita agua",
            "🌊 Mantente hidratado",
            "💧 Es momento de tomar agua"
        )
        
        val title = testMessages.random()
        val body = if (remaining > 0) {
            "Progreso: ${currentIntake}ml / ${dailyGoal}ml\nFaltan: ${remaining}ml para tu meta"
        } else {
            "🎉 ¡Meta alcanzada! Total: ${currentIntake}ml"
        }
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val notification = NotificationCompat.Builder(this, "HYDRATION_CHANNEL")
            .setSmallIcon(com.example.water.R.drawable.ic_water_drop)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(Random.nextInt(1000, 9999), notification)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopLocalNotifications()
        instance = null
    }
}

@Composable
fun WearApp() {
    val navController = rememberSwipeDismissableNavController()
    
    MaterialTheme {
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                HomeScreen(
                    onSettingsClick = { navController.navigate("settings") },
                    onStatsClick = { navController.navigate("stats") },
                    onGroupsClick = { navController.navigate("groups") }
                )
            }
            composable("settings") {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("stats") {
                StatsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("groups") {
                GroupsScreen(
                    onBackClick = { navController.popBackStack() },
                    onGroupSelected = { groupId ->
                        navController.navigate("group_detail/$groupId")
                    },
                    onCreateGroup = {
                        navController.navigate("create_group")
                    },
                    onViewUsers = {
                        navController.navigate("all_users")
                    }
                )
            }
            composable("create_group") {
                CreateGroupScreen(
                    onBackClick = { navController.popBackStack() },
                    onGroupCreated = { navController.popBackStack() }
                )
            }
            composable("group_detail/{groupId}") { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                GroupDetailScreen(
                    groupId = groupId,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("all_users") {
                AllUsersScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    onSettingsClick: () -> Unit,
    onStatsClick: () -> Unit,
    onGroupsClick: () -> Unit
) {
    val context = LocalContext.current
    var waterIntake by remember { mutableStateOf(HydrationManager.getTodayIntake(context)) }
    var dailyGoal by remember { mutableStateOf(HydrationManager.getDailyGoal(context)) }
    
    val progress = (waterIntake.toFloat() / dailyGoal.toFloat()).coerceAtMost(1f)
    
    Scaffold(
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = rememberScalingLazyListState()) }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                // Indicador circular de progreso
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.surface),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = progress,
                        modifier = Modifier.size(100.dp),
                        strokeWidth = 8.dp,
                        indicatorColor = AppColors.Blue,
                        trackColor = MaterialTheme.colors.surface.copy(alpha = 0.2f)
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${waterIntake}ml",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurface
                        )
                        Text(
                            text = "de ${dailyGoal}ml",
                            fontSize = 10.sp,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            item {
                Text(
                    text = "💧 Hidratación",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Botón +250ml
                    Chip(
                        onClick = {
                            waterIntake = HydrationManager.addWater(context, 250)
                        },
                        modifier = Modifier.width(80.dp),
                        colors = ChipDefaults.chipColors(backgroundColor = AppColors.Green),
                        label = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.LocalDrink, contentDescription = null)
                                Text("250ml", fontSize = 10.sp)
                            }
                        }
                    )
                    
                    // Botón +500ml
                    Chip(
                        onClick = {
                            waterIntake = HydrationManager.addWater(context, 500)
                        },
                        modifier = Modifier.width(80.dp),
                        colors = ChipDefaults.chipColors(backgroundColor = AppColors.Blue),
                        label = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.LocalDrink, contentDescription = null)
                                Text("500ml", fontSize = 10.sp)
                            }
                        }
                    )
                }
            }
            
            item {
                // Botón personalizado
                Chip(
                    onClick = {
                        // Aquí podrías abrir un diálogo para cantidad personalizada
                        waterIntake = HydrationManager.addWater(context, 100)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ChipDefaults.chipColors(backgroundColor = AppColors.Orange),
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Cantidad personalizada")
                        }
                    }
                )
            }

            // Replace the navigation buttons section in HomeScreen with this:
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Chip(
                            onClick = onStatsClick,
                            modifier = Modifier.width(80.dp),
                            label = { Text("Stats", fontSize = 10.sp) }
                        )
                        Chip(
                            onClick = onGroupsClick,
                            modifier = Modifier.width(80.dp),
                            label = { Text("Grupos", fontSize = 10.sp) }
                        )
                    }
                    Chip(
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .width(80.dp)
                            .padding(top = 4.dp),
                        label = {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    var dailyGoal by remember { mutableStateOf(HydrationManager.getDailyGoal(context)) }
    var reminderInterval by remember { mutableStateOf(HydrationManager.getReminderInterval(context)) }
    
    Scaffold(
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "⚙️ Configuración",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                Text(
                    text = "Meta diaria: ${dailyGoal}ml",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Chip(
                        onClick = {
                            if (dailyGoal > 500) {
                                dailyGoal = HydrationManager.setDailyGoal(context, dailyGoal - 250)
                            }
                        },
                        modifier = Modifier.width(60.dp),
                        label = { Text("-250", fontSize = 10.sp) }
                    )
                    
                    Chip(
                        onClick = {
                            if (dailyGoal < 5000) {
                                dailyGoal = HydrationManager.setDailyGoal(context, dailyGoal + 250)
                            }
                        },
                        modifier = Modifier.width(60.dp),
                        label = { Text("+250", fontSize = 10.sp) }
                    )
                }
            }
            
            item {
                Text(
                    text = "Recordatorio cada: ${reminderInterval}h",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Chip(
                        onClick = {
                            if (reminderInterval > 1) {
                                reminderInterval = HydrationManager.setReminderInterval(context, reminderInterval - 1)
                            }
                        },
                        modifier = Modifier.width(50.dp),
                        label = { Text("-1h", fontSize = 10.sp) }
                    )
                    
                    Chip(
                        onClick = {
                            if (reminderInterval < 8) {
                                reminderInterval = HydrationManager.setReminderInterval(context, reminderInterval + 1)
                            }
                        },
                        modifier = Modifier.width(50.dp),
                        label = { Text("+1h", fontSize = 10.sp) }
                    )
                }
            }
            
            // Botón de notificaciones de prueba
            item {
                val mainActivity = MainActivity.getInstance()
                var isNotificationRunning by remember { mutableStateOf(false) }
                
                Text(
                    text = "🧪 Prueba de Notificaciones",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
                
                Chip(
                    onClick = {
                        mainActivity?.let { activity ->
                            if (isNotificationRunning) {
                                activity.stopLocalNotifications()
                                isNotificationRunning = false
                            } else {
                                activity.startLocalNotifications()
                                isNotificationRunning = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ChipDefaults.chipColors(
                        backgroundColor = if (isNotificationRunning) AppColors.Red else AppColors.Green
                    ),
                    label = {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isNotificationRunning) Icons.Default.NotificationsOff else Icons.Default.Notifications,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isNotificationRunning) "Detener Notificaciones" else "Notificaciones cada 30s",
                                fontSize = 10.sp
                            )
                        }
                    }
                )
            }
            
            item {
                Chip(
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ChipDefaults.chipColors(backgroundColor = MaterialTheme.colors.secondary),
                    label = { Text("Volver") }
                )
            }
        }
    }
}

@Composable
fun StatsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val weekData = remember { HydrationManager.getWeekData(context) }
    
    Scaffold(
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                Text(
                    text = "📊 Estadísticas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            items(weekData) { dayData ->
                Card(
                    onClick = { /* No action needed for stats display */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = dayData.day,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${dayData.intake}ml",
                                fontSize = 12.sp,
                                color = if (dayData.intake >= dayData.goal) AppColors.Green else AppColors.Orange
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.Gray.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction = (dayData.intake.toFloat() / dayData.goal.toFloat()).coerceAtMost(1f))
                                    .background(if (dayData.intake >= dayData.goal) AppColors.Green else AppColors.Orange)
                            )
                        }
                    }
                }
            }
            
            item {
                Chip(
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ChipDefaults.chipColors(backgroundColor = MaterialTheme.colors.secondary),
                    label = { Text("Volver") }
                )
            }
        }
    }
}

data class DayData(
    val day: String,
    val intake: Int,
    val goal: Int
)

// Definición de colores para usar en toda la aplicación
// Estos colores son constantes que reemplazan las referencias directas como Color.Orange
object AppColors {
    val Blue = Color(0xFF2196F3)
    val Green = Color(0xFF4CAF50)
    val Orange = Color(0xFFFF9800)
    val Red = Color(0xFFE53935)
    val LightBlue = Color(0xFF03A9F4)
    val Purple = Color(0xFF9C27B0)
}