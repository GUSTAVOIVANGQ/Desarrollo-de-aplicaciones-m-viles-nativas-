package com.example.systembooks.firebase;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.systembooks.models.FriendRequest;
import com.example.systembooks.models.Friendship;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository class for handling friendship operations with Firebase
 */
public class FriendshipRepository {
    private static final String TAG = "FriendshipRepository";
    private static final String FRIEND_REQUESTS_COLLECTION = "friend_requests";
    private static final String FRIENDSHIPS_COLLECTION = "friendships";
    private static final String USERS_COLLECTION = "users";

    private final FirebaseFirestore db;
    private final Context context;
    private final NotificationHelper notificationHelper;

    public FriendshipRepository(Context context) {
        this.context = context;
        this.db = FirebaseManager.getInstance().getFirestore();
        this.notificationHelper = new NotificationHelper(context);
    }

    /**
     * Send a friend request to another user
     * @param receiverUserId ID of the user to send request to
     * @param callback Callback to handle the result
     */
    public void sendFriendRequest(String receiverUserId, FirebaseAuthRepository.FirebaseCallback<FriendRequest> callback) {
        String currentUserId = FirebaseManager.getInstance().getAuth().getUid();
        if (currentUserId == null) {
            callback.onError("User not authenticated");
            return;
        }

        if (currentUserId.equals(receiverUserId)) {
            callback.onError("No puedes enviarte una solicitud de amistad a ti mismo");
            return;
        }

        // First, check if there's already a pending request between these users
        checkExistingRequest(currentUserId, receiverUserId, new FirebaseAuthRepository.FirebaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean hasExistingRequest) {
                if (hasExistingRequest) {
                    callback.onError("Ya existe una solicitud de amistad pendiente");
                    return;
                }

                // Check if they are already friends
                checkIfAlreadyFriends(currentUserId, receiverUserId, new FirebaseAuthRepository.FirebaseCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean areFriends) {
                        if (areFriends) {
                            callback.onError("Ya son amigos");
                            return;
                        }

                        // Get current user data and receiver user data
                        getUserData(currentUserId, new FirebaseAuthRepository.FirebaseCallback<FirebaseUser>() {
                            @Override
                            public void onSuccess(FirebaseUser currentUser) {
                                getUserData(receiverUserId, new FirebaseAuthRepository.FirebaseCallback<FirebaseUser>() {
                                    @Override
                                    public void onSuccess(FirebaseUser receiverUser) {
                                        // Create and send the friend request
                                        FriendRequest friendRequest = new FriendRequest(
                                                currentUserId, currentUser.getUsername(), currentUser.getEmail(), currentUser.getPhotoUrl(),
                                                receiverUserId, receiverUser.getUsername(), receiverUser.getEmail(), receiverUser.getPhotoUrl()
                                        );

                                        db.collection(FRIEND_REQUESTS_COLLECTION)
                                                .add(friendRequest.toMap())
                                                .addOnSuccessListener(documentReference -> {
                                                    friendRequest.setId(documentReference.getId());
                                                    Log.d(TAG, "Friend request sent successfully");

                                                    // Send notification to receiver
                                                    sendFriendRequestNotification(receiverUserId, currentUser.getUsername());

                                                    callback.onSuccess(friendRequest);
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e(TAG, "Error sending friend request", e);
                                                    callback.onError("Error enviando solicitud: " + e.getMessage());
                                                });
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        callback.onError("Error obteniendo datos del usuario receptor: " + errorMessage);
                                    }
                                });
                            }

                            @Override
                            public void onError(String errorMessage) {
                                callback.onError("Error obteniendo tus datos: " + errorMessage);
                            }
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        callback.onError("Error verificando amistad: " + errorMessage);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError("Error verificando solicitudes existentes: " + errorMessage);
            }
        });
    }

    /**
     * Accept a friend request
     * @param requestId ID of the friend request
     * @param callback Callback to handle the result
     */
    public void acceptFriendRequest(String requestId, FirebaseAuthRepository.FirebaseCallback<Friendship> callback) {
        db.collection(FRIEND_REQUESTS_COLLECTION).document(requestId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            FriendRequest request = task.getResult().toObject(FriendRequest.class);
                            if (request != null) {
                                // Update request status
                                request.setStatus(FriendRequest.STATUS_ACCEPTED);

                                db.collection(FRIEND_REQUESTS_COLLECTION).document(requestId)
                                        .update("status", FriendRequest.STATUS_ACCEPTED, "updatedAt", System.currentTimeMillis())
                                        .addOnSuccessListener(aVoid -> {
                                            // Create friendship
                                            Friendship friendship = new Friendship(
                                                    request.getSenderId(), request.getSenderName(), request.getSenderEmail(), request.getSenderPhotoUrl(),
                                                    request.getReceiverId(), request.getReceiverName(), request.getReceiverEmail(), request.getReceiverPhotoUrl()
                                            );

                                            db.collection(FRIENDSHIPS_COLLECTION)
                                                    .add(friendship.toMap())
                                                    .addOnSuccessListener(friendshipRef -> {
                                                        friendship.setId(friendshipRef.getId());
                                                        Log.d(TAG, "Friendship created successfully");

                                                        // Send notification to the sender
                                                        sendFriendRequestAcceptedNotification(request.getSenderId(), request.getReceiverName());

                                                        callback.onSuccess(friendship);
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e(TAG, "Error creating friendship", e);
                                                        callback.onError("Error creando amistad: " + e.getMessage());
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Error updating request status", e);
                                            callback.onError("Error actualizando solicitud: " + e.getMessage());
                                        });
                            } else {
                                callback.onError("Solicitud de amistad no válida");
                            }
                        } else {
                            Log.e(TAG, "Friend request not found");
                            callback.onError("Solicitud de amistad no encontrada");
                        }
                    }
                });
    }

