package com.example.lottery_legend;

import com.example.lottery_legend.model.Comment;
import com.example.lottery_legend.model.Entrant;
import com.example.lottery_legend.model.Event;
import com.example.lottery_legend.model.Notification;
import com.example.lottery_legend.model.Organizer;
import com.example.lottery_legend.model.Reaction;
import com.google.firebase.Timestamp;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Combined unit tests for all models to ensure full coverage.
 */
public class ModelUnitTests {

    @Test
    public void testEntrantFullConstructor() {
        Timestamp now = new Timestamp(new Date());
        Entrant entrant = new Entrant("dev1", "Name", "email@test.com", "123", true, now, now, true, "base64img");
        
        assertEquals("dev1", entrant.getDeviceId());
        assertEquals("Name", entrant.getName());
        assertEquals("email@test.com", entrant.getEmail());
        assertEquals("123", entrant.getPhone());
        assertTrue(entrant.isNotificationsEnabled());
        assertEquals(now, entrant.getJoinDate());
        assertEquals(now, entrant.getUpdatedAt());
        assertTrue(entrant.getIsAdmin());
        assertEquals("base64img", entrant.getProfileImage());
    }

    @Test
    public void testEntrantEquals() {
        Entrant e1 = new Entrant();
        e1.setDeviceId("id1");
        Entrant e2 = new Entrant();
        e2.setDeviceId("id1");
        Entrant e3 = new Entrant();
        e3.setDeviceId("id2");

        assertEquals(e1, e2);
        assertNotEquals(e1, e3);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1, null);
        assertNotEquals(e1, new Object());
    }

    @Test
    public void testEventLocation() {
        Event.EventLocation loc = new Event.EventLocation("Name", "Address", 1.0, 2.0);
        assertEquals("Name", loc.getName());
        assertEquals("Address", loc.getAddress());
        assertEquals(Double.valueOf(1.0), loc.getLatitude());
        assertEquals(Double.valueOf(2.0), loc.getLongitude());
        
        loc.setName("New");
        loc.setAddress("New Addr");
        loc.setLatitude(3.0);
        loc.setLongitude(4.0);
        
        assertEquals("New", loc.getName());
        assertEquals("New Addr", loc.getAddress());
        assertEquals(Double.valueOf(3.0), loc.getLatitude());
        assertEquals(Double.valueOf(4.0), loc.getLongitude());
        assertTrue(loc.toString().contains("New"));
    }

    @Test
    public void testWaitingListEntry() {
        Timestamp now = Timestamp.now();
        Event.WaitingListEntry entry = new Event.WaitingListEntry();
        entry.setDeviceId("user1");
        entry.setParticipationStatus("waiting");
        entry.setJoinedAt(now);
        entry.setFinalResult("WIN");
        entry.setInviteSentAt(now);
        entry.setRespondedAt(now);
        entry.setSelectedAt(now);
        entry.setConfirmedAt(now);
        entry.setDeclinedAt(now);
        entry.setCancelledAt(now);
        entry.setEnrolledAt(now);
        entry.setLeftAt(now);
        entry.setIsReplacement(true);
        entry.setSelectionRound(2);
        entry.setJoinedWithGeo(true);
        entry.setJoinLatitude(10.0);
        entry.setJoinLongitude(20.0);
        entry.setNote("Note");
        
        assertEquals("user1", entry.getDeviceId());
        assertEquals("waiting", entry.getParticipationStatus());
        assertEquals(now, entry.getJoinedAt());
        assertEquals("WIN", entry.getFinalResult());
        assertEquals(now, entry.getInviteSentAt());
        assertEquals(now, entry.getRespondedAt());
        assertEquals(now, entry.getSelectedAt());
        assertEquals(now, entry.getConfirmedAt());
        assertEquals(now, entry.getDeclinedAt());
        assertEquals(now, entry.getCancelledAt());
        assertEquals(now, entry.getEnrolledAt());
        assertEquals(now, entry.getLeftAt());
        assertTrue(entry.isIsReplacement());
        assertEquals(2, entry.getSelectionRound());
        assertTrue(entry.isJoinedWithGeo());
        assertEquals(Double.valueOf(10.0), entry.getJoinLatitude());
        assertEquals(Double.valueOf(20.0), entry.getJoinLongitude());
        assertEquals("Note", entry.getNote());
        assertTrue(entry.toString().contains("user1"));
    }

    @Test
    public void testOrganizerFullConstructor() {
        Timestamp now = Timestamp.now();
        List<Organizer.CreatedEvent> events = new ArrayList<>();
        Organizer org = new Organizer("org1", "OrgName", "org@test.com", "555", now, now, false, events);
        org.setProfileImage("img");
        
        assertEquals("org1", org.getDeviceId());
        assertEquals("OrgName", org.getName());
        assertEquals("org@test.com", org.getEmail());
        assertFalse(org.getIsAdmin());
        assertEquals(events, org.getCreatedEvents());
        assertEquals("img", org.getProfileImage());
        assertEquals("org1", org.getUserId());
        
        org.setUserId("org2");
        assertEquals("org2", org.getDeviceId());
    }

    @Test
    public void testEventGettersSettersExtra() {
        Event event = new Event();
        Timestamp now = Timestamp.now();
        
        event.setEventId("e1");
        event.setOrganizerId("o1");
        event.setTitle("T");
        event.setDescription("D");
        event.setPrice(10.5);
        event.setIsPrivateEvent(true);
        event.setGeoEnabled(true);
        event.setEventStartAt(now);
        event.setEventEndAt(now);
        event.setRegistrationStartAt(now);
        event.setRegistrationEndAt(now);
        event.setDrawAt(now);
        event.setCapacity(100);
        event.setMaxWaitingList(200);
        event.setWaitingListCount(10);
        event.setSelectedCount(5);
        event.setCancelledCount(2);
        event.setEnrolledCount(3);
        event.setPosterImage("poster");
        event.setQrCodeImage("qr");
        event.setQrCodeValue("val");
        event.setLotteryGuidelines("guides");
        event.setStatus("open");
        event.setCreatedAt(now);
        event.setUpdatedAt(now);
        
        assertEquals("e1", event.getEventId());
        assertEquals("o1", event.getOrganizerId());
        assertEquals("T", event.getTitle());
        assertEquals("D", event.getDescription());
        assertEquals(10.5, event.getPrice(), 0.001);
        assertTrue(event.isIsPrivateEvent());
        assertTrue(event.isGeoEnabled());
        assertEquals(now, event.getEventStartAt());
        assertEquals(now, event.getEventEndAt());
        assertEquals(now, event.getRegistrationStartAt());
        assertEquals(now, event.getRegistrationEndAt());
        assertEquals(now, event.getDrawAt());
        assertEquals(100, event.getCapacity());
        assertEquals(Integer.valueOf(200), event.getMaxWaitingList());
        assertEquals(10, event.getWaitingListCount());
        assertEquals(5, event.getSelectedCount());
        assertEquals(2, event.getCancelledCount());
        assertEquals(3, event.getEnrolledCount());
        assertEquals("poster", event.getPosterImage());
        assertEquals("qr", event.getQrCodeImage());
        assertEquals("val", event.getQrCodeValue());
        assertEquals("guides", event.getLotteryGuidelines());
        assertEquals("open", event.getStatus());
        assertEquals(now, event.getCreatedAt());
        assertEquals(now, event.getUpdatedAt());
        
        assertTrue(event.toString().contains("T"));
        
        Event event2 = new Event();
        event2.setEventId("e1");
        assertEquals(event, event2);
        assertEquals(event.hashCode(), event2.hashCode());
    }

    @Test
    public void testEventCoOrganizer() {
        Timestamp now = Timestamp.now();
        Event.CoOrganizer coOrg = new Event.CoOrganizer("dev1", "org1", "ACCEPTED", now, now);
        assertEquals("dev1", coOrg.getDeviceId());
        assertEquals("org1", coOrg.getOrganizerId());
        assertEquals("ACCEPTED", coOrg.getStatus());
        assertEquals(now, coOrg.getInvitedAt());
        assertEquals(now, coOrg.getRespondedAt());
        assertTrue(coOrg.toString().contains("dev1"));
    }

    @Test
    public void testEventTicket() {
        Timestamp now = Timestamp.now();
        Event.Ticket ticket = new Event.Ticket("t1", "d1", "e1", "Name", "Email", "o1", "Title", "Loc", now, now, 10.0, now, "VALID", "pdf", "code", "qrval");
        
        assertEquals("t1", ticket.getTicketId());
        assertEquals("d1", ticket.getDeviceId());
        assertEquals("e1", ticket.getEventId());
        assertEquals("Name", ticket.getEntrantNameSnapshot());
        assertEquals("Email", ticket.getEntrantEmailSnapshot());
        assertEquals("o1", ticket.getOrganizerId());
        assertEquals("Title", ticket.getEventTitleSnapshot());
        assertEquals("Loc", ticket.getEventLocationSnapshot());
        assertEquals(now, ticket.getEventStartAtSnapshot());
        assertEquals(now, ticket.getEventEndAtSnapshot());
        assertEquals(10.0, ticket.getPriceSnapshot(), 0.001);
        assertEquals(now, ticket.getIssuedAt());
        assertEquals("VALID", ticket.getTicketStatus());
        assertEquals("pdf", ticket.getPdfBase64());
        assertEquals("code", ticket.getTicketCode());
        assertEquals("qrval", ticket.getQrCodeValue());
        assertTrue(ticket.toString().contains("t1"));
    }

    @Test
    public void testReaction() {
        Timestamp now = Timestamp.now();
        Reaction r = new Reaction("d1", true, false, true, now);
        assertEquals("d1", r.getDeviceId());
        assertTrue(r.isLike());
        assertFalse(r.isLove());
        assertTrue(r.isHelpful());
        assertEquals(now, r.getUpdatedAt());
        
        r.setDeviceId("d2");
        r.setLike(false);
        r.setLove(true);
        r.setHelpful(false);
        r.setUpdatedAt(now);
        
        assertEquals("d2", r.getDeviceId());
        assertFalse(r.isLike());
        assertTrue(r.isLove());
        assertFalse(r.isHelpful());
    }

    @Test
    public void testComment() {
        Comment c = new Comment();
        c.setCommentId("c1");
        c.setAuthorId("a1");
        c.setAuthorType("ENTRANT");
        c.setAuthorNameSnapshot("N");
        c.setContent("C");
        Timestamp now = Timestamp.now();
        c.setCreatedAt(now);
        c.setUpdatedAt(now);
        c.setParentCommentId("p1");
        c.setRootCommentId("r1");
        c.setThreadLevel(1);
        c.setReplyToUserId("u1");
        c.setReplyToUserNameSnapshot("UN");
        c.setLikeCount(1);
        c.setLoveCount(2);
        c.setHelpfulCount(3);
        c.setReactionCount(6);
        c.setReplyCount(5);
        Map<String, Integer> counts = new HashMap<>();
        counts.put("like", 1);
        c.setReactionTypeCounts(counts);
        
        assertEquals("c1", c.getCommentId());
        assertEquals("a1", c.getAuthorId());
        assertEquals("ENTRANT", c.getAuthorType());
        assertEquals("N", c.getAuthorNameSnapshot());
        assertEquals("C", c.getContent());
        assertEquals(now, c.getCreatedAt());
        assertEquals(now, c.getUpdatedAt());
        assertEquals("p1", c.getParentCommentId());
        assertEquals("r1", c.getRootCommentId());
        assertEquals(1, c.getThreadLevel());
        assertEquals("u1", c.getReplyToUserId());
        assertEquals("UN", c.getReplyToUserNameSnapshot());
        assertEquals(1, c.getLikeCount());
        assertEquals(2, c.getLoveCount());
        assertEquals(3, c.getHelpfulCount());
        assertEquals(6, c.getReactionCount());
        assertEquals(5, c.getReplyCount());
        assertEquals(counts, c.getReactionTypeCounts());
    }
}
