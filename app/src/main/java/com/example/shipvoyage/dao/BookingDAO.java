package com.example.shipvoyage.dao;
import com.example.shipvoyage.model.Booking;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import java.util.Map;
public class BookingDAO {
    public DatabaseReference bookingsRef;
    public BookingDAO() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        bookingsRef = database.getReference("bookings");
    }
    public Task<Void> addBooking(Booking booking) {
        String id = bookingsRef.push().getKey();
        booking.setId(id);
        return bookingsRef.child(id).setValue(booking);
    }
    public Task<DataSnapshot> getBooking(String id) {
        return bookingsRef.child(id).get();
    }
    public Task<DataSnapshot> getAllBookings() {
        return bookingsRef.get();
    }
    public Task<DataSnapshot> getBookingsByUser(String userId) {
        return bookingsRef.orderByChild("userId").equalTo(userId).get();
    }
    public Task<Void> updateBooking(String id, Map<String, Object> updates) {
        return bookingsRef.child(id).updateChildren(updates);
    }
    public Task<Void> deleteBooking(String id) {
        return bookingsRef.child(id).removeValue();
    }
}