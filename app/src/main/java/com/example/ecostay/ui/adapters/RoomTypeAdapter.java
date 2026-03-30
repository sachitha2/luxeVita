package com.example.ecostay.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.RoomTypeEntity;

import java.util.ArrayList;
import java.util.List;

public class RoomTypeAdapter extends RecyclerView.Adapter<RoomTypeAdapter.RoomTypeViewHolder> {

    public interface OnRoomClickListener {
        void onRoomClicked(RoomTypeEntity roomType);
    }

    private final OnRoomClickListener listener;
    private final List<RoomTypeEntity> items = new ArrayList<>();

    public RoomTypeAdapter(OnRoomClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<RoomTypeEntity> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoomTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_type, parent, false);
        return new RoomTypeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomTypeViewHolder holder, int position) {
        RoomTypeEntity item = items.get(position);
        holder.tvName.setText(item.name);
        holder.tvDescription.setText(item.description);
        holder.tvPrice.setText("$" + item.pricePerNight + " / night");

        holder.card.setOnClickListener(v -> listener.onRoomClicked(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class RoomTypeViewHolder extends RecyclerView.ViewHolder {
        final CardView card;
        final TextView tvName;
        final TextView tvDescription;
        final TextView tvPrice;

        RoomTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (CardView) itemView;
            tvName = itemView.findViewById(R.id.tvRoomName);
            tvDescription = itemView.findViewById(R.id.tvRoomDescription);
            tvPrice = itemView.findViewById(R.id.tvPricePerNight);
        }
    }
}

