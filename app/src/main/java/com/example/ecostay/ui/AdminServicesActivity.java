package com.example.ecostay.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.MainActivity;
import com.example.ecostay.R;
import com.example.ecostay.data.EcoStayDatabase;
import com.example.ecostay.data.dao.ServiceDao;
import com.example.ecostay.data.entity.ServiceEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.adapters.AdminServiceAdapter;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminServicesActivity extends AppCompatActivity {

    private static final String[] CATEGORIES = new String[]{"SPA", "DINING", "CABANAS", "TOURS"};
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    private ServiceDao serviceDao;
    private RecyclerView rvServices;
    private TextView tvEmptyState;
    private AdminServiceAdapter adapter;
    private List<ServiceEntity> services = new ArrayList<>();
    @Nullable
    private Uri pendingImageUri;
    @Nullable
    private ImageView activePreviewImageView;
    private ActivityResultLauncher<String[]> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SessionManager.isAdmin(this)) {
            redirectToLogin();
            return;
        }
        setContentView(R.layout.activity_admin_services);

        serviceDao = EcoStayDatabase.getInstance(this).serviceDao();
        rvServices = findViewById(R.id.rvAdminServices);
        tvEmptyState = findViewById(R.id.tvAdminServicesEmptyState);
        Button btnAdd = findViewById(R.id.btnAdminAddService);

        adapter = new AdminServiceAdapter(new AdminServiceAdapter.OnServiceActionListener() {
            @Override
            public void onEdit(ServiceEntity service) {
                showUpsertDialog(service);
            }

            @Override
            public void onDelete(ServiceEntity service) {
                deleteService(service);
            }
        });
        rvServices.setLayoutManager(new LinearLayoutManager(this));
        rvServices.setAdapter(adapter);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        pendingImageUri = uri;
                        try {
                            getContentResolver().takePersistableUriPermission(
                                    uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                        } catch (SecurityException ignored) {
                            // URI may already be persisted by the system/provider.
                        }
                        if (activePreviewImageView != null) {
                            activePreviewImageView.setImageURI(uri);
                        }
                    }
                }
        );

        btnAdd.setOnClickListener(v -> showUpsertDialog(null));
        loadServices();
    }

    private void loadServices() {
        dbExecutor.execute(() -> {
            services = serviceDao.getAll();
            runOnUiThread(() -> {
                adapter.setItems(services);
                boolean hasItems = services != null && !services.isEmpty();
                rvServices.setVisibility(hasItems ? View.VISIBLE : View.INVISIBLE);
                tvEmptyState.setVisibility(hasItems ? View.GONE : View.VISIBLE);
            });
        });
    }

    private void showUpsertDialog(ServiceEntity existing) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_service_form, null, false);
        TextInputEditText etName = dialogView.findViewById(R.id.etAdminServiceName);
        Spinner spCategory = dialogView.findViewById(R.id.spAdminServiceCategory);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etAdminServiceDescription);
        TextInputEditText etPrice = dialogView.findViewById(R.id.etAdminServicePrice);
        ImageView ivPreview = dialogView.findViewById(R.id.ivAdminServicePreview);
        Button btnSelectImage = dialogView.findViewById(R.id.btnAdminSelectServiceImage);
        pendingImageUri = null;
        activePreviewImageView = ivPreview;

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, CATEGORIES);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);

        if (existing != null) {
            etName.setText(existing.name);
            etDescription.setText(existing.description);
            etPrice.setText(String.valueOf(existing.price));
            for (int i = 0; i < CATEGORIES.length; i++) {
                if (CATEGORIES[i].equalsIgnoreCase(existing.category)) {
                    spCategory.setSelection(i);
                    break;
                }
            }

            if (existing.imageRef != null && !existing.imageRef.trim().isEmpty()) {
                try {
                    Uri uri = Uri.parse(existing.imageRef);
                    if ("content".equals(uri.getScheme()) || "file".equals(uri.getScheme())) {
                        ivPreview.setImageURI(uri);
                        pendingImageUri = uri;
                    } else {
                        int fallbackRes = R.drawable.service_default;
                        int imageRes = dialogView.getContext().getResources()
                                .getIdentifier(existing.imageRef, "drawable", dialogView.getContext().getPackageName());
                        ivPreview.setImageResource(imageRes != 0 ? imageRes : fallbackRes);
                    }
                } catch (Exception e) {
                    ivPreview.setImageResource(R.drawable.service_default);
                }
            } else {
                ivPreview.setImageResource(R.drawable.service_default);
            }
        } else {
            ivPreview.setImageResource(R.drawable.service_default);
        }

        btnSelectImage.setOnClickListener(v -> imagePickerLauncher.launch(new String[]{"image/*"}));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(existing == null ? R.string.admin_add_service : R.string.admin_edit_service)
                .setView(dialogView)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(existing == null ? R.string.admin_add_service : R.string.admin_save_service, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etName.getText() == null ? "" : etName.getText().toString().trim();
            String description = etDescription.getText() == null ? "" : etDescription.getText().toString().trim();
            String priceRaw = etPrice.getText() == null ? "" : etPrice.getText().toString().trim();
            String category = String.valueOf(spCategory.getSelectedItem());

            if (name.isEmpty() || description.isEmpty() || priceRaw.isEmpty()) {
                Toast.makeText(this, R.string.admin_service_validation_error, Toast.LENGTH_SHORT).show();
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceRaw);
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.admin_service_price_error, Toast.LENGTH_SHORT).show();
                return;
            }

            ServiceEntity target = existing == null ? new ServiceEntity() : existing;
            target.name = name;
            target.category = category;
            target.description = description;
            target.price = price;
            if (pendingImageUri != null) {
                target.imageRef = pendingImageUri.toString();
            } else if (target.imageRef == null || target.imageRef.trim().isEmpty()) {
                target.imageRef = "service_default";
            }

            dbExecutor.execute(() -> {
                if (existing == null) {
                    serviceDao.insert(target);
                } else {
                    serviceDao.update(target);
                }
                runOnUiThread(() -> {
                    dialog.dismiss();
                    activePreviewImageView = null;
                    loadServices();
                });
            });
        }));

        dialog.setOnDismissListener(d -> activePreviewImageView = null);
        dialog.show();
    }

    private void deleteService(ServiceEntity service) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.admin_delete_service_title)
                .setMessage(getString(R.string.admin_delete_service_message, service.name))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.admin_delete_service_confirm, (dialog, which) -> dbExecutor.execute(() -> {
                    serviceDao.delete(service);
                    runOnUiThread(this::loadServices);
                }))
                .show();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
