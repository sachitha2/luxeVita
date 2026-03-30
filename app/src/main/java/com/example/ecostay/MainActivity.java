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
        TextInputEditText etEmail = findViewById(R.id.etEmail);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        TextInputEditText etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnSubmit = findViewById(R.id.btnSubmit);
        TextView tvMessage = findViewById(R.id.tvMessage);

        RadioButton rbLogin = findViewById(R.id.rbLogin);

        final boolean[] isSignupMode = new boolean[]{!rbLogin.isChecked()};
        tilConfirmPassword.setVisibility(isSignupMode[0] ? View.VISIBLE : View.GONE);
        btnSubmit.setText(isSignupMode[0] ? "Sign Up" : "Login");

        radioMode.setOnCheckedChangeListener((group, checkedId) -> {
            boolean signup = checkedId == R.id.rbSignup;
            isSignupMode[0] = signup;
            tilConfirmPassword.setVisibility(signup ? View.VISIBLE : View.GONE);
            btnSubmit.setText(signup ? "Sign Up" : "Login");
            tvMessage.setText("");
        });

        btnSubmit.setOnClickListener(v -> {
            String email = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();
            String password = etPassword.getText() == null ? "" : etPassword.getText().toString();
            String confirmPassword = etConfirmPassword.getText() == null ? "" : etConfirmPassword.getText().toString();

            tvMessage.setText("");

            if (!ValidationUtils.isValidEmail(email)) {
                tvMessage.setText("Enter a valid email address.");
                return;
            }

            if (!ValidationUtils.isValidPassword(password)) {
                tvMessage.setText("Password must be at least 8 characters.");
                return;
            }

            if (isSignupMode[0]) {
                if (confirmPassword.isEmpty()) {
                    tvMessage.setText("Confirm your password.");
                    return;
                }
                if (!password.equals(confirmPassword)) {
                    tvMessage.setText("Passwords do not match.");
                    return;
                }

                // Sign-up flow.
                String safeEmail = email;
                dbExecutor.execute(() -> {
                    try {
                        UserEntity existing = userDao.findByEmail(safeEmail);
                        if (existing != null) {
                            mainHandler.post(() -> tvMessage.setText("Email already registered. Try login."));
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
                        mainHandler.post(() -> tvMessage.setText("Sign-up failed. Please try again."));
                    }
                });
            } else {
                // Login flow.
                String safeEmail = email;
                String passwordToCheck = password;
                dbExecutor.execute(() -> {
                    try {
                        UserEntity user = userDao.findByEmail(safeEmail);
                        if (user == null) {
                            mainHandler.post(() -> tvMessage.setText("No account found. Please sign up."));
                            return;
                        }

                        boolean ok = PasswordUtils.verifyPassword(
                                passwordToCheck,
                                user.passwordSalt,
                                user.passwordHash
                        );
                        if (!ok) {
                            mainHandler.post(() -> tvMessage.setText("Incorrect password."));
                            return;
                        }

                        SessionManager.saveSession(MainActivity.this, user.id, safeEmail);
                        mainHandler.post(() -> {
                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    } catch (Exception e) {
                        mainHandler.post(() -> tvMessage.setText("Login failed. Please try again."));
                    }
                });
            }
        });
    }
}