    /**
     * Reject a friend request
     * @param requestId ID of the friend request
     * @param callback Callback to handle the result
     */
    public void rejectFriendRequest(String requestId, FirebaseAuthRepository.FirebaseCallback<Void> callback) {
        db.collection(FRIEND_REQUESTS_COLLECTION).document(requestId)
                .update("status", FriendRequest.STATUS_REJECTED, "updatedAt", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Friend request rejected successfully");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error rejecting friend request", e);
                    callback.onError("Error rechazando solicitud: " + e.getMessage());
                });
    }

    /**
     * Get incoming friend requests for the current user
     * @param callback Callback to handle the result
     */    public void getIncomingFriendRequests(FirebaseAuthRepository.FirebaseCallback<List<FriendRequest>> callback) {
        String currentUserId = FirebaseManager.getInstance().getAuth().getUid();
        if (currentUserId == null) {
            callback.onError("User not authenticated");
            return;
        }

        Log.d(TAG, "Getting incoming friend requests for user: " + currentUserId);

        db.collection(FRIEND_REQUESTS_COLLECTION)
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("status", FriendRequest.STATUS_PENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<FriendRequest> requests = new ArrayList<>();
                            Log.d(TAG, "Query successful, found " + task.getResult().size() + " incoming requests");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    FriendRequest request = document.toObject(FriendRequest.class);
                                    request.setId(document.getId());
                                    requests.add(request);
                                    Log.d(TAG, "Found incoming request from: " + request.getSenderName());
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing friend request document: " + document.getId(), e);
                                }
                            }
                            callback.onSuccess(requests);
                        } else {
                            Log.e(TAG, "Error getting friend requests", task.getException());
                            callback.onError("Error obteniendo solicitudes: " + 
                                    (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        }
                    }
                });
    }

    /**
     * Get outgoing friend requests from the current user
     * @param callback Callback to handle the result
     */    public void getOutgoingFriendRequests(FirebaseAuthRepository.FirebaseCallback<List<FriendRequest>> callback) {
        String currentUserId = FirebaseManager.getInstance().getAuth().getUid();
        if (currentUserId == null) {
            callback.onError("User not authenticated");
            return;
        }

        Log.d(TAG, "Getting outgoing friend requests for user: " + currentUserId);

        db.collection(FRIEND_REQUESTS_COLLECTION)
                .whereEqualTo("senderId", currentUserId)
                .whereEqualTo("status", FriendRequest.STATUS_PENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<FriendRequest> requests = new ArrayList<>();
                            Log.d(TAG, "Query successful, found " + task.getResult().size() + " outgoing requests");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    FriendRequest request = document.toObject(FriendRequest.class);
                                    request.setId(document.getId());
                                    requests.add(request);
                                    Log.d(TAG, "Found outgoing request to: " + request.getReceiverName());
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing friend request document: " + document.getId(), e);
                                }
                            }
                            callback.onSuccess(requests);
                        } else {
                            Log.e(TAG, "Error getting outgoing friend requests", task.getException());
                            callback.onError("Error obteniendo solicitudes enviadas: " + 
                                    (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        }
                    }
                });
    }

    /**
     * Get the user's friends list
     * @param callback Callback to handle the result
     */    public void getFriendsList(FirebaseAuthRepository.FirebaseCallback<List<Map<String, String>>> callback) {
        String currentUserId = FirebaseManager.getInstance().getAuth().getUid();
        if (currentUserId == null) {
            callback.onError("User not authenticated");
            return;
        }

        Log.d(TAG, "Getting friends list for user: " + currentUserId);

        // Query for friendships where current user is either user1 or user2
        db.collection(FRIENDSHIPS_COLLECTION)
                .whereEqualTo("user1Id", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot1 -> {
                    Log.d(TAG, "Found " + querySnapshot1.size() + " friendships where user is user1");
                    
                    db.collection(FRIENDSHIPS_COLLECTION)
                            .whereEqualTo("user2Id", currentUserId)
                            .get()
                            .addOnSuccessListener(querySnapshot2 -> {
                                Log.d(TAG, "Found " + querySnapshot2.size() + " friendships where user is user2");
                                
                                List<Map<String, String>> friends = new ArrayList<>();
                                
                                // Process friendships where current user is user1
                                for (QueryDocumentSnapshot document : querySnapshot1) {
                                    Friendship friendship = document.toObject(Friendship.class);
                                    friendship.setId(document.getId());
                                    Map<String, String> friendInfo = friendship.getFriendInfo(currentUserId);
                                    friends.add(friendInfo);
                                    Log.d(TAG, "Added friend from user1 query: " + friendInfo.get("name"));
                                }
                                
                                // Process friendships where current user is user2
                                for (QueryDocumentSnapshot document : querySnapshot2) {
                                    Friendship friendship = document.toObject(Friendship.class);
                                    friendship.setId(document.getId());
                                    Map<String, String> friendInfo = friendship.getFriendInfo(currentUserId);
                                    friends.add(friendInfo);
                                    Log.d(TAG, "Added friend from user2 query: " + friendInfo.get("name"));
                                }
                                
                                Log.d(TAG, "Total friends found: " + friends.size());
                                callback.onSuccess(friends);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error getting friends list (user2)", e);
                                callback.onError("Error obteniendo lista de amigos: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting friends list (user1)", e);
                    callback.onError("Error obteniendo lista de amigos: " + e.getMessage());
                });
    }

    /**
     * Search users by username or email to send friend requests
     * @param searchQuery Search query (username or email)
     * @param callback Callback to handle the result
     */
    public void searchUsers(String searchQuery, FirebaseAuthRepository.FirebaseCallback<List<FirebaseUser>> callback) {
        String currentUserId = FirebaseManager.getInstance().getAuth().getUid();
        if (currentUserId == null) {
            callback.onError("User not authenticated");
            return;
        }

        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            callback.onError("Ingresa un término de búsqueda");
            return;
        }

        String query = searchQuery.trim().toLowerCase();

        // Search by username first
        db.collection(USERS_COLLECTION)
                .orderBy("username")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(20)
                .get()
                .addOnSuccessListener(usernameResults -> {
                    // Then search by email
                    db.collection(USERS_COLLECTION)
                            .orderBy("email")
                            .startAt(query)
                            .endAt(query + "\uf8ff")
                            .limit(20)
                            .get()
                            .addOnSuccessListener(emailResults -> {
                                List<FirebaseUser> users = new ArrayList<>();
                                List<String> addedUserIds = new ArrayList<>();

                                // Add users from username search
                                for (QueryDocumentSnapshot document : usernameResults) {
                                    String userId = document.getId();
                                    if (!userId.equals(currentUserId) && !addedUserIds.contains(userId)) {
                                        FirebaseUser user = document.toObject(FirebaseUser.class);
                                        user.setUid(userId);
                                        users.add(user);
                                        addedUserIds.add(userId);
                                    }
                                }

                                // Add users from email search (avoiding duplicates)
                                for (QueryDocumentSnapshot document : emailResults) {
                                    String userId = document.getId();
                                    if (!userId.equals(currentUserId) && !addedUserIds.contains(userId)) {
                                        FirebaseUser user = document.toObject(FirebaseUser.class);
                                        user.setUid(userId);
                                        users.add(user);
                                        addedUserIds.add(userId);
                                    }
                                }

                                callback.onSuccess(users);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error searching users by email", e);
                                callback.onError("Error buscando usuarios: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error searching users by username", e);
                    callback.onError("Error buscando usuarios: " + e.getMessage());
                });
    }

    // Helper methods

    private void checkExistingRequest(String senderId, String receiverId, FirebaseAuthRepository.FirebaseCallback<Boolean> callback) {
        db.collection(FRIEND_REQUESTS_COLLECTION)
                .whereEqualTo("senderId", senderId)
                .whereEqualTo("receiverId", receiverId)
                .whereEqualTo("status", FriendRequest.STATUS_PENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    callback.onSuccess(!querySnapshot.isEmpty());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking existing request", e);
                    callback.onError("Error verificando solicitudes existentes");
                });
    }

    private void checkIfAlreadyFriends(String user1Id, String user2Id, FirebaseAuthRepository.FirebaseCallback<Boolean> callback) {
        db.collection(FRIENDSHIPS_COLLECTION)
                .whereEqualTo("user1Id", user1Id)
                .whereEqualTo("user2Id", user2Id)
                .get()
                .addOnSuccessListener(querySnapshot1 -> {
                    if (!querySnapshot1.isEmpty()) {
                        callback.onSuccess(true);
                        return;
                    }

                    // Check reverse relationship
                    db.collection(FRIENDSHIPS_COLLECTION)
                            .whereEqualTo("user1Id", user2Id)
                            .whereEqualTo("user2Id", user1Id)
                            .get()
                            .addOnSuccessListener(querySnapshot2 -> {
                                callback.onSuccess(!querySnapshot2.isEmpty());
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error checking friendship (reverse)", e);
                                callback.onError("Error verificando amistad");
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking friendship", e);
                    callback.onError("Error verificando amistad");
                });
    }

    private void getUserData(String userId, FirebaseAuthRepository.FirebaseCallback<FirebaseUser> callback) {
        db.collection(USERS_COLLECTION).document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            FirebaseUser user = task.getResult().toObject(FirebaseUser.class);
                            if (user != null) {
                                user.setUid(userId);
                                callback.onSuccess(user);
                            } else {
                                callback.onError("User data not found");
                            }
                        } else {
                            Log.e(TAG, "Error getting user data", task.getException());
                            callback.onError("Error getting user data: " + 
                                    (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        }
                    }
                });
    }

    private void sendFriendRequestNotification(String receiverId, String senderName) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "friend_request");
        data.put("senderName", senderName);

        notificationHelper.sendNotificationToUser(
                receiverId,
                "Nueva solicitud de amistad",
                senderName + " te ha enviado una solicitud de amistad",
                data,
                new FirebaseAuthRepository.FirebaseCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Log.d(TAG, "Friend request notification sent successfully");
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Error sending friend request notification: " + errorMessage);
                    }
                }
        );
    }

    private void sendFriendRequestAcceptedNotification(String senderId, String accepterName) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "friend_request_accepted");
        data.put("accepterName", accepterName);

        notificationHelper.sendNotificationToUser(
                senderId,
                "Solicitud de amistad aceptada",
                accepterName + " ha aceptado tu solicitud de amistad",
                data,
                new FirebaseAuthRepository.FirebaseCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Log.d(TAG, "Friend request accepted notification sent successfully");
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Error sending friend request accepted notification: " + errorMessage);
                    }
                }
        );
    }
}
