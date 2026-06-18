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
import com.example.ecostay.util.ToolbarUtils;

import java.util.List;

public class BrowseServicesActivity extends AppCompatActivity {

    private ServiceViewModel serviceViewModel;
    private RepairServiceAdapter adapter;
    private TextView tvEmpty;
    private TextView tvResultsCount;
    private View ivEmpty;
    private RecyclerView rvServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this)) {
            finish();
            return;
        }

        setContentView(R.layout.activity_browse_services);
        ToolbarUtils.setupBackToolbar(this, R.string.browse_services_title);

        serviceViewModel = new ViewModelProvider(this).get(ServiceViewModel.class);

        Spinner spCategory = findViewById(R.id.spDeviceCategory);
        rvServices = findViewById(R.id.rvServices);
        tvEmpty = findViewById(R.id.tvEmpty);
        ivEmpty = findViewById(R.id.ivEmpty);
        tvResultsCount = findViewById(R.id.tvResultsCount);

        adapter = new RepairServiceAdapter(service -> {
            Intent intent = new Intent(this, SubmitRepairRequestActivity.class);
            intent.putExtra(SubmitRepairRequestActivity.EXTRA_SERVICE_ID, service.serviceId);
            intent.putExtra(SubmitRepairRequestActivity.EXTRA_DEVICE_TYPE, service.deviceType);
            startActivity(intent);
        });

        rvServices.setLayoutManager(new LinearLayoutManager(this));
        rvServices.setAdapter(adapter);

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                this, R.array.device_categories, R.layout.item_spinner_category);
        categoryAdapter.setDropDownViewResource(R.layout.item_spinner_category_dropdown);
        spCategory.setAdapter(categoryAdapter);

        serviceViewModel.getServices().observe(this, services -> {
            int count = services != null ? services.size() : 0;
            updateEmptyState(count == 0);
            updateResultsCount(count);
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
        int emptyVisibility = empty ? View.VISIBLE : View.GONE;
        tvEmpty.setVisibility(emptyVisibility);
        ivEmpty.setVisibility(emptyVisibility);
        rvServices.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            tvResultsCount.setVisibility(View.GONE);
        }
    }

    private void updateResultsCount(int count) {
        if (count == 0) {
            return;
        }
        tvResultsCount.setText(getString(R.string.services_count, count));
        tvResultsCount.setVisibility(View.VISIBLE);
    }
}
