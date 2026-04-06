package com.example.lottery_legend.entrant;

import android.content.Context;
import android.widget.Toast;

import com.example.lottery_legend.model.Event;
import com.example.lottery_legend.model.Notification;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;
import java.util.Objects;

/**
 * Helper class to centralize business logic for entrant actions on notifications and events.
 * It provides methods for accepting or declining event invitations and managing related Firestore updates.
 */
public class EntrantActionHelper {

    /**
     * Interface for button visibility callback.
     */
    public interface ButtonVisibilityCallback {
        /**
         * Called when the visibility check is complete.
         * @param showButtons True if buttons should be displayed.
         */
        void onVisibilityChanged(boolean showButtons);
    }

    /**
     * Handles accepting a SIGN_UP_MESSAGE (Lottery invitation).
     * Updates the entrant's status in the event's waiting list to 'accepted' and marks the notification as processed.
     * @param context Context for showing Toasts.
     * @param deviceId Unique ID of the entrant.
     * @param notification The notification object triggering the action.
     * @param onSuccess Callback to run after successful Firestore commit.
     */
    public static void acceptSignUp(Context context, String deviceId, Notification notification, Runnable onSuccess) {
        if (notification == null || notification.getEventId() == null || deviceId == null) return;
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events").document(notification.getEventId()).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) return;

            Event event = doc.toObject(Event.class);
            if (event == null || event.getWaitingList() == null) return;

            List<Event.WaitingListEntry> list = event.getWaitingList();
            boolean updated = false;

            // Find the entrant's entry in the event's waiting list
            for (Event.WaitingListEntry entry : list) {
                if (entry != null && deviceId.equals(entry.getDeviceId())) {
                    entry.setParticipationStatus("accepted");
                    entry.setRespondedAt(Timestamp.now());
                    entry.setUpdatedAt(Timestamp.now());
                    updated = true;
                    break;
                }
            }

            if (!updated) return;

            // Use a batch to update both the event document and the notification status atomically
            WriteBatch batch = db.batch();
            batch.update(db.collection("events").document(notification.getEventId()), "waitingList", list);
            batch.update(db.collection("notifications").document(notification.getNotificationId()), 
                    "actionStatus", "ACCEPTED", "isRead", true);

            batch.commit().addOnSuccessListener(aVoid -> {
                Toast.makeText(context, "Invitation accepted", Toast.LENGTH_SHORT).show();
                if (onSuccess != null) onSuccess.run();
            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Failed to accept invitation", Toast.LENGTH_SHORT).show();
            });
        });
    }

    /**
     * Handles declining a SIGN_UP_MESSAGE (Lottery invitation).
     * Updates the entrant's status in the event's waiting list to 'declined' and marks the notification as processed.
     * @param context Context for showing Toasts.
     * @param deviceId Unique ID of the entrant.
     * @param notification The notification object triggering the action.
     * @param onSuccess Callback to run after successful Firestore commit.
     */
    public static void declineSignUp(Context context, String deviceId, Notification notification, Runnable onSuccess) {
        if (notification == null || notification.getEventId() == null || deviceId == null) return;
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events").document(notification.getEventId()).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) return;

            Event event = doc.toObject(Event.class);
            if (event == null || event.getWaitingList() == null) return;

            List<Event.WaitingListEntry> list = event.getWaitingList();
            boolean updated = false;

            // Find the entrant's entry in the event's waiting list
            for (Event.WaitingListEntry entry : list) {
                if (entry != null && deviceId.equals(entry.getDeviceId())) {
                    entry.setParticipationStatus("declined");
                    entry.setDeclinedAt(Timestamp.now());
                    entry.setUpdatedAt(Timestamp.now());
                    updated = true;
                    break;
                }
            }

            if (!updated) return;

            // Perform batch update
            WriteBatch batch = db.batch();
            batch.update(db.collection("events").document(notification.getEventId()), "waitingList", list);
            batch.update(db.collection("notifications").document(notification.getNotificationId()), 
                    "actionStatus", "DECLINED", "isRead", true);

            batch.commit().addOnSuccessListener(aVoid -> {
                Toast.makeText(context, "Invitation declined", Toast.LENGTH_SHORT).show();
                if (onSuccess != null) onSuccess.run();
            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Failed to decline invitation", Toast.LENGTH_SHORT).show();
            });
        });
    }

    /**
     * Finds the pending SIGN_UP_MESSAGE for a specific event and performs the requested action (accept/decline).
     * Useful when the action is triggered from the Event Details page rather than the Notification page.
     * @param context Activity context.
     * @param deviceId ID of the current device.
     * @param eventId ID of the event.
     * @param accept True to accept, false to decline.
     * @param onSuccess Success callback.
     */
    public static void processSignUpActionFromEvent(Context context, String deviceId, String eventId, boolean accept, Runnable onSuccess) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notifications")
                .whereEqualTo("recipientId", deviceId)
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("type", "SIGN_UP_MESSAGE")
                .whereEqualTo("actionStatus", "PENDING")
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        Notification notification = doc.toObject(Notification.class);
                        if (notification != null) {
                            notification.setNotificationId(doc.getId());
                            if (accept) {
                                acceptSignUp(context, deviceId, notification, onSuccess);
                            } else {
                                declineSignUp(context, deviceId, notification, onSuccess);
                            }
                        }
                    } else {
                        Toast.makeText(context, "No pending sign-up message found", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Checks if the accept/decline buttons should be visible for a user viewing an event.
     * Buttons are shown if the event is open, the user is selected, and there is a pending notification.
     * @param deviceId ID of the entrant.
     * @param eventId ID of the event.
     * @param participationStatus Current participation status of the entrant.
     * @param eventStatus Current status of the event (e.g., 'open', 'closed').
     * @param callback Result callback.
     */
    public static void checkSignUpButtonVisibility(String deviceId, String eventId, String participationStatus, String eventStatus, ButtonVisibilityCallback callback) {
        if (deviceId == null || eventId == null) {
            callback.onVisibilityChanged(false);
            return;
        }

        // Must be an active event and user must have 'selected' status
        if (!"open".equalsIgnoreCase(eventStatus) || !"selected".equalsIgnoreCase(participationStatus)) {
            callback.onVisibilityChanged(false);
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notifications")
                .whereEqualTo("recipientId", deviceId)
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("type", "SIGN_UP_MESSAGE")
                .whereEqualTo("actionStatus", "PENDING")
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    callback.onVisibilityChanged(!queryDocumentSnapshots.isEmpty());
                })
                .addOnFailureListener(e -> callback.onVisibilityChanged(false));
    }

    /**
     * Logical check to determine if an invitation action is valid.
     * @param deviceId Current user ID.
     * @param event Event object.
     * @param notification Notification object.
     * @return True if valid.
     */
    public static boolean canPerformSignUpAction(String deviceId, Event event, Notification notification) {
        if (deviceId == null || event == null || notification == null) return false;
        if (!"SIGN_UP_MESSAGE".equals(notification.getType())) return false;
        if (!"PENDING".equalsIgnoreCase(notification.getActionStatus())) return false;
        if (!"open".equalsIgnoreCase(event.getStatus())) return false;

        if (event.getWaitingList() != null) {
            for (Event.WaitingListEntry entry : event.getWaitingList()) {
                if (Objects.equals(entry.getDeviceId(), deviceId)) {
                    return "selected".equalsIgnoreCase(entry.getParticipationStatus());
                }
            }
        }
        return false;
    }

    /**
     * Updates an entrant's status to 'not_selected' when they receive a lottery loss notification.
     * @param deviceId ID of the entrant.
     * @param notification The loss notification.
     */
    public static void handleLotteryLose(String deviceId, Notification notification) {
        if (notification == null || notification.getEventId() == null || deviceId == null) return;
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events").document(notification.getEventId()).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) return;

            Event event = doc.toObject(Event.class);
            if (event == null || event.getWaitingList() == null) return;

            List<Event.WaitingListEntry> list = event.getWaitingList();
            boolean updated = false;

            for (Event.WaitingListEntry entry : list) {
                if (entry != null && deviceId.equals(entry.getDeviceId())) {
                    if (!"not_selected".equals(entry.getParticipationStatus())) {
                        entry.setParticipationStatus("not_selected");
                        entry.setUpdatedAt(Timestamp.now());
                        updated = true;
                    }
                    break;
                }
            }

            if (updated) {
                db.collection("events").document(notification.getEventId()).update("waitingList", list);
            }
        });
    }
}
