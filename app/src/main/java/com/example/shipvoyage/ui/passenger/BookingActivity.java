package com.example.shipvoyage.ui.passenger;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shipvoyage.R;
import com.example.shipvoyage.dao.BookingDAO;
import com.example.shipvoyage.dao.RoomDAO;
import com.example.shipvoyage.dao.TourInstanceDAO;
import com.example.shipvoyage.model.Booking;
import com.example.shipvoyage.model.Room;
import com.example.shipvoyage.model.TourInstance;
import com.example.shipvoyage.util.ThreadPool;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BookingActivity extends AppCompatActivity {

    private String tourInstanceId;
    private TourInstance tourInstance;
    private RoomDAO roomDAO;
    private BookingDAO bookingDAO;
    private TourInstanceDAO tourInstanceDAO;

    private LinearLayout roomGrid;
    private TextView totalLabel;
    private RadioGroup paymentMethodGroup;
    private RadioButton visaRadio, bkashRadio;
    private Button confirmBookingButton, cancelButton;

    private List<Room> availableRooms = new ArrayList<>();
    private Map<String, Room> roomById = new HashMap<>();
    private Set<String> bookedRoomIds = new HashSet<>();
    private Set<String> selectedRoomIds = new LinkedHashSet<>();
    private double totalAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_booking);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bookingRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tourInstanceId = getIntent().getStringExtra("tourInstanceId");
        if (tourInstanceId == null) {
            Toast.makeText(this, "Invalid tour instance", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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
        if (tourInstance == null || tourInstance.getShipId() == null) {
            Toast.makeText(this, "Tour instance or ship information missing", Toast.LENGTH_SHORT).show();
            return;
        }

        roomDAO.getRoomsByShip(tourInstance.getShipId()).addOnSuccessListener(dataSnapshot -> {
            ThreadPool.getExecutor().execute(() -> {
                List<Room> newRooms = new ArrayList<>();
                Map<String, Room> newRoomById = new HashMap<>();
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Room room = snapshot.getValue(Room.class);
                    if (room != null && tourInstance.getShipId().equals(room.getShipId())) {
                        // Treat missing availability as available
                        boolean available = room.isAvailability() || !snapshot.hasChild("availability");
                        room.setAvailability(available);
                        newRooms.add(room);
                        if (room.getId() != null) {
                            newRoomById.put(room.getId(), room);
                        }
                    }
                }
                
                runOnUiThread(() -> {
                    availableRooms.clear();
                    availableRooms.addAll(newRooms);
                    roomById.clear();
                    roomById.putAll(newRoomById);
                    loadBookedRooms();
                });
            });
        }).addOnFailureListener(e -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "Failed to load rooms: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void loadBookedRooms() {
        bookingDAO.getAllBookings().addOnSuccessListener(dataSnapshot -> {
            ThreadPool.getExecutor().execute(() -> {
                Map<String, String> roomNumberToId = new HashMap<>();
                for (Room room : availableRooms) {
                    roomNumberToId.put(room.getRoomNumber(), room.getId());
                }

                Set<String> newBooked = new HashSet<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Booking booking = snapshot.getValue(Booking.class);
                    if (booking != null
                            && tourInstanceId.equals(booking.getTourInstanceId())
                            && booking.getSelectedRooms() != null) {
                        for (String roomNum : booking.getSelectedRooms()) {
                            String roomId = roomNumberToId.get(roomNum.trim());
                            if (roomId != null) {
                                newBooked.add(roomId);
                            }
                        }
                    }
                }
                
                runOnUiThread(() -> {
                    bookedRoomIds.clear();
                    bookedRoomIds.addAll(newBooked);
                    generateRoomGrid();
                });
            });
        }).addOnFailureListener(e -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "Failed to load bookings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                generateRoomGrid();
            });
        });
    }

    private void generateRoomGrid() {
        roomGrid.removeAllViews();

        if (availableRooms.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No rooms available for this ship");
            emptyText.setTextSize(16);
            emptyText.setPadding(16, 32, 16, 32);
            emptyText.setTextColor(Color.GRAY);
            roomGrid.addView(emptyText);
            return;
        }

        roomGrid.removeAllViews();
        
        LinearLayout currentRow = null;
        int columnCount = 0;
        final int COLUMNS_PER_ROW = 4;

        for (Room room : availableRooms) {
            if (columnCount == 0) {
                currentRow = new LinearLayout(this);
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                currentRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
                roomGrid.addView(currentRow);
            }

            Button roomButton = new Button(this);
            roomButton.setText(room.getRoomNumber());
            roomButton.setTextSize(14);
            roomButton.setTextColor(Color.WHITE);

            boolean isBooked = bookedRoomIds.contains(room.getId());
            boolean isAvailable = room.isAvailability();

            if (isBooked) {
                roomButton.setEnabled(false);
                roomButton.setBackgroundColor(Color.parseColor("#F97316")); // Orange
            } else if (!isAvailable) {
                roomButton.setEnabled(false);
                roomButton.setBackgroundColor(Color.parseColor("#9CA3AF")); // Gray
            } else {
                roomButton.setBackgroundColor(getColorForRoom(room));
                roomButton.setOnClickListener(v -> handleRoomSelection(room, roomButton));
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, // width
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f); // weight for equal distribution
            params.setMargins(8, 8, 8, 8);
            roomButton.setLayoutParams(params);

            currentRow.addView(roomButton);
            columnCount++;
            
            if (columnCount == COLUMNS_PER_ROW) {
                columnCount = 0;
            }
        }
    }

    private int getColorForRoom(Room room) {
        String type = room.getType() != null ? room.getType().toLowerCase() : "";
        if (type.contains("single")) {
            return Color.parseColor("#10B981"); // Green
        } else if (type.contains("double")) {
            return Color.parseColor("#2563EB"); // Blue
        }
        return Color.parseColor("#6B7280"); // Gray
    }

    private void handleRoomSelection(Room room, Button button) {
        String roomId = room.getId();
        if (roomId == null) {
            Toast.makeText(this, "Room missing id", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedRoomIds.contains(roomId)) {
            selectedRoomIds.remove(roomId);
            totalAmount -= room.getPrice();
            button.setBackgroundColor(getColorForRoom(room));
        } else {
            selectedRoomIds.add(roomId);
            totalAmount += room.getPrice();
            button.setBackgroundColor(Color.parseColor("#1D4ED8")); // Dark blue
        }
        updateTotal();
    }

    private void updateTotal() {
        totalLabel.setText(String.format("৳%.0f", totalAmount));
    }

    private void onConfirmBooking() {
        if (selectedRoomIds.isEmpty()) {
            Toast.makeText(this, "Please select at least one room", Toast.LENGTH_SHORT).show();
            return;
        }

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

        String userId = "guest_user_" + System.currentTimeMillis();
        List<String> roomNumbers = new ArrayList<>();
        for (String roomId : selectedRoomIds) {
            Room r = roomById.get(roomId);
            if (r != null) {
                roomNumbers.add(r.getRoomNumber());
            }
        }

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setTourInstanceId(tourInstanceId);
        booking.setPrice(totalAmount);
        booking.setStatus("PENDING");
        booking.setBookingDate(System.currentTimeMillis() + "");
        booking.setPaymentMethod(paymentMethod);
        booking.setSelectedRooms(roomNumbers);

        bookingDAO.addBooking(booking).addOnSuccessListener(unused -> {
            Intent intent = new Intent(this, PaymentActivity.class);
            intent.putExtra("booking", booking);
            intent.putExtra("paymentMethod", paymentMethod);
            intent.putExtra("totalAmount", totalAmount);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to start booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
