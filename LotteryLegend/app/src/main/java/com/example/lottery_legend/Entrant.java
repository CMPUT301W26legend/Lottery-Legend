package com.example.lottery_legend;

public class Entrant {
    public String name;
    public String email;
    public String phone;
    public boolean notification;

    // Required for Firestore
    public Entrant() {}

    public Entrant(String name, String email, String phone, boolean notification) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.notification = notification;
    }
}