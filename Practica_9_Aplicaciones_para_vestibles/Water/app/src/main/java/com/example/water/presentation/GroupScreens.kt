package com.example.water.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import kotlinx.coroutines.launch

@Composable
fun GroupsScreen(
    onBackClick: () -> Unit,
    onGroupSelected: (String) -> Unit,
    onCreateGroup: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var groups by remember { mutableStateOf(emptyList<GroupData>()) }
    
    // Cargar grupos al iniciar
    LaunchedEffect(Unit) {
        GroupManager.loadUserGroups { success, message ->
            isLoading = false
            if (success) {
                groups = GroupManager.currentUserGroups
            } else {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
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
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onBackClick,
                        modifier = Modifier.size(32.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.surface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colors.onSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "👥 Mis Grupos",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (groups.isEmpty()) {
                item {
                    Text(
                        text = "No tienes grupos",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                }
            } else {
                items(groups) { group ->
                    Chip(
                        onClick = { onGroupSelected(group.id) },
                        colors = ChipDefaults.chipColors(
                            backgroundColor = Color(0xFF3F51B5)
                        ),
                        label = {
                            Column {
                                Text(
                                    text = group.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${group.members.size} miembros",
                                    fontSize = 10.sp
                                )
                            }
                        },
                        icon = {
                            Icon(Icons.Default.Group, contentDescription = null)
                        }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                
                Chip(
                    onClick = onCreateGroup,
                    colors = ChipDefaults.chipColors(
                        backgroundColor = Color(0xFF4CAF50)
                    ),
                    label = {
                        Text("Crear grupo")
                    },
                    icon = {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                )
            }
        }
    }
}

@Composable
fun CreateGroupScreen(
    onBackClick: () -> Unit,
    onGroupCreated: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var groupName by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }
    
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
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onBackClick,
                        modifier = Modifier.size(32.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.surface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colors.onSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "✨ Nuevo Grupo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                Text(
                    text = "Nombre del grupo:",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                
                // En un wearable real, esto abriría un input de texto o reconocimiento de voz
                // Para simplificar, usamos un chip que "simula" la entrada
                Chip(
                    onClick = {
                        // En un caso real, esto abriría un diálogo para ingresar texto
                        groupName = "Grupo ${(1..100).random()}"
                        Toast.makeText(context, "Nombre asignado: $groupName", Toast.LENGTH_SHORT).show()
                    },
                    label = {
                        Text(if (groupName.isEmpty()) "Tocar para asignar nombre" else groupName)
                    },
                    colors = ChipDefaults.chipColors(
                        backgroundColor = MaterialTheme.colors.surface
                    )
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isCreating) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp)
                        )
                    }
                } else {
                    Chip(
                        onClick = {
                            if (groupName.isEmpty()) {
                                Toast.makeText(context, "Ingresa un nombre para el grupo", Toast.LENGTH_SHORT).show()
                                return@Chip
                            }
                            
                            isCreating = true
                            coroutineScope.launch {
                                GroupManager.createGroup(groupName) { success, message ->
                                    isCreating = false
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    if (success) {
                                        onGroupCreated()
                                    }
                                }
                            }
                        },
                        label = {
                            Text("Crear Grupo")
                        },
                        colors = ChipDefaults.chipColors(
                            backgroundColor = Color(0xFF4CAF50)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun GroupDetailScreen(
    groupId: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var members by remember { mutableStateOf(emptyList<FriendData>()) }
    var group by remember { mutableStateOf<GroupData?>(null) }
    var friendEmail by remember { mutableStateOf("") }
    
    // Cargar datos del grupo
    LaunchedEffect(groupId) {
        // Buscar el grupo en la lista local
        val foundGroup = GroupManager.currentUserGroups.find { it.id == groupId }
        group = foundGroup
        
        // Cargar miembros del grupo
        GroupManager.loadGroupMembers(groupId) { success, message ->
            isLoading = false
            if (success) {
                members = GroupManager.currentGroupMembers
            } else {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    val onlineFriends by GroupManager.onlineFriends.collectAsState()
    
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
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onBackClick,
                        modifier = Modifier.size(32.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.surface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colors.onSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = group?.name ?: "Grupo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                Text(
                    text = "👥 Miembros: ${members.size}",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                Text(
                    text = "🟢 Online: $onlineFriends",
                    fontSize = 14.sp,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (members.isEmpty()) {
                item {
                    Text(
                        text = "No hay miembros en este grupo",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                }
            } else {
                items(members) { member ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Indicador de online
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (member.isOnline) Color(0xFF4CAF50) else Color.Gray)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3F51B5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Nombre e información
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = member.name.ifEmpty { "Usuario" },
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            Text(
                                text = "${member.dailyIntake}ml / ${member.dailyGoal}ml",
                                fontSize = 10.sp,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                
                Chip(
                    onClick = {
                        // En un caso real, esto abriría un diálogo para ingresar email
                        friendEmail = "amigo${(1..100).random()}@ejemplo.com"
                        
                        coroutineScope.launch {
                            GroupManager.addFriendToGroup(groupId, friendEmail) { success, message ->
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                if (success) {
                                    // Recargar miembros
                                    isLoading = true
                                    GroupManager.loadGroupMembers(groupId) { loadSuccess, _ ->
                                        isLoading = false
                                        if (loadSuccess) {
                                            members = GroupManager.currentGroupMembers
                                        }
                                    }
                                }
                            }
                        }
                    },
                    colors = ChipDefaults.chipColors(
                        backgroundColor = Color(0xFF4CAF50)
                    ),
                    label = {
                        Text("Añadir amigo")
                    },
                    icon = {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                )
            }
            
            item {
                Chip(
                    onClick = {
                        coroutineScope.launch {
                            GroupManager.leaveGroup(groupId) { success, message ->
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                if (success) {
                                    onBackClick()
                                }
                            }
                        }
                    },
                    colors = ChipDefaults.chipColors(
                        backgroundColor = Color(0xFFE53935)
                    ),
                    label = {
                        Text("Salir del grupo")
                    }
                )
            }
        }
    }
}
