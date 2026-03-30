package com.example.ecostay.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;

import java.util.ArrayList;
import java.util.List;

public class DateOptionsAdapter extends RecyclerView.Adapter<DateOptionsAdapter.DateOptionViewHolder> {

    public interface OnDateSelectedListener {
        void onDateSelected(DateOption option);
    }

    public static class DateOption {
        public final long epochDay;
        public final String label;
        public final boolean isAvailable;
        public final boolean isBookedByUser;

        public DateOption(long epochDay, String label, boolean isAvailable, boolean isBookedByUser) {
            this.epochDay = epochDay;
            this.label = label;
            this.isAvailable = isAvailable;
            this.isBookedByUser = isBookedByUser;
        }
    }

    private final OnDateSelectedListener listener;
    private final List<DateOption> items = new ArrayList<>();
    private int selectedPosition = -1;

    public DateOptionsAdapter(OnDateSelectedListener listener) {
        this.listener = listener;
    }

    public void setItems(List<DateOption> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public long getSelectedEpochDay() {
        if (selectedPosition < 0 || selectedPosition >= items.size()) return -1L;
        return items.get(selectedPosition).epochDay;
    }

    public void setSelectedEpochDay(long epochDay) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).epochDay == epochDay) {
                selectedPosition = i;
                notifyDataSetChanged();
                return;
            }
        }
    }

    @NonNull
    @Override
    public DateOptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_option, parent, false);
        return new DateOptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateOptionViewHolder holder, int position) {
        DateOption option = items.get(position);
        holder.tv.setText(option.label);

        boolean selected = position == selectedPosition;
        holder.tv.setAlpha(selected ? 1.0f : 0.65f);
        holder.tv.setTextSize(selected ? 14f : 13f);

        holder.itemView.setOnClickListener(v -> {
            selectedPosition = position;
            notifyDataSetChanged();
            if (listener != null) listener.onDateSelected(option);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class DateOptionViewHolder extends RecyclerView.ViewHolder {
        final TextView tv;

        DateOptionViewHolder(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tvDateOption);
        }
    }
}

