package com.example.lottery_legend.entrant;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery_legend.R;
import com.example.lottery_legend.model.Comment;
import com.example.lottery_legend.model.Reaction;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommentThreadActivity extends AppCompatActivity implements ReplyAdapter.OnReplyInteractionListener {

    private static final String TAG = "CommentThreadActivity";

    private String eventId;
    private String parentCommentId;
    private String deviceId;
    private String currentUserName;
    private String currentUserType;

    private FirebaseFirestore db;
    private Comment parentComment;
    private List<Comment> replies = new ArrayList<>();
    private ReplyAdapter adapter;

    private RecyclerView recyclerViewReplies;
    private EditText editTextReply;
    private ImageButton buttonSendReply;
    private TextView textToolbarTitle;

    private TextView textParentAuthorName, textParentTime, textParentContent;
    private TextView textParentLikeCount, textParentLoveCount, textParentHelpfulCount;
    private MaterialCardView cardParentLike, cardParentLove, cardParentHelpful;
    private View reactionSummary, buttonParentReply, buttonParentReact, buttonParentDelete;

    private Comment activeReplyTarget = null;
    private Reaction parentUserReaction = null;

    private boolean hasChanges = false;

    private ListenerRegistration parentCommentRegistration;
    private ListenerRegistration parentReactionRegistration;
    private ListenerRegistration repliesRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comment_thread);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        if (!loadIntentExtras()) {
            Toast.makeText(this, "Error: Missing Thread ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startRealtimeListeners();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRealtimeListeners();
    }

    @Override
    public void finish() {
        if (hasChanges) {
            setResult(RESULT_OK, new Intent());
        }
        super.finish();
    }

    private boolean loadIntentExtras() {
        eventId = getIntent().getStringExtra("eventId");
        parentCommentId = getIntent().getStringExtra("parentCommentId");
        deviceId = getIntent().getStringExtra("deviceId");
        currentUserName = getIntent().getStringExtra("currentUserName");
        currentUserType = getIntent().getStringExtra("currentUserType");
        return !TextUtils.isEmpty(eventId) && !TextUtils.isEmpty(parentCommentId);
    }

    private void initViews() {
        recyclerViewReplies = findViewById(R.id.recyclerViewReplies);
        editTextReply = findViewById(R.id.editTextReply);
        buttonSendReply = findViewById(R.id.buttonSendReply);
        textToolbarTitle = findViewById(R.id.textToolbarTitle);

        View parentHeader = findViewById(R.id.layoutParentComment);
        textParentAuthorName = parentHeader.findViewById(R.id.textParentAuthorName);
        textParentTime = parentHeader.findViewById(R.id.textParentTime);
        textParentContent = parentHeader.findViewById(R.id.textParentContent);

        reactionSummary = parentHeader.findViewById(R.id.layoutParentReactions);
        textParentLikeCount = parentHeader.findViewById(R.id.textParentLikeCount);
        textParentLoveCount = parentHeader.findViewById(R.id.textParentLoveCount);
        textParentHelpfulCount = parentHeader.findViewById(R.id.textParentHelpfulCount);

        cardParentLike = parentHeader.findViewById(R.id.cardParentLike);
        cardParentLove = parentHeader.findViewById(R.id.cardParentLove);
        cardParentHelpful = parentHeader.findViewById(R.id.cardParentHelpful);

        buttonParentReply = parentHeader.findViewById(R.id.buttonParentReply);
        buttonParentReact = parentHeader.findViewById(R.id.buttonParentReact);
        buttonParentDelete = parentHeader.findViewById(R.id.buttonParentDelete);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarCommentThread);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());
        textToolbarTitle.setText("Thread");
    }

    private void setupRecyclerView() {
        adapter = new ReplyAdapter(this, currentUserType, deviceId, this);
        recyclerViewReplies.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReplies.setAdapter(adapter);
    }

    private void setupListeners() {
        buttonSendReply.setOnClickListener(v -> postReply());

        buttonParentReply.setOnClickListener(v -> setReplyTarget(null));

        buttonParentReact.setOnClickListener(v -> {
            if (parentComment == null) return;
            String[] options = {"LIKE 👍", "LOVE ❤️", "HELPFUL ⭐"};
            new AlertDialog.Builder(this)
                    .setTitle("React with")
                    .setItems(options, (dialog, which) -> {
                        String[] types = {"LIKE", "LOVE", "HELPFUL"};
                        toggleReaction(parentComment, types[which]);
                    }).show();
        });

        buttonParentDelete.setOnClickListener(v -> {
            if (parentComment != null) {
                deleteComment(parentComment);
            }
        });

        cardParentLike.setOnClickListener(v -> {
            if (parentComment != null) toggleReaction(parentComment, "LIKE");
        });
        cardParentLove.setOnClickListener(v -> {
            if (parentComment != null) toggleReaction(parentComment, "LOVE");
        });
        cardParentHelpful.setOnClickListener(v -> {
            if (parentComment != null) toggleReaction(parentComment, "HELPFUL");
        });
    }

    private void startRealtimeListeners() {
        stopRealtimeListeners();
        listenParentComment();
        listenParentUserReaction();
        listenReplies();
    }

    private void stopRealtimeListeners() {
        if (parentCommentRegistration != null) {
            parentCommentRegistration.remove();
            parentCommentRegistration = null;
        }
        if (parentReactionRegistration != null) {
            parentReactionRegistration.remove();
            parentReactionRegistration = null;
        }
        if (repliesRegistration != null) {
            repliesRegistration.remove();
            repliesRegistration = null;
        }
    }

    private void listenParentComment() {
        parentCommentRegistration = db.collection("events").document(eventId)
                .collection("comments").document(parentCommentId)
                .addSnapshotListener((doc, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error loading parent comment", e);
                        return;
                    }
                    if (doc != null && doc.exists()) {
                        parentComment = doc.toObject(Comment.class);
                        bindParentComment(parentComment);
                    } else {
                        finish();
                    }
                });
    }

    private void listenParentUserReaction() {
        parentReactionRegistration = db.collection("events").document(eventId)
                .collection("comments").document(parentCommentId)
                .collection("reactions").document(deviceId)
                .addSnapshotListener((doc, e) -> {
                    if (doc != null && doc.exists()) {
                        parentUserReaction = doc.toObject(Reaction.class);
                    } else {
                        parentUserReaction = null;
                    }
                    if (parentComment != null) bindParentComment(parentComment);
                });
    }

    private void listenReplies() {
        repliesRegistration = db.collection("events").document(eventId)
                .collection("comments")
                .whereEqualTo("rootCommentId", parentCommentId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading replies", error);
                        return;
                    }
                    if (value != null) {
                        List<Comment> replyList = value.toObjects(Comment.class);
                        this.replies = sortRepliesNested(replyList);
                        adapter.setReplies(this.replies);
                    }
                });
    }

    private List<Comment> sortRepliesNested(List<Comment> rawReplies) {
        Map<String, List<Comment>> byParent = new HashMap<>();
        List<Comment> level1 = new ArrayList<>();

        for (Comment c : rawReplies) {
            if (c == null) continue;

            if (c.getThreadLevel() == 1) {
                level1.add(c);
            } else {
                String pid = c.getParentCommentId();
                if (!TextUtils.isEmpty(pid)) {
                    if (!byParent.containsKey(pid)) byParent.put(pid, new ArrayList<>());
                    byParent.get(pid).add(c);
                }
            }
        }

        Collections.sort(level1, this::compareComments);

        List<Comment> result = new ArrayList<>();
        for (Comment c : level1) {
            addWithChildren(c, byParent, result);
        }
        return result;
    }

    private void addWithChildren(Comment parent, Map<String, List<Comment>> byParent, List<Comment> result) {
        result.add(parent);
        List<Comment> children = byParent.get(parent.getCommentId());
        if (children != null) {
            Collections.sort(children, this::compareComments);
            for (Comment child : children) {
                result.add(child);
            }
        }
    }

    private int compareComments(Comment c1, Comment c2) {
        if (c1.getCreatedAt() == null && c2.getCreatedAt() == null) return 0;
        if (c1.getCreatedAt() == null) return -1;
        if (c2.getCreatedAt() == null) return 1;
        return c1.getCreatedAt().compareTo(c2.getCreatedAt());
    }

    private void bindParentComment(Comment comment) {
        textParentAuthorName.setText(comment.getAuthorNameSnapshot());
        textParentContent.setText(comment.getContent());

        if (comment.getCreatedAt() != null) {
            textParentTime.setText(new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                    .format(comment.getCreatedAt().toDate()));
        } else {
            textParentTime.setText("");
        }

        reactionSummary.setVisibility(comment.getReactionCount() > 0 ? View.VISIBLE : View.GONE);

        textParentLikeCount.setText(String.valueOf(comment.getLikeCount()));
        textParentLoveCount.setText(String.valueOf(comment.getLoveCount()));
        textParentHelpfulCount.setText(String.valueOf(comment.getHelpfulCount()));

        cardParentLike.setVisibility(comment.getLikeCount() > 0 ? View.VISIBLE : View.GONE);
        cardParentLove.setVisibility(comment.getLoveCount() > 0 ? View.VISIBLE : View.GONE);
        cardParentHelpful.setVisibility(comment.getHelpfulCount() > 0 ? View.VISIBLE : View.GONE);

        setCardSelected(cardParentLike, parentUserReaction != null && parentUserReaction.isLike(), Color.parseColor("#2563EB"), textParentLikeCount);
        setCardSelected(cardParentLove, parentUserReaction != null && parentUserReaction.isLove(), Color.parseColor("#EF4444"), textParentLoveCount);
        setCardSelected(cardParentHelpful, parentUserReaction != null && parentUserReaction.isHelpful(), Color.parseColor("#EAB308"), textParentHelpfulCount);

        boolean canDelete = !TextUtils.isEmpty(deviceId)
                && (deviceId.equals(comment.getAuthorId()) || "ORGANIZER".equals(currentUserType));
        buttonParentDelete.setVisibility(canDelete ? View.VISIBLE : View.GONE);
    }

    private void setCardSelected(MaterialCardView card, boolean selected, int color, TextView countText) {
        if (selected) {
            card.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
            countText.setTextColor(color);
        } else {
            card.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#F3F4F6")));
            countText.setTextColor(Color.parseColor("#6B7280"));
        }
    }

    private void setReplyTarget(Comment target) {
        activeReplyTarget = target;
        if (target == null) {
            editTextReply.setHint("Write a reply...");
        } else {
            editTextReply.setHint("Replying to " + target.getAuthorNameSnapshot() + "...");
            editTextReply.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(editTextReply, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void postReply() {
        String content = editTextReply.getText().toString().trim();
        if (TextUtils.isEmpty(content)) return;

        Comment reply = new Comment();
        DocumentReference replyRef = db.collection("events").document(eventId).collection("comments").document();
        String replyId = replyRef.getId();

        reply.setCommentId(replyId);
        reply.setAuthorId(deviceId);
        reply.setAuthorType(currentUserType);
        reply.setAuthorNameSnapshot(currentUserName);
        reply.setContent(content);
        reply.setCreatedAt(Timestamp.now());
        reply.setUpdatedAt(Timestamp.now());
        reply.setRootCommentId(parentCommentId);

        if (activeReplyTarget != null) {
            reply.setParentCommentId(activeReplyTarget.getCommentId());
            reply.setReplyToUserId(activeReplyTarget.getAuthorId());
            reply.setReplyToUserNameSnapshot(activeReplyTarget.getAuthorNameSnapshot());
            reply.setThreadLevel(2);
        } else {
            reply.setParentCommentId(parentCommentId);
            reply.setReplyToUserId(null);
            reply.setReplyToUserNameSnapshot(null);
            reply.setThreadLevel(1);
        }

        WriteBatch batch = db.batch();
        batch.set(replyRef, reply);

        if (reply.getThreadLevel() == 1) {
            DocumentReference rootRef = db.collection("events").document(eventId)
                    .collection("comments").document(parentCommentId);
            batch.update(rootRef, "replyCount", FieldValue.increment(1));
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    hasChanges = true;
                    editTextReply.setText("");
                    activeReplyTarget = null;
                    editTextReply.setHint("Write a reply...");
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) imm.hideSoftInputFromWindow(editTextReply.getWindowToken(), 0);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to post reply", Toast.LENGTH_SHORT).show());
    }

    private void deleteComment(Comment comment) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_comment_delete, null);
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.TransparentDialog).setView(dialogView).create();

        dialogView.findViewById(R.id.buttonCancelDelete).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.buttonConfirmDelete).setOnClickListener(v -> {
            WriteBatch batch = db.batch();
            DocumentReference commentRef = db.collection("events").document(eventId).collection("comments").document(comment.getCommentId());
            batch.delete(commentRef);

            if (comment.getThreadLevel() == 1) {
                DocumentReference rootRef = db.collection("events").document(eventId).collection("comments").document(parentCommentId);
                batch.update(rootRef, "replyCount", FieldValue.increment(-1));
            }

            batch.commit().addOnSuccessListener(aVoid -> {
                hasChanges = true;
                dialog.dismiss();
                if (comment.getCommentId().equals(parentCommentId)) finish();
            });
        });
        dialog.show();
    }

    private void toggleReaction(Comment comment, String type) {
        if (comment == null) return;

        DocumentReference commentRef = db.collection("events").document(eventId)
                .collection("comments").document(comment.getCommentId());
        DocumentReference reactionRef = commentRef.collection("reactions").document(deviceId);

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
                    transaction.update(commentRef,
                            countField, FieldValue.increment(inc),
                            "reactionCount", FieldValue.increment(inc));
                    return null;
                }).addOnSuccessListener(unused -> hasChanges = true)
                .addOnFailureListener(e -> Log.e(TAG, "Reaction toggle failed", e));
    }

    @Override
    public void onReplyClicked(Comment comment) {
        setReplyTarget(comment);
    }

    @Override
    public void onDeleteClicked(Comment comment) {
        deleteComment(comment);
    }
}