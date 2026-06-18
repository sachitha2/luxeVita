package com.example.ecostay.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.example.ecostay.data.entity.DeviceEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.adapters.DeviceAdapter;
import com.example.ecostay.ui.viewmodel.DeviceViewModel;
import com.example.ecostay.util.ToolbarUtils;

public class DeviceListActivity extends AppCompatActivity {

    private DeviceViewModel deviceViewModel;
    private DeviceAdapter adapter;
    private TextView tvEmpty;
    private View ivEmpty;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this)) {
            finish();
            return;
        }

        userId = SessionManager.getUserId(this);
        setContentView(R.layout.activity_device_list);
        ToolbarUtils.setupBackToolbar(this, R.string.device_list_title);

        deviceViewModel = new ViewModelProvider(this).get(DeviceViewModel.class);

        RecyclerView rvDevices = findViewById(R.id.rvDevices);
        tvEmpty = findViewById(R.id.tvEmpty);
        ivEmpty = findViewById(R.id.ivEmpty);
        Button btnAdd = findViewById(R.id.btnAddDevice);

        adapter = new DeviceAdapter(new DeviceAdapter.Listener() {
            @Override
            public void onEdit(DeviceEntity device) {
                Intent intent = new Intent(DeviceListActivity.this, AddDeviceActivity.class);
                intent.putExtra(AddDeviceActivity.EXTRA_DEVICE_ID, device.deviceId);
                startActivity(intent);
            }

            @Override
            public void onDelete(DeviceEntity device) {
                new AlertDialog.Builder(DeviceListActivity.this)
                        .setMessage(R.string.delete_device_confirm)
                        .setPositiveButton(R.string.action_delete, (d, w) ->
                                deviceViewModel.deleteDevice(device))
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        });

        rvDevices.setLayoutManager(new LinearLayoutManager(this));
        rvDevices.setAdapter(adapter);

        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddDeviceActivity.class)));

        deviceViewModel.getDevices().observe(this, devices -> {
            boolean empty = devices == null || devices.isEmpty();
            int visibility = empty ? View.VISIBLE : View.GONE;
            tvEmpty.setVisibility(visibility);
            ivEmpty.setVisibility(visibility);
            adapter.setItems(devices);
        });

        deviceViewModel.getDeleteResult().observe(this, result -> {
            if (result != null && result.success) {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        deviceViewModel.loadDevices(userId);
    }
}
