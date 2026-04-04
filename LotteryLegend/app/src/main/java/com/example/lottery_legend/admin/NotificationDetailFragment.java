package com.example.lottery_legend.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.lottery_legend.R;
import com.example.lottery_legend.model.Notification;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * DialogFragment that displays the full details of a specific notification log.
 */
public class NotificationDetailFragment extends DialogFragment {

    private static final String ARG_LOG = "notification_log";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault());

    public static NotificationDetailFragment newInstance(Notification log) {
        NotificationDetailFragment fragment = new NotificationDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_LOG, log);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification_detail, container, false);

        Notification log = (Notification) getArguments().getSerializable(ARG_LOG);

        TextView tvEventTitle = view.findViewById(R.id.detail_event_title);
        TextView tvSenderName = view.findViewById(R.id.detail_sender_name);
        TextView tvReceiverGroup = view.findViewById(R.id.detail_receiver_group);
        TextView tvDateTime = view.findViewById(R.id.detail_date_time);
        TextView tvMsgTitle = view.findViewById(R.id.detail_msg_title);
        TextView tvMsgBody = view.findViewById(R.id.detail_msg_body);
        ImageView btnClose = view.findViewById(R.id.btn_close_detail);

        if (log != null) {
            tvEventTitle.setText(log.getEventTitle() != null ? log.getEventTitle() : "Unknown Event");
            tvSenderName.setText(log.getSenderName() != null ? log.getSenderName() : "Unknown Sender");
            tvReceiverGroup.setText(log.getReceiverGroup());
            
            if (log.getCreatedAt() != null) {
                tvDateTime.setText(dateFormat.format(log.getCreatedAt().toDate()));
            }

            tvMsgTitle.setText(log.getTitle());
            tvMsgBody.setText(log.getMessage());
        }

        btnClose.setOnClickListener(v -> dismiss());

        return view;
    }
}
