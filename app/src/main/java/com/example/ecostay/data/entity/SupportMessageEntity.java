package com.example.ecostay.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "support_messages",
        indices = @Index("userId")
)
public class SupportMessageEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "messageId")
    public int messageId;

    public int userId;

    @NonNull
    public String userName;

    @NonNull
    public String message;

    @NonNull
    public String createdAt;
}
