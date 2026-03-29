package com.example.lottery_legend;

import com.google.firebase.Timestamp;
import java.util.Objects;

/**
 * Model class representing an Entrant in the Lottery Legend system.
 * Designed for seamless serialization and deserialization with Firebase Firestore.
 */
public class Entrant {

    /** Unique identifier for the device/user. */
    private String deviceId;

    /** Full name of the entrant. */
    private String name;

    /** Email address for communication. */
    private String email;

    /** Phone number of the entrant. */
    private String phone;

    /** Flag indicating if push notifications are enabled. */
    private boolean notificationsEnabled;

    /** Timestamp indicating when the entrant first joined. */
    private Timestamp joinDate;

    /** Timestamp indicating the last time the profile was updated. */
    private Timestamp updatedAt;

    /** Flag indicating if the user has administrative privileges. */
    private boolean isAdmin;

    /**
     * Default no-argument constructor required for Firebase Firestore.
     */
    public Entrant() {
    }

    /**
     * Full constructor to initialize all fields of the Entrant.
     *
     * @param deviceId             The unique device ID.
     * @param name                 The name of the entrant.
     * @param email                The email of the entrant.
     * @param phone                The phone number of the entrant.
     * @param notificationsEnabled Whether notifications are active.
     * @param joinDate             The date the entrant joined.
     * @param updatedAt           The date of the last update.
     * @param isAdmin              Administrative status.
     */
    public Entrant(String deviceId, String name, String email, String phone,
                   boolean notificationsEnabled, Timestamp joinDate,
                   Timestamp updatedAt, boolean isAdmin) {
        this.deviceId = deviceId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.notificationsEnabled = notificationsEnabled;
        this.joinDate = joinDate;
        this.updatedAt = updatedAt;
        this.isAdmin = isAdmin;
    }

    /** @return The unique device identifier. */
    public String getDeviceId() {
        return deviceId;
    }

    /** @param deviceId The unique device identifier to set. */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /** @return The entrant's name. */
    public String getName() {
        return name;
    }

    /** @param name The name to set. */
    public void setName(String name) {
        this.name = name;
    }

    /** @return The entrant's email. */
    public String getEmail() {
        return email;
    }

    /** @param email The email to set. */
    public void setEmail(String email) {
        this.email = email;
    }

    /** @return The entrant's phone number. */
    public String getPhone() {
        return phone;
    }

    /** @param phone The phone number to set. */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /** @return True if notifications are enabled. */
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    /** @param notificationsEnabled True to enable notifications. */
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    /** @return The date the entrant joined. */
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

    /** @param updatedAt The update timestamp to set. */
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** @return True if the user is an admin. */
    public boolean getIsAdmin() {
        return isAdmin;
    }

    /** @param isAdmin True to set user as admin. */
    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    /**
     * Compares this Entrant to another object based on deviceId.
     * @param o The object to compare.
     * @return True if the deviceIds match.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entrant entrant = (Entrant) o;
        return Objects.equals(deviceId, entrant.deviceId);
    }

    /**
     * Generates a hash code based on deviceId.
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(deviceId);
    }

    /**
     * Returns a string representation of the Entrant for debugging.
     * @return Formatted string containing entrant details.
     */
    @Override
    public String toString() {
        return "Entrant{" +
                "deviceId='" + deviceId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", notificationsEnabled=" + notificationsEnabled +
                ", joinDate=" + joinDate +
                ", updatedAt=" + updatedAt +
                ", isAdmin=" + isAdmin +
                '}';
    }
}