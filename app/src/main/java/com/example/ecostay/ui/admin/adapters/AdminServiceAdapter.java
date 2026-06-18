package com.example.ecostay.ui.admin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.ServiceEntity;

import java.util.ArrayList;
import java.util.List;

public class AdminServiceAdapter extends RecyclerView.Adapter<AdminServiceAdapter.ViewHolder> {

    public interface Listener {
        void onEdit(ServiceEntity service);

        void onDelete(ServiceEntity service);
    }

    private final List<ServiceEntity> items = new ArrayList<>();
    private final Listener listener;

    public AdminServiceAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<ServiceEntity> services) {
        items.clear();
        if (services != null) {
            items.addAll(services);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServiceEntity service = items.get(position);
        holder.tvDeviceType.setText(service.deviceType);
        holder.tvServiceName.setText(service.serviceName);
        holder.tvDescription.setText(service.description);
        holder.tvPrice.setText(holder.itemView.getContext().getString(
                R.string.price_format, service.estimatedPrice));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(service));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(service));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvDeviceType;
        final TextView tvServiceName;
        final TextView tvDescription;
        final TextView tvPrice;
        final Button btnEdit;
        final Button btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceType = itemView.findViewById(R.id.tvDeviceType);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
