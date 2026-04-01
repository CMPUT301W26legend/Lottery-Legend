package com.example.lottery_legend.organizer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery_legend.R;
import com.example.lottery_legend.model.Comment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrganizerCommentsActivity extends AppCompatActivity {

    private String eventId;
    private String deviceId;
    private String authorName;
    private String authorType = "ORGANIZER";

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private CommentsAdapter adapter;
    private EditText editComment;
    private View btnSend;

    private Comment replyingTo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_comments);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("eventId");
        deviceId = getIntent().getStringExtra("deviceId");
        authorName = getIntent().getStringExtra("authorName");

        setupViews();
        loadComments();
    }

    private void setupViews() {
        Toolbar toolbar = findViewById(R.id.toolbarComments);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewComments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommentsAdapter();
        recyclerView.setAdapter(adapter);

        editComment = findViewById(R.id.editTextComment);
        btnSend = findViewById(R.id.buttonSendComment);

        btnSend.setOnClickListener(v -> {
            if (replyingTo != null) {
                postReply();
            } else {
                postComment();
            }
        });
    }

    private void loadComments() {
        db.collection("events").document(eventId)
                .collection("comments")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading comments", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        List<Comment> allComments = value.toObjects(Comment.class);
                        List<Comment> parentComments = new ArrayList<>();
                        for (Comment c : allComments) {
                            if (c.getThreadLevel() == 0) {
                                parentComments.add(c);
                            }
                        }
                        adapter.setComments(parentComments);
                    }
                });
    }

    private void postComment() {
        String content = editComment.getText().toString().trim();
        if (TextUtils.isEmpty(content)) return;

        DocumentReference ref = db.collection("events").document(eventId).collection("comments").document();
        Comment comment = new Comment();
        comment.setCommentId(ref.getId());
        comment.setAuthorId(deviceId);
        comment.setAuthorType(authorType);
        comment.setAuthorNameSnapshot(authorName);
        comment.setContent(content);
        comment.setCreatedAt(Timestamp.now());
        comment.setUpdatedAt(Timestamp.now());
        comment.setThreadLevel(0);
        comment.setReactionCount(0);
        comment.setReplyCount(0);
        comment.setReactionTypeCounts(new HashMap<>());

        ref.set(comment).addOnSuccessListener(aVoid -> {
            editComment.setText("");
            recyclerView.postDelayed(() -> {
                if (adapter.getItemCount() > 0) {
                    recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                }
            }, 300);
        });
    }

    private void postReply() {
        String content = editComment.getText().toString().trim();
        if (TextUtils.isEmpty(content)) return;

        DocumentReference ref = db.collection("events").document(eventId).collection("comments").document();
        Comment reply = new Comment();
        reply.setCommentId(ref.getId());
        reply.setAuthorId(deviceId);
        reply.setAuthorType(authorType);
        reply.setAuthorNameSnapshot(authorName);
        reply.setContent(content);
        reply.setCreatedAt(Timestamp.now());
        reply.setUpdatedAt(Timestamp.now());
        reply.setParentCommentId(replyingTo.getCommentId());
        reply.setRootCommentId(replyingTo.getRootCommentId() != null ? replyingTo.getRootCommentId() : replyingTo.getCommentId());
        reply.setThreadLevel(replyingTo.getThreadLevel() + 1);
        reply.setReactionCount(0);
        reply.setReplyCount(0);
        reply.setReactionTypeCounts(new HashMap<>());

        WriteBatch batch = db.batch();
        batch.set(ref, reply);
        
        DocumentReference parentRef = db.collection("events").document(eventId)
                .collection("comments").document(replyingTo.getCommentId());
        batch.update(parentRef, "replyCount", FieldValue.increment(1));

        batch.commit().addOnSuccessListener(aVoid -> {
            editComment.setText("");
            editComment.setHint("Write a comment...");
            replyingTo = null;
            Toast.makeText(this, "Reply posted", Toast.LENGTH_SHORT).show();
        });
    }

    private void deleteComment(Comment comment) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_comment_delete, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        MaterialButton btnCancel = dialogView.findViewById(R.id.buttonCancelDelete);
        MaterialButton btnConfirm = dialogView.findViewById(R.id.buttonConfirmDelete);

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                db.collection("events").document(eventId)
                        .collection("comments").document(comment.getCommentId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Comment deleted", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        });
            });
        }

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void showReactionDialog(Comment comment) {
        String[] options = {"LIKE 👍", "LOVE ❤️", "HELPFUL ⭐"};
        String[] types = {"LIKE", "LOVE", "HELPFUL"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("React with");
        builder.setItems(options, (dialog, which) -> {
            toggleReaction(comment, types[which]);
        });
        builder.show();
    }

    private void toggleReaction(Comment comment, String reactionType) {
        DocumentReference reactionRef = db.collection("events").document(eventId)
                .collection("comments").document(comment.getCommentId())
                .collection("reactions").document(deviceId + "_" + reactionType);

        reactionRef.get().addOnSuccessListener(doc -> {
            WriteBatch batch = db.batch();
            DocumentReference commentRef = db.collection("events").document(eventId)
                    .collection("comments").document(comment.getCommentId());

            if (doc.exists()) {
                batch.delete(reactionRef);
                batch.update(commentRef, "reactionCount", FieldValue.increment(-1));
                batch.update(commentRef, "reactionTypeCounts." + reactionType, FieldValue.increment(-1));
            } else {
                Map<String, Object> reaction = new HashMap<>();
                reaction.put("deviceId", deviceId);
                reaction.put("reactionType", reactionType);
                reaction.put("createdAt", Timestamp.now());
                batch.set(reactionRef, reaction);
                batch.update(commentRef, "reactionCount", FieldValue.increment(1));
                batch.update(commentRef, "reactionTypeCounts." + reactionType, FieldValue.increment(1));
            }
            batch.commit();
        });
    }

    private class CommentsAdapter extends RecyclerView.Adapter<CommentViewHolder> {
        private List<Comment> comments = new ArrayList<>();

        public void setComments(List<Comment> newComments) {
            this.comments = newComments;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_organizer, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
            holder.bind(comments.get(position));
        }

        @Override
        public int getItemCount() { return comments.size(); }
    }

    private class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView authorName, time, content, reply, react, btnDelete, buttonViewReplies;
        TextView likeCount, heartCount, likeEmoji, heartEmoji;
        View likeCard, heartCard, reactionSummary;
        ImageView avatar;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            authorName = itemView.findViewById(R.id.textCommentUserName);
            time = itemView.findViewById(R.id.textCommentTime);
            content = itemView.findViewById(R.id.textCommentContent);
            reply = itemView.findViewById(R.id.buttonReply);
            react = itemView.findViewById(R.id.buttonReact);
            btnDelete = itemView.findViewById(R.id.buttonDelete);
            buttonViewReplies = itemView.findViewById(R.id.buttonViewReplies);
            avatar = itemView.findViewById(R.id.imageAvatar);
            reactionSummary = itemView.findViewById(R.id.layoutReactionSummary);
            
            likeCount = itemView.findViewById(R.id.textLikeCount);
            heartCount = itemView.findViewById(R.id.textHeartCount);
            
            if (likeCount != null) {
                likeEmoji = (TextView) ((ViewGroup) likeCount.getParent()).getChildAt(0);
                likeCard = (View) likeCount.getParent().getParent();
            }
            if (heartCount != null) {
                heartEmoji = (TextView) ((ViewGroup) heartCount.getParent()).getChildAt(0);
                heartCard = (View) heartCount.getParent().getParent();
            }
        }

        public void bind(Comment comment) {
            authorName.setText(comment.getAuthorNameSnapshot());
            content.setText(comment.getContent());

            if (comment.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
                time.setText(sdf.format(comment.getCreatedAt().toDate()));
            }

            if (comment.getReactionCount() > 0 && comment.getReactionTypeCounts() != null) {
                reactionSummary.setVisibility(View.VISIBLE);
                Map<String, Integer> counts = comment.getReactionTypeCounts();
                List<String> activeTypes = new ArrayList<>();
                if (counts.getOrDefault("LIKE", 0) > 0) activeTypes.add("LIKE");
                if (counts.getOrDefault("LOVE", 0) > 0) activeTypes.add("LOVE");
                if (counts.getOrDefault("HELPFUL", 0) > 0) activeTypes.add("HELPFUL");

                if (activeTypes.size() > 0) {
                    likeCard.setVisibility(View.VISIBLE);
                    String type = activeTypes.get(0);
                    likeEmoji.setText(getEmoji(type));
                    likeCount.setText(String.valueOf(counts.get(type)));
                    likeCard.setOnClickListener(v -> toggleReaction(comment, type));
                } else {
                    likeCard.setVisibility(View.GONE);
                }

                if (activeTypes.size() > 1) {
                    heartCard.setVisibility(View.VISIBLE);
                    String type = activeTypes.get(1);
                    heartEmoji.setText(getEmoji(type));
                    heartCount.setText(String.valueOf(counts.get(type)));
                    heartCard.setOnClickListener(v -> toggleReaction(comment, type));
                } else {
                    heartCard.setVisibility(View.GONE);
                }
            } else {
                reactionSummary.setVisibility(View.GONE);
            }

            if (comment.getReplyCount() > 0) {
                buttonViewReplies.setVisibility(View.VISIBLE);
                buttonViewReplies.setText("View " + comment.getReplyCount() + " Replies");
            } else {
                buttonViewReplies.setVisibility(View.GONE);
            }

            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> deleteComment(comment));

            reply.setOnClickListener(v -> {
                replyingTo = comment;
                editComment.setHint("Reply to " + comment.getAuthorNameSnapshot() + "...");
                editComment.requestFocus();
            });

            react.setOnClickListener(v -> showReactionDialog(comment));
            
            buttonViewReplies.setOnClickListener(v -> {
                Toast.makeText(OrganizerCommentsActivity.this, "Opening Replies Activity...", Toast.LENGTH_SHORT).show();
            });
        }

        private String getEmoji(String type) {
            switch (type) {
                case "LIKE": return "👍";
                case "LOVE": return "❤️";
                case "HELPFUL": return "⭐";
                default: return "";
            }
        }
    }
}
