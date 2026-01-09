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
import com.example.shipvoyage.adapter.TourAdapter;
import com.example.shipvoyage.dao.TourDAO;
import com.example.shipvoyage.model.Tour;
import com.example.shipvoyage.util.AdminNavHelper;
import com.example.shipvoyage.util.ThreadPool;
import java.util.ArrayList;
import java.util.List;
public class ManageToursActivity extends AppCompatActivity {
    private RecyclerView toursRecyclerView;
    private EditText nameField;
    private EditText fromField;
    private EditText toField;
    private EditText descriptionField;
    private EditText searchField;
    private Button saveBtn;
    private Button cancelBtn;
    private Button searchBtn;
    private TourDAO tourDAO;
    private List<Tour> toursList;
    private TourAdapter tourAdapter;
    private String editingTourId = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_tours);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.manageToursRoot), (v, insets) -> {
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
        tourDAO = new TourDAO();
        toursList = new ArrayList<>();
        initViews();
        setupListeners();
        loadTours();
    }
    private void initViews() {
        toursRecyclerView = findViewById(R.id.toursRecyclerView);
        nameField = findViewById(R.id.nameField);
        fromField = findViewById(R.id.fromField);
        toField = findViewById(R.id.toField);
        descriptionField = findViewById(R.id.descriptionField);
        searchField = findViewById(R.id.searchField);
        saveBtn = findViewById(R.id.saveBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        searchBtn = findViewById(R.id.searchBtn);
        ImageButton menuBtn = findViewById(R.id.menuBtn);
        menuBtn.setOnClickListener(v -> AdminNavHelper.setupNavigationMenu(this, v));
        toursRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tourAdapter = new TourAdapter(new TourAdapter.OnTourClickListener() {
            @Override
            public void onEditClick(Tour tour) {
                editingTourId = tour.getId();
                nameField.setText(tour.getName());
                fromField.setText(tour.getFrom());
                toField.setText(tour.getTo());
                descriptionField.setText(tour.getDescription());
            }
            @Override
            public void onDeleteClick(Tour tour) {
                tourDAO.deleteTour(tour.getId()).addOnSuccessListener(unused -> {
                    Toast.makeText(ManageToursActivity.this, "Tour deleted", Toast.LENGTH_SHORT).show();
                    loadTours();
                }).addOnFailureListener(e -> {
                    Toast.makeText(ManageToursActivity.this, "Failed to delete tour", Toast.LENGTH_SHORT).show();
                });
            }
        });
        toursRecyclerView.setAdapter(tourAdapter);
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) {
            AdminNavHelper.setupBottomNavigation(this, bottomNav);
        }
    }
    private void setupListeners() {
        saveBtn.setOnClickListener(v -> saveTour());
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
    private void loadTours() {
        tourDAO.getAllTours().addOnSuccessListener(dataSnapshot -> {
            ThreadPool.getExecutor().execute(() -> {
                List<Tour> newTours = new ArrayList<>();
                for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Tour tour = snapshot.getValue(Tour.class);
                    if (tour != null) {
                        newTours.add(tour);
                    }
                }
                runOnUiThread(() -> {
                    toursList.clear();
                    toursList.addAll(newTours);
                    updateRecyclerView();
                });
            });
        }).addOnFailureListener(e -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "Failed to load tours", Toast.LENGTH_SHORT).show();
            });
        });
    }
    private void saveTour() {
        String name = nameField.getText().toString().trim();
        String from = fromField.getText().toString().trim();
        String to = toField.getText().toString().trim();
        String description = descriptionField.getText().toString().trim();
        if (name.isEmpty() || from.isEmpty() || to.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        for (Tour existingTour : toursList) {
            if (existingTour.getName().equalsIgnoreCase(name) && 
                (editingTourId == null || !existingTour.getId().equals(editingTourId))) {
                Toast.makeText(this, "Tour name already exists", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        String tourId = editingTourId != null ? editingTourId : tourDAO.toursRef.push().getKey();
        if (tourId != null) {
            Tour tour = new Tour(tourId, name, from, to, description);
            tourDAO.addTour(tour).addOnSuccessListener(unused -> {
                Toast.makeText(this, editingTourId != null ? "Tour updated successfully" : "Tour saved successfully", Toast.LENGTH_SHORT).show();
                clearForm();
                loadTours();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to save tour", Toast.LENGTH_SHORT).show();
            });
        }
    }
    private void clearForm() {
        editingTourId = null;
        nameField.setText("");
        fromField.setText("");
        toField.setText("");
        descriptionField.setText("");
    }
    private void performSearch() {
        String query = searchField.getText().toString().trim().toLowerCase();
        if (query.isEmpty()) {
            tourAdapter.submitList(new ArrayList<>(toursList));
            return;
        }
        List<Tour> filteredTours = new ArrayList<>();
        for (Tour tour : toursList) {
            if (tour.getName().toLowerCase().contains(query) ||
                tour.getFrom().toLowerCase().contains(query) ||
                tour.getTo().toLowerCase().contains(query)) {
                filteredTours.add(tour);
            }
        }
        tourAdapter.submitList(new ArrayList<>(filteredTours));
    }
    private void updateRecyclerView() {
        tourAdapter.submitList(new ArrayList<>(toursList));
    }
}