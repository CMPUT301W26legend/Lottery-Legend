package com.example.lottery_legend.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lottery_legend.R;
import com.example.lottery_legend.model.Notification;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying notification logs in the Admin section.
 */
public class AdminLogsAdapter extends RecyclerView.Adapter<AdminLogsAdapter.LogViewHolder> {

    private List<Notification> logList;
    private final OnLogClickListener listener;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault());

    public interface OnLogClickListener {
        void onLogClick(Notification log);
    }

    public AdminLogsAdapter(List<Notification> logList, OnLogClickListener listener) {
        this.logList = logList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log_admin, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        Notification log = logList.get(position);

        // Set placeholders
        holder.tvEventTitle.setText(log.getEventTitle() != null ? log.getEventTitle() : "Loading");
        holder.tvSenderName.setText(log.getSenderName() != null ? log.getSenderName() : "Loading");
        holder.tvReceiverGroup.setText(log.getReceiverGroup());
        
        if (log.getCreatedAt() != null) {
            holder.tvDate.setText(dateFormat.format(log.getCreatedAt().toDate()));
        }

        // Fetch Event Title if not already resolved
        if (log.getEventTitle() == null && log.getEventId() != null) {
            db.collection("events").document(log.getEventId()).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    log.setEventTitle(doc.getString("title"));
                    notifyItemChanged(holder.getAdapterPosition());
                }
            });
        }

        // Fetch Sender Name if not already resolved
        if (log.getSenderName() == null && log.getSenderId() != null) {
            db.collection("organizers").document(log.getSenderId()).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    log.setSenderName(doc.getString("name"));
                    notifyItemChanged(holder.getAdapterPosition());
                }
            });
        }

        holder.itemView.setOnClickListener(v -> listener.onLogClick(log));
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    public void updateList(List<Notification> newList) {
        this.logList = newList;
        notifyDataSetChanged();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventTitle, tvSenderName, tvReceiverGroup, tvDate;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventTitle = itemView.findViewById(R.id.log_event_title);
            tvSenderName = itemView.findViewById(R.id.log_sender_name);
            tvReceiverGroup = itemView.findViewById(R.id.log_receiver_group);
            tvDate = itemView.findViewById(R.id.log_date);
        }
    }
}
