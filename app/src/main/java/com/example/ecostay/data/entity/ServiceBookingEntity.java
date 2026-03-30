package com.example.ecostay.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "service_bookings",
        indices = {
                @Index(value = {"user_id"}),
                @Index(value = {"service_id"}),
                @Index(value = {"booking_date_epoch_day"})
        }
)
public class ServiceBookingEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "user_id")
    public long userId;

    @ColumnInfo(name = "service_id")
    public long serviceId;

    @ColumnInfo(name = "booking_date_epoch_day")
    public long bookingDateEpochDay;

    @NonNull
    public String status; // CONFIRMED, CANCELLED

    @ColumnInfo(name = "created_at_epoch_millis")
    public long createdAtEpochMillis;
}

