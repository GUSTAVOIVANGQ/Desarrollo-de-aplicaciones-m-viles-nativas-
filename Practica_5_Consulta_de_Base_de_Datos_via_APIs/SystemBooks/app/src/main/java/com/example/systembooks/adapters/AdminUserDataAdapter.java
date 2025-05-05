package com.example.systembooks.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.systembooks.R;
import com.example.systembooks.models.FavoriteBook;
import com.example.systembooks.models.SearchHistoryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying user data in admin view
 * @param <T> Type of items (SearchHistoryItem or FavoriteBook)
 */
public class AdminUserDataAdapter<T> extends RecyclerView.Adapter<AdminUserDataAdapter.ViewHolder> {

    private final Context context;
    private final List<UserData<T>> userDataList;
    private final int itemLayoutResId;
    private final int titleStringResId;
    private final LayoutInflater inflater;

    public AdminUserDataAdapter(Context context, int itemLayoutResId, int titleStringResId) {
        this.context = context;
        this.userDataList = new ArrayList<>();
        this.itemLayoutResId = itemLayoutResId;
        this.titleStringResId = titleStringResId;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_user_data, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserData<T> userData = userDataList.get(position);
        
        holder.tvUserName.setText(userData.getUsername());
        holder.tvUserEmail.setText(userData.getEmail());
        holder.tvCount.setText(String.valueOf(userData.getItems().size()));
        
        // Set up the inner RecyclerView
        setupInnerRecyclerView(holder, userData);
        
        // Handle expand/collapse of item details
        setupExpandCollapse(holder);
    }

    private void setupInnerRecyclerView(ViewHolder holder, UserData<T> userData) {
        holder.recyclerViewItems.setLayoutManager(new LinearLayoutManager(context));
        
        RecyclerView.Adapter<?> innerAdapter;
        // Check the type of items and create appropriate adapter
        if (!userData.getItems().isEmpty()) {
            if (userData.getItems().get(0) instanceof SearchHistoryItem) {
                // Create adapter for search history items
                innerAdapter = new AdminSearchHistoryAdapter(context, 
                        (List<SearchHistoryItem>) userData.getItems());
            } else if (userData.getItems().get(0) instanceof FavoriteBook) {
                // Create adapter for favorite books
                innerAdapter = new AdminFavoriteBookAdapter(context, 
                        (List<FavoriteBook>) userData.getItems());
            } else {
                throw new IllegalArgumentException("Unsupported item type");
            }
            holder.recyclerViewItems.setAdapter(innerAdapter);
        }
        
        // Set user name in title
        holder.tvExpand.setTag(context.getString(titleStringResId, userData.getUsername()));
    }
    
    private void setupExpandCollapse(ViewHolder holder) {
        holder.tvExpand.setOnClickListener(v -> {
            if (holder.recyclerViewItems.getVisibility() == View.VISIBLE) {
                // Collapse
                holder.recyclerViewItems.setVisibility(View.GONE);
                holder.tvExpand.setText(R.string.show_details);
            } else {
                // Expand
                holder.recyclerViewItems.setVisibility(View.VISIBLE);
                holder.tvExpand.setText(R.string.hide_details);
                // Show title on expand
                holder.tvExpand.setText(holder.tvExpand.getTag().toString());
            }
        });
    }

    @Override
    public int getItemCount() {
        return userDataList.size();
    }

    public void submitList(List<UserData<T>> newList) {
        userDataList.clear();
        userDataList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserEmail, tvCount, tvExpand;
        RecyclerView recyclerViewItems;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvCount = itemView.findViewById(R.id.tvCount);
            tvExpand = itemView.findViewById(R.id.tvExpand);
            recyclerViewItems = itemView.findViewById(R.id.recyclerViewItems);
        }
    }

    /**
     * Data class to hold user information and their items (history or favorites)
     * @param <T> Type of items
     */
    public static class UserData<T> {
        private final long userId;
        private final String username;
        private final String email;
        private final List<T> items;

        public UserData(long userId, String username, String email, List<T> items) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.items = items;
        }

        public long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public List<T> getItems() {
            return items;
        }
    }
}