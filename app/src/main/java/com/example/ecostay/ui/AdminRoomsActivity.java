package com.example.ecostay.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.MainActivity;
import com.example.ecostay.R;
import com.example.ecostay.data.EcoStayDatabase;
import com.example.ecostay.data.dao.RoomTypeDao;
import com.example.ecostay.data.entity.RoomTypeEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.adapters.AdminRoomTypeAdapter;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminRoomsActivity extends AppCompatActivity {

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    private RoomTypeDao roomTypeDao;
    private RecyclerView rvRooms;
    private TextView tvEmptyState;
    private AdminRoomTypeAdapter adapter;
    private List<RoomTypeEntity> roomTypes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SessionManager.isAdmin(this)) {
            redirectToLogin();
            return;
        }
        setContentView(R.layout.activity_admin_rooms);

        roomTypeDao = EcoStayDatabase.getInstance(this).roomTypeDao();
        rvRooms = findViewById(R.id.rvAdminRooms);
        tvEmptyState = findViewById(R.id.tvAdminRoomsEmptyState);
        Button btnAdd = findViewById(R.id.btnAdminAddRoom);

        adapter = new AdminRoomTypeAdapter(new AdminRoomTypeAdapter.OnRoomActionListener() {
            @Override
            public void onEdit(RoomTypeEntity roomType) {
                showUpsertDialog(roomType);
            }

            @Override
            public void onDelete(RoomTypeEntity roomType) {
                deleteRoom(roomType);
            }
        });

        rvRooms.setLayoutManager(new LinearLayoutManager(this));
        rvRooms.setAdapter(adapter);
        btnAdd.setOnClickListener(v -> showUpsertDialog(null));
        loadRooms();
    }

    private void loadRooms() {
        dbExecutor.execute(() -> {
            roomTypes = roomTypeDao.getAll();
            runOnUiThread(() -> {
                adapter.setItems(roomTypes);
                boolean hasItems = roomTypes != null && !roomTypes.isEmpty();
                rvRooms.setVisibility(hasItems ? View.VISIBLE : View.INVISIBLE);
                tvEmptyState.setVisibility(hasItems ? View.GONE : View.VISIBLE);
            });
        });
    }

    private void showUpsertDialog(RoomTypeEntity existing) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_room_form, null, false);
        TextInputEditText etName = dialogView.findViewById(R.id.etAdminRoomName);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etAdminRoomDescription);
        TextInputEditText etPrice = dialogView.findViewById(R.id.etAdminRoomPrice);
        TextInputEditText etTotalRooms = dialogView.findViewById(R.id.etAdminRoomInventory);
        TextInputEditText etImageRef = dialogView.findViewById(R.id.etAdminRoomImageRef);

        if (existing != null) {
            etName.setText(existing.name);
            etDescription.setText(existing.description);
            etPrice.setText(String.valueOf(existing.pricePerNight));
            etTotalRooms.setText(String.valueOf(existing.totalRooms));
            etImageRef.setText(existing.imageRef == null ? "" : existing.imageRef);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(existing == null ? R.string.admin_add_room : R.string.admin_edit_room)
                .setView(dialogView)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(existing == null ? R.string.admin_add_room : R.string.admin_save_room, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etName.getText() == null ? "" : etName.getText().toString().trim();
            String description = etDescription.getText() == null ? "" : etDescription.getText().toString().trim();
            String priceRaw = etPrice.getText() == null ? "" : etPrice.getText().toString().trim();
            String totalRoomsRaw = etTotalRooms.getText() == null ? "" : etTotalRooms.getText().toString().trim();
            String imageRef = etImageRef.getText() == null ? "" : etImageRef.getText().toString().trim();

            if (name.isEmpty() || description.isEmpty() || priceRaw.isEmpty() || totalRoomsRaw.isEmpty()) {
                Toast.makeText(this, R.string.admin_room_validation_error, Toast.LENGTH_SHORT).show();
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceRaw);
                if (price <= 0) {
                    Toast.makeText(this, R.string.admin_room_price_error, Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.admin_room_price_error, Toast.LENGTH_SHORT).show();
                return;
            }

            int totalRooms;
            try {
                totalRooms = Integer.parseInt(totalRoomsRaw);
                if (totalRooms <= 0) {
                    Toast.makeText(this, R.string.admin_room_inventory_error, Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.admin_room_inventory_error, Toast.LENGTH_SHORT).show();
                return;
            }

            RoomTypeEntity target = existing == null ? new RoomTypeEntity() : existing;
            target.name = name;
            target.description = description;
            target.pricePerNight = price;
            target.totalRooms = totalRooms;
            target.imageRef = imageRef.isEmpty() ? "room_default" : imageRef;

            dbExecutor.execute(() -> {
                if (existing == null) {
                    roomTypeDao.insert(target);
                } else {
                    roomTypeDao.update(target);
                }
                runOnUiThread(() -> {
                    dialog.dismiss();
                    loadRooms();
                });
            });
        }));

        dialog.show();
    }

    private void deleteRoom(RoomTypeEntity roomType) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.admin_delete_room_title)
                .setMessage(getString(R.string.admin_delete_room_message, roomType.name))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.admin_delete_room_confirm, (dialog, which) -> dbExecutor.execute(() -> {
                    roomTypeDao.delete(roomType);
                    runOnUiThread(this::loadRooms);
                }))
                .show();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
