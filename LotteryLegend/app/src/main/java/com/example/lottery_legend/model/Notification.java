package com.example.lottery_legend.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;

/**
 * Model class for Notifications in the system.
 */
public class Notification implements Serializable {
    private String notificationId;
    private String senderId;
    private String recipientId;
    private String recipientType;
    private String eventId;
    private String type;
    private String title;
    private String message;
    private boolean isRead;
    private Timestamp createdAt;
    private String actionStatus;
    private String eventTitle;
    private String senderName;
    private String recipientName;

    public Notification() {
    }

    public Notification(String notificationId, String senderId, String recipientId, String recipientType, String eventId, String type, String title, String message, boolean isRead, Timestamp createdAt, String actionStatus) {
        this.notificationId = notificationId;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.recipientType = recipientType;
        this.eventId = eventId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.actionStatus = actionStatus;
    }

    // Getters and Setters

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(String recipientType) {
        this.recipientType = recipientType;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @PropertyName("isRead")
    public boolean getIsRead() {
        return isRead;
    }

    @PropertyName("isRead")
    public void setIsRead(boolean read) {
        isRead = read;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getActionStatus() {
        return actionStatus;
    }

    public void setActionStatus(String actionStatus) {
        this.actionStatus = actionStatus;
    }

    @Exclude
    public String getEventTitle() {
        return eventTitle;
    }

    @Exclude
    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    @Exclude
    public String getSenderName() {
        return senderName;
    }

    @Exclude
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    @Exclude
    public String getReceiverGroup() {
        if (type == null) return recipientType != null ? recipientType : "Unknown";

        switch (type) {
            case "LOTTERY_WIN":
                return "Selected Users";
            case "LOTTERY_LOSE":
                return "Waiting Users";
            case "CANCELLED_MESSAGE":
                return "Cancelled/Declined Users";
            case "PRIVATE_INVITE":
                return "Specifically Invited Users";
            case "CO_ORGANIZER_INVITE":
                return "Co-Organizers invite";
            case "GENERIC_ANNOUNCEMENT":
                return "All Entrants";
            default:
                return recipientType != null ? recipientType : "General Group";
        }
    }
    @Exclude
    public String getRecipientName() {
        return recipientName;
    }

    @Exclude
    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }
}
