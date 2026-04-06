package com.example.lottery_legend;

import com.example.lottery_legend.model.Reaction;
import com.google.firebase.Timestamp;
import org.junit.Test;
import java.util.Date;
import static org.junit.Assert.*;

/**
 * Unit test for the Reaction model class.
 */
public class ReactionUnitTest {

    @Test
    public void testReactionConstructorAndGetters() {
        String deviceId = "device123";
        boolean like = true;
        boolean love = false;
        boolean helpful = true;
        Timestamp now = new Timestamp(new Date());

        Reaction reaction = new Reaction(deviceId, like, love, helpful, now);

        assertEquals(deviceId, reaction.getDeviceId());
        assertTrue(reaction.isLike());
        assertFalse(reaction.isLove());
        assertTrue(reaction.isHelpful());
        assertEquals(now, reaction.getUpdatedAt());
    }

    @Test
    public void testReactionSetters() {
        Reaction reaction = new Reaction();
        String deviceId = "device456";
        Timestamp now = new Timestamp(new Date());

        reaction.setDeviceId(deviceId);
        reaction.setLike(false);
        reaction.setLove(true);
        reaction.setHelpful(false);
        reaction.setUpdatedAt(now);

        assertEquals(deviceId, reaction.getDeviceId());
        assertFalse(reaction.isLike());
        assertTrue(reaction.isLove());
        assertFalse(reaction.isHelpful());
        assertEquals(now, reaction.getUpdatedAt());
    }
}
