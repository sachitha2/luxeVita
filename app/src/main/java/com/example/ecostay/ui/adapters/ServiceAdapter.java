package com.example.ecostay.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.ServiceEntity;

import java.util.ArrayList;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    public interface OnServiceClickListener {
        void onServiceClicked(ServiceEntity service);
    }

    private final OnServiceClickListener listener;
    private final List<ServiceEntity> items = new ArrayList<>();

    public ServiceAdapter(OnServiceClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ServiceEntity> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        ServiceEntity item = items.get(position);
        holder.tvName.setText(item.name);
        holder.tvCategory.setText(item.category);
        holder.tvPrice.setText("$" + item.price + "");
        holder.tvDescription.setText(item.description);

        holder.card.setOnClickListener(v -> listener.onServiceClicked(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        final CardView card;
        final TextView tvName;
        final TextView tvCategory;
        final TextView tvPrice;
        final TextView tvDescription;

        ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (CardView) itemView;
            tvName = itemView.findViewById(R.id.tvServiceName);
            tvCategory = itemView.findViewById(R.id.tvServiceCategory);
            tvPrice = itemView.findViewById(R.id.tvServicePrice);
            tvDescription = itemView.findViewById(R.id.tvServiceDescription);
        }
    }
}

