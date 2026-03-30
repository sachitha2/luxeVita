package com.example.ecostay.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.EcoStayDatabase;
import com.example.ecostay.data.dao.ServiceDao;
import com.example.ecostay.data.entity.ServiceEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.adapters.ServiceAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServicesActivity extends AppCompatActivity {

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    private Spinner spCategoryFilter;
    private RecyclerView rvServices;

    private ServiceAdapter adapter;
    private List<ServiceEntity> allServices = new ArrayList<>();

    private static final String[] CATEGORIES = new String[]{"All", "SPA", "DINING", "CABANAS", "TOURS"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        Long userId = SessionManager.getUserId(this);
        if (userId == null) {
            finish();
            return;
        }

        EcoStayDatabase database = EcoStayDatabase.getInstance(this);
        ServiceDao serviceDao = database.serviceDao();

        spCategoryFilter = findViewById(R.id.spCategoryFilter);
        rvServices = findViewById(R.id.rvServices);

        adapter = new ServiceAdapter(service -> {
            Intent intent = new Intent(ServicesActivity.this, ServiceBookingActivity.class);
            intent.putExtra("serviceId", service.id);
            startActivity(intent);
        });

        rvServices.setLayoutManager(new LinearLayoutManager(this));
        rvServices.setAdapter(adapter);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                CATEGORIES
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoryFilter.setAdapter(categoryAdapter);

        spCategoryFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String category = CATEGORIES[position];
                applyFilter(category);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        dbExecutor.execute(() -> {
            allServices = serviceDao.getAll();
            runOnUiThread(() -> adapter.setItems(allServices));
        });
    }

    private void applyFilter(String category) {
        if (allServices == null) return;

        if (category == null || category.equals("All")) {
            adapter.setItems(allServices);
            return;
        }

        List<ServiceEntity> filtered = new ArrayList<>();
        for (ServiceEntity s : allServices) {
            if (s.category != null && s.category.equalsIgnoreCase(category)) {
                filtered.add(s);
            }
        }
        adapter.setItems(filtered);
    }
}

