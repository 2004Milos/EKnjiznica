package com.example.eknjiznica.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eknjiznica.R;
import com.example.eknjiznica.adapters.BookAdapter;
import com.example.eknjiznica.api.ApiService;
import com.example.eknjiznica.api.RetrofitClient;
import com.example.eknjiznica.models.ApiResponse;
import com.example.eknjiznica.models.Book;
import com.example.eknjiznica.utils.SharedPreferencesHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BooksActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private BookAdapter adapter;
    private List<Book> bookList;
    private SharedPreferencesHelper prefsHelper;
    private ApiService apiService;
    private FloatingActionButton fabAddBook;
    private TextInputEditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);

        prefsHelper = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getInstance().getApiService();

        recyclerView = findViewById(R.id.recyclerViewBooks);
        fabAddBook = findViewById(R.id.fabAddBook);
        etSearch = findViewById(R.id.etSearch);

        bookList = new ArrayList<>();
        adapter = new BookAdapter(bookList, prefsHelper.isLibrarian(), new BookAdapter.OnBookClickListener() {
            @Override
            public void onBookClick(Book book) {
                Intent intent = new Intent(BooksActivity.this, BookDetailsActivity.class);
                intent.putExtra("book", book);
                startActivity(intent);
            }

            @Override
            public void onReserveClick(Book book) {
                reserveBook(book.getId());
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Show FAB only for librarians
        if (prefsHelper.isLibrarian()) {
            fabAddBook.setVisibility(View.VISIBLE);
            fabAddBook.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddEditBookActivity.class);
                startActivity(intent);
            });
        } else {
            fabAddBook.setVisibility(View.GONE);
        }

        // Setup search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Debounce search - search after user stops typing
                recyclerView.removeCallbacks(searchRunnable);
                recyclerView.postDelayed(searchRunnable, 500);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadBooks();
    }

    private final Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            String searchQuery = etSearch.getText().toString().trim();
            loadBooks(searchQuery.isEmpty() ? null : searchQuery);
        }
    };

    private void loadBooks() {
        loadBooks(null);
    }

    private void loadBooks(String searchQuery) {
        Call<ApiResponse<List<Book>>> call = apiService.getBooks(searchQuery);
        call.enqueue(new Callback<ApiResponse<List<Book>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Book>>> call, Response<ApiResponse<List<Book>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    bookList.clear();
                    bookList.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(BooksActivity.this, "Failed to load books", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Book>>> call, Throwable t) {
                Toast.makeText(BooksActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void reserveBook(int bookId) {
        String token = prefsHelper.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<com.example.eknjiznica.models.ApiResponse<com.example.eknjiznica.models.Reservation>> call = 
            apiService.reserveBook(token, bookId);
        call.enqueue(new Callback<com.example.eknjiznica.models.ApiResponse<com.example.eknjiznica.models.Reservation>>() {
            @Override
            public void onResponse(Call<com.example.eknjiznica.models.ApiResponse<com.example.eknjiznica.models.Reservation>> call, 
                                  Response<com.example.eknjiznica.models.ApiResponse<com.example.eknjiznica.models.Reservation>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(BooksActivity.this, "Book reserved successfully!", Toast.LENGTH_SHORT).show();
                    loadBooks(); // Refresh list
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "Failed to reserve book";
                    Toast.makeText(BooksActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.example.eknjiznica.models.ApiResponse<com.example.eknjiznica.models.Reservation>> call, Throwable t) {
                Toast.makeText(BooksActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBooks(); // Refresh when returning to this activity
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            prefsHelper.clear();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
