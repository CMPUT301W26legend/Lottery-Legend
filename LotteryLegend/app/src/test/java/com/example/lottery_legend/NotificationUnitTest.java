package com.example.lottery_legend;

import com.example.lottery_legend.model.Notification;
import com.google.firebase.Timestamp;
import org.junit.Test;
import java.util.Date;
import static org.junit.Assert.*;

/**
 * Unit test for the Notification model class.
 */
public class NotificationUnitTest {

    @Test
    public void testNotificationConstructorAndGetters() {
        String notificationId = "notif123";
        String senderId = "sender456";
        String recipientId = "recipient789";
        String recipientType = "ENTRANT";
        String eventId = "event000";
        String type = "LOTTERY_WIN";
        String title = "Congratulations!";
        String message = "You won the lottery.";
        boolean isRead = false;
        Timestamp now = new Timestamp(new Date());
        String actionStatus = "PENDING";

        Notification notification = new Notification(notificationId, senderId, recipientId, recipientType, eventId, type, title, message, isRead, now, actionStatus);

        assertEquals(notificationId, notification.getNotificationId());
        assertEquals(senderId, notification.getSenderId());
        assertEquals(recipientId, notification.getRecipientId());
        assertEquals(recipientType, notification.getRecipientType());
        assertEquals(eventId, notification.getEventId());
        assertEquals(type, notification.getType());
        assertEquals(title, notification.getTitle());
        assertEquals(message, notification.getMessage());
        assertFalse(notification.getIsRead());
        assertEquals(now, notification.getCreatedAt());
        assertEquals(actionStatus, notification.getActionStatus());
    }

    @Test
    public void testGetReceiverGroup() {
        Notification notification = new Notification();
        
        notification.setType("LOTTERY_WIN");
        assertEquals("Selected/Accepted Users", notification.getReceiverGroup());
        
        notification.setType("LOTTERY_LOSE");
        assertEquals("Non-Selected Users", notification.getReceiverGroup());
        
        notification.setType("SELECTED_MESSAGE");
        assertEquals("Selected/Accepted Users", notification.getReceiverGroup());

        notification.setType("WAITLIST_MESSAGE");
        assertEquals("Waiting List Users", notification.getReceiverGroup());

        notification.setType("SIGN_UP_MESSAGE");
        assertEquals("Selected Users", notification.getReceiverGroup());

        notification.setType("CANCELLED_MESSAGE");
        assertEquals("Cancelled/Declined Users", notification.getReceiverGroup());

        notification.setType("PRIVATE_INVITE");
        assertEquals("Specifically Invited Users", notification.getReceiverGroup());

        notification.setType("CO_ORGANIZER_INVITE");
        assertEquals("Promoted Co-Organizers", notification.getReceiverGroup());

        notification.setType("GENERIC_ANNOUNCEMENT");
        assertEquals("All Entrants", notification.getReceiverGroup());
        
        notification.setType("UNKNOWN");
        notification.setRecipientType("Custom Type");
        assertEquals("Custom Type", notification.getReceiverGroup());

        notification.setType(null);
        notification.setRecipientType("Entrant");
        assertEquals("Entrant", notification.getReceiverGroup());

        notification.setRecipientType(null);
        assertEquals("Unknown", notification.getReceiverGroup());
    }

    @Test
    public void testSetters() {
        Notification notification = new Notification();
        notification.setEventTitle("Test Event");
        notification.setSenderName("System");
        notification.setRecipientName("User");
        notification.setIsRead(true);

        assertEquals("Test Event", notification.getEventTitle());
        assertEquals("System", notification.getSenderName());
        assertEquals("User", notification.getRecipientName());
        assertTrue(notification.getIsRead());
    }
}
