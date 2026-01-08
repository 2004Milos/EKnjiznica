package com.example.eknjiznica.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
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

public class AddEditBookActivity extends AppCompatActivity {
    private EditText etTitle, etAuthor, etYear, etGenre;
    private Button btnSave;
    private Book book;
    private boolean isEditMode;
    private SharedPreferencesHelper prefsHelper;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_book);

        prefsHelper = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getInstance().getApiService();

        if (!prefsHelper.isLibrarian()) {
            Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etTitle = findViewById(R.id.etTitle);
        etAuthor = findViewById(R.id.etAuthor);
        etYear = findViewById(R.id.etYear);
        etGenre = findViewById(R.id.etGenre);
        btnSave = findViewById(R.id.btnSave);

        book = (Book) getIntent().getSerializableExtra("book");
        isEditMode = book != null;

        if (isEditMode) {
            setTitle("Edit Book");
            etTitle.setText(book.getTitle());
            etAuthor.setText(book.getAuthor());
            etYear.setText(String.valueOf(book.getYear()));
            etGenre.setText(book.getGenre());
        } else {
            setTitle("Add New Book");
        }

        btnSave.setOnClickListener(v -> saveBook());
    }

    private void saveBook() {
        String title = etTitle.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();
        String yearStr = etYear.getText().toString().trim();
        String genre = etGenre.getText().toString().trim();

        if (title.isEmpty() || author.isEmpty() || yearStr.isEmpty() || genre.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid year", Toast.LENGTH_SHORT).show();
            return;
        }

        Book bookToSave = isEditMode ? book : new Book();
        bookToSave.setTitle(title);
        bookToSave.setAuthor(author);
        bookToSave.setYear(year);
        bookToSave.setGenre(genre);
        if (!isEditMode) {
            bookToSave.setAvailable(true);
        }

        String token = prefsHelper.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        Call<ApiResponse<Book>> call;
        if (isEditMode) {
            call = apiService.updateBook(token, book.getId(), bookToSave);
        } else {
            call = apiService.addBook(token, bookToSave);
        }

        call.enqueue(new Callback<ApiResponse<Book>>() {
            @Override
            public void onResponse(Call<ApiResponse<Book>> call, Response<ApiResponse<Book>> response) {
                btnSave.setEnabled(true);
                btnSave.setText("Save");

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AddEditBookActivity.this, 
                        isEditMode ? "Book updated successfully" : "Book added successfully", 
                        Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "Failed to save book";
                    Toast.makeText(AddEditBookActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Book>> call, Throwable t) {
                btnSave.setEnabled(true);
                btnSave.setText("Save");
                Toast.makeText(AddEditBookActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
