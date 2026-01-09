package com.example.eknjiznica.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eknjiznica.R;
import com.example.eknjiznica.models.Review;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<Review> reviews;
    private String currentUserId;
    private OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Review review);
    }

    public ReviewAdapter(List<Review> reviews, String currentUserId) {
        this.reviews = reviews;
        this.currentUserId = currentUserId;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        
        // Try to get user email from review object (if API includes it)
        String userEmail = "User";
        try {
            // If review has a user object with email, use it
            if (review.getUser() != null) {
                Object userObj = review.getUser();
                if (userObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> userMap = (Map<String, Object>) userObj;
                    Object emailObj = userMap.get("email");
                    if (emailObj != null) {
                        userEmail = emailObj.toString();
                    }
                }
            }
        } catch (Exception e) {
            // Fallback to userId if email not available
            userEmail = review.getUserId() != null ? review.getUserId().substring(0, Math.min(8, review.getUserId().length())) + "..." : "User";
        }
        
        holder.tvUserEmail.setText(userEmail);
        holder.tvRating.setText("Rating: " + review.getRating() + "/5");
        
        // Display stars
        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (i <= review.getRating()) {
                stars.append("★");
            } else {
                stars.append("☆");
            }
        }
        holder.tvStars.setText(stars.toString());
        
        if (review.getComment() != null && !review.getComment().isEmpty()) {
            holder.tvComment.setText(review.getComment());
            holder.tvComment.setVisibility(View.VISIBLE);
        } else {
            holder.tvComment.setVisibility(View.GONE);
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        if (review.getReviewDate() != null) {
            holder.tvDate.setText(sdf.format(review.getReviewDate()));
        }
        
        // Show delete button only for own reviews
        if (currentUserId != null && currentUserId.equals(review.getUserId())) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(review);
                }
            });
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserEmail, tvRating, tvStars, tvComment, tvDate;
        Button btnDelete;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvStars = itemView.findViewById(R.id.tvStars);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
