package com.example.lottery_legend.entrant;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Base64;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery_legend.R;
import com.example.lottery_legend.model.Comment;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying replies in a comment thread.
 */
public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder> {

    /**
     * Interface for handling interactions with individual replies.
     */
    public interface OnReplyInteractionListener {
        /**
         * Triggered when the reply button is clicked on a comment.
         * @param comment The comment being replied to.
         */
        void onReplyClicked(Comment comment);
        /**
         * Triggered when the delete button is clicked on a comment.
         * @param comment The comment to be deleted.
         */
        void onDeleteClicked(Comment comment);
    }

    private final Context context;
    private final String currentUserType;
    private final String deviceId;
    private final boolean isAdmin;
    private final OnReplyInteractionListener listener;
    private final FirebaseFirestore db;

    private final List<Comment> replies = new ArrayList<>();

    /**
     * Constructor for ReplyAdapter.
     * @param context         The activity context.
     * @param currentUserType The role of the current user (ENTRANT/ORGANIZER).
     * @param deviceId        The unique device ID.
     * @param isAdmin         Whether the user has admin privileges.
     * @param listener        Listener for reply and delete actions.
     */
    public ReplyAdapter(Context context,
                        String currentUserType,
                        String deviceId,
                        boolean isAdmin,
                        OnReplyInteractionListener listener) {
        this.context = context;
        this.currentUserType = currentUserType;
        this.deviceId = deviceId;
        this.isAdmin = isAdmin;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Updates the list of replies and refreshes the RecyclerView.
     * @param newReplies The new list of replies.
     */
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

    /**
     * ViewHolder for individual reply items.
     */
    class ReplyViewHolder extends RecyclerView.ViewHolder {

        private final View layoutReplyRoot;
        private final View viewThreadLine;
        private final ImageView imageReplyAvatar;
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
            imageReplyAvatar = itemView.findViewById(R.id.imageReplyAvatar);
            textReplyAuthorName = itemView.findViewById(R.id.textReplyAuthorName);
            textReplyTime = itemView.findViewById(R.id.textReplyTime);
            textReplyContent = itemView.findViewById(R.id.textReplyContent);
            textReplyToUser = itemView.findViewById(R.id.textReplyToUser);
            buttonReplyReply = itemView.findViewById(R.id.buttonReplyReply);
            buttonReplyDelete = itemView.findViewById(R.id.buttonReplyDelete);
        }

        /**
         * Binds comment data to the view.
         * @param comment The comment to bind.
         */
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

            // Load author avatar
            loadAuthorAvatar(comment, imageReplyAvatar);

            // Display @target for nested replies
            if (comment.getThreadLevel() >= 2 && !TextUtils.isEmpty(comment.getReplyToUserNameSnapshot())) {
                textReplyToUser.setVisibility(View.VISIBLE);
                textReplyToUser.setText("@" + comment.getReplyToUserNameSnapshot());
                textReplyToUser.setTextColor(Color.parseColor("#2563EB"));
            } else {
                textReplyToUser.setVisibility(View.GONE);
            }

            // Apply visual indentation based on nesting level
            applyIndentation(comment);

            buttonReplyReply.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReplyClicked(comment);
                }
            });

            // Check if the current user can delete this comment
            boolean canDelete = isAdmin || (!TextUtils.isEmpty(deviceId) && deviceId.equals(comment.getAuthorId()));

            if (buttonReplyDelete != null) {
                buttonReplyDelete.setVisibility(canDelete ? View.VISIBLE : View.GONE);
                buttonReplyDelete.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDeleteClicked(comment);
                    }
                });
            }
        }

        /**
         * Fetches the author's avatar from Firestore and displays it.
         */
        private void loadAuthorAvatar(Comment comment, ImageView imageView) {
            String collection = "ORGANIZER".equalsIgnoreCase(comment.getAuthorType()) ? "organizers" : "entrants";
            db.collection(collection).document(comment.getAuthorId()).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String profileImage = doc.getString("profileImage");
                    if (profileImage != null && !profileImage.isEmpty()) {
                        displayBase64Image(profileImage, imageView);
                    } else {
                        imageView.setImageResource(R.drawable.ic_profile_avatar);
                    }
                } else {
                    imageView.setImageResource(R.drawable.ic_profile_avatar);
                }
            }).addOnFailureListener(e -> imageView.setImageResource(R.drawable.ic_profile_avatar));
        }

        /**
         * Decodes a Base64 string into a Bitmap and sets it to an ImageView.
         */
        private void displayBase64Image(String base64, ImageView imageView) {
            try {
                byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (bitmap != null && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            } catch (Exception ignored) {
            }
        }

        /**
         * Adjusts the horizontal margin and thread line based on nesting level.
         */
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
                // Shift level 2 comments slightly to the right
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

                // Show thread line for all replies
                viewThreadLine.setVisibility(View.VISIBLE);
            }
        }

        /**
         * Converts DP to pixels.
         */
        private int dp(int value) {
            return (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    value,
                    context.getResources().getDisplayMetrics()
            );
        }
    }
}
