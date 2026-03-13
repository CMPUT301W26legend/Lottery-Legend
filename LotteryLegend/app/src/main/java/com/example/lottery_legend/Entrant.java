package com.example.lottery_legend;

import com.google.firebase.Timestamp;

/**
 * Entrant class representing a user profile in the system.
 * Fields are public for easy access.
 */
public class Entrant {
    public String name;
    public String email;
    public String phone;
    public boolean notification;
    public String userId;
    public Timestamp joinDate;
    public boolean isAdmin;

    /**
     * Required no-argument constructor for Firebase Firestore deserialization.
     */
    public Entrant() {}

    /**
     * Constructs a new Entrant with a default non-admin status.
     *
     * @param name         The name of the user.
     * @param email        The email of the user.
     * @param phone        The phone number of the user.
     * @param notification Whether notifications are enabled.
     * @param userId       Unique ID for the user.
     * @param joinDate     Timestamp of when the profile was created.
     */
    public Entrant(String name, String email, String phone, boolean notification, String userId, Timestamp joinDate) {
        this(name, email, phone, notification, userId, joinDate, false);
    }

    /**
     * Constructs a new Entrant with all specified fields.
     *
     * @param name         The name of the user.
     * @param email        The email of the user.
     * @param phone        The phone number of the user.
     * @param notification Whether notifications are enabled.
     * @param userId       Unique ID for the user.
     * @param joinDate     Timestamp of when the profile was created.
     * @param isAdmin      True if the user is an administrator, false otherwise.
     */
    public Entrant(String name, String email, String phone, boolean notification, String userId, Timestamp joinDate, boolean isAdmin) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.notification = notification;
        this.userId = userId;
        this.joinDate = joinDate;
        this.isAdmin = isAdmin;
    }

    /** @return The name of the entrant. */
    public String getName() {
        return name;
    }

    /** @param name The name to set. */
    public void setName(String name) {
        this.name = name;
    }

    /** @return The email of the entrant. */
    public String getEmail() {
        return email;
    }

    /** @param email The email to set. */
    public void setEmail(String email) {
        this.email = email;
    }

    /** @return The phone number of the entrant. */
    public String getPhone() {
        return phone;
    }

    /** @param phone The phone number to set. */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /** @return True if notifications are enabled, false otherwise. */
    public boolean isNotification() {
        return notification;
    }

    /** @param notification True to enable notifications, false to disable. */
    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    /** @return The unique user ID. */
    public String getUserId() {
        return userId;
    }

    /** @param userId The user ID to set. */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /** @return The profile creation timestamp. */
    public Timestamp getJoinDate() {
        return joinDate;
    }

    /** @param joinDate The join date to set. */
    public void setJoinDate(Timestamp joinDate) {
        this.joinDate = joinDate;
    }

    /** @return True if the user is an admin, false otherwise. */
    public boolean getIsAdmin() {
        return isAdmin;
    }

    /** @param isAdmin True to grant admin status, false to revoke it. */
    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}
