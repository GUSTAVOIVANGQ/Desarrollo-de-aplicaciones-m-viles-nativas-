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

import com.example.systembooks.R;
import com.example.systembooks.model.User;
import com.example.systembooks.util.ImageUtils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private final Context context;
    private final OnUserClickListener listener;

    public interface OnUserClickListener {
        void onEditClick(User user);
        void onDeleteClick(User user);
    }

    public UserAdapter(Context context, List<User> userList, OnUserClickListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
    }

    public void updateUsers(List<User> users) {
        this.userList = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        
        holder.textViewName.setText(user.getNombre());
        holder.textViewEmail.setText(user.getEmail());
        
        // Cargar imagen de perfil
        if (!TextUtils.isEmpty(user.getImagen())) {
            // Usar ImageUtils para cargar la imagen desde URL o ruta
            ImageUtils.loadProfileImage(context, user.getImagen(), holder.imageViewPhoto);
        } else {
            holder.imageViewPhoto.setImageResource(R.drawable.default_profile);
        }
        
        // Configurar listeners
        holder.buttonEdit.setOnClickListener(v -> listener.onEditClick(user));
        holder.buttonDelete.setOnClickListener(v -> listener.onDeleteClick(user));
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imageViewPhoto;
        TextView textViewName;
        TextView textViewEmail;
        ImageButton buttonEdit;
        ImageButton buttonDelete;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewPhoto = itemView.findViewById(R.id.imageViewUserPhoto);
            textViewName = itemView.findViewById(R.id.textViewUserName);
            textViewEmail = itemView.findViewById(R.id.textViewUserEmail);
            buttonEdit = itemView.findViewById(R.id.buttonEditUser);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteUser);
        }
    }
}
