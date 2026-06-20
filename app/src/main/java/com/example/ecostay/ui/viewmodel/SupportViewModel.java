package com.example.ecostay.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.ecostay.data.repository.SupportMessageRepository;
import com.example.ecostay.data.repository.SupportMessageRepository.OperationResult;

public class SupportViewModel extends AndroidViewModel {

    private final SupportMessageRepository supportMessageRepository;

    public SupportViewModel(@NonNull Application application) {
        super(application);
        supportMessageRepository = new SupportMessageRepository(application);
    }

    public LiveData<OperationResult> getSubmitResult() {
        return supportMessageRepository.getSubmitResult();
    }

    public void clearSubmitResult() {
        supportMessageRepository.clearSubmitResult();
    }

    public void submitMessage(int userId, String userName, String message) {
        supportMessageRepository.clearSubmitResult();
        supportMessageRepository.submitMessage(userId, userName, message);
    }
}
