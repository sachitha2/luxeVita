package com.example.ecostay.ui.admin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostay.R;
import com.example.ecostay.data.model.AdminBookingSummary;
import com.example.ecostay.util.StatusUiUtils;

import java.util.ArrayList;
import java.util.List;

public class AdminBookingAdapter extends RecyclerView.Adapter<AdminBookingAdapter.ViewHolder> {

    public interface Listener {
        void onBookingClick(AdminBookingSummary booking);
    }

    private final List<AdminBookingSummary> items = new ArrayList<>();
    private final Listener listener;

    public AdminBookingAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<AdminBookingSummary> bookings) {
        items.clear();
        if (bookings != null) {
            items.addAll(bookings);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminBookingSummary booking = items.get(position);
        holder.tvBookingId.setText(holder.itemView.getContext().getString(
                R.string.label_booking_id, booking.bookingId));
        holder.tvCustomer.setText(holder.itemView.getContext().getString(
                R.string.label_customer, booking.customerName));
        holder.tvServiceName.setText(booking.serviceName);
        holder.tvDevice.setText(booking.deviceLabel);
        StatusUiUtils.applyStatusChip(holder.tvStatus, booking.status);
        holder.tvTechnician.setText(holder.itemView.getContext().getString(
                R.string.label_technician, booking.technicianName));
        holder.tvSchedule.setText(holder.itemView.getContext().getString(
                R.string.label_preferred_schedule, booking.preferredDate, booking.preferredTime));
        holder.tvServiceMethod.setText(holder.itemView.getContext().getString(
                R.string.label_service_method, booking.serviceMethod));
        holder.itemView.setOnClickListener(v -> listener.onBookingClick(booking));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvBookingId;
        final TextView tvCustomer;
        final TextView tvServiceName;
        final TextView tvDevice;
        final TextView tvStatus;
        final TextView tvTechnician;
        final TextView tvSchedule;
        final TextView tvServiceMethod;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookingId = itemView.findViewById(R.id.tvBookingId);
            tvCustomer = itemView.findViewById(R.id.tvCustomer);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvDevice = itemView.findViewById(R.id.tvDevice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTechnician = itemView.findViewById(R.id.tvTechnician);
            tvSchedule = itemView.findViewById(R.id.tvSchedule);
            tvServiceMethod = itemView.findViewById(R.id.tvServiceMethod);
        }
    }
}
