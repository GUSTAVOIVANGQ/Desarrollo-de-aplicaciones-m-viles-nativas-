package com.example.systembooks.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.systembooks.R;
import com.example.systembooks.models.Book;
import com.example.systembooks.repositories.BookRepository;
import com.example.systembooks.repositories.FavoritesRepository;
import com.example.systembooks.util.SessionManager;
import com.example.systembooks.utils.NetworkUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class BookDetailActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK_ID = "extra_book_id";
    
    private ImageView bookCover;
    private TextView bookTitle;
    private TextView bookAuthor;
    private TextView bookPublisher;
    private TextView bookYear;
    private TextView bookDescription;
    private TextView bookPageCount;
    private View loadingView;
    private View contentView;
    private View errorView;
    private TextView errorMessage;
    private FloatingActionButton fabFavorite;
    
    private BookRepository bookRepository;
    private FavoritesRepository favoritesRepository;
    private SessionManager sessionManager;
    
    private Book currentBook;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        
        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        initViews();
        
        // Initialize repositories and session manager
        bookRepository = new BookRepository(this);
        favoritesRepository = new FavoritesRepository(this);
        sessionManager = new SessionManager(this);
        
        String bookId = getIntent().getStringExtra(EXTRA_BOOK_ID);
        if (bookId != null) {
            loadBookDetails(bookId);
            
            // Check if book is in favorites (if user is logged in)
            if (sessionManager.isLoggedIn()) {
                checkFavoriteStatus(bookId);
            } else {
                // Hide favorite button if not logged in
                fabFavorite.setVisibility(View.GONE);
            }
        } else {
            showError("No se encontró el ID del libro");
        }
    }
    
    private void initViews() {
        bookCover = findViewById(R.id.book_cover);
        bookTitle = findViewById(R.id.book_title);
        bookAuthor = findViewById(R.id.book_author);
        bookPublisher = findViewById(R.id.book_publisher);
        bookYear = findViewById(R.id.book_year);
        bookDescription = findViewById(R.id.book_description);
        bookPageCount = findViewById(R.id.book_page_count);
        fabFavorite = findViewById(R.id.fab_favorite);
        
        // Vistas de carga y error
        loadingView = findViewById(R.id.loading_view);
        contentView = findViewById(R.id.content_view);
        errorView = findViewById(R.id.error_container);
        errorMessage = findViewById(R.id.error_message);
        
        // Configurar botón de reintentar
        findViewById(R.id.retry_button).setOnClickListener(v -> {
            String bookId = getIntent().getStringExtra(EXTRA_BOOK_ID);
            if (bookId != null) {
                loadBookDetails(bookId);
            }
        });
        
        // Setup favorite button
        fabFavorite.setOnClickListener(v -> toggleFavorite());
    }
    
    private void loadBookDetails(String bookId) {
        // Verificar conectividad a internet
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showError(getString(R.string.error_network));
            return;
        }
        
        showLoading();
        
        bookRepository.getBookDetails(bookId, new BookRepository.BookCallback<Book>() {
            @Override
            public void onSuccess(Book book) {
                runOnUiThread(() -> {
                    currentBook = book;
                    displayBookDetails(book);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> showError(message));
            }
        });
    }
    
    private void displayBookDetails(Book book) {
        // Mostrar datos del libro en la UI
        bookTitle.setText(book.getTitle());
        bookAuthor.setText(book.getAuthor());
        
        if (book.getPublisher() != null && !book.getPublisher().isEmpty()) {
            bookPublisher.setText(book.getPublisher());
        } else {
            bookPublisher.setText("Desconocido");
        }
        
        if (book.getPublishYear() != null && !book.getPublishYear().isEmpty()) {
            bookYear.setText(book.getPublishYear());
        } else {
            bookYear.setText("Desconocido");
        }
        
        if (book.getDescription() != null && !book.getDescription().isEmpty()) {
            bookDescription.setText(book.getDescription());
        } else {
            bookDescription.setText("No hay descripción disponible");
        }
        
        if (book.getPageCount() > 0) {
            bookPageCount.setText(String.valueOf(book.getPageCount()));
        } else {
            bookPageCount.setText("Desconocido");
        }
        
        // Cargar imagen de portada
        if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            Glide.with(this)
                .load(book.getCoverUrl())
                .apply(new RequestOptions()
                    .placeholder(R.drawable.book_placeholder)
                    .error(R.drawable.book_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(bookCover);
        } else {
            bookCover.setImageResource(R.drawable.book_placeholder);
        }
        
        showContent();
    }
    
    private void checkFavoriteStatus(String bookId) {
        Long userId = sessionManager.getUserId();
        if (userId == -1) return;
        
        new Thread(() -> {
            boolean isBookFavorite = favoritesRepository.isFavorite(userId, bookId);
            runOnUiThread(() -> {
                isFavorite = isBookFavorite;
                updateFavoriteIcon();
            });
        }).start();
    }
    
    private void toggleFavorite() {
        if (currentBook == null || !sessionManager.isLoggedIn()) return;
        
        Long userId = sessionManager.getUserId();
        if (userId == -1) {
            Toast.makeText(this, "Inicia sesión para agregar favoritos", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new Thread(() -> {
            final boolean success;
            if (isFavorite) {
                success = favoritesRepository.removeFromFavorites(userId, currentBook.getId());
            } else {
                success = favoritesRepository.addToFavorites(userId, currentBook);
            }
            
            runOnUiThread(() -> {
                if (success) {
                    isFavorite = !isFavorite;
                    updateFavoriteIcon();
                    
                    // Show appropriate message
                    String message = isFavorite 
                            ? getString(R.string.added_to_favorites) 
                            : getString(R.string.removed_from_favorites);
                    Toast.makeText(BookDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BookDetailActivity.this, 
                            getString(R.string.error_updating_favorites), 
                            Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
    
    private void updateFavoriteIcon() {
        int iconRes = isFavorite 
                ? R.drawable.ic_favorite 
                : R.drawable.ic_favorite_border;
        fabFavorite.setImageResource(iconRes);
    }
    
    private void showLoading() {
        loadingView.setVisibility(View.VISIBLE);
        contentView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        fabFavorite.setVisibility(View.GONE);
    }
    
    private void showContent() {
        loadingView.setVisibility(View.GONE);
        contentView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        if (sessionManager.isLoggedIn()) {
            fabFavorite.setVisibility(View.VISIBLE);
        }
    }
    
    private void showError(String message) {
        loadingView.setVisibility(View.GONE);
        contentView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        fabFavorite.setVisibility(View.GONE);
        errorMessage.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
