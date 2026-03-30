package com.example.ecostay.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.AttractionEntity;

import java.util.ArrayList;
import java.util.List;

public class AttractionAdapter extends RecyclerView.Adapter<AttractionAdapter.AttractionViewHolder> {

    private final List<AttractionEntity> items = new ArrayList<>();

    public void setItems(List<AttractionEntity> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AttractionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attraction, parent, false);
        return new AttractionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttractionViewHolder holder, int position) {
        AttractionEntity item = items.get(position);
        holder.tvTitle.setText(item.title);
        holder.tvLocation.setText(item.location);
        holder.tvDescription.setText(item.description);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AttractionViewHolder extends RecyclerView.ViewHolder {
        final CardView card;
        final TextView tvTitle;
        final TextView tvLocation;
        final TextView tvDescription;

        AttractionViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (CardView) itemView;
            tvTitle = itemView.findViewById(R.id.tvAttractionTitle);
            tvLocation = itemView.findViewById(R.id.tvAttractionLocation);
            tvDescription = itemView.findViewById(R.id.tvAttractionDescription);
        }
    }
}

