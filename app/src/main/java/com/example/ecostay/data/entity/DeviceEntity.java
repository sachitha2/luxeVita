package com.example.ecostay.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "devices",
        foreignKeys = @ForeignKey(
                entity = UserEntity.class,
                parentColumns = "userId",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("userId")}
)
public class DeviceEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "deviceId")
    public int deviceId;

    public int userId;

    @NonNull
    public String deviceType;

    @NonNull
    public String brand;

    @NonNull
    public String model;
}
