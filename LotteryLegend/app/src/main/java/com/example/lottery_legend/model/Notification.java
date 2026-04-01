package com.example.lottery_legend.model;

import com.google.firebase.Timestamp;

/**
 * Model class for Notifications in the system.
 */
public class Notification {
    private String notificationId;
    private String senderId;
    private String recipientId;
    private String recipientType; // ENTRANT, ORGANIZER
    private String eventId;
    private String type; // LOTTERY_WIN, LOTTERY_LOSE, CO_ORGANIZER_INVITE, etc.
    private String title;
    private String message;
    private boolean isRead;
    private Timestamp createdAt;
    private String actionStatus; // NONE, PENDING, ACCEPTED, DECLINED, DISMISSED

    public Notification() {}

    public Notification(String notificationId, String senderId, String recipientId, String recipientType, 
                        String eventId, String type, String title, String message, 
                        boolean isRead, Timestamp createdAt, String actionStatus) {
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
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
    public String getRecipientType() { return recipientType; }
    public void setRecipientType(String recipientType) { this.recipientType = recipientType; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isIsRead() { return isRead; }
    public void setIsRead(boolean read) { isRead = read; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public String getActionStatus() { return actionStatus; }
    public void setActionStatus(String actionStatus) { this.actionStatus = actionStatus; }
}
