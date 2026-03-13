package com.example.lottery_legend;

import com.google.firebase.Timestamp;

/**
 * Model class for an Organizer in the Lottery Legend system.
 */
public class Organizer {
    private String name;
    private String email;
    private String phone;
    private String userId;
    private Timestamp joinDate;
    public boolean isAdmin;

    /**
     * Default constructor required for Firebase Firestore deserialization.
     */
    public Organizer() {}

    /**
     * Constructs a new Organizer with the specified details, defaulting to non-admin status.
     *
     * @param name     The name of the organizer.
     * @param email    The email of the organizer.
     * @param phone    The phone number of the organizer.
     * @param userId   The unique ID of the organizer.
     * @param joinDate The timestamp when the organizer joined.
     */
    public Organizer(String name, String email, String phone, String userId, Timestamp joinDate) {
        this(name, email, phone, userId, joinDate, false);
    }

    /**
     * Constructs a new Organizer with all specified fields.
     *
     * @param name     The name of the organizer.
     * @param email    The email of the organizer.
     * @param phone    The phone number of the organizer.
     * @param userId   The unique ID of the organizer.
     * @param joinDate The timestamp when the organizer joined.
     * @param isAdmin  True if the organizer is an admin, false otherwise.
     */
    public Organizer(String name, String email, String phone, String userId, Timestamp joinDate, boolean isAdmin) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.userId = userId;
        this.joinDate = joinDate;
        this.isAdmin = isAdmin;
    }

    /**
     * Gets the name of the organizer.
     * @return The organizer's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the organizer.
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the email of the organizer.
     * @return The organizer's email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email of the organizer.
     * @param email The email to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the phone number of the organizer.
     * @return The organizer's phone number.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the phone number of the organizer.
     * @param phone The phone number to set.
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Gets the unique user ID of the organizer.
     * @return The organizer's user ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the unique user ID of the organizer.
     * @param userId The user ID to set.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the join date of the organizer.
     * @return The organizer's join date.
     */
    public Timestamp getJoinDate() {
        return joinDate;
    }

    /**
     * Sets the join date of the organizer.
     * @param joinDate The join date to set.
     */
    public void setJoinDate(Timestamp joinDate) {
        this.joinDate = joinDate;
    }

    /**
     * Gets whether the organizer has admin privileges.
     * @return True if the organizer is an admin, false otherwise.
     */
    public boolean getIsAdmin() {
        return isAdmin;
    }

    /**
     * Sets whether the organizer has admin privileges.
     * @param isAdmin True to set as admin, false otherwise.
     */
    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}
