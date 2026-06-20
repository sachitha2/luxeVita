package com.example.ecostay.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.ecostay.data.entity.UserEntity;
import com.example.ecostay.data.repository.UserRepository;
import com.example.ecostay.data.repository.UserRepository.ProfileUpdateResult;

public class ProfileViewModel extends AndroidViewModel {

    private final UserRepository userRepository;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public LiveData<UserEntity> getProfileUser() {
        return userRepository.getProfileUser();
    }

    public LiveData<ProfileUpdateResult> getProfileUpdateResult() {
        return userRepository.getProfileUpdateResult();
    }

    public void clearProfileUpdateResult() {
        userRepository.clearProfileUpdateResult();
    }

    public void loadProfile(int userId) {
        userRepository.loadProfile(userId);
    }

    public void updateProfile(int userId, String fullName, String email, String phone,
                              String address, String profileImagePath, boolean removeProfileImage) {
        userRepository.clearProfileUpdateResult();
        userRepository.updateProfile(
                userId, fullName, email, phone, address, profileImagePath, removeProfileImage);
    }
}
