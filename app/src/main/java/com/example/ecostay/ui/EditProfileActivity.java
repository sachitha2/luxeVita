package com.example.ecostay.ui;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.UserEntity;
import com.example.ecostay.data.repository.UserRepository;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.viewmodel.ProfileViewModel;
import com.example.ecostay.util.PhotoUtils;
import com.example.ecostay.util.ToolbarUtils;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;

public class EditProfileActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedPhotoUri = uri;
                    removePhoto = false;
                    showPhotoPreview(uri);
                }
            });

    private ProfileViewModel profileViewModel;
    private TextInputEditText etFullName;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;
    private TextInputEditText etAddress;
    private ImageView ivProfilePhoto;
    private TextView tvRemovePhoto;
    private TextView tvMessage;
    private Button btnSaveProfile;
    private ProgressBar progressSave;

    private int userId;
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
        if (userId <= 0) {
            finish();
            return;
        }

        setContentView(R.layout.activity_edit_profile);
        ToolbarUtils.setupBackToolbar(this, R.string.profile_title);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        tvRemovePhoto = findViewById(R.id.tvRemovePhoto);
        tvMessage = findViewById(R.id.tvMessage);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        progressSave = findViewById(R.id.progressSave);

        findViewById(R.id.profilePhotoContainer).setOnClickListener(v ->
                pickImageLauncher.launch("image/*"));
        tvRemovePhoto.setOnClickListener(v -> clearPhotoSelection());
        btnSaveProfile.setOnClickListener(v -> saveProfile());

        profileViewModel.getProfileUser().observe(this, this::bindUser);
        profileViewModel.getProfileUpdateResult().observe(this, result -> {
            if (result == null) {
                return;
            }
            setSaving(false);
            if (result.success) {
                SessionManager.updateUserName(this, result.user.fullName);
                Toast.makeText(this, R.string.profile_saved, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                tvMessage.setText(mapError(result.error));
            }
            profileViewModel.clearProfileUpdateResult();
        });

        profileViewModel.loadProfile(userId);
    }

    private void bindUser(UserEntity user) {
        if (user == null) {
            tvMessage.setText(R.string.error_profile_load);
            btnSaveProfile.setEnabled(false);
            return;
        }
        etFullName.setText(user.fullName);
        etEmail.setText(user.email);
        etPhone.setText(user.phone);
        etAddress.setText(user.address);
        existingPhotoPath = user.profileImagePath;
        if (existingPhotoPath != null && !existingPhotoPath.isEmpty()) {
            showSavedPhoto(existingPhotoPath);
        }
    }

    private void showSavedPhoto(String path) {
        File file = new File(path);
        if (!file.exists()) {
            resetPhotoPlaceholder();
            return;
        }
        ivProfilePhoto.setPadding(0, 0, 0, 0);
        ivProfilePhoto.setImageBitmap(BitmapFactory.decodeFile(path));
        tvRemovePhoto.setVisibility(View.VISIBLE);
    }

    private void showPhotoPreview(Uri uri) {
        ivProfilePhoto.setPadding(0, 0, 0, 0);
        ivProfilePhoto.setImageURI(uri);
        tvRemovePhoto.setVisibility(View.VISIBLE);
    }

    private void clearPhotoSelection() {
        selectedPhotoUri = null;
        removePhoto = true;
        resetPhotoPlaceholder();
        tvRemovePhoto.setVisibility(View.GONE);
    }

    private void resetPhotoPlaceholder() {
        ivProfilePhoto.setPadding(24, 24, 24, 24);
        ivProfilePhoto.setImageResource(R.drawable.ic_nav_profile);
    }

    private void saveProfile() {
        tvMessage.setText("");
        String profileImagePath = null;
        if (selectedPhotoUri != null) {
            try {
                profileImagePath = PhotoUtils.saveProfilePhotoFromUri(this, userId, selectedPhotoUri);
                if (existingPhotoPath != null
                        && !existingPhotoPath.equals(profileImagePath)) {
                    PhotoUtils.deletePhotoFile(existingPhotoPath);
                }
            } catch (IOException e) {
                tvMessage.setText(R.string.error_photo_save);
                return;
            }
        }
        if (removePhoto && existingPhotoPath != null && !existingPhotoPath.isEmpty()) {
            PhotoUtils.deletePhotoFile(existingPhotoPath);
        }
        setSaving(true);
        profileViewModel.updateProfile(
                userId,
                textOf(etFullName),
                textOf(etEmail),
                textOf(etPhone),
                textOf(etAddress),
                profileImagePath,
                removePhoto
        );
    }

    private void setSaving(boolean saving) {
        btnSaveProfile.setEnabled(!saving);
        progressSave.setVisibility(saving ? View.VISIBLE : View.GONE);
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString() : "";
    }

    private int mapError(UserRepository.ProfileError error) {
        if (error == null) {
            return R.string.error_profile_save;
        }
        switch (error) {
            case EMPTY_FIELDS:
                return R.string.error_empty_fields;
            case INVALID_EMAIL:
                return R.string.error_valid_email;
            case INVALID_PHONE:
                return R.string.error_valid_phone;
            case EMAIL_EXISTS:
                return R.string.error_email_registered;
            case PHONE_EXISTS:
                return R.string.error_phone_registered;
            case USER_NOT_FOUND:
                return R.string.error_session_expired;
            default:
                return R.string.error_profile_save;
        }
    }
}
