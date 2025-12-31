package com.example.shipvoyage.model;

import java.util.List;

public class Booking {
    public String id;
    public String tourInstanceId;
    public String userId;
    public List<String> selectedRooms;
    public double price;
    public String status; // PENDING, CONFIRMED, CANCELLED
    public String paymentMethod; // BKASH, VISA, etc.
    
    // Transient fields for display
    public String customerName;
    public String customerEmail;
    public String customerPhone;

    public Booking() {}

    public Booking(String id, String tourInstanceId, String userId, List<String> selectedRooms, double price, String status, String paymentMethod) {
        this.id = id;
        this.tourInstanceId = tourInstanceId;
        this.userId = userId;
        this.selectedRooms = selectedRooms;
        this.price = price;
        this.status = status;
        this.paymentMethod = paymentMethod;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTourInstanceId() {
        return tourInstanceId;
    }

    public void setTourInstanceId(String tourInstanceId) {
        this.tourInstanceId = tourInstanceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getSelectedRooms() {
        return selectedRooms;
    }

    public void setSelectedRooms(List<String> selectedRooms) {
        this.selectedRooms = selectedRooms;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getSelectedRoomsString() {
        if (selectedRooms == null || selectedRooms.isEmpty()) {
            return "N/A";
        }
        return String.join(", ", selectedRooms);
    }
}
