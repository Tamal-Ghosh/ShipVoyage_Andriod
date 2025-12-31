package com.example.shipvoyage.model;

import java.util.HashMap;
import java.util.Map;

public class Room {
    public String id;
    public String shipId;
    public String roomNumber;
    public String type;
    public double price;
    public boolean availability;

    public Room() {}

    public Room(String id, String shipId, String roomNumber, String type, double price, boolean availability) {
        this.id = id;
        this.shipId = shipId;
        this.roomNumber = roomNumber;
        this.type = type;
        this.price = price;
        this.availability = availability;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShipId() {
        return shipId;
    }

    public void setShipId(String shipId) {
        this.shipId = shipId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isAvailability() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }
    
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("shipId", shipId);
        map.put("roomNumber", roomNumber);
        map.put("type", type);
        map.put("price", price);
        map.put("availability", availability);
        return map;
    }
}
