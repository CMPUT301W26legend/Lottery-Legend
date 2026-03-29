package com.example.lottery_legend;

import com.google.firebase.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Model class representing an Event in the Lottery Legend system.
 * Designed for Firebase Firestore compatibility.
 */
public class Event {

    /** Unique identifier for the event. */
    private String eventId;
    /** Unique identifier for the organizer. */
    private String organizerId;

    /** Title of the event. */
    private String title;
    /** Detailed description of the event. */
    private String description;

    /** Location details of the event. */
    private EventLocation eventLocation;
    /** Entry price for the event. */
    private double price;

    /** Flag indicating if the event is private. */
    private boolean isPrivateEvent;
    /** Flag indicating if geolocation is required for registration. */
    private boolean geoEnabled;

    /** Timestamp when the event starts. */
    private Timestamp eventStartAt;
    /** Timestamp when the event ends. */
    private Timestamp eventEndAt;

    /** Timestamp when registration opens. */
    private Timestamp registrationStartAt;
    /** Timestamp when registration closes. */
    private Timestamp registrationEndAt;

    /** Timestamp when the lottery draw occurs. */
    private Timestamp drawAt;

    /** Maximum number of participants allowed to be selected. */
    private int capacity;
    /** Maximum size of the waiting list. Null means no limit. */
    private Integer maxWaitingList;

    /** Current number of entrants on the waiting list. */
    private int waitingListCount;
    /** Current number of entrants selected. */
    private int selectedCount;
    /** Current number of entrants who cancelled. */
    private int cancelledCount;
    /** Current number of entrants who enrolled. */
    private int enrolledCount;

    /** URL or Base64 string of the event poster image. */
    private String posterImage;
    /** URL or Base64 string of the QR code image. */
    private String qrCodeImage;
    /** Value encoded in the QR code. */
    private String qrCodeValue;

    /** Guidelines for the lottery process. */
    private String lotteryGuidelines;
    /** Current status of the event (e.g., open, closed, drawn). */
    private String status;

    /** Timestamp when the event document was created. */
    private Timestamp createdAt;
    /** Timestamp when the event document was last updated. */
    private Timestamp updatedAt;

    /** List of entrants on the waiting list. */
    private List<WaitingListEntry> waitingList;
    /** List of comments associated with the event. */
    private List<Comment> comments;
    /** List of co-organizers for the event. */
    private List<CoOrganizer> coOrganizers;
    /** List of tickets issued for the event. */
    private List<Ticket> tickets;

    /**
     * Default no-argument constructor required for Firebase Firestore.
     */
    public Event() {}

    /**
     * Full constructor to initialize all fields of the Event.
     */
    public Event(String eventId, String organizerId, String title, String description, EventLocation eventLocation, double price, boolean isPrivateEvent, boolean geoEnabled, Timestamp eventStartAt, Timestamp eventEndAt, Timestamp registrationStartAt, Timestamp registrationEndAt, Timestamp drawAt, int capacity, Integer maxWaitingList, int waitingListCount, int selectedCount, int cancelledCount, int enrolledCount, String posterImage, String qrCodeImage, String qrCodeValue, String lotteryGuidelines, String status, Timestamp createdAt, Timestamp updatedAt, List<WaitingListEntry> waitingList, List<Comment> comments, List<CoOrganizer> coOrganizers, List<Ticket> tickets) {
        this.eventId = eventId;
        this.organizerId = organizerId;
        this.title = title;
        this.description = description;
        this.eventLocation = eventLocation;
        this.price = price;
        this.isPrivateEvent = isPrivateEvent;
        this.geoEnabled = geoEnabled;
        this.eventStartAt = eventStartAt;
        this.eventEndAt = eventEndAt;
        this.registrationStartAt = registrationStartAt;
        this.registrationEndAt = registrationEndAt;
        this.drawAt = drawAt;
        this.capacity = capacity;
        this.maxWaitingList = maxWaitingList;
        this.waitingListCount = waitingListCount;
        this.selectedCount = selectedCount;
        this.cancelledCount = cancelledCount;
        this.enrolledCount = enrolledCount;
        this.posterImage = posterImage;
        this.qrCodeImage = qrCodeImage;
        this.qrCodeValue = qrCodeValue;
        this.lotteryGuidelines = lotteryGuidelines;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.waitingList = waitingList;
        this.comments = comments;
        this.coOrganizers = coOrganizers;
        this.tickets = tickets;
    }

