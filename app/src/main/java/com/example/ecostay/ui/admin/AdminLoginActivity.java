package com.example.ecostay.ui.admin;

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
import com.example.ecostay.ui.LoginActivity;
import com.example.ecostay.ui.admin.viewmodel.AdminAuthViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AdminLoginActivity extends AppCompatActivity {

    private AdminAuthViewModel authViewModel;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextView tvMessage;
    private Button btnLogin;
    private ProgressBar progressLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        if (SessionManager.isLoggedIn(this) && SessionManager.isAdmin(this)) {
            goToDashboard();
            return;
        }

        authViewModel = new ViewModelProvider(this).get(AdminAuthViewModel.class);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        TextInputEditText etEmail = findViewById(R.id.etEmail);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressLogin = findViewById(R.id.progressLogin);
        tvMessage = findViewById(R.id.tvMessage);

        findViewById(R.id.tvCustomerLink).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btnLogin.setOnClickListener(v -> {
            clearErrors();
            setLoading(true);
            tvMessage.setText(R.string.progress_logging_in);
            authViewModel.adminLogin(textOf(etEmail), textOf(etPassword));
        });

        authViewModel.getAdminLoginResult().observe(this, result -> {
            if (result == null) return;
            setLoading(false);
            if (result.success && result.user != null) {
                SessionManager.saveSession(
                        this,
                        result.user.userId,
                        result.user.fullName,
                        result.user.role);
                goToDashboard();
            } else {
                tvMessage.setText(mapError(result.error));
            }
        });
    }

    private void goToDashboard() {
        startActivity(new Intent(this, AdminDashboardActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        progressLogin.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void clearErrors() {
        tilEmail.setError(null);
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
