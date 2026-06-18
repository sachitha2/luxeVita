package com.example.ecostay.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.ecostay.data.repository.UserRepository;

public class AuthViewModel extends AndroidViewModel {

    private final UserRepository userRepository;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public LiveData<UserRepository.AuthResult> getLoginResult() {
        return userRepository.getLoginResult();
    }

    public LiveData<UserRepository.AuthResult> getRegisterResult() {
        return userRepository.getRegisterResult();
    }

    public void login(String identifier, String password) {
        userRepository.login(identifier, password);
    }

    public void register(String fullName, String email, String phone,
                         String password, String address) {
        userRepository.register(fullName, email, phone, password, address);
    }
}
