package com.example.shipvoyage.ui.admin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
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
import com.example.shipvoyage.adapter.TourInstanceAdapter;
import com.example.shipvoyage.dao.ShipDAO;
import com.example.shipvoyage.dao.TourDAO;
import com.example.shipvoyage.dao.TourInstanceDAO;
import com.example.shipvoyage.model.Ship;
import com.example.shipvoyage.model.Tour;
import com.example.shipvoyage.model.TourInstance;
import com.example.shipvoyage.util.AdminNavHelper;
import com.example.shipvoyage.util.ThreadPool;
import com.google.firebase.database.DataSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ManageTourInstancesActivity extends AppCompatActivity {

    private RecyclerView instancesRecyclerView;
    private Spinner tourSpinner;
    private Spinner shipSpinner;
    private EditText startDateField;
    private EditText endDateField;
    private EditText searchField;
    private Button saveBtn;
    private Button cancelBtn;
    private Button searchBtn;

    private TourInstanceDAO tourInstanceDAO;
    private TourDAO tourDAO;
    private ShipDAO shipDAO;
    private List<TourInstance> instancesList;
    private List<Tour> toursList;
    private List<Ship> shipsList;
    private TourInstanceAdapter instanceAdapter;
    private String editingInstanceId = null;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_tour_instances);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.manageTourInstanceRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tourInstanceDAO = new TourInstanceDAO();
        tourDAO = new TourDAO();
        shipDAO = new ShipDAO();
        instancesList = new ArrayList<>();
        toursList = new ArrayList<>();
        shipsList = new ArrayList<>();

        initViews();
        setupListeners();
        loadTours();
        loadShips();
        loadTourInstances();
    }

    private void initViews() {
        instancesRecyclerView = findViewById(R.id.instancesRecyclerView);
        tourSpinner = findViewById(R.id.tourSpinner);
        shipSpinner = findViewById(R.id.shipSpinner);
        startDateField = findViewById(R.id.startDateField);
        endDateField = findViewById(R.id.endDateField);
        searchField = findViewById(R.id.searchField);
        saveBtn = findViewById(R.id.saveBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        searchBtn = findViewById(R.id.searchBtn);

        ImageButton menuBtn = findViewById(R.id.menuBtn);
        menuBtn.setOnClickListener(v -> AdminNavHelper.setupNavigationMenu(this, v));

        instancesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        instanceAdapter = new TourInstanceAdapter(new TourInstanceAdapter.OnTourInstanceClickListener() {
            @Override
            public void onEditClick(TourInstance tourInstance) {
                editingInstanceId = tourInstance.getId();
                
                // Set tour spinner
                for (int i = 0; i < toursList.size(); i++) {
                    if (toursList.get(i).getId().equals(tourInstance.getTourId())) {
                        tourSpinner.setSelection(i);
                        break;
                    }
                }
                
                // Set ship spinner
                for (int i = 0; i < shipsList.size(); i++) {
                    if (shipsList.get(i).getId().equals(tourInstance.getShipId())) {
                        shipSpinner.setSelection(i);
                        break;
                    }
                }
                
                startDateField.setText(tourInstance.getStartDate());
                endDateField.setText(tourInstance.getEndDate());
                saveBtn.setText("Update");
            }

            @Override
            public void onDeleteClick(TourInstance tourInstance) {
                tourInstanceDAO.deleteTourInstance(tourInstance.getId()).addOnSuccessListener(unused -> {
                    Toast.makeText(ManageTourInstancesActivity.this, "Tour instance deleted", Toast.LENGTH_SHORT).show();
                    loadTourInstances();
                }).addOnFailureListener(e -> {
                    Toast.makeText(ManageTourInstancesActivity.this, "Failed to delete tour instance", Toast.LENGTH_SHORT).show();
                });
            }
        });

        instancesRecyclerView.setAdapter(instanceAdapter);
    }

    private void setupListeners() {
        saveBtn.setOnClickListener(v -> saveTourInstance());
        cancelBtn.setOnClickListener(v -> clearForm());
        searchBtn.setOnClickListener(v -> performSearch());

        startDateField.setOnClickListener(v -> showDatePicker(startDateField));
        endDateField.setOnClickListener(v -> showDatePicker(endDateField));

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

    private void showDatePicker(EditText dateField) {
        Calendar calendar = Calendar.getInstance();
        
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            dateField.setText(dateFormat.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadTours() {
        tourDAO.getAllTours().addOnSuccessListener(dataSnapshot -> {
            ThreadPool.getExecutor().execute(() -> {
                List<Tour> newTours = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Tour tour = snapshot.getValue(Tour.class);
                    if (tour != null) {
                        newTours.add(tour);
                    }
                }
                runOnUiThread(() -> {
                    toursList.clear();
                    toursList.addAll(newTours);
                    updateTourSpinner();
                });
            });
        }).addOnFailureListener(e -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "Failed to load tours", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void loadShips() {
        shipDAO.getAllShips().addOnSuccessListener(dataSnapshot -> {
            ThreadPool.getExecutor().execute(() -> {
                List<Ship> newShips = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Ship ship = snapshot.getValue(Ship.class);
                    if (ship != null) {
                        newShips.add(ship);
                    }
                }
                runOnUiThread(() -> {
                    shipsList.clear();
                    shipsList.addAll(newShips);
                    updateShipSpinner();
                });
            });
        }).addOnFailureListener(e -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "Failed to load ships", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void loadTourInstances() {
        tourInstanceDAO.getAllTourInstances().addOnSuccessListener(dataSnapshot -> {
            ThreadPool.getExecutor().execute(() -> {
                List<TourInstance> newInstances = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TourInstance tourInstance = snapshot.getValue(TourInstance.class);
                    if (tourInstance != null) {
                        // Set tour and ship names
                        for (Tour tour : toursList) {
                            if (tour.getId().equals(tourInstance.getTourId())) {
                                tourInstance.setTourName(tour.getName());
                                break;
                            }
                        }
                        for (Ship ship : shipsList) {
                            if (ship.getId().equals(tourInstance.getShipId())) {
                                tourInstance.setShipName(ship.getName());
                                break;
                            }
                        }
                        newInstances.add(tourInstance);
                    }
                }
                runOnUiThread(() -> {
                    instancesList.clear();
                    instancesList.addAll(newInstances);
                    updateRecyclerView();
                });
            });
        }).addOnFailureListener(e -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "Failed to load tour instances", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void updateTourSpinner() {
        List<String> tourNames = new ArrayList<>();
        for (Tour tour : toursList) {
            tourNames.add(tour.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tourNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tourSpinner.setAdapter(adapter);
    }

    private void updateShipSpinner() {
        List<String> shipNames = new ArrayList<>();
        for (Ship ship : shipsList) {
            shipNames.add(ship.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, shipNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shipSpinner.setAdapter(adapter);
    }

    private void updateRecyclerView() {
        instanceAdapter.submitList(new ArrayList<>(instancesList));
    }

    private void saveTourInstance() {
        int tourPosition = tourSpinner.getSelectedItemPosition();
        int shipPosition = shipSpinner.getSelectedItemPosition();
        String startDate = startDateField.getText().toString().trim();
        String endDate = endDateField.getText().toString().trim();

        if (tourPosition < 0 || toursList.isEmpty()) {
            Toast.makeText(this, "Please select a tour", Toast.LENGTH_SHORT).show();
            return;
        }

        if (shipPosition < 0 || shipsList.isEmpty()) {
            Toast.makeText(this, "Please select a ship", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please enter both start and end dates", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate dates
        try {
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);
            
            if (start.after(end)) {
                Toast.makeText(this, "Start date must be before end date", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }

        Tour selectedTour = toursList.get(tourPosition);
        Ship selectedShip = shipsList.get(shipPosition);

        if (editingInstanceId != null) {
            // Update existing instance
            Map<String, Object> updates = new HashMap<>();
            updates.put("tourId", selectedTour.getId());
            updates.put("shipId", selectedShip.getId());
            updates.put("startDate", startDate);
            updates.put("endDate", endDate);

            tourInstanceDAO.updateTourInstance(editingInstanceId, updates).addOnSuccessListener(unused -> {
                Toast.makeText(this, "Tour instance updated", Toast.LENGTH_SHORT).show();
                clearForm();
                loadTourInstances();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to update tour instance", Toast.LENGTH_SHORT).show();
            });
        } else {
            // Create new instance
            TourInstance tourInstance = new TourInstance();
            tourInstance.setTourId(selectedTour.getId());
            tourInstance.setShipId(selectedShip.getId());
            tourInstance.setStartDate(startDate);
            tourInstance.setEndDate(endDate);

            tourInstanceDAO.addTourInstance(tourInstance).addOnSuccessListener(unused -> {
                Toast.makeText(this, "Tour instance saved", Toast.LENGTH_SHORT).show();
                clearForm();
                loadTourInstances();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to save tour instance", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void clearForm() {
        tourSpinner.setSelection(0);
        shipSpinner.setSelection(0);
        startDateField.setText("");
        endDateField.setText("");
        editingInstanceId = null;
        saveBtn.setText("Save");
    }

    private void performSearch() {
        String query = searchField.getText().toString().trim().toLowerCase();

        if (query.isEmpty()) {
            updateRecyclerView();
            return;
        }

        List<TourInstance> filteredList = new ArrayList<>();
        for (TourInstance instance : instancesList) {
            if (instance.getTourName().toLowerCase().contains(query) ||
                    instance.getShipName().toLowerCase().contains(query) ||
                    instance.getStartDate().contains(query) ||
                    instance.getEndDate().contains(query)) {
                filteredList.add(instance);
            }
        }

        instanceAdapter.submitList(filteredList);
    }
}

