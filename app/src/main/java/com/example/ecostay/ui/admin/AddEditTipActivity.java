package com.example.ecostay.ui.admin;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.MaintenanceTipEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.admin.viewmodel.AdminTipViewModel;
import com.example.ecostay.util.PhotoUtils;
import com.example.ecostay.util.ToolbarUtils;
import com.example.ecostay.util.ValidationUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;

public class AddEditTipActivity extends AppCompatActivity {

    public static final String EXTRA_TIP_ID = "tipId";

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedPhotoUri = uri;
                    removePhoto = false;
                    showPhotoPreview(uri);
                }
            });

    private AdminTipViewModel tipViewModel;
    private TextInputLayout tilTitle;
    private TextInputLayout tilDescription;
    private Spinner spDeviceType;
    private View contentImageContainer;
    private View ivContentImageIcon;
    private View tvContentImageTitle;
    private View tvContentImageHint;
    private ImageView ivContentImagePreview;
    private TextView tvRemoveContentImage;
    private int tipId = -1;
    private boolean isEdit;
    private Uri selectedPhotoUri;
    private String existingPhotoPath;
    private boolean removePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this) || !SessionManager.isAdmin(this)) {
            finish();
            return;
        }

        tipId = getIntent().getIntExtra(EXTRA_TIP_ID, -1);
        isEdit = tipId > 0;

        setContentView(R.layout.activity_add_edit_tip);
        ToolbarUtils.setupBackToolbar(this,
                isEdit ? R.string.edit_tip_title : R.string.add_tip_title);
        tipViewModel = new ViewModelProvider(this).get(AdminTipViewModel.class);

        spDeviceType = findViewById(R.id.spDeviceType);
        tilTitle = findViewById(R.id.tilTitle);
        tilDescription = findViewById(R.id.tilDescription);
        TextInputEditText etTitle = findViewById(R.id.etTitle);
        TextInputEditText etDescription = findViewById(R.id.etDescription);
        Button btnSave = findViewById(R.id.btnSave);
        contentImageContainer = findViewById(R.id.contentImageContainer);
        ivContentImageIcon = findViewById(R.id.ivContentImageIcon);
        tvContentImageTitle = findViewById(R.id.tvContentImageTitle);
        tvContentImageHint = findViewById(R.id.tvContentImageHint);
        ivContentImagePreview = findViewById(R.id.ivContentImagePreview);
        tvRemoveContentImage = findViewById(R.id.tvRemoveContentImage);

        ArrayAdapter<CharSequence> deviceAdapter = ArrayAdapter.createFromResource(
                this, R.array.device_types, R.layout.item_spinner_category);
        deviceAdapter.setDropDownViewResource(R.layout.item_spinner_category_dropdown);
        spDeviceType.setAdapter(deviceAdapter);

        setupPhotoPicker();

        if (isEdit) {
            MutableLiveData<MaintenanceTipEntity> liveData = new MutableLiveData<>();
            liveData.observe(this, tip -> {
                if (tip == null) return;
                etTitle.setText(tip.title);
                etDescription.setText(tip.description);
                setSpinnerSelection(spDeviceType, tip.deviceType);
                existingPhotoPath = tip.imagePath;
                if (existingPhotoPath != null && !existingPhotoPath.isEmpty()) {
                    showSavedPhoto(existingPhotoPath);
                }
            });
            tipViewModel.loadTipById(tipId, liveData);
        }

        tipViewModel.getSaveResult().observe(this, result -> {
            if (result != null && result.success) {
                Toast.makeText(this, R.string.tip_saved, Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnSave.setOnClickListener(v -> {
            if (!validate(etTitle, etDescription)) return;
            saveTip(etTitle, etDescription);
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

    private void saveTip(TextInputEditText etTitle, TextInputEditText etDescription) {
        String imagePath = existingPhotoPath;
        if (selectedPhotoUri != null) {
            try {
                imagePath = PhotoUtils.saveTipImageFromUri(this, tipId, selectedPhotoUri);
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

        MaintenanceTipEntity tip = new MaintenanceTipEntity();
        if (isEdit) {
            tip.tipId = tipId;
        }
        tip.deviceType = spDeviceType.getSelectedItem().toString();
        tip.title = etTitle.getText().toString().trim();
        tip.description = etDescription.getText().toString().trim();
        tip.imagePath = imagePath;
        tipViewModel.saveTip(tip, isEdit);
    }

    private boolean validate(TextInputEditText etTitle, TextInputEditText etDescription) {
        tilTitle.setError(null);
        tilDescription.setError(null);

        String title = etTitle.getText() != null ? etTitle.getText().toString() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString() : "";

        if (ValidationUtils.isEmpty(title)) {
            tilTitle.setError(getString(R.string.error_empty_fields));
            return false;
        }
        if (ValidationUtils.isEmpty(description)) {
            tilDescription.setError(getString(R.string.error_empty_fields));
            return false;
        }
        return true;
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter == null) return;
        for (int i = 0; i < adapter.getCount(); i++) {
            if (value.equals(adapter.getItem(i).toString())) {
                spinner.setSelection(i);
                return;
            }
        }
    }
}
