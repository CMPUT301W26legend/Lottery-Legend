package com.example.lottery_legend;

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
        String userId = "user123";
        Timestamp joinDate = new Timestamp(new Date());

        Entrant entrant = new Entrant(name, email, phone, notification, userId, joinDate);

        assertEquals(name, entrant.getName());
        assertEquals(email, entrant.getEmail());
        assertEquals(phone, entrant.getPhone());
        assertTrue(entrant.isNotification());
        assertEquals(userId, entrant.getUserId());
        assertEquals(joinDate, entrant.getJoinDate());
    }

    @Test
    public void testEmptyConstructor() {
        Entrant entrant = new Entrant();
        assertNull(entrant.getName());
        assertNull(entrant.getEmail());
        assertNull(entrant.getPhone());
        assertFalse(entrant.isNotification());
        assertNull(entrant.getUserId());
        assertNull(entrant.getJoinDate());
    }
}