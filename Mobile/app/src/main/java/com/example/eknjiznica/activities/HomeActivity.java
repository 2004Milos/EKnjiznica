package com.example.eknjiznica.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.eknjiznica.R;
import com.example.eknjiznica.api.ApiService;
import com.example.eknjiznica.api.RetrofitClient;
import com.example.eknjiznica.models.ApiResponse;
import com.example.eknjiznica.utils.SharedPreferencesHelper;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    private SharedPreferencesHelper prefsHelper;
    private ApiService apiService;
    private TextView tvWelcome, tvMemberSection, tvLibrarianSection;
    private CardView cvBooks, cvLoans, cvReservations, cvFines, cvManageBooks, cvManageLoans, cvManageReservations, cvManageFines, cvManageMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        prefsHelper = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getInstance().getApiService();

        if (!prefsHelper.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        tvWelcome = findViewById(R.id.tvWelcome);
        tvMemberSection = findViewById(R.id.tvMemberSection);
        tvLibrarianSection = findViewById(R.id.tvLibrarianSection);
        
        String email = prefsHelper.getEmail();
        tvWelcome.setText("Welcome, " + (email != null ? email : "User") + "!");

        setupViews();
        setupNavigation();
        loadStatistics();
    }

    private void loadStatistics() {
        String token = prefsHelper.getAuthHeader();
        if (token == null) return;

        if (prefsHelper.isLibrarian()) {
            Call<ApiResponse<Object>> call = apiService.getLibrarianStatistics(token);
            call.enqueue(new Callback<ApiResponse<Object>>() {
                @Override
                public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        // Statistics loaded - could display in cards if needed
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                    // Silently fail
                }
            });
        } else if (prefsHelper.isMember()) {
            Call<ApiResponse<Object>> call = apiService.getMemberStatistics(token);
            call.enqueue(new Callback<ApiResponse<Object>>() {
                @Override
                public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        // Statistics loaded - could display in cards if needed
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                    // Silently fail
                }
            });
        }
    }

    private void setupViews() {
        cvBooks = findViewById(R.id.cvBooks);
        cvLoans = findViewById(R.id.cvLoans);
        cvReservations = findViewById(R.id.cvReservations);
        cvFines = findViewById(R.id.cvFines);
        cvManageBooks = findViewById(R.id.cvManageBooks);
        cvManageLoans = findViewById(R.id.cvManageLoans);
        cvManageReservations = findViewById(R.id.cvManageReservations);
        cvManageFines = findViewById(R.id.cvManageFines);
        cvManageMembers = findViewById(R.id.cvManageMembers);

        // Show/hide based on role
        if (prefsHelper.isLibrarian()) {
            // Show librarian views
            tvLibrarianSection.setVisibility(View.VISIBLE);
            cvManageBooks.setVisibility(View.VISIBLE);
            cvManageLoans.setVisibility(View.VISIBLE);
            cvManageReservations.setVisibility(View.VISIBLE);
            cvManageFines.setVisibility(View.VISIBLE);
            cvManageMembers.setVisibility(View.VISIBLE);
        } else {
            // Hide librarian views
            tvLibrarianSection.setVisibility(View.GONE);
            cvManageBooks.setVisibility(View.GONE);
            cvManageLoans.setVisibility(View.GONE);
            cvManageReservations.setVisibility(View.GONE);
            cvManageFines.setVisibility(View.GONE);
            cvManageMembers.setVisibility(View.GONE);
        }

        if (prefsHelper.isMember()) {
            // Show member views
            tvMemberSection.setVisibility(View.VISIBLE);
            cvBooks.setVisibility(View.VISIBLE);
            cvLoans.setVisibility(View.VISIBLE);
            cvReservations.setVisibility(View.VISIBLE);
            cvFines.setVisibility(View.VISIBLE);
        } else {
            // Hide member views
            tvMemberSection.setVisibility(View.GONE);
            cvBooks.setVisibility(View.GONE);
            cvLoans.setVisibility(View.GONE);
            cvReservations.setVisibility(View.GONE);
            cvFines.setVisibility(View.GONE);
        }
    }

    private void setupNavigation() {
        // Member navigation
        cvBooks.setOnClickListener(v -> {
            Intent intent = new Intent(this, BooksActivity.class);
            startActivity(intent);
        });

        cvLoans.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyLoansActivity.class);
            startActivity(intent);
        });

        cvReservations.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyReservationsActivity.class);
            startActivity(intent);
        });

        cvFines.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyFinesActivity.class);
            startActivity(intent);
        });

        // Librarian navigation
        cvManageBooks.setOnClickListener(v -> {
            Intent intent = new Intent(this, BooksActivity.class);
            startActivity(intent);
        });

        cvManageLoans.setOnClickListener(v -> {
            Intent intent = new Intent(this, AllLoansActivity.class);
            startActivity(intent);
        });

        cvManageReservations.setOnClickListener(v -> {
            Intent intent = new Intent(this, AllReservationsActivity.class);
            startActivity(intent);
        });

        cvManageFines.setOnClickListener(v -> {
            Intent intent = new Intent(this, AllFinesActivity.class);
            startActivity(intent);
        });

        cvManageMembers.setOnClickListener(v -> {
            Intent intent = new Intent(this, UsersManagementActivity.class);
            startActivity(intent);
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