    /** @return Unique identifier for the event. */
    public String getEventId() { return eventId; }
    /** @param eventId Unique identifier for the event. */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /** @return Unique identifier for the organizer. */
    public String getOrganizerId() { return organizerId; }
    /** @param organizerId Unique identifier for the organizer. */
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    /** @return Title of the event. */
    public String getTitle() { return title; }
    /** @param title Title of the event. */
    public void setTitle(String title) { this.title = title; }

    /** @return Detailed description of the event. */
    public String getDescription() { return description; }
    /** @param description Detailed description of the event. */
    public void setDescription(String description) { this.description = description; }

    /** @return Location details of the event. */
    public EventLocation getEventLocation() { return eventLocation; }
    /** @param eventLocation Location details of the event. */
    public void setEventLocation(EventLocation eventLocation) { this.eventLocation = eventLocation; }

    /** @return Entry price for the event. */
    public double getPrice() { return price; }
    /** @param price Entry price for the event. */
    public void setPrice(double price) { this.price = price; }

    /** @return Flag indicating if the event is private. */
    public boolean isIsPrivateEvent() { return isPrivateEvent; }
    /** @param privateEvent Flag indicating if the event is private. */
    public void setIsPrivateEvent(boolean privateEvent) { isPrivateEvent = privateEvent; }

    /** @return Flag indicating if geolocation is required for registration. */
    public boolean isGeoEnabled() { return geoEnabled; }
    /** @param geoEnabled Flag indicating if geolocation is required for registration. */
    public void setGeoEnabled(boolean geoEnabled) { this.geoEnabled = geoEnabled; }

    /** @return Timestamp when the event starts. */
    public Timestamp getEventStartAt() { return eventStartAt; }
    /** @param eventStartAt Timestamp when the event starts. */
    public void setEventStartAt(Timestamp eventStartAt) { this.eventStartAt = eventStartAt; }

    /** @return Timestamp when the event ends. */
    public Timestamp getEventEndAt() { return eventEndAt; }
    /** @param eventEndAt Timestamp when the event ends. */
    public void setEventEndAt(Timestamp eventEndAt) { this.eventEndAt = eventEndAt; }

    /** @return Timestamp when registration opens. */
    public Timestamp getRegistrationStartAt() { return registrationStartAt; }
    /** @param registrationStartAt Timestamp when registration opens. */
    public void setRegistrationStartAt(Timestamp registrationStartAt) { this.registrationStartAt = registrationStartAt; }

    /** @return Timestamp when registration closes. */
    public Timestamp getRegistrationEndAt() { return registrationEndAt; }
    /** @param registrationEndAt Timestamp when registration closes. */
    public void setRegistrationEndAt(Timestamp registrationEndAt) { this.registrationEndAt = registrationEndAt; }

    /** @return Timestamp when the lottery draw occurs. */
    public Timestamp getDrawAt() { return drawAt; }
    /** @param drawAt Timestamp when the lottery draw occurs. */
    public void setDrawAt(Timestamp drawAt) { this.drawAt = drawAt; }

    /** @return Maximum number of participants allowed to be selected. */
    public int getCapacity() { return capacity; }
    /** @param capacity Maximum number of participants allowed to be selected. */
    public void setCapacity(int capacity) { this.capacity = capacity; }

    /** @return Maximum size of the waiting list. Null means no limit. */
    public Integer getMaxWaitingList() { return maxWaitingList; }
    /** @param maxWaitingList Maximum size of the waiting list. Null means no limit. */
    public void setMaxWaitingList(Integer maxWaitingList) { this.maxWaitingList = maxWaitingList; }

    /** @return Current number of entrants on the waiting list. */
    public int getWaitingListCount() { return waitingListCount; }
    /** @param waitingListCount Current number of entrants on the waiting list. */
    public void setWaitingListCount(int waitingListCount) { this.waitingListCount = waitingListCount; }

    /** @return Current number of entrants selected. */
    public int getSelectedCount() { return selectedCount; }
    /** @param selectedCount Current number of entrants selected. */
    public void setSelectedCount(int selectedCount) { this.selectedCount = selectedCount; }

    /** @return Current number of entrants who cancelled. */
    public int getCancelledCount() { return cancelledCount; }
    /** @param cancelledCount Current number of entrants who cancelled. */
    public void setCancelledCount(int cancelledCount) { this.cancelledCount = cancelledCount; }

