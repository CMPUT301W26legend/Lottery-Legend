package com.example.lottery_legend.organizer;

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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Activity for organizers to view a specific comment thread.
 * Displays a parent comment and all its nested replies.
 * Provides the same interactive features as the main comments view, but focused on one thread.
 * Organizers have the privilege to delete any comment within the thread.
 */
public class OrganizerCommentThreadActivity extends AppCompatActivity implements OrganizerReplyAdapter.OnReplyInteractionListener {

    private static final String TAG = "OrganizerCommentThread";

    private String eventId;
    private String parentCommentId;
    private String deviceId;
    private String currentUserName;
    private String currentUserType;

    private FirebaseFirestore db;
    private Comment parentComment;
    private List<Comment> replies = new ArrayList<>();
    private OrganizerReplyAdapter adapter;

    private RecyclerView recyclerViewReplies;
    private EditText editTextReply;
    private ImageButton buttonSendReply;
    private TextView textToolbarTitle;

    private TextView textParentAuthorName, textParentTime, textParentContent;
    private TextView textParentLikeCount, textParentLoveCount, textParentHelpfulCount;
    private MaterialCardView cardParentLike, cardParentLove, cardParentHelpful;
    private View reactionSummary, buttonParentReply, buttonParentReact, buttonParentDelete;

    /** The specific comment in the thread being replied to. If null, the reply targets the root comment. */
    private Comment activeReplyTarget = null;
    /** The current user's reaction to the main parent comment of this thread. */
    private Reaction parentUserReaction = null;

    /** Flag tracking if any data (reactions, deletions, posts) has changed to notify the parent activity. */
    private boolean hasChanges = false;

