package com.example.ecostay.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.AppDatabase;
import com.example.ecostay.data.entity.BookingEntity;
import com.example.ecostay.data.entity.DeviceEntity;
import com.example.ecostay.data.entity.ServiceEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.adapters.RepairStatusAdapter;
import com.example.ecostay.ui.viewmodel.BookingViewModel;
import com.example.ecostay.util.TechnicianUtils;
import com.example.ecostay.util.ToolbarUtils;

public class BookingDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_BOOKING_ID = "bookingId";

    private BookingViewModel bookingViewModel;
    private RepairStatusAdapter statusAdapter;
    private TextView tvBookingInfo;
    private LinearLayout layoutTimeline;
    private Button btnUpdateStatus;
    private int bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this)) {
            finish();
            return;
        }

        bookingId = getIntent().getIntExtra(EXTRA_BOOKING_ID, -1);
        if (bookingId <= 0) {
            finish();
            return;
        }

        setContentView(R.layout.activity_booking_details);
        ToolbarUtils.setupBackToolbar(this, R.string.booking_details_title);
        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        tvBookingInfo = findViewById(R.id.tvBookingInfo);
        layoutTimeline = findViewById(R.id.layoutTimeline);
        btnUpdateStatus = findViewById(R.id.btnUpdateStatus);
        RecyclerView rvHistory = findViewById(R.id.rvStatusHistory);

        statusAdapter = new RepairStatusAdapter();
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(statusAdapter);

        bookingViewModel.getBookingDetail().observe(this, this::showBookingDetails);
        bookingViewModel.getStatusHistory().observe(this, statusAdapter::setItems);

        btnUpdateStatus.setOnClickListener(v -> bookingViewModel.advanceStatus(bookingId));

        bookingViewModel.getUpdateStatusResult().observe(this, result -> {
            if (result != null && result.success) {
                Toast.makeText(this, R.string.status_updated, Toast.LENGTH_SHORT).show();
                bookingViewModel.loadBookingDetail(bookingId);
            }
        });

        bookingViewModel.loadBookingDetail(bookingId);
    }

    private void showBookingDetails(BookingEntity booking) {
        if (booking == null) return;

        AppDatabase.getWriteExecutor().execute(() -> {
            ServiceEntity service = AppDatabase.getInstance(this).serviceDao()
                    .getById(booking.serviceId);
            DeviceEntity device = AppDatabase.getInstance(this).deviceDao()
                    .getById(booking.deviceId);

            String serviceName = service != null ? service.serviceName : "Unknown";
            String deviceLabel = device != null
                    ? device.brand + " " + device.model + " (" + device.deviceType + ")"
                    : "Unknown";

            String info = "Booking ID: " + booking.bookingId + "\n"
                    + "Service: " + serviceName + "\n"
                    + "Device: " + deviceLabel + "\n"
                    + "Issue: " + booking.issueDescription + "\n"
                    + getString(R.string.label_service_method, booking.serviceMethod) + "\n"
                    + "Preferred: " + booking.preferredDate + " " + booking.preferredTime + "\n"
                    + getString(R.string.label_status, booking.status) + "\n"
                    + getString(R.string.label_technician, booking.technicianName) + "\n"
                    + getString(R.string.label_estimated_completion, booking.estimatedCompletion);

            runOnUiThread(() -> {
                tvBookingInfo.setText(info);
                renderTimeline(booking.status);
                btnUpdateStatus.setEnabled(TechnicianUtils.canAdvanceStatus(booking.status));
            });
        });
    }

    private void renderTimeline(String currentStatus) {
        layoutTimeline.removeAllViews();
        int currentIndex = TechnicianUtils.getStatusIndex(currentStatus);

        for (int i = 0; i < TechnicianUtils.STATUS_FLOW.length; i++) {
            TextView step = new TextView(this);
            String label = (i <= currentIndex ? "✓ " : "○ ") + TechnicianUtils.STATUS_FLOW[i];
            step.setText(label);
            step.setTextSize(15f);
            step.setPadding(0, 8, 0, 8);
            if (i <= currentIndex) {
                step.setTextColor(ContextCompat.getColor(this, R.color.brand_primary));
                step.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                step.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            }
            layoutTimeline.addView(step);
        }
    }
}
