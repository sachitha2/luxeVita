package com.example.ecostay.ui;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.DeviceEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.viewmodel.DeviceViewModel;
import com.example.ecostay.util.PhotoUtils;
import com.example.ecostay.util.ToolbarUtils;
import com.example.ecostay.util.ValidationUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;

public class AddDeviceActivity extends AppCompatActivity {

    public static final String EXTRA_DEVICE_ID = "deviceId";

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedPhotoUri = uri;
                    removePhoto = false;
                    showPhotoPreview(uri);
                }
            });

    private DeviceViewModel deviceViewModel;
    private int userId;
    private int deviceId = -1;
    private boolean isEdit;
    private Button btnSave;
    private ProgressBar progressSave;
    private TextInputLayout tilBrand;
    private TextInputLayout tilModel;
    private View contentImageContainer;
    private View ivContentImageIcon;
    private View tvContentImageTitle;
    private View tvContentImageHint;
    private ImageView ivContentImagePreview;
    private TextView tvRemoveContentImage;
    private Uri selectedPhotoUri;
    private String existingPhotoPath;
    private boolean removePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this)) {
            finish();
            return;
        }

        userId = SessionManager.getUserId(this);
        deviceId = getIntent().getIntExtra(EXTRA_DEVICE_ID, -1);
        isEdit = deviceId > 0;

        setContentView(R.layout.activity_add_device);
        ToolbarUtils.setupBackToolbar(this, isEdit ? R.string.edit_device_title : R.string.add_device_title);
        deviceViewModel = new ViewModelProvider(this).get(DeviceViewModel.class);

        TextView tvSubtitle = findViewById(R.id.tvSubtitle);
        tvSubtitle.setText(isEdit ? R.string.edit_device_subtitle : R.string.add_device_subtitle);

        Spinner spDeviceType = findViewById(R.id.spDeviceType);
        TextInputEditText etBrand = findViewById(R.id.etBrand);
        TextInputEditText etModel = findViewById(R.id.etModel);
        btnSave = findViewById(R.id.btnSave);
        progressSave = findViewById(R.id.progressSave);
        tilBrand = findViewById(R.id.tilBrand);
        tilModel = findViewById(R.id.tilModel);
        contentImageContainer = findViewById(R.id.contentImageContainer);
        ivContentImageIcon = findViewById(R.id.ivContentImageIcon);
        tvContentImageTitle = findViewById(R.id.tvContentImageTitle);
        tvContentImageHint = findViewById(R.id.tvContentImageHint);
        ivContentImagePreview = findViewById(R.id.ivContentImagePreview);
        tvRemoveContentImage = findViewById(R.id.tvRemoveContentImage);

        ((TextView) tvContentImageTitle).setText(R.string.label_device_image);
        ((TextView) tvContentImageHint).setText(R.string.device_image_attach_hint);
        setupPhotoPicker();

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                this, R.array.device_types, R.layout.item_spinner_category);
        typeAdapter.setDropDownViewResource(R.layout.item_spinner_category_dropdown);
        spDeviceType.setAdapter(typeAdapter);

        if (isEdit) {
            MutableLiveData<DeviceEntity> liveData = new MutableLiveData<>();
            liveData.observe(this, device -> {
                if (device == null) return;
                setSpinnerSelection(spDeviceType, device.deviceType);
                etBrand.setText(device.brand);
                etModel.setText(device.model);
                existingPhotoPath = device.imagePath;
                if (existingPhotoPath != null && !existingPhotoPath.isEmpty()) {
                    showSavedPhoto(existingPhotoPath);
                }
            });
            deviceViewModel.loadDevice(deviceId, liveData);
        }

        btnSave.setOnClickListener(v -> saveDevice(spDeviceType, etBrand, etModel));

        deviceViewModel.clearSaveResult();
        deviceViewModel.getSaveResult().observe(this, result -> {
            if (result == null) return;
            setSaving(false);
            if (result.success) {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupPhotoPicker() {
        contentImageContainer.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        tvRemoveContentImage.setOnClickListener(v -> clearPhotoSelection());
    }

    private void showSavedPhoto(String path) {
        File file = new File(path);
        if (!file.exists()) {
            resetPhotoPlaceholder();
            return;
        }
        ivContentImagePreview.setImageURI(Uri.fromFile(file));
        showPhotoSelectedState();
    }

    private void showPhotoPreview(Uri uri) {
        ivContentImagePreview.setImageURI(uri);
        showPhotoSelectedState();
    }

    private void showPhotoSelectedState() {
        ivContentImagePreview.setVisibility(View.VISIBLE);
        tvRemoveContentImage.setVisibility(View.VISIBLE);
        ivContentImageIcon.setVisibility(View.GONE);
        tvContentImageTitle.setVisibility(View.GONE);
        tvContentImageHint.setVisibility(View.GONE);
    }

    private void clearPhotoSelection() {
        selectedPhotoUri = null;
        removePhoto = true;
        resetPhotoPlaceholder();
    }

    private void resetPhotoPlaceholder() {
        ivContentImagePreview.setVisibility(View.GONE);
        ivContentImagePreview.setImageDrawable(null);
        tvRemoveContentImage.setVisibility(View.GONE);
        ivContentImageIcon.setVisibility(View.VISIBLE);
        tvContentImageTitle.setVisibility(View.VISIBLE);
        tvContentImageHint.setVisibility(View.VISIBLE);
    }

    private void saveDevice(Spinner spDeviceType, TextInputEditText etBrand, TextInputEditText etModel) {
        clearFieldErrors();
        String brand = textOf(etBrand);
        String model = textOf(etModel);
        String deviceType = spDeviceType.getSelectedItem().toString();

        if (ValidationUtils.isEmpty(brand)) {
            tilBrand.setError(getString(R.string.error_empty_fields));
            return;
        }
        if (ValidationUtils.isEmpty(model)) {
            tilModel.setError(getString(R.string.error_empty_fields));
            return;
        }

        String imagePath = existingPhotoPath;
        if (selectedPhotoUri != null) {
            try {
                imagePath = PhotoUtils.saveDeviceImageFromUri(this, deviceId, selectedPhotoUri);
                if (existingPhotoPath != null && !existingPhotoPath.equals(imagePath)) {
                    PhotoUtils.deletePhotoFile(existingPhotoPath);
                }
            } catch (IOException e) {
                Toast.makeText(this, R.string.error_photo_save, Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (removePhoto && existingPhotoPath != null && !existingPhotoPath.isEmpty()) {
            PhotoUtils.deletePhotoFile(existingPhotoPath);
            imagePath = null;
        }

        setSaving(true);

        DeviceEntity device = new DeviceEntity();
        device.userId = userId;
        device.deviceType = deviceType;
        device.brand = brand.trim();
        device.model = model.trim();
        device.imagePath = imagePath;
        if (isEdit) {
            device.deviceId = deviceId;
        }
        deviceViewModel.saveDevice(device, isEdit);
    }

    private void setSaving(boolean saving) {
        btnSave.setEnabled(!saving);
        progressSave.setVisibility(saving ? View.VISIBLE : View.GONE);
    }

    private void clearFieldErrors() {
        tilBrand.setError(null);
        tilModel.setError(null);
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString() : "";
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (value.equals(adapter.getItem(i).toString())) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}
