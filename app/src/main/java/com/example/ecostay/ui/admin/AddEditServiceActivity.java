package com.example.ecostay.ui.admin;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.ServiceEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.admin.viewmodel.AdminServiceViewModel;
import com.example.ecostay.util.ToolbarUtils;
import com.example.ecostay.util.ValidationUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddEditServiceActivity extends AppCompatActivity {

    public static final String EXTRA_SERVICE_ID = "serviceId";

    private AdminServiceViewModel serviceViewModel;
    private TextInputLayout tilServiceName;
    private TextInputLayout tilDescription;
    private TextInputLayout tilPrice;
    private Spinner spDeviceType;
    private int serviceId = -1;
    private boolean isEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this) || !SessionManager.isAdmin(this)) {
            finish();
            return;
        }

        serviceId = getIntent().getIntExtra(EXTRA_SERVICE_ID, -1);
        isEdit = serviceId > 0;

        setContentView(R.layout.activity_add_edit_service);
        ToolbarUtils.setupBackToolbar(this,
                isEdit ? R.string.edit_service_title : R.string.add_service_title);
        serviceViewModel = new ViewModelProvider(this).get(AdminServiceViewModel.class);

        spDeviceType = findViewById(R.id.spDeviceType);
        tilServiceName = findViewById(R.id.tilServiceName);
        tilDescription = findViewById(R.id.tilDescription);
        tilPrice = findViewById(R.id.tilPrice);
        TextInputEditText etServiceName = findViewById(R.id.etServiceName);
        TextInputEditText etDescription = findViewById(R.id.etDescription);
        TextInputEditText etPrice = findViewById(R.id.etPrice);
        Button btnSave = findViewById(R.id.btnSave);

        ArrayAdapter<CharSequence> deviceAdapter = ArrayAdapter.createFromResource(
                this, R.array.device_types, android.R.layout.simple_spinner_item);
        deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDeviceType.setAdapter(deviceAdapter);

        if (isEdit) {
            MutableLiveData<ServiceEntity> liveData = new MutableLiveData<>();
            liveData.observe(this, service -> {
                if (service == null) return;
                etServiceName.setText(service.serviceName);
                etDescription.setText(service.description);
                etPrice.setText(String.valueOf(service.estimatedPrice));
                setSpinnerSelection(spDeviceType, service.deviceType);
            });
            serviceViewModel.loadServiceById(serviceId, liveData);
        }

        serviceViewModel.getSaveResult().observe(this, result -> {
            if (result != null && result.success) {
                Toast.makeText(this, R.string.service_saved, Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnSave.setOnClickListener(v -> {
            if (!validate(etServiceName, etDescription, etPrice)) return;

            ServiceEntity service = new ServiceEntity();
            if (isEdit) {
                service.serviceId = serviceId;
            }
            service.deviceType = spDeviceType.getSelectedItem().toString();
            service.serviceName = etServiceName.getText().toString().trim();
            service.description = etDescription.getText().toString().trim();
            service.estimatedPrice = Double.parseDouble(etPrice.getText().toString().trim());
            serviceViewModel.saveService(service, isEdit);
        });
    }

    private boolean validate(TextInputEditText etServiceName,
                             TextInputEditText etDescription,
                             TextInputEditText etPrice) {
        tilServiceName.setError(null);
        tilDescription.setError(null);
        tilPrice.setError(null);

        String name = etServiceName.getText() != null ? etServiceName.getText().toString() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString() : "";
        String price = etPrice.getText() != null ? etPrice.getText().toString() : "";

        if (ValidationUtils.isEmpty(name) || ValidationUtils.isEmpty(description)
                || ValidationUtils.isEmpty(price)) {
            if (ValidationUtils.isEmpty(name)) tilServiceName.setError(getString(R.string.error_empty_fields));
            if (ValidationUtils.isEmpty(description)) tilDescription.setError(getString(R.string.error_empty_fields));
            if (ValidationUtils.isEmpty(price)) tilPrice.setError(getString(R.string.error_empty_fields));
            return false;
        }
        if (!ValidationUtils.isValidPrice(price)) {
            tilPrice.setError(getString(R.string.error_valid_price));
            return false;
        }
        return true;
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter == null) return;
        for (int i = 0; i < adapter.getCount(); i++) {
            if (value.equals(adapter.getItem(i).toString())) {
                spinner.setSelection(i);
                return;
            }
        }
    }
}
