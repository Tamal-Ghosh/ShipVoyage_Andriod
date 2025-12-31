package com.example.shipvoyage.dao;

import com.example.shipvoyage.model.Room;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;

import java.util.Map;

public class RoomDAO {

    public DatabaseReference roomsRef;

    public RoomDAO() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        roomsRef = database.getReference("rooms");
    }

    public Task<Void> addRoom(Room room) {
        String id = roomsRef.push().getKey();
        if (id != null) {
            room.setId(id);
            return roomsRef.child(id).setValue(room);
        }
        return null;
    }

    public Task<DataSnapshot> getRoom(String id) {
        return roomsRef.child(id).get();
    }

    public Task<DataSnapshot> getAllRooms() {
        return roomsRef.get();
    }

    public Task<DataSnapshot> getRoomsByShip(String shipId) {
        return roomsRef.orderByChild("shipId").equalTo(shipId).get();
    }

    public Task<Void> updateRoom(String id, Map<String, Object> updates) {
        return roomsRef.child(id).updateChildren(updates);
    }

    public Task<Void> deleteRoom(String id) {
        return roomsRef.child(id).removeValue();
    }
}

