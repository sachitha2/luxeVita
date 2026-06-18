package com.example.ecostay.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.DeviceEntity;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    public interface Listener {
        void onEdit(DeviceEntity device);

        void onDelete(DeviceEntity device);
    }

    private final List<DeviceEntity> items = new ArrayList<>();
    private final Listener listener;

    public DeviceAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<DeviceEntity> devices) {
        items.clear();
        if (devices != null) {
            items.addAll(devices);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DeviceEntity device = items.get(position);
        holder.tvDeviceLabel.setText(device.brand + " " + device.model);
        holder.tvDeviceType.setText(device.deviceType);
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(device));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(device));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvDeviceLabel;
        final TextView tvDeviceType;
        final Button btnEdit;
        final Button btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceLabel = itemView.findViewById(R.id.tvDeviceLabel);
            tvDeviceType = itemView.findViewById(R.id.tvDeviceType);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
