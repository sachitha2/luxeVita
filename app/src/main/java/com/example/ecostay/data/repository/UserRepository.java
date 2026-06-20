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

    public enum ProfileError {
        EMPTY_FIELDS,
        INVALID_EMAIL,
        INVALID_PHONE,
        EMAIL_EXISTS,
        PHONE_EXISTS,
        USER_NOT_FOUND,
        UNKNOWN
    }

    public static final class ProfileUpdateResult {
        public final boolean success;
        public final ProfileError error;
        public final UserEntity user;

        private ProfileUpdateResult(boolean success, ProfileError error, UserEntity user) {
            this.success = success;
            this.error = error;
            this.user = user;
        }

        public static ProfileUpdateResult ok(UserEntity user) {
            return new ProfileUpdateResult(true, null, user);
        }

        public static ProfileUpdateResult fail(ProfileError error) {
            return new ProfileUpdateResult(false, error, null);
        }
    }

    private final MutableLiveData<ProfileUpdateResult> profileUpdateResult = new MutableLiveData<>();
    private final MutableLiveData<UserEntity> profileUser = new MutableLiveData<>();

    public LiveData<ProfileUpdateResult> getProfileUpdateResult() {
        return profileUpdateResult;
    }

    public LiveData<UserEntity> getProfileUser() {
        return profileUser;
    }

    public void clearProfileUpdateResult() {
        profileUpdateResult.postValue(null);
    }

    public void loadProfile(int userId) {
        AppDatabase.getWriteExecutor().execute(() -> {
            profileUser.postValue(userDao.getById(userId));
        });
    }

    public void updateProfile(int userId, String fullName, String email, String phone,
                            String address, String profileImagePath, boolean removeProfileImage) {
        if (ValidationUtils.isEmpty(fullName) || ValidationUtils.isEmpty(email)
                || ValidationUtils.isEmpty(phone) || ValidationUtils.isEmpty(address)) {
            profileUpdateResult.postValue(ProfileUpdateResult.fail(ProfileError.EMPTY_FIELDS));
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            profileUpdateResult.postValue(ProfileUpdateResult.fail(ProfileError.INVALID_EMAIL));
            return;
        }
        if (!ValidationUtils.isValidPhone(phone)) {
            profileUpdateResult.postValue(ProfileUpdateResult.fail(ProfileError.INVALID_PHONE));
            return;
        }
        AppDatabase.getWriteExecutor().execute(() -> {
            try {
                UserEntity user = userDao.getById(userId);
                if (user == null) {
                    profileUpdateResult.postValue(ProfileUpdateResult.fail(ProfileError.USER_NOT_FOUND));
                    return;
                }
                String normalizedEmail = email.trim();
                String normalizedPhone = phone.trim();
                UserEntity emailMatch = userDao.findByEmail(normalizedEmail);
                if (emailMatch != null && emailMatch.userId != userId) {
                    profileUpdateResult.postValue(ProfileUpdateResult.fail(ProfileError.EMAIL_EXISTS));
                    return;
                }
                UserEntity phoneMatch = userDao.findByPhone(normalizedPhone);
                if (phoneMatch != null && phoneMatch.userId != userId) {
                    profileUpdateResult.postValue(ProfileUpdateResult.fail(ProfileError.PHONE_EXISTS));
                    return;
                }
                user.fullName = fullName.trim();
                user.email = normalizedEmail;
                user.phone = normalizedPhone;
                user.address = address.trim();
                if (removeProfileImage) {
                    user.profileImagePath = null;
                } else if (profileImagePath != null) {
                    user.profileImagePath = profileImagePath;
                }
                userDao.update(user);
                profileUpdateResult.postValue(ProfileUpdateResult.ok(user));
            } catch (Exception e) {
                profileUpdateResult.postValue(ProfileUpdateResult.fail(ProfileError.UNKNOWN));
            }
        });
    }
}
