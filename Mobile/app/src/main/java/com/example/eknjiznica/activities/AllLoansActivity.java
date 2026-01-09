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
import com.example.eknjiznica.adapters.LoanAdapter;
import com.example.eknjiznica.api.ApiService;
import com.example.eknjiznica.api.RetrofitClient;
import com.example.eknjiznica.models.ApiResponse;
import com.example.eknjiznica.models.Loan;
import com.example.eknjiznica.utils.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllLoansActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private LoanAdapter adapter;
    private List<Loan> loanList;
    private SharedPreferencesHelper prefsHelper;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_loans);

        prefsHelper = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getInstance().getApiService();

        if (!prefsHelper.isLibrarian()) {
            Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerViewLoans);

        loanList = new ArrayList<>();
        adapter = new LoanAdapter(loanList, true);
        adapter.setOnReturnClickListener(loan -> returnLoan(loan.getId()));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadLoans();
    }

    private void loadLoans() {
        String token = prefsHelper.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Call<ApiResponse<List<Loan>>> call = apiService.getAllLoans(token);
        call.enqueue(new Callback<ApiResponse<List<Loan>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Loan>>> call, Response<ApiResponse<List<Loan>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    loanList.clear();
                    loanList.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(AllLoansActivity.this, "Failed to load loans", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Loan>>> call, Throwable t) {
                Toast.makeText(AllLoansActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void returnLoan(int loanId) {
        String token = prefsHelper.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<ApiResponse<Loan>> call = apiService.returnLoan(token, loanId);
        call.enqueue(new Callback<ApiResponse<Loan>>() {
            @Override
            public void onResponse(Call<ApiResponse<Loan>> call, Response<ApiResponse<Loan>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AllLoansActivity.this, "Loan returned successfully!", Toast.LENGTH_SHORT).show();
                    loadLoans();
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "Failed to return loan";
                    Toast.makeText(AllLoansActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Loan>> call, Throwable t) {
                Toast.makeText(AllLoansActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLoans();
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