    private ListenerRegistration parentCommentRegistration;
    private ListenerRegistration parentReactionRegistration;
    private ListenerRegistration repliesRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comment_thread_organizer);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

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

        NavbarOrganizer.setup(this, deviceId, NavbarOrganizer.Tab.HOME);
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

    /**
     * Overrides finish to send a result code if changes were made, 
     * prompting the caller to refresh its data.
     */
    @Override
    public void finish() {
        if (hasChanges) {
            setResult(RESULT_OK, new Intent());
        }
        super.finish();
    }

    /**
     * Loads required identifiers and session info from the starting intent.
     * @return True if all mandatory data is present.
     */
    private boolean loadIntentExtras() {
        eventId = getIntent().getStringExtra("eventId");
        parentCommentId = getIntent().getStringExtra("parentCommentId");
        deviceId = getIntent().getStringExtra("deviceId");
        currentUserName = getIntent().getStringExtra("currentUserName");
        currentUserType = getIntent().getStringExtra("currentUserType");
        return !TextUtils.isEmpty(eventId) && !TextUtils.isEmpty(parentCommentId);
    }

    /**
     * Initializes UI component references.
     */
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

    /**
     * Sets up the MaterialToolbar with navigation.
     */
    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarCommentThread);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        textToolbarTitle.setText("Thread");
    }

    /**
     * Initializes the RecyclerView for replies.
     */
    private void setupRecyclerView() {
        adapter = new OrganizerReplyAdapter(this, currentUserType, deviceId, this);
        recyclerViewReplies.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReplies.setAdapter(adapter);
    }

    /**
     * Sets up click listeners for the main parent comment actions.
     */
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
                    })
                    .show();
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

    /**
     * Attaches Firestore listeners for the parent comment, its replies, and the current user's reactions.
     */
    private void startRealtimeListeners() {
        stopRealtimeListeners();
        listenParentComment();
        listenParentUserReaction();
        listenReplies();
    }

    /**
     * Detaches all active Firestore snapshot listeners.
     */
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

    /**
     * Listens for changes to the root comment of this thread.
     */
    private void listenParentComment() {
        parentCommentRegistration = db.collection("events")
                .document(eventId)
                .collection("comments")
                .document(parentCommentId)
                .addSnapshotListener((doc, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error loading parent comment", e);
                        return;
                    }

                    if (doc != null && doc.exists()) {
                        parentComment = doc.toObject(Comment.class);
                        bindParentComment(parentComment);
                    } else {
                        // If the parent comment is deleted, close the thread view
                        finish();
                    }
                });
    }

    /**
     * Listens for the current user's reaction specifically on the parent comment.
     */
    private void listenParentUserReaction() {
        parentReactionRegistration = db.collection("events")
                .document(eventId)
                .collection("comments")
                .document(parentCommentId)
                .collection("reactions")
                .document(deviceId)
                .addSnapshotListener((doc, e) -> {
                    if (doc != null && doc.exists()) {
                        parentUserReaction = doc.toObject(Reaction.class);
                    } else {
                        parentUserReaction = null;
                    }

                    if (parentComment != null) {
                        bindParentComment(parentComment);
                    }
                });
    }

    /**
     * Listens for all replies that belong to this thread's root comment.
     */
    private void listenReplies() {
        repliesRegistration = db.collection("events")
                .document(eventId)
                .collection("comments")
                .whereEqualTo("rootCommentId", parentCommentId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading replies", error);
                        return;
                    }

                    if (value != null) {
                        List<Comment> replyList = value.toObjects(Comment.class);
                        replies = sortRepliesNested(replyList);
                        adapter.setReplies(replies);
                    }
                });
    }

    /**
     * Sorts replies chronologically while maintaining a simple nested structure for levels 1 and 2.
     * @param rawReplies The unsorted list of reply models.
     * @return A sorted and leveled list of comments.
     */
    private List<Comment> sortRepliesNested(List<Comment> rawReplies) {
        Map<String, List<Comment>> byParent = new HashMap<>();
        List<Comment> level1 = new ArrayList<>();

        for (Comment c : rawReplies) {
            if (c == null || TextUtils.isEmpty(c.getCommentId())) continue;

            if (TextUtils.equals(parentCommentId, c.getParentCommentId())) {
                c.setThreadLevel(1);
                level1.add(c);
            } else {
                String pid = c.getParentCommentId();
                if (!TextUtils.isEmpty(pid)) {
                    if (!byParent.containsKey(pid)) {
                        byParent.put(pid, new ArrayList<>());
                    }
                    byParent.get(pid).add(c);
                }
            }
        }

        Collections.sort(level1, this::compareComments);

        List<Comment> result = new ArrayList<>();
        for (Comment topReply : level1) {
            addWithChildren(topReply, byParent, result, false);
        }

        return result;
    }

    /**
     * Recursively adds a comment and its children to the flattened list for display.
     */
    private void addWithChildren(Comment parent, Map<String, List<Comment>> byParent, List<Comment> result, boolean forceLevel2) {
        if (forceLevel2) {
            parent.setThreadLevel(2);
        } else if (parent.getThreadLevel() <= 0) {
            parent.setThreadLevel(1);
        }

        result.add(parent);

        List<Comment> children = byParent.get(parent.getCommentId());
        if (children == null || children.isEmpty()) {
            return;
        }

        Collections.sort(children, this::compareComments);

        for (Comment child : children) {
            child.setThreadLevel(2);
            addWithChildren(child, byParent, result, true);
        }
    }

    /**
     * Comparator for sorting comments by creation timestamp.
     */
    private int compareComments(Comment c1, Comment c2) {
        if (c1 == null && c2 == null) return 0;
        if (c1 == null) return -1;
        if (c2 == null) return 1;

        if (c1.getCreatedAt() == null && c2.getCreatedAt() == null) return 0;
        if (c1.getCreatedAt() == null) return -1;
        if (c2.getCreatedAt() == null) return 1;

        return c1.getCreatedAt().compareTo(c2.getCreatedAt());
    }

    /**
     * Updates the parent comment UI section with data from the model.
     * @param comment The parent comment model.
     */
    private void bindParentComment(Comment comment) {
        if (comment == null) return;

        textParentAuthorName.setText(comment.getAuthorNameSnapshot());
        textParentContent.setText(comment.getContent());

        if (comment.getCreatedAt() != null) {
            textParentTime.setText(
                    new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                            .format(comment.getCreatedAt().toDate())
            );
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

        setCardSelected(
                cardParentLike,
                parentUserReaction != null && parentUserReaction.isLike(),
                Color.parseColor("#2563EB"),
                textParentLikeCount
        );
        setCardSelected(
                cardParentLove,
                parentUserReaction != null && parentUserReaction.isLove(),
                Color.parseColor("#EF4444"),
                textParentLoveCount
        );
        setCardSelected(
                cardParentHelpful,
                parentUserReaction != null && parentUserReaction.isHelpful(),
                Color.parseColor("#EAB308"),
                textParentHelpfulCount
        );

        // Organizers can always delete any comment
        buttonParentDelete.setVisibility(View.VISIBLE);
    }

    /**
     * Updates the visual state of a reaction card.
     */
    private void setCardSelected(MaterialCardView card, boolean selected, int color, TextView countText) {
        if (selected) {
            card.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
            countText.setTextColor(color);
        } else {
            card.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#F3F4F6")));
            countText.setTextColor(Color.parseColor("#6B7280"));
        }
    }

    /**
     * Sets the active reply target and focuses the input field.
     * @param target The comment being replied to, or null for thread-level.
     */
    private void setReplyTarget(Comment target) {
        activeReplyTarget = target;

        if (target == null) {
            editTextReply.setHint("Write a reply...");
        } else {
            editTextReply.setHint("Replying to " + target.getAuthorNameSnapshot() + "...");
            editTextReply.requestFocus();

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(editTextReply, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    /**
     * Validates and posts a new reply to Firestore.
     */
    private void postReply() {
        String content = editTextReply.getText().toString().trim();
        if (TextUtils.isEmpty(content)) return;

        Comment reply = new Comment();
        DocumentReference replyRef = db.collection("events")
                .document(eventId)
                .collection("comments")
                .document();

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
            DocumentReference rootRef = db.collection("events")
                    .document(eventId)
                    .collection("comments")
                    .document(parentCommentId);

            batch.update(rootRef, "replyCount", FieldValue.increment(1));
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    hasChanges = true;
                    editTextReply.setText("");
                    activeReplyTarget = null;
                    editTextReply.setHint("Write a reply...");

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(editTextReply.getWindowToken(), 0);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to post reply", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Displays a deletion confirmation dialog and removes the comment and its subtree from Firestore.
     * @param comment The comment to delete.
     */
    private void deleteComment(Comment comment) {
        if (comment == null) return;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_comment_delete, null);
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.TransparentDialog)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.buttonCancelDelete).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.buttonConfirmDelete).setOnClickListener(v -> {
            WriteBatch batch = db.batch();

            // Collect IDs of this comment and all nested replies to delete them in one batch
            Set<String> idsToDelete = collectSubtreeCommentIds(comment.getCommentId());

            for (String id : idsToDelete) {
                DocumentReference commentRef = db.collection("events")
                        .document(eventId)
                        .collection("comments")
                        .document(id);
                batch.delete(commentRef);
            }

            // If we're deleting level 1 replies, decrement the parent's replyCount
            int deletedLevel1Count = countDeletedLevel1Replies(idsToDelete);

            if (deletedLevel1Count > 0 && !TextUtils.equals(comment.getCommentId(), parentCommentId)) {
                DocumentReference rootRef = db.collection("events")
                        .document(eventId)
                        .collection("comments")
                        .document(parentCommentId);
                batch.update(rootRef, "replyCount", FieldValue.increment(-deletedLevel1Count));
            }

            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        hasChanges = true;
                        dialog.dismiss();

                        // If the thread's parent was deleted, we must leave this activity
                        if (TextUtils.equals(comment.getCommentId(), parentCommentId)) {
                            finish();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to delete comment", Toast.LENGTH_SHORT).show()
                    );
        });

        dialog.show();
    }

    /**
     * Traverses the local replies cache to find all descendant comment IDs for a given parent.
     */
    private Set<String> collectSubtreeCommentIds(String startCommentId) {
        Set<String> result = new LinkedHashSet<>();
        if (TextUtils.isEmpty(startCommentId)) return result;

        result.add(startCommentId);

        boolean added;
        do {
            added = false;
            for (Comment reply : replies) {
                if (reply == null || TextUtils.isEmpty(reply.getCommentId())) continue;

                String parentId = reply.getParentCommentId();
                if (!TextUtils.isEmpty(parentId) && result.contains(parentId) && !result.contains(reply.getCommentId())) {
                    result.add(reply.getCommentId());
                    added = true;
                }
            }
        } while (added);

        return result;
    }

    /**
     * Counts how many of the comments marked for deletion are direct (level 1) children of the thread parent.
     */
    private int countDeletedLevel1Replies(Set<String> idsToDelete) {
        int count = 0;
        for (Comment reply : replies) {
            if (reply == null || TextUtils.isEmpty(reply.getCommentId())) continue;

            if (idsToDelete.contains(reply.getCommentId())
                    && TextUtils.equals(reply.getParentCommentId(), parentCommentId)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Toggles a user reaction on any comment within the thread.
     * @param comment The target comment.
     * @param type    The reaction type.
     */
    private void toggleReaction(Comment comment, String type) {
        if (comment == null) return;

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
                            countField, FieldValue.increment(inc),
                            "reactionCount", FieldValue.increment(inc)
                    );

                    return null;
                })
                .addOnSuccessListener(unused -> hasChanges = true)
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
