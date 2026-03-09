package com.example.lottery_legend;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Model class for an Event.
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
    private String status;
    private List<String> waitingList;

    public Event() {
        this.waitingList = new ArrayList<>();
    }

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

    // Getters and Setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isGeoEnabled() { return geoEnabled; }
    public void setGeoEnabled(boolean geoEnabled) { this.geoEnabled = geoEnabled; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getEventStartDate() { return eventStartDate; }
    public void setEventStartDate(String eventStartDate) { this.eventStartDate = eventStartDate; }

    public String getEventEndDate() { return eventEndDate; }
    public void setEventEndDate(String eventEndDate) { this.eventEndDate = eventEndDate; }

    public String getRegistrationStartDate() { return registrationStartDate; }
    public void setRegistrationStartDate(String registrationStartDate) { this.registrationStartDate = registrationStartDate; }

    public String getRegistrationEndDate() { return registrationEndDate; }
    public void setRegistrationEndDate(String registrationEndDate) { this.registrationEndDate = registrationEndDate; }

    public String getDrawDate() { return drawDate; }
    public void setDrawDate(String drawDate) { this.drawDate = drawDate; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public Integer getMaxWaitingList() { return maxWaitingList; }
    public void setMaxWaitingList(Integer maxWaitingList) { this.maxWaitingList = maxWaitingList; }

    public String getPosterImage() { return posterImage; }
    public void setPosterImage(String posterImage) { this.posterImage = posterImage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getWaitingList() { return waitingList; }
    public void setWaitingList(List<String> waitingList) { this.waitingList = waitingList; }
}
