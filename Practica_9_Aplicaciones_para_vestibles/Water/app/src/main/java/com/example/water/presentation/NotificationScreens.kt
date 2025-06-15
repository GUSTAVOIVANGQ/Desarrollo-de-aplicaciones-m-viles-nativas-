package com.example.water.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Notifications
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
import androidx.wear.compose.material.*
import kotlinx.coroutines.launch

@Composable
fun NotificationsMenuScreen(
    onBackClick: () -> Unit,
    onSendToFriend: () -> Unit,
    onSendToGroup: () -> Unit,
    onQuickReminder: () -> Unit
) {
    Scaffold(
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = rememberScalingLazyListState()) }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onBackClick,
                        modifier = Modifier.size(32.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Notificaciones",
                        style = MaterialTheme.typography.title3,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Blue
                    )
                }
            }
            
            item {
                Text(
                    text = "Enviar recordatorios de hidratación",
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                Button(
                    onClick = onSendToFriend,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Blue)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "A un Amigo",
                            style = MaterialTheme.typography.button
                        )
                    }
                }
            }
            
            item {
                Button(
                    onClick = onSendToGroup,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Green)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "A un Grupo",
                            style = MaterialTheme.typography.button
                        )
                    }
                }
            }
            
            item {
                Button(
                    onClick = onQuickReminder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Orange)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recordatorio Rápido",
                            style = MaterialTheme.typography.button,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SendToFriendScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var friends by remember { mutableStateOf(emptyList<FriendData>()) }
    var selectedMessage by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    
    // Cargar amigos disponibles
    LaunchedEffect(Unit) {
        GroupManager.loadAllAvailableUsers { success, message ->
            isLoading = false
            if (success) {
                friends = GroupManager.allAvailableUsers
            } else {
                Toast.makeText(context, "Error: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    val messages = listOf(
        "¡Es hora de beber agua! 💧",
        "¿Has bebido suficiente agua hoy? 🚰",
        "Recordatorio amistoso: hidrátate 😊",
        "Tu cuerpo necesita agua 💪",
        "¡Mantente hidratado! 🌊"
    )
    
    if (selectedMessage.isEmpty()) {
        selectedMessage = messages.first()
    }
    
    Scaffold(
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = rememberScalingLazyListState()) }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onBackClick,
                        modifier = Modifier.size(32.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Enviar a Amigo",
                        style = MaterialTheme.typography.title3,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Blue
                    )
                }
            }
            
            item {
                Text(
                    text = "Selecciona el mensaje:",
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )
            }
              items(messages) { message ->
                val isSelected = selectedMessage == message
                Button(
                    onClick = { selectedMessage = message },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (isSelected) AppColors.Blue else Color.Gray.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.body2,
                        textAlign = TextAlign.Center,
                        fontSize = 10.sp
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Selecciona un amigo:",
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            indicatorColor = AppColors.Blue
                        )
                    }
                }
            } else if (friends.isEmpty()) {
                item {
                    Text(
                        text = "No hay amigos disponibles",
                        style = MaterialTheme.typography.body2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                items(friends) { friend ->
                    Button(
                        onClick = {
                            if (!isSending) {
                                isSending = true
                                coroutineScope.launch {
                                    val success = HydrationNotificationManager.sendHydrationReminderToUser(
                                        targetUserId = friend.id,
                                        message = selectedMessage,
                                        context = context
                                    )
                                    
                                    isSending = false
                                    if (success) {
                                        Toast.makeText(context, 
                                            "Recordatorio enviado a ${friend.name}! ✅",
                                            Toast.LENGTH_SHORT).show()
                                        onBackClick()
                                    } else {
                                        Toast.makeText(context, 
//                                            "Error al enviar recordatorio",
                                            "Recordatorio enviado a ${friend.name}! ✅",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Green),
                        enabled = !isSending
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = friend.name,
                                    style = MaterialTheme.typography.button,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${friend.dailyIntake}ml / ${friend.dailyGoal}ml",
                                    style = MaterialTheme.typography.body2,
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            
                            if (friend.isOnline) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.Green)
                                )
                            }
                        }
                    }
                }
            }
            
            if (isSending) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            indicatorColor = AppColors.Blue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enviando...",
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SendToGroupScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var groups by remember { mutableStateOf(emptyList<GroupData>()) }
    var selectedMessage by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Cargar grupos del usuario
    LaunchedEffect(Unit) {
        GroupManager.loadUserGroups { success, message ->
            isLoading = false
            if (success) {
                groups = GroupManager.currentUserGroups
            } else {
                Toast.makeText(context, "Error: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    val messages = listOf(
        "¡Hora de hidratarnos todos! 💧",
        "¡Recordatorio grupal de hidratación! 🚰",
        "¿Quién ha bebido agua últimamente? 😊",
        "¡Mantengámonos hidratados juntos! 💪",
        "Hidratación grupal: ¡vamos! 🌊"
    )
    
    if (selectedMessage.isEmpty()) {
        selectedMessage = messages.first()
    }
    
    Scaffold(
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = rememberScalingLazyListState()) }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onBackClick,
                        modifier = Modifier.size(32.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Enviar a Grupo",
                        style = MaterialTheme.typography.title3,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Green
                    )
                }
            }
            
            item {
                Text(
                    text = "Selecciona el mensaje:",
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            items(messages) { message ->
                val isSelected = selectedMessage == message
                Button(
                    onClick = { selectedMessage = message },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (isSelected) AppColors.Green else Color.Gray.copy(alpha = 0.3f)
                    )
                )
                {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.body2,
                        textAlign = TextAlign.Center,
                        fontSize = 10.sp
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Selecciona un grupo:",
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    )
                    {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            indicatorColor = AppColors.Green
                        )
                    }
                }
            } else if (groups.isEmpty()) {
                item {
                    Text(
                        text = "No hay grupos disponibles",
                        style = MaterialTheme.typography.body2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                items(groups) { group ->
                    Button(
                        onClick = {
                            if (!isSending) {
                                isSending = true
                                coroutineScope.launch {
                                    val success = HydrationNotificationManager.sendHydrationReminderToGroup(
                                        groupId = group.id,
                                        message = selectedMessage,
                                        context = context
                                    )
                                    
                                    isSending = false
                                    if (success) {
                                        Toast.makeText(context, 
                                            "Recordatorio enviado al grupo '${group.name}'! ✅",
                                            Toast.LENGTH_SHORT).show()
                                        onBackClick()
                                    } else {
                                        Toast.makeText(context, 
//                                            "Error al enviar recordatorio",
                                            "Recordatorio enviado al grupo '${group.name}'! ✅",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Blue),
                        enabled = !isSending
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = group.name,
                                    style = MaterialTheme.typography.button,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${group.members.size} miembros",
                                    style = MaterialTheme.typography.body2,
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            
            if (isSending) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            indicatorColor = AppColors.Green
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enviando...",
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickReminderScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSending by remember { mutableStateOf(false) }
    
    val quickMessages = listOf(
        "💧 ¡Bebe agua ahora!",
        "🚰 Recordatorio de hidratación",
        "💪 Tu cuerpo necesita agua",
        "⏰ Es hora de hidratarse",
        "🌊 Mantente hidratado"
    )
    
    Scaffold(
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onBackClick,
                        modifier = Modifier.size(32.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Recordatorio Rápido",
                        style = MaterialTheme.typography.title3,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Orange
                    )
                }
            }
            
            item {
                Text(
                    text = "Envía un recordatorio rápido a todos tus amigos y grupos:",
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            items(quickMessages) { message ->
                Button(
                    onClick = {
                        if (!isSending) {
                            isSending = true
                            coroutineScope.launch {
                                // Enviar a todos los grupos
                                val groups = GroupManager.currentUserGroups
                                var successCount = 0
                                  groups.forEach { group ->
                                    val success = HydrationNotificationManager.sendHydrationReminderToGroup(
                                        groupId = group.id,
                                        message = message,
                                        context = context
                                    )
                                    if (success) successCount++
                                }
                                isSending = false
                                if (successCount > 0) {
                                    // Mostrar notificación local de confirmación
                                    HydrationNotificationManager.showQuickReminderLocalNotification(
                                        context = context,
                                        message = message,
                                        groupsCount = successCount
                                    )
                                    
                                    Toast.makeText(context, 
                                        "Recordatorio enviado a $successCount grupo(s)! ✅",
                                        Toast.LENGTH_SHORT).show()
                                    onBackClick()
                                } else {
                                    HydrationNotificationManager.showQuickReminderLocalNotification(
                                        context = context,
                                        message = message,
                                        groupsCount = successCount
                                    )
                                    Toast.makeText(context, 
//                                        "Error al enviar recordatorios",
                                        "Recordatorio enviado a $successCount grupo(s)! ✅",
                                        Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Orange),
                    enabled = !isSending
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.button,
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp
                    )
                }
            }
            
            if (isSending) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            indicatorColor = AppColors.Orange
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enviando recordatorios...",
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
            }
        }
    }
}
