package com.example.ecostay.ui.admin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.FaqEntity;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.admin.viewmodel.AdminFaqViewModel;
import com.example.ecostay.util.ToolbarUtils;
import com.example.ecostay.util.ValidationUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddEditFaqActivity extends AppCompatActivity {

    public static final String EXTRA_FAQ_ID = "faqId";

    private AdminFaqViewModel faqViewModel;
    private TextInputLayout tilQuestion;
    private TextInputLayout tilAnswer;
    private int faqId = -1;
    private boolean isEdit;

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

        if (isEdit) {
            MutableLiveData<FaqEntity> liveData = new MutableLiveData<>();
            liveData.observe(this, faq -> {
                if (faq == null) return;
                etQuestion.setText(faq.question);
                etAnswer.setText(faq.answer);
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

            FaqEntity faq = new FaqEntity();
            if (isEdit) {
                faq.faqId = faqId;
            }
            faq.question = etQuestion.getText().toString().trim();
            faq.answer = etAnswer.getText().toString().trim();
            faqViewModel.saveFaq(faq, isEdit);
        });
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
