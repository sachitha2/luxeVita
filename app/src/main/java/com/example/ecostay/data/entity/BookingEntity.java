package com.example.ecostay.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "bookings",
        foreignKeys = {
                @ForeignKey(
                        entity = UserEntity.class,
                        parentColumns = "userId",
                        childColumns = "userId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = DeviceEntity.class,
                        parentColumns = "deviceId",
                        childColumns = "deviceId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = ServiceEntity.class,
                        parentColumns = "serviceId",
                        childColumns = "serviceId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("userId"),
                @Index("deviceId"),
                @Index("serviceId")
        }
)
public class BookingEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "bookingId")
    public int bookingId;

    public int userId;
    public int deviceId;
    public int serviceId;

    @NonNull
    public String issueDescription;

    @NonNull
    public String serviceMethod;

    @NonNull
    public String preferredDate;

    @NonNull
    public String preferredTime;

    @NonNull
    public String status;

    @NonNull
    public String technicianName;

    @NonNull
    public String estimatedCompletion;

    @NonNull
    public String createdAt;

    @NonNull
    public String photoPath = "";

    @NonNull
    public String adminRemarks = "";
}
