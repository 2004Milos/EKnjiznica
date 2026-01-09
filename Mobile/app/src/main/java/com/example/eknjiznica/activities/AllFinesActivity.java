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
import com.example.eknjiznica.adapters.FineAdapter;
import com.example.eknjiznica.api.ApiService;
import com.example.eknjiznica.api.RetrofitClient;
import android.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.eknjiznica.models.ApiResponse;
import com.example.eknjiznica.models.CreateFineRequest;
import com.example.eknjiznica.models.Fine;
import com.example.eknjiznica.utils.SharedPreferencesHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllFinesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FineAdapter adapter;
    private List<Fine> fineList;
    private SharedPreferencesHelper prefsHelper;
    private ApiService apiService;
    private FloatingActionButton fabAddFine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_fines);

        prefsHelper = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getInstance().getApiService();

        if (!prefsHelper.isLibrarian()) {
            Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerViewFines);
        fabAddFine = findViewById(R.id.fabAddFine);

        fineList = new ArrayList<>();
        adapter = new FineAdapter(fineList, true);
        adapter.setOnMarkPaidClickListener(fine -> markFineAsPaid(fine.getId()));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fabAddFine.setOnClickListener(v -> showAddFineDialog());

        loadFines();
    }

    private void showAddFineDialog() {
        // First load members
        String token = prefsHelper.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<ApiResponse<List<Object>>> call = apiService.getMembers(token);
        call.enqueue(new Callback<ApiResponse<List<Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Object>>> call, Response<ApiResponse<List<Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Object> members = response.body().getData();
                    showAddFineDialogWithMembers(members);
                } else {
                    Toast.makeText(AllFinesActivity.this, "Failed to load members", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Object>>> call, Throwable t) {
                Toast.makeText(AllFinesActivity.this, "Error loading members: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddFineDialogWithMembers(List<Object> members) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create Fine");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_fine, null);
        Spinner spinnerMember = dialogView.findViewById(R.id.spinnerMember);
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        EditText etReason = dialogView.findViewById(R.id.etReason);

        // Setup member spinner
        List<String> memberEmails = new ArrayList<>();
        List<String> memberIds = new ArrayList<>();
        for (Object obj : members) {
            if (obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> member = (Map<String, Object>) obj;
                memberEmails.add((String) member.get("email"));
                memberIds.add((String) member.get("id"));
            }
        }

        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, memberEmails);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMember.setAdapter(adapter);

        builder.setView(dialogView);

        builder.setPositiveButton("Create", (dialog, which) -> {
            int selectedPosition = spinnerMember.getSelectedItemPosition();
            if (selectedPosition < 0 || selectedPosition >= memberIds.size()) {
                Toast.makeText(this, "Please select a member", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = memberIds.get(selectedPosition);
            String amountStr = etAmount.getText().toString().trim();
            String reason = etReason.getText().toString().trim();

            if (amountStr.isEmpty() || reason.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }
                createFine(userId, amount, reason);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void createFine(String userId, double amount, String reason) {
        String token = prefsHelper.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        CreateFineRequest request = new CreateFineRequest(userId, amount, reason);
        Call<ApiResponse<Fine>> call = apiService.createFine(token, request);
        call.enqueue(new Callback<ApiResponse<Fine>>() {
            @Override
            public void onResponse(Call<ApiResponse<Fine>> call, Response<ApiResponse<Fine>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AllFinesActivity.this, "Fine created successfully!", Toast.LENGTH_SHORT).show();
                    loadFines();
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "Failed to create fine";
                    Toast.makeText(AllFinesActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Fine>> call, Throwable t) {
                Toast.makeText(AllFinesActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFines() {
        String token = prefsHelper.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Call<ApiResponse<List<Fine>>> call = apiService.getAllFines(token);
        call.enqueue(new Callback<ApiResponse<List<Fine>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Fine>>> call, Response<ApiResponse<List<Fine>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    fineList.clear();
                    fineList.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(AllFinesActivity.this, "Failed to load fines", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Fine>>> call, Throwable t) {
                Toast.makeText(AllFinesActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markFineAsPaid(int fineId) {
        String token = prefsHelper.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<ApiResponse<Fine>> call = apiService.markFineAsPaid(token, fineId);
        call.enqueue(new Callback<ApiResponse<Fine>>() {
            @Override
            public void onResponse(Call<ApiResponse<Fine>> call, Response<ApiResponse<Fine>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AllFinesActivity.this, "Fine marked as paid!", Toast.LENGTH_SHORT).show();
                    loadFines();
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "Failed to mark fine as paid";
                    Toast.makeText(AllFinesActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Fine>> call, Throwable t) {
                Toast.makeText(AllFinesActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFines();
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
