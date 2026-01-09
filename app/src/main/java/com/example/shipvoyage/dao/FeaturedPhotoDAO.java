package com.example.shipvoyage.dao;
import com.example.shipvoyage.model.FeaturedPhoto;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
public class FeaturedPhotoDAO {
    public DatabaseReference photosRef = FirebaseDatabase.getInstance().getReference("featured_photos");
    public com.google.android.gms.tasks.Task<Void> addPhoto(FeaturedPhoto photo) {
        String key = photosRef.push().getKey();
        photo.setId(key);
        return photosRef.child(key).setValue(photo);
    }
    public com.google.android.gms.tasks.Task<Void> updatePhoto(String photoId, FeaturedPhoto photo) {
        return photosRef.child(photoId).setValue(photo);
    }
    public com.google.android.gms.tasks.Task<Void> deletePhoto(String photoId) {
        return photosRef.child(photoId).removeValue();
    }
    public com.google.android.gms.tasks.Task<com.google.firebase.database.DataSnapshot> getPhoto(String photoId) {
        return photosRef.child(photoId).get();
    }
    public com.google.android.gms.tasks.Task<com.google.firebase.database.DataSnapshot> getAllPhotos() {
        return photosRef.get();
    }
}