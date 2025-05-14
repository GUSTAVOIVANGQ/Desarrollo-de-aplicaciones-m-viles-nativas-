package com.example.systembooks.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.systembooks.R;
import com.example.systembooks.models.SearchHistoryItem;
import com.example.systembooks.utils.DateUtils;

import java.util.List;

/**
 * Adapter for displaying search history items in admin view
 */
public class AdminSearchHistoryAdapter extends RecyclerView.Adapter<AdminSearchHistoryAdapter.ViewHolder> {

    private final Context context;
    private final List<SearchHistoryItem> searchHistoryItems;
    private final LayoutInflater inflater;

    public AdminSearchHistoryAdapter(Context context, List<SearchHistoryItem> searchHistoryItems) {
        this.context = context;
        this.searchHistoryItems = searchHistoryItems;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_admin_search_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchHistoryItem item = searchHistoryItems.get(position);

        holder.tvSearchQuery.setText(item.getQuery());
        holder.tvSearchDate.setText(DateUtils.formatDate(item.getSearchDate()));
    }

    @Override
    public int getItemCount() {
        return searchHistoryItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSearchQuery, tvSearchDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSearchQuery = itemView.findViewById(R.id.tvSearchQuery);
            tvSearchDate = itemView.findViewById(R.id.tvSearchDate);
        }
    }
}