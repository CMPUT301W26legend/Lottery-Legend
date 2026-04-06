package com.example.lottery_legend.model;

import com.google.firebase.Timestamp;

/**
 * Model class representing a user's reaction to a comment.
 * Reactions are stored independently to allow multiple users to react to the same comment.
 * Stored in Firestore at /events/{eventId}/comments/{commentId}/reactions/{deviceId}.
 */
public class Reaction {
    /** Unique identifier for the device/user who reacted. */
    private String deviceId;
    /** Flag indicating if the user liked the comment. */
    private boolean like;
    /** Flag indicating if the user loved the comment. */
    private boolean love;
    /** Flag indicating if the user found the comment helpful. */
    private boolean helpful;
    /** Timestamp when the reaction was last updated. */
    private Timestamp updatedAt;

    /**
     * Default constructor required for Firebase Firestore deserialization.
     */
    public Reaction() {}

    /**
     * Constructs a new Reaction with the specified values.
     *
     * @param deviceId  The unique device identifier.
     * @param like      True if liked.
     * @param love      True if loved.
     * @param helpful   True if helpful.
     * @param updatedAt The last update timestamp.
     */
    public Reaction(String deviceId, boolean like, boolean love, boolean helpful, Timestamp updatedAt) {
        this.deviceId = deviceId;
        this.like = like;
        this.love = love;
        this.helpful = helpful;
        this.updatedAt = updatedAt;
    }

    /** @return The unique device identifier. */
    public String getDeviceId() { return deviceId; }
    /** @param deviceId The unique device identifier to set. */
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    /** @return True if liked. */
    public boolean isLike() { return like; }
    /** @param like True if liked. */
    public void setLike(boolean like) { this.like = like; }

    /** @return True if loved. */
    public boolean isLove() { return love; }
    /** @param love True if loved. */
    public void setLove(boolean love) { this.love = love; }

    /** @return True if helpful. */
    public boolean isHelpful() { return helpful; }
    /** @param helpful True if helpful. */
    public void setHelpful(boolean helpful) { this.helpful = helpful; }

    /** @return The last update timestamp. */
    public Timestamp getUpdatedAt() { return updatedAt; }
    /** @param updatedAt The last update timestamp to set. */
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
