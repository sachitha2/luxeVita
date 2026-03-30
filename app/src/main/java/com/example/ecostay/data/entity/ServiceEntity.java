package com.example.ecostay.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "services")
public class ServiceEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name;

    // SPA, DINING, CABANAS, TOURS (stored as string to keep the app simple)
    @NonNull
    public String category;

    @NonNull
    public String description;

    @ColumnInfo(name = "price")
    public double price;
}

