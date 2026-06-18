package com.example.ecostay.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecostay.data.AdminUserSeeder;
import com.example.ecostay.data.AppDatabase;
import com.example.ecostay.data.dao.UserDao;
import com.example.ecostay.data.entity.UserEntity;
import com.example.ecostay.security.PasswordUtils;
import com.example.ecostay.session.SessionManager;
import com.example.ecostay.util.ValidationUtils;

public class UserRepository {

    public enum AuthError {
        EMPTY_FIELDS,
        INVALID_EMAIL,
        INVALID_PHONE,
        INVALID_PASSWORD,
        EMAIL_EXISTS,
        PHONE_EXISTS,
        ACCOUNT_NOT_FOUND,
        INCORRECT_PASSWORD,
        USE_ADMIN_LOGIN,
        NOT_ADMIN_ACCOUNT,
        UNKNOWN
    }

    public static final class AuthResult {
        public final boolean success;
        public final AuthError error;
        public final UserEntity user;

        private AuthResult(boolean success, AuthError error, UserEntity user) {
            this.success = success;
            this.error = error;
            this.user = user;
        }

        public static AuthResult ok(UserEntity user) {
            return new AuthResult(true, null, user);
        }

        public static AuthResult fail(AuthError error) {
            return new AuthResult(false, error, null);
        }
    }

    private final UserDao userDao;
    private final MutableLiveData<AuthResult> loginResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResult> adminLoginResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResult> registerResult = new MutableLiveData<>();

    public UserRepository(Application application) {
        userDao = AppDatabase.getInstance(application).userDao();
    }

    public LiveData<AuthResult> getLoginResult() {
        return loginResult;
    }

    public LiveData<AuthResult> getAdminLoginResult() {
        return adminLoginResult;
    }

    public LiveData<AuthResult> getRegisterResult() {
        return registerResult;
    }

    public void login(String identifier, String password) {
        if (ValidationUtils.isEmpty(identifier) || ValidationUtils.isEmpty(password)) {
            loginResult.postValue(AuthResult.fail(AuthError.EMPTY_FIELDS));
            return;
        }
        AppDatabase.getWriteExecutor().execute(() -> {
            try {
                UserEntity user = userDao.findByEmailOrPhone(identifier.trim());
                if (user == null) {
                    loginResult.postValue(AuthResult.fail(AuthError.ACCOUNT_NOT_FOUND));
                    return;
                }
                if (SessionManager.ROLE_ADMIN.equals(user.role)) {
                    loginResult.postValue(AuthResult.fail(AuthError.USE_ADMIN_LOGIN));
                    return;
                }
                boolean valid = PasswordUtils.verifyPassword(
                        password, user.passwordSalt, user.password);
                if (!valid) {
                    loginResult.postValue(AuthResult.fail(AuthError.INCORRECT_PASSWORD));
                    return;
                }
                loginResult.postValue(AuthResult.ok(user));
            } catch (Exception e) {
                loginResult.postValue(AuthResult.fail(AuthError.UNKNOWN));
            }
        });
    }

    public void adminLogin(String email, String password) {
        if (ValidationUtils.isEmpty(email) || ValidationUtils.isEmpty(password)) {
            adminLoginResult.postValue(AuthResult.fail(AuthError.EMPTY_FIELDS));
            return;
        }
        AppDatabase.getWriteExecutor().execute(() -> {
            try {
                AdminUserSeeder.ensureAdminUser(userDao);
                String normalizedEmail = email.trim().toLowerCase();
                UserEntity user = userDao.findByEmailAndRole(
                        normalizedEmail, SessionManager.ROLE_ADMIN);
                if (user == null) {
                    adminLoginResult.postValue(AuthResult.fail(AuthError.ACCOUNT_NOT_FOUND));
                    return;
                }
                boolean valid = PasswordUtils.verifyPassword(
                        password, user.passwordSalt, user.password);
                if (!valid) {
                    adminLoginResult.postValue(AuthResult.fail(AuthError.INCORRECT_PASSWORD));
                    return;
                }
                adminLoginResult.postValue(AuthResult.ok(user));
            } catch (Exception e) {
                adminLoginResult.postValue(AuthResult.fail(AuthError.UNKNOWN));
            }
        });
    }

    public void register(String fullName, String email, String phone,
                         String password, String address) {
        if (ValidationUtils.isEmpty(fullName) || ValidationUtils.isEmpty(email)
                || ValidationUtils.isEmpty(phone) || ValidationUtils.isEmpty(password)
                || ValidationUtils.isEmpty(address)) {
            registerResult.postValue(AuthResult.fail(AuthError.EMPTY_FIELDS));
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            registerResult.postValue(AuthResult.fail(AuthError.INVALID_EMAIL));
            return;
        }
        if (!ValidationUtils.isValidPhone(phone)) {
            registerResult.postValue(AuthResult.fail(AuthError.INVALID_PHONE));
            return;
        }
        if (!ValidationUtils.isValidPassword(password)) {
            registerResult.postValue(AuthResult.fail(AuthError.INVALID_PASSWORD));
            return;
        }
        AppDatabase.getWriteExecutor().execute(() -> {
            try {
                if (userDao.findByEmail(email.trim()) != null) {
                    registerResult.postValue(AuthResult.fail(AuthError.EMAIL_EXISTS));
                    return;
                }
                if (userDao.findByPhone(phone.trim()) != null) {
                    registerResult.postValue(AuthResult.fail(AuthError.PHONE_EXISTS));
                    return;
                }
                PasswordUtils.SaltedHash hash = PasswordUtils.hashPassword(password);
                UserEntity user = new UserEntity();
                user.fullName = fullName.trim();
                user.email = email.trim();
                user.phone = phone.trim();
                user.password = hash.hashBase64;
                user.passwordSalt = hash.saltBase64;
                user.address = address.trim();
                user.role = SessionManager.ROLE_CUSTOMER;
                long id = userDao.insert(user);
                user.userId = (int) id;
                registerResult.postValue(AuthResult.ok(user));
            } catch (Exception e) {
                registerResult.postValue(AuthResult.fail(AuthError.UNKNOWN));
            }
        });
    }

    public void loadUserById(int userId, MutableLiveData<UserEntity> liveData) {
        AppDatabase.getWriteExecutor().execute(() -> {
            liveData.postValue(userDao.getById(userId));
        });
    }
}
