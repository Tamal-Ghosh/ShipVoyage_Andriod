package com.example.shipvoyage.dao;

import com.example.shipvoyage.model.TourInstance;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;

import java.util.Map;

public class TourInstanceDAO {

    private DatabaseReference tourInstancesRef;

    public TourInstanceDAO() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        tourInstancesRef = database.getReference("tourInstances");
    }

    public Task<Void> addTourInstance(TourInstance tourInstance) {
        String id = tourInstancesRef.push().getKey();
        tourInstance.setId(id);
        return tourInstancesRef.child(id).setValue(tourInstance);
    }

    public Task<DataSnapshot> getTourInstance(String id) {
        return tourInstancesRef.child(id).get();
    }

    public Task<DataSnapshot> getAllTourInstances() {
        return tourInstancesRef.get();
    }

    public Task<Void> updateTourInstance(String id, Map<String, Object> updates) {
        return tourInstancesRef.child(id).updateChildren(updates);
    }

    public Task<Void> deleteTourInstance(String id) {
        return tourInstancesRef.child(id).removeValue();
    }
}

