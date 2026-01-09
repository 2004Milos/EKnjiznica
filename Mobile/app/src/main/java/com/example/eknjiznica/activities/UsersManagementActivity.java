package com.example.eknjiznica.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eknjiznica.R;
import com.example.eknjiznica.adapters.UserAdapter;
import com.example.eknjiznica.api.ApiService;
import com.example.eknjiznica.api.RetrofitClient;
import com.example.eknjiznica.models.ApiResponse;
import com.example.eknjiznica.models.CreateUserRequest;
import com.example.eknjiznica.utils.SharedPreferencesHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersManagementActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<Map<String, Object>> userList;
    private SharedPreferencesHelper prefsHelper;
    private ApiService apiService;
    private FloatingActionButton fabAddUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_management);

        prefsHelper = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getInstance().getApiService();

        if (!prefsHelper.isLibrarian()) {
            Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerViewUsers);
        fabAddUser = findViewById(R.id.fabAddUser);

        userList = new ArrayList<>();
        adapter = new UserAdapter(userList, new UserAdapter.OnUserClickListener() {
            @Override
            public void onDeleteClick(Map<String, Object> user) {

                deleteUser((String) user.get("id"));
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fabAddUser.setOnClickListener(v -> showAddUserDialog());

        loadUsers();
    }

    private void loadUsers() {
        String token = prefsHelper.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Call<ApiResponse<List<Object>>> call = apiService.getAllUsers(token);
        call.enqueue(new Callback<ApiResponse<List<Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Object>>> call, Response<ApiResponse<List<Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    userList.clear();
                    // Convert objects to maps
                    for (Object obj : response.body().getData()) {
                        if (obj instanceof Map) {
                            userList.add((Map<String, Object>) obj);
                        }
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(UsersManagementActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Object>>> call, Throwable t) {
                Toast.makeText(UsersManagementActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddUserDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Add New User");

        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_user, null);
        android.widget.EditText etEmail = dialogView.findViewById(R.id.etEmail);
        android.widget.EditText etPassword = dialogView.findViewById(R.id.etPassword);
        android.widget.Spinner spinnerRole = dialogView.findViewById(R.id.spinnerRole);
        
        // Setup spinner
        String[] roles = {"Member", "Librarian"};
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        builder.setView(dialogView);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String role = spinnerRole.getSelectedItem().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            createUser(email, password, role);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void createUser(String email, String password, String role) {
        String token = prefsHelper.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        CreateUserRequest request = new CreateUserRequest(email, password, role);
        Call<ApiResponse<Object>> call = apiService.createUser(token, request);
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(UsersManagementActivity.this, "User created successfully!", Toast.LENGTH_SHORT).show();
                    loadUsers();
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "Failed to create user";
                    Toast.makeText(UsersManagementActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(UsersManagementActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUser(String userId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    String token = prefsHelper.getAuthHeader();
                    if (token == null) {
                        Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Call<ApiResponse<Object>> call = apiService.deleteUser(token, userId);
                    call.enqueue(new Callback<ApiResponse<Object>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Toast.makeText(UsersManagementActivity.this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                                loadUsers();
                            } else {
                                String message = response.body() != null ? response.body().getMessage() : "Failed to delete user";
                                Toast.makeText(UsersManagementActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                            Toast.makeText(UsersManagementActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
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
