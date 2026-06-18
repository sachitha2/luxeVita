package com.example.ecostay.ui.admin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.MaintenanceTipEntity;
import com.example.ecostay.util.StatusUiUtils;

import java.util.ArrayList;
import java.util.List;

public class AdminTipAdapter extends RecyclerView.Adapter<AdminTipAdapter.ViewHolder> {

    public interface Listener {
        void onEdit(MaintenanceTipEntity tip);

        void onDelete(MaintenanceTipEntity tip);
    }

    private final List<MaintenanceTipEntity> items = new ArrayList<>();
    private final Listener listener;

    public AdminTipAdapter(Listener listener) {
        this.listener = listener;
    }

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
                .inflate(R.layout.item_admin_tip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MaintenanceTipEntity tip = items.get(position);
        StatusUiUtils.applyStatusChip(holder.tvDeviceType, tip.deviceType);
        holder.tvTitle.setText(tip.title);
        holder.tvDescription.setText(tip.description);
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(tip));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(tip));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvDeviceType;
        final TextView tvTitle;
        final TextView tvDescription;
        final Button btnEdit;
        final Button btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceType = itemView.findViewById(R.id.tvDeviceType);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
