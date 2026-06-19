package com.example.ecostay.ui.admin;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.model.AdminBookingDetail;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.admin.adapters.AdminRepairStatusAdapter;
import com.example.ecostay.ui.admin.viewmodel.AdminBookingViewModel;
import com.example.ecostay.util.TechnicianUtils;
import com.example.ecostay.util.ToolbarUtils;
import com.google.android.material.textfield.TextInputEditText;

public class AdminBookingDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_BOOKING_ID = "bookingId";

    private AdminBookingViewModel bookingViewModel;
    private AdminRepairStatusAdapter statusAdapter;
    private Spinner spTechnician;
    private Spinner spStatus;
    private TextInputEditText etEstimatedCompletion;
    private TextInputEditText etRemarks;
    private TextView tvBookingInfo;
    private int bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this) || !SessionManager.isAdmin(this)) {
            finish();
            return;
        }

        bookingId = getIntent().getIntExtra(EXTRA_BOOKING_ID, -1);
        if (bookingId <= 0) {
            finish();
            return;
        }

        setContentView(R.layout.activity_admin_booking_details);
        ToolbarUtils.setupBackToolbar(this, R.string.admin_booking_details_title);
        bookingViewModel = new ViewModelProvider(this).get(AdminBookingViewModel.class);

        tvBookingInfo = findViewById(R.id.tvBookingInfo);
        spTechnician = findViewById(R.id.spTechnician);
        spStatus = findViewById(R.id.spStatus);
        etEstimatedCompletion = findViewById(R.id.etEstimatedCompletion);
        etRemarks = findViewById(R.id.etRemarks);
        Button btnSave = findViewById(R.id.btnSave);
        RecyclerView rvHistory = findViewById(R.id.rvStatusHistory);

        ArrayAdapter<String> technicianAdapter = new ArrayAdapter<>(
                this, R.layout.item_spinner_category, TechnicianUtils.getAllTechnicianLabels());
        technicianAdapter.setDropDownViewResource(R.layout.item_spinner_category_dropdown);
        spTechnician.setAdapter(technicianAdapter);

        ArrayAdapter<String> statusAdapterSpinner = new ArrayAdapter<>(
                this, R.layout.item_spinner_category, TechnicianUtils.ADMIN_STATUS_OPTIONS);
        statusAdapterSpinner.setDropDownViewResource(R.layout.item_spinner_category_dropdown);
        spStatus.setAdapter(statusAdapterSpinner);

        statusAdapter = new AdminRepairStatusAdapter();
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(statusAdapter);

        bookingViewModel.getAdminBookingDetail().observe(this, this::showDetail);
        bookingViewModel.getStatusHistory().observe(this, statusAdapter::setItems);
        bookingViewModel.getAdminUpdateResult().observe(this, result -> {
            if (result != null && result.success) {
                Toast.makeText(this, R.string.admin_booking_saved, Toast.LENGTH_SHORT).show();
            }
        });

        btnSave.setOnClickListener(v -> saveChanges());

        bookingViewModel.loadAdminBookingDetail(bookingId);
    }

    private void showDetail(AdminBookingDetail detail) {
        if (detail == null || detail.booking == null) return;

        StringBuilder info = new StringBuilder();
        info.append(getString(R.string.label_booking_id, detail.booking.bookingId)).append("\n");
        if (detail.customer != null) {
            info.append(getString(R.string.label_customer, detail.customer.fullName)).append("\n");
            info.append(detail.customer.email).append(" | ").append(detail.customer.phone).append("\n");
            info.append(detail.customer.address).append("\n\n");
        }
        if (detail.device != null) {
            info.append(detail.device.brand).append(" ")
                    .append(detail.device.model).append(" (")
                    .append(detail.device.deviceType).append(")\n");
        }
        if (detail.service != null) {
            info.append(detail.service.serviceName).append("\n");
            info.append(detail.service.description).append("\n\n");
        }
        info.append(getString(R.string.label_issue)).append(": ")
                .append(detail.booking.issueDescription).append("\n");
        info.append(getString(R.string.label_service_method, detail.booking.serviceMethod)).append("\n");
        info.append(getString(R.string.label_preferred_schedule,
                detail.booking.preferredDate, detail.booking.preferredTime)).append("\n");
        info.append(getString(R.string.label_status, detail.booking.status)).append("\n");
        info.append(getString(R.string.label_technician, detail.booking.technicianName)).append("\n");
        info.append(getString(R.string.label_estimated_completion,
                detail.booking.estimatedCompletion));
        tvBookingInfo.setText(info);

        etEstimatedCompletion.setText(detail.booking.estimatedCompletion);
        if (detail.booking.adminRemarks != null && !detail.booking.adminRemarks.isEmpty()) {
            etRemarks.setText(detail.booking.adminRemarks);
        }

        setSpinnerSelection(spTechnician,
                TechnicianUtils.getTechnicianLabelForName(detail.booking.technicianName));
        setSpinnerSelection(spStatus, detail.booking.status);
    }

    private void saveChanges() {
        String technicianLabel = spTechnician.getSelectedItem().toString();
        String technicianName = TechnicianUtils.getTechnicianNameFromLabel(technicianLabel);
        String status = spStatus.getSelectedItem().toString();
        String estimated = etEstimatedCompletion.getText() != null
                ? etEstimatedCompletion.getText().toString() : "";
        String remarks = etRemarks.getText() != null ? etRemarks.getText().toString() : "";

        bookingViewModel.updateBookingAsAdmin(
                bookingId,
                status,
                technicianName,
                estimated,
                remarks,
                SessionManager.getUserName(this));
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