    /** @return Current number of entrants who enrolled. */
    public int getEnrolledCount() { return enrolledCount; }
    /** @param enrolledCount Current number of entrants who enrolled. */
    public void setEnrolledCount(int enrolledCount) { this.enrolledCount = enrolledCount; }

    /** @return URL or Base64 string of the event poster image. */
    public String getPosterImage() { return posterImage; }
    /** @param posterImage URL or Base64 string of the event poster image. */
    public void setPosterImage(String posterImage) { this.posterImage = posterImage; }

    /** @return URL or Base64 string of the QR code image. */
    public String getQrCodeImage() { return qrCodeImage; }
    /** @param qrCodeImage URL or Base64 string of the QR code image. */
    public void setQrCodeImage(String qrCodeImage) { this.qrCodeImage = qrCodeImage; }

    /** @return Value encoded in the QR code. */
    public String getQrCodeValue() { return qrCodeValue; }
    /** @param qrCodeValue Value encoded in the QR code. */
    public void setQrCodeValue(String qrCodeValue) { this.qrCodeValue = qrCodeValue; }

    /** @return Guidelines for the lottery process. */
    public String getLotteryGuidelines() { return lotteryGuidelines; }
    /** @param lotteryGuidelines Guidelines for the lottery process. */
    public void setLotteryGuidelines(String lotteryGuidelines) { this.lotteryGuidelines = lotteryGuidelines; }

    /** @return Current status of the event (e.g., open, closed, drawn). */
    public String getStatus() { return status; }
    /** @param status Current status of the event (e.g., open, closed, drawn). */
    public void setStatus(String status) { this.status = status; }

    /** @return Timestamp when the event document was created. */
    public Timestamp getCreatedAt() { return createdAt; }
    /** @param createdAt Timestamp when the event document was created. */
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    /** @return Timestamp when the event document was last updated. */
    public Timestamp getUpdatedAt() { return updatedAt; }
    /** @param updatedAt Timestamp when the event document was last updated. */
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    /** @return List of entrants on the waiting list. */
    public List<WaitingListEntry> getWaitingList() { return waitingList; }
    /** @param waitingList List of entrants on the waiting list. */
    public void setWaitingList(List<WaitingListEntry> waitingList) { this.waitingList = waitingList; }

    /** @return List of comments associated with the event. */
    public List<Comment> getComments() { return comments; }
    /** @param comments List of comments associated with the event. */
    public void setComments(List<Comment> comments) { this.comments = comments; }

    /** @return List of co-organizers for the event. */
    public List<CoOrganizer> getCoOrganizers() { return coOrganizers; }
    /** @param coOrganizers List of co-organizers for the event. */
    public void setCoOrganizers(List<CoOrganizer> coOrganizers) { this.coOrganizers = coOrganizers; }

