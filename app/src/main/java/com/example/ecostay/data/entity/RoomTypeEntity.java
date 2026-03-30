package com.example.ecostay.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "room_types")
public class RoomTypeEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name;

    @NonNull
    public String description;

    @ColumnInfo(name = "price_per_night")
    public double pricePerNight;

    // Total inventory of this room type across the property.
    public int totalRooms;

    // Optional image reference (e.g., drawable resource name) for UI.
    public String imageRef;
}

