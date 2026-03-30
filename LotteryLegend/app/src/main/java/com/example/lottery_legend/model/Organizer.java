package com.example.lottery_legend.model;

import com.google.firebase.Timestamp;
import java.util.List;import java.util.Objects;

/**
 * Model class representing an Organizer in the system.
 * Maps to the "/organizers/{deviceId}" collection in Firestore.
 */
public class Organizer {

    private String deviceId;
    private String name;
    private String email;
    private String phone;
    private Timestamp joinDate;
    private Timestamp updatedAt;
    private boolean isAdmin;
    private List<CreatedEvent> createdEvents;

    /**
     * Required no-argument constructor for Firestore serialization.
     */
    public Organizer() {
    }

    /**
     * Full constructor for Organizer.
     *
     * @param deviceId      The unique device identifier.
     * @param name          The organizer's name.
     * @param email         The organizer's email address.
     * @param phone         The organizer's phone number.
     * @param joinDate      The timestamp when the organizer joined.
     * @param updatedAt     The timestamp of the last profile update.
     * @param isAdmin       Whether the organizer has administrative privileges.
     * @param createdEvents List of events created by this organizer (subcollection mapping).
     */
    public Organizer(String deviceId, String name, String email, String phone,
                     Timestamp joinDate, Timestamp updatedAt, boolean isAdmin,
                     List<CreatedEvent> createdEvents) {
        this.deviceId = deviceId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.joinDate = joinDate;
        this.updatedAt = updatedAt;
        this.isAdmin = isAdmin;
        this.createdEvents = createdEvents;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Alias for getDeviceId() to maintain compatibility with Admin views.
     * @return The unique device/user identifier.
     */
    public String getUserId() {
        return deviceId;
    }

    /**
     * Alias for setDeviceId() to maintain compatibility with Admin views.
     * @param userId The unique identifier to set.
     */
    public void setUserId(String userId) {
        this.deviceId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Timestamp getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Timestamp joinDate) {
        this.joinDate = joinDate;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean admin) {
        isAdmin = admin;
    }

    public List<CreatedEvent> getCreatedEvents() {
        return createdEvents;
    }

    public void setCreatedEvents(List<CreatedEvent> createdEvents) {
        this.createdEvents = createdEvents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Organizer organizer = (Organizer) o;
        return Objects.equals(deviceId, organizer.deviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId);
    }

    @Override
    public String toString() {
        return "Organizer{" +
                "deviceId='" + deviceId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", isAdmin=" + isAdmin +
                '}';
    }

    /**
     * Inner class representing a summary of an event created by the organizer.
     * Maps to the "/organizers/{deviceId}/createdEvents/{eventId}" subcollection.
     */
    public static class CreatedEvent {
        private String eventId;
        private String title;
        private String status;
        private Timestamp createdAt;

        /**
         * Required no-argument constructor for Firestore serialization.
         */
        public CreatedEvent() {
        }

        public CreatedEvent(String eventId, String title, String status, Timestamp createdAt) {
            this.eventId = eventId;
            this.title = title;
            this.status = status;
            this.createdAt = createdAt;
        }

        public String getEventId() {
            return eventId;
        }

        public void setEventId(String eventId) {
            this.eventId = eventId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Timestamp getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Timestamp createdAt) {
            this.createdAt = createdAt;
        }

        @Override
        public String toString() {
            return "CreatedEvent{" +
                    "eventId='" + eventId + '\'' +
                    ", title='" + title + '\'' +
                    ", status='" + status + '\'' +
                    '}';
        }
    }
}