package com.example.ecostay.ui;

import android.os.Bundle;
import android.widget.TextView;

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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookingReceiptActivity extends AppCompatActivity {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.getDefault());
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_receipt);

        Long userId = SessionManager.getUserId(this);
        if (userId == null) {
            finish();
            return;
        }
        long bookingId = getIntent().getLongExtra("bookingId", -1L);
        String bookingType = getIntent().getStringExtra("bookingType");
        if (bookingId <= 0 || bookingType == null) {
            finish();
            return;
        }

        TextView tvReceiptTitle = findViewById(R.id.tvReceiptTitle);
        TextView tvReceiptBody = findViewById(R.id.tvReceiptBody);

        EcoStayDatabase database = EcoStayDatabase.getInstance(this);
        RoomBookingDao roomBookingDao = database.roomBookingDao();
        ServiceBookingDao serviceBookingDao = database.serviceBookingDao();
        RoomTypeDao roomTypeDao = database.roomTypeDao();
        ServiceDao serviceDao = database.serviceDao();

        dbExecutor.execute(() -> {
            String body;
            if ("ROOM".equals(bookingType)) {
                RoomBookingEntity booking = roomBookingDao.findByIdForUser(bookingId, userId);
                if (booking == null) {
                    runOnUiThread(this::finish);
                    return;
                }
                RoomTypeEntity roomType = roomTypeDao.findById(booking.roomTypeId);
                String title = roomType == null ? getString(R.string.booking_unknown_room) : roomType.name;
                body = buildReceiptBody(
                        booking.id,
                        "Room Booking",
                        title,
                        LocalDate.ofEpochDay(booking.startDateEpochDay).format(DATE_FORMAT)
                                + " to "
                                + LocalDate.ofEpochDay(booking.endDateEpochDay).format(DATE_FORMAT),
                        booking.status,
                        booking.paymentStatus,
                        booking.paymentMethod,
                        booking.totalAmount,
                        booking.createdAtEpochMillis
                );
            } else {
                ServiceBookingEntity booking = serviceBookingDao.findByIdForUser(bookingId, userId);
                if (booking == null) {
                    runOnUiThread(this::finish);
                    return;
                }
                ServiceEntity service = serviceDao.findById(booking.serviceId);
                String title = service == null ? getString(R.string.booking_unknown_service) : service.name;
                body = buildReceiptBody(
                        booking.id,
                        "Service Booking",
                        title,
                        LocalDate.ofEpochDay(booking.bookingDateEpochDay).format(DATE_FORMAT),
                        booking.status,
                        booking.paymentStatus,
                        booking.paymentMethod,
                        booking.totalAmount,
                        booking.createdAtEpochMillis
                );
            }
            runOnUiThread(() -> {
                tvReceiptTitle.setText(getString(R.string.booking_receipt_title));
                tvReceiptBody.setText(body);
            });
        });
    }

    private String buildReceiptBody(
            long bookingId,
            String type,
            String itemName,
            String schedule,
            String status,
            String paymentStatus,
            String paymentMethod,
            double totalAmount,
            long createdAtEpochMillis
    ) {
        String created = DATE_TIME_FORMAT.format(Instant.ofEpochMilli(createdAtEpochMillis).atZone(ZoneId.systemDefault()));
        String method = paymentMethod == null || paymentMethod.trim().isEmpty() ? "N/A" : paymentMethod;
        StringBuilder sb = new StringBuilder();
        sb.append("Receipt ID: RCP-").append(bookingId).append("\n");
        sb.append("Booking ID: ").append(bookingId).append("\n");
        sb.append("Type: ").append(type).append("\n");
        sb.append("Item: ").append(itemName).append("\n");
        sb.append("Schedule: ").append(schedule).append("\n");
        sb.append("Status: ").append(status).append("\n");
        sb.append("Payment Status: ").append(paymentStatus).append("\n");
        sb.append("Payment Method: ").append(method).append("\n");
        sb.append("Amount: ").append(String.format(Locale.getDefault(), "$%.2f", totalAmount)).append("\n");
        sb.append("Issued At: ").append(created).append("\n");
        sb.append("\n");
        sb.append("Thank you for choosing EcoStay.");
        return sb.toString();
    }
}
