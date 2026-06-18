package com.example.ecostay.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.MaintenanceTipEntity;

import java.util.ArrayList;
import java.util.List;

public class MaintenanceTipAdapter extends RecyclerView.Adapter<MaintenanceTipAdapter.ViewHolder> {

    private final List<MaintenanceTipEntity> items = new ArrayList<>();

    public void setItems(List<MaintenanceTipEntity> tips) {
        items.clear();
        if (tips != null) {
            items.addAll(tips);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_maintenance_tip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MaintenanceTipEntity tip = items.get(position);
        holder.tvDeviceType.setText(tip.deviceType);
        holder.tvTitle.setText(tip.title);
        holder.tvDescription.setText(tip.description);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvDeviceType;
        final TextView tvTitle;
        final TextView tvDescription;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceType = itemView.findViewById(R.id.tvDeviceType);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }
}
