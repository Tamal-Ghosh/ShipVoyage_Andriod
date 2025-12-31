package com.example.shipvoyage.ui.passenger;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.shipvoyage.R;
import com.example.shipvoyage.dao.BookingDAO;
import com.example.shipvoyage.dao.RoomDAO;
import com.example.shipvoyage.dao.TourInstanceDAO;
import com.example.shipvoyage.model.Booking;
import com.example.shipvoyage.model.Room;
import com.example.shipvoyage.model.TourInstance;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BookingActivity extends AppCompatActivity {
    
    private String tourInstanceId;
    private TourInstance tourInstance;
    private RoomDAO roomDAO;
    private BookingDAO bookingDAO;
    private TourInstanceDAO tourInstanceDAO;
    
    private GridLayout roomGrid;
    private TextView totalLabel;
    private RadioGroup paymentMethodGroup;
    private RadioButton visaRadio, bkashRadio;
    private Button confirmBookingButton, cancelButton;
    
    private List<Room> availableRooms = new ArrayList<>();
    private Set<String> bookedRoomIds = new HashSet<>();
    private Set<Room> selectedRooms = new HashSet<>();
    private double totalAmount = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        
        // Get tour instance ID from intent
        tourInstanceId = getIntent().getStringExtra("tourInstanceId");
        if (tourInstanceId == null) {
            Toast.makeText(this, "Invalid tour instance", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize DAOs
        roomDAO = new RoomDAO();
        bookingDAO = new BookingDAO();
        tourInstanceDAO = new TourInstanceDAO();
        
        initializeViews();
        loadData();
    }
    
    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        
        roomGrid = findViewById(R.id.roomGrid);
        totalLabel = findViewById(R.id.totalLabel);
        paymentMethodGroup = findViewById(R.id.paymentMethodGroup);
        visaRadio = findViewById(R.id.visaRadio);
        bkashRadio = findViewById(R.id.bkashRadio);
        confirmBookingButton = findViewById(R.id.confirmBookingButton);
        cancelButton = findViewById(R.id.cancelButton);
        
        cancelButton.setOnClickListener(v -> finish());
        confirmBookingButton.setOnClickListener(v -> onConfirmBooking());
    }
    
    private void loadData() {
        // Load tour instance
        tourInstanceDAO.getTourInstance(tourInstanceId).addOnSuccessListener(dataSnapshot -> {
            tourInstance = dataSnapshot.getValue(TourInstance.class);
            if (tourInstance != null) {
                loadRooms();
            } else {
                Toast.makeText(this, "Tour instance not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load tour instance", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
    
    private void loadRooms() {
        // Load rooms for this ship
        roomDAO.getRoomsByShip(tourInstance.getShipId()).addOnSuccessListener(dataSnapshot -> {
            availableRooms.clear();
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                Room room = snapshot.getValue(Room.class);
                if (room != null && room.isAvailability()) {
                    availableRooms.add(room);
                }
            }
            
            // Load booked rooms for this instance
            loadBookedRooms();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load rooms", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void loadBookedRooms() {
        bookingDAO.bookingsRef.orderByChild("tourInstanceId")
                .equalTo(tourInstanceId)
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    bookedRoomIds.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Booking booking = snapshot.getValue(Booking.class);
                        if (booking != null && booking.getSelectedRooms() != null) {
                            // Get room IDs from selectedRooms list
                            for (String roomNum : booking.getSelectedRooms()) {
                                // Find room ID by room number
                                for (Room room : availableRooms) {
                                    if (room.getRoomNumber().equals(roomNum.trim())) {
                                        bookedRoomIds.add(room.getId());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    
                    // Generate room grid
                    generateRoomGrid();
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load bookings", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void generateRoomGrid() {
        roomGrid.removeAllViews();
        
        for (Room room : availableRooms) {
            Button roomButton = new Button(this);
            roomButton.setText(room.getRoomNumber());
            roomButton.setTextSize(14);
            
            // Set initial color based on room type
            int color = getColorForRoom(room, false);
            roomButton.setBackgroundColor(color);
            
            // Disable if already booked
            boolean isBooked = bookedRoomIds.contains(room.getId());
            if (isBooked) {
                roomButton.setEnabled(false);
                roomButton.setBackgroundColor(Color.parseColor("#F97316")); // Orange for booked
                roomButton.setTextColor(Color.WHITE);
            } else {
                roomButton.setTextColor(Color.WHITE);
                roomButton.setOnClickListener(v -> handleRoomSelection(room, roomButton));
            }
            
            // Set layout params
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(8, 8, 8, 8);
            roomButton.setLayoutParams(params);
            
            roomGrid.addView(roomButton);
        }
    }
    
    private int getColorForRoom(Room room, boolean isSelected) {
        if (isSelected) {
            return Color.parseColor("#3B82F6"); // Blue for selected
        }
        
        // Color code by room type
        String type = room.getType().toLowerCase();
        if (type.contains("single")) {
            return Color.parseColor("#10B981"); // Green for single
        } else if (type.contains("double")) {
            return Color.parseColor("#8B5CF6"); // Purple for double
        } else if (type.contains("suite")) {
            return Color.parseColor("#F59E0B"); // Amber for suite
        } else {
            return Color.parseColor("#6366F1"); // Indigo for others
        }
    }
    
    private void handleRoomSelection(Room room, Button button) {
        if (selectedRooms.contains(room)) {
            // Deselect
            selectedRooms.remove(room);
            totalAmount -= room.getPrice();
            button.setBackgroundColor(getColorForRoom(room, false));
        } else {
            // Select
            selectedRooms.add(room);
            totalAmount += room.getPrice();
            button.setBackgroundColor(getColorForRoom(room, true));
        }
        
        updateTotal();
    }
    
    private void updateTotal() {
        totalLabel.setText(String.format("৳%.0f", totalAmount));
    }
    
    private void onConfirmBooking() {
        // Validate selection
        if (selectedRooms.isEmpty()) {
            Toast.makeText(this, "Please select at least one room", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Get payment method
        int selectedPaymentId = paymentMethodGroup.getCheckedRadioButtonId();
        String paymentMethod;
        if (selectedPaymentId == R.id.visaRadio) {
            paymentMethod = "Visa";
        } else if (selectedPaymentId == R.id.bkashRadio) {
            paymentMethod = "bKash";
        } else {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Use a dummy user ID for now (no authentication)
        String userId = "guest_user_" + System.currentTimeMillis();
        
        // Create booking
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setTourInstanceId(tourInstanceId);
        booking.setPrice(totalAmount);
        booking.setStatus("PENDING");
        booking.setBookingDate(System.currentTimeMillis() + "");
        booking.setPaymentMethod(paymentMethod);
        
        // Build selected rooms list
        List<String> roomNumbers = new ArrayList<>();
        for (Room room : selectedRooms) {
            roomNumbers.add(room.getRoomNumber());
        }
        booking.setSelectedRooms(roomNumbers);
        
        // Navigate to payment activity
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("booking", booking);
        intent.putExtra("paymentMethod", paymentMethod);
        intent.putExtra("totalAmount", totalAmount);
        startActivity(intent);
        finish();
    }
}
