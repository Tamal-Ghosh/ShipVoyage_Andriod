package com.example.shipvoyage.dao;
import com.example.shipvoyage.model.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import java.util.Map;
public class UserDAO {
    public DatabaseReference usersRef;
    public UserDAO() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
    }
    public Task<Void> addUser(User user) {
        String id = user.getId();
        return usersRef.child(id).setValue(user);
    }
    public Task<DataSnapshot> getUser(String id) {
        return usersRef.child(id).get();
    }
    public Task<DataSnapshot> getAllUsers() {
        return usersRef.get();
    }
    public Task<Void> updateUser(String id, Map<String, Object> updates) {
        return usersRef.child(id).updateChildren(updates);
    }
    public Task<Void> deleteUser(String id) {
        return usersRef.child(id).removeValue();
    }
}