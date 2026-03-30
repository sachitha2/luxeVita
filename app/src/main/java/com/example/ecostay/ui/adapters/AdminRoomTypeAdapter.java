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
import com.example.ecostay.data.entity.RoomTypeEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminRoomTypeAdapter extends RecyclerView.Adapter<AdminRoomTypeAdapter.AdminRoomTypeViewHolder> {

    public interface OnRoomActionListener {
        void onEdit(RoomTypeEntity roomType);

        void onDelete(RoomTypeEntity roomType);
    }

    private final OnRoomActionListener listener;
    private final List<RoomTypeEntity> items = new ArrayList<>();

    public AdminRoomTypeAdapter(OnRoomActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<RoomTypeEntity> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminRoomTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_admin_manage, parent, false);
        return new AdminRoomTypeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminRoomTypeViewHolder holder, int position) {
        RoomTypeEntity item = items.get(position);
        holder.tvName.setText(item.name);
        holder.tvDescription.setText(item.description);
        holder.tvPrice.setText(String.format(Locale.getDefault(), "$%.2f / night", item.pricePerNight));
        holder.tvInventory.setText(
                holder.itemView.getContext().getString(R.string.admin_room_inventory_count, item.totalRooms)
        );
        holder.tvImageRef.setText(
                holder.itemView.getContext().getString(
                        R.string.admin_room_image_ref_value,
                        item.imageRef == null || item.imageRef.trim().isEmpty() ? "room_default" : item.imageRef
                )
        );
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AdminRoomTypeViewHolder extends RecyclerView.ViewHolder {
        final CardView card;
        final TextView tvName;
        final TextView tvDescription;
        final TextView tvPrice;
        final TextView tvInventory;
        final TextView tvImageRef;
        final Button btnEdit;
        final Button btnDelete;

        AdminRoomTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (CardView) itemView;
            tvName = itemView.findViewById(R.id.tvAdminRoomName);
            tvDescription = itemView.findViewById(R.id.tvAdminRoomDescription);
            tvPrice = itemView.findViewById(R.id.tvAdminRoomPrice);
            tvInventory = itemView.findViewById(R.id.tvAdminRoomInventory);
            tvImageRef = itemView.findViewById(R.id.tvAdminRoomImageRef);
            btnEdit = itemView.findViewById(R.id.btnAdminRoomEdit);
            btnDelete = itemView.findViewById(R.id.btnAdminRoomDelete);
        }
    }
}
