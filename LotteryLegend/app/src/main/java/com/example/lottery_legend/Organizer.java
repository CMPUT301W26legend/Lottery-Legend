package com.example.lottery_legend;

import com.google.firebase.Timestamp;

/**
 * Model class for an Organizer.
 */
public class Organizer {
    private String name;
    private String email;
    private String phone;
    private String userId;
    private Timestamp joinDate;
    public boolean isAdmin;

    public Organizer() {}

    public Organizer(String name, String email, String phone, String userId, Timestamp joinDate) {
        this(name, email, phone, userId, joinDate, false);
    }

    public Organizer(String name, String email, String phone, String userId, Timestamp joinDate, boolean isAdmin) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.userId = userId;
        this.joinDate = joinDate;
        this.isAdmin = isAdmin;
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

    public boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}
