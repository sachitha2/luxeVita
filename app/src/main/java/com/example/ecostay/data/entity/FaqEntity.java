package com.example.ecostay.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "faqs")
public class FaqEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "faqId")
    public int faqId;

    @NonNull
    public String question;

    @NonNull
    public String answer;

    @ColumnInfo(name = "image_path")
    public String imagePath;
}