    /** @return List of tickets issued for the event. */
    public List<Ticket> getTickets() { return tickets; }
    /** @param tickets List of tickets issued for the event. */
    public void setTickets(List<Ticket> tickets) { this.tickets = tickets; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(eventId, event.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventId='" + eventId + '\'' +
                ", organizerId='" + organizerId + '\'' +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    /**
     * Represents the location of an event.
     */
    public static class EventLocation {
        private String name;
        private String address;
        private Double latitude;
        private Double longitude;

        /** Default constructor for Firestore. */
        public EventLocation() {}

        /** Full constructor. */
        public EventLocation(String name, String address, Double latitude, Double longitude) {
            this.name = name;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }

        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }

        @Override
        public String toString() {
            return "EventLocation{" + "name='" + name + '\'' + ", address='" + address + '\'' + '}';
        }
    }

    /**
     * Represents an entry in the event's waiting list.
     */
    public static class WaitingListEntry {
        private String deviceId;
        private Timestamp joinedAt;
        private Timestamp updatedAt;
        private String participationStatus;
        private Timestamp inviteSentAt;
        private Timestamp respondedAt;
        private Timestamp selectedAt;
        private Timestamp confirmedAt;
        private Timestamp declinedAt;
        private Timestamp cancelledAt;
        private Timestamp enrolledAt;
        private Timestamp leftAt;
        private boolean isReplacement;
        private int selectionRound;
        private boolean joinedWithGeo;
        private Double joinLatitude;
        private Double joinLongitude;
        private String note;

        /** Default constructor for Firestore. */
        public WaitingListEntry() {}

        /** Full constructor. */
        public WaitingListEntry(String deviceId, Timestamp joinedAt, Timestamp updatedAt, String participationStatus, Timestamp inviteSentAt, Timestamp respondedAt, Timestamp selectedAt, Timestamp confirmedAt, Timestamp declinedAt, Timestamp cancelledAt, Timestamp enrolledAt, Timestamp leftAt, boolean isReplacement, int selectionRound, boolean joinedWithGeo, Double joinLatitude, Double joinLongitude, String note) {
            this.deviceId = deviceId;
            this.joinedAt = joinedAt;
            this.updatedAt = updatedAt;
            this.participationStatus = participationStatus;
            this.inviteSentAt = inviteSentAt;
            this.respondedAt = respondedAt;
            this.selectedAt = selectedAt;
            this.confirmedAt = confirmedAt;
            this.declinedAt = declinedAt;
            this.cancelledAt = cancelledAt;
            this.enrolledAt = enrolledAt;
            this.leftAt = leftAt;
            this.isReplacement = isReplacement;
            this.selectionRound = selectionRound;
            this.joinedWithGeo = joinedWithGeo;
            this.joinLatitude = joinLatitude;
            this.joinLongitude = joinLongitude;
            this.note = note;
        }

        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

        public Timestamp getJoinedAt() { return joinedAt; }
        public void setJoinedAt(Timestamp joinedAt) { this.joinedAt = joinedAt; }

        public Timestamp getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

        public String getParticipationStatus() { return participationStatus; }
        public void setParticipationStatus(String participationStatus) { this.participationStatus = participationStatus; }

        public Timestamp getInviteSentAt() { return inviteSentAt; }
        public void setInviteSentAt(Timestamp inviteSentAt) { this.inviteSentAt = inviteSentAt; }

        public Timestamp getRespondedAt() { return respondedAt; }
        public void setRespondedAt(Timestamp respondedAt) { this.respondedAt = respondedAt; }

        public Timestamp getSelectedAt() { return selectedAt; }
        public void setSelectedAt(Timestamp selectedAt) { this.selectedAt = selectedAt; }

        public Timestamp getConfirmedAt() { return confirmedAt; }
        public void setConfirmedAt(Timestamp confirmedAt) { this.confirmedAt = confirmedAt; }

        public Timestamp getDeclinedAt() { return declinedAt; }
        public void setDeclinedAt(Timestamp declinedAt) { this.declinedAt = declinedAt; }

        public Timestamp getCancelledAt() { return cancelledAt; }
        public void setCancelledAt(Timestamp cancelledAt) { this.cancelledAt = cancelledAt; }

        public Timestamp getEnrolledAt() { return enrolledAt; }
        public void setEnrolledAt(Timestamp enrolledAt) { this.enrolledAt = enrolledAt; }

        public Timestamp getLeftAt() { return leftAt; }
        public void setLeftAt(Timestamp leftAt) { this.leftAt = leftAt; }

        public boolean isIsReplacement() { return isReplacement; }
        public void setIsReplacement(boolean replacement) { isReplacement = replacement; }

        public int getSelectionRound() { return selectionRound; }
        public void setSelectionRound(int selectionRound) { this.selectionRound = selectionRound; }

        public boolean isJoinedWithGeo() { return joinedWithGeo; }
        public void setJoinedWithGeo(boolean joinedWithGeo) { this.joinedWithGeo = joinedWithGeo; }

        public Double getJoinLatitude() { return joinLatitude; }
        public void setJoinLatitude(Double joinLatitude) { this.joinLatitude = joinLatitude; }

        public Double getJoinLongitude() { return joinLongitude; }
        public void setJoinLongitude(Double joinLongitude) { this.joinLongitude = joinLongitude; }

        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }

        @Override
        public String toString() {
            return "WaitingListEntry{" + "deviceId='" + deviceId + '\'' + ", participationStatus='" + participationStatus + '\'' + '}';
        }
    }

    /**
     * Represents a comment on an event.
     */
    public static class Comment {
        private String commentId;
        private String authorId;
        private String authorType;
        private String authorNameSnapshot;
        private String content;
        private Timestamp createdAt;
        private Timestamp updatedAt;
        private String parentCommentId;
        private String rootCommentId;
        private int threadLevel;
        private String replyToUserId;
        private String replyToUserNameSnapshot;
        private Map<String, Integer> reactionSummary;
        private int totalReactionCount;

