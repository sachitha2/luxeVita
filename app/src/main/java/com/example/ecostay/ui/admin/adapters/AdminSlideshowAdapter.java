package com.example.ecostay.ui.admin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.SlideshowSlideEntity;
import com.example.ecostay.util.PhotoUtils;

import java.util.ArrayList;
import java.util.List;

public class AdminSlideshowAdapter extends RecyclerView.Adapter<AdminSlideshowAdapter.ViewHolder> {

    public interface Listener {
        void onEdit(SlideshowSlideEntity slide);

        void onDelete(SlideshowSlideEntity slide);
    }

    private final List<SlideshowSlideEntity> items = new ArrayList<>();
    private final Listener listener;

    public AdminSlideshowAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<SlideshowSlideEntity> slides) {
        items.clear();
        if (slides != null) {
            items.addAll(slides);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_slideshow, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SlideshowSlideEntity slide = items.get(position);
        holder.tvTitle.setText(slide.title);
        if (slide.caption != null && !slide.caption.isEmpty()) {
            holder.tvCaption.setVisibility(View.VISIBLE);
            holder.tvCaption.setText(slide.caption);
        } else {
            holder.tvCaption.setVisibility(View.GONE);
        }
        holder.tvStatus.setText(slide.isActive
                ? holder.itemView.getContext().getString(R.string.slide_status_active)
                : holder.itemView.getContext().getString(R.string.slide_status_inactive));
        PhotoUtils.bindImage(holder.ivPreview, slide.imagePath);
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(slide));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(slide));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivPreview;
        final TextView tvTitle;
        final TextView tvCaption;
        final TextView tvStatus;
        final Button btnEdit;
        final Button btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPreview = itemView.findViewById(R.id.ivPreview);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
