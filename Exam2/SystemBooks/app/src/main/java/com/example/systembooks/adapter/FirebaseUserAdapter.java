package com.example.systembooks.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.systembooks.R;
import com.example.systembooks.firebase.FirebaseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Adapter for displaying Firebase users in RecyclerView
 */
public class FirebaseUserAdapter extends RecyclerView.Adapter<FirebaseUserAdapter.FirebaseUserViewHolder> {

    private List<FirebaseUser> userList;
    private final Context context;
    private final OnFirebaseUserClickListener listener;

    public interface OnFirebaseUserClickListener {
        void onEditClick(FirebaseUser user);
        void onDeleteClick(FirebaseUser user);
        void onRoleClick(FirebaseUser user);
    }

    public FirebaseUserAdapter(Context context, List<FirebaseUser> userList, OnFirebaseUserClickListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
    }

    public void updateUsers(List<FirebaseUser> users) {
        this.userList = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FirebaseUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_firebase_user, parent, false);
        return new FirebaseUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FirebaseUserViewHolder holder, int position) {
        FirebaseUser user = userList.get(position);
        
        // Set user information
        holder.textViewName.setText(user.getUsername() != null ? user.getUsername() : "N/A");
        holder.textViewEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");
        
        // Set role with color coding
        String role = user.getRole();
        if (FirebaseUser.ROLE_ADMIN.equals(role)) {
            holder.textViewRole.setText("ADMIN");
            holder.textViewRole.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        } else {
            holder.textViewRole.setText("USER");
            holder.textViewRole.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
        }
        
        // Set creation date
        if (user.getCreatedAt() != null) {
            String dateStr = android.text.format.DateFormat.getDateFormat(context)
                    .format(new java.util.Date(user.getCreatedAt()));
            holder.textViewCreatedAt.setText("Created: " + dateStr);
        } else {
            holder.textViewCreatedAt.setText("Creation date unknown");
        }
        
        // Load profile image
        if (!TextUtils.isEmpty(user.getPhotoUrl())) {
            Glide.with(context)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(holder.imageViewPhoto);
        } else {
            holder.imageViewPhoto.setImageResource(R.drawable.default_profile);
        }
        
        // Configure click listeners
        holder.buttonEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(user);
            }
        });
        
        holder.buttonDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(user);
            }
        });
        
        holder.buttonChangeRole.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRoleClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    static class FirebaseUserViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imageViewPhoto;
        TextView textViewName;
        TextView textViewEmail;
        TextView textViewRole;
        TextView textViewCreatedAt;
        ImageButton buttonEdit;
        ImageButton buttonDelete;
        ImageButton buttonChangeRole;

        FirebaseUserViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewPhoto = itemView.findViewById(R.id.imageViewFirebaseUserPhoto);
            textViewName = itemView.findViewById(R.id.textViewFirebaseUserName);
            textViewEmail = itemView.findViewById(R.id.textViewFirebaseUserEmail);
            textViewRole = itemView.findViewById(R.id.textViewFirebaseUserRole);
            textViewCreatedAt = itemView.findViewById(R.id.textViewFirebaseUserCreatedAt);
            buttonEdit = itemView.findViewById(R.id.buttonEditFirebaseUser);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteFirebaseUser);
            buttonChangeRole = itemView.findViewById(R.id.buttonChangeFirebaseUserRole);
        }
    }
}
