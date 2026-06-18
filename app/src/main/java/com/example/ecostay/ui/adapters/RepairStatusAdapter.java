package com.example.ecostay.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.RepairStatusEntity;

import java.util.ArrayList;
import java.util.List;

public class RepairStatusAdapter extends RecyclerView.Adapter<RepairStatusAdapter.ViewHolder> {

    private final List<RepairStatusEntity> items = new ArrayList<>();

    public void setItems(List<RepairStatusEntity> statuses) {
        items.clear();
        if (statuses != null) {
            items.addAll(statuses);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_repair_status, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RepairStatusEntity status = items.get(position);
        holder.tvStatus.setText(status.status);
        holder.tvRemarks.setText(status.remarks);
        holder.tvUpdatedAt.setText(status.updatedAt);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvStatus;
        final TextView tvRemarks;
        final TextView tvUpdatedAt;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvRemarks = itemView.findViewById(R.id.tvRemarks);
            tvUpdatedAt = itemView.findViewById(R.id.tvUpdatedAt);
        }
    }
}
