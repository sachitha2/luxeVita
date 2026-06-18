package com.example.ecostay.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "maintenance_tips")
public class MaintenanceTipEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "tipId")
    public int tipId;

    @NonNull
    public String deviceType;

    @NonNull
    public String title;

    @NonNull
    public String description;
}
