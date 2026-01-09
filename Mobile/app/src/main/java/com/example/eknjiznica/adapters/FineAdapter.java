package com.example.eknjiznica.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eknjiznica.R;
import com.example.eknjiznica.models.Fine;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class FineAdapter extends RecyclerView.Adapter<FineAdapter.FineViewHolder> {
    private List<Fine> fines;
    private boolean isLibrarian;
    private OnMarkPaidClickListener markPaidListener;

    public interface OnMarkPaidClickListener {
        void onMarkPaidClick(Fine fine);
    }

    public void setOnMarkPaidClickListener(OnMarkPaidClickListener listener) {
        this.markPaidListener = listener;
    }

    public FineAdapter(List<Fine> fines, boolean isLibrarian) {
        this.fines = fines;
        this.isLibrarian = isLibrarian;
    }

    @NonNull
    @Override
    public FineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fine, parent, false);
        return new FineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FineViewHolder holder, int position) {
        Fine fine = fines.get(position);
        
        holder.tvAmount.setText(String.format(Locale.getDefault(), "Amount: %.2f EUR", fine.getAmount()));
        holder.tvReason.setText("Reason: " + fine.getReason());
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        if (fine.getIssueDate() != null) {
            holder.tvIssueDate.setText("Issue Date: " + sdf.format(fine.getIssueDate()));
        }
        
        if (fine.getPaidDate() != null) {
            holder.tvPaidDate.setText("Paid Date: " + sdf.format(fine.getPaidDate()));
            holder.tvPaidDate.setVisibility(View.VISIBLE);
        } else {
            holder.tvPaidDate.setVisibility(View.GONE);
        }
        
        holder.tvStatus.setText(fine.isPaid() ? "Paid" : "Unpaid");
        holder.tvStatus.setTextColor(fine.isPaid() ?
            holder.itemView.getContext().getColor(android.R.color.holo_green_dark) :
            holder.itemView.getContext().getColor(android.R.color.holo_red_dark));

        if (isLibrarian && !fine.isPaid()) {
            holder.btnMarkPaid.setVisibility(View.VISIBLE);
            holder.btnMarkPaid.setOnClickListener(v -> {
                if (markPaidListener != null) {
                    markPaidListener.onMarkPaidClick(fine);
                }
            });
        } else {
            holder.btnMarkPaid.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return fines.size();
    }

    static class FineViewHolder extends RecyclerView.ViewHolder {
        TextView tvAmount, tvReason, tvIssueDate, tvPaidDate, tvStatus;
        Button btnMarkPaid;

        public FineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvIssueDate = itemView.findViewById(R.id.tvIssueDate);
            tvPaidDate = itemView.findViewById(R.id.tvPaidDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnMarkPaid = itemView.findViewById(R.id.btnMarkPaid);
        }
    }
}
