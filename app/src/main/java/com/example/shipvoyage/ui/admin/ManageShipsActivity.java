package com.example.shipvoyage.ui.admin;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.shipvoyage.R;
import com.example.shipvoyage.adapter.ShipAdapter;
import com.example.shipvoyage.dao.ShipDAO;
import com.example.shipvoyage.model.Ship;
import com.example.shipvoyage.util.AdminNavHelper;
import com.google.firebase.database.DataSnapshot;
import java.util.ArrayList;
import java.util.List;
public class ManageShipsActivity extends AppCompatActivity {
    private RecyclerView shipsRecyclerView;
    private EditText nameField;
    private EditText capacityField;
    private EditText searchField;
    private Button saveBtn;
    private Button cancelBtn;
    private Button searchBtn;
    private ShipDAO shipDAO;
    private List<Ship> shipsList;
    private ShipAdapter shipAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_ships);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.manageShipsRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
        shipDAO = new ShipDAO();
        shipsList = new ArrayList<>();
        initViews();
        setupListeners();
        loadShips();
    }
    private void initViews() {
        shipsRecyclerView = findViewById(R.id.shipsRecyclerView);
        nameField = findViewById(R.id.nameField);
        capacityField = findViewById(R.id.capacityField);
        searchField = findViewById(R.id.searchField);
        saveBtn = findViewById(R.id.saveBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        searchBtn = findViewById(R.id.searchBtn);
        ImageButton menuBtn = findViewById(R.id.menuBtn);
        menuBtn.setOnClickListener(v -> AdminNavHelper.setupNavigationMenu(this, v));
        shipsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        shipAdapter = new ShipAdapter(new ShipAdapter.OnShipClickListener() {
            @Override
            public void onEditClick(Ship ship) {
                nameField.setText(ship.getName());
                capacityField.setText(String.valueOf(ship.getCapacity()));
            }
            @Override
            public void onDeleteClick(Ship ship) {
                shipDAO.deleteShip(ship.getId()).addOnSuccessListener(unused -> {
                    Toast.makeText(ManageShipsActivity.this, "Ship deleted", Toast.LENGTH_SHORT).show();
                    loadShips();
                }).addOnFailureListener(e -> {
                    Toast.makeText(ManageShipsActivity.this, "Failed to delete ship", Toast.LENGTH_SHORT).show();
                });
            }
        });
        shipsRecyclerView.setAdapter(shipAdapter);
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) {
            AdminNavHelper.setupBottomNavigation(this, bottomNav);
        }
    }
    private void setupListeners() {
        saveBtn.setOnClickListener(v -> saveShip());
        cancelBtn.setOnClickListener(v -> clearForm());
        searchBtn.setOnClickListener(v -> performSearch());
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch();
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
    private void loadShips() {
        shipDAO.getAllShips().addOnSuccessListener(dataSnapshot -> {
            shipsList.clear();
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                Ship ship = snapshot.getValue(Ship.class);
                if (ship != null) {
                    shipsList.add(ship);
                }
            }
            updateRecyclerView();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load ships", Toast.LENGTH_SHORT).show();
        });
    }
    private void saveShip() {
        String name = nameField.getText().toString().trim();
        String capacityStr = capacityField.getText().toString().trim();
        if (name.isEmpty() || capacityStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        int capacity;
        try {
            capacity = Integer.parseInt(capacityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid capacity number", Toast.LENGTH_SHORT).show();
            return;
        }
        if (capacity <= 0) {
            Toast.makeText(this, "Capacity must be greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }
        for (Ship existingShip : shipsList) {
            if (existingShip.getName().equalsIgnoreCase(name)) {
                Toast.makeText(this, "Ship name already exists", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        String shipId = shipDAO.shipsRef.push().getKey();
        if (shipId != null) {
            Ship ship = new Ship(shipId, name, capacity, "");
            shipDAO.addShip(ship).addOnSuccessListener(unused -> {
                Toast.makeText(this, "Ship saved successfully", Toast.LENGTH_SHORT).show();
                clearForm();
                loadShips();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to save ship", Toast.LENGTH_SHORT).show();
            });
        }
    }
    private void clearForm() {
        nameField.setText("");
        capacityField.setText("");
    }
    private void performSearch() {
        String query = searchField.getText().toString().trim().toLowerCase();
        if (query.isEmpty()) {
            shipAdapter.submitList(new ArrayList<>(shipsList));
            return;
        }
        List<Ship> filteredShips = new ArrayList<>();
        for (Ship ship : shipsList) {
            if (ship.getName().toLowerCase().contains(query)) {
                filteredShips.add(ship);
            }
        }
        shipAdapter.submitList(new ArrayList<>(filteredShips));
    }
    private void updateRecyclerView() {
        shipAdapter.submitList(new ArrayList<>(shipsList));
    }
}