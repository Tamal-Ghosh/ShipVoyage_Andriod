package com.example.shipvoyage.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shipvoyage.R;
import com.example.shipvoyage.adapter.RoomAdapter;
import com.example.shipvoyage.dao.RoomDAO;
import com.example.shipvoyage.dao.ShipDAO;
import com.example.shipvoyage.model.Room;
import com.example.shipvoyage.model.Ship;
import com.example.shipvoyage.util.ThreadPool;

import java.util.ArrayList;
import java.util.List;

public class ManageRoomsFragment extends Fragment {
    private RecyclerView roomsRecyclerView;
    private EditText roomNumberField, typeField, priceField, searchField;
    private Spinner shipSpinner;
    private Button saveBtn, cancelBtn, searchBtn;
    private RoomDAO roomDAO;
    private ShipDAO shipDAO;
    private List<Room> roomsList = new ArrayList<>();
    private List<Ship> shipsList = new ArrayList<>();
    private RoomAdapter roomAdapter;
    private String editingRoomId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_rooms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        roomDAO = new RoomDAO();
        shipDAO = new ShipDAO();
        initViews(view);
        setupListeners();
        loadShips();
        loadRooms();
    }

    private void initViews(View view) {
        roomsRecyclerView = view.findViewById(R.id.roomsRecyclerView);
        roomNumberField = view.findViewById(R.id.roomNumberField);
        typeField = view.findViewById(R.id.typeField);
        priceField = view.findViewById(R.id.priceField);
        searchField = view.findViewById(R.id.searchField);
        shipSpinner = view.findViewById(R.id.shipSpinner);
        saveBtn = view.findViewById(R.id.saveBtn);
        cancelBtn = view.findViewById(R.id.cancelBtn);
        searchBtn = view.findViewById(R.id.searchBtn);

        roomsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        roomAdapter = new RoomAdapter(new RoomAdapter.OnRoomActionListener() {
            @Override
            public void onEdit(Room room) {
                editingRoomId = room.getId();
                roomNumberField.setText(room.getRoomNumber());
                typeField.setText(room.getType());
                priceField.setText(String.valueOf(room.getPrice()));
                
                // Set ship spinner selection
                for (int i = 0; i < shipsList.size(); i++) {
                    if (shipsList.get(i).getId().equals(room.getShipId())) {
                        shipSpinner.setSelection(i);
                        break;
                    }
                }
            }

            @Override
            public void onDelete(Room room) {
                roomDAO.deleteRoom(room.getId()).addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "Room deleted", Toast.LENGTH_SHORT).show();
                    loadRooms();
                }).addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to delete room", Toast.LENGTH_SHORT).show();
                });
            }
        });
        roomsRecyclerView.setAdapter(roomAdapter);
    }

    private void setupListeners() {
        saveBtn.setOnClickListener(v -> saveRoom());
        cancelBtn.setOnClickListener(v -> clearForm());
        searchBtn.setOnClickListener(v -> performSearch());
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadShips() {
        shipDAO.getAllShips().addOnSuccessListener(dataSnapshot -> {
            ThreadPool.getExecutor().execute(() -> {
                List<Ship> newShips = new ArrayList<>();
                for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Ship ship = snapshot.getValue(Ship.class);
                    if (ship != null) {
                        newShips.add(ship);
                    }
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        shipsList.clear();
                        shipsList.addAll(newShips);
                        updateShipSpinner();
                    });
                }
            });
        });
    }

    private void updateShipSpinner() {
        android.widget.ArrayAdapter<Ship> adapter = new android.widget.ArrayAdapter<Ship>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                shipsList
        ) {

            public String toString(Ship ship) {
                return ship.getName();
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shipSpinner.setAdapter(adapter);
    }

    private void loadRooms() {
        roomDAO.getAllRooms().addOnSuccessListener(dataSnapshot -> {
            ThreadPool.getExecutor().execute(() -> {
                List<Room> newRooms = new ArrayList<>();
                for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Room room = snapshot.getValue(Room.class);
                    if (room != null) {
                        newRooms.add(room);
                    }
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        roomsList.clear();
                        roomsList.addAll(newRooms);
                        updateRecyclerView();
                    });
                }
            });
        }).addOnFailureListener(e -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Failed to load rooms", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void saveRoom() {
        String roomNumber = roomNumberField.getText().toString().trim();
        String type = typeField.getText().toString().trim();
        String priceStr = priceField.getText().toString().trim();

        if (roomNumber.isEmpty() || type.isEmpty() || priceStr.isEmpty() || shipSpinner.getSelectedItem() == null) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            Ship selectedShip = (Ship) shipSpinner.getSelectedItem();

            String roomId = editingRoomId != null ? editingRoomId : roomDAO.roomsRef.push().getKey();
            if (roomId != null) {
                Room room = new Room(roomId, selectedShip.getId(), roomNumber, type, price, true);
                roomDAO.addRoom(room).addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), editingRoomId != null ? "Room updated" : "Room saved", Toast.LENGTH_SHORT).show();
                    clearForm();
                    loadRooms();
                }).addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to save room", Toast.LENGTH_SHORT).show();
                });
            }
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid price", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        editingRoomId = null;
        roomNumberField.setText("");
        typeField.setText("");
        priceField.setText("");
    }

    private void performSearch() {
        String query = searchField.getText().toString().trim().toLowerCase();
        if (query.isEmpty()) {
            roomAdapter.submitList(new ArrayList<>(roomsList));
            return;
        }

        List<Room> filteredRooms = new ArrayList<>();
        for (Room room : roomsList) {
            if (room.getRoomNumber().toLowerCase().contains(query) ||
                room.getType().toLowerCase().contains(query)) {
                filteredRooms.add(room);
            }
        }
        roomAdapter.submitList(new ArrayList<>(filteredRooms));
    }

    private void updateRecyclerView() {
        roomAdapter.submitList(new ArrayList<>(roomsList));
    }
}
