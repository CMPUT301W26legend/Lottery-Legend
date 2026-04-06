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
 */
public class EntrantActionHelper {

    /**
     * Interface for button visibility callback.
     */
    public interface ButtonVisibilityCallback {
        void onVisibilityChanged(boolean showButtons);
    }

    /**
     * Handles accepting a SIGN_UP_MESSAGE.
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
     * Handles declining a SIGN_UP_MESSAGE.
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
     * Finds the pending SIGN_UP_MESSAGE for an event and performs an action.
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
     * Checks if the accept/decline buttons should be shown for a SIGN_UP_MESSAGE.
     * Rule: show buttons only when:
     * 1. event is open
     * 2. entrant is in waitingList and status is "selected"
     * 3. related SIGN_UP_MESSAGE notification actionStatus is still "PENDING"
     */
    public static void checkSignUpButtonVisibility(String deviceId, String eventId, String participationStatus, String eventStatus, ButtonVisibilityCallback callback) {
        if (deviceId == null || eventId == null) {
            callback.onVisibilityChanged(false);
            return;
        }

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
     * Determines if a SIGN_UP_MESSAGE action can be performed given the event and notification.
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
     * Handles LOTTERY_LOSE message by moving event to "not_selected" history.
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
