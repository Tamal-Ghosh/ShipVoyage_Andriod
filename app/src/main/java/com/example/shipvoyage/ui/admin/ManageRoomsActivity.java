package com.example.shipvoyage.ui.admin;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shipvoyage.R;
import com.example.shipvoyage.adapter.RoomAdapter;
import com.example.shipvoyage.dao.RoomDAO;
import com.example.shipvoyage.dao.ShipDAO;
import com.example.shipvoyage.model.Room;
import com.example.shipvoyage.model.Ship;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ManageRoomsActivity extends AppCompatActivity implements RoomAdapter.OnRoomActionListener {
    
    private RecyclerView roomsRecyclerView;
    private RoomAdapter roomAdapter;
    private RoomDAO roomDAO;
    private ShipDAO shipDAO;
    
    private AutoCompleteTextView shipField, roomTypeField;
    private TextInputEditText roomNumberField, priceField;
    private Button saveButton, clearButton;
    
    private List<Room> roomsList = new ArrayList<>();
    private List<Ship> shipsList = new ArrayList<>();
    private String[] roomTypes = {"Single", "Double"};
    
    private String selectedShipId = null;
    private String editingRoomId = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_rooms);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.manageRoomsRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        roomDAO = new RoomDAO();
        shipDAO = new ShipDAO();
        
        initializeViews();
        setupRecyclerView();
        loadData();
    }
    
    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        
        roomsRecyclerView = findViewById(R.id.roomsRecyclerView);
        shipField = findViewById(R.id.shipField);
        roomNumberField = findViewById(R.id.roomNumberField);
        roomTypeField = findViewById(R.id.roomTypeField);
        priceField = findViewById(R.id.priceField);
        saveButton = findViewById(R.id.saveButton);
        clearButton = findViewById(R.id.clearButton);
        
        saveButton.setOnClickListener(v -> onSaveRoom());
        clearButton.setOnClickListener(v -> onClearRoom());
        
        // Setup room type dropdown
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roomTypes);
        roomTypeField.setAdapter(typeAdapter);
    }
    
    private void setupRecyclerView() {
        roomAdapter = new RoomAdapter(this);
        roomsRecyclerView.setAdapter(roomAdapter);
    }
    
    private void loadData() {
        loadShips();
        loadRooms();
    }
    
    private void loadShips() {
        shipDAO.getAllShips().addOnSuccessListener(dataSnapshot -> {
            shipsList.clear();
            
            List<String> shipNames = new ArrayList<>();
            
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                Ship ship = snapshot.getValue(Ship.class);
                if (ship != null) {
                    shipsList.add(ship);
                    shipNames.add(ship.getName());
                }
            }
            
            // Setup ship field autocomplete
            ArrayAdapter<String> shipAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, shipNames);
            shipField.setAdapter(shipAdapter);
            
            // Handle ship selection
            shipField.setOnItemClickListener((parent, view, position, id) -> {
                String selectedShipName = shipNames.get(position);
                selectedShipId = shipsList.get(position).getId();
                filterRoomsByShip();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load ships", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void loadRooms() {
        roomDAO.getAllRooms().addOnSuccessListener(dataSnapshot -> {
            roomsList.clear();
            
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                Room room = snapshot.getValue(Room.class);
                if (room != null) {
                    roomsList.add(room);
                }
            }
            
            // Filter by selected ship if one is selected
            filterRoomsByShip();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load rooms", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void filterRoomsByShip() {
        if (selectedShipId == null) {
            roomAdapter.submitList(new ArrayList<>()); // Show no rooms if no ship selected
        } else {
            List<Room> filteredRooms = new ArrayList<>();
            for (Room room : roomsList) {
                if (room.getShipId().equals(selectedShipId)) {
                    filteredRooms.add(room);
                }
            }
            roomAdapter.submitList(filteredRooms);
        }
    }
    
    private void onSaveRoom() {
        String roomNumber = roomNumberField.getText().toString().trim();
        String roomType = roomTypeField.getText().toString().trim();
        String priceStr = priceField.getText().toString().trim();
        
        if (selectedShipId == null) {
            Toast.makeText(this, "Please select a ship", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (roomNumber.isEmpty()) {
            Toast.makeText(this, "Please enter room number", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (roomType.isEmpty()) {
            Toast.makeText(this, "Please select room type", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (priceStr.isEmpty()) {
            Toast.makeText(this, "Please enter price", Toast.LENGTH_SHORT).show();
            return;
        }
        
        double price = Double.parseDouble(priceStr);
        
        Room room = new Room();
        room.setShipId(selectedShipId);
        room.setRoomNumber(roomNumber);
        room.setType(roomType);
        room.setPrice(price);
        room.setAvailability(true);
        
        if (editingRoomId != null) {
            // Update existing room
            room.setId(editingRoomId);
            roomDAO.updateRoom(editingRoomId, room.toMap()).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Room updated successfully", Toast.LENGTH_SHORT).show();
                onClearRoom();
                loadRooms();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to update room", Toast.LENGTH_SHORT).show();
            });
        } else {
            // Add new room
            roomDAO.addRoom(room).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Room added successfully", Toast.LENGTH_SHORT).show();
                onClearRoom();
                loadRooms();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to add room", Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    private void onClearRoom() {
        shipField.setText("");
        roomNumberField.setText("");
        roomTypeField.setText("");
        priceField.setText("");
        selectedShipId = null;
        editingRoomId = null;
        saveButton.setText("Save");
    }
    
    @Override
    public void onEdit(Room room) {
        editingRoomId = room.getId();
        
        // Find ship and set in field
        for (Ship ship : shipsList) {
            if (ship.getId().equals(room.getShipId())) {
                shipField.setText(ship.getName(), false);
                selectedShipId = ship.getId();
                break;
            }
        }
        
        roomNumberField.setText(room.getRoomNumber());
        roomTypeField.setText(room.getType(), false);
        priceField.setText(String.valueOf(room.getPrice()));
        saveButton.setText("Update");
    }
    
    @Override
    public void onDelete(Room room) {
        roomDAO.deleteRoom(room.getId()).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Room deleted successfully", Toast.LENGTH_SHORT).show();
            loadRooms();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to delete room", Toast.LENGTH_SHORT).show();
        });
    }
}
