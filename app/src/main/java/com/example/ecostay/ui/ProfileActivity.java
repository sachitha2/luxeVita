package com.example.ecostay.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ecostay.R;
import com.example.ecostay.data.EcoStayDatabase;
import com.example.ecostay.data.dao.OfferDao;
import com.example.ecostay.data.dao.RoomTypeDao;
import com.example.ecostay.data.dao.RoomBookingDao;
import com.example.ecostay.data.dao.ServiceBookingDao;
import com.example.ecostay.data.dao.UserDao;
import com.example.ecostay.data.entity.RoomBookingEntity;
import com.example.ecostay.data.entity.RoomTypeEntity;
import com.example.ecostay.data.entity.ServiceBookingEntity;
import com.example.ecostay.data.entity.UserEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.util.DateValidationUtils;

import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    private Spinner spPreferredRoomType;
    private TextInputEditText etTravelStart;
    private TextInputEditText etTravelEnd;
    private TextInputEditText etMaxBudget;
    private Button btnSaveProfile;
    private TextView tvBookingHistory;

    private Long selectedTravelStartEpochDay;
    private Long selectedTravelEndEpochDay;

    private List<RoomTypeEntity> roomTypes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        spPreferredRoomType = findViewById(R.id.spPreferredRoomType);
        etTravelStart = findViewById(R.id.etTravelStart);
        etTravelEnd = findViewById(R.id.etTravelEnd);
        etMaxBudget = findViewById(R.id.etMaxBudget);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        tvBookingHistory = findViewById(R.id.tvBookingHistory);

        EcoStayDatabase database = EcoStayDatabase.getInstance(this);
        UserDao userDao = database.userDao();
        RoomTypeDao roomTypeDao = database.roomTypeDao();
        RoomBookingDao roomBookingDao = database.roomBookingDao();
        ServiceBookingDao serviceBookingDao = database.serviceBookingDao();

        Long userId = SessionManager.getUserId(this);
        if (userId == null) {
            finish();
            return;
        }

        // Date pickers
        etTravelStart.setOnClickListener(v -> showDatePicker(true));
        etTravelEnd.setOnClickListener(v -> showDatePicker(false));

        btnSaveProfile.setOnClickListener(v -> {
            String maxBudgetStr = etMaxBudget.getText() == null ? "" : etMaxBudget.getText().toString().trim();

            if (selectedTravelStartEpochDay == null || selectedTravelEndEpochDay == null) {
                toast("Please select travel start and end dates.");
                return;
            }

            if (!DateValidationUtils.isValidEpochDayRange(selectedTravelStartEpochDay, selectedTravelEndEpochDay)) {
                toast("Travel start date must be before travel end date.");
                return;
            }

            Double parsedMaxBudget = null;
            if (!maxBudgetStr.isEmpty()) {
                try {
                    parsedMaxBudget = Double.parseDouble(maxBudgetStr);
                    if (parsedMaxBudget < 0) throw new NumberFormatException("negative");
                } catch (NumberFormatException e) {
                    toast("Max budget must be a valid number.");
                    return;
                }
            }

            final Double finalMaxBudget = parsedMaxBudget;
            final long preferredRoomTypeId = getSelectedPreferredRoomTypeIdOrZero();

            dbExecutor.execute(() -> {
                UserEntity user = userDao.findById(userId);
                if (user == null) return;

                user.travelStartDateEpochDay = selectedTravelStartEpochDay;
                user.travelEndDateEpochDay = selectedTravelEndEpochDay;
                user.maxBudget = finalMaxBudget;
                user.preferredRoomTypeId = preferredRoomTypeId == 0 ? null : preferredRoomTypeId;
                userDao.update(user);

                // Refresh booking history after saving.
                List<RoomBookingEntity> roomBookings = roomBookingDao.getAllByUser(userId);
                List<ServiceBookingEntity> serviceBookings = serviceBookingDao.getAllByUser(userId);

                String historyText = buildBookingHistoryText(roomBookings, serviceBookings);
                runOnUiThread(() -> {
                    tvBookingHistory.setText(historyText);
                    toast("Profile saved.");
                });
            });
        });

        // Load room types + profile + booking history.
        dbExecutor.execute(() -> {
            UserEntity user = userDao.findById(userId);
            roomTypes = roomTypeDao.getAll();

            List<String> spinnerItems = new ArrayList<>();
            spinnerItems.add("No preference");
            for (RoomTypeEntity rt : roomTypes) spinnerItems.add(rt.name);

            List<RoomBookingEntity> roomBookings = roomBookingDao.getAllByUser(userId);
            List<ServiceBookingEntity> serviceBookings = serviceBookingDao.getAllByUser(userId);
            final String historyText = buildBookingHistoryText(roomBookings, serviceBookings);

            long preferredId = user == null || user.preferredRoomTypeId == null ? 0 : user.preferredRoomTypeId;

            int selectionIndex = 0; // 0 = No preference
            if (preferredId != 0) {
                for (int i = 0; i < roomTypes.size(); i++) {
                    if (roomTypes.get(i).id == preferredId) {
                        selectionIndex = i + 1;
                        break;
                    }
                }
            }

            long startEpoch = user == null ? 0 : (user.travelStartDateEpochDay == null ? 0 : user.travelStartDateEpochDay);
            long endEpoch = user == null ? 0 : (user.travelEndDateEpochDay == null ? 0 : user.travelEndDateEpochDay);

            Double budget = user == null ? null : user.maxBudget;

            long finalSelectionIndex = selectionIndex;
            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerItems);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spPreferredRoomType.setAdapter(adapter);
                spPreferredRoomType.setSelection((int) finalSelectionIndex);

                if (startEpoch != 0) {
                    selectedTravelStartEpochDay = startEpoch;
                    etTravelStart.setText(LocalDate.ofEpochDay(startEpoch).format(DISPLAY_FORMAT));
                }
                if (endEpoch != 0) {
                    selectedTravelEndEpochDay = endEpoch;
                    etTravelEnd.setText(LocalDate.ofEpochDay(endEpoch).format(DISPLAY_FORMAT));
                }

                if (budget != null) {
                    etMaxBudget.setText(String.valueOf(budget));
                }
                tvBookingHistory.setText(historyText);
            });
        });
    }

    private void showDatePicker(boolean isStart) {
        LocalDate initial = LocalDate.now();
        if (isStart && selectedTravelStartEpochDay != null) initial = LocalDate.ofEpochDay(selectedTravelStartEpochDay);
        if (!isStart && selectedTravelEndEpochDay != null) initial = LocalDate.ofEpochDay(selectedTravelEndEpochDay);

        int year = initial.getYear();
        int month = initial.getMonthValue() - 1;
        int day = initial.getDayOfMonth();

        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            LocalDate picked = LocalDate.of(y, m + 1, d);
            long epochDay = picked.toEpochDay();
            if (isStart) {
                selectedTravelStartEpochDay = epochDay;
                etTravelStart.setText(picked.format(DISPLAY_FORMAT));
            } else {
                selectedTravelEndEpochDay = epochDay;
                etTravelEnd.setText(picked.format(DISPLAY_FORMAT));
            }
        }, year, month, day);
        dialog.show();
    }

    private long getSelectedPreferredRoomTypeIdOrZero() {
        int pos = spPreferredRoomType.getSelectedItemPosition();
        if (pos <= 0) return 0;
        if (pos - 1 >= roomTypes.size()) return 0;
        return roomTypes.get(pos - 1).id;
    }

    private String buildBookingHistoryText(List<RoomBookingEntity> roomBookings, List<ServiceBookingEntity> serviceBookings) {
        StringBuilder sb = new StringBuilder();
        sb.append("Booking History\n");
        sb.append("----------------\n");

        if (roomBookings == null || roomBookings.isEmpty()) {
            sb.append("Room bookings: none\n");
        } else {
            sb.append("Room bookings:\n");
            for (RoomBookingEntity b : roomBookings) {
                LocalDate start = LocalDate.ofEpochDay(b.startDateEpochDay);
                LocalDate end = LocalDate.ofEpochDay(b.endDateEpochDay);
                sb.append("- ").append(start.format(DISPLAY_FORMAT))
                        .append(" to ").append(end.format(DISPLAY_FORMAT))
                        .append(" (roomTypeId=").append(b.roomTypeId).append(")\n");
            }
        }

        if (serviceBookings == null || serviceBookings.isEmpty()) {
            sb.append("\nService bookings: none\n");
        } else {
            sb.append("\nService bookings:\n");
            for (ServiceBookingEntity sbk : serviceBookings) {
                LocalDate day = LocalDate.ofEpochDay(sbk.bookingDateEpochDay);
                sb.append("- ").append(day.format(DISPLAY_FORMAT))
                        .append(" (serviceId=").append(sbk.serviceId).append(")\n");
            }
        }

        return sb.toString();
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

