package com.example.lottery_legend.entrant;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery_legend.R;
import com.example.lottery_legend.model.Notification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final List<Notification> notifications;
    private final OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(List<Notification> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification, listener);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvMessage, tvEventName, tvTime;
        private final View unreadDot, cardBackground;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvTime = itemView.findViewById(R.id.tvNotificationTime);
            unreadDot = itemView.findViewById(R.id.viewUnreadDot);
            cardBackground = itemView.findViewById(R.id.layoutNotificationCard);
        }

        public void bind(Notification notification, OnNotificationClickListener listener) {
            tvTitle.setText(notification.getTitle());
            tvMessage.setText(notification.getMessage());
            
            // Assuming we don't have event name in notification model directly, 
            // maybe we can hide it or use it for something else if available
            tvEventName.setVisibility(View.GONE); 

            if (notification.getCreatedAt() != null) {
                long now = System.currentTimeMillis();
                CharSequence timeSpan = DateUtils.getRelativeTimeSpanString(
                        notification.getCreatedAt().toDate().getTime(), 
                        now, 
                        DateUtils.MINUTE_IN_MILLIS);
                tvTime.setText(timeSpan);
            }

            unreadDot.setVisibility(notification.getIsRead() ? View.GONE : View.VISIBLE);
            
            // Optional: change background color for unread
            if (!notification.getIsRead()) {
                cardBackground.setBackgroundResource(R.drawable.bg_notification_card_unread);
            } else {
                cardBackground.setBackgroundResource(R.drawable.bg_notification_card);
            }

            itemView.setOnClickListener(v -> listener.onNotificationClick(notification));
        }
    }
}
