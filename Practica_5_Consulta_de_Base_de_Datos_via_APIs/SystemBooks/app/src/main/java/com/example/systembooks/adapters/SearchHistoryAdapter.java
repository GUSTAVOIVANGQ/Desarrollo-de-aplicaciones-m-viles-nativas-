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

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.SearchHistoryViewHolder> {

    private final Context context;
    private List<SearchHistoryItem> historyItems;
    private final OnHistoryItemClickListener listener;
    private final SimpleDateFormat dateFormat;

    public interface OnHistoryItemClickListener {
        void onHistoryItemClick(String query);
    }

    public SearchHistoryAdapter(Context context, List<SearchHistoryItem> historyItems, OnHistoryItemClickListener listener) {
        this.context = context;
        this.historyItems = historyItems;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public SearchHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_history, parent, false);
        return new SearchHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchHistoryViewHolder holder, int position) {
        SearchHistoryItem item = historyItems.get(position);
        holder.tvSearchQuery.setText(item.getQuery());
        
        // Format and set the search date
        if (item.getSearchDate() != null) {
            holder.tvSearchDate.setText(dateFormat.format(item.getSearchDate()));
        } else {
            holder.tvSearchDate.setVisibility(View.GONE);
        }
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHistoryItemClick(item.getQuery());
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyItems != null ? historyItems.size() : 0;
    }

    public void updateHistoryItems(List<SearchHistoryItem> newItems) {
        this.historyItems = newItems;
        notifyDataSetChanged();
    }

    static class SearchHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvSearchQuery;
        TextView tvSearchDate;

        SearchHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSearchQuery = itemView.findViewById(R.id.tvSearchQuery);
            tvSearchDate = itemView.findViewById(R.id.tvSearchDate);
        }
    }
}