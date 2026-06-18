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
import com.example.ecostay.data.entity.MaintenanceTipEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.admin.viewmodel.AdminTipViewModel;
import com.example.ecostay.util.ToolbarUtils;
import com.example.ecostay.util.ValidationUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddEditTipActivity extends AppCompatActivity {

    public static final String EXTRA_TIP_ID = "tipId";

    private AdminTipViewModel tipViewModel;
    private TextInputLayout tilTitle;
    private TextInputLayout tilDescription;
    private Spinner spDeviceType;
    private int tipId = -1;
    private boolean isEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this) || !SessionManager.isAdmin(this)) {
            finish();
            return;
        }

        tipId = getIntent().getIntExtra(EXTRA_TIP_ID, -1);
        isEdit = tipId > 0;

        setContentView(R.layout.activity_add_edit_tip);
        ToolbarUtils.setupBackToolbar(this,
                isEdit ? R.string.edit_tip_title : R.string.add_tip_title);
        tipViewModel = new ViewModelProvider(this).get(AdminTipViewModel.class);

        spDeviceType = findViewById(R.id.spDeviceType);
        tilTitle = findViewById(R.id.tilTitle);
        tilDescription = findViewById(R.id.tilDescription);
        TextInputEditText etTitle = findViewById(R.id.etTitle);
        TextInputEditText etDescription = findViewById(R.id.etDescription);
        Button btnSave = findViewById(R.id.btnSave);

        ArrayAdapter<CharSequence> deviceAdapter = ArrayAdapter.createFromResource(
                this, R.array.device_types, android.R.layout.simple_spinner_item);
        deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDeviceType.setAdapter(deviceAdapter);

        if (isEdit) {
            MutableLiveData<MaintenanceTipEntity> liveData = new MutableLiveData<>();
            liveData.observe(this, tip -> {
                if (tip == null) return;
                etTitle.setText(tip.title);
                etDescription.setText(tip.description);
                setSpinnerSelection(spDeviceType, tip.deviceType);
            });
            tipViewModel.loadTipById(tipId, liveData);
        }

        tipViewModel.getSaveResult().observe(this, result -> {
            if (result != null && result.success) {
                Toast.makeText(this, R.string.tip_saved, Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnSave.setOnClickListener(v -> {
            if (!validate(etTitle, etDescription)) return;

            MaintenanceTipEntity tip = new MaintenanceTipEntity();
            if (isEdit) {
                tip.tipId = tipId;
            }
            tip.deviceType = spDeviceType.getSelectedItem().toString();
            tip.title = etTitle.getText().toString().trim();
            tip.description = etDescription.getText().toString().trim();
            tipViewModel.saveTip(tip, isEdit);
        });
    }

    private boolean validate(TextInputEditText etTitle, TextInputEditText etDescription) {
        tilTitle.setError(null);
        tilDescription.setError(null);

        String title = etTitle.getText() != null ? etTitle.getText().toString() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString() : "";

        if (ValidationUtils.isEmpty(title)) {
            tilTitle.setError(getString(R.string.error_empty_fields));
            return false;
        }
        if (ValidationUtils.isEmpty(description)) {
            tilDescription.setError(getString(R.string.error_empty_fields));
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
