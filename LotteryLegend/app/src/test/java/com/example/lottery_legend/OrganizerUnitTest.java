package com.example.lottery_legend;

import com.google.firebase.Timestamp;
import org.junit.Test;
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
        String userId = "org123";
        Timestamp joinDate = new Timestamp(new Date());
        boolean isAdmin = true;

        Organizer organizer = new Organizer(name, email, phone, userId, joinDate, isAdmin);

        assertEquals(name, organizer.getName());
        assertEquals(email, organizer.getEmail());
        assertEquals(phone, organizer.getPhone());
        assertEquals(userId, organizer.getUserId());
        assertEquals(joinDate, organizer.getJoinDate());
        assertTrue(organizer.getIsAdmin());
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

        organizer.setUserId("newId");
        assertEquals("newId", organizer.getUserId());

        Timestamp newDate = new Timestamp(new Date());
        organizer.setJoinDate(newDate);
        assertEquals(newDate, organizer.getJoinDate());

        organizer.setIsAdmin(false);
        assertFalse(organizer.getIsAdmin());
    }

    @Test
    public void testEmptyConstructor() {
        Organizer organizer = new Organizer();
        assertNull(organizer.getName());
        assertNull(organizer.getEmail());
        assertNull(organizer.getPhone());
        assertNull(organizer.getUserId());
        assertNull(organizer.getJoinDate());
        assertFalse(organizer.getIsAdmin());
    }
}
