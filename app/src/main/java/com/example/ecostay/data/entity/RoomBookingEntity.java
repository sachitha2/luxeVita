package com.example.ecostay.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "room_bookings",
        indices = {
                @Index(value = {"user_id"}),
                @Index(value = {"room_type_id"})
        }
)
public class RoomBookingEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "user_id")
    public long userId;

    @ColumnInfo(name = "room_type_id")
    public long roomTypeId;

    // Dates are stored as epoch day (LocalDate -> epochDay) so we avoid TypeConverters.
    @ColumnInfo(name = "start_date_epoch_day")
    public long startDateEpochDay;

    @ColumnInfo(name = "end_date_epoch_day")
    public long endDateEpochDay;

    @NonNull
    public String status; // e.g., CONFIRMED, CANCELLED

    @ColumnInfo(name = "created_at_epoch_millis")
    public long createdAtEpochMillis;
}

