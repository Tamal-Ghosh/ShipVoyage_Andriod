package com.example.shipvoyage.dao;
import com.example.shipvoyage.model.Ship;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import java.util.HashMap;
import java.util.Map;
public class ShipDAO {
    public DatabaseReference shipsRef;
    public ShipDAO() {
        this.shipsRef = FirebaseDatabase.getInstance().getReference("ships");
    }
    public Task<Void> addShip(Ship ship) {
        return shipsRef.child(ship.getId()).setValue(ship);
    }
    public Task<DataSnapshot> getShip(String shipId) {
        return shipsRef.child(shipId).get();
    }
    public Task<DataSnapshot> getAllShips() {
        return shipsRef.get();
    }
    public Task<Void> updateShip(String shipId, Map<String, Object> updates) {
        return shipsRef.child(shipId).updateChildren(updates);
    }
    public Task<Void> deleteShip(String shipId) {
        return shipsRef.child(shipId).removeValue();
    }
}