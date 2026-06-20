package com.example.ecostay.ui.admin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.model.AdminSupportMessageSummary;

import java.util.ArrayList;
import java.util.List;

public class AdminSupportMessageAdapter extends RecyclerView.Adapter<AdminSupportMessageAdapter.ViewHolder> {

    public interface Listener {
        void onMessageClick(AdminSupportMessageSummary message);
    }

    private final List<AdminSupportMessageSummary> items = new ArrayList<>();
    private final Listener listener;

    public AdminSupportMessageAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<AdminSupportMessageSummary> messages) {
        items.clear();
        if (messages != null) {
            items.addAll(messages);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_support_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminSupportMessageSummary message = items.get(position);
        holder.tvSender.setText(holder.itemView.getContext().getString(
                R.string.label_message_from, message.userName));
        holder.tvDate.setText(holder.itemView.getContext().getString(
                R.string.label_message_date, message.createdAt));
        holder.tvMessagePreview.setText(message.message);
        holder.itemView.setOnClickListener(v -> listener.onMessageClick(message));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvSender;
        final TextView tvDate;
        final TextView tvMessagePreview;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSender = itemView.findViewById(R.id.tvSender);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvMessagePreview = itemView.findViewById(R.id.tvMessagePreview);
        }
    }
}
