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

    // Required for Firestore
    public Entrant() {}

    public Entrant(String name, String email, String phone, boolean notification, String userId, Timestamp joinDate) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.notification = notification;
        this.userId = userId;
        this.joinDate = joinDate;
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

    public boolean isNotification() {
        return notification;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Timestamp joinDate) {
        this.joinDate = joinDate;
    }
}