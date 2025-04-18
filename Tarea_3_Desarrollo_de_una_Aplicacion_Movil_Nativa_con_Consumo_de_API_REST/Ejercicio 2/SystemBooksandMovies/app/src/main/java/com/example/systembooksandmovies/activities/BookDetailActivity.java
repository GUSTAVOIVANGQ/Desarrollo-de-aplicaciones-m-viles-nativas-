package com.example.systembooksandmovies.activities;

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
import com.example.systembooksandmovies.R;
import com.example.systembooksandmovies.models.Book;
import com.example.systembooksandmovies.repositories.BookRepository;
import com.example.systembooksandmovies.utils.NetworkUtils;

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
    private BookRepository bookRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        
        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        initViews();
        
        // Inicializar repositorio
        bookRepository = new BookRepository(this);
        
        String bookId = getIntent().getStringExtra(EXTRA_BOOK_ID);
        if (bookId != null) {
            loadBookDetails(bookId);
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
                runOnUiThread(() -> displayBookDetails(book));
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
    
    private void showLoading() {
        loadingView.setVisibility(View.VISIBLE);
        contentView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
    }
    
    private void showContent() {
        loadingView.setVisibility(View.GONE);
        contentView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
    }
    
    private void showError(String message) {
        loadingView.setVisibility(View.GONE);
        contentView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
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
