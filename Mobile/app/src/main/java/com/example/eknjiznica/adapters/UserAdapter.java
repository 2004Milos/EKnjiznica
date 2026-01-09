package com.example.eknjiznica.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eknjiznica.R;

import java.util.List;
import java.util.Map;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<Map<String, Object>> users;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onDeleteClick(Map<String, Object> user);
    }

    public UserAdapter(List<Map<String, Object>> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Map<String, Object> user = users.get(position);
        
        String email = (String) user.get("email");
        holder.tvEmail.setText(email != null ? email : "Unknown");
        
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) user.get("roles");
        if (roles != null && !roles.isEmpty()) {
            holder.tvRole.setText("Role: " + roles.get(0));
        } else {
            holder.tvRole.setText("Role: Unknown");
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmail, tvRole;
        Button btnDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvRole = itemView.findViewById(R.id.tvRole);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
