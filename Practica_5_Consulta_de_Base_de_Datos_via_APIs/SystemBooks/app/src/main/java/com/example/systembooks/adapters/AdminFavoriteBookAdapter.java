package com.example.systembooks.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.systembooks.R;
import com.example.systembooks.models.FavoriteBook;
import com.example.systembooks.utils.DateUtils;

import java.util.List;

/**
 * Adapter for displaying favorite books in admin view
 */
public class AdminFavoriteBookAdapter extends RecyclerView.Adapter<AdminFavoriteBookAdapter.ViewHolder> {

    private final Context context;
    private final List<FavoriteBook> favoriteBooks;
    private final LayoutInflater inflater;

    public AdminFavoriteBookAdapter(Context context, List<FavoriteBook> favoriteBooks) {
        this.context = context;
        this.favoriteBooks = favoriteBooks;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_admin_favorite_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavoriteBook favoriteBook = favoriteBooks.get(position);

        holder.tvBookTitle.setText(favoriteBook.getTitle());
        holder.tvBookAuthor.setText(favoriteBook.getAuthor());
        holder.tvDateAdded.setText(DateUtils.formatDate(favoriteBook.getDateAdded()));

        // Load book cover image with Glide
        if (favoriteBook.getCoverUrl() != null && !favoriteBook.getCoverUrl().isEmpty()) {
            Glide.with(context)
                    .load(favoriteBook.getCoverUrl())
                    .placeholder(R.drawable.book_cover_placeholder)
                    .into(holder.ivBookCover);
        } else {
            holder.ivBookCover.setImageResource(R.drawable.book_cover_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return favoriteBooks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBookCover;
        TextView tvBookTitle, tvBookAuthor, tvDateAdded;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBookCover = itemView.findViewById(R.id.ivBookCover);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvBookAuthor = itemView.findViewById(R.id.tvBookAuthor);
            tvDateAdded = itemView.findViewById(R.id.tvDateAdded);
        }
    }
}