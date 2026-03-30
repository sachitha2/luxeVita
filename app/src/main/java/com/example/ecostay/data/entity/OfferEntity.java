package com.example.ecostay.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "offers")
public class OfferEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String title;

    @NonNull
    public String description;

    @ColumnInfo(name = "valid_from_epoch_day")
    public long validFromEpochDay;

    @ColumnInfo(name = "valid_to_epoch_day")
    public long validToEpochDay;
}

