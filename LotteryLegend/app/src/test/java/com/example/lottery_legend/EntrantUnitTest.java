package com.example.lottery_legend;

import com.example.lottery_legend.model.Entrant;
import com.google.firebase.Timestamp;
import org.junit.Test;
import java.util.Date;
import static org.junit.Assert.*;

/**
 * Unit test for the Entrant model class.
 */
public class EntrantUnitTest {

    @Test
    public void testEntrantConstructorAndGetters() {
        String name = "John Doe";
        String email = "john@example.com";
        String phone = "1234567890";
        boolean notification = true;
        String deviceId = "user123";
        Timestamp joinDate = new Timestamp(new Date());
        Timestamp updatedAt = new Timestamp(new Date());
        boolean isAdmin = false;

        Entrant entrant = new Entrant(deviceId, name, email, phone, notification, joinDate, updatedAt, isAdmin);

        assertEquals(deviceId, entrant.getDeviceId());
        assertEquals(name, entrant.getName());
        assertEquals(email, entrant.getEmail());
        assertEquals(phone, entrant.getPhone());
        assertTrue(entrant.isNotificationsEnabled());
        assertEquals(joinDate, entrant.getJoinDate());
        assertEquals(updatedAt, entrant.getUpdatedAt());
        assertFalse(entrant.getIsAdmin());
    }

    @Test
    public void testEmptyConstructor() {
        Entrant entrant = new Entrant();
        assertNull(entrant.getDeviceId());
        assertNull(entrant.getName());
        assertNull(entrant.getEmail());
        assertNull(entrant.getPhone());
        assertFalse(entrant.isNotificationsEnabled());
        assertNull(entrant.getJoinDate());
        assertNull(entrant.getUpdatedAt());
        assertFalse(entrant.getIsAdmin());
    }
}