package com.example.lottery_legend;

import com.example.lottery_legend.model.Organizer;
import com.google.firebase.Timestamp;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import static org.junit.Assert.*;

/**
 * Unit test for the Organizer model class.
 */
public class OrganizerUnitTest {

    @Test
    public void testOrganizerConstructorAndGetters() {
        String name = "Jane Doe";
        String email = "jane@example.com";
        String phone = "0987654321";
        String deviceId = "org123";
        Timestamp joinDate = new Timestamp(new Date());
        Timestamp updatedAt = new Timestamp(new Date());
        boolean isAdmin = true;

        Organizer organizer = new Organizer(deviceId, name, email, phone, joinDate, updatedAt, isAdmin, new ArrayList<>());

        assertEquals(name, organizer.getName());
        assertEquals(email, organizer.getEmail());
        assertEquals(phone, organizer.getPhone());
        assertEquals(deviceId, organizer.getDeviceId());
        assertEquals(joinDate, organizer.getJoinDate());
        assertEquals(updatedAt, organizer.getUpdatedAt());
        assertTrue(organizer.getIsAdmin());
        assertNotNull(organizer.getCreatedEvents());
    }

    @Test
    public void testSettersAndGetters() {
        Organizer organizer = new Organizer();
        
        organizer.setName("New Name");
        assertEquals("New Name", organizer.getName());

        organizer.setEmail("new@example.com");
        assertEquals("new@example.com", organizer.getEmail());

        organizer.setPhone("1112223333");
        assertEquals("1112223333", organizer.getPhone());

        organizer.setDeviceId("newId");
        assertEquals("newId", organizer.getDeviceId());

        Timestamp newDate = new Timestamp(new Date());
        organizer.setJoinDate(newDate);
        assertEquals(newDate, organizer.getJoinDate());

        Timestamp updateDate = new Timestamp(new Date());
        organizer.setUpdatedAt(updateDate);
        assertEquals(updateDate, organizer.getUpdatedAt());

        organizer.setIsAdmin(false);
        assertFalse(organizer.getIsAdmin());

        organizer.setCreatedEvents(new ArrayList<>());
        assertNotNull(organizer.getCreatedEvents());
    }

    @Test
    public void testEmptyConstructor() {
        Organizer organizer = new Organizer();
        assertNull(organizer.getName());
        assertNull(organizer.getEmail());
        assertNull(organizer.getPhone());
        assertNull(organizer.getDeviceId());
        assertNull(organizer.getJoinDate());
        assertNull(organizer.getUpdatedAt());
        assertFalse(organizer.getIsAdmin());
        assertNull(organizer.getCreatedEvents());
    }

    @Test
    public void testCreatedEvent() {
        String eventId = "event123";
        String title = "Lottery Event";
        String status = "open";
        Timestamp now = Timestamp.now();
        
        Organizer.CreatedEvent event = new Organizer.CreatedEvent(eventId, title, status, now);
        
        assertEquals(eventId, event.getEventId());
        assertEquals(title, event.getTitle());
        assertEquals(status, event.getStatus());
        assertEquals(now, event.getCreatedAt());
    }
}
