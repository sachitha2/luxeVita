package com.example.ecostay.ui.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
        int fallbackRes = R.drawable.room_default;

        if (item.imageRef != null && !item.imageRef.trim().isEmpty()) {
            try {
                Uri uri = Uri.parse(item.imageRef);
                if ("content".equals(uri.getScheme()) || "file".equals(uri.getScheme())) {
                    holder.ivRoomImage.setImageURI(uri);
                } else {
                    int imageRes = holder.itemView.getContext().getResources()
                            .getIdentifier(item.imageRef, "drawable", holder.itemView.getContext().getPackageName());
                    holder.ivRoomImage.setImageResource(imageRes != 0 ? imageRes : fallbackRes);
                }
            } catch (Exception e) {
                holder.ivRoomImage.setImageResource(fallbackRes);
            }
        } else {
            holder.ivRoomImage.setImageResource(fallbackRes);
        }

        holder.card.setOnClickListener(v -> listener.onRoomClicked(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class RoomTypeViewHolder extends RecyclerView.ViewHolder {
        final CardView card;
        final ImageView ivRoomImage;
        final TextView tvName;
        final TextView tvDescription;
        final TextView tvPrice;

        RoomTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (CardView) itemView;
            ivRoomImage = itemView.findViewById(R.id.ivRoomImage);
            tvName = itemView.findViewById(R.id.tvRoomName);
            tvDescription = itemView.findViewById(R.id.tvRoomDescription);
            tvPrice = itemView.findViewById(R.id.tvPricePerNight);
        }
    }
}

