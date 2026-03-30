package com.example.ecostay.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.OfferEntity;

import java.util.ArrayList;
import java.util.List;

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.OfferViewHolder> {

    private final List<OfferEntity> items = new ArrayList<>();

    public void setItems(List<OfferEntity> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_offer, parent, false);
        return new OfferViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
        OfferEntity offer = items.get(position);
        holder.tvTitle.setText(offer.title);
        holder.tvDesc.setText(offer.description);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OfferViewHolder extends RecyclerView.ViewHolder {
        final CardView card;
        final TextView tvTitle;
        final TextView tvDesc;

        OfferViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (CardView) itemView;
            tvTitle = itemView.findViewById(R.id.tvOfferTitle);
            tvDesc = itemView.findViewById(R.id.tvOfferDescription);
        }
    }
}

