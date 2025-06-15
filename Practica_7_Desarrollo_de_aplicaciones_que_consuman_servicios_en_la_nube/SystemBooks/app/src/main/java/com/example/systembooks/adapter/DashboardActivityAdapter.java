package com.example.systembooks.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.systembooks.R;
import com.example.systembooks.models.DashboardActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying dashboard activities in RecyclerView
 */
public class DashboardActivityAdapter extends RecyclerView.Adapter<DashboardActivityAdapter.ActivityViewHolder> {
    
    private final Context context;
    private List<DashboardActivity> activities;
    private final SimpleDateFormat timeFormat;
    
    public DashboardActivityAdapter(Context context) {
        this.context = context;
        this.activities = new ArrayList<>();
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dashboard_activity, parent, false);
        return new ActivityViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        DashboardActivity activity = activities.get(position);
        holder.bind(activity);
    }
    
    @Override
    public int getItemCount() {
        return activities.size();
    }
    
    /**
     * Update the activities list
     */
    public void updateActivities(List<DashboardActivity> newActivities) {
        this.activities.clear();
        this.activities.addAll(newActivities);
        notifyDataSetChanged();
    }
    
    /**
     * Add a new activity to the beginning of the list
     */
    public void addActivity(DashboardActivity activity) {
        activities.add(0, activity);
        notifyItemInserted(0);
    }
    
    /**
     * Clear all activities
     */
    public void clearActivities() {
        int size = activities.size();
        activities.clear();
        notifyItemRangeRemoved(0, size);
    }
    
    class ActivityViewHolder extends RecyclerView.ViewHolder {
        
        private final ImageView imageViewIcon;
        private final TextView textViewDescription;
        private final TextView textViewUsername;
        private final TextView textViewTime;
        private final TextView textViewDetails;
        private final View viewColorIndicator;
        
        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            
            imageViewIcon = itemView.findViewById(R.id.imageViewActivityIcon);
            textViewDescription = itemView.findViewById(R.id.textViewActivityDescription);
            textViewUsername = itemView.findViewById(R.id.textViewActivityUsername);
            textViewTime = itemView.findViewById(R.id.textViewActivityTime);
            textViewDetails = itemView.findViewById(R.id.textViewActivityDetails);
            viewColorIndicator = itemView.findViewById(R.id.viewActivityColorIndicator);
        }
        
        public void bind(DashboardActivity activity) {
            // Set icon
            imageViewIcon.setImageResource(activity.getIconResource());
            
            // Set color indicator
            int color = ContextCompat.getColor(context, activity.getColorResource());
            viewColorIndicator.setBackgroundColor(color);
            
            // Set description
            textViewDescription.setText(activity.getDescription());
            
            // Set username with role badge if available
            String usernameText = activity.getUsername();
            if (activity.getUserRole() != null && !activity.getUserRole().isEmpty()) {
                String roleBadge = activity.getUserRole().contains("ADMIN") ? " [Admin]" : " [Usuario]";
                usernameText += roleBadge;
            }
            textViewUsername.setText(usernameText);
            
            // Set time with relative format
            String timeText = getRelativeTimeString(activity.getTimestamp());
            textViewTime.setText(timeText);
            
            // Set details (show/hide based on availability)
            if (activity.getDetails() != null && !activity.getDetails().trim().isEmpty()) {
                textViewDetails.setVisibility(View.VISIBLE);
                textViewDetails.setText(activity.getDetails());
            } else {
                textViewDetails.setVisibility(View.GONE);
            }
            
            // Set click listener for expanding details
            itemView.setOnClickListener(v -> {
                if (textViewDetails.getVisibility() == View.VISIBLE) {
                    boolean isExpanded = textViewDetails.getMaxLines() != 2;
                    if (isExpanded) {
                        textViewDetails.setMaxLines(2);
                    } else {
                        textViewDetails.setMaxLines(Integer.MAX_VALUE);
                    }
                }
            });
        }
        
        /**
         * Get relative time string (e.g., "2 minutes ago", "1 hour ago")
         */
        private String getRelativeTimeString(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;
            
            if (diff < DateUtils.MINUTE_IN_MILLIS) {
                return "Ahora";
            } else if (diff < DateUtils.HOUR_IN_MILLIS) {
                long minutes = diff / DateUtils.MINUTE_IN_MILLIS;
                return minutes + "m";
            } else if (diff < DateUtils.DAY_IN_MILLIS) {
                long hours = diff / DateUtils.HOUR_IN_MILLIS;
                return hours + "h";
            } else if (diff < 7 * DateUtils.DAY_IN_MILLIS) {
                long days = diff / DateUtils.DAY_IN_MILLIS;
                return days + "d";
            } else {
                // For older activities, show actual time
                return timeFormat.format(new Date(timestamp));
            }
        }
    }
}
