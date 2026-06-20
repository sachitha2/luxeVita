package com.example.ecostay.ui.admin.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.ecostay.data.model.AdminSupportMessageSummary;
import com.example.ecostay.data.repository.SupportMessageRepository;

import java.util.List;

public class AdminSupportViewModel extends AndroidViewModel {

    private final SupportMessageRepository supportMessageRepository;

    public AdminSupportViewModel(@NonNull Application application) {
        super(application);
        supportMessageRepository = new SupportMessageRepository(application);
    }

    public LiveData<List<AdminSupportMessageSummary>> getMessages() {
        return supportMessageRepository.getAdminMessages();
    }

    public void loadMessages() {
        supportMessageRepository.loadAdminMessages();
    }
}
