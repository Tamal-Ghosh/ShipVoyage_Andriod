package com.example.shipvoyage.model;

public class TourInstance {
    public String id;
    public String tourId;
    public String shipId;
    public long startDate;
    public long endDate;

    public TourInstance() {}

    public TourInstance(String id, String tourId, String shipId, long startDate, long endDate) {
        this.id = id;
        this.tourId = tourId;
        this.shipId = shipId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTourId() {
        return tourId;
    }

    public void setTourId(String tourId) {
        this.tourId = tourId;
    }

    public String getShipId() {
        return shipId;
    }

    public void setShipId(String shipId) {
        this.shipId = shipId;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }
}
