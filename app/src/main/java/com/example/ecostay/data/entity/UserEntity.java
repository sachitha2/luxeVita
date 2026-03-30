package com.example.ecostay.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "users",
        indices = {@Index(value = {"email"}, unique = true)}
)
public class UserEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String email;

    @NonNull
    @ColumnInfo(name = "password_hash")
    public String passwordHash;

    @NonNull
    @ColumnInfo(name = "password_salt")
    public String passwordSalt;

    // Preferences / personalization (optional until user sets them)
    public Long preferredRoomTypeId;
    public Double maxBudget;

    public Long travelStartDateEpochDay;
    public Long travelEndDateEpochDay;
}

