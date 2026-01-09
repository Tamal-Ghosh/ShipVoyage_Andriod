package com.example.shipvoyage.model;

public class User {
    public String id;
    public String username;
    public String name;
    public String email;
    public String phone;
    public String role;
    public long createdAt;
    public String profileImagePath;
    public String lastInstance;
    public String paymentStatus;

    public User() {}

    public User(String id, String username, String email, String phone, String role, long createdAt, String profileImagePath) {
        this.id = id;
        this.username = username;
        this.name = username;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.createdAt = createdAt;
        this.profileImagePath = profileImagePath;
    }

    public User(String id, String username, String email, String phone, String role) {
        this.id = id;
        this.username = username;
        this.name = username;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name != null ? name : username;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    public String getLastInstance() {
        return lastInstance;
    }

    public void setLastInstance(String lastInstance) {
        this.lastInstance = lastInstance;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
