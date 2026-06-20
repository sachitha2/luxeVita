package com.example.ecostay.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.SlideshowSlideEntity;
import com.example.ecostay.util.PhotoUtils;

import java.util.ArrayList;
import java.util.List;

public class SlideshowAdapter extends RecyclerView.Adapter<SlideshowAdapter.SlideViewHolder> {

    private final List<SlideshowSlideEntity> slides = new ArrayList<>();

    public void setSlides(List<SlideshowSlideEntity> items) {
        slides.clear();
        if (items != null) {
            slides.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_slideshow_slide, parent, false);
        return new SlideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
        SlideshowSlideEntity slide = slides.get(position);
        holder.tvTitle.setText(slide.title);
        if (slide.caption != null && !slide.caption.isEmpty()) {
            holder.tvCaption.setVisibility(View.VISIBLE);
            holder.tvCaption.setText(slide.caption);
        } else {
            holder.tvCaption.setVisibility(View.GONE);
        }
        PhotoUtils.bindImage(holder.ivSlideImage, slide.imagePath);
    }

    @Override
    public int getItemCount() {
        return slides.size();
    }

    static class SlideViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivSlideImage;
        final TextView tvTitle;
        final TextView tvCaption;

        SlideViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSlideImage = itemView.findViewById(R.id.ivSlideImage);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCaption = itemView.findViewById(R.id.tvCaption);
        }
    }
}
