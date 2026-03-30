package com.example.ecostay.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookingDetailActivity extends AppCompatActivity {

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    private long userId;
    private long bookingId;
    private String bookingType;

    private TextView tvTitle;
    private TextView tvBookingMeta;
    private TextView tvSchedule;
    private TextView tvStatus;
    private TextView tvPayment;
    private TextView tvAmount;
    private Button btnCancel;
    private Button btnModify;
    private Button btnReceipt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_detail);

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

        tvTitle = findViewById(R.id.tvBookingDetailTitle);
        tvBookingMeta = findViewById(R.id.tvBookingDetailMeta);
        tvSchedule = findViewById(R.id.tvBookingDetailSchedule);
        tvStatus = findViewById(R.id.tvBookingDetailStatus);
        tvPayment = findViewById(R.id.tvBookingDetailPayment);
        tvAmount = findViewById(R.id.tvBookingDetailAmount);
        btnCancel = findViewById(R.id.btnBookingDetailCancel);
        btnModify = findViewById(R.id.btnBookingDetailModify);
        btnReceipt = findViewById(R.id.btnBookingDetailReceipt);

        btnCancel.setOnClickListener(v -> cancelBooking());
        btnModify.setOnClickListener(v -> {
            Intent intent = new Intent(BookingDetailActivity.this, ModifyBookingActivity.class);
            intent.putExtra("bookingType", bookingType);
            intent.putExtra("bookingId", bookingId);
            startActivity(intent);
        });
        btnReceipt.setOnClickListener(v -> {
            Intent intent = new Intent(BookingDetailActivity.this, BookingReceiptActivity.class);
            intent.putExtra("bookingType", bookingType);
            intent.putExtra("bookingId", bookingId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindBooking();
    }

    private void bindBooking() {
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
                String title = roomType == null ? getString(R.string.booking_unknown_room) : roomType.name;
                runOnUiThread(() -> {
                    tvTitle.setText(title);
                    tvBookingMeta.setText("Room booking #" + booking.id);
                    tvSchedule.setText(
                            LocalDate.ofEpochDay(booking.startDateEpochDay).format(DISPLAY_FORMAT)
                                    + " to "
                                    + LocalDate.ofEpochDay(booking.endDateEpochDay).format(DISPLAY_FORMAT)
                    );
                    bindCommonState(booking.status, booking.paymentStatus, booking.totalAmount);
                });
            } else {
                ServiceBookingEntity booking = serviceBookingDao.findByIdForUser(bookingId, userId);
                if (booking == null) {
                    runOnUiThread(this::finish);
                    return;
                }
                ServiceEntity service = serviceDao.findById(booking.serviceId);
                String title = service == null ? getString(R.string.booking_unknown_service) : service.name;
                runOnUiThread(() -> {
                    tvTitle.setText(title);
                    tvBookingMeta.setText("Service booking #" + booking.id);
                    tvSchedule.setText(LocalDate.ofEpochDay(booking.bookingDateEpochDay).format(DISPLAY_FORMAT));
                    bindCommonState(booking.status, booking.paymentStatus, booking.totalAmount);
                });
            }
        });
    }

    private void bindCommonState(String status, String paymentStatus, double totalAmount) {
        tvStatus.setText(status);
        tvPayment.setText(paymentStatus);
        tvAmount.setText(String.format(Locale.getDefault(), "$%.2f", totalAmount));
        boolean isConfirmed = "CONFIRMED".equalsIgnoreCase(status);
        btnCancel.setVisibility(isConfirmed ? View.VISIBLE : View.GONE);
        btnModify.setVisibility(isConfirmed ? View.VISIBLE : View.GONE);
    }

    private void cancelBooking() {
        EcoStayDatabase database = EcoStayDatabase.getInstance(this);
        RoomBookingDao roomBookingDao = database.roomBookingDao();
        ServiceBookingDao serviceBookingDao = database.serviceBookingDao();
        dbExecutor.execute(() -> {
            long now = System.currentTimeMillis();
            if ("ROOM".equals(bookingType)) {
                RoomBookingEntity booking = roomBookingDao.findByIdForUser(bookingId, userId);
                if (booking == null || !"CONFIRMED".equals(booking.status)) {
                    return;
                }
                booking.status = "CANCELLED";
                booking.paymentStatus = "REFUNDED";
                booking.updatedAtEpochMillis = now;
                booking.cancelledAtEpochMillis = now;
                roomBookingDao.update(booking);
            } else {
                ServiceBookingEntity booking = serviceBookingDao.findByIdForUser(bookingId, userId);
                if (booking == null || !"CONFIRMED".equals(booking.status)) {
                    return;
                }
                booking.status = "CANCELLED";
                booking.paymentStatus = "REFUNDED";
                booking.updatedAtEpochMillis = now;
                booking.cancelledAtEpochMillis = now;
                serviceBookingDao.update(booking);
            }
            runOnUiThread(() -> {
                Toast.makeText(this, R.string.booking_cancelled, Toast.LENGTH_SHORT).show();
                bindBooking();
            });
        });
    }
}
