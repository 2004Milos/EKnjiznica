package com.example.eknjiznica.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eknjiznica.R;
import com.example.eknjiznica.models.Book;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private List<Book> books;
    private boolean isLibrarian;
    private OnBookClickListener listener;

    public interface OnBookClickListener {
        void onBookClick(Book book);
        void onReserveClick(Book book);
    }

    public BookAdapter(List<Book> books, boolean isLibrarian, OnBookClickListener listener) {
        this.books = books;
        this.isLibrarian = isLibrarian;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.tvTitle.setText(book.getTitle());
        holder.tvAuthor.setText("Author: " + book.getAuthor());
        holder.tvYear.setText("Year: " + book.getYear());
        holder.tvGenre.setText("Genre: " + book.getGenre());
        holder.tvAvailable.setText(book.isAvailable() ? "Available" : "Not Available");
        holder.tvAvailable.setTextColor(book.isAvailable() ? 
            holder.itemView.getContext().getColor(android.R.color.holo_green_dark) : 
            holder.itemView.getContext().getColor(android.R.color.holo_red_dark));

        // Show reserve button only for members and if book is available
        if (!isLibrarian && book.isAvailable()) {
            holder.btnReserve.setVisibility(View.VISIBLE);
            holder.btnReserve.setOnClickListener(v -> listener.onReserveClick(book));
        } else {
            holder.btnReserve.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onBookClick(book));
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthor, tvYear, tvGenre, tvAvailable;
        Button btnReserve;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvYear = itemView.findViewById(R.id.tvYear);
            tvGenre = itemView.findViewById(R.id.tvGenre);
            tvAvailable = itemView.findViewById(R.id.tvAvailable);
            btnReserve = itemView.findViewById(R.id.btnReserve);
        }
    }
}
