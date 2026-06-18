package com.example.ecostay.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecostay.R;
import com.example.ecostay.data.repository.UserRepository;
import com.example.ecostay.ui.viewmodel.AuthViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private TextView tvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        TextInputEditText etFullName = findViewById(R.id.etFullName);
        TextInputEditText etEmail = findViewById(R.id.etEmail);
        TextInputEditText etPhone = findViewById(R.id.etPhone);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        TextInputEditText etAddress = findViewById(R.id.etAddress);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvLoginLink = findViewById(R.id.tvLoginLink);
        tvMessage = findViewById(R.id.tvMessage);

        btnRegister.setOnClickListener(v -> {
            tvMessage.setText(R.string.progress_registering);
            authViewModel.register(
                    textOf(etFullName),
                    textOf(etEmail),
                    textOf(etPhone),
                    textOf(etPassword),
                    textOf(etAddress)
            );
        });

        tvLoginLink.setOnClickListener(v -> finish());

        authViewModel.getRegisterResult().observe(this, result -> {
            if (result == null) return;
            if (result.success) {
                Toast.makeText(this, R.string.action_create_account, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                tvMessage.setText(mapError(result.error));
            }
        });
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString() : "";
    }

    private int mapError(UserRepository.AuthError error) {
        if (error == null) return R.string.error_register_failed;
        switch (error) {
            case EMPTY_FIELDS:
                return R.string.error_empty_fields;
            case INVALID_EMAIL:
                return R.string.error_valid_email;
            case INVALID_PHONE:
                return R.string.error_valid_phone;
            case INVALID_PASSWORD:
                return R.string.error_valid_password;
            case EMAIL_EXISTS:
                return R.string.error_email_registered;
            case PHONE_EXISTS:
                return R.string.error_phone_registered;
            default:
                return R.string.error_register_failed;
        }
    }
}
