package com.example.shipvoyage.ui.passenger;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shipvoyage.R;
import com.example.shipvoyage.adapter.FeaturedPhotoAdapter;
import com.example.shipvoyage.adapter.TourSearchResultAdapter;
import com.example.shipvoyage.adapter.UpcomingTripAdapter;
import com.example.shipvoyage.dao.PhotoDAO;
import com.example.shipvoyage.dao.ShipDAO;
import com.example.shipvoyage.dao.TourDAO;
import com.example.shipvoyage.dao.TourInstanceDAO;
import com.example.shipvoyage.model.FeaturedPhoto;
import com.example.shipvoyage.model.Ship;
import com.example.shipvoyage.model.Tour;
import com.example.shipvoyage.model.TourInstance;
import com.example.shipvoyage.util.PassengerNavHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PassengerHomeActivity extends AppCompatActivity {

    private AutoCompleteTextView fromField, toField;
    private TextInputEditText dateField;
    private Button searchButton;
    private ImageButton menuBtn;
    private LinearLayout heroSection, resultsSection, mainContent;
    private RecyclerView searchResultsRecyclerView, featuredRecyclerView, upcomingRecyclerView;
    private TextView viewAllUpcoming;

    private TourSearchResultAdapter searchResultsAdapter;
    private FeaturedPhotoAdapter featuredAdapter;
    private UpcomingTripAdapter upcomingAdapter;

    private TourDAO tourDAO;
    private TourInstanceDAO tourInstanceDAO;
    private ShipDAO shipDAO;
    private PhotoDAO photoDAO;

    private List<TourInstance> allInstances = new ArrayList<>();
    private Map<String, Tour> toursMap = new HashMap<>();
    private Map<String, Ship> shipsMap = new HashMap<>();
    private Set<String> fromLocations = new HashSet<>();
    private Set<String> toLocations = new HashSet<>();
    private String selectedDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_passenger_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.passengerHomeRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tourDAO = new TourDAO();
        tourInstanceDAO = new TourInstanceDAO();
        shipDAO = new ShipDAO();
        photoDAO = new PhotoDAO();

        initViews();
        loadData();
    }

    private void setupAutoComplete() {
        ArrayAdapter<String> fromAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            new ArrayList<>(fromLocations)
        );
        
        ArrayAdapter<String> toAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            new ArrayList<>(toLocations)
        );
        
        fromField.setAdapter(fromAdapter);
        fromField.setThreshold(1);
        
        toField.setAdapter(toAdapter);
        toField.setThreshold(1);
    }

    private void initViews() {
        fromField = findViewById(R.id.fromField);
        toField = findViewById(R.id.toField);
        dateField = findViewById(R.id.dateField);
        searchButton = findViewById(R.id.searchButton);
        menuBtn = findViewById(R.id.menuBtn);
        heroSection = findViewById(R.id.heroSection);
        resultsSection = findViewById(R.id.resultsSection);
        mainContent = findViewById(R.id.mainContent);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        featuredRecyclerView = findViewById(R.id.featuredRecyclerView);
        upcomingRecyclerView = findViewById(R.id.upcomingRecyclerView);
        viewAllUpcoming = findViewById(R.id.viewAllUpcoming);

        dateField.setOnClickListener(v -> showDatePicker());
        searchButton.setOnClickListener(v -> searchTours());
        menuBtn.setOnClickListener(v -> PassengerNavHelper.setupNavigationMenu(this, v));
        viewAllUpcoming.setOnClickListener(v -> startActivity(new Intent(this, UpcomingToursActivity.class)));

        searchResultsAdapter = new TourSearchResultAdapter(instance -> {
            Intent intent = new Intent(this, BookingActivity.class);
            intent.putExtra("tourInstanceId", instance.getId());
            startActivity(intent);
        });
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchResultsRecyclerView.setAdapter(searchResultsAdapter);

        featuredAdapter = new FeaturedPhotoAdapter();
        featuredRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        featuredRecyclerView.setAdapter(featuredAdapter);

        upcomingAdapter = new UpcomingTripAdapter();
        upcomingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        upcomingRecyclerView.setAdapter(upcomingAdapter);
    }

    private void loadData() {
        loadTours();
        loadShips();
        loadInstances();
        loadFeaturedPhotos();
        loadUpcomingTrips();
    }

    private void loadTours() {
        tourDAO.getAllTours().addOnSuccessListener(dataSnapshot -> {
            toursMap.clear();
            fromLocations.clear();
            toLocations.clear();
            
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                Tour tour = snapshot.getValue(Tour.class);
                if (tour != null) {
                    toursMap.put(tour.getId(), tour);
                    fromLocations.add(tour.getFrom());
                    toLocations.add(tour.getTo());
                }
            }
            
            setupAutoComplete();
        });
    }

    private void loadShips() {
        shipDAO.getAllShips().addOnSuccessListener(dataSnapshot -> {
            shipsMap.clear();
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                Ship ship = snapshot.getValue(Ship.class);
                if (ship != null) {
                    shipsMap.put(ship.getId(), ship);
                }
            }
        });
    }

    private void loadInstances() {
        tourInstanceDAO.getAllTourInstances().addOnSuccessListener(dataSnapshot -> {
            allInstances.clear();
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                TourInstance instance = snapshot.getValue(TourInstance.class);
                if (instance != null) {
                    Tour tour = toursMap.get(instance.getTourId());
                    Ship ship = shipsMap.get(instance.getShipId());
                    if (tour != null) {
                        instance.setTourName(tour.getName());
                    }
                    if (ship != null) {
                        instance.setShipName(ship.getName());
                    }
                    allInstances.add(instance);
                }
            }
        });
    }

    private void loadFeaturedPhotos() {
        photoDAO.getAllPhotos().addOnSuccessListener(dataSnapshot -> {
            List<FeaturedPhoto> photos = new ArrayList<>();
            int count = 0;
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                if (count >= 6) break;
                FeaturedPhoto photo = snapshot.getValue(FeaturedPhoto.class);
                if (photo != null) {
                    photos.add(photo);
                    count++;
                }
            }
            featuredAdapter.submitList(photos);
        });
    }

    private void loadUpcomingTrips() {
        tourInstanceDAO.getAllTourInstances().addOnSuccessListener(dataSnapshot -> {
            List<TourInstance> upcoming = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String today = sdf.format(new Date());
            
            int count = 0;
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                if (count >= 2) break;
                TourInstance instance = snapshot.getValue(TourInstance.class);
                if (instance != null && instance.getStartDate().compareTo(today) >= 0) {
                    Tour tour = toursMap.get(instance.getTourId());
                    Ship ship = shipsMap.get(instance.getShipId());
                    if (tour != null) {
                        instance.setTourName(tour.getName());
                    }
                    if (ship != null) {
                        instance.setShipName(ship.getName());
                    }
                    upcoming.add(instance);
                    count++;
                }
            }
            upcomingAdapter.submitList(upcoming);
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    selectedDate = sdf.format(calendar.getTime());
                    dateField.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void searchTours() {
        String from = fromField.getText() != null ? fromField.getText().toString().trim() : "";
        String to = toField.getText() != null ? toField.getText().toString().trim() : "";

        if (from.isEmpty() || to.isEmpty() || selectedDate == null) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        List<TourInstance> results = new ArrayList<>();
        for (TourInstance instance : allInstances) {
            Tour tour = toursMap.get(instance.getTourId());
            if (tour != null && 
                tour.getFrom().equalsIgnoreCase(from) &&
                tour.getTo().equalsIgnoreCase(to) &&
                instance.getStartDate().compareTo(selectedDate) >= 0) {
                results.add(instance);
            }
        }

        if (results.isEmpty()) {
            Toast.makeText(this, "No tours found", Toast.LENGTH_SHORT).show();
        } else {
            searchResultsAdapter.submitList(results);
            resultsSection.setVisibility(View.VISIBLE);
            mainContent.setVisibility(View.GONE);
        }
    }

    private void showHome() {
        fromField.setText("");
        toField.setText("");
        dateField.setText("");
        selectedDate = null;
        resultsSection.setVisibility(View.GONE);
        mainContent.setVisibility(View.VISIBLE);
    }
}

