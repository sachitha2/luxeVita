package com.example.ecostay.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.ServiceEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminServiceAdapter extends RecyclerView.Adapter<AdminServiceAdapter.AdminServiceViewHolder> {

    public interface OnServiceActionListener {
        void onEdit(ServiceEntity service);

        void onDelete(ServiceEntity service);
    }

    private final OnServiceActionListener listener;
    private final List<ServiceEntity> items = new ArrayList<>();

    public AdminServiceAdapter(OnServiceActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ServiceEntity> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service_admin_manage, parent, false);
        return new AdminServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminServiceViewHolder holder, int position) {
        ServiceEntity item = items.get(position);
        holder.tvName.setText(item.name);
        holder.tvCategory.setText(item.category);
        holder.tvDescription.setText(item.description);
        holder.tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", item.price));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AdminServiceViewHolder extends RecyclerView.ViewHolder {
        final CardView card;
        final TextView tvName;
        final TextView tvCategory;
        final TextView tvDescription;
        final TextView tvPrice;
        final Button btnEdit;
        final Button btnDelete;

        AdminServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (CardView) itemView;
            tvName = itemView.findViewById(R.id.tvAdminServiceName);
            tvCategory = itemView.findViewById(R.id.tvAdminServiceCategory);
            tvDescription = itemView.findViewById(R.id.tvAdminServiceDescription);
            tvPrice = itemView.findViewById(R.id.tvAdminServicePrice);
            btnEdit = itemView.findViewById(R.id.btnAdminServiceEdit);
            btnDelete = itemView.findViewById(R.id.btnAdminServiceDelete);
        }
    }
}
