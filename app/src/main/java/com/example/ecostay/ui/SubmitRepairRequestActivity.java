package com.example.ecostay.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.example.ecostay.util.PhotoUtils;
import com.example.ecostay.util.ToolbarUtils;
import com.example.ecostay.util.ValidationUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SubmitRepairRequestActivity extends AppCompatActivity {

    public static final String EXTRA_SERVICE_ID = "serviceId";
    public static final String EXTRA_DEVICE_TYPE = "deviceType";

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedPhotoUri = uri;
                    showPhotoPreview(uri);
                }
            });

    private DeviceViewModel deviceViewModel;
    private ServiceViewModel serviceViewModel;
    private BookingViewModel bookingViewModel;

    private Spinner spDevice;
    private Spinner spService;
    private View formContent;
    private View cardNoDevices;
    private View cardPhoto;
    private View ivPhotoIcon;
    private View tvPhotoTitle;
    private View tvPhotoPlaceholder;
    private ImageView ivPhotoPreview;
    private TextView tvRemovePhoto;
    private Button btnSubmit;
    private ProgressBar progressSubmit;
    private TextInputLayout tilIssue;
    private TextInputLayout tilDate;
    private TextInputLayout tilTime;
    private TextInputEditText etDate;
    private TextInputEditText etTime;
    private List<DeviceEntity> userDevices = new ArrayList<>();
    private List<ServiceEntity> services = new ArrayList<>();
    private int preselectedServiceId = -1;
    private String preselectedDeviceType;
    private Uri selectedPhotoUri;

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

        formContent = findViewById(R.id.formContent);
        cardNoDevices = findViewById(R.id.cardNoDevices);
        cardPhoto = findViewById(R.id.cardPhoto);
        ivPhotoIcon = findViewById(R.id.ivPhotoIcon);
        tvPhotoTitle = findViewById(R.id.tvPhotoTitle);
        tvPhotoPlaceholder = findViewById(R.id.tvPhotoPlaceholder);
        ivPhotoPreview = findViewById(R.id.ivPhotoPreview);
        tvRemovePhoto = findViewById(R.id.tvRemovePhoto);
        spDevice = findViewById(R.id.spDevice);
        spService = findViewById(R.id.spService);
        TextInputEditText etIssue = findViewById(R.id.etIssue);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        RadioGroup rgMethod = findViewById(R.id.rgServiceMethod);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressSubmit = findViewById(R.id.progressSubmit);
        tilIssue = findViewById(R.id.tilIssue);
        tilDate = findViewById(R.id.tilDate);
        tilTime = findViewById(R.id.tilTime);

        findViewById(R.id.btnAddDevice).setOnClickListener(v ->
                startActivity(new Intent(this, AddDeviceActivity.class)));

        setupDatePicker();
        setupTimePicker();
        setupPhotoPicker();

        deviceViewModel.getDevices().observe(this, devices -> {
            userDevices = devices != null ? devices : new ArrayList<>();
            updateDeviceAvailability();
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

        btnSubmit.setOnClickListener(v -> submit(userId, etIssue, rgMethod));

        bookingViewModel.getSubmitResult().observe(this, result -> {
            if (result == null) return;
            setSubmitting(false);
            if (result.success) {
                Toast.makeText(this, R.string.booking_submitted, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDatePicker() {
        View.OnClickListener listener = v -> showDatePicker();
        etDate.setOnClickListener(listener);
        tilDate.setEndIconOnClickListener(listener);
    }

    private void setupTimePicker() {
        View.OnClickListener listener = v -> showTimePicker();
        etTime.setOnClickListener(listener);
        tilTime.setEndIconOnClickListener(listener);
    }

    private void setupPhotoPicker() {
        cardPhoto.setOnClickListener(v -> {
            if (selectedPhotoUri == null) {
                pickImageLauncher.launch("image/*");
            } else {
                pickImageLauncher.launch("image/*");
            }
        });
        tvRemovePhoto.setOnClickListener(v -> {
            selectedPhotoUri = null;
            ivPhotoPreview.setVisibility(View.GONE);
            ivPhotoPreview.setImageDrawable(null);
            tvRemovePhoto.setVisibility(View.GONE);
            ivPhotoIcon.setVisibility(View.VISIBLE);
            tvPhotoTitle.setVisibility(View.VISIBLE);
            tvPhotoPlaceholder.setVisibility(View.VISIBLE);
            ((TextView) tvPhotoPlaceholder).setText(R.string.photo_attach_hint);
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    LocalDate date = LocalDate.of(year, month + 1, dayOfMonth);
                    etDate.setText(DateTimeUtils.formatDisplayDate(date));
                    tilDate.setError(null);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    etTime.setText(DateTimeUtils.formatTime(LocalTime.of(hourOfDay, minute)));
                    tilTime.setError(null);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        dialog.show();
    }

    private void showPhotoPreview(Uri uri) {
        ivPhotoPreview.setImageURI(uri);
        ivPhotoPreview.setVisibility(View.VISIBLE);
        tvRemovePhoto.setVisibility(View.VISIBLE);
        ivPhotoIcon.setVisibility(View.GONE);
        tvPhotoTitle.setVisibility(View.GONE);
        tvPhotoPlaceholder.setVisibility(View.GONE);
    }

    private void updateDeviceAvailability() {
        boolean hasDevices = !userDevices.isEmpty();
        formContent.setVisibility(hasDevices ? View.VISIBLE : View.GONE);
        cardNoDevices.setVisibility(hasDevices ? View.GONE : View.VISIBLE);
    }

    private void bindDeviceSpinner() {
        List<String> labels = new ArrayList<>();
        for (DeviceEntity device : userDevices) {
            labels.add(device.brand + " " + device.model + " (" + device.deviceType + ")");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.item_spinner_category, labels);
        adapter.setDropDownViewResource(R.layout.item_spinner_category_dropdown);
        spDevice.setAdapter(adapter);
    }

    private void bindServiceSpinner() {
        List<String> labels = new ArrayList<>();
        for (ServiceEntity service : services) {
            labels.add(service.serviceName + " — Rs. " + (int) service.estimatedPrice);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.item_spinner_category, labels);
        adapter.setDropDownViewResource(R.layout.item_spinner_category_dropdown);
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

    private void submit(int userId, TextInputEditText etIssue, RadioGroup rgMethod) {
        clearFieldErrors();

        if (userDevices.isEmpty()) {
            Toast.makeText(this, R.string.no_devices_for_repair, Toast.LENGTH_SHORT).show();
            return;
        }
        if (services.isEmpty()) {
            Toast.makeText(this, R.string.error_select_service, Toast.LENGTH_SHORT).show();
            return;
        }

        String issue = textOf(etIssue);
        String date = textOf(etDate);
        String time = textOf(etTime);

        boolean valid = true;
        if (!ValidationUtils.isValidIssueDescription(issue)) {
            tilIssue.setError(getString(R.string.error_valid_issue));
            valid = false;
        }
        if (!DateTimeUtils.isValidDate(date)) {
            tilDate.setError(getString(R.string.error_valid_date));
            valid = false;
        }
        if (!DateTimeUtils.isValidTime(time)) {
            tilTime.setError(getString(R.string.error_valid_time));
            valid = false;
        }
        if (!valid) {
            return;
        }

        int methodId = rgMethod.getCheckedRadioButtonId();
        if (methodId == -1) {
            Toast.makeText(this, R.string.error_select_service_method, Toast.LENGTH_SHORT).show();
            return;
        }

        String photoPath = "";
        if (selectedPhotoUri != null) {
            try {
                photoPath = PhotoUtils.savePhotoFromUri(this, selectedPhotoUri);
            } catch (IOException e) {
                Toast.makeText(this, R.string.error_photo_save, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        setSubmitting(true);

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
        booking.photoPath = photoPath;

        bookingViewModel.submitBooking(booking);
    }

    private void setSubmitting(boolean submitting) {
        btnSubmit.setEnabled(!submitting);
        progressSubmit.setVisibility(submitting ? View.VISIBLE : View.GONE);
    }

    private void clearFieldErrors() {
        tilIssue.setError(null);
        tilDate.setError(null);
        tilTime.setError(null);
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString() : "";
    }
}
