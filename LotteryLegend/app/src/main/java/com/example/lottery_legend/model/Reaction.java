package com.example.lottery_legend.model;

import com.google.firebase.Timestamp;

/**
 * Model class for independent reactions on a comment.
 * Stored in /events/{eventId}/comments/{commentId}/reactions/{deviceId}
 */
public class Reaction {
    private String deviceId;
    private boolean like;
    private boolean love;
    private boolean helpful;
    private Timestamp updatedAt;

    public Reaction() {}

    public Reaction(String deviceId, boolean like, boolean love, boolean helpful, Timestamp updatedAt) {
        this.deviceId = deviceId;
        this.like = like;
        this.love = love;
        this.helpful = helpful;
        this.updatedAt = updatedAt;
    }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public boolean isLike() { return like; }
    public void setLike(boolean like) { this.like = like; }

    public boolean isLove() { return love; }
    public void setLove(boolean love) { this.love = love; }

    public boolean isHelpful() { return helpful; }
    public void setHelpful(boolean helpful) { this.helpful = helpful; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
