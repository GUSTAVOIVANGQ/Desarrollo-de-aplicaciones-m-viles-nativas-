package com.example.systembooks.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.systembooks.R;
import com.example.systembooks.firebase.FirebaseAuthRepository;
import com.example.systembooks.firebase.FirebaseUser;
import com.example.systembooks.firebase.FriendshipRepository;
import com.example.systembooks.models.FriendRequest;
import com.example.systembooks.util.LocalNotificationHelper;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsFragment extends Fragment {

    private static final String TAG = "FriendsFragment";

    // UI Components
    private TextInputEditText editTextSearchUsers;
    private Button buttonSearchUsers;
    private MaterialCardView cardSearchResults;
    private RecyclerView recyclerViewSearchResults;
    private RecyclerView recyclerViewFriendRequests;
    private RecyclerView recyclerViewSentRequests;
    private RecyclerView recyclerViewFriends;
    private TextView textViewNoFriendRequests;
    private TextView textViewNoSentRequests;
    private TextView textViewNoFriends;
    private ProgressBar progressBar;    // Data and Adapters
    private FriendshipRepository friendshipRepository;
    private LocalNotificationHelper localNotificationHelper;
    private UserSearchAdapter userSearchAdapter;
    private FriendRequestAdapter friendRequestAdapter;
    private SentRequestAdapter sentRequestAdapter;
    private FriendsAdapter friendsAdapter;

    private List<FirebaseUser> searchResults = new ArrayList<>();
    private List<FriendRequest> friendRequests = new ArrayList<>();
    private List<FriendRequest> sentRequests = new ArrayList<>();
    private List<Map<String, String>> friends = new ArrayList<>();    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        Log.d(TAG, "FriendsFragment onCreateView called");        initializeViews(view);
        setupAdapters();
        setupListeners();

        friendshipRepository = new FriendshipRepository(requireContext());
        localNotificationHelper = new LocalNotificationHelper(requireContext());

        // Load data after a short delay to ensure UI is ready
        view.post(() -> {
            Log.d(TAG, "Loading data after UI setup");
            loadFriendRequests();
            loadSentRequests();
            loadFriendsList();
        });

        return view;
    }    private void initializeViews(View view) {
        editTextSearchUsers = view.findViewById(R.id.editTextSearchUsers);
        buttonSearchUsers = view.findViewById(R.id.buttonSearchUsers);
        cardSearchResults = view.findViewById(R.id.cardSearchResults);
        recyclerViewSearchResults = view.findViewById(R.id.recyclerViewSearchResults);
        recyclerViewFriendRequests = view.findViewById(R.id.recyclerViewFriendRequests);
        recyclerViewSentRequests = view.findViewById(R.id.recyclerViewSentRequests);
        recyclerViewFriends = view.findViewById(R.id.recyclerViewFriends);
        textViewNoFriendRequests = view.findViewById(R.id.textViewNoFriendRequests);
        textViewNoSentRequests = view.findViewById(R.id.textViewNoSentRequests);
        textViewNoFriends = view.findViewById(R.id.textViewNoFriends);
        progressBar = view.findViewById(R.id.progressBar);
        
        Log.d(TAG, "Views initialized");
        verifyUIComponents();
    }private void setupAdapters() {
        // Setup search results adapter
        userSearchAdapter = new UserSearchAdapter(searchResults, this::sendFriendRequest);
        recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewSearchResults.setAdapter(userSearchAdapter);

        // Setup friend requests adapter
        friendRequestAdapter = new FriendRequestAdapter(friendRequests, new FriendRequestAdapter.OnRequestActionListener() {
            @Override
            public void onAccept(FriendRequest request) {
                acceptFriendRequest(request);
            }
            @Override
            public void onReject(FriendRequest request) {
                rejectFriendRequest(request);
            }
        });
        recyclerViewFriendRequests.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewFriendRequests.setAdapter(friendRequestAdapter);

        // Setup sent requests adapter
        sentRequestAdapter = new SentRequestAdapter(sentRequests);
        recyclerViewSentRequests.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewSentRequests.setAdapter(sentRequestAdapter);

        // Setup friends adapter
        friendsAdapter = new FriendsAdapter(friends);
        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewFriends.setAdapter(friendsAdapter);

        // Log adapter setup
        Log.d(TAG, "All adapters setup completed");
    }

    private void setupListeners() {
        buttonSearchUsers.setOnClickListener(v -> searchUsers());
    }

    private void searchUsers() {
        String searchQuery = editTextSearchUsers.getText().toString().trim();
        if (TextUtils.isEmpty(searchQuery)) {
            Toast.makeText(requireContext(), "Ingresa un término de búsqueda", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        friendshipRepository.searchUsers(searchQuery, new FirebaseAuthRepository.FirebaseCallback<List<FirebaseUser>>() {
            @Override
            public void onSuccess(List<FirebaseUser> users) {
                progressBar.setVisibility(View.GONE);
                searchResults.clear();
                searchResults.addAll(users);
                userSearchAdapter.notifyDataSetChanged();

                if (users.isEmpty()) {
                    Toast.makeText(requireContext(), "No se encontraron usuarios", Toast.LENGTH_SHORT).show();
                    cardSearchResults.setVisibility(View.GONE);
                } else {
                    cardSearchResults.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendFriendRequest(FirebaseUser user) {
        progressBar.setVisibility(View.VISIBLE);
        friendshipRepository.sendFriendRequest(user.getUid(), new FirebaseAuthRepository.FirebaseCallback<FriendRequest>() {
            @Override
            public void onSuccess(FriendRequest friendRequest) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Solicitud enviada a " + user.getUsername(), Toast.LENGTH_SHORT).show();
                loadSentRequests(); // Refresh sent requests
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void acceptFriendRequest(FriendRequest request) {
        progressBar.setVisibility(View.VISIBLE);
        friendshipRepository.acceptFriendRequest(request.getId(), new FirebaseAuthRepository.FirebaseCallback<com.example.systembooks.models.Friendship>() {
            @Override
            public void onSuccess(com.example.systembooks.models.Friendship friendship) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Solicitud aceptada", Toast.LENGTH_SHORT).show();
                loadFriendRequests(); // Refresh requests
                loadFriendsList(); // Refresh friends list
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rejectFriendRequest(FriendRequest request) {
        progressBar.setVisibility(View.VISIBLE);
        friendshipRepository.rejectFriendRequest(request.getId(), new FirebaseAuthRepository.FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Solicitud rechazada", Toast.LENGTH_SHORT).show();
                loadFriendRequests(); // Refresh requests
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }    private void loadFriendRequests() {
        Log.d(TAG, "Loading friend requests...");
        friendshipRepository.getIncomingFriendRequests(new FirebaseAuthRepository.FirebaseCallback<List<FriendRequest>>() {
            @Override
            public void onSuccess(List<FriendRequest> requests) {
                Log.d(TAG, "Successfully loaded " + requests.size() + " friend requests");
                
                // Verificar que las listas no sean null
                if (friendRequests == null) {
                    friendRequests = new ArrayList<>();
                }
                
                friendRequests.clear();
                friendRequests.addAll(requests);
                
                // Verificar que el adaptador no sea null
                if (friendRequestAdapter != null) {
                    Log.d(TAG, "Notifying adapter of data change for friend requests");
                    friendRequestAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "FriendRequestAdapter is null!");
                }

                textViewNoFriendRequests.setVisibility(requests.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerViewFriendRequests.setVisibility(requests.isEmpty() ? View.GONE : View.VISIBLE);
                
                Log.d(TAG, "Friend requests UI updated. RecyclerView visible: " + (recyclerViewFriendRequests.getVisibility() == View.VISIBLE));
                  // Send local notification if there are pending requests and this is the initial load
                if (!requests.isEmpty() && getView() != null && shouldSendNotification()) {
                    String title = "Solicitudes de amistad";
                    String message = requests.size() == 1 
                        ? "Tienes 1 solicitud de amistad pendiente"
                        : "Tienes " + requests.size() + " solicitudes de amistad pendientes";
                    
                    Log.d(TAG, "Sending local notification for friend requests: " + message);
                    
                    // Delay the notification to avoid sending it immediately on fragment load
                    getView().postDelayed(() -> {
                        if (localNotificationHelper != null) {
                            localNotificationHelper.sendLocalNotification(title, message);
                            markNotificationSent();
                        }
                    }, 3000); // 3 second delay
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error loading friend requests: " + errorMessage);
                textViewNoFriendRequests.setVisibility(View.VISIBLE);
                recyclerViewFriendRequests.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error cargando solicitudes: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }private void loadSentRequests() {
        Log.d(TAG, "Loading sent requests...");
        friendshipRepository.getOutgoingFriendRequests(new FirebaseAuthRepository.FirebaseCallback<List<FriendRequest>>() {
            @Override
            public void onSuccess(List<FriendRequest> requests) {
                Log.d(TAG, "Successfully loaded " + requests.size() + " sent requests");
                
                // Verificar que las listas no sean null
                if (sentRequests == null) {
                    sentRequests = new ArrayList<>();
                }
                
                sentRequests.clear();
                sentRequests.addAll(requests);
                
                // Verificar que el adaptador no sea null
                if (sentRequestAdapter != null) {
                    Log.d(TAG, "Notifying adapter of data change for sent requests");
                    sentRequestAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "SentRequestAdapter is null!");
                }

                textViewNoSentRequests.setVisibility(requests.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerViewSentRequests.setVisibility(requests.isEmpty() ? View.GONE : View.VISIBLE);
                
                Log.d(TAG, "Sent requests UI updated. RecyclerView visible: " + (recyclerViewSentRequests.getVisibility() == View.VISIBLE));
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error loading sent requests: " + errorMessage);
                textViewNoSentRequests.setVisibility(View.VISIBLE);
                recyclerViewSentRequests.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error cargando solicitudes enviadas: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }    private void loadFriendsList() {
        Log.d(TAG, "Loading friends list...");
        friendshipRepository.getFriendsList(new FirebaseAuthRepository.FirebaseCallback<List<Map<String, String>>>() {
            @Override
            public void onSuccess(List<Map<String, String>> friendsList) {
                Log.d(TAG, "Successfully loaded " + friendsList.size() + " friends");
                
                // Verificar que las listas no sean null
                if (friends == null) {
                    friends = new ArrayList<>();
                }
                
                friends.clear();
                friends.addAll(friendsList);
                
                // Verificar que el adaptador no sea null
                if (friendsAdapter != null) {
                    Log.d(TAG, "Notifying adapter of data change for friends list");
                    friendsAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "FriendsAdapter is null!");
                }

                textViewNoFriends.setVisibility(friendsList.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerViewFriends.setVisibility(friendsList.isEmpty() ? View.GONE : View.VISIBLE);
                
                Log.d(TAG, "Friends list UI updated. RecyclerView visible: " + (recyclerViewFriends.getVisibility() == View.VISIBLE));
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error loading friends list: " + errorMessage);
                textViewNoFriends.setVisibility(View.VISIBLE);
                recyclerViewFriends.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error cargando lista de amigos: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Public method to refresh all friend data
     */
    public void refreshAllData() {
        Log.d(TAG, "Refreshing all friend data");
        loadFriendRequests();
        loadSentRequests();
        loadFriendsList();
    }

    /**
     * Verify that all UI components are properly initialized
     */
    private void verifyUIComponents() {
        Log.d(TAG, "Verifying UI components...");
        
        if (recyclerViewFriendRequests == null) {
            Log.e(TAG, "recyclerViewFriendRequests is null!");
            return;
        }
        
        if (recyclerViewSentRequests == null) {
            Log.e(TAG, "recyclerViewSentRequests is null!");
            return;
        }
        
        if (recyclerViewFriends == null) {
            Log.e(TAG, "recyclerViewFriends is null!");
            return;
        }
        
        if (textViewNoFriendRequests == null) {
            Log.e(TAG, "textViewNoFriendRequests is null!");
            return;
        }
        
        if (textViewNoSentRequests == null) {
            Log.e(TAG, "textViewNoSentRequests is null!");
            return;
        }
        
        if (textViewNoFriends == null) {
            Log.e(TAG, "textViewNoFriends is null!");
            return;
        }
        
        Log.d(TAG, "All UI components verified successfully");
    }

    // Adapter Classes

    private static class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserViewHolder> {
        private List<FirebaseUser> users;
        private OnUserClickListener listener;

        interface OnUserClickListener {
            void onSendRequest(FirebaseUser user);
        }

        public UserSearchAdapter(List<FirebaseUser> users, OnUserClickListener listener) {
            this.users = users;
            this.listener = listener;
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_search, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            FirebaseUser user = users.get(position);
            holder.bind(user, listener);
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        static class UserViewHolder extends RecyclerView.ViewHolder {
            CircleImageView imageViewUserAvatar;
            TextView textViewUserName;
            TextView textViewUserEmail;
            Button buttonSendRequest;

            public UserViewHolder(@NonNull View itemView) {
                super(itemView);
                imageViewUserAvatar = itemView.findViewById(R.id.imageViewUserAvatar);
                textViewUserName = itemView.findViewById(R.id.textViewUserName);
                textViewUserEmail = itemView.findViewById(R.id.textViewUserEmail);
                buttonSendRequest = itemView.findViewById(R.id.buttonSendRequest);
            }

            public void bind(FirebaseUser user, OnUserClickListener listener) {
                textViewUserName.setText(user.getUsername());
                textViewUserEmail.setText(user.getEmail());

                if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(user.getPhotoUrl())
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(imageViewUserAvatar);
                } else {
                    imageViewUserAvatar.setImageResource(R.drawable.ic_profile);
                }

                buttonSendRequest.setOnClickListener(v -> listener.onSendRequest(user));
            }
        }
    }

    private static class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.RequestViewHolder> {
        private List<FriendRequest> requests;
        private OnRequestActionListener listener;

        interface OnRequestActionListener {
            void onAccept(FriendRequest request);
            void onReject(FriendRequest request);
        }

        public FriendRequestAdapter(List<FriendRequest> requests, OnRequestActionListener listener) {
            this.requests = requests;
            this.listener = listener;
        }

        @NonNull
        @Override
        public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_request, parent, false);
            return new RequestViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
            FriendRequest request = requests.get(position);
            holder.bind(request, listener);
        }

        @Override
        public int getItemCount() {
            return requests.size();
        }

        static class RequestViewHolder extends RecyclerView.ViewHolder {
            CircleImageView imageViewSenderAvatar;
            TextView textViewSenderName;
            TextView textViewSenderEmail;
            TextView textViewRequestTime;
            Button buttonAcceptRequest;
            Button buttonRejectRequest;

            public RequestViewHolder(@NonNull View itemView) {
                super(itemView);
                imageViewSenderAvatar = itemView.findViewById(R.id.imageViewSenderAvatar);
                textViewSenderName = itemView.findViewById(R.id.textViewSenderName);
                textViewSenderEmail = itemView.findViewById(R.id.textViewSenderEmail);
                textViewRequestTime = itemView.findViewById(R.id.textViewRequestTime);
                buttonAcceptRequest = itemView.findViewById(R.id.buttonAcceptRequest);
                buttonRejectRequest = itemView.findViewById(R.id.buttonRejectRequest);
            }

            public void bind(FriendRequest request, OnRequestActionListener listener) {
                textViewSenderName.setText(request.getSenderName());
                textViewSenderEmail.setText(request.getSenderEmail());
                textViewRequestTime.setText(getTimeAgo(request.getCreatedAt()));

                if (request.getSenderPhotoUrl() != null && !request.getSenderPhotoUrl().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(request.getSenderPhotoUrl())
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(imageViewSenderAvatar);
                } else {
                    imageViewSenderAvatar.setImageResource(R.drawable.ic_profile);
                }

                buttonAcceptRequest.setOnClickListener(v -> listener.onAccept(request));
                buttonRejectRequest.setOnClickListener(v -> listener.onReject(request));
            }

            private String getTimeAgo(Long timestamp) {
                if (timestamp == null) return "Hace un momento";
                
                long now = System.currentTimeMillis();
                long diff = now - timestamp;
                
                long seconds = diff / 1000;
                long minutes = seconds / 60;
                long hours = minutes / 60;
                long days = hours / 24;
                
                if (days > 0) {
                    return "Hace " + days + " día" + (days > 1 ? "s" : "");
                } else if (hours > 0) {
                    return "Hace " + hours + " hora" + (hours > 1 ? "s" : "");
                } else if (minutes > 0) {
                    return "Hace " + minutes + " minuto" + (minutes > 1 ? "s" : "");
                } else {
                    return "Hace un momento";
                }
            }
        }
    }

    private static class SentRequestAdapter extends RecyclerView.Adapter<SentRequestAdapter.SentRequestViewHolder> {
        private List<FriendRequest> requests;

        public SentRequestAdapter(List<FriendRequest> requests) {
            this.requests = requests;
        }

        @NonNull
        @Override
        public SentRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sent_request, parent, false);
            return new SentRequestViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SentRequestViewHolder holder, int position) {
            FriendRequest request = requests.get(position);
            holder.bind(request);
        }

        @Override
        public int getItemCount() {
            return requests.size();
        }

        static class SentRequestViewHolder extends RecyclerView.ViewHolder {
            CircleImageView imageViewReceiverAvatar;
            TextView textViewReceiverName;
            TextView textViewReceiverEmail;
            TextView textViewSentTime;

            public SentRequestViewHolder(@NonNull View itemView) {
                super(itemView);
                imageViewReceiverAvatar = itemView.findViewById(R.id.imageViewReceiverAvatar);
                textViewReceiverName = itemView.findViewById(R.id.textViewReceiverName);
                textViewReceiverEmail = itemView.findViewById(R.id.textViewReceiverEmail);
                textViewSentTime = itemView.findViewById(R.id.textViewSentTime);
            }

            public void bind(FriendRequest request) {
                textViewReceiverName.setText(request.getReceiverName());
                textViewReceiverEmail.setText(request.getReceiverEmail());
                textViewSentTime.setText("Enviada " + getTimeAgo(request.getCreatedAt()));

                if (request.getReceiverPhotoUrl() != null && !request.getReceiverPhotoUrl().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(request.getReceiverPhotoUrl())
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(imageViewReceiverAvatar);
                } else {
                    imageViewReceiverAvatar.setImageResource(R.drawable.ic_profile);
                }
            }

            private String getTimeAgo(Long timestamp) {
                if (timestamp == null) return "hace un momento";
                
                long now = System.currentTimeMillis();
                long diff = now - timestamp;
                
                long seconds = diff / 1000;
                long minutes = seconds / 60;
                long hours = minutes / 60;
                long days = hours / 24;
                
                if (days > 0) {
                    return "hace " + days + " día" + (days > 1 ? "s" : "");
                } else if (hours > 0) {
                    return "hace " + hours + " hora" + (hours > 1 ? "s" : "");
                } else if (minutes > 0) {
                    return "hace " + minutes + " minuto" + (minutes > 1 ? "s" : "");
                } else {
                    return "hace un momento";
                }
            }
        }
    }

    private static class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {
        private List<Map<String, String>> friends;

        public FriendsAdapter(List<Map<String, String>> friends) {
            this.friends = friends;
        }

        @NonNull
        @Override
        public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
            return new FriendViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
            Map<String, String> friend = friends.get(position);
            holder.bind(friend);
        }

        @Override
        public int getItemCount() {
            return friends.size();
        }

        static class FriendViewHolder extends RecyclerView.ViewHolder {
            CircleImageView imageViewFriendAvatar;
            TextView textViewFriendName;
            TextView textViewFriendEmail;
            TextView textViewFriendSince;

            public FriendViewHolder(@NonNull View itemView) {
                super(itemView);
                imageViewFriendAvatar = itemView.findViewById(R.id.imageViewFriendAvatar);
                textViewFriendName = itemView.findViewById(R.id.textViewFriendName);
                textViewFriendEmail = itemView.findViewById(R.id.textViewFriendEmail);
                textViewFriendSince = itemView.findViewById(R.id.textViewFriendSince);
            }

            public void bind(Map<String, String> friend) {
                textViewFriendName.setText(friend.get("name"));
                textViewFriendEmail.setText(friend.get("email"));
                textViewFriendSince.setText("Amigos desde hace un tiempo");

                String photoUrl = friend.get("photoUrl");
                if (photoUrl != null && !photoUrl.isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(photoUrl)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(imageViewFriendAvatar);
                } else {
                    imageViewFriendAvatar.setImageResource(R.drawable.ic_profile);
                }
            }
        }
    }    /**
     * Send local notification for pending friend requests
     * @param incomingRequestsCount Number of incoming friend requests
     * @param outgoingRequestsCount Number of outgoing friend requests
     */
    private void sendLocalNotificationsForPendingRequests(int incomingRequestsCount, int outgoingRequestsCount) {
        Log.d(TAG, "Checking for pending requests to send notifications - Incoming: " + incomingRequestsCount + ", Outgoing: " + outgoingRequestsCount);
        
        // Check if we should send notifications
        if (!shouldSendNotification()) {
            return;
        }
        
        // Send notification for incoming friend requests (priority)
        if (incomingRequestsCount > 0) {
            String title = "Solicitudes de amistad";
            String message = incomingRequestsCount == 1 
                ? "Tienes 1 solicitud de amistad pendiente"
                : "Tienes " + incomingRequestsCount + " solicitudes de amistad pendientes";
            
            Log.d(TAG, "Sending local notification for incoming requests: " + message);
            localNotificationHelper.sendLocalNotification(title, message);
            markNotificationSent();
            return; // Only send one notification at a time
        }
        
        // Send notification for outgoing friend requests (secondary priority)
        if (outgoingRequestsCount > 0) {
            String title = "Solicitudes enviadas";
            String message = outgoingRequestsCount == 1 
                ? "Tienes 1 solicitud enviada esperando respuesta"
                : "Tienes " + outgoingRequestsCount + " solicitudes enviadas esperando respuesta";
            
            Log.d(TAG, "Sending local notification for outgoing requests: " + message);
            localNotificationHelper.sendLocalNotification(title, message);
            markNotificationSent();
        }
    }

    /**
     * Check and send notifications when fragment becomes visible
     */
    private void checkAndSendNotificationsOnResume() {
        Log.d(TAG, "Checking for notifications on fragment resume");
        
        // Load requests specifically for notification checking
        loadRequestsForNotificationCheck();
    }

    /**
     * Load requests specifically for notification purposes
     */
    private void loadRequestsForNotificationCheck() {
        Log.d(TAG, "Loading requests for notification check...");
        
        // Create counters for both types of requests
        final int[] incomingCount = {0};
        final int[] outgoingCount = {0};
        final boolean[] incomingLoaded = {false};
        final boolean[] outgoingLoaded = {false};
        
        // Load incoming requests
        friendshipRepository.getIncomingFriendRequests(new FirebaseAuthRepository.FirebaseCallback<List<FriendRequest>>() {
            @Override
            public void onSuccess(List<FriendRequest> requests) {
                incomingCount[0] = requests.size();
                incomingLoaded[0] = true;
                
                Log.d(TAG, "Loaded " + requests.size() + " incoming requests for notification check");
                
                // Check if both are loaded and send notifications
                if (outgoingLoaded[0]) {
                    sendLocalNotificationsForPendingRequests(incomingCount[0], outgoingCount[0]);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error loading incoming requests for notification: " + errorMessage);
                incomingLoaded[0] = true;
                
                // Still check if outgoing is loaded
                if (outgoingLoaded[0]) {
                    sendLocalNotificationsForPendingRequests(0, outgoingCount[0]);
                }
            }
        });
        
        // Load outgoing requests
        friendshipRepository.getOutgoingFriendRequests(new FirebaseAuthRepository.FirebaseCallback<List<FriendRequest>>() {
            @Override
            public void onSuccess(List<FriendRequest> requests) {
                outgoingCount[0] = requests.size();
                outgoingLoaded[0] = true;
                
                Log.d(TAG, "Loaded " + requests.size() + " outgoing requests for notification check");
                
                // Check if both are loaded and send notifications
                if (incomingLoaded[0]) {
                    sendLocalNotificationsForPendingRequests(incomingCount[0], outgoingCount[0]);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error loading outgoing requests for notification: " + errorMessage);
                outgoingLoaded[0] = true;
                
                // Still check if incoming is loaded
                if (incomingLoaded[0]) {
                    sendLocalNotificationsForPendingRequests(incomingCount[0], 0);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "FriendsFragment onResume - checking for pending requests");
        
        // Check for pending requests and send notifications when fragment becomes visible
        if (friendshipRepository != null && localNotificationHelper != null) {
            // Add a delay to ensure the fragment is fully loaded
            if (getView() != null) {
                getView().postDelayed(() -> {
                    checkAndSendNotificationsOnResume();
                }, 2000); // 2 second delay to ensure everything is loaded
            }
        }
    }
    
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        
        if (isVisibleToUser && isResumed()) {
            Log.d(TAG, "FriendsFragment became visible to user - checking for notifications");
            
            // Check for pending requests when fragment becomes visible
            if (friendshipRepository != null && localNotificationHelper != null) {
                // Add a delay to ensure the fragment is fully loaded
                if (getView() != null) {
                    getView().postDelayed(() -> {
                        checkAndSendNotificationsOnResume();
                    }, 1500); // 1.5 second delay
                }
            }
        }
    }

    // Variables para controlar las notificaciones
    private boolean hasShownNotificationThisSession = false;
    private static final String PREFS_NAME = "FriendsFragmentPrefs";
    private static final String KEY_LAST_NOTIFICATION_TIME = "last_notification_time";
    private static final long NOTIFICATION_COOLDOWN = 300000; // 5 minutos en milisegundos

    /**
     * Check if we should send a notification based on cooldown period
     * @return true if notification should be sent, false otherwise
     */
    private boolean shouldSendNotification() {
        // Don't send if already shown in this session
        if (hasShownNotificationThisSession) {
            Log.d(TAG, "Notification already shown this session, skipping");
            return false;
        }
        
        // Check cooldown period using SharedPreferences
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        long lastNotificationTime = prefs.getLong(KEY_LAST_NOTIFICATION_TIME, 0);
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastNotificationTime < NOTIFICATION_COOLDOWN) {
            Log.d(TAG, "Notification cooldown active, skipping. Last: " + lastNotificationTime + ", Current: " + currentTime);
            return false;
        }
        
        return true;
    }

    /**
     * Mark that a notification has been sent
     */
    private void markNotificationSent() {
        hasShownNotificationThisSession = true;
        
        // Update SharedPreferences
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        prefs.edit().putLong(KEY_LAST_NOTIFICATION_TIME, System.currentTimeMillis()).apply();
        
        Log.d(TAG, "Marked notification as sent for this session");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "FriendsFragment onPause");
        // Reset notification flag when leaving the fragment
        // This allows notifications to be shown again next time the user enters
        // hasShownNotificationThisSession = false; // Uncomment if you want notifications every time
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "FriendsFragment onDestroyView");
        // Clean up resources
        if (localNotificationHelper != null) {
            // Cancel any pending notifications
            localNotificationHelper.cancelAllNotifications();
        }
    }
}
