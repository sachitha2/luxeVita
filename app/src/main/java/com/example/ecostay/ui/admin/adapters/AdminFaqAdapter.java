package com.example.ecostay.ui.admin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.entity.FaqEntity;

import java.util.ArrayList;
import java.util.List;

public class AdminFaqAdapter extends RecyclerView.Adapter<AdminFaqAdapter.ViewHolder> {

    public interface Listener {
        void onEdit(FaqEntity faq);

        void onDelete(FaqEntity faq);
    }

    private final List<FaqEntity> items = new ArrayList<>();
    private final Listener listener;

    public AdminFaqAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<FaqEntity> faqs) {
        items.clear();
        if (faqs != null) {
            items.addAll(faqs);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_faq, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FaqEntity faq = items.get(position);
        holder.tvQuestion.setText(faq.question);
        holder.tvAnswer.setText(faq.answer);
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(faq));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(faq));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvQuestion;
        final TextView tvAnswer;
        final Button btnEdit;
        final Button btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            tvAnswer = itemView.findViewById(R.id.tvAnswer);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
