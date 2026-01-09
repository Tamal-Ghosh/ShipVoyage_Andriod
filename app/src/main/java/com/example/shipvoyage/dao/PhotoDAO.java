package com.example.shipvoyage.dao;
import com.example.shipvoyage.model.FeaturedPhoto;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import java.util.Map;
public class PhotoDAO {
    public DatabaseReference photosRef;
    public PhotoDAO() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        photosRef = database.getReference("featured_photos");
    }
    public Task<Void> addPhoto(FeaturedPhoto photo) {
        String id = photosRef.push().getKey();
        if (id != null) {
            photo.setId(id);
            return photosRef.child(id).setValue(photo);
        }
        return null;
    }
    public Task<DataSnapshot> getPhoto(String id) {
        return photosRef.child(id).get();
    }
    public Task<DataSnapshot> getAllPhotos() {
        return photosRef.get();
    }
    public Task<Void> updatePhoto(String id, Map<String, Object> updates) {
        return photosRef.child(id).updateChildren(updates);
    }
    public Task<Void> deletePhoto(String id) {
        return photosRef.child(id).removeValue();
    }
}