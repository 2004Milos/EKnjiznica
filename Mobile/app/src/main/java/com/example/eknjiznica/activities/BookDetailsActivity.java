package com.example.eknjiznica.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eknjiznica.R;
import com.example.eknjiznica.api.ApiService;
import com.example.eknjiznica.api.RetrofitClient;
import com.example.eknjiznica.models.Book;
import com.example.eknjiznica.models.ApiResponse;
import com.example.eknjiznica.utils.SharedPreferencesHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookDetailsActivity extends AppCompatActivity {
    private Book book;
    private TextView tvTitle, tvAuthor, tvYear, tvGenre, tvAvailable;
    private Button btnReserve, btnEdit, btnDelete;
    private SharedPreferencesHelper prefsHelper;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        prefsHelper = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getInstance().getApiService();

        book = (Book) getIntent().getSerializableExtra("book");
        if (book == null) {
            Toast.makeText(this, "Book not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTitle = findViewById(R.id.tvTitle);
        tvAuthor = findViewById(R.id.tvAuthor);
        tvYear = findViewById(R.id.tvYear);
        tvGenre = findViewById(R.id.tvGenre);
        tvAvailable = findViewById(R.id.tvAvailable);
        btnReserve = findViewById(R.id.btnReserve);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        displayBook();

        if (prefsHelper.isLibrarian()) {
            btnEdit.setVisibility(android.view.View.VISIBLE);
            btnDelete.setVisibility(android.view.View.VISIBLE);
            btnReserve.setVisibility(android.view.View.GONE);

            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddEditBookActivity.class);
                intent.putExtra("book", book);
                startActivity(intent);
            });

            btnDelete.setOnClickListener(v -> deleteBook());
        } else if (prefsHelper.isMember()) {
            btnReserve.setVisibility(book.isAvailable() ? android.view.View.VISIBLE : android.view.View.GONE);
            btnEdit.setVisibility(android.view.View.GONE);
            btnDelete.setVisibility(android.view.View.GONE);

            btnReserve.setOnClickListener(v -> reserveBook());
        }
    }

    private void displayBook() {
        tvTitle.setText(book.getTitle());
        tvAuthor.setText("Author: " + book.getAuthor());
        tvYear.setText("Year: " + book.getYear());
        tvGenre.setText("Genre: " + book.getGenre());
        tvAvailable.setText(book.isAvailable() ? "Available" : "Not Available");
        tvAvailable.setTextColor(book.isAvailable() ?
            getColor(android.R.color.holo_green_dark) :
            getColor(android.R.color.holo_red_dark));
    }

    private void reserveBook() {
        String token = prefsHelper.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<ApiResponse<com.example.eknjiznica.models.Reservation>> call = apiService.reserveBook(token, book.getId());
        call.enqueue(new Callback<ApiResponse<com.example.eknjiznica.models.Reservation>>() {
            @Override
            public void onResponse(Call<ApiResponse<com.example.eknjiznica.models.Reservation>> call,
                                  Response<ApiResponse<com.example.eknjiznica.models.Reservation>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(BookDetailsActivity.this, "Book reserved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "Failed to reserve book";
                    Toast.makeText(BookDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<com.example.eknjiznica.models.Reservation>> call, Throwable t) {
                Toast.makeText(BookDetailsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteBook() {
        String token = prefsHelper.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<ApiResponse<Object>> call = apiService.deleteBook(token, book.getId());
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(BookDetailsActivity.this, "Book deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "Failed to delete book";
                    Toast.makeText(BookDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(BookDetailsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
