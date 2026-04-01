package com.example.lottery_legend.entrant;

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

public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder> {

    public interface OnReplyInteractionListener {
        void onReplyClicked(Comment comment);
        void onDeleteClicked(Comment comment);
    }

    private final Context context;
    private final String currentUserType;
    private final String deviceId;
    private final OnReplyInteractionListener listener;

    private final List<Comment> replies = new ArrayList<>();

    public ReplyAdapter(Context context,
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

            // 二级回复显示 @target
            if (comment.getThreadLevel() >= 2 && !TextUtils.isEmpty(comment.getReplyToUserNameSnapshot())) {
                textReplyToUser.setVisibility(View.VISIBLE);
                textReplyToUser.setText("@" + comment.getReplyToUserNameSnapshot());
                textReplyToUser.setTextColor(Color.parseColor("#2563EB"));
            } else {
                textReplyToUser.setVisibility(View.GONE);
            }

            // 阶梯式缩进修复：
            // parent 不动，这里只处理 reply item
            applyIndentation(comment);

            buttonReplyReply.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReplyClicked(comment);
                }
            });

            boolean canDelete = (!TextUtils.isEmpty(deviceId) && deviceId.equals(comment.getAuthorId()))
                    || "ORGANIZER".equals(currentUserType);

            if (buttonReplyDelete != null) {
                buttonReplyDelete.setVisibility(canDelete ? View.VISIBLE : View.VISIBLE);
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

            // 根据设计做适度阶梯，不要过大
            int startMarginDp;
            int lineStartDp;

            if (level <= 1) {
                startMarginDp = 0;
                lineStartDp = 4;
            } else {
                // level 2 整体后移一点
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

                // 一级和二级都显示线，但二级跟着缩进一起后移
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