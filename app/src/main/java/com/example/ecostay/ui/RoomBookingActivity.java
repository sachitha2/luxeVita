package com.example.ecostay.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ecostay.R;
import com.example.ecostay.data.EcoStayDatabase;
import com.example.ecostay.data.dao.RoomBookingDao;
import com.example.ecostay.data.dao.RoomTypeDao;
import com.example.ecostay.data.entity.RoomBookingEntity;
import com.example.ecostay.data.entity.RoomTypeEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.util.BookingValidationUtils;
import com.example.ecostay.util.DateValidationUtils;

import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.ecostay.notifications.NotificationScheduler;

public class RoomBookingActivity extends AppCompatActivity {

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    private Long roomTypeId;
    private RoomTypeEntity roomType;

    private TextView tvRoomName;
    private TextView tvRoomPrice;
    private TextInputEditText etStartDate;
    private TextInputEditText etEndDate;
    private Button btnConfirmBooking;
    private TextView tvBookingMessage;

    private Long startEpochDay;
    private Long endEpochDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_booking);

        tvRoomName = findViewById(R.id.tvRoomName);
        tvRoomPrice = findViewById(R.id.tvRoomPrice);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        tvBookingMessage = findViewById(R.id.tvBookingMessage);

        Long userId = SessionManager.getUserId(this);
        if (userId == null) {
            finish();
            return;
        }

        roomTypeId = getIntent().getLongExtra("roomTypeId", -1L);
        if (roomTypeId <= 0) {
            finish();
            return;
        }

        EcoStayDatabase database = EcoStayDatabase.getInstance(this);
        RoomTypeDao roomTypeDao = database.roomTypeDao();
        RoomBookingDao roomBookingDao = database.roomBookingDao();

        etStartDate.setOnClickListener(v -> showDatePicker(true));
        etEndDate.setOnClickListener(v -> showDatePicker(false));

        btnConfirmBooking.setOnClickListener(v -> {
            tvBookingMessage.setText("");

            if (startEpochDay == null || endEpochDay == null) {
                tvBookingMessage.setText("Please select start and end dates.");
                return;
            }
            if (!DateValidationUtils.isValidEpochDayRange(startEpochDay, endEpochDay)) {
                tvBookingMessage.setText("Start date must be before end date.");
                return;
            }

            dbExecutor.execute(() -> {
                // Reload room type to get latest inventory value.
                RoomTypeEntity rt = roomTypeDao.findById(roomTypeId);
                if (rt == null) return;

                int overlapping = roomBookingDao.countOverlappingConfirmed(rt.id, startEpochDay, endEpochDay);
                if (!BookingValidationUtils.isRoomAvailable(overlapping, rt.totalRooms)) {
                    runOnUiThread(() -> tvBookingMessage.setText("No availability for selected dates. Try another range."));
                    return;
                }

                RoomBookingEntity booking = new RoomBookingEntity();
                booking.userId = userId;
                booking.roomTypeId = rt.id;
                booking.startDateEpochDay = startEpochDay;
                booking.endDateEpochDay = endEpochDay;
                booking.status = "CONFIRMED";
                booking.paymentStatus = "PENDING";
                booking.paymentMethod = "Pay at hotel";
                long nights = Math.max(1, endEpochDay - startEpochDay);
                booking.totalAmount = nights * rt.pricePerNight;
                booking.createdAtEpochMillis = System.currentTimeMillis();
                booking.updatedAtEpochMillis = booking.createdAtEpochMillis;
                booking.cancelledAtEpochMillis = null;

                long bookingId = roomBookingDao.insert(booking);

                // Schedule a local reminder 1 day before check-in (approx. 09:00 local time).
                LocalDate start = LocalDate.ofEpochDay(startEpochDay);
                LocalDate reminderDay = start.minusDays(1);
                ZonedDateTime reminderDateTime = reminderDay.atTime(9, 0).atZone(ZoneId.systemDefault());
                long delayMillis = reminderDateTime.toInstant().toEpochMilli() - System.currentTimeMillis();
                String uniqueWorkName = "room_booking_reminder_" + bookingId;

                NotificationScheduler.scheduleBookingReminder(
                        RoomBookingActivity.this,
                        uniqueWorkName,
                        delayMillis,
                        (int) (bookingId % Integer.MAX_VALUE),
                        "Upcoming room booking",
                        "Your stay starts on " + start.format(DISPLAY_FORMAT) + "."
                );

                runOnUiThread(() -> {
                    Toast.makeText(RoomBookingActivity.this, "Room booked successfully.", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        });

        dbExecutor.execute(() -> {
            RoomTypeEntity rt = roomTypeDao.findById(roomTypeId);
            roomType = rt;
            if (rt == null) return;
            runOnUiThread(() -> {
                tvRoomName.setText(rt.name);
                tvRoomPrice.setText("$" + rt.pricePerNight + " / night");
            });
        });
    }

    private void showDatePicker(boolean isStart) {
        LocalDate initial = LocalDate.now();
        if (isStart && startEpochDay != null) initial = LocalDate.ofEpochDay(startEpochDay);
        if (!isStart && endEpochDay != null) initial = LocalDate.ofEpochDay(endEpochDay);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            LocalDate picked = LocalDate.of(year, month + 1, dayOfMonth);
            long epoch = picked.toEpochDay();
            if (isStart) {
                startEpochDay = epoch;
                etStartDate.setText(picked.format(DISPLAY_FORMAT));
            } else {
                endEpochDay = epoch;
                etEndDate.setText(picked.format(DISPLAY_FORMAT));
            }
        }, initial.getYear(), initial.getMonthValue() - 1, initial.getDayOfMonth());
        dialog.show();
    }
}

