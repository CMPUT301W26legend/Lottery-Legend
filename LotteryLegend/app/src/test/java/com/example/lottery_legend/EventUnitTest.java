package com.example.lottery_legend;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

import com.example.lottery_legend.model.Event;

/**
 * Unit test for the Event model class.
 */
public class EventUnitTest {

    @Test
    public void testEventConstructorAndGetters() {
        String organizerId = "org123";
        String title = "Summer Concert";
        String description = "Music under the stars";
        boolean geoEnabled = true;
        Event.EventLocation location = new Event.EventLocation("Central Park", "Central Park", 0.0, 0.0);
        int capacity = 500;
        Integer maxWaitingList = 1000;

        Event event = new Event();
        event.setOrganizerId(organizerId);
        event.setTitle(title);
        event.setDescription(description);
        event.setGeoEnabled(geoEnabled);
        event.setEventLocation(location);
        event.setCapacity(capacity);
        event.setMaxWaitingList(maxWaitingList);
        event.setStatus("open");

        assertEquals(organizerId, event.getOrganizerId());
        assertEquals(title, event.getTitle());
        assertEquals(description, event.getDescription());
        assertTrue(event.isGeoEnabled());
        assertEquals(location, event.getEventLocation());
        assertEquals(capacity, event.getCapacity());
        assertEquals(maxWaitingList, event.getMaxWaitingList());
        assertEquals("open", event.getStatus());
        assertNull(event.getWaitingList()); // Waiting list is null by default in new Event()
    }

    @Test
    public void testSettersAndGetters() {
        Event event = new Event();
        
        event.setEventId("id123");
        assertEquals("id123", event.getEventId());

        event.setTitle("Updated Title");
        assertEquals("Updated Title", event.getTitle());

        event.setStatus("closed");
        assertEquals("closed", event.getStatus());

        List<Event.WaitingListEntry> waitingList = new ArrayList<>();
        Event.WaitingListEntry entry = new Event.WaitingListEntry();
        entry.setDeviceId("user1");
        waitingList.add(entry);
        event.setWaitingList(waitingList);
        assertEquals(1, event.getWaitingList().size());
        assertEquals("user1", event.getWaitingList().get(0).getDeviceId());
    }

    @Test
    public void testEmptyConstructor() {
        Event event = new Event();
        assertNull(event.getEventId());
        assertNull(event.getWaitingList());
    }
}
