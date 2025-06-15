package com.example.water_mobile

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

data class GroupData(
    val id: String = "",
    val name: String = "",
    val members: List<String> = emptyList(),
    val createdBy: String = "",
    val createdAt: Long = 0L
)

data class FriendData(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val isOnline: Boolean = false,
    val lastActive: Long = 0L
)

object GroupManager {
    private const val TAG = "GroupManager"
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    var currentUserGroups: List<GroupData> = emptyList()
        private set
    
    private val _onlineFriends = MutableStateFlow<List<FriendData>>(emptyList())
    val onlineFriends: StateFlow<List<FriendData>> = _onlineFriends
    
    private var onlineFriendsListener: ListenerRegistration? = null
    
    /**
     * Carga los grupos del usuario actual
     */
    fun loadUserGroups(callback: (Boolean, String) -> Unit) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            callback(false, "Usuario no autenticado")
            return
        }
        
        firestore.collection("groups")
            .whereArrayContains("members", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                val groups = documents.map { doc ->
                    GroupData(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        members = doc.get("members") as? List<String> ?: emptyList(),
                        createdBy = doc.getString("createdBy") ?: "",
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                }
                currentUserGroups = groups
                callback(true, "Grupos cargados exitosamente")
                Log.d(TAG, "Grupos cargados: ${groups.size}")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error cargando grupos", exception)
                callback(false, "Error al cargar grupos: ${exception.message}")
            }
    }
    
    /**
     * Crea un nuevo grupo
     */
    fun createGroup(groupName: String, callback: (Boolean, String) -> Unit) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            callback(false, "Usuario no autenticado")
            return
        }
        
        val groupData = hashMapOf(
            "name" to groupName,
            "members" to listOf(currentUserId),
            "createdBy" to currentUserId,
            "createdAt" to System.currentTimeMillis()
        )
        
        firestore.collection("groups")
            .add(groupData)
            .addOnSuccessListener { documentRef ->
                Log.d(TAG, "Grupo creado con ID: ${documentRef.id}")
                // Recargar grupos después de crear uno nuevo
                loadUserGroups { _, _ -> }
                callback(true, "Grupo creado exitosamente")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error creando grupo", exception)
                callback(false, "Error al crear grupo: ${exception.message}")
            }
    }
    
    /**
     * Añade un miembro a un grupo
     */
    fun addMemberToGroup(groupId: String, userEmail: String, callback: (Boolean, String) -> Unit) {
        // Primero buscar al usuario por email
        firestore.collection("users")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { userDocs ->
                if (userDocs.isEmpty) {
                    callback(false, "Usuario no encontrado")
                    return@addOnSuccessListener
                }
                
                val userId = userDocs.documents[0].id
                
                // Añadir el usuario al grupo
                firestore.collection("groups").document(groupId)
                    .get()
                    .addOnSuccessListener { groupDoc ->
                        if (groupDoc.exists()) {
                            val currentMembers = groupDoc.get("members") as? List<String> ?: emptyList()
                            if (currentMembers.contains(userId)) {
                                callback(false, "El usuario ya está en el grupo")
                                return@addOnSuccessListener
                            }
                            
                            val updatedMembers = currentMembers + userId
                            firestore.collection("groups").document(groupId)
                                .update("members", updatedMembers)
                                .addOnSuccessListener {
                                    callback(true, "Usuario añadido al grupo")
                                    // Recargar grupos
                                    loadUserGroups { _, _ -> }
                                }
                                .addOnFailureListener { exception ->
                                    callback(false, "Error al añadir usuario: ${exception.message}")
                                }
                        } else {
                            callback(false, "Grupo no encontrado")
                        }
                    }
            }
            .addOnFailureListener { exception ->
                callback(false, "Error buscando usuario: ${exception.message}")
            }
    }
    
    /**
     * Carga los miembros de un grupo específico
     */
    fun loadGroupMembers(groupId: String, callback: (Boolean, String) -> Unit) {
        firestore.collection("groups").document(groupId)
            .get()
            .addOnSuccessListener { groupDoc ->
                if (groupDoc.exists()) {
                    val memberIds = groupDoc.get("members") as? List<String> ?: emptyList()
                    
                    if (memberIds.isEmpty()) {
                        callback(true, "No hay miembros en el grupo")
                        return@addOnSuccessListener
                    }
                    
                    // Cargar información de todos los miembros
                    firestore.collection("users")
                        .whereIn("__name__", memberIds)
                        .get()
                        .addOnSuccessListener { userDocs ->
                            val members = userDocs.map { doc ->
                                FriendData(
                                    id = doc.id,
                                    name = doc.getString("name") ?: "Usuario",
                                    email = doc.getString("email") ?: "",
                                    isOnline = doc.getBoolean("isActive") ?: false,
                                    lastActive = doc.getLong("lastActive") ?: 0L
                                )
                            }
                            Log.d(TAG, "Miembros del grupo cargados: ${members.size}")
                            callback(true, "Miembros cargados")
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Error cargando miembros", exception)
                            callback(false, "Error al cargar miembros: ${exception.message}")
                        }
                } else {
                    callback(false, "Grupo no encontrado")
                }
            }
            .addOnFailureListener { exception ->
                callback(false, "Error al acceder al grupo: ${exception.message}")
            }
    }
    
    /**
     * Carga todos los usuarios disponibles (para añadir a grupos)
     */
    fun loadAllAvailableUsers(callback: (Boolean, String) -> Unit) {
        val currentUserId = auth.currentUser?.uid
        
        firestore.collection("users")
            .whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener { documents ->
                val users = documents.mapNotNull { doc ->
                    // No incluir al usuario actual
                    if (doc.id == currentUserId) return@mapNotNull null
                    
                    FriendData(
                        id = doc.id,
                        name = doc.getString("name") ?: "Usuario",
                        email = doc.getString("email") ?: "",
                        isOnline = doc.getBoolean("isActive") ?: false,
                        lastActive = doc.getLong("lastActive") ?: 0L
                    )
                }
                
                Log.d(TAG, "Usuarios disponibles cargados: ${users.size}")
                callback(true, "Usuarios cargados")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error cargando usuarios", exception)
                callback(false, "Error al cargar usuarios: ${exception.message}")
            }
    }
    
    /**
     * Inicia el listener para monitorear amigos online en tiempo real
     */
    fun startOnlineFriendsListener() {
        val currentUserId = auth.currentUser?.uid ?: return
        
        // Primero obtener todos los amigos de todos los grupos
        val friendIds = mutableSetOf<String>()
        currentUserGroups.forEach { group ->
            group.members.forEach { memberId ->
                if (memberId != currentUserId) {
                    friendIds.add(memberId)
                }
            }
        }
        
        if (friendIds.isEmpty()) {
            _onlineFriends.value = emptyList()
            return
        }
        
        // Configurar listener en tiempo real
        onlineFriendsListener = firestore.collection("users")
            .whereIn("__name__", friendIds.toList())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error en listener de amigos online", error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val onlineFriendsList = snapshot.documents.mapNotNull { doc ->
                        val isOnline = doc.getBoolean("isActive") ?: false
                        val lastActive = doc.getLong("lastActive") ?: 0L
                        
                        // Considerar online si la última actividad fue hace menos de 5 minutos
                        val isRecentlyActive = System.currentTimeMillis() - lastActive < 5 * 60 * 1000
                        
                        FriendData(
                            id = doc.id,
                            name = doc.getString("name") ?: "Usuario",
                            email = doc.getString("email") ?: "",
                            isOnline = isOnline && isRecentlyActive,
                            lastActive = lastActive
                        )
                    }
                    
                    _onlineFriends.value = onlineFriendsList
                    Log.d(TAG, "Amigos online actualizados: ${onlineFriendsList.count { it.isOnline }}")
                }
            }
    }
    
    /**
     * Detiene el listener de amigos online
     */
    fun stopOnlineFriendsListener() {
        onlineFriendsListener?.remove()
        onlineFriendsListener = null
    }
    
    /**
     * Actualiza el estado online del usuario actual
     */
    suspend fun updateUserOnlineStatus(isOnline: Boolean) {
        val currentUserId = auth.currentUser?.uid ?: return
        
        try {
            val updates = hashMapOf<String, Any>(
                "isActive" to isOnline,
                "lastActive" to System.currentTimeMillis()
            )
            
            firestore.collection("users").document(currentUserId)
                .update(updates)
                .await()
                
            Log.d(TAG, "Estado online actualizado: $isOnline")
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando estado online", e)
        }
    }
}
