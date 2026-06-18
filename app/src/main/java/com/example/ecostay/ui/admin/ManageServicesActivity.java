package com.example.ecostay.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.ServiceEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.admin.adapters.AdminServiceAdapter;
import com.example.ecostay.ui.admin.viewmodel.AdminServiceViewModel;
import com.example.ecostay.util.ToolbarUtils;

public class ManageServicesActivity extends AppCompatActivity {

    private AdminServiceViewModel serviceViewModel;
    private AdminServiceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this) || !SessionManager.isAdmin(this)) {
            finish();
            return;
        }

        setContentView(R.layout.activity_manage_services);
        ToolbarUtils.setupBackToolbar(this, R.string.manage_services_title);
        serviceViewModel = new ViewModelProvider(this).get(AdminServiceViewModel.class);

        Button btnAdd = findViewById(R.id.btnAdd);
        RecyclerView rvItems = findViewById(R.id.rvItems);
        TextView tvEmpty = findViewById(R.id.tvEmpty);

        adapter = new AdminServiceAdapter(new AdminServiceAdapter.Listener() {
            @Override
            public void onEdit(ServiceEntity service) {
                Intent intent = new Intent(ManageServicesActivity.this, AddEditServiceActivity.class);
                intent.putExtra(AddEditServiceActivity.EXTRA_SERVICE_ID, service.serviceId);
                startActivity(intent);
            }

            @Override
            public void onDelete(ServiceEntity service) {
                new AlertDialog.Builder(ManageServicesActivity.this)
                        .setMessage(R.string.delete_service_confirm)
                        .setPositiveButton(R.string.action_delete, (d, w) ->
                                serviceViewModel.deleteService(service))
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        });
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(adapter);

        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditServiceActivity.class)));

        serviceViewModel.getServices().observe(this, services -> {
            adapter.setItems(services);
            boolean empty = services == null || services.isEmpty();
            tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            rvItems.setVisibility(empty ? View.GONE : View.VISIBLE);
        });

        serviceViewModel.getDeleteResult().observe(this, result -> {
            if (result == null) return;
            if (result.success) {
                Toast.makeText(this, R.string.service_deleted, Toast.LENGTH_SHORT).show();
            } else if ("has_bookings".equals(result.message)) {
                Toast.makeText(this, R.string.error_delete_service_has_bookings, Toast.LENGTH_LONG).show();
            }
        });

        serviceViewModel.loadServices();
    }

    @Override
    protected void onResume() {
        super.onResume();
        serviceViewModel.loadServices();
    }
}
