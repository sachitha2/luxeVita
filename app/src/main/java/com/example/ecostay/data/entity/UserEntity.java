package com.example.ecostay.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "users",
        indices = {
                @Index(value = {"email"}, unique = true),
                @Index(value = {"phone"}, unique = true)
        }
)
public class UserEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "userId")
    public int userId;

    @NonNull
    public String fullName;

    @NonNull
    public String email;

    @NonNull
    public String phone;

    @NonNull
    public String password;

    @NonNull
    @ColumnInfo(name = "password_salt")
    public String passwordSalt;

    @NonNull
    public String address;

    @NonNull
    public String role = "CUSTOMER";
}
