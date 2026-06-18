package com.example.ecostay.ui.admin.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.ecostay.data.repository.UserRepository;
import com.example.ecostay.data.repository.UserRepository.AuthResult;

public class AdminAuthViewModel extends AndroidViewModel {

    private final UserRepository userRepository;

    public AdminAuthViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public LiveData<AuthResult> getAdminLoginResult() {
        return userRepository.getAdminLoginResult();
    }

    public void adminLogin(String email, String password) {
        userRepository.adminLogin(email, password);
    }
}
