package com.example.ecostay.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "slideshow_slides")
public class SlideshowSlideEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "slideId")
    public int slideId;

    @NonNull
    public String title;

    public String caption;

    @ColumnInfo(name = "image_path")
    public String imagePath;

    @ColumnInfo(name = "sort_order")
    public int sortOrder;

    @ColumnInfo(name = "is_active")
    public boolean isActive;
}
