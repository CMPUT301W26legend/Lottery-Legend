package com.example.lottery_legend.model;

import com.google.firebase.Timestamp;
import java.util.Map;

/**
 * Model class representing a comment on an event.
 * Stored in /events/{eventId}/comments/{commentId}
 */
public class Comment {
    private String commentId;
    private String authorId;
    private String authorType; // ENTRANT, ORGANIZER
    private String authorNameSnapshot;
    private String content;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String parentCommentId; // null for top-level
    private String rootCommentId;   // null for top-level
    private int threadLevel;        // 0, 1, 2
    private String replyToUserId;
    private String replyToUserNameSnapshot;
    
    // Reaction Counts
    private int likeCount;
    private int loveCount;
    private int helpfulCount;
    
    private int reactionCount; // Total reactions
    private int replyCount;
    private Map<String, Integer> reactionTypeCounts; // Deprecated but keeping for compatibility if needed

    public Comment() {}

    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorType() { return authorType; }
    public void setAuthorType(String authorType) { this.authorType = authorType; }

    public String getAuthorNameSnapshot() { return authorNameSnapshot; }
    public void setAuthorNameSnapshot(String authorNameSnapshot) { this.authorNameSnapshot = authorNameSnapshot; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(String parentCommentId) { this.parentCommentId = parentCommentId; }

    public String getRootCommentId() { return rootCommentId; }
    public void setRootCommentId(String rootCommentId) { this.rootCommentId = rootCommentId; }

    public int getThreadLevel() { return threadLevel; }
    public void setThreadLevel(int threadLevel) { this.threadLevel = threadLevel; }

    public String getReplyToUserId() { return replyToUserId; }
    public void setReplyToUserId(String replyToUserId) { this.replyToUserId = replyToUserId; }

    public String getReplyToUserNameSnapshot() { return replyToUserNameSnapshot; }
    public void setReplyToUserNameSnapshot(String replyToUserNameSnapshot) { this.replyToUserNameSnapshot = replyToUserNameSnapshot; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public int getLoveCount() { return loveCount; }
    public void setLoveCount(int loveCount) { this.loveCount = loveCount; }

    public int getHelpfulCount() { return helpfulCount; }
    public void setHelpfulCount(int helpfulCount) { this.helpfulCount = helpfulCount; }

    public int getReactionCount() { return reactionCount; }
    public void setReactionCount(int reactionCount) { this.reactionCount = reactionCount; }

    public int getReplyCount() { return replyCount; }
    public void setReplyCount(int replyCount) { this.replyCount = replyCount; }

    public Map<String, Integer> getReactionTypeCounts() { return reactionTypeCounts; }
    public void setReactionTypeCounts(Map<String, Integer> reactionTypeCounts) { this.reactionTypeCounts = reactionTypeCounts; }
}
