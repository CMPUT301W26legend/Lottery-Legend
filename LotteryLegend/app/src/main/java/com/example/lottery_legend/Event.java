package com.example.lottery_legend;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents an Event in the Lottery Legend system.
 */
public class Event {
    private String eventId;
    private String organizerId;
    private String title;
    private String description;
    private boolean geoEnabled;
    private String location;
    private String eventStartDate;
    private String eventEndDate;
    private String registrationStartDate;
    private String registrationEndDate;
    private String drawDate;
    private int capacity;
    private Integer maxWaitingList;
    private String posterImage;
    private String qrCodeImage;
    private String status;
    private List<String> waitingList;

    /**
     * Default constructor required for Firebase Firestore deserialization.
     * Initializes an empty waiting list.
     */
    public Event() {
        this.waitingList = new ArrayList<>();
    }

    /**
     * Constructs a new Event with the specified details.
     * Automatically generates a unique event ID and sets the initial status to "open".
     *
     * @param organizerId           ID of the organizer.
     * @param title                 Name of the event.
     * @param description           Details about the event.
     * @param geoEnabled            Whether geolocation is required.
     * @param location              Where the event happens.
     * @param eventStartDate        When the event starts.
     * @param eventEndDate          When the event ends.
     * @param registrationStartDate When registration begins.
     * @param registrationEndDate   When registration ends.
     * @param drawDate              When the lottery is performed.
     * @param capacity              Number of available spots.
     * @param maxWaitingList        Maximum size of the waiting list (can be null).
     */
    public Event(String organizerId, String title, String description, boolean geoEnabled, String location,
                 String eventStartDate, String eventEndDate, String registrationStartDate, String registrationEndDate,
                 String drawDate, int capacity, Integer maxWaitingList) {
        this.eventId = UUID.randomUUID().toString();
        this.organizerId = organizerId;
        this.title = title;
        this.description = description;
        this.geoEnabled = geoEnabled;
        this.location = location;
        this.eventStartDate = eventStartDate;
        this.eventEndDate = eventEndDate;
        this.registrationStartDate = registrationStartDate;
        this.registrationEndDate = registrationEndDate;
        this.drawDate = drawDate;
        this.capacity = capacity;
        this.maxWaitingList = maxWaitingList;
        this.status = "open";
        this.waitingList = new ArrayList<>();
    }

    /** @return The unique event ID. */
    public String getEventId() { return eventId; }
    /** @param eventId The event ID to set. */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /** @return The organizer's unique ID. */
    public String getOrganizerId() { return organizerId; }
    /** @param organizerId The organizer ID to set. */
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    /** @return The event title. */
    public String getTitle() { return title; }
    /** @param title The title to set. */
    public void setTitle(String title) { this.title = title; }

    /** @return The event description. */
    public String getDescription() { return description; }
    /** @param description The description to set. */
    public void setDescription(String description) { this.description = description; }

    /** @return True if geolocation is enabled, false otherwise. */
    public boolean isGeoEnabled() { return geoEnabled; }
    /** @param geoEnabled True to enable geolocation, false to disable. */
    public void setGeoEnabled(boolean geoEnabled) { this.geoEnabled = geoEnabled; }

    /** @return The event location. */
    public String getLocation() { return location; }
    /** @param location The location to set. */
    public void setLocation(String location) { this.location = location; }

    /** @return The event start date. */
    public String getEventStartDate() { return eventStartDate; }
    /** @param eventStartDate The start date to set. */
    public void setEventStartDate(String eventStartDate) { this.eventStartDate = eventStartDate; }

    /** @return The event end date. */
    public String getEventEndDate() { return eventEndDate; }
    /** @param eventEndDate The end date to set. */
    public void setEventEndDate(String eventEndDate) { this.eventEndDate = eventEndDate; }

    /** @return The date registration starts. */
    public String getRegistrationStartDate() { return registrationStartDate; }
    /** @param registrationStartDate The registration start date to set. */
    public void setRegistrationStartDate(String registrationStartDate) { this.registrationStartDate = registrationStartDate; }

    /** @return The date registration ends. */
    public String getRegistrationEndDate() { return registrationEndDate; }
    /** @param registrationEndDate The registration end date to set. */
    public void setRegistrationEndDate(String registrationEndDate) { this.registrationEndDate = registrationEndDate; }

    /** @return The date of the lottery draw. */
    public String getDrawDate() { return drawDate; }
    /** @param drawDate The draw date to set. */
    public void setDrawDate(String drawDate) { this.drawDate = drawDate; }

    /** @return The maximum capacity of winners. */
    public int getCapacity() { return capacity; }
    /** @param capacity The capacity to set. */
    public void setCapacity(int capacity) { this.capacity = capacity; }

    /** @return The maximum size of the waiting list (null if no limit). */
    public Integer getMaxWaitingList() { return maxWaitingList; }
    /** @param maxWaitingList The maximum waiting list size to set. */
    public void setMaxWaitingList(Integer maxWaitingList) { this.maxWaitingList = maxWaitingList; }

    /** @return Base64 string of the poster image. */
    public String getPosterImage() { return posterImage; }
    /** @param posterImage Base64 string of the poster image to set. */
    public void setPosterImage(String posterImage) { this.posterImage = posterImage; }

    /** @return Base64 string of the QR code image. */
    public String getQrCodeImage() { return qrCodeImage; }
    /** @param qrCodeImage Base64 string of the QR code image to set. */
    public void setQrCodeImage(String qrCodeImage) { this.qrCodeImage = qrCodeImage; }

    /** @return The current status of the event. */
    public String getStatus() { return status; }
    /** @param status The status to set. */
    public void setStatus(String status) { this.status = status; }

    /** @return The list of user IDs on the waiting list. */
    public List<String> getWaitingList() { return waitingList; }
    /** @param waitingList The waiting list to set. */
    public void setWaitingList(List<String> waitingList) { this.waitingList = waitingList; }
}
