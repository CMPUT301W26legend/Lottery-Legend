package com.example.lottery_legend.model;

import com.google.firebase.Timestamp;
import java.util.List;import java.util.Objects;

/**
 * Model class representing an Organizer in the system.
 * Maps to the "/organizers/{deviceId}" collection in Firestore.
 * Organizers can create and manage events.
 */
public class Organizer {

    /** Unique identifier for the device/user. */
    private String deviceId;
    /** Full name of the organizer. */
    private String name;
    /** Email address of the organizer. */
    private String email;
    /** Phone number of the organizer. */
    private String phone;
    /** Timestamp when the organizer first joined the system. */
    private Timestamp joinDate;
    /** Timestamp when the organizer's profile was last updated. */
    private Timestamp updatedAt;
    /** Flag indicating if the organizer has administrative privileges. */
    private boolean isAdmin;
    /** List of summary objects for events created by this organizer. */
    private List<CreatedEvent> createdEvents;
    /** Base64 encoded profile image or URL. */
    private String profileImage;

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
     * @param createdEvents List of events created by this organizer.
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

    /** @return The unique device identifier. */
    public String getDeviceId() {
        return deviceId;
    }

    /** @param deviceId The unique device identifier to set. */
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

    /** @return The organizer's name. */
    public String getName() {
        return name;
    }

    /** @param name The name to set. */
    public void setName(String name) {
        this.name = name;
    }

    /** @return The organizer's email. */
    public String getEmail() {
        return email;
    }

    /** @param email The email to set. */
    public void setEmail(String email) {
        this.email = email;
    }

    /** @return The organizer's phone number. */
    public String getPhone() {
        return phone;
    }

    /** @param phone The phone number to set. */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /** @return The date the organizer joined. */
    public Timestamp getJoinDate() {
        return joinDate;
    }

    /** @param joinDate The join date to set. */
    public void setJoinDate(Timestamp joinDate) {
        this.joinDate = joinDate;
    }

    /** @return The last update timestamp. */
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    /** @param updatedAt The last update timestamp to set. */
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** @return True if the user is an admin. */
    public boolean getIsAdmin() {
        return isAdmin;
    }

    /** @param admin True to set as admin. */
    public void setIsAdmin(boolean admin) {
        isAdmin = admin;
    }

    /** @return List of created events. */
    public List<CreatedEvent> getCreatedEvents() {
        return createdEvents;
    }

    /** @param createdEvents List of created events to set. */
    public void setCreatedEvents(List<CreatedEvent> createdEvents) {
        this.createdEvents = createdEvents;
    }

    /** @return Base64 encoded profile image. */
    public String getProfileImage() {
        return profileImage;
    }

    /** @param profileImage Base64 encoded profile image to set. */
    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
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
     * Maps to the "/organizers/{deviceId}/createdEvents/{eventId}" subcollection in Firestore.
     */
    public static class CreatedEvent {
        /** Unique identifier for the event. */
        private String eventId;
        /** Title of the event. */
        private String title;
        /** Current status of the event. */
        private String status;
        /** Timestamp when the event was created. */
        private Timestamp createdAt;

        /**
         * Required no-argument constructor for Firestore serialization.
         */
        public CreatedEvent() {
        }

        /**
         * Full constructor for CreatedEvent.
         * @param eventId   Event ID.
         * @param title     Event title.
         * @param status    Event status.
         * @param createdAt Creation timestamp.
         */
        public CreatedEvent(String eventId, String title, String status, Timestamp createdAt) {
            this.eventId = eventId;
            this.title = title;
            this.status = status;
            this.createdAt = createdAt;
        }

        /** @return Event identifier. */
        public String getEventId() {
            return eventId;
        }

        /** @param eventId Event identifier to set. */
        public void setEventId(String eventId) {
            this.eventId = eventId;
        }

        /** @return Event title. */
        public String getTitle() {
            return title;
        }

        /** @param title Event title to set. */
        public void setTitle(String title) {
            this.title = title;
        }

        /** @return Event status. */
        public String getStatus() {
            return status;
        }

        /** @param status Event status to set. */
        public void setStatus(String status) {
            this.status = status;
        }

        /** @return Creation timestamp. */
        public Timestamp getCreatedAt() {
            return createdAt;
        }

        /** @param createdAt Creation timestamp to set. */
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
