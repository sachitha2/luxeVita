package com.example.ecostay.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ecostay.R;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.util.ToolbarUtils;
import com.example.ecostay.util.ValidationUtils;
import com.google.android.material.textfield.TextInputEditText;

public class SupportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this)) {
            finish();
            return;
        }

        setContentView(R.layout.activity_support);
        ToolbarUtils.setupBackToolbar(this, R.string.support_title);

        TextView tvContactInfo = findViewById(R.id.tvContactInfo);
        tvContactInfo.setText(getString(R.string.support_phone) + "\n"
                + getString(R.string.support_email) + "\n"
                + getString(R.string.support_address));

        TextInputEditText etName = findViewById(R.id.etName);
        TextInputEditText etMessage = findViewById(R.id.etMessage);
        Button btnSend = findViewById(R.id.btnSend);

        btnSend.setOnClickListener(v -> {
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

            Toast.makeText(this, R.string.support_success, Toast.LENGTH_LONG).show();
            etName.setText("");
            etMessage.setText("");
        });
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString() : "";
    }
}
