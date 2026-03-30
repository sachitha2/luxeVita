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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookingListAdapter extends RecyclerView.Adapter<BookingListAdapter.BookingViewHolder> {

    public interface OnBookingClickListener {
        void onBookingClicked(Item item);
    }

    public static class Item {
        public long bookingId;
        public String bookingType;
        public String title;
        public String scheduleText;
        public String status;
        public String paymentStatus;
        public double totalAmount;
    }

    private final OnBookingClickListener listener;
    private final List<Item> items = new ArrayList<>();

    public BookingListAdapter(OnBookingClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Item> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_manage, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Item item = items.get(position);
        holder.tvTitle.setText(item.title);
        holder.tvBookingId.setText("Booking #" + item.bookingId);
        holder.tvType.setText(item.bookingType);
        holder.tvSchedule.setText(item.scheduleText);
        holder.tvStatus.setText(item.status);
        holder.tvPaymentStatus.setText(item.paymentStatus);
        holder.tvAmount.setText(String.format(Locale.getDefault(), "$%.2f", item.totalAmount));

        holder.card.setOnClickListener(v -> listener.onBookingClicked(item));
        holder.btnViewDetails.setOnClickListener(v -> listener.onBookingClicked(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        final CardView card;
        final TextView tvTitle;
        final TextView tvBookingId;
        final TextView tvType;
        final TextView tvSchedule;
        final TextView tvStatus;
        final TextView tvPaymentStatus;
        final TextView tvAmount;
        final Button btnViewDetails;

        BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (CardView) itemView;
            tvTitle = itemView.findViewById(R.id.tvManageBookingTitle);
            tvBookingId = itemView.findViewById(R.id.tvManageBookingId);
            tvType = itemView.findViewById(R.id.tvManageBookingType);
            tvSchedule = itemView.findViewById(R.id.tvManageBookingSchedule);
            tvStatus = itemView.findViewById(R.id.tvManageBookingStatus);
            tvPaymentStatus = itemView.findViewById(R.id.tvManageBookingPaymentStatus);
            tvAmount = itemView.findViewById(R.id.tvManageBookingAmount);
            btnViewDetails = itemView.findViewById(R.id.btnManageBookingDetails);
        }
    }
}
