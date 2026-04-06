package com.example.lottery_legend.model;

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
    /** Current status of the event (e.g., open, closed, drawn, finalized). */
    private String status;

    /** Timestamp when the event document was created. */
    private Timestamp createdAt;
    /** Timestamp when the event document was last updated. */
    private Timestamp updatedAt;

    /** List of entrants on the waiting list. */
    private List<WaitingListEntry> waitingList;
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
     *
     * @param eventId             Unique identifier for the event.
     * @param organizerId         Unique identifier for the organizer.
     * @param title               Title of the event.
     * @param description         Detailed description of the event.
     * @param eventLocation       Location details of the event.
     * @param price               Entry price for the event.
     * @param isPrivateEvent      Flag indicating if the event is private.
     * @param geoEnabled          Flag indicating if geolocation is required.
     * @param eventStartAt        Timestamp when the event starts.
     * @param eventEndAt          Timestamp when the event ends.
     * @param registrationStartAt Timestamp when registration opens.
     * @param registrationEndAt   Timestamp when registration closes.
     * @param drawAt              Timestamp when the lottery draw occurs.
     * @param capacity            Maximum number of participants allowed.
     * @param maxWaitingList      Maximum size of the waiting list.
     * @param waitingListCount    Current number of entrants on the waiting list.
     * @param selectedCount       Current number of entrants selected.
     * @param cancelledCount      Current number of entrants who cancelled.
     * @param enrolledCount       Current number of entrants who enrolled.
     * @param posterImage         URL or Base64 string of the event poster image.
     * @param qrCodeImage         URL or Base64 string of the QR code image.
     * @param qrCodeValue         Value encoded in the QR code.
     * @param lotteryGuidelines   Guidelines for the lottery process.
     * @param status              Current status of the event.
     * @param createdAt           Timestamp when the event document was created.
     * @param updatedAt           Timestamp when the event document was last updated.
     * @param waitingList         List of entrants on the waiting list.
     * @param coOrganizers        List of co-organizers for the event.
     * @param tickets             List of tickets issued for the event.
     */
    public Event(String eventId, String organizerId, String title, String description, EventLocation eventLocation, double price, boolean isPrivateEvent, boolean geoEnabled, Timestamp eventStartAt, Timestamp eventEndAt, Timestamp registrationStartAt, Timestamp registrationEndAt, Timestamp drawAt, int capacity, Integer maxWaitingList, int waitingListCount, int selectedCount, int cancelledCount, int enrolledCount, String posterImage, String qrCodeImage, String qrCodeValue, String lotteryGuidelines, String status, Timestamp createdAt, Timestamp updatedAt, List<WaitingListEntry> waitingList, List<CoOrganizer> coOrganizers, List<Ticket> tickets) {
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
        this.coOrganizers = coOrganizers;
        this.tickets = tickets;
    }

    /** @return Unique identifier for the event. */
    public String getEventId() { return eventId; }
    /** @param eventId Unique identifier for the event to set. */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /** @return Unique identifier for the organizer. */
    public String getOrganizerId() { return organizerId; }
    /** @param organizerId Unique identifier for the organizer to set. */
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    /** @return Title of the event. */
    public String getTitle() { return title; }
    /** @param title Title of the event to set. */
    public void setTitle(String title) { this.title = title; }

    /** @return Detailed description of the event. */
    public String getDescription() { return description; }
    /** @param description Detailed description of the event to set. */
    public void setDescription(String description) { this.description = description; }

    /** @return Location details of the event. */
    public EventLocation getEventLocation() { return eventLocation; }
    /** @param eventLocation Location details of the event to set. */
    public void setEventLocation(EventLocation eventLocation) { this.eventLocation = eventLocation; }

    /** @return Entry price for the event. */
    public double getPrice() { return price; }
    /** @param price Entry price for the event to set. */
    public void setPrice(double price) { this.price = price; }

    /** @return Flag indicating if the event is private. */
    public boolean isIsPrivateEvent() { return isPrivateEvent; }
    /** @param privateEvent Flag indicating if the event is private to set. */
    public void setIsPrivateEvent(boolean privateEvent) { isPrivateEvent = privateEvent; }

    /** @return Flag indicating if geolocation is required for registration. */
    public boolean isGeoEnabled() { return geoEnabled; }
    /** @param geoEnabled Flag indicating if geolocation is required to set. */
    public void setGeoEnabled(boolean geoEnabled) { this.geoEnabled = geoEnabled; }

    /** @return Timestamp when the event starts. */
    public Timestamp getEventStartAt() { return eventStartAt; }
    /** @param eventStartAt Timestamp when the event starts to set. */
    public void setEventStartAt(Timestamp eventStartAt) { this.eventStartAt = eventStartAt; }

    /** @return Timestamp when the event ends. */
    public Timestamp getEventEndAt() { return eventEndAt; }
    /** @param eventEndAt Timestamp when the event ends to set. */
    public void setEventEndAt(Timestamp eventEndAt) { this.eventEndAt = eventEndAt; }

    /** @return Timestamp when registration opens. */
    public Timestamp getRegistrationStartAt() { return registrationStartAt; }
    /** @param registrationStartAt Timestamp when registration opens to set. */
    public void setRegistrationStartAt(Timestamp registrationStartAt) { this.registrationStartAt = registrationStartAt; }

    /** @return Timestamp when registration closes. */
    public Timestamp getRegistrationEndAt() { return registrationEndAt; }
    /** @param registrationEndAt Timestamp when registration closes to set. */
    public void setRegistrationEndAt(Timestamp registrationEndAt) { this.registrationEndAt = registrationEndAt; }

    /** @return Timestamp when the lottery draw occurs. */
    public Timestamp getDrawAt() { return drawAt; }
    /** @param drawAt Timestamp when the lottery draw occurs to set. */
    public void setDrawAt(Timestamp drawAt) { this.drawAt = drawAt; }

    /** @return Maximum number of participants allowed to be selected. */
    public int getCapacity() { return capacity; }
    /** @param capacity Maximum number of participants allowed to be selected to set. */
    public void setCapacity(int capacity) { this.capacity = capacity; }

    /** @return Maximum size of the waiting list. Null means no limit. */
    public Integer getMaxWaitingList() { return maxWaitingList; }
    /** @param maxWaitingList Maximum size of the waiting list to set. */
    public void setMaxWaitingList(Integer maxWaitingList) { this.maxWaitingList = maxWaitingList; }

    /** @return Current number of entrants on the waiting list. */
    public int getWaitingListCount() { return waitingListCount; }
    /** @param waitingListCount Current number of entrants on the waiting list to set. */
    public void setWaitingListCount(int waitingListCount) { this.waitingListCount = waitingListCount; }

    /** @return Current number of entrants selected. */
    public int getSelectedCount() { return selectedCount; }
    /** @param selectedCount Current number of entrants selected to set. */
    public void setSelectedCount(int selectedCount) { this.selectedCount = selectedCount; }

    /** @return Current number of entrants who cancelled. */
    public int getCancelledCount() { return cancelledCount; }
    /** @param cancelledCount Current number of entrants who cancelled to set. */
    public void setCancelledCount(int cancelledCount) { this.cancelledCount = cancelledCount; }

    /** @return Current number of entrants who enrolled. */
    public int getEnrolledCount() { return enrolledCount; }
    /** @param enrolledCount Current number of entrants who enrolled to set. */
    public void setEnrolledCount(int enrolledCount) { this.enrolledCount = enrolledCount; }

    /** @return URL or Base64 string of the event poster image. */
    public String getPosterImage() { return posterImage; }
    /** @param posterImage URL or Base64 string of the event poster image to set. */
    public void setPosterImage(String posterImage) { this.posterImage = posterImage; }

    /** @return URL or Base64 string of the QR code image. */
    public String getQrCodeImage() { return qrCodeImage; }
    /** @param qrCodeImage URL or Base64 string of the QR code image to set. */
    public void setQrCodeImage(String qrCodeImage) { this.qrCodeImage = qrCodeImage; }

    /** @return Value encoded in the QR code. */
    public String getQrCodeValue() { return qrCodeValue; }
    /** @param qrCodeValue Value encoded in the QR code to set. */
    public void setQrCodeValue(String qrCodeValue) { this.qrCodeValue = qrCodeValue; }

    /** @return Guidelines for the lottery process. */
    public String getLotteryGuidelines() { return lotteryGuidelines; }
    /** @param lotteryGuidelines Guidelines for the lottery process to set. */
    public void setLotteryGuidelines(String lotteryGuidelines) { this.lotteryGuidelines = lotteryGuidelines; }

    /** @return Current status of the event (e.g., open, closed, drawn, finalized). */
    public String getStatus() { return status; }
    /** @param status Current status of the event to set. */
    public void setStatus(String status) { this.status = status; }

    /** @return Timestamp when the event document was created. */
    public Timestamp getCreatedAt() { return createdAt; }
    /** @param createdAt Timestamp when the event document was created to set. */
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    /** @return Timestamp when the event document was last updated. */
    public Timestamp getUpdatedAt() { return updatedAt; }
    /** @param updatedAt Timestamp when the event document was last updated to set. */
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    /** @return List of entrants on the waiting list. */
    public List<WaitingListEntry> getWaitingList() { return waitingList; }
    /** @param waitingList List of entrants on the waiting list to set. */
    public void setWaitingList(List<WaitingListEntry> waitingList) { this.waitingList = waitingList; }

    /** @return List of co-organizers for the event. */
    public List<CoOrganizer> getCoOrganizers() { return coOrganizers; }
    /** @param coOrganizers List of co-organizers for the event to set. */
    public void setCoOrganizers(List<CoOrganizer> coOrganizers) { this.coOrganizers = coOrganizers; }

    /** @return List of tickets issued for the event. */
    public List<Ticket> getTickets() { return tickets; }
    /** @param tickets List of tickets issued for the event to set. */
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
        /** Name of the location. */
        private String name;
        /** Full address of the location. */
        private String address;
        /** Latitude of the location. */
        private Double latitude;
        /** Longitude of the location. */
        private Double longitude;

        /** Default constructor for Firestore. */
        public EventLocation() {}

        /**
         * Full constructor for EventLocation.
         * @param name      Location name.
         * @param address   Location address.
         * @param latitude  Latitude coordinate.
         * @param longitude Longitude coordinate.
         */
        public EventLocation(String name, String address, Double latitude, Double longitude) {
            this.name = name;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        /** @return Name of the location. */
        public String getName() { return name; }
        /** @param name Name of the location to set. */
        public void setName(String name) { this.name = name; }

        /** @return Address of the location. */
        public String getAddress() { return address; }
        /** @param address Address of the location to set. */
        public void setAddress(String address) { this.address = address; }

        /** @return Latitude coordinate. */
        public Double getLatitude() { return latitude; }
        /** @param latitude Latitude coordinate to set. */
        public void setLatitude(Double latitude) { this.latitude = latitude; }

        /** @return Longitude coordinate. */
        public Double getLongitude() { return longitude; }
        /** @param longitude Longitude coordinate to set. */
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
        /** Unique device/user identifier. */
        private String deviceId;
        /** Timestamp when the user joined the waiting list. */
        private Timestamp joinedAt;
        /** Timestamp of the last update to this entry. */
        private Timestamp updatedAt;
        /** Current participation status (e.g., waiting, selected, confirmed). */
        private String participationStatus;
        /** Timestamp when an invitation was sent. */
        private Timestamp inviteSentAt;
        /** Timestamp when the user responded to an invitation. */
        private Timestamp respondedAt;
        /** Timestamp when the user was selected in the lottery. */
        private Timestamp selectedAt;
        /** Timestamp when the user confirmed their participation. */
        private Timestamp confirmedAt;
        /** Timestamp when the user declined their participation. */
        private Timestamp declinedAt;
        /** Timestamp when the user cancelled their participation. */
        private Timestamp cancelledAt;
        /** Timestamp when the user enrolled. */
        private Timestamp enrolledAt;
        /** Timestamp when the user left the waiting list. */
        private Timestamp leftAt;
        /** Flag indicating if the user was selected as a replacement. */
        private boolean isReplacement;
        /** Round number in which the user was selected. */
        private int selectionRound;
        /** Flag indicating if geolocation was used when joining. */
        private boolean joinedWithGeo;
        /** Latitude when the user joined. */
        private Double joinLatitude;
        /** Longitude when the user joined. */
        private Double joinLongitude;
        /** Optional note associated with the entry. */
        private String note;
        /** Final result for the user (e.g., "WIN", "LOSS"). */
        private String finalResult;

        /** Default constructor for Firestore. */
        public WaitingListEntry() {}

        /**
         * Full constructor for WaitingListEntry.
         */
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

        /** @return Device/user identifier. */
        public String getDeviceId() { return deviceId; }
        /** @param deviceId Device/user identifier to set. */
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

        /** @return Timestamp when joined. */
        public Timestamp getJoinedAt() { return joinedAt; }
        /** @param joinedAt Timestamp when joined to set. */
        public void setJoinedAt(Timestamp joinedAt) { this.joinedAt = joinedAt; }

        /** @return Timestamp of last update. */
        public Timestamp getUpdatedAt() { return updatedAt; }
        /** @param updatedAt Timestamp of last update to set. */
        public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

        /** @return Current participation status. */
        public String getParticipationStatus() { return participationStatus; }
        /** @param participationStatus Current participation status to set. */
        public void setParticipationStatus(String participationStatus) { this.participationStatus = participationStatus; }

        /** @return Timestamp when invitation was sent. */
        public Timestamp getInviteSentAt() { return inviteSentAt; }
        /** @param inviteSentAt Timestamp when invitation was sent to set. */
        public void setInviteSentAt(Timestamp inviteSentAt) { this.inviteSentAt = inviteSentAt; }

        /** @return Timestamp when responded. */
        public Timestamp getRespondedAt() { return respondedAt; }
        /** @param respondedAt Timestamp when responded to set. */
        public void setRespondedAt(Timestamp respondedAt) { this.respondedAt = respondedAt; }

        /** @return Timestamp when selected. */
        public Timestamp getSelectedAt() { return selectedAt; }
        /** @param selectedAt Timestamp when selected to set. */
        public void setSelectedAt(Timestamp selectedAt) { this.selectedAt = selectedAt; }

        /** @return Timestamp when confirmed. */
        public Timestamp getConfirmedAt() { return confirmedAt; }
        /** @param confirmedAt Timestamp when confirmed to set. */
        public void setConfirmedAt(Timestamp confirmedAt) { this.confirmedAt = confirmedAt; }

        /** @return Timestamp when declined. */
        public Timestamp getDeclinedAt() { return declinedAt; }
        /** @param declinedAt Timestamp when declined to set. */
        public void setDeclinedAt(Timestamp declinedAt) { this.declinedAt = declinedAt; }

        /** @return Timestamp when cancelled. */
        public Timestamp getCancelledAt() { return cancelledAt; }
        /** @param cancelledAt Timestamp when cancelled to set. */
        public void setCancelledAt(Timestamp cancelledAt) { this.cancelledAt = cancelledAt; }

        /** @return Timestamp when enrolled. */
        public Timestamp getEnrolledAt() { return enrolledAt; }
        /** @param enrolledAt Timestamp when enrolled to set. */
        public void setEnrolledAt(Timestamp enrolledAt) { this.enrolledAt = enrolledAt; }

        /** @return Timestamp when left. */
        public Timestamp getLeftAt() { return leftAt; }
        /** @param leftAt Timestamp when left to set. */
        public void setLeftAt(Timestamp leftAt) { this.leftAt = leftAt; }

        /** @return True if user is a replacement. */
        public boolean isIsReplacement() { return isReplacement; }
        /** @param replacement True if user is a replacement to set. */
        public void setIsReplacement(boolean replacement) { isReplacement = replacement; }

        /** @return Round number of selection. */
        public int getSelectionRound() { return selectionRound; }
        /** @param selectionRound Round number of selection to set. */
        public void setSelectionRound(int selectionRound) { this.selectionRound = selectionRound; }

        /** @return True if joined with geolocation. */
        public boolean isJoinedWithGeo() { return joinedWithGeo; }
        /** @param joinedWithGeo True if joined with geolocation to set. */
        public void setJoinedWithGeo(boolean joinedWithGeo) { this.joinedWithGeo = joinedWithGeo; }

        /** @return Latitude when joined. */
        public Double getJoinLatitude() { return joinLatitude; }
        /** @param joinLatitude Latitude when joined to set. */
        public void setJoinLatitude(Double joinLatitude) { this.joinLatitude = joinLatitude; }

        /** @return Longitude when joined. */
        public Double getJoinLongitude() { return joinLongitude; }
        /** @param joinLongitude Longitude when joined to set. */
        public void setJoinLongitude(Double joinLongitude) { this.joinLongitude = joinLongitude; }

        /** @return Optional note. */
        public String getNote() { return note; }
        /** @param note Optional note to set. */
        public void setNote(String note) { this.note = note; }

        /** @return Final result (WIN/LOSS). */
        public String getFinalResult() { return finalResult; }
        /** @param finalResult Final result to set. */
        public void setFinalResult(String finalResult) { this.finalResult = finalResult; }

        @Override
        public String toString() {
            return "WaitingListEntry{" + "deviceId='" + deviceId + '\'' + ", participationStatus='" + participationStatus + '\'' + '}';
        }
    }

    /**
     * Represents a co-organizer for an event.
     */
    public static class CoOrganizer {
        /** Unique device/user identifier of the co-organizer. */
        private String deviceId;
        /** Identifier of the main organizer. */
        private String organizerId;
        /** Status of the co-organizer invitation (e.g., pending, accepted). */
        private String status;
        /** Timestamp when the invitation was sent. */
        private Timestamp invitedAt;
        /** Timestamp when the invitation was responded to. */
        private Timestamp respondedAt;

        /** Default constructor for Firestore. */
        public CoOrganizer() {}

        /**
         * Full constructor for CoOrganizer.
         * @param deviceId    Device/user ID.
         * @param organizerId Main organizer ID.
         * @param status      Invitation status.
         * @param invitedAt   Invitation timestamp.
         * @param respondedAt Response timestamp.
         */
        public CoOrganizer(String deviceId, String organizerId, String status, Timestamp invitedAt, Timestamp respondedAt) {
            this.deviceId = deviceId;
            this.organizerId = organizerId;
            this.status = status;
            this.invitedAt = invitedAt;
            this.respondedAt = respondedAt;
        }

        /** @return Device/user identifier. */
        public String getDeviceId() { return deviceId; }
        /** @param deviceId Device/user identifier to set. */
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

        /** @return Main organizer identifier. */
        public String getOrganizerId() { return organizerId; }
        /** @param organizerId Main organizer identifier to set. */
        public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

        /** @return Invitation status. */
        public String getStatus() { return status; }
        /** @param status Invitation status to set. */
        public void setStatus(String status) { this.status = status; }

        /** @return Timestamp when invited. */
        public Timestamp getInvitedAt() { return invitedAt; }
        /** @param invitedAt Timestamp when invited to set. */
        public void setInvitedAt(Timestamp invitedAt) { this.invitedAt = invitedAt; }

        /** @return Timestamp when responded. */
        public Timestamp getRespondedAt() { return respondedAt; }
        /** @param respondedAt Timestamp when responded to set. */
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
        /** Unique identifier for the ticket. */
        private String ticketId;
        /** Unique device/user identifier of the ticket holder. */
        private String deviceId;
        /** Identifier of the associated event. */
        private String eventId;
        /** Snapshot of the entrant's name. */
        private String entrantNameSnapshot;
        /** Snapshot of the entrant's email. */
        private String entrantEmailSnapshot;
        /** Identifier of the event organizer. */
        private String organizerId;
        /** Snapshot of the event title. */
        private String eventTitleSnapshot;
        /** Snapshot of the event location. */
        private String eventLocationSnapshot;
        /** Snapshot of the event start time. */
        private Timestamp eventStartAtSnapshot;
        /** Snapshot of the event end time. */
        private Timestamp eventEndAtSnapshot;
        /** Snapshot of the entry price. */
        private double priceSnapshot;
        /** Timestamp when the ticket was issued. */
        private Timestamp issuedAt;
        /** Current status of the ticket. */
        private String ticketStatus;
        /** Base64 string of the ticket PDF. */
        private String pdfBase64;
        /** Unique alphanumeric ticket code. */
        private String ticketCode;
        /** Value encoded in the ticket's QR code. */
        private String qrCodeValue;

        /** Default constructor for Firestore. */
        public Ticket() {}

        /**
         * Full constructor for Ticket.
         */
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

        /** @return Unique ticket identifier. */
        public String getTicketId() { return ticketId; }
        /** @param ticketId Unique ticket identifier to set. */
        public void setTicketId(String ticketId) { this.ticketId = ticketId; }

        /** @return Device/user identifier. */
        public String getDeviceId() { return deviceId; }
        /** @param deviceId Device/user identifier to set. */
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

        /** @return Event identifier. */
        public String getEventId() { return eventId; }
        /** @param eventId Event identifier to set. */
        public void setEventId(String eventId) { this.eventId = eventId; }

        /** @return Entrant's name snapshot. */
        public String getEntrantNameSnapshot() { return entrantNameSnapshot; }
        /** @param entrantNameSnapshot Entrant's name snapshot to set. */
        public void setEntrantNameSnapshot(String entrantNameSnapshot) { this.entrantNameSnapshot = entrantNameSnapshot; }

        /** @return Entrant's email snapshot. */
        public String getEntrantEmailSnapshot() { return entrantEmailSnapshot; }
        /** @param entrantEmailSnapshot Entrant's email snapshot to set. */
        public void setEntrantEmailSnapshot(String entrantEmailSnapshot) { this.entrantEmailSnapshot = entrantEmailSnapshot; }

        /** @return Organizer identifier. */
        public String getOrganizerId() { return organizerId; }
        /** @param organizerId Organizer identifier to set. */
        public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

        /** @return Event title snapshot. */
        public String getEventTitleSnapshot() { return eventTitleSnapshot; }
        /** @param eventTitleSnapshot Event title snapshot to set. */
        public void setEventTitleSnapshot(String eventTitleSnapshot) { this.eventTitleSnapshot = eventTitleSnapshot; }

        /** @return Event location snapshot. */
        public String getEventLocationSnapshot() { return eventLocationSnapshot; }
        /** @param eventLocationSnapshot Event location snapshot to set. */
        public void setEventLocationSnapshot(String eventLocationSnapshot) { this.eventLocationSnapshot = eventLocationSnapshot; }

        /** @return Event start time snapshot. */
        public Timestamp getEventStartAtSnapshot() { return eventStartAtSnapshot; }
        /** @param eventStartAtSnapshot Event start time snapshot to set. */
        public void setEventStartAtSnapshot(Timestamp eventStartAtSnapshot) { this.eventStartAtSnapshot = eventStartAtSnapshot; }

        /** @return Event end time snapshot. */
        public Timestamp getEventEndAtSnapshot() { return eventEndAtSnapshot; }
        /** @param eventEndAtSnapshot Event end time snapshot to set. */
        public void setEventEndAtSnapshot(Timestamp eventEndAtSnapshot) { this.eventEndAtSnapshot = eventEndAtSnapshot; }

        /** @return Entry price snapshot. */
        public double getPriceSnapshot() { return priceSnapshot; }
        /** @param priceSnapshot Entry price snapshot to set. */
        public void setPriceSnapshot(double priceSnapshot) { this.priceSnapshot = priceSnapshot; }

        /** @return Timestamp when issued. */
        public Timestamp getIssuedAt() { return issuedAt; }
        /** @param issuedAt Timestamp when issued to set. */
        public void setIssuedAt(Timestamp issuedAt) { this.issuedAt = issuedAt; }

        /** @return Current ticket status. */
        public String getTicketStatus() { return ticketStatus; }
        /** @param ticketStatus Current ticket status to set. */
        public void setTicketStatus(String ticketStatus) { this.ticketStatus = ticketStatus; }

        /** @return Base64 PDF content. */
        public String getPdfBase64() { return pdfBase64; }
        /** @param pdfBase64 Base64 PDF content to set. */
        public void setPdfBase64(String pdfBase64) { this.pdfBase64 = pdfBase64; }

        /** @return Alphanumeric ticket code. */
        public String getTicketCode() { return ticketCode; }
        /** @param ticketCode Alphanumeric ticket code to set. */
        public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }

        /** @return QR code value. */
        public String getQrCodeValue() { return qrCodeValue; }
        /** @param qrCodeValue QR code value to set. */
        public void setQrCodeValue(String qrCodeValue) { this.qrCodeValue = qrCodeValue; }

        @Override
        public String toString() {
            return "Ticket{" + "ticketId='" + ticketId + '\'' + ", ticketStatus='" + ticketStatus + '\'' + '}';
        }
    }
}
