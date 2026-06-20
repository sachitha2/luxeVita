package com.example.ecostay.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ecostay.data.AppDatabase;
import com.example.ecostay.data.dao.SupportMessageDao;
import com.example.ecostay.data.dao.UserDao;
import com.example.ecostay.data.entity.SupportMessageEntity;
import com.example.ecostay.data.entity.UserEntity;
import com.example.ecostay.data.model.AdminSupportMessageSummary;
import com.example.ecostay.util.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class SupportMessageRepository {

    public static final class OperationResult {
        public final boolean success;
        public final String message;

        public OperationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    private final SupportMessageDao supportMessageDao;
    private final UserDao userDao;
    private final MutableLiveData<List<AdminSupportMessageSummary>> adminMessages =
            new MutableLiveData<>();
    private final MutableLiveData<OperationResult> submitResult = new MutableLiveData<>();

    public SupportMessageRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        supportMessageDao = db.supportMessageDao();
        userDao = db.userDao();
    }

    public LiveData<List<AdminSupportMessageSummary>> getAdminMessages() {
        return adminMessages;
    }

    public LiveData<OperationResult> getSubmitResult() {
        return submitResult;
    }

    public void clearSubmitResult() {
        submitResult.setValue(null);
    }

    public void submitMessage(int userId, String userName, String message) {
        AppDatabase.getWriteExecutor().execute(() -> {
            try {
                if (userId <= 0) {
                    submitResult.postValue(new OperationResult(false, "Invalid session"));
                    return;
                }

                String trimmedName = userName.trim();
                String trimmedMessage = message.trim();
                if (trimmedName.isEmpty() || trimmedMessage.isEmpty()) {
                    submitResult.postValue(new OperationResult(false, "Empty fields"));
                    return;
                }

                SupportMessageEntity entity = new SupportMessageEntity();
                entity.userId = userId;
                entity.userName = trimmedName;
                entity.message = trimmedMessage;
                entity.createdAt = DateTimeUtils.nowIso();
                supportMessageDao.insert(entity);
                submitResult.postValue(new OperationResult(true, "Message sent"));
            } catch (Exception e) {
                submitResult.postValue(new OperationResult(false, "Failed to send message"));
            }
        });
    }

    public void loadAdminMessages() {
        AppDatabase.getWriteExecutor().execute(() -> {
            List<SupportMessageEntity> messages = supportMessageDao.getAll();
            List<AdminSupportMessageSummary> summaries = new ArrayList<>();
            for (SupportMessageEntity message : messages) {
                summaries.add(toAdminSummary(message));
            }
            adminMessages.postValue(summaries);
        });
    }

    private AdminSupportMessageSummary toAdminSummary(SupportMessageEntity message) {
        AdminSupportMessageSummary summary = new AdminSupportMessageSummary();
        summary.messageId = message.messageId;
        summary.userName = message.userName;
        summary.message = message.message;
        summary.createdAt = message.createdAt;

        UserEntity user = userDao.getById(message.userId);
        if (user != null) {
            summary.userEmail = user.email;
            summary.userPhone = user.phone;
        } else {
            summary.userEmail = "";
            summary.userPhone = "";
        }
        return summary;
    }
}
