package com.example.shipvoyage.dao;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.example.shipvoyage.model.Tour;
import java.util.Map;
public class TourDAO {
    public DatabaseReference toursRef;
    public TourDAO() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        toursRef = database.getReference("tours");
    }
    public Task<Void> addTour(Tour tour) {
        return toursRef.child(tour.getId()).setValue(tour);
    }
    public Task<DataSnapshot> getTour(String tourId) {
        return toursRef.child(tourId).get();
    }
    public Task<DataSnapshot> getAllTours() {
        return toursRef.get();
    }
    public Task<Void> updateTour(String tourId, Map<String, Object> updates) {
        return toursRef.child(tourId).updateChildren(updates);
    }
    public Task<Void> deleteTour(String tourId) {
        return toursRef.child(tourId).removeValue();
    }
}