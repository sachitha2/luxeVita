package com.example.ecostay;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.ecostay.data.EcoStayDatabase;
import com.example.ecostay.data.dao.UserDao;
import com.example.ecostay.data.entity.UserEntity;
import com.example.ecostay.security.PasswordUtils;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.ui.HomeActivity;
import com.example.ecostay.util.ValidationUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EcoStayDatabase database = EcoStayDatabase.getInstance(this);
        UserDao userDao = database.userDao();

        RadioGroup radioMode = findViewById(R.id.radioMode);
        TextInputLayout tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        TextInputLayout tilEmail = findViewById(R.id.tilEmail);
        TextInputLayout tilPassword = findViewById(R.id.tilPassword);
        TextInputEditText etEmail = findViewById(R.id.etEmail);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        TextInputEditText etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnSubmit = findViewById(R.id.btnSubmit);
        TextView tvMessage = findViewById(R.id.tvMessage);

        RadioButton rbLogin = findViewById(R.id.rbLogin);

        final boolean[] isSignupMode = new boolean[]{!rbLogin.isChecked()};
        tilConfirmPassword.setVisibility(isSignupMode[0] ? View.VISIBLE : View.GONE);
        btnSubmit.setText(isSignupMode[0] ? getString(R.string.action_create_account) : getString(R.string.mode_login));

        radioMode.setOnCheckedChangeListener((group, checkedId) -> {
            boolean signup = checkedId == R.id.rbSignup;
            isSignupMode[0] = signup;
            tilConfirmPassword.setVisibility(signup ? View.VISIBLE : View.GONE);
            btnSubmit.setText(signup ? getString(R.string.action_create_account) : getString(R.string.mode_login));
            tvMessage.setText("");
            clearValidationErrors(tilEmail, tilPassword, tilConfirmPassword);
        });

        btnSubmit.setOnClickListener(v -> {
            String email = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();
            String password = etPassword.getText() == null ? "" : etPassword.getText().toString();
            String confirmPassword = etConfirmPassword.getText() == null ? "" : etConfirmPassword.getText().toString();

            tvMessage.setText("");
            clearValidationErrors(tilEmail, tilPassword, tilConfirmPassword);

            if (!ValidationUtils.isValidEmail(email)) {
                tilEmail.setError(getString(R.string.error_valid_email));
                return;
            }

            if (!ValidationUtils.isValidPassword(password)) {
                tilPassword.setError(getString(R.string.error_valid_password));
                return;
            }

            if (isSignupMode[0]) {
                if (confirmPassword.isEmpty()) {
                    tilConfirmPassword.setError(getString(R.string.error_confirm_password));
                    return;
                }
                if (!password.equals(confirmPassword)) {
                    tilConfirmPassword.setError(getString(R.string.error_password_mismatch));
                    return;
                }

                setSubmitLoading(btnSubmit, true, true);
                // Sign-up flow.
                String safeEmail = email;
                dbExecutor.execute(() -> {
                    try {
                        UserEntity existing = userDao.findByEmail(safeEmail);
                        if (existing != null) {
                            mainHandler.post(() -> {
                                setSubmitLoading(btnSubmit, false, true);
                                tvMessage.setText(R.string.error_email_registered);
                            });
                            return;
                        }

                        PasswordUtils.SaltedHash saltedHash = PasswordUtils.hashPassword(password);
                        UserEntity user = new UserEntity();
                        user.email = safeEmail;
                        user.passwordSalt = saltedHash.saltBase64;
                        user.passwordHash = saltedHash.hashBase64;

                        long userId = userDao.insert(user);
                        SessionManager.saveSession(MainActivity.this, userId, safeEmail);

                        mainHandler.post(() -> {
                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    } catch (Exception e) {
                        mainHandler.post(() -> {
                            setSubmitLoading(btnSubmit, false, true);
                            tvMessage.setText(R.string.error_signup_failed);
                        });
                    }
                });
            } else {
                setSubmitLoading(btnSubmit, true, false);
                // Login flow.
                String safeEmail = email;
                String passwordToCheck = password;
                dbExecutor.execute(() -> {
                    try {
                        UserEntity user = userDao.findByEmail(safeEmail);
                        if (user == null) {
                            mainHandler.post(() -> {
                                setSubmitLoading(btnSubmit, false, false);
                                tvMessage.setText(R.string.error_account_not_found);
                            });
                            return;
                        }

                        boolean ok = PasswordUtils.verifyPassword(
                                passwordToCheck,
                                user.passwordSalt,
                                user.passwordHash
                        );
                        if (!ok) {
                            mainHandler.post(() -> {
                                setSubmitLoading(btnSubmit, false, false);
                                tvMessage.setText(R.string.error_incorrect_password);
                            });
                            return;
                        }

                        SessionManager.saveSession(MainActivity.this, user.id, safeEmail);
                        mainHandler.post(() -> {
                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    } catch (Exception e) {
                        mainHandler.post(() -> {
                            setSubmitLoading(btnSubmit, false, false);
                            tvMessage.setText(R.string.error_login_failed);
                        });
                    }
                });
            }
        });
    }

    private void clearValidationErrors(TextInputLayout... inputLayouts) {
        for (TextInputLayout inputLayout : inputLayouts) {
            inputLayout.setError(null);
        }
    }

    private void setSubmitLoading(Button btnSubmit, boolean isLoading, boolean signupMode) {
        btnSubmit.setEnabled(!isLoading);
        if (isLoading) {
            btnSubmit.setText(signupMode ? R.string.progress_signing_up : R.string.progress_logging_in);
            return;
        }
        btnSubmit.setText(signupMode ? R.string.action_create_account : R.string.mode_login);
    }
}