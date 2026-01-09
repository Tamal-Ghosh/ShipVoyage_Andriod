package com.example.shipvoyage.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.example.shipvoyage.adapter.BookingAdapter;
import com.example.shipvoyage.dao.BookingDAO;
import com.example.shipvoyage.dao.TourDAO;
import com.example.shipvoyage.dao.TourInstanceDAO;
import com.example.shipvoyage.dao.UserDAO;
import com.example.shipvoyage.model.Booking;
import com.example.shipvoyage.model.Tour;
import com.example.shipvoyage.model.TourInstance;
import com.example.shipvoyage.model.User;
import com.example.shipvoyage.util.AdminNavHelper;
import com.example.shipvoyage.util.ThreadPool;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewBookingsActivity extends AppCompatActivity {

    private RecyclerView bookingsRecyclerView;
    private Spinner tourInstanceSpinner;

    private BookingDAO bookingDAO;
    private TourDAO tourDAO;
    private TourInstanceDAO tourInstanceDAO;
    private UserDAO userDAO;
    private List<Booking> bookingsList;
    private List<Tour> toursList;
    private List<TourInstance> instancesList;
    private Map<String, User> usersMap;
    private BookingAdapter bookingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_bookings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.viewBookingsRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        bookingDAO = new BookingDAO();
        tourDAO = new TourDAO();
        tourInstanceDAO = new TourInstanceDAO();
        userDAO = new UserDAO();
        bookingsList = new ArrayList<>();
        toursList = new ArrayList<>();
        instancesList = new ArrayList<>();
        usersMap = new HashMap<>();

        initViews();
        loadUsers();
        loadTours();
        loadBookings();
    }

    private void initViews() {
        bookingsRecyclerView = findViewById(R.id.bookingsRecyclerView);
        tourInstanceSpinner = findViewById(R.id.tourInstanceSpinner);

        ImageButton menuBtn = findViewById(R.id.menuBtn);
        menuBtn.setOnClickListener(v -> AdminNavHelper.setupNavigationMenu(this, v));

        bookingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        bookingAdapter = new BookingAdapter(new BookingAdapter.OnBookingClickListener() {
            @Override
            public void onViewClick(Booking booking) {
                Toast.makeText(ViewBookingsActivity.this, "Booking details: " + booking.getId(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(Booking booking) {
                bookingDAO.deleteBooking(booking.getId()).addOnSuccessListener(unused -> {
                    Toast.makeText(ViewBookingsActivity.this, "Booking deleted", Toast.LENGTH_SHORT).show();
                    loadBookings();
                }).addOnFailureListener(e -> {
                    Toast.makeText(ViewBookingsActivity.this, "Failed to delete booking", Toast.LENGTH_SHORT).show();
                });
            }
        });

        bookingsRecyclerView.setAdapter(bookingAdapter);
    }

    private void loadUsers() {
        userDAO.getAllUsers().addOnSuccessListener(dataSnapshot -> {
            ThreadPool.getExecutor().execute(() -> {
                Map<String, User> newUsersMap = new HashMap<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        newUsersMap.put(user.getId(), user);
                    }
                }
                runOnUiThread(() -> {
                    usersMap.clear();
                    usersMap.putAll(newUsersMap);
                });
            });
        }).addOnFailureListener(e -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show();
            });
        });
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
                    loadInstances();
                });
            });
        }).addOnFailureListener(e -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "Failed to load tours", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void loadInstances() {
        tourInstanceDAO.getAllTourInstances().addOnSuccessListener(dataSnapshot -> {
            ThreadPool.getExecutor().execute(() -> {
                List<TourInstance> newInstances = new ArrayList<>();
                List<String> instanceNames = new ArrayList<>();
                instanceNames.add("All Tour Instances");

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TourInstance instance = snapshot.getValue(TourInstance.class);
                    if (instance != null) {
                        for (Tour tour : toursList) {
                            if (tour.getId().equals(instance.getTourId())) {
                                instance.setTourName(tour.getName());
                                break;
                            }
                        }
                        newInstances.add(instance);
                        instanceNames.add(instance.getTourName() + " - " + instance.getStartDate());
                    }
                }

                runOnUiThread(() -> {
                    instancesList.clear();
                    instancesList.addAll(newInstances);

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, instanceNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    tourInstanceSpinner.setAdapter(adapter);

                    tourInstanceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            filterBookings();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                });
            });
        }).addOnFailureListener(e -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "Failed to load instances", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void loadBookings() {
        bookingDAO.getAllBookings().addOnSuccessListener(dataSnapshot -> {
            ThreadPool.getExecutor().execute(() -> {
                List<Booking> newBookings = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Booking booking = snapshot.getValue(Booking.class);
                    if (booking != null) {

                        User user = usersMap.get(booking.getUserId());
                        if (user != null) {
                            booking.setCustomerName(user.getName());
                            booking.setCustomerEmail(user.getEmail());
                            booking.setCustomerPhone(user.getPhone());
                        }
                        newBookings.add(booking);
                    }
                }
                runOnUiThread(() -> {
                    bookingsList.clear();
                    bookingsList.addAll(newBookings);
                    filterBookings();
                });
            });
        }).addOnFailureListener(e -> {

            runOnUiThread(() -> {
                Toast.makeText(this, "Failed to load bookings", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void filterBookings() {
        int selectedPosition = tourInstanceSpinner.getSelectedItemPosition();
        
        // Guard against empty instances list
        if (instancesList == null || instancesList.isEmpty()) {
            bookingAdapter.submitList(new ArrayList<>(bookingsList));
            return;
        }
        
        if (selectedPosition == 0) {
            // Show all bookings
            bookingAdapter.submitList(new ArrayList<>(bookingsList));
        } else if (selectedPosition - 1 < instancesList.size()) {
            // Filter by selected instance
            TourInstance selectedInstance = instancesList.get(selectedPosition - 1);
            List<Booking> filteredList = new ArrayList<>();
            
            for (Booking booking : bookingsList) {
                if (booking.getTourInstanceId().equals(selectedInstance.getId())) {
                    filteredList.add(booking);
                }
            }
            
            bookingAdapter.submitList(filteredList);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

