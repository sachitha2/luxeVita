package com.example.ecostay.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.BookingEntity;
import com.example.ecostay.data.entity.DeviceEntity;
import com.example.ecostay.data.entity.ServiceEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.viewmodel.BookingViewModel;
import com.example.ecostay.ui.viewmodel.DeviceViewModel;
import com.example.ecostay.ui.viewmodel.ServiceViewModel;
import com.example.ecostay.util.DateTimeUtils;
import com.example.ecostay.util.ToolbarUtils;
import com.example.ecostay.util.ValidationUtils;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class SubmitRepairRequestActivity extends AppCompatActivity {

    public static final String EXTRA_SERVICE_ID = "serviceId";
    public static final String EXTRA_DEVICE_TYPE = "deviceType";

    private DeviceViewModel deviceViewModel;
    private ServiceViewModel serviceViewModel;
    private BookingViewModel bookingViewModel;

    private Spinner spDevice;
    private Spinner spService;
    private List<DeviceEntity> userDevices = new ArrayList<>();
    private List<ServiceEntity> services = new ArrayList<>();
    private int preselectedServiceId = -1;
    private String preselectedDeviceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this)) {
            finish();
            return;
        }

        preselectedServiceId = getIntent().getIntExtra(EXTRA_SERVICE_ID, -1);
        preselectedDeviceType = getIntent().getStringExtra(EXTRA_DEVICE_TYPE);

        setContentView(R.layout.activity_submit_repair);
        ToolbarUtils.setupBackToolbar(this, R.string.submit_repair_title);

        int userId = SessionManager.getUserId(this);
        deviceViewModel = new ViewModelProvider(this).get(DeviceViewModel.class);
        serviceViewModel = new ViewModelProvider(this).get(ServiceViewModel.class);
        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        spDevice = findViewById(R.id.spDevice);
        spService = findViewById(R.id.spService);
        TextInputEditText etIssue = findViewById(R.id.etIssue);
        TextInputEditText etDate = findViewById(R.id.etDate);
        TextInputEditText etTime = findViewById(R.id.etTime);
        RadioGroup rgMethod = findViewById(R.id.rgServiceMethod);

        deviceViewModel.getDevices().observe(this, devices -> {
            userDevices = devices != null ? devices : new ArrayList<>();
            bindDeviceSpinner();
        });
        deviceViewModel.loadDevices(userId);

        serviceViewModel.getServices().observe(this, list -> {
            services = list != null ? list : new ArrayList<>();
            bindServiceSpinner();
        });

        if (preselectedDeviceType != null) {
            serviceViewModel.loadServicesByDeviceType(preselectedDeviceType);
        } else {
            serviceViewModel.loadAllServices();
        }

        findViewById(R.id.btnSubmit).setOnClickListener(v -> submit(
                userId, etIssue, etDate, etTime, rgMethod));

        bookingViewModel.getSubmitResult().observe(this, result -> {
            if (result == null) return;
            if (result.success) {
                Toast.makeText(this, R.string.booking_submitted, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindDeviceSpinner() {
        List<String> labels = new ArrayList<>();
        for (DeviceEntity device : userDevices) {
            labels.add(device.brand + " " + device.model + " (" + device.deviceType + ")");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDevice.setAdapter(adapter);
    }

    private void bindServiceSpinner() {
        List<String> labels = new ArrayList<>();
        for (ServiceEntity service : services) {
            labels.add(service.serviceName + " - Rs. " + (int) service.estimatedPrice);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spService.setAdapter(adapter);

        if (preselectedServiceId > 0) {
            for (int i = 0; i < services.size(); i++) {
                if (services.get(i).serviceId == preselectedServiceId) {
                    spService.setSelection(i);
                    break;
                }
            }
        }
    }

    private void submit(int userId, TextInputEditText etIssue, TextInputEditText etDate,
                        TextInputEditText etTime, RadioGroup rgMethod) {
        if (userDevices.isEmpty()) {
            Toast.makeText(this, R.string.error_select_device, Toast.LENGTH_SHORT).show();
            return;
        }
        if (services.isEmpty()) {
            Toast.makeText(this, R.string.error_select_service, Toast.LENGTH_SHORT).show();
            return;
        }

        String issue = textOf(etIssue);
        String date = textOf(etDate);
        String time = textOf(etTime);

        if (!ValidationUtils.isValidIssueDescription(issue)) {
            Toast.makeText(this, R.string.error_valid_issue, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!DateTimeUtils.isValidDate(date)) {
            Toast.makeText(this, R.string.error_valid_date, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!DateTimeUtils.isValidTime(time)) {
            Toast.makeText(this, R.string.error_valid_time, Toast.LENGTH_SHORT).show();
            return;
        }

        int methodId = rgMethod.getCheckedRadioButtonId();
        if (methodId == -1) {
            Toast.makeText(this, R.string.error_select_service_method, Toast.LENGTH_SHORT).show();
            return;
        }

        String serviceMethod = methodId == R.id.rbPickup
                ? getString(R.string.method_pickup)
                : getString(R.string.method_dropoff);

        int deviceIndex = spDevice.getSelectedItemPosition();
        int serviceIndex = spService.getSelectedItemPosition();

        BookingEntity booking = new BookingEntity();
        booking.userId = userId;
        booking.deviceId = userDevices.get(deviceIndex).deviceId;
        booking.serviceId = services.get(serviceIndex).serviceId;
        booking.issueDescription = issue.trim();
        booking.serviceMethod = serviceMethod;
        booking.preferredDate = date.trim();
        booking.preferredTime = time.trim();

        bookingViewModel.submitBooking(booking);
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString() : "";
    }
}
