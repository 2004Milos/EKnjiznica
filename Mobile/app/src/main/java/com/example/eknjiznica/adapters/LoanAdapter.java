package com.example.eknjiznica.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eknjiznica.R;
import com.example.eknjiznica.models.Loan;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LoanAdapter extends RecyclerView.Adapter<LoanAdapter.LoanViewHolder> {
    private List<Loan> loans;
    private boolean isLibrarian;

    public LoanAdapter(List<Loan> loans, boolean isLibrarian) {
        this.loans = loans;
        this.isLibrarian = isLibrarian;
    }

    @NonNull
    @Override
    public LoanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_loan, parent, false);
        return new LoanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LoanViewHolder holder, int position) {
        Loan loan = loans.get(position);
        
        if (loan.getBook() != null) {
            holder.tvBookTitle.setText(loan.getBook().getTitle());
            holder.tvAuthor.setText("Author: " + loan.getBook().getAuthor());
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        if (loan.getLoanDate() != null) {
            holder.tvLoanDate.setText("Loan Date: " + sdf.format(loan.getLoanDate()));
        }
        
        if (loan.getDueDate() != null) {
            holder.tvDueDate.setText("Due Date: " + sdf.format(loan.getDueDate()));
        }
        
        if (loan.getReturnDate() != null) {
            holder.tvReturnDate.setText("Return Date: " + sdf.format(loan.getReturnDate()));
            holder.tvReturnDate.setVisibility(View.VISIBLE);
        } else {
            holder.tvReturnDate.setVisibility(View.GONE);
        }
        
        holder.tvStatus.setText("Status: " + loan.getStatus());
        if ("Overdue".equals(loan.getStatus())) {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_red_dark));
        } else if ("Active".equals(loan.getStatus())) {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
        } else {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(android.R.color.darker_gray));
        }
    }

    @Override
    public int getItemCount() {
        return loans.size();
    }

    static class LoanViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookTitle, tvAuthor, tvLoanDate, tvDueDate, tvReturnDate, tvStatus;

        public LoanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvLoanDate = itemView.findViewById(R.id.tvLoanDate);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            tvReturnDate = itemView.findViewById(R.id.tvReturnDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
