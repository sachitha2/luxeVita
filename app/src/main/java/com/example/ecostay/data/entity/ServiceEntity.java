package com.example.ecostay.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "services")
public class ServiceEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "serviceId")
    public int serviceId;

    @NonNull
    public String deviceType;

    @NonNull
    public String serviceName;

    @NonNull
    public String description;

    public double estimatedPrice;
}
