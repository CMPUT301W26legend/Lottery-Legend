package com.example.lottery_legend.entrant;

import android.text.TextUtils;
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
import java.util.Locale;

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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
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

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvMessage;
        private final TextView tvEventName;
        private final TextView tvTime;
        private final View unreadDot;
        private final View cardBackground;

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
            String type = notification.getType() != null ? notification.getType() : "";
            String actionStatus = notification.getActionStatus() != null
                    ? notification.getActionStatus().trim().toUpperCase(Locale.ROOT)
                    : "";

            tvTitle.setText(resolveTitle(notification, type));
            tvMessage.setText(resolveMessage(notification, type, actionStatus));

            // 当前 Notification model 没有可靠持久化的 eventTitle，先隐藏
            tvEventName.setVisibility(View.GONE);

            if (notification.getCreatedAt() != null) {
                long now = System.currentTimeMillis();
                CharSequence timeSpan = DateUtils.getRelativeTimeSpanString(
                        notification.getCreatedAt().toDate().getTime(),
                        now,
                        DateUtils.MINUTE_IN_MILLIS
                );
                tvTime.setText(timeSpan);
            } else {
                tvTime.setText("");
            }

            unreadDot.setVisibility(notification.getIsRead() ? View.GONE : View.VISIBLE);

            if (!notification.getIsRead()) {
                cardBackground.setBackgroundResource(R.drawable.bg_notification_card_unread);
            } else {
                cardBackground.setBackgroundResource(R.drawable.bg_notification_card);
            }

            itemView.setOnClickListener(v -> listener.onNotificationClick(notification));
        }

        private static String resolveTitle(Notification notification, String type) {
            if (!TextUtils.isEmpty(notification.getTitle())) {
                return notification.getTitle();
            }

            switch (type) {
                case "LOTTERY_WIN":
                case "LOTTERY_LOSE":
                    return "Lottery Result";

                case "SIGN_UP_MESSAGE":
                    return "Sign-up Invitation";

                case "WAITLIST_MESSAGE":
                    return "Waiting List Update";

                case "SELECTED_MESSAGE":
                    return "Selected Group Message";

                case "CANCELLED_MESSAGE":
                    return "Cancellation Update";

                case "CO_ORGANIZER_INVITE":
                    return "Co-organizer Invitation";

                case "PRIVATE_INVITE":
                    return "Private Event Invitation";

                case "GENERIC_ANNOUNCEMENT":
                    return "General Notification";

                default:
                    return "Notification";
            }
        }

        private static String resolveMessage(Notification notification, String type, String actionStatus) {
            String originalMessage = notification.getMessage() != null ? notification.getMessage() : "";

            switch (type) {
                case "SIGN_UP_MESSAGE":
                case "PRIVATE_INVITE":
                case "CO_ORGANIZER_INVITE":
                    if ("ACCEPTED".equals(actionStatus)) {
                        return appendStatusIfNeeded(originalMessage, "Accepted");
                    }
                    if ("DECLINED".equals(actionStatus)) {
                        return appendStatusIfNeeded(originalMessage, "Declined");
                    }
                    return originalMessage;

                case "LOTTERY_WIN":
                case "LOTTERY_LOSE":
                case "WAITLIST_MESSAGE":
                case "SELECTED_MESSAGE":
                case "CANCELLED_MESSAGE":
                case "GENERIC_ANNOUNCEMENT":
                default:
                    return originalMessage;
            }
        }

        private static String appendStatusIfNeeded(String message, String status) {
            if (TextUtils.isEmpty(message)) {
                return status;
            }

            String suffix = " • " + status;
            if (message.endsWith(suffix) || message.endsWith(status)) {
                return message;
            }
            return message + suffix;
        }
    }
}