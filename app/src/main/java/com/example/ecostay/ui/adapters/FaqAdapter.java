package com.example.ecostay.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.FaqEntity;

import java.util.ArrayList;
import java.util.List;

public class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.ViewHolder> {

    private final List<FaqEntity> items = new ArrayList<>();
    private int expandedPosition = -1;

    public void setItems(List<FaqEntity> faqs) {
        items.clear();
        expandedPosition = -1;
        if (faqs != null) {
            items.addAll(faqs);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_faq, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FaqEntity faq = items.get(position);
        holder.tvQuestion.setText(faq.question);
        holder.tvAnswer.setText(faq.answer);

        boolean expanded = position == expandedPosition;
        holder.tvAnswer.setVisibility(expanded ? View.VISIBLE : View.GONE);
        holder.dividerAnswer.setVisibility(expanded ? View.VISIBLE : View.GONE);
        holder.ivExpand.setRotation(expanded ? 180f : 0f);

        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) {
                return;
            }
            int previous = expandedPosition;
            expandedPosition = expandedPosition == adapterPosition ? -1 : adapterPosition;
            if (previous != -1) {
                notifyItemChanged(previous);
            }
            notifyItemChanged(adapterPosition);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvQuestion;
        final TextView tvAnswer;
        final View dividerAnswer;
        final ImageView ivExpand;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            tvAnswer = itemView.findViewById(R.id.tvAnswer);
            dividerAnswer = itemView.findViewById(R.id.dividerAnswer);
            ivExpand = itemView.findViewById(R.id.ivExpand);
        }
    }
}
