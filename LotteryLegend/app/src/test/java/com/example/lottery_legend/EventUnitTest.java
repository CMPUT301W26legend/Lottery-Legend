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
        String location = "Central Park";
        String startDate = "2025-07-01";
        String endDate = "2025-07-01";
        String regStart = "2025-06-01";
        String regEnd = "2025-06-15";
        String drawDate = "2025-06-16";
        int capacity = 500;
        Integer maxWaitingList = 1000;

        Event event = new Event(organizerId, title, description, geoEnabled, location,
                startDate, endDate, regStart, regEnd, drawDate, capacity, maxWaitingList);

        assertNotNull(event.getEventId());
        assertEquals(organizerId, event.getOrganizerId());
        assertEquals(title, event.getTitle());
        assertEquals(description, event.getDescription());
        assertTrue(event.isGeoEnabled());
        assertEquals(location, event.getLocation());
        assertEquals(startDate, event.getEventStartDate());
        assertEquals(endDate, event.getEventEndDate());
        assertEquals(regStart, event.getRegistrationStartDate());
        assertEquals(regEnd, event.getRegistrationEndDate());
        assertEquals(drawDate, event.getDrawDate());
        assertEquals(capacity, event.getCapacity());
        assertEquals(maxWaitingList, event.getMaxWaitingList());
        assertEquals("open", event.getStatus());
        assertNotNull(event.getWaitingList());
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

        List<String> waitingList = new ArrayList<>();
        waitingList.add("user1");
        event.setWaitingList(waitingList);
        assertEquals(1, event.getWaitingList().size());
        assertEquals("user1", event.getWaitingList().get(0));
    }

    @Test
    public void testEmptyConstructor() {
        Event event = new Event();
        assertNull(event.getEventId());
        assertNotNull(event.getWaitingList());
        assertTrue(event.getWaitingList().isEmpty());
    }
}
