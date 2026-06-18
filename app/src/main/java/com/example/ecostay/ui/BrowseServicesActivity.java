package com.example.ecostay.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.ServiceEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.adapters.RepairServiceAdapter;
import com.example.ecostay.ui.viewmodel.ServiceViewModel;

import java.util.List;

public class BrowseServicesActivity extends AppCompatActivity {

    private ServiceViewModel serviceViewModel;
    private RepairServiceAdapter adapter;
    private TextView tvEmpty;
    private List<ServiceEntity> allServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this)) {
            finish();
            return;
        }

        setContentView(R.layout.activity_browse_services);

        serviceViewModel = new ViewModelProvider(this).get(ServiceViewModel.class);

        Spinner spCategory = findViewById(R.id.spDeviceCategory);
        RecyclerView rvServices = findViewById(R.id.rvServices);
        tvEmpty = findViewById(R.id.tvEmpty);

        adapter = new RepairServiceAdapter(service -> {
            Intent intent = new Intent(this, SubmitRepairRequestActivity.class);
            intent.putExtra(SubmitRepairRequestActivity.EXTRA_SERVICE_ID, service.serviceId);
            intent.putExtra(SubmitRepairRequestActivity.EXTRA_DEVICE_TYPE, service.deviceType);
            startActivity(intent);
        });

        rvServices.setLayoutManager(new LinearLayoutManager(this));
        rvServices.setAdapter(adapter);

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                this, R.array.device_categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);

        serviceViewModel.getServices().observe(this, services -> {
            allServices = services;
            updateEmptyState(services == null || services.isEmpty());
            adapter.setItems(services);
        });

        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if ("All".equals(selected)) {
                    serviceViewModel.loadAllServices();
                } else {
                    serviceViewModel.loadServicesByDeviceType(selected);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        serviceViewModel.loadAllServices();
    }

    private void updateEmptyState(boolean empty) {
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
    }
}
