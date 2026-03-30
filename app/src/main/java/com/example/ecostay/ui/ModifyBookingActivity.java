package com.example.ecostay.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.example.ecostay.R;
import com.example.ecostay.data.EcoStayDatabase;
import com.example.ecostay.data.dao.RoomBookingDao;
import com.example.ecostay.data.dao.RoomTypeDao;
import com.example.ecostay.data.dao.ServiceBookingDao;
import com.example.ecostay.data.dao.ServiceDao;
import com.example.ecostay.data.entity.RoomBookingEntity;
import com.example.ecostay.data.entity.RoomTypeEntity;
import com.example.ecostay.data.entity.ServiceBookingEntity;
import com.example.ecostay.data.entity.ServiceEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.util.DateValidationUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ModifyBookingActivity extends AppCompatActivity {

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    private long userId;
    private long bookingId;
    private String bookingType;

    private TextView tvModifyTitle;
    private TextInputEditText etModifyStartDate;
    private TextInputEditText etModifyEndDate;
    private TextInputEditText etModifyServiceDate;
    private TextInputLayout tilModifyStartDate;
    private TextInputLayout tilModifyEndDate;
    private TextInputLayout tilModifyServiceDate;
    private TextView tvModifyMessage;
    private Button btnSave;

    private Long startEpochDay;
    private Long endEpochDay;
    private Long serviceEpochDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_booking);

        Long sessionUserId = SessionManager.getUserId(this);
        if (sessionUserId == null) {
            finish();
            return;
        }
        userId = sessionUserId;

        bookingId = getIntent().getLongExtra("bookingId", -1L);
        bookingType = getIntent().getStringExtra("bookingType");
        if (bookingId <= 0 || bookingType == null) {
            finish();
            return;
        }

        tvModifyTitle = findViewById(R.id.tvModifyBookingTitle);
        etModifyStartDate = findViewById(R.id.etModifyStartDate);
        etModifyEndDate = findViewById(R.id.etModifyEndDate);
        etModifyServiceDate = findViewById(R.id.etModifyServiceDate);
        tilModifyStartDate = findViewById(R.id.tilModifyStartDate);
        tilModifyEndDate = findViewById(R.id.tilModifyEndDate);
        tilModifyServiceDate = findViewById(R.id.tilModifyServiceDate);
        tvModifyMessage = findViewById(R.id.tvModifyBookingMessage);
        btnSave = findViewById(R.id.btnModifyBookingSave);

        etModifyStartDate.setOnClickListener(v -> showDatePicker("START"));
        etModifyEndDate.setOnClickListener(v -> showDatePicker("END"));
        etModifyServiceDate.setOnClickListener(v -> showDatePicker("SERVICE"));

        btnSave.setOnClickListener(v -> saveChanges());

        configureMode();
        loadInitialData();
    }

    private void configureMode() {
        boolean room = "ROOM".equals(bookingType);
        tilModifyStartDate.setVisibility(room ? View.VISIBLE : View.GONE);
        tilModifyEndDate.setVisibility(room ? View.VISIBLE : View.GONE);
        tilModifyServiceDate.setVisibility(room ? View.GONE : View.VISIBLE);
        tvModifyTitle.setText(room ? R.string.modify_room_booking : R.string.modify_service_booking);

        ConstraintLayout root = findViewById(R.id.modifyBookingRoot);
        ConstraintSet set = new ConstraintSet();
        set.clone(root);
        int anchorId = room ? R.id.tilModifyEndDate : R.id.tilModifyServiceDate;
        set.connect(R.id.btnModifyBookingSave, ConstraintSet.TOP, anchorId, ConstraintSet.BOTTOM, 16);
        set.applyTo(root);
    }

    private void loadInitialData() {
        EcoStayDatabase database = EcoStayDatabase.getInstance(this);
        RoomBookingDao roomBookingDao = database.roomBookingDao();
        ServiceBookingDao serviceBookingDao = database.serviceBookingDao();
        RoomTypeDao roomTypeDao = database.roomTypeDao();
        ServiceDao serviceDao = database.serviceDao();

        dbExecutor.execute(() -> {
            if ("ROOM".equals(bookingType)) {
                RoomBookingEntity booking = roomBookingDao.findByIdForUser(bookingId, userId);
                if (booking == null) {
                    runOnUiThread(this::finish);
                    return;
                }
                RoomTypeEntity roomType = roomTypeDao.findById(booking.roomTypeId);
                String roomName = roomType == null ? getString(R.string.booking_unknown_room) : roomType.name;
                startEpochDay = booking.startDateEpochDay;
                endEpochDay = booking.endDateEpochDay;
                runOnUiThread(() -> {
                    tvModifyTitle.setText(getString(R.string.modify_room_booking_for, roomName));
                    etModifyStartDate.setText(LocalDate.ofEpochDay(startEpochDay).format(DISPLAY_FORMAT));
                    etModifyEndDate.setText(LocalDate.ofEpochDay(endEpochDay).format(DISPLAY_FORMAT));
                });
            } else {
                ServiceBookingEntity booking = serviceBookingDao.findByIdForUser(bookingId, userId);
                if (booking == null) {
                    runOnUiThread(this::finish);
                    return;
                }
                ServiceEntity service = serviceDao.findById(booking.serviceId);
                String serviceName = service == null ? getString(R.string.booking_unknown_service) : service.name;
                serviceEpochDay = booking.bookingDateEpochDay;
                runOnUiThread(() -> {
                    tvModifyTitle.setText(getString(R.string.modify_service_booking_for, serviceName));
                    etModifyServiceDate.setText(LocalDate.ofEpochDay(serviceEpochDay).format(DISPLAY_FORMAT));
                });
            }
        });
    }

    private void saveChanges() {
        tvModifyMessage.setText("");
        EcoStayDatabase database = EcoStayDatabase.getInstance(this);
        RoomBookingDao roomBookingDao = database.roomBookingDao();
        ServiceBookingDao serviceBookingDao = database.serviceBookingDao();
        RoomTypeDao roomTypeDao = database.roomTypeDao();
        ServiceDao serviceDao = database.serviceDao();

        dbExecutor.execute(() -> {
            long now = System.currentTimeMillis();
            if ("ROOM".equals(bookingType)) {
                if (startEpochDay == null || endEpochDay == null || !DateValidationUtils.isValidEpochDayRange(startEpochDay, endEpochDay)) {
                    runOnUiThread(() -> tvModifyMessage.setText(getString(R.string.error_invalid_date_range)));
                    return;
                }
                RoomBookingEntity booking = roomBookingDao.findByIdForUser(bookingId, userId);
                if (booking == null || !"CONFIRMED".equals(booking.status)) {
                    return;
                }
                RoomTypeEntity roomType = roomTypeDao.findById(booking.roomTypeId);
                if (roomType == null) return;

                int overlapCount = roomBookingDao.countOverlappingConfirmed(roomType.id, startEpochDay, endEpochDay);
                int available = roomType.totalRooms - overlapCount + 1;
                if (available <= 0) {
                    runOnUiThread(() -> tvModifyMessage.setText(getString(R.string.error_no_availability)));
                    return;
                }
                booking.startDateEpochDay = startEpochDay;
                booking.endDateEpochDay = endEpochDay;
                booking.totalAmount = Math.max(1, endEpochDay - startEpochDay) * roomType.pricePerNight;
                booking.updatedAtEpochMillis = now;
                roomBookingDao.update(booking);
            } else {
                if (serviceEpochDay == null) {
                    runOnUiThread(() -> tvModifyMessage.setText(getString(R.string.error_select_service_date)));
                    return;
                }
                ServiceBookingEntity booking = serviceBookingDao.findByIdForUser(bookingId, userId);
                if (booking == null || !"CONFIRMED".equals(booking.status)) {
                    return;
                }
                int conflicts = serviceBookingDao.countConfirmedForDate(booking.serviceId, serviceEpochDay);
                if (serviceEpochDay != booking.bookingDateEpochDay && conflicts > 0) {
                    runOnUiThread(() -> tvModifyMessage.setText(getString(R.string.error_service_date_booked)));
                    return;
                }
                ServiceEntity service = serviceDao.findById(booking.serviceId);
                booking.bookingDateEpochDay = serviceEpochDay;
                booking.totalAmount = service == null ? booking.totalAmount : service.price;
                booking.updatedAtEpochMillis = now;
                serviceBookingDao.update(booking);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, R.string.booking_updated, Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void showDatePicker(String mode) {
        LocalDate initial = LocalDate.now();
        if ("START".equals(mode) && startEpochDay != null) initial = LocalDate.ofEpochDay(startEpochDay);
        if ("END".equals(mode) && endEpochDay != null) initial = LocalDate.ofEpochDay(endEpochDay);
        if ("SERVICE".equals(mode) && serviceEpochDay != null) initial = LocalDate.ofEpochDay(serviceEpochDay);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            LocalDate picked = LocalDate.of(year, month + 1, dayOfMonth);
            long epochDay = picked.toEpochDay();
            if ("START".equals(mode)) {
                startEpochDay = epochDay;
                etModifyStartDate.setText(picked.format(DISPLAY_FORMAT));
            } else if ("END".equals(mode)) {
                endEpochDay = epochDay;
                etModifyEndDate.setText(picked.format(DISPLAY_FORMAT));
            } else {
                serviceEpochDay = epochDay;
                etModifyServiceDate.setText(picked.format(DISPLAY_FORMAT));
            }
        }, initial.getYear(), initial.getMonthValue() - 1, initial.getDayOfMonth());
        dialog.show();
    }
}
