package com.example.ecostay.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ecostay.data.entity.UserEntity;

import java.util.List;

@Dao
public interface UserDao {

    @Insert
    long insert(UserEntity user);

    @Update
    void update(UserEntity user);

    @Delete
    void delete(UserEntity user);

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    UserEntity getById(int userId);

    @Query("SELECT * FROM users")
    List<UserEntity> getAll();

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity findByEmail(String email);

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    UserEntity findByPhone(String phone);

    @Query("SELECT * FROM users WHERE email = :identifier OR phone = :identifier LIMIT 1")
    UserEntity findByEmailOrPhone(String identifier);

    @Query("SELECT * FROM users WHERE email = :email AND role = :role LIMIT 1")
    UserEntity findByEmailAndRole(String email, String role);
}
