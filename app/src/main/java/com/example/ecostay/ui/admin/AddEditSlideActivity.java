package com.example.ecostay.ui.admin;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.SlideshowSlideEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.admin.viewmodel.AdminSlideshowViewModel;
import com.example.ecostay.util.PhotoUtils;
import com.example.ecostay.util.ToolbarUtils;
import com.example.ecostay.util.ValidationUtils;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;

public class AddEditSlideActivity extends AppCompatActivity {

    public static final String EXTRA_SLIDE_ID = "slideId";

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedPhotoUri = uri;
                    removePhoto = false;
                    showPhotoPreview(uri);
                }
            });

    private AdminSlideshowViewModel slideshowViewModel;
    private TextInputLayout tilTitle;
    private TextInputLayout tilCaption;
    private View contentImageContainer;
    private View ivContentImageIcon;
    private View tvContentImageTitle;
    private View tvContentImageHint;
    private ImageView ivContentImagePreview;
    private TextView tvRemoveContentImage;
    private SwitchMaterial switchActive;
    private int slideId = -1;
    private boolean isEdit;
    private Uri selectedPhotoUri;
    private String existingPhotoPath;
    private boolean removePhoto;
    private int existingSortOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this) || !SessionManager.isAdmin(this)) {
            finish();
            return;
        }

        slideId = getIntent().getIntExtra(EXTRA_SLIDE_ID, -1);
        isEdit = slideId > 0;

        setContentView(R.layout.activity_add_edit_slide);
        ToolbarUtils.setupBackToolbar(this,
                isEdit ? R.string.edit_slide_title : R.string.add_slide_title);
        slideshowViewModel = new ViewModelProvider(this).get(AdminSlideshowViewModel.class);

        tilTitle = findViewById(R.id.tilTitle);
        tilCaption = findViewById(R.id.tilCaption);
        TextInputEditText etTitle = findViewById(R.id.etTitle);
        TextInputEditText etCaption = findViewById(R.id.etCaption);
        Button btnSave = findViewById(R.id.btnSave);
        contentImageContainer = findViewById(R.id.contentImageContainer);
        ivContentImageIcon = findViewById(R.id.ivContentImageIcon);
        tvContentImageTitle = findViewById(R.id.tvContentImageTitle);
        tvContentImageHint = findViewById(R.id.tvContentImageHint);
        ivContentImagePreview = findViewById(R.id.ivContentImagePreview);
        tvRemoveContentImage = findViewById(R.id.tvRemoveContentImage);
        switchActive = findViewById(R.id.switchActive);

        tvContentImageTitle.setVisibility(View.VISIBLE);
        ((TextView) tvContentImageTitle).setText(R.string.label_slide_image);

        setupPhotoPicker();

        if (isEdit) {
            MutableLiveData<SlideshowSlideEntity> liveData = new MutableLiveData<>();
            liveData.observe(this, slide -> {
                if (slide == null) return;
                etTitle.setText(slide.title);
                etCaption.setText(slide.caption);
                switchActive.setChecked(slide.isActive);
                existingSortOrder = slide.sortOrder;
                existingPhotoPath = slide.imagePath;
                if (existingPhotoPath != null && !existingPhotoPath.isEmpty()) {
                    showSavedPhoto(existingPhotoPath);
                }
            });
            slideshowViewModel.loadSlideById(slideId, liveData);
        } else {
            switchActive.setChecked(true);
        }

        slideshowViewModel.getSaveResult().observe(this, result -> {
            if (result != null && result.success) {
                Toast.makeText(this, R.string.slide_saved, Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnSave.setOnClickListener(v -> {
            if (!validate(etTitle)) return;
            saveSlide(etTitle, etCaption);
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
        ((TextView) tvContentImageTitle).setText(R.string.label_slide_image);
    }

    private void resetPhotoPlaceholder() {
        ivContentImagePreview.setVisibility(View.GONE);
        ivContentImagePreview.setImageDrawable(null);
        tvRemoveContentImage.setVisibility(View.GONE);
        ivContentImageIcon.setVisibility(View.VISIBLE);
        tvContentImageTitle.setVisibility(View.VISIBLE);
        tvContentImageHint.setVisibility(View.VISIBLE);
    }

    private void saveSlide(TextInputEditText etTitle, TextInputEditText etCaption) {
        String imagePath = existingPhotoPath;
        if (selectedPhotoUri != null) {
            try {
                imagePath = PhotoUtils.saveSlideImageFromUri(this, slideId, selectedPhotoUri);
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

        if (imagePath == null || imagePath.isEmpty()) {
            Toast.makeText(this, R.string.error_slide_image_required, Toast.LENGTH_SHORT).show();
            return;
        }

        SlideshowSlideEntity slide = new SlideshowSlideEntity();
        if (isEdit) {
            slide.slideId = slideId;
        }
        slide.title = etTitle.getText().toString().trim();
        slide.caption = etCaption.getText() != null ? etCaption.getText().toString().trim() : "";
        slide.imagePath = imagePath;
        slide.isActive = switchActive.isChecked();
        slide.sortOrder = isEdit ? existingSortOrder : 0;
        slideshowViewModel.saveSlide(slide, isEdit);
    }

    private boolean validate(TextInputEditText etTitle) {
        tilTitle.setError(null);

        String title = etTitle.getText() != null ? etTitle.getText().toString() : "";
        if (ValidationUtils.isEmpty(title)) {
            tilTitle.setError(getString(R.string.error_empty_fields));
            return false;
        }
        return true;
    }
}
