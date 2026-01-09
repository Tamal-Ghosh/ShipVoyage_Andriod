package com.example.shipvoyage.ui.passenger;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Toast;

import com.example.shipvoyage.R;
import com.example.shipvoyage.adapter.TourSearchResultAdapter;
import com.example.shipvoyage.dao.TourDAO;
import com.example.shipvoyage.dao.ShipDAO;
import com.example.shipvoyage.dao.TourInstanceDAO;
import com.example.shipvoyage.model.Tour;
import com.example.shipvoyage.model.Ship;
import com.example.shipvoyage.model.TourInstance;
import com.example.shipvoyage.util.ThreadPool;
import com.google.firebase.database.DataSnapshot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UpcomingToursActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private TourSearchResultAdapter adapter;
    private TourInstanceDAO tourInstanceDAO;
    private TourDAO tourDAO;
    private ShipDAO shipDAO;
    
    private List<Tour> tours = new ArrayList<>();
    private List<Ship> ships = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upcoming_tours);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.upcommingTourRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
        
        recyclerView = findViewById(R.id.upcomingToursRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new TourSearchResultAdapter(instance -> {
            Intent intent = new Intent(this, BookingActivity.class);
            intent.putExtra("tourInstanceId", instance.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
        
        tourInstanceDAO = new TourInstanceDAO();
        tourDAO = new TourDAO();
        shipDAO = new ShipDAO();
        
        loadData();
    }
    
    private void loadData() {
        ThreadPool.getExecutor().execute(() -> {
            tourDAO.getAllTours().addOnSuccessListener(dataSnapshot -> {
                tours.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Tour tour = snapshot.getValue(Tour.class);
                    if (tour != null) {
                        tours.add(tour);
                    }
                }
                loadShips();
            }).addOnFailureListener(e -> {
                runOnUiThread(() -> Toast.makeText(this, "Failed to load tours", Toast.LENGTH_SHORT).show());
            });
        });
    }
    
    private void loadShips() {
        ThreadPool.getExecutor().execute(() -> {
            shipDAO.getAllShips().addOnSuccessListener(dataSnapshot -> {
                ships.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Ship ship = snapshot.getValue(Ship.class);
                    if (ship != null) {
                        ships.add(ship);
                    }
                }
                loadUpcomingTours();
            }).addOnFailureListener(e -> {
                runOnUiThread(() -> Toast.makeText(this, "Failed to load ships", Toast.LENGTH_SHORT).show());
            });
        });
    }
    
    private void loadUpcomingTours() {
        ThreadPool.getExecutor().execute(() -> {
            tourInstanceDAO.getAllTourInstances().addOnSuccessListener(dataSnapshot -> {
                List<TourInstance> upcomingInstances = new ArrayList<>();
                LocalDate today = LocalDate.now();
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TourInstance instance = snapshot.getValue(TourInstance.class);
                    if (instance != null && instance.getStartDate() != null) {
                        try {
                            LocalDate startDate = LocalDate.parse(instance.getStartDate());
                            if (!startDate.isBefore(today)) {
                                for (Tour tour : tours) {
                                    if (tour.getId().equals(instance.getTourId())) {
                                        instance.setTourName(tour.getName());
                                        break;
                                    }
                                }
                                for (Ship ship : ships) {
                                    if (ship.getId().equals(instance.getShipId())) {
                                        instance.setShipName(ship.getName());
                                        break;
                                    }
                                }
                                upcomingInstances.add(instance);
                            }
                        } catch (Exception e) {
                        }
                    }
                }
                
                runOnUiThread(() -> {
                    adapter.submitList(upcomingInstances);
                    if (upcomingInstances.isEmpty()) {
                        Toast.makeText(this, "No upcoming tours found", Toast.LENGTH_SHORT).show();
                    }
                });
            }).addOnFailureListener(e -> {
                runOnUiThread(() -> Toast.makeText(this, "Failed to load upcoming tours", Toast.LENGTH_SHORT).show());
            });
        });
    }
}
