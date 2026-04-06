package com.example.lottery_legend.model;

import com.google.firebase.Timestamp;
import java.util.Map;

/**
 * Model class representing a comment on an event.
 * Stored in Firestore at /events/{eventId}/comments/{commentId}.
 * Supports nested replies and reaction counts.
 */
public class Comment {
    /** Unique identifier for the comment. */
    private String commentId;
    /** Unique identifier for the author of the comment. */
    private String authorId;
    /** Type of author (e.g., ENTRANT, ORGANIZER). */
    private String authorType;
    /** Snapshot of the author's name at the time of posting. */
    private String authorNameSnapshot;
    /** The actual text content of the comment. */
    private String content;
    /** Timestamp when the comment was created. */
    private Timestamp createdAt;
    /** Timestamp when the comment was last updated. */
    private Timestamp updatedAt;
    /** ID of the parent comment, null if it is a top-level comment. */
    private String parentCommentId;
    /** ID of the root comment in the thread, null if it is a top-level comment. */
    private String rootCommentId;
    /** Nesting level of the comment in the thread. */
    private int threadLevel;
    /** ID of the user being replied to. */
    private String replyToUserId;
    /** Snapshot of the name of the user being replied to. */
    private String replyToUserNameSnapshot;
    
    /** Number of "like" reactions. */
    private int likeCount;
    /** Number of "love" reactions. */
    private int loveCount;
    /** Number of "helpful" reactions. */
    private int helpfulCount;
    
    /** Total count of all reactions. */
    private int reactionCount;
    /** Total number of replies to this comment. */
    private int replyCount;
    /** Mapping of reaction types to their counts. Deprecated but kept for compatibility. */
    private Map<String, Integer> reactionTypeCounts;

    /**
     * Default constructor required for Firebase Firestore deserialization.
     */
    public Comment() {}

    /** @return The unique identifier for the comment. */
    public String getCommentId() { return commentId; }
    /** @param commentId The unique identifier for the comment. */
    public void setCommentId(String commentId) { this.commentId = commentId; }

    /** @return The unique identifier for the author. */
    public String getAuthorId() { return authorId; }
    /** @param authorId The unique identifier for the author. */
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    /** @return The type of author (ENTRANT or ORGANIZER). */
    public String getAuthorType() { return authorType; }
    /** @param authorType The type of author (ENTRANT or ORGANIZER). */
    public void setAuthorType(String authorType) { this.authorType = authorType; }

    /** @return The author's name snapshot. */
    public String getAuthorNameSnapshot() { return authorNameSnapshot; }
    /** @param authorNameSnapshot The author's name snapshot. */
    public void setAuthorNameSnapshot(String authorNameSnapshot) { this.authorNameSnapshot = authorNameSnapshot; }

    /** @return The content of the comment. */
    public String getContent() { return content; }
    /** @param content The content of the comment. */
    public void setContent(String content) { this.content = content; }

    /** @return The creation timestamp. */
    public Timestamp getCreatedAt() { return createdAt; }
    /** @param createdAt The creation timestamp. */
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    /** @return The last update timestamp. */
    public Timestamp getUpdatedAt() { return updatedAt; }
    /** @param updatedAt The last update timestamp. */
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    /** @return The parent comment's unique identifier. */
    public String getParentCommentId() { return parentCommentId; }
    /** @param parentCommentId The parent comment's unique identifier. */
    public void setParentCommentId(String parentCommentId) { this.parentCommentId = parentCommentId; }

    /** @return The root comment's unique identifier. */
    public String getRootCommentId() { return rootCommentId; }
    /** @param rootCommentId The root comment's unique identifier. */
    public void setRootCommentId(String rootCommentId) { this.rootCommentId = rootCommentId; }

    /** @return The nesting level of the comment. */
    public int getThreadLevel() { return threadLevel; }
    /** @param threadLevel The nesting level of the comment. */
    public void setThreadLevel(int threadLevel) { this.threadLevel = threadLevel; }

    /** @return The identifier of the user being replied to. */
    public String getReplyToUserId() { return replyToUserId; }
    /** @param replyToUserId The identifier of the user being replied to. */
    public void setReplyToUserId(String replyToUserId) { this.replyToUserId = replyToUserId; }

    /** @return The name snapshot of the user being replied to. */
    public String getReplyToUserNameSnapshot() { return replyToUserNameSnapshot; }
    /** @param replyToUserNameSnapshot The name snapshot of the user being replied to. */
    public void setReplyToUserNameSnapshot(String replyToUserNameSnapshot) { this.replyToUserNameSnapshot = replyToUserNameSnapshot; }

    /** @return The count of like reactions. */
    public int getLikeCount() { return likeCount; }
    /** @param likeCount The count of like reactions. */
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    /** @return The count of love reactions. */
    public int getLoveCount() { return loveCount; }
    /** @param loveCount The count of love reactions. */
    public void setLoveCount(int loveCount) { this.loveCount = loveCount; }

    /** @return The count of helpful reactions. */
    public int getHelpfulCount() { return helpfulCount; }
    /** @param helpfulCount The count of helpful reactions. */
    public void setHelpfulCount(int helpfulCount) { this.helpfulCount = helpfulCount; }

    /** @return The total reaction count. */
    public int getReactionCount() { return reactionCount; }
    /** @param reactionCount The total reaction count. */
    public void setReactionCount(int reactionCount) { this.reactionCount = reactionCount; }

    /** @return The total reply count. */
    public int getReplyCount() { return replyCount; }
    /** @param replyCount The total reply count. */
    public void setReplyCount(int replyCount) { this.replyCount = replyCount; }

    /** @return A map of reaction types to counts. */
    public Map<String, Integer> getReactionTypeCounts() { return reactionTypeCounts; }
    /** @param reactionTypeCounts A map of reaction types to counts. */
    public void setReactionTypeCounts(Map<String, Integer> reactionTypeCounts) { this.reactionTypeCounts = reactionTypeCounts; }
}
