package com.example.lottery_legend.organizer;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery_legend.R;
import com.example.lottery_legend.model.Comment;
import com.example.lottery_legend.model.Reaction;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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

    private final Map<String, Reaction> userReactions = new HashMap<>();
    private final List<Comment> allComments = new ArrayList<>();
    private final Map<String, Integer> directReplyCountMap = new HashMap<>();

    private ListenerRegistration commentsRegistration;
    private ListenerRegistration reactionsRegistration;

    private final ActivityResultLauncher<Intent> threadLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            restartRealtimeListeners();
                        }
                    }
            );

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

        if (TextUtils.isEmpty(eventId)) {
            finish();
        }

        NavbarOrganizer.setup(this, deviceId, NavbarOrganizer.Tab.HOME);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!TextUtils.isEmpty(eventId)) {
            startRealtimeListeners();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRealtimeListeners();
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

    private void startRealtimeListeners() {
        stopRealtimeListeners();
        listenComments();
        listenCurrentUserReactions();
    }

    private void restartRealtimeListeners() {
        startRealtimeListeners();
    }

    private void stopRealtimeListeners() {
        if (commentsRegistration != null) {
            commentsRegistration.remove();
            commentsRegistration = null;
        }
        if (reactionsRegistration != null) {
            reactionsRegistration.remove();
            reactionsRegistration = null;
        }
    }

    private void listenComments() {
        commentsRegistration = db.collection("events")
                .document(eventId)
                .collection("comments")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) {
                        return;
                    }

                    List<Comment> comments = value.toObjects(Comment.class);

                    Collections.sort(comments, (c1, c2) -> {
                        if (c1.getCreatedAt() == null && c2.getCreatedAt() == null) return 0;
                        if (c1.getCreatedAt() == null) return -1;
                        if (c2.getCreatedAt() == null) return 1;
                        return c1.getCreatedAt().compareTo(c2.getCreatedAt());
                    });

                    allComments.clear();
                    allComments.addAll(comments);

                    rebuildDirectReplyCountMap(comments);

                    List<Comment> parentComments = new ArrayList<>();
                    for (Comment comment : comments) {
                        if (comment.getThreadLevel() == 0) {
                            parentComments.add(comment);
                        }
                    }

                    adapter.setComments(parentComments);
                });
    }

    private void rebuildDirectReplyCountMap(List<Comment> comments) {
        directReplyCountMap.clear();

        for (Comment comment : comments) {
            if (comment == null) continue;

            if (comment.getThreadLevel() == 1) {
                String rootCommentId = comment.getRootCommentId();
                if (!TextUtils.isEmpty(rootCommentId)) {
                    int current = directReplyCountMap.getOrDefault(rootCommentId, 0);
                    directReplyCountMap.put(rootCommentId, current + 1);
                }
            }
        }
    }

    private void listenCurrentUserReactions() {
        reactionsRegistration = db.collectionGroup("reactions")
                .whereEqualTo("deviceId", deviceId)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) {
                        return;
                    }

                    userReactions.clear();

                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : value) {
                        if (doc.getReference().getParent() != null
                                && doc.getReference().getParent().getParent() != null) {
                            String commentId = doc.getReference().getParent().getParent().getId();
                            userReactions.put(commentId, doc.toObject(Reaction.class));
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private void postComment() {
        String content = editComment.getText().toString().trim();
        if (TextUtils.isEmpty(content)) return;

        DocumentReference ref = db.collection("events")
                .document(eventId)
                .collection("comments")
                .document();

        Comment comment = new Comment();
        comment.setCommentId(ref.getId());
        comment.setAuthorId(deviceId);
        comment.setAuthorType(authorType);
        comment.setAuthorNameSnapshot(authorName);
        comment.setContent(content);
        comment.setCreatedAt(Timestamp.now());
        comment.setUpdatedAt(Timestamp.now());
        comment.setThreadLevel(0);
        comment.setParentCommentId(null);
        comment.setRootCommentId(null);
        comment.setReplyToUserId(null);
        comment.setReplyToUserNameSnapshot(null);
        comment.setReplyCount(0);

        ref.set(comment).addOnSuccessListener(aVoid -> editComment.setText(""));
    }

    private void postReply() {
        String content = editComment.getText().toString().trim();
        if (TextUtils.isEmpty(content) || replyingTo == null) return;

        DocumentReference ref = db.collection("events")
                .document(eventId)
                .collection("comments")
                .document();

        Comment reply = new Comment();
        reply.setCommentId(ref.getId());
        reply.setAuthorId(deviceId);
        reply.setAuthorType(authorType);
        reply.setAuthorNameSnapshot(authorName);
        reply.setContent(content);
        reply.setCreatedAt(Timestamp.now());
        reply.setUpdatedAt(Timestamp.now());

        String rootId = !TextUtils.isEmpty(replyingTo.getRootCommentId())
                ? replyingTo.getRootCommentId()
                : replyingTo.getCommentId();

        reply.setRootCommentId(rootId);
        reply.setParentCommentId(replyingTo.getCommentId());
        reply.setThreadLevel(replyingTo.getThreadLevel() + 1);

        if (reply.getThreadLevel() == 1) {
            reply.setReplyToUserId(null);
            reply.setReplyToUserNameSnapshot(null);
        } else {
            reply.setReplyToUserId(replyingTo.getAuthorId());
            reply.setReplyToUserNameSnapshot(replyingTo.getAuthorNameSnapshot());
        }

        ref.set(reply).addOnSuccessListener(aVoid -> {
            editComment.setText("");
            editComment.setHint("Write a comment...");
            replyingTo = null;
        });
    }

    private void deleteComment(Comment comment) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_comment_delete, null);
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.TransparentDialog)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.buttonCancelDelete).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.buttonConfirmDelete).setOnClickListener(v -> {
            db.collection("events")
                    .document(eventId)
                    .collection("comments")
                    .document(comment.getCommentId())
                    .delete()
                    .addOnSuccessListener(aVoid -> dialog.dismiss());
        });

        dialog.show();
    }

    private void toggleReaction(Comment comment, String type) {
        DocumentReference commentRef = db.collection("events")
                .document(eventId)
                .collection("comments")
                .document(comment.getCommentId());

        DocumentReference reactionRef = commentRef
                .collection("reactions")
                .document(deviceId);

        db.runTransaction(transaction -> {
            Reaction existing = transaction.get(reactionRef).toObject(Reaction.class);
            if (existing == null) {
                existing = new Reaction();
                existing.setDeviceId(deviceId);
            }

            boolean newValue;
            String countField;

            switch (type) {
                case "LIKE":
                    newValue = !existing.isLike();
                    existing.setLike(newValue);
                    countField = "likeCount";
                    break;
                case "LOVE":
                    newValue = !existing.isLove();
                    existing.setLove(newValue);
                    countField = "loveCount";
                    break;
                case "HELPFUL":
                    newValue = !existing.isHelpful();
                    existing.setHelpful(newValue);
                    countField = "helpfulCount";
                    break;
                default:
                    return null;
            }

            existing.setUpdatedAt(Timestamp.now());
            transaction.set(reactionRef, existing);

            int inc = newValue ? 1 : -1;
            transaction.update(
                    commentRef,
                    countField, com.google.firebase.firestore.FieldValue.increment(inc),
                    "reactionCount", com.google.firebase.firestore.FieldValue.increment(inc)
            );

            return null;
        });
    }

    private class CommentsAdapter extends RecyclerView.Adapter<CommentViewHolder> {
        private List<Comment> comments = new ArrayList<>();

        public void setComments(List<Comment> newComments) {
            comments = newComments != null ? newComments : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new CommentViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_organizer, parent, false)
            );
        }

        @Override
        public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
            holder.bind(comments.get(position));
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }
    }

    private class CommentViewHolder extends RecyclerView.ViewHolder {

        ImageView imageAvatar;
        TextView authorName;
        TextView time;
        TextView content;
        TextView reply;
        TextView react;
        TextView buttonViewReplies;
        TextView buttonDelete;
        TextView likeCount;
        TextView heartCount;

        MaterialCardView cardLike;
        MaterialCardView cardLove;

        View reactionSummary;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            imageAvatar = itemView.findViewById(R.id.imageAvatar);
            authorName = itemView.findViewById(R.id.textCommentUserName);
            time = itemView.findViewById(R.id.textCommentTime);
            content = itemView.findViewById(R.id.textCommentContent);
            reply = itemView.findViewById(R.id.buttonReply);
            react = itemView.findViewById(R.id.buttonReact);
            buttonViewReplies = itemView.findViewById(R.id.buttonViewReplies);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);

            reactionSummary = itemView.findViewById(R.id.layoutReactionSummary);
            likeCount = itemView.findViewById(R.id.textLikeCount);
            heartCount = itemView.findViewById(R.id.textHeartCount);

            // Corrected IDs from item_comment_organizer.xml
            cardLike = itemView.findViewById(R.id.cardLike);
            cardLove = itemView.findViewById(R.id.cardLove);
        }

        public void bind(Comment comment) {
            authorName.setText(comment.getAuthorNameSnapshot());
            content.setText(comment.getContent());

            if (comment.getCreatedAt() != null) {
                time.setText(new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                        .format(comment.getCreatedAt().toDate()));
            } else {
                time.setText("");
            }

            // Load author avatar
            loadAuthorAvatar(comment);

            updateReactionUI(comment);

            int directReplyCount = directReplyCountMap.getOrDefault(comment.getCommentId(), 0);

            if (directReplyCount > 0) {
                buttonViewReplies.setVisibility(View.VISIBLE);
                buttonViewReplies.setText("View " + directReplyCount + " Replies");
            } else {
                buttonViewReplies.setVisibility(View.GONE);
            }

            reply.setOnClickListener(v -> {
                replyingTo = comment;
                editComment.setHint("Reply to " + comment.getAuthorNameSnapshot() + "...");
                editComment.requestFocus();
            });

            react.setOnClickListener(v -> {
                String[] options = {"LIKE 👍", "LOVE ❤️", "HELPFUL ⭐"};
                new AlertDialog.Builder(OrganizerCommentsActivity.this)
                        .setTitle("React with")
                        .setItems(options, (dialog, which) -> {
                            String[] types = {"LIKE", "LOVE", "HELPFUL"};
                            toggleReaction(comment, types[which]);
                        })
                        .show();
            });

            buttonViewReplies.setOnClickListener(v -> {
                Intent intent = new Intent(OrganizerCommentsActivity.this, OrganizerCommentThreadActivity.class);
                intent.putExtra("eventId", eventId);
                intent.putExtra("parentCommentId", comment.getCommentId());
                intent.putExtra("deviceId", deviceId);
                intent.putExtra("currentUserName", OrganizerCommentsActivity.this.authorName);
                intent.putExtra("currentUserType", authorType);
                threadLauncher.launch(intent);
            });

            // Organizer can always delete
            buttonDelete.setVisibility(View.VISIBLE);
            buttonDelete.setOnClickListener(v -> deleteComment(comment));

            if (cardLike != null) cardLike.setOnClickListener(v -> toggleReaction(comment, "LIKE"));
            if (cardLove != null) cardLove.setOnClickListener(v -> toggleReaction(comment, "LOVE"));
        }

        private void loadAuthorAvatar(Comment comment) {
            String collection = "ORGANIZER".equalsIgnoreCase(comment.getAuthorType()) ? "organizers" : "entrants";
            db.collection(collection).document(comment.getAuthorId()).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String profileImage = doc.getString("profileImage");
                    if (profileImage != null && !profileImage.isEmpty()) {
                        displayBase64Image(profileImage, imageAvatar);
                    } else {
                        imageAvatar.setImageResource(R.drawable.ic_profile_avatar);
                    }
                } else {
                    imageAvatar.setImageResource(R.drawable.ic_profile_avatar);
                }
            }).addOnFailureListener(e -> imageAvatar.setImageResource(R.drawable.ic_profile_avatar));
        }

        private void updateReactionUI(Comment comment) {
            reactionSummary.setVisibility(comment.getReactionCount() > 0 ? View.VISIBLE : View.GONE);

            if (likeCount != null) likeCount.setText(String.valueOf(comment.getLikeCount()));
            if (heartCount != null) heartCount.setText(String.valueOf(comment.getLoveCount()));

            if (cardLike != null) cardLike.setVisibility(comment.getLikeCount() > 0 ? View.VISIBLE : View.GONE);
            if (cardLove != null) cardLove.setVisibility(comment.getLoveCount() > 0 ? View.VISIBLE : View.GONE);

            Reaction reaction = userReactions.get(comment.getCommentId());

            if (cardLike != null) setCardSelected(cardLike, reaction != null && reaction.isLike(), Color.parseColor("#2563EB"), likeCount);
            if (cardLove != null) setCardSelected(cardLove, reaction != null && reaction.isLove(), Color.parseColor("#EF4444"), heartCount);
        }

        private void setCardSelected(MaterialCardView card, boolean selected, int color, TextView countText) {
            if (selected) {
                card.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
                if (countText != null) countText.setTextColor(color);
            } else {
                card.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#F3F4F6")));
                if (countText != null) countText.setTextColor(Color.parseColor("#6B7280"));
            }
        }
    }

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
}
