package com.example.eknjiznica.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eknjiznica.R;
import com.example.eknjiznica.models.Reservation;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {
    private List<Reservation> reservations;
    private boolean isLibrarian;
    private OnApproveClickListener approveListener;

    public interface OnApproveClickListener {
        void onApproveClick(Reservation reservation);
    }

    public ReservationAdapter(List<Reservation> reservations, boolean isLibrarian) {
        this.reservations = reservations;
        this.isLibrarian = isLibrarian;
    }

    public void setOnApproveClickListener(OnApproveClickListener listener) {
        this.approveListener = listener;
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        Reservation reservation = reservations.get(position);
        
        if (reservation.getBook() != null) {
            holder.tvBookTitle.setText(reservation.getBook().getTitle());
            holder.tvAuthor.setText("Author: " + reservation.getBook().getAuthor());
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        if (reservation.getReservationDate() != null) {
            holder.tvReservationDate.setText("Reserved: " + sdf.format(reservation.getReservationDate()));
        }
        
        if (reservation.getExpiryDate() != null) {
            holder.tvExpiryDate.setText("Expires: " + sdf.format(reservation.getExpiryDate()));
        }
        
        holder.tvStatus.setText(reservation.isApproved() ? "Approved" : "Pending");
        holder.tvStatus.setTextColor(reservation.isApproved() ?
            holder.itemView.getContext().getColor(android.R.color.holo_green_dark) :
            holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));

        if (isLibrarian && !reservation.isApproved()) {
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnApprove.setOnClickListener(v -> {
                if (approveListener != null) {
                    approveListener.onApproveClick(reservation);
                }
            });
        } else {
            holder.btnApprove.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    static class ReservationViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookTitle, tvAuthor, tvReservationDate, tvExpiryDate, tvStatus;
        Button btnApprove;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvReservationDate = itemView.findViewById(R.id.tvReservationDate);
            tvExpiryDate = itemView.findViewById(R.id.tvExpiryDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnApprove = itemView.findViewById(R.id.btnApprove);
        }
    }
}
