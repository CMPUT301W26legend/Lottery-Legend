package com.example.lottery_legend.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;

/**
 * Model class representing a Notification in the system.
 * Notifications can be sent to entrants or organizers regarding event updates, 
 * lottery results, or invitations.
 */
public class Notification implements Serializable {
    /** Unique identifier for the notification. */
    private String notificationId;
    /** Unique identifier for the sender of the notification. */
    private String senderId;
    /** Unique identifier for the recipient of the notification. */
    private String recipientId;
    /** Type of the recipient (e.g., ENTRANT, ORGANIZER). */
    private String recipientType;
    /** ID of the event associated with this notification. */
    private String eventId;
    /** Type of notification (e.g., LOTTERY_WIN, PRIVATE_INVITE). */
    private String type;
    /** Title of the notification. */
    private String title;
    /** Detailed message of the notification. */
    private String message;
    /** Flag indicating if the notification has been read. */
    private boolean isRead;
    /** Timestamp when the notification was created. */
    private Timestamp createdAt;
    /** Status of the action associated with the notification (e.g., accepted, declined). */
    private String actionStatus;
    
    // Fields excluded from Firestore but used for UI display
    /** Title of the associated event. Not stored in Firestore. */
    private String eventTitle;
    /** Name of the sender. Not stored in Firestore. */
    private String senderName;
    /** Name of the recipient. Not stored in Firestore. */
    private String recipientName;

    /**
     * Default no-argument constructor required for Firebase Firestore.
     */
    public Notification() {
    }

    /**
     * Full constructor for Notification.
     *
     * @param notificationId Unique ID.
     * @param senderId       Sender's ID.
     * @param recipientId    Recipient's ID.
     * @param recipientType  Type of recipient.
     * @param eventId        Associated event ID.
     * @param type           Type of notification.
     * @param title          Notification title.
     * @param message        Notification message.
     * @param isRead         Read status.
     * @param createdAt      Creation timestamp.
     * @param actionStatus   Status of associated action.
     */
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

    /** @return The unique identifier for the notification. */
    public String getNotificationId() {
        return notificationId;
    }

    /** @param notificationId The unique identifier for the notification. */
    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    /** @return The unique identifier for the sender. */
    public String getSenderId() {
        return senderId;
    }

    /** @param senderId The unique identifier for the sender. */
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    /** @return The unique identifier for the recipient. */
    public String getRecipientId() {
        return recipientId;
    }

    /** @param recipientId The unique identifier for the recipient. */
    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    /** @return The type of recipient. */
    public String getRecipientType() {
        return recipientType;
    }

    /** @param recipientType The type of recipient. */
    public void setRecipientType(String recipientType) {
        this.recipientType = recipientType;
    }

    /** @return The associated event identifier. */
    public String getEventId() {
        return eventId;
    }

    /** @param eventId The associated event identifier. */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /** @return The type of notification. */
    public String getType() {
        return type;
    }

    /** @param type The type of notification. */
    public void setType(String type) {
        this.type = type;
    }

    /** @return The title of the notification. */
    public String getTitle() {
        return title;
    }

    /** @param title The title of the notification. */
    public void setTitle(String title) {
        this.title = title;
    }

    /** @return The message body of the notification. */
    public String getMessage() {
        return message;
    }

    /** @param message The message body of the notification. */
    public void setMessage(String message) {
        this.message = message;
    }

    /** @return True if the notification has been read. */
    @PropertyName("isRead")
    public boolean getIsRead() {
        return isRead;
    }

    /** @param read The read status to set. */
    @PropertyName("isRead")
    public void setIsRead(boolean read) {
        isRead = read;
    }

    /** @return The creation timestamp. */
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    /** @param createdAt The creation timestamp. */
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /** @return The status of the action related to this notification. */
    public String getActionStatus() {
        return actionStatus;
    }

    /** @param actionStatus The status of the action related to this notification. */
    public void setActionStatus(String actionStatus) {
        this.actionStatus = actionStatus;
    }

    /** @return The event title (UI display only). */
    @Exclude
    public String getEventTitle() {
        return eventTitle;
    }

    /** @param eventTitle The event title (UI display only). */
    @Exclude
    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    /** @return The sender's name (UI display only). */
    @Exclude
    public String getSenderName() {
        return senderName;
    }

    /** @param senderName The sender's name (UI display only). */
    @Exclude
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    /** 
     * Determines the human-readable receiver group based on the notification type.
     * @return String describing the recipient group.
     */
    @Exclude
    public String getReceiverGroup() {
        if (type == null) return recipientType != null ? recipientType : "Unknown";

        switch (type) {
            case "LOTTERY_WIN":
                return "Selected/Accepted Users";
            case "LOTTERY_LOSE":
                return "Non-Selected Users";
            case "SELECTED_MESSAGE":
                return "Selected/Accepted Users";
            case "WAITLIST_MESSAGE":
                return "Waiting List Users";
            case "SIGN_UP_MESSAGE":
                return "Selected Users";
            case "CANCELLED_MESSAGE":
                return "Cancelled/Declined Users";
            case "PRIVATE_INVITE":
                return "Specifically Invited Users";
            case "CO_ORGANIZER_INVITE":
                return "Promoted Co-Organizers";
            case "GENERIC_ANNOUNCEMENT":
                return "All Entrants";
            default:
                return recipientType != null ? recipientType : "General Group";
        }
    }

    /** @return The recipient's name (UI display only). */
    @Exclude
    public String getRecipientName() {
        return recipientName;
    }

    /** @param recipientName The recipient's name (UI display only). */
    @Exclude
    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }
}