        /** Default constructor for Firestore. */
        public Comment() {}

        /** Full constructor. */
        public Comment(String commentId, String authorId, String authorType, String authorNameSnapshot, String content, Timestamp createdAt, Timestamp updatedAt, String parentCommentId, String rootCommentId, int threadLevel, String replyToUserId, String replyToUserNameSnapshot, Map<String, Integer> reactionSummary, int totalReactionCount) {
            this.commentId = commentId;
            this.authorId = authorId;
            this.authorType = authorType;
            this.authorNameSnapshot = authorNameSnapshot;
            this.content = content;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.parentCommentId = parentCommentId;
            this.rootCommentId = rootCommentId;
            this.threadLevel = threadLevel;
            this.replyToUserId = replyToUserId;
            this.replyToUserNameSnapshot = replyToUserNameSnapshot;
            this.reactionSummary = reactionSummary;
            this.totalReactionCount = totalReactionCount;
        }

        public String getCommentId() { return commentId; }
        public void setCommentId(String commentId) { this.commentId = commentId; }

        public String getAuthorId() { return authorId; }
        public void setAuthorId(String authorId) { this.authorId = authorId; }

        public String getAuthorType() { return authorType; }
        public void setAuthorType(String authorType) { this.authorType = authorType; }

        public String getAuthorNameSnapshot() { return authorNameSnapshot; }
        public void setAuthorNameSnapshot(String authorNameSnapshot) { this.authorNameSnapshot = authorNameSnapshot; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public Timestamp getCreatedAt() { return createdAt; }
        public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

        public Timestamp getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

        public String getParentCommentId() { return parentCommentId; }
        public void setParentCommentId(String parentCommentId) { this.parentCommentId = parentCommentId; }

        public String getRootCommentId() { return rootCommentId; }
        public void setRootCommentId(String rootCommentId) { this.rootCommentId = rootCommentId; }

        public int getThreadLevel() { return threadLevel; }
        public void setThreadLevel(int threadLevel) { this.threadLevel = threadLevel; }

        public String getReplyToUserId() { return replyToUserId; }
        public void setReplyToUserId(String replyToUserId) { this.replyToUserId = replyToUserId; }

        public String getReplyToUserNameSnapshot() { return replyToUserNameSnapshot; }
        public void setReplyToUserNameSnapshot(String replyToUserNameSnapshot) { this.replyToUserNameSnapshot = replyToUserNameSnapshot; }

        public Map<String, Integer> getReactionSummary() { return reactionSummary; }
        public void setReactionSummary(Map<String, Integer> reactionSummary) { this.reactionSummary = reactionSummary; }

        public int getTotalReactionCount() { return totalReactionCount; }
        public void setTotalReactionCount(int totalReactionCount) { this.totalReactionCount = totalReactionCount; }

        @Override
        public String toString() {
            return "Comment{" + "commentId='" + commentId + '\'' + ", authorNameSnapshot='" + authorNameSnapshot + '\'' + '}';
        }
    }

    /**
     * Represents a co-organizer for an event.
     */
    public static class CoOrganizer {
        private String deviceId;
        private String organizerId;
        private String status;
        private Timestamp invitedAt;
        private Timestamp respondedAt;

        /** Default constructor for Firestore. */
        public CoOrganizer() {}

        /** Full constructor. */
        public CoOrganizer(String deviceId, String organizerId, String status, Timestamp invitedAt, Timestamp respondedAt) {
            this.deviceId = deviceId;
            this.organizerId = organizerId;
            this.status = status;
            this.invitedAt = invitedAt;
            this.respondedAt = respondedAt;
        }

        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

        public String getOrganizerId() { return organizerId; }
        public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Timestamp getInvitedAt() { return invitedAt; }
        public void setInvitedAt(Timestamp invitedAt) { this.invitedAt = invitedAt; }

        public Timestamp getRespondedAt() { return respondedAt; }
        public void setRespondedAt(Timestamp respondedAt) { this.respondedAt = respondedAt; }

        @Override
        public String toString() {
            return "CoOrganizer{" + "deviceId='" + deviceId + '\'' + ", status='" + status + '\'' + '}';
        }
    }

