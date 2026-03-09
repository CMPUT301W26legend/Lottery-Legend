package com.example.lottery_legend;

import org.junit.Test;
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

        Entrant entrant = new Entrant(name, email, phone, notification);

        assertEquals(name, entrant.name);
        assertEquals(email, entrant.email);
        assertEquals(phone, entrant.phone);
        assertTrue(entrant.notification);
    }

    @Test
    public void testEmptyConstructor() {
        Entrant entrant = new Entrant();
        assertNull(entrant.name);
        assertNull(entrant.email);
        assertNull(entrant.phone);
        assertFalse(entrant.notification);
    }
}