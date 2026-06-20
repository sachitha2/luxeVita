package com.example.ecostay.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecostay.R;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.viewmodel.SupportViewModel;
import com.example.ecostay.util.ToolbarUtils;
import com.example.ecostay.util.ValidationUtils;
import com.google.android.material.textfield.TextInputEditText;

public class SupportActivity extends AppCompatActivity {

    private SupportViewModel supportViewModel;
    private TextInputEditText etName;
    private TextInputEditText etMessage;
    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this)) {
            finish();
            return;
        }

        setContentView(R.layout.activity_support);
        ToolbarUtils.setupBackToolbar(this, R.string.support_title);
        supportViewModel = new ViewModelProvider(this).get(SupportViewModel.class);

        TextView tvContactInfo = findViewById(R.id.tvContactInfo);
        tvContactInfo.setText(getString(R.string.support_phone) + "\n"
                + getString(R.string.support_email) + "\n"
                + getString(R.string.support_address));

        etName = findViewById(R.id.etName);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        btnSend.setOnClickListener(v -> submitMessage());

        supportViewModel.getSubmitResult().observe(this, result -> {
            if (result == null) {
                return;
            }
            setSubmitting(false);
            if (result.success) {
                Toast.makeText(this, R.string.support_success, Toast.LENGTH_LONG).show();
                etName.setText("");
                etMessage.setText("");
            } else if ("Invalid session".equals(result.message)) {
                SessionManager.clearSession(this);
                Toast.makeText(this, R.string.error_session_expired, Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, R.string.support_submit_error, Toast.LENGTH_SHORT).show();
            }
            supportViewModel.clearSubmitResult();
        });
    }

    private void submitMessage() {
        String name = textOf(etName);
        String message = textOf(etMessage);

        if (ValidationUtils.isEmpty(name)) {
            Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!ValidationUtils.isValidIssueDescription(message)) {
            Toast.makeText(this, R.string.error_message_short, Toast.LENGTH_SHORT).show();
            return;
        }

        setSubmitting(true);
        supportViewModel.submitMessage(SessionManager.getUserId(this), name, message);
    }

    private void setSubmitting(boolean submitting) {
        btnSend.setEnabled(!submitting);
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString() : "";
    }
}
