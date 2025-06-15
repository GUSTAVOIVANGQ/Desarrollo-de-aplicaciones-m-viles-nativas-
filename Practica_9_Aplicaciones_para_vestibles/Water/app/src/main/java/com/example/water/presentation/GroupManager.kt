package com.example.water.presentation

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class FriendData(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val fcmToken: String = "",
    val photoUrl: String = "",
    val role: String = "",
    val createdAt: Long = 0,
    val lastActive: Long = 0,
    val isOnline: Boolean = false,
    val dailyIntake: Int = 0,
    val dailyGoal: Int = 2000
)

data class GroupData(
    val id: String = "",
    val name: String = "",
    val ownerUserId: String = "",
    val members: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

object GroupManager {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _currentUserGroups = mutableStateListOf<GroupData>()
    val currentUserGroups: List<GroupData> = _currentUserGroups
    
    private val _currentGroupMembers = mutableStateListOf<FriendData>()
    val currentGroupMembers: List<FriendData> = _currentGroupMembers
    
    private val _onlineFriends = MutableStateFlow(0)
    val onlineFriends: StateFlow<Int> = _onlineFriends
    
    // Lista de todos los usuarios disponibles (Firebase + ficticios)
    private val _allAvailableUsers = mutableStateListOf<FriendData>()
    val allAvailableUsers: List<FriendData> = _allAvailableUsers
    
    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: ""
    }
    
    fun createGroup(groupName: String, onComplete: (Boolean, String) -> Unit) {
        val currentUserId = getCurrentUserId()
        if (currentUserId.isEmpty()) {
            onComplete(false, "Usuario no autenticado")
            return
        }
        
        val groupId = firestore.collection("groups").document().id
        val newGroup = GroupData(
            id = groupId,
            name = groupName,
            ownerUserId = currentUserId,
            members = listOf(currentUserId)
        )
        
        firestore.collection("groups").document(groupId)
            .set(newGroup)
            .addOnSuccessListener {
                _currentUserGroups.add(newGroup)
                onComplete(true, "Grupo creado exitosamente")
            }
            .addOnFailureListener { e ->
                onComplete(false, "Error al crear grupo: ${e.message}")
            }
    }
    
    fun loadUserGroups(onComplete: (Boolean, String) -> Unit) {
        val currentUserId = getCurrentUserId()
        if (currentUserId.isEmpty()) {
            onComplete(false, "Usuario no autenticado")
            return
        }
        
        _currentUserGroups.clear()
        
        firestore.collection("groups")
            .whereArrayContains("members", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val group = document.toObject(GroupData::class.java)
                    _currentUserGroups.add(group)
                }
                onComplete(true, "Grupos cargados: ${documents.size()}")
            }
            .addOnFailureListener { e ->
                onComplete(false, "Error al cargar grupos: ${e.message}")
            }
    }
      fun loadGroupMembers(groupId: String, onComplete: (Boolean, String) -> Unit) {
        _currentGroupMembers.clear()
        
        val group = _currentUserGroups.find { it.id == groupId }
        if (group == null) {
            onComplete(false, "Grupo no encontrado")
            return
        }
        
        var completedCount = 0
        var errorCount = 0
        var totalMembers = group.members.size
        
        // Si no hay miembros, terminar inmediatamente
        if (totalMembers == 0) {
            onComplete(true, "No hay miembros en este grupo")
            return
        }
        
        group.members.forEach { memberId ->
            // Verificar si es un usuario ficticio
            val fictitiousUser = _allAvailableUsers.find { it.id == memberId }
            if (fictitiousUser != null) {
                _currentGroupMembers.add(fictitiousUser)
                updateOnlineFriendsCount()
                completedCount++
                checkIfComplete(completedCount, errorCount, totalMembers, onComplete)
            } else {
                // Buscar en Firebase
                firestore.collection("users").document(memberId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val userData = document.toObject(FriendData::class.java)?.copy(
                                id = memberId,
                                // Simulamos aleatoriamente si están en línea
                                isOnline = Math.random() > 0.5
                            ) ?: FriendData(id = memberId)
                            
                            _currentGroupMembers.add(userData)
                            
                            // Actualizar contador de amigos online
                            updateOnlineFriendsCount()
                        }
                        
                        completedCount++
                        checkIfComplete(completedCount, errorCount, totalMembers, onComplete)
                    }
                    .addOnFailureListener { e ->
                        errorCount++
                        completedCount++
                        checkIfComplete(completedCount, errorCount, totalMembers, onComplete)
                    }
            }
        }
    }
      private fun updateOnlineFriendsCount() {
        // Contar usuarios online tanto en la lista de miembros del grupo como en todos los usuarios
        val onlineGroupMembers = _currentGroupMembers.count { it.isOnline }
        val onlineAllUsers = _allAvailableUsers.count { it.isOnline }
        
        // Usar el número mayor para mostrar más actividad
        _onlineFriends.value = maxOf(onlineGroupMembers, onlineAllUsers)
    }
    
    private fun checkIfComplete(
        completed: Int, 
        errors: Int, 
        total: Int, 
        onComplete: (Boolean, String) -> Unit
    ) {
        if (completed == total) {
            val success = errors < total
            val message = if (success) {
                "Miembros cargados: ${_currentGroupMembers.size}"
            } else {
                "Algunos miembros no pudieron ser cargados"
            }
            onComplete(success, message)
        }
    }
      fun addFriendToGroup(groupId: String, friendEmail: String, onComplete: (Boolean, String) -> Unit) {
        // Primero verificar si es un usuario ficticio en la lista local
        val fictitiousUser = _allAvailableUsers.find { 
            it.email.equals(friendEmail, ignoreCase = true) && it.id.startsWith("fictitious_") 
        }
        
        if (fictitiousUser != null) {
            // Es un usuario ficticio, simular añadirlo al grupo
            val groupIndex = _currentUserGroups.indexOfFirst { it.id == groupId }
            if (groupIndex >= 0) {
                val updatedGroup = _currentUserGroups[groupIndex].copy(
                    members = _currentUserGroups[groupIndex].members + fictitiousUser.id
                )
                _currentUserGroups[groupIndex] = updatedGroup
            }
            onComplete(true, "Amigo ${fictitiousUser.name} añadido al grupo")
            return
        }
        
        // Buscar usuario real en Firebase por email
        firestore.collection("users")
            .whereEqualTo("email", friendEmail)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    onComplete(false, "Usuario no encontrado")
                    return@addOnSuccessListener
                }
                
                val friendDoc = documents.documents[0]
                val friendId = friendDoc.id
                
                // Ahora actualizar el grupo en Firebase
                firestore.collection("groups").document(groupId)
                    .update("members", com.google.firebase.firestore.FieldValue.arrayUnion(friendId))
                    .addOnSuccessListener {
                        // Actualizar la lista local
                        val groupIndex = _currentUserGroups.indexOfFirst { it.id == groupId }
                        if (groupIndex >= 0) {
                            val updatedGroup = _currentUserGroups[groupIndex].copy(
                                members = _currentUserGroups[groupIndex].members + friendId
                            )
                            _currentUserGroups[groupIndex] = updatedGroup
                        }
                        onComplete(true, "Amigo añadido al grupo")
                    }
                    .addOnFailureListener { e ->
                        onComplete(false, "Error al añadir amigo: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                onComplete(false, "Error al buscar usuario: ${e.message}")
            }
    }
    
    fun joinGroup(groupId: String, onComplete: (Boolean, String) -> Unit) {
        val currentUserId = getCurrentUserId()
        if (currentUserId.isEmpty()) {
            onComplete(false, "Usuario no autenticado")
            return
        }
        
        firestore.collection("groups").document(groupId)
            .update("members", com.google.firebase.firestore.FieldValue.arrayUnion(currentUserId))
            .addOnSuccessListener {
                // Recargar los grupos del usuario
                loadUserGroups { success, message ->
                    onComplete(success, if (success) "Te has unido al grupo" else message)
                }
            }
            .addOnFailureListener { e ->
                onComplete(false, "Error al unirse al grupo: ${e.message}")
            }
    }
      fun leaveGroup(groupId: String, onComplete: (Boolean, String) -> Unit) {
        val currentUserId = getCurrentUserId()
        if (currentUserId.isEmpty()) {
            onComplete(false, "Usuario no autenticado")
            return
        }
        
        firestore.collection("groups").document(groupId)
            .update("members", com.google.firebase.firestore.FieldValue.arrayRemove(currentUserId))
            .addOnSuccessListener {
                // Eliminar el grupo de la lista local
                val index = _currentUserGroups.indexOfFirst { it.id == groupId }
                if (index >= 0) {
                    _currentUserGroups.removeAt(index)
                }
                onComplete(true, "Has salido del grupo")
            }
            .addOnFailureListener { e ->
                onComplete(false, "Error al salir del grupo: ${e.message}")
            }
    }
      /**
     * Carga todos los usuarios disponibles desde Firebase más usuarios ficticios
     * con puntos verdes que simulan conexión en tiempo real
     */
    fun loadAllAvailableUsers(onComplete: (Boolean, String) -> Unit) {
        val currentUserId = getCurrentUserId()
        if (currentUserId.isEmpty()) {
            onComplete(false, "Usuario no autenticado")
            return
        }
        
        _allAvailableUsers.clear()
        
        // Cargar usuarios reales de Firebase
        firestore.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                val firebaseUsers = mutableListOf<FriendData>()
                
                for (document in documents) {
                    try {
                        val userData = document.data
                        val userId = document.id
                        
                        // No incluir al usuario actual
                        if (userId == currentUserId) continue
                        
                        val friendData = FriendData(
                            id = userId,
                            name = userData["username"] as? String ?: (userData["email"] as? String)?.substringBefore("@") ?: "Usuario",
                            username = userData["username"] as? String ?: "",
                            email = userData["email"] as? String ?: "",
                            fcmToken = userData["fcmToken"] as? String ?: "",
                            photoUrl = userData["photoUrl"] as? String ?: "",
                            role = userData["role"] as? String ?: "user",
                            createdAt = userData["createdAt"] as? Long ?: System.currentTimeMillis(),
                            lastActive = System.currentTimeMillis() - (0..3600000).random(), // Última actividad simulada
                            isOnline = (0..1).random() == 1, // Conexión simulada
                            dailyIntake = (500..2500).random(),
                            dailyGoal = listOf(1500, 2000, 2500, 3000).random()
                        )
                        
                        firebaseUsers.add(friendData)
                    } catch (e: Exception) {
                        android.util.Log.e("GroupManager", "Error procesando usuario: ${e.message}")
                    }
                }
                
                // Agregar usuarios ficticios adicionales si hay pocos usuarios reales
                val fictitiousUsers = if (firebaseUsers.size < 5) {
                    listOf(
                        FriendData(
                            id = "fictitious_1",
                            name = "Ana García",
                            username = "ana_garcia",
                            email = "ana.garcia@example.com",
                            fcmToken = "fake_token_1",
                            lastActive = System.currentTimeMillis() - 1800000,
                            isOnline = true,
                            dailyIntake = 1800,
                            dailyGoal = 2000
                        ),
                        FriendData(
                            id = "fictitious_2", 
                            name = "Carlos López",
                            username = "carlos_lopez",
                            email = "carlos.lopez@example.com",
                            fcmToken = "fake_token_2",
                            lastActive = System.currentTimeMillis() - 300000,
                            isOnline = true,
                            dailyIntake = 1200,
                            dailyGoal = 2500
                        ),
                        FriendData(
                            id = "fictitious_3",
                            name = "María Rodriguez",
                            username = "maria_rodriguez", 
                            email = "maria.rodriguez@example.com",
                            fcmToken = "fake_token_3",
                            lastActive = System.currentTimeMillis() - 7200000,
                            isOnline = false,
                            dailyIntake = 2200,
                            dailyGoal = 2000
                        ),
                        FriendData(
                            id = "fictitious_4",
                            name = "David Martín",
                            username = "david_martin",
                            email = "david.martin@example.com",
                            fcmToken = "fake_token_4",
                            lastActive = System.currentTimeMillis() - 900000,
                            isOnline = true,
                            dailyIntake = 1500,
                            dailyGoal = 3000
                        ),
                        FriendData(
                            id = "fictitious_5",
                            name = "Laura Sánchez",
                            username = "laura_sanchez",
                            email = "laura.sanchez@example.com",
                            fcmToken = "fake_token_5",
                            lastActive = System.currentTimeMillis() - 600000,
                            isOnline = false,
                            dailyIntake = 800,
                            dailyGoal = 1500
                        )
                    )
                } else emptyList()
                
                // Combinar usuarios reales y ficticios
                val allUsers = firebaseUsers + fictitiousUsers
                _allAvailableUsers.addAll(allUsers.sortedBy { it.name })
                
                updateOnlineFriendsCount()
                onComplete(true, "Usuarios cargados: ${firebaseUsers.size} reales, ${fictitiousUsers.size} ficticios")
            }
            .addOnFailureListener { exception ->
                android.util.Log.e("GroupManager", "Error cargando usuarios: ${exception.message}")
                
                // En caso de error, cargar solo usuarios ficticios
                val fictitiousUsers = listOf(
                    FriendData(
                        id = "fictitious_1",
                        name = "Ana García",
                        username = "ana_garcia",
                        email = "ana.garcia@example.com",
                        fcmToken = "fake_token_1",
                        lastActive = System.currentTimeMillis() - 1800000,
                        isOnline = true,
                        dailyIntake = 1800,
                        dailyGoal = 2000
                    ),
                    FriendData(
                        id = "fictitious_2",
                        name = "Carlos López", 
                        username = "carlos_lopez",
                        email = "carlos.lopez@example.com",
                        fcmToken = "fake_token_2",
                        lastActive = System.currentTimeMillis() - 300000,
                        isOnline = true,
                        dailyIntake = 1200,
                        dailyGoal = 2500
                    ),
                    FriendData(
                        id = "fictitious_3",
                        name = "María Rodriguez",
                        username = "maria_rodriguez",
                        email = "maria.rodriguez@example.com",
                        fcmToken = "fake_token_3",
                        lastActive = System.currentTimeMillis() - 7200000,
                        isOnline = false,
                        dailyIntake = 2200,
                        dailyGoal = 2000
                    )
                )
                
                _allAvailableUsers.addAll(fictitiousUsers)
                updateOnlineFriendsCount()
                onComplete(false, "Error conectando a Firebase. Mostrando usuarios de ejemplo.")
            }
    }
    
    /**
     * Busca un usuario por email en la lista de usuarios disponibles
     */
    fun findUserByEmail(email: String): FriendData? {
        return _allAvailableUsers.find { it.email.equals(email, ignoreCase = true) }
    }
    
    /**
     * Obtiene usuarios online de la lista actual
     */
    fun getOnlineUsers(): List<FriendData> {
        return _allAvailableUsers.filter { it.isOnline }
    }
    
    /**
     * Refresca el estado online de los usuarios (simulado)
     */
    fun refreshOnlineStatus() {
        _allAvailableUsers.forEachIndexed { index, user ->
            // Simular cambios de estado online ocasionales
            if ((0..10).random() == 1) { // 10% de probabilidad de cambio
                val updatedUser = user.copy(
                    isOnline = !user.isOnline,
                    lastActive = if (!user.isOnline) System.currentTimeMillis() else user.lastActive
                )
                _allAvailableUsers[index] = updatedUser
            }
        }
        updateOnlineFriendsCount()
    }
}
