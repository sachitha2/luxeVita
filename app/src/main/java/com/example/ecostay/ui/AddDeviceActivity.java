package com.example.ecostay.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.DeviceEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.viewmodel.DeviceViewModel;
import com.example.ecostay.util.ToolbarUtils;
import com.example.ecostay.util.ValidationUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddDeviceActivity extends AppCompatActivity {

    public static final String EXTRA_DEVICE_ID = "deviceId";

    private DeviceViewModel deviceViewModel;
    private int userId;
    private int deviceId = -1;
    private boolean isEdit;
    private Button btnSave;
    private ProgressBar progressSave;
    private TextInputLayout tilBrand;
    private TextInputLayout tilModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this)) {
            finish();
            return;
        }

        userId = SessionManager.getUserId(this);
        deviceId = getIntent().getIntExtra(EXTRA_DEVICE_ID, -1);
        isEdit = deviceId > 0;

        setContentView(R.layout.activity_add_device);
        ToolbarUtils.setupBackToolbar(this, isEdit ? R.string.edit_device_title : R.string.add_device_title);
        deviceViewModel = new ViewModelProvider(this).get(DeviceViewModel.class);

        TextView tvSubtitle = findViewById(R.id.tvSubtitle);
        tvSubtitle.setText(isEdit ? R.string.edit_device_subtitle : R.string.add_device_subtitle);

        Spinner spDeviceType = findViewById(R.id.spDeviceType);
        TextInputEditText etBrand = findViewById(R.id.etBrand);
        TextInputEditText etModel = findViewById(R.id.etModel);
        btnSave = findViewById(R.id.btnSave);
        progressSave = findViewById(R.id.progressSave);
        tilBrand = findViewById(R.id.tilBrand);
        tilModel = findViewById(R.id.tilModel);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                this, R.array.device_types, R.layout.item_spinner_category);
        typeAdapter.setDropDownViewResource(R.layout.item_spinner_category_dropdown);
        spDeviceType.setAdapter(typeAdapter);

        if (isEdit) {
            MutableLiveData<DeviceEntity> liveData = new MutableLiveData<>();
            liveData.observe(this, device -> {
                if (device == null) return;
                setSpinnerSelection(spDeviceType, device.deviceType);
                etBrand.setText(device.brand);
                etModel.setText(device.model);
            });
            deviceViewModel.loadDevice(deviceId, liveData);
        }

        btnSave.setOnClickListener(v -> {
            clearFieldErrors();
            String brand = textOf(etBrand);
            String model = textOf(etModel);
            String deviceType = spDeviceType.getSelectedItem().toString();

            if (ValidationUtils.isEmpty(brand)) {
                tilBrand.setError(getString(R.string.error_empty_fields));
                return;
            }
            if (ValidationUtils.isEmpty(model)) {
                tilModel.setError(getString(R.string.error_empty_fields));
                return;
            }

            setSaving(true);

            DeviceEntity device = new DeviceEntity();
            device.userId = userId;
            device.deviceType = deviceType;
            device.brand = brand.trim();
            device.model = model.trim();
            if (isEdit) {
                device.deviceId = deviceId;
            }
            deviceViewModel.saveDevice(device, isEdit);
        });

        deviceViewModel.getSaveResult().observe(this, result -> {
            if (result == null) return;
            setSaving(false);
            if (result.success) {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setSaving(boolean saving) {
        btnSave.setEnabled(!saving);
        progressSave.setVisibility(saving ? View.VISIBLE : View.GONE);
    }

    private void clearFieldErrors() {
        tilBrand.setError(null);
        tilModel.setError(null);
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString() : "";
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (value.equals(adapter.getItem(i).toString())) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}
