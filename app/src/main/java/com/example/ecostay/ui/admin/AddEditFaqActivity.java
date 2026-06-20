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
import com.example.ecostay.data.entity.FaqEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.admin.viewmodel.AdminFaqViewModel;
import com.example.ecostay.util.PhotoUtils;
import com.example.ecostay.util.ToolbarUtils;
import com.example.ecostay.util.ValidationUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;

public class AddEditFaqActivity extends AppCompatActivity {

    public static final String EXTRA_FAQ_ID = "faqId";

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedPhotoUri = uri;
                    removePhoto = false;
                    showPhotoPreview(uri);
                }
            });

    private AdminFaqViewModel faqViewModel;
    private TextInputLayout tilQuestion;
    private TextInputLayout tilAnswer;
    private View contentImageContainer;
    private View ivContentImageIcon;
    private View tvContentImageTitle;
    private View tvContentImageHint;
    private ImageView ivContentImagePreview;
    private TextView tvRemoveContentImage;
    private int faqId = -1;
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

        faqId = getIntent().getIntExtra(EXTRA_FAQ_ID, -1);
        isEdit = faqId > 0;

        setContentView(R.layout.activity_add_edit_faq);
        ToolbarUtils.setupBackToolbar(this,
                isEdit ? R.string.edit_faq_title : R.string.add_faq_title);
        faqViewModel = new ViewModelProvider(this).get(AdminFaqViewModel.class);

        tilQuestion = findViewById(R.id.tilQuestion);
        tilAnswer = findViewById(R.id.tilAnswer);
        TextInputEditText etQuestion = findViewById(R.id.etQuestion);
        TextInputEditText etAnswer = findViewById(R.id.etAnswer);
        Button btnSave = findViewById(R.id.btnSave);
        contentImageContainer = findViewById(R.id.contentImageContainer);
        ivContentImageIcon = findViewById(R.id.ivContentImageIcon);
        tvContentImageTitle = findViewById(R.id.tvContentImageTitle);
        tvContentImageHint = findViewById(R.id.tvContentImageHint);
        ivContentImagePreview = findViewById(R.id.ivContentImagePreview);
        tvRemoveContentImage = findViewById(R.id.tvRemoveContentImage);

        setupPhotoPicker();

        if (isEdit) {
            MutableLiveData<FaqEntity> liveData = new MutableLiveData<>();
            liveData.observe(this, faq -> {
                if (faq == null) return;
                etQuestion.setText(faq.question);
                etAnswer.setText(faq.answer);
                existingPhotoPath = faq.imagePath;
                if (existingPhotoPath != null && !existingPhotoPath.isEmpty()) {
                    showSavedPhoto(existingPhotoPath);
                }
            });
            faqViewModel.loadFaqById(faqId, liveData);
        }

        faqViewModel.getSaveResult().observe(this, result -> {
            if (result != null && result.success) {
                Toast.makeText(this, R.string.faq_saved, Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnSave.setOnClickListener(v -> {
            if (!validate(etQuestion, etAnswer)) return;
            saveFaq(etQuestion, etAnswer);
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

    private void saveFaq(TextInputEditText etQuestion, TextInputEditText etAnswer) {
        String imagePath = existingPhotoPath;
        if (selectedPhotoUri != null) {
            try {
                imagePath = PhotoUtils.saveFaqImageFromUri(this, faqId, selectedPhotoUri);
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

        FaqEntity faq = new FaqEntity();
        if (isEdit) {
            faq.faqId = faqId;
        }
        faq.question = etQuestion.getText().toString().trim();
        faq.answer = etAnswer.getText().toString().trim();
        faq.imagePath = imagePath;
        faqViewModel.saveFaq(faq, isEdit);
    }

    private boolean validate(TextInputEditText etQuestion, TextInputEditText etAnswer) {
        tilQuestion.setError(null);
        tilAnswer.setError(null);

        String question = etQuestion.getText() != null ? etQuestion.getText().toString() : "";
        String answer = etAnswer.getText() != null ? etAnswer.getText().toString() : "";

        if (ValidationUtils.isEmpty(question)) {
            tilQuestion.setError(getString(R.string.error_empty_fields));
            return false;
        }
        if (ValidationUtils.isEmpty(answer)) {
            tilAnswer.setError(getString(R.string.error_empty_fields));
            return false;
        }
        return true;
    }
}
