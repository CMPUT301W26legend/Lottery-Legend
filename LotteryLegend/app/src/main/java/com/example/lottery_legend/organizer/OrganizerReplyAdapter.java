package com.example.lottery_legend.organizer;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery_legend.R;
import com.example.lottery_legend.model.Comment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying replies in a comment thread for Organizers.
 * Differs from entrant version by always allowing deletion of any reply.
 */
public class OrganizerReplyAdapter extends RecyclerView.Adapter<OrganizerReplyAdapter.ReplyViewHolder> {

    public interface OnReplyInteractionListener {
        void onReplyClicked(Comment comment);
        void onDeleteClicked(Comment comment);
    }

    private final Context context;
    private final String currentUserType;
    private final String deviceId;
    private final OnReplyInteractionListener listener;

    private final List<Comment> replies = new ArrayList<>();

    public OrganizerReplyAdapter(Context context,
                                String currentUserType,
                                String deviceId,
                                OnReplyInteractionListener listener) {
        this.context = context;
        this.currentUserType = currentUserType;
        this.deviceId = deviceId;
        this.listener = listener;
    }

    public void setReplies(List<Comment> newReplies) {
        replies.clear();
        if (newReplies != null) {
            replies.addAll(newReplies);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reply_comment, parent, false);
        return new ReplyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyViewHolder holder, int position) {
        holder.bind(replies.get(position));
    }

    @Override
    public int getItemCount() {
        return replies.size();
    }

    class ReplyViewHolder extends RecyclerView.ViewHolder {

        private final View layoutReplyRoot;
        private final View viewThreadLine;
        private final TextView textReplyAuthorName;
        private final TextView textReplyTime;
        private final TextView textReplyContent;
        private final TextView textReplyToUser;
        private final TextView buttonReplyReply;
        private final TextView buttonReplyDelete;

        ReplyViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutReplyRoot = itemView.findViewById(R.id.layoutReplyRoot);
            viewThreadLine = itemView.findViewById(R.id.viewThreadLine);
            textReplyAuthorName = itemView.findViewById(R.id.textReplyAuthorName);
            textReplyTime = itemView.findViewById(R.id.textReplyTime);
            textReplyContent = itemView.findViewById(R.id.textReplyContent);
            textReplyToUser = itemView.findViewById(R.id.textReplyToUser);
            buttonReplyReply = itemView.findViewById(R.id.buttonReplyReply);
            buttonReplyDelete = itemView.findViewById(R.id.buttonReplyDelete);
        }

        void bind(Comment comment) {
            textReplyAuthorName.setText(comment.getAuthorNameSnapshot());

            if (comment.getCreatedAt() != null) {
                textReplyTime.setText(
                        new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                                .format(comment.getCreatedAt().toDate())
                );
            } else {
                textReplyTime.setText("");
            }

            textReplyContent.setText(comment.getContent());

            if (comment.getThreadLevel() >= 2 && !TextUtils.isEmpty(comment.getReplyToUserNameSnapshot())) {
                textReplyToUser.setVisibility(View.VISIBLE);
                textReplyToUser.setText("@" + comment.getReplyToUserNameSnapshot());
                textReplyToUser.setTextColor(Color.parseColor("#2563EB"));
            } else {
                textReplyToUser.setVisibility(View.GONE);
            }

            applyIndentation(comment);

            buttonReplyReply.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReplyClicked(comment);
                }
            });

            // Organizer can always delete any reply
            if (buttonReplyDelete != null) {
                buttonReplyDelete.setVisibility(View.VISIBLE);
                buttonReplyDelete.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDeleteClicked(comment);
                    }
                });
            }
        }

        private void applyIndentation(Comment comment) {
            ViewGroup.MarginLayoutParams rootParams =
                    (ViewGroup.MarginLayoutParams) layoutReplyRoot.getLayoutParams();

            int level = comment.getThreadLevel();

            int startMarginDp;
            int lineStartDp;

            if (level <= 1) {
                startMarginDp = 0;
                lineStartDp = 4;
            } else {
                startMarginDp = 22;
                lineStartDp = 16;
            }

            rootParams.setMarginStart(dp(startMarginDp));
            layoutReplyRoot.setLayoutParams(rootParams);

            if (viewThreadLine != null) {
                ViewGroup.MarginLayoutParams lineParams =
                        (ViewGroup.MarginLayoutParams) viewThreadLine.getLayoutParams();
                lineParams.setMarginStart(dp(lineStartDp));
                viewThreadLine.setLayoutParams(lineParams);
                viewThreadLine.setVisibility(View.VISIBLE);
            }
        }

        private int dp(int value) {
            return (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    value,
                    context.getResources().getDisplayMetrics()
            );
        }
    }
}