    /**
     * Represents a ticket for an event.
     */
    public static class Ticket {
        private String ticketId;
        private String deviceId;
        private String eventId;
        private String entrantNameSnapshot;
        private String entrantEmailSnapshot;
        private String organizerId;
        private String eventTitleSnapshot;
        private String eventLocationSnapshot;
        private Timestamp eventStartAtSnapshot;
        private Timestamp eventEndAtSnapshot;
        private double priceSnapshot;
        private Timestamp issuedAt;
        private String ticketStatus;
        private String pdfBase64;
        private String ticketCode;
        private String qrCodeValue;

        /** Default constructor for Firestore. */
        public Ticket() {}

        /** Full constructor. */
        public Ticket(String ticketId, String deviceId, String eventId, String entrantNameSnapshot, String entrantEmailSnapshot, String organizerId, String eventTitleSnapshot, String eventLocationSnapshot, Timestamp eventStartAtSnapshot, Timestamp eventEndAtSnapshot, double priceSnapshot, Timestamp issuedAt, String ticketStatus, String pdfBase64, String ticketCode, String qrCodeValue) {
            this.ticketId = ticketId;
            this.deviceId = deviceId;
            this.eventId = eventId;
            this.entrantNameSnapshot = entrantNameSnapshot;
            this.entrantEmailSnapshot = entrantEmailSnapshot;
            this.organizerId = organizerId;
            this.eventTitleSnapshot = eventTitleSnapshot;
            this.eventLocationSnapshot = eventLocationSnapshot;
            this.eventStartAtSnapshot = eventStartAtSnapshot;
            this.eventEndAtSnapshot = eventEndAtSnapshot;
            this.priceSnapshot = priceSnapshot;
            this.issuedAt = issuedAt;
            this.ticketStatus = ticketStatus;
            this.pdfBase64 = pdfBase64;
            this.ticketCode = ticketCode;
            this.qrCodeValue = qrCodeValue;
        }

        public String getTicketId() { return ticketId; }
        public void setTicketId(String ticketId) { this.ticketId = ticketId; }

        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }

        public String getEntrantNameSnapshot() { return entrantNameSnapshot; }
        public void setEntrantNameSnapshot(String entrantNameSnapshot) { this.entrantNameSnapshot = entrantNameSnapshot; }

        public String getEntrantEmailSnapshot() { return entrantEmailSnapshot; }
        public void setEntrantEmailSnapshot(String entrantEmailSnapshot) { this.entrantEmailSnapshot = entrantEmailSnapshot; }

        public String getOrganizerId() { return organizerId; }
        public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

        public String getEventTitleSnapshot() { return eventTitleSnapshot; }
        public void setEventTitleSnapshot(String eventTitleSnapshot) { this.eventTitleSnapshot = eventTitleSnapshot; }

        public String getEventLocationSnapshot() { return eventLocationSnapshot; }
        public void setEventLocationSnapshot(String eventLocationSnapshot) { this.eventLocationSnapshot = eventLocationSnapshot; }

        public Timestamp getEventStartAtSnapshot() { return eventStartAtSnapshot; }
        public void setEventStartAtSnapshot(Timestamp eventStartAtSnapshot) { this.eventStartAtSnapshot = eventStartAtSnapshot; }

        public Timestamp getEventEndAtSnapshot() { return eventEndAtSnapshot; }
        public void setEventEndAtSnapshot(Timestamp eventEndAtSnapshot) { this.eventEndAtSnapshot = eventEndAtSnapshot; }

        public double getPriceSnapshot() { return priceSnapshot; }
        public void setPriceSnapshot(double priceSnapshot) { this.priceSnapshot = priceSnapshot; }

        public Timestamp getIssuedAt() { return issuedAt; }
        public void setIssuedAt(Timestamp issuedAt) { this.issuedAt = issuedAt; }

        public String getTicketStatus() { return ticketStatus; }
        public void setTicketStatus(String ticketStatus) { this.ticketStatus = ticketStatus; }

        public String getPdfBase64() { return pdfBase64; }
        public void setPdfBase64(String pdfBase64) { this.pdfBase64 = pdfBase64; }

        public String getTicketCode() { return ticketCode; }
        public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }

        public String getQrCodeValue() { return qrCodeValue; }
        public void setQrCodeValue(String qrCodeValue) { this.qrCodeValue = qrCodeValue; }

        @Override
        public String toString() {
            return "Ticket{" + "ticketId='" + ticketId + '\'' + ", ticketStatus='" + ticketStatus + '\'' + '}';
        }
    }
}
