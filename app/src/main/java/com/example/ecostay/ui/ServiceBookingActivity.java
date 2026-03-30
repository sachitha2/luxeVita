package com.example.ecostay.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.EcoStayDatabase;
import com.example.ecostay.data.dao.ServiceBookingDao;
import com.example.ecostay.data.dao.ServiceDao;
import com.example.ecostay.data.entity.ServiceBookingEntity;
import com.example.ecostay.data.entity.ServiceEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.adapters.DateOptionsAdapter;
import com.example.ecostay.util.BookingValidationUtils;

import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.ecostay.notifications.NotificationScheduler;

public class ServiceBookingActivity extends AppCompatActivity {

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    private Long serviceId;
    private ServiceEntity service;

    private TextView tvServiceName;
    private TextView tvServicePrice;
    private RecyclerView rvDates;
    private Button btnReserveOrCancel;
    private TextView tvServiceBookingMessage;

    private DateOptionsAdapter adapter;
    private DateOptionsAdapter.DateOption selectedOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_booking);

        Long userId = SessionManager.getUserId(this);
        if (userId == null) {
            finish();
            return;
        }

        serviceId = getIntent().getLongExtra("serviceId", -1L);
        if (serviceId <= 0) {
            finish();
            return;
        }

        tvServiceName = findViewById(R.id.tvServiceName);
        tvServicePrice = findViewById(R.id.tvServicePrice);
        rvDates = findViewById(R.id.rvDates);
        btnReserveOrCancel = findViewById(R.id.btnReserveOrCancel);
        tvServiceBookingMessage = findViewById(R.id.tvServiceBookingMessage);

        EcoStayDatabase database = EcoStayDatabase.getInstance(this);
        ServiceDao serviceDao = database.serviceDao();
        ServiceBookingDao serviceBookingDao = database.serviceBookingDao();

        adapter = new DateOptionsAdapter(option -> {
            selectedOption = option;
            btnReserveOrCancel.setText(option.isBookedByUser ? "Cancel reservation" : "Reserve");
        });

        rvDates.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvDates.setAdapter(adapter);

        btnReserveOrCancel.setOnClickListener(v -> {
            tvServiceBookingMessage.setText("");
            if (selectedOption == null) {
                tvServiceBookingMessage.setText("Please select a date.");
                return;
            }

            long dateEpochDay = selectedOption.epochDay;

            dbExecutor.execute(() -> {
                if (selectedOption.isBookedByUser) {
                    ServiceBookingEntity existing = serviceBookingDao.findConfirmedByUserServiceDate(userId, serviceId, dateEpochDay);
                    if (existing != null) {
                        NotificationScheduler.cancelBookingReminder(
                                this,
                                "service_booking_reminder_" + existing.id
                        );
                    }
                    serviceBookingDao.deleteConfirmedByUserServiceDate(userId, serviceId, dateEpochDay);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Reservation cancelled.", Toast.LENGTH_SHORT).show();
                        // Reload date options to refresh availability.
                        loadDatesAndRefresh(userId, serviceId, serviceBookingDao);
                    });
                    return;
                }

                int conflictCount = serviceBookingDao.countConfirmedForDate(serviceId, dateEpochDay);
                if (!BookingValidationUtils.isServiceDateAvailable(conflictCount)) {
                    runOnUiThread(() -> tvServiceBookingMessage.setText("Selected date is already fully booked."));
                    return;
                }

                ServiceBookingEntity booking = new ServiceBookingEntity();
                booking.userId = userId;
                booking.serviceId = serviceId;
                booking.bookingDateEpochDay = dateEpochDay;
                booking.status = "CONFIRMED";
                booking.paymentStatus = "PENDING";
                booking.paymentMethod = "Pay at hotel";
                booking.totalAmount = service == null ? 0.0 : service.price;
                booking.createdAtEpochMillis = System.currentTimeMillis();
                booking.updatedAtEpochMillis = booking.createdAtEpochMillis;
                booking.cancelledAtEpochMillis = null;

                long bookingId = serviceBookingDao.insert(booking);

                // Schedule a local reminder 1 day before reservation date (approx. 10:00 local time).
                LocalDate day = LocalDate.ofEpochDay(dateEpochDay);
                LocalDate reminderDay = day.minusDays(1);
                ZonedDateTime reminderDateTime = reminderDay.atTime(10, 0).atZone(ZoneId.systemDefault());
                long delayMillis = reminderDateTime.toInstant().toEpochMilli() - System.currentTimeMillis();
                NotificationScheduler.scheduleBookingReminder(
                        this,
                        "service_booking_reminder_" + bookingId,
                        delayMillis,
                        (int) (bookingId % Integer.MAX_VALUE),
                        "Upcoming service reservation",
                        "Your reservation is on " + day.format(DISPLAY_FORMAT) + "."
                );
                runOnUiThread(() -> {
                    Toast.makeText(this, "Service reserved successfully.", Toast.LENGTH_SHORT).show();
                    loadDatesAndRefresh(userId, serviceId, serviceBookingDao);
                });
            });
        });

        dbExecutor.execute(() -> {
            ServiceEntity s = serviceDao.findById(serviceId);
            service = s;
            runOnUiThread(() -> {
                if (s != null) {
                    tvServiceName.setText(s.name);
                    tvServicePrice.setText("$" + s.price + "");
                }
            });
        });

        loadDatesAndRefresh(userId, serviceId, serviceBookingDao);
    }

    private void loadDatesAndRefresh(long userId, long serviceId, ServiceBookingDao serviceBookingDao) {
        // Rebuild the next-14-days "calendar" options.
        dbExecutor.execute(() -> {
            LocalDate today = LocalDate.now();
            List<DateOptionsAdapter.DateOption> options = new ArrayList<>();

            long selectedEpochDay = selectedOption != null ? selectedOption.epochDay : today.toEpochDay();

            for (int i = 0; i < 14; i++) {
                LocalDate day = today.plusDays(i);
                long epochDay = day.toEpochDay();

                int bookedCount = serviceBookingDao.countConfirmedForDate(serviceId, epochDay);
                ServiceBookingEntity myBooking = serviceBookingDao.findConfirmedByUserServiceDate(userId, serviceId, epochDay);

                boolean isBookedByUser = myBooking != null;
                boolean isAvailable = !isBookedByUser && bookedCount == 0;

                String label;
                if (isBookedByUser) {
                    label = day.format(DISPLAY_FORMAT) + "\nYour booking";
                } else if (bookedCount > 0) {
                    label = day.format(DISPLAY_FORMAT) + "\nBooked";
                } else {
                    label = day.format(DISPLAY_FORMAT) + "\nAvailable";
                }

                options.add(new DateOptionsAdapter.DateOption(epochDay, label, isAvailable, isBookedByUser));
            }

            runOnUiThread(() -> {
                adapter.setItems(options);
                // Try to restore selection.
                adapter.setSelectedEpochDay(selectedEpochDay);
                DateOptionsAdapter.DateOption opt = null;
                if (adapter.getSelectedEpochDay() != -1L) {
                    long selected = adapter.getSelectedEpochDay();
                    for (DateOptionsAdapter.DateOption o : options) {
                        if (o.epochDay == selected) {
                            opt = o;
                            break;
                        }
                    }
                }
                selectedOption = opt;
                if (selectedOption != null) {
                    btnReserveOrCancel.setText(selectedOption.isBookedByUser ? "Cancel reservation" : "Reserve");
                } else if (!options.isEmpty()) {
                    selectedOption = options.get(0);
                    btnReserveOrCancel.setText(selectedOption.isBookedByUser ? "Cancel reservation" : "Reserve");
                }
            });
        });
    }
}

