package com.example.ecostay.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "repair_status",
        foreignKeys = @ForeignKey(
                entity = BookingEntity.class,
                parentColumns = "bookingId",
                childColumns = "bookingId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("bookingId")}
)
public class RepairStatusEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "statusId")
    public int statusId;

    public int bookingId;

    @NonNull
    public String status;

    @NonNull
    public String remarks;

    @NonNull
    public String updatedAt;
}
