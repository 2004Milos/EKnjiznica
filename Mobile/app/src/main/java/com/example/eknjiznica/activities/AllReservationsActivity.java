package com.example.eknjiznica.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eknjiznica.R;
import com.example.eknjiznica.adapters.ReservationAdapter;
import com.example.eknjiznica.api.ApiService;
import com.example.eknjiznica.api.RetrofitClient;
import com.example.eknjiznica.models.ApiResponse;
import com.example.eknjiznica.models.Reservation;
import com.example.eknjiznica.utils.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllReservationsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ReservationAdapter adapter;
    private List<Reservation> reservationList;
    private SharedPreferencesHelper prefsHelper;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_reservations);

        prefsHelper = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getInstance().getApiService();

        if (!prefsHelper.isLibrarian()) {
            Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerViewReservations);

        reservationList = new ArrayList<>();
        adapter = new ReservationAdapter(reservationList, true);
        adapter.setOnApproveClickListener(reservation -> approveReservation(reservation.getId()));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadReservations();
    }

    private void loadReservations() {
        String token = prefsHelper.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Call<ApiResponse<List<Reservation>>> call = apiService.getAllReservations(token);
        call.enqueue(new Callback<ApiResponse<List<Reservation>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Reservation>>> call, Response<ApiResponse<List<Reservation>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    reservationList.clear();
                    reservationList.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(AllReservationsActivity.this, "Failed to load reservations", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Reservation>>> call, Throwable t) {
                Toast.makeText(AllReservationsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void approveReservation(int reservationId) {
        String token = prefsHelper.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<ApiResponse<com.example.eknjiznica.models.Loan>> call = apiService.approveReservation(token, reservationId);
        call.enqueue(new Callback<ApiResponse<com.example.eknjiznica.models.Loan>>() {
            @Override
            public void onResponse(Call<ApiResponse<com.example.eknjiznica.models.Loan>> call,
                                  Response<ApiResponse<com.example.eknjiznica.models.Loan>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AllReservationsActivity.this, "Reservation approved and loan created!", Toast.LENGTH_SHORT).show();
                    loadReservations();
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "Failed to approve reservation";
                    Toast.makeText(AllReservationsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<com.example.eknjiznica.models.Loan>> call, Throwable t) {
                Toast.makeText(AllReservationsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReservations();
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
