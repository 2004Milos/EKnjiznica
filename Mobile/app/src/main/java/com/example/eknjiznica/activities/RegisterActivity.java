package com.example.eknjiznica.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eknjiznica.R;
import com.example.eknjiznica.api.ApiService;
import com.example.eknjiznica.api.RetrofitClient;
import com.example.eknjiznica.models.LoginResponse;
import com.example.eknjiznica.models.RegisterRequest;
import com.example.eknjiznica.utils.SharedPreferencesHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private EditText etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private SharedPreferencesHelper prefsHelper;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        prefsHelper = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getInstance().getApiService();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(v -> register());
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void register() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("Registering...");

        RegisterRequest request = new RegisterRequest(email, password);
        Call<LoginResponse> call = apiService.register(request);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Register");

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    prefsHelper.saveLoginResponse(loginResponse);
                    
                    Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                    finish();
                } else {
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "Registration failed";
                        Toast.makeText(RegisterActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Register");
                Toast.makeText(RegisterActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
