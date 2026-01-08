package com.example.eknjiznica;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eknjiznica.activities.HomeActivity;
import com.example.eknjiznica.activities.LoginActivity;
import com.example.eknjiznica.utils.SharedPreferencesHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferencesHelper prefsHelper = new SharedPreferencesHelper(this);

        // Check if user is logged in
        if (prefsHelper.isLoggedIn()) {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        finish();
    }
}