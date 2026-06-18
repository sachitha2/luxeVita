package com.example.ecostay.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ecostay.R;
import com.example.ecostay.data.repository.UserRepository;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.viewmodel.AuthViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private TextInputLayout tilIdentifier;
    private TextInputLayout tilPassword;
    private TextView tvMessage;
    private Button btnLogin;
    private ProgressBar progressLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (SessionManager.isLoggedIn(this)) {
            goToDashboard();
            return;
        }

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        tilIdentifier = findViewById(R.id.tilIdentifier);
        tilPassword = findViewById(R.id.tilPassword);
        TextInputEditText etIdentifier = findViewById(R.id.etIdentifier);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressLogin = findViewById(R.id.progressLogin);
        TextView tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvMessage = findViewById(R.id.tvMessage);

        btnLogin.setOnClickListener(v -> {
            clearErrors();
            setLoading(true);
            tvMessage.setText(R.string.progress_logging_in);
            authViewModel.login(
                    textOf(etIdentifier),
                    textOf(etPassword)
            );
        });

        tvRegisterLink.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        authViewModel.getLoginResult().observe(this, result -> {
            if (result == null) return;
            setLoading(false);
            if (result.success && result.user != null) {
                SessionManager.saveSession(this, result.user.userId, result.user.fullName);
                goToDashboard();
            } else {
                tvMessage.setText(mapError(result.error));
            }
        });
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        progressLogin.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void goToDashboard() {
        startActivity(new Intent(this, DashboardActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void clearErrors() {
        tilIdentifier.setError(null);
        tilPassword.setError(null);
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString() : "";
    }

    private int mapError(UserRepository.AuthError error) {
        if (error == null) return R.string.error_login_failed;
        switch (error) {
            case EMPTY_FIELDS:
                return R.string.error_empty_fields;
            case ACCOUNT_NOT_FOUND:
                return R.string.error_account_not_found;
            case INCORRECT_PASSWORD:
                return R.string.error_incorrect_password;
            default:
                return R.string.error_login_failed;
        }
    }
}
