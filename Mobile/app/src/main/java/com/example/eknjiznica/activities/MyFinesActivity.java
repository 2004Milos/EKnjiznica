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
import com.example.eknjiznica.models.Fine;
import com.example.eknjiznica.utils.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

// Note: This activity assumes there will be a Fines API endpoint
// For now, it's a placeholder that can be connected when the API is available
public class MyFinesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FineAdapter adapter;
    private List<Fine> fineList;
    private SharedPreferencesHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_fines);

        prefsHelper = new SharedPreferencesHelper(this);

        recyclerView = findViewById(R.id.recyclerViewFines);

        fineList = new ArrayList<>();
        adapter = new FineAdapter(fineList, false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // TODO: Load fines from API when endpoint is available
        Toast.makeText(this, "Fines feature - API endpoint needed", Toast.LENGTH_SHORT).show();
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
