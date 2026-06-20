package com.example.ecostay.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.DeviceEntity;
import com.example.ecostay.util.PhotoUtils;
import com.example.ecostay.util.StatusUiUtils;

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
        StatusUiUtils.applyStatusChip(holder.tvDeviceType, device.deviceType);
        holder.tvDeviceLabel.setText(device.brand);
        holder.tvDeviceMeta.setText(device.model);
        PhotoUtils.bindImage(holder.ivDeviceImage, device.imagePath);
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
        final TextView tvDeviceMeta;
        final ImageView ivDeviceImage;
        final Button btnEdit;
        final Button btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceLabel = itemView.findViewById(R.id.tvDeviceLabel);
            tvDeviceType = itemView.findViewById(R.id.tvDeviceType);
            tvDeviceMeta = itemView.findViewById(R.id.tvDeviceMeta);
            ivDeviceImage = itemView.findViewById(R.id.ivDeviceImage);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
