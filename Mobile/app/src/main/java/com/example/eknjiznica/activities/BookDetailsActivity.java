package com.example.eknjiznica.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.widget.EditText;
import android.widget.RatingBar;

import com.example.eknjiznica.R;
import com.example.eknjiznica.adapters.ReviewAdapter;
import com.example.eknjiznica.api.ApiService;
import com.example.eknjiznica.api.RetrofitClient;
import com.example.eknjiznica.models.Book;
import com.example.eknjiznica.models.ApiResponse;
import com.example.eknjiznica.models.CreateReviewRequest;
import com.example.eknjiznica.models.Review;
import com.example.eknjiznica.utils.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookDetailsActivity extends AppCompatActivity {
    private Book book;
    private TextView tvTitle, tvAuthor, tvYear, tvGenre, tvAvailable, tvAverageRating;
    private Button btnReserve, btnEdit, btnDelete, btnAddReview;
    private RecyclerView recyclerViewReviews;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;
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
        tvAverageRating = findViewById(R.id.tvAverageRating);
        btnReserve = findViewById(R.id.btnReserve);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        btnAddReview = findViewById(R.id.btnAddReview);
        recyclerViewReviews = findViewById(R.id.recyclerViewReviews);

        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(reviewList, prefsHelper.getUserId());
        reviewAdapter.setOnDeleteClickListener(review -> deleteReview(review.getId()));
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReviews.setAdapter(reviewAdapter);

        displayBook();
        loadReviews();
        loadRating();

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
            btnAddReview.setVisibility(android.view.View.VISIBLE);

            btnReserve.setOnClickListener(v -> reserveBook());
            btnAddReview.setOnClickListener(v -> showAddReviewDialog());
        } else {
            btnAddReview.setVisibility(android.view.View.GONE);
        }
    }

    private void loadReviews() {
        Call<ApiResponse<List<Review>>> call = apiService.getBookReviews(book.getId());
        call.enqueue(new Callback<ApiResponse<List<Review>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Review>>> call, Response<ApiResponse<List<Review>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    reviewList.clear();
                    reviewList.addAll(response.body().getData());
                    reviewAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Review>>> call, Throwable t) {
                // Silently fail - reviews are optional
            }
        });
    }

    private void loadRating() {
        Call<ApiResponse<Object>> call = apiService.getBookRating(book.getId());
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    try {
                        Object data = response.body().getData();
                        if (data instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> ratingData = (Map<String, Object>) data;
                            Object avgRating = ratingData.get("averageRating");
                            Object reviewCount = ratingData.get("reviewCount");
                            
                            if (avgRating != null && reviewCount != null) {
                                double avg = ((Number) avgRating).doubleValue();
                                int count = ((Number) reviewCount).intValue();
                                if (count > 0) {
                                    tvAverageRating.setText(String.format("Average Rating: %.1f/5.0 (%d reviews)", avg, count));
                                } else {
                                    tvAverageRating.setText("No reviews yet");
                                }
                            } else {
                                tvAverageRating.setText("Average Rating: -");
                            }
                        } else {
                            tvAverageRating.setText("Average Rating: -");
                        }
                    } catch (Exception e) {
                        tvAverageRating.setText("Average Rating: -");
                    }
                } else {
                    tvAverageRating.setText("Average Rating: -");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                tvAverageRating.setText("Average Rating: -");
            }
        });
    }

    private void showAddReviewDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Review");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_review, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText etComment = dialogView.findViewById(R.id.etComment);

        builder.setView(dialogView);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            int rating = (int) ratingBar.getRating();
            String comment = etComment.getText().toString().trim();

            if (rating < 1) {
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
                return;
            }

            createReview(rating, comment);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void createReview(int rating, String comment) {
        String token = prefsHelper.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        CreateReviewRequest request = new CreateReviewRequest(book.getId(), rating, comment);
        Call<ApiResponse<Review>> call = apiService.createReview(token, request);
        call.enqueue(new Callback<ApiResponse<Review>>() {
            @Override
            public void onResponse(Call<ApiResponse<Review>> call, Response<ApiResponse<Review>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(BookDetailsActivity.this, "Review added successfully!", Toast.LENGTH_SHORT).show();
                    loadReviews();
                    loadRating();
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "Failed to add review";
                    Toast.makeText(BookDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Review>> call, Throwable t) {
                Toast.makeText(BookDetailsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteReview(int reviewId) {
        String token = prefsHelper.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<ApiResponse<Object>> call = apiService.deleteReview(token, reviewId);
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(BookDetailsActivity.this, "Review deleted successfully", Toast.LENGTH_SHORT).show();
                    loadReviews();
                    loadRating();
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "Failed to delete review";
                    Toast.makeText(BookDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(BookDetailsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
    protected void onResume() {
        super.onResume();
        // Refresh book data when returning from edit
        if (book != null) {
            loadBookDetails();
        }
    }

    private void loadBookDetails() {
        Call<ApiResponse<Book>> call = apiService.getBook(book.getId());
        call.enqueue(new Callback<ApiResponse<Book>>() {
            @Override
            public void onResponse(Call<ApiResponse<Book>> call, Response<ApiResponse<Book>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    book = response.body().getData();
                    displayBook();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Book>> call, Throwable t) {
                // Silently fail - keep existing data
            }
        });
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
