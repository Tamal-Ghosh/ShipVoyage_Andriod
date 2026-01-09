package com.example.shipvoyage.ui.passenger;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.shipvoyage.R;
import com.example.shipvoyage.adapter.MyBookingAdapter;
import com.example.shipvoyage.dao.BookingDAO;
import com.example.shipvoyage.dao.TourDAO;
import com.example.shipvoyage.dao.TourInstanceDAO;
import com.example.shipvoyage.model.Booking;
import com.example.shipvoyage.model.Tour;
import com.example.shipvoyage.model.TourInstance;
import com.example.shipvoyage.util.PassengerNavHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class MyBookingsActivity extends AppCompatActivity {
    private static final String TAG = "MyBookingsActivity";
    private RecyclerView recyclerView;
    private TextView emptyView;
    private MyBookingAdapter adapter;
    private BookingDAO bookingDAO;
    private TourDAO tourDAO;
    private TourInstanceDAO tourInstanceDAO;
    private FirebaseAuth mAuth;
    private Map<String, Tour> toursMap = new HashMap<>();
    private Map<String, TourInstance> instancesMap = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_bookings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.myBookingsRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bookingDAO = new BookingDAO();
        tourDAO = new TourDAO();
        tourInstanceDAO = new TourInstanceDAO();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to view your trips", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, com.example.shipvoyage.ui.auth.UserTypeActivity.class));
            finish();
            return;
        }
        initViews();
        loadData();
    }
    private void initViews() {
        recyclerView = findViewById(R.id.bookingsRecyclerView);
        emptyView = findViewById(R.id.emptyView);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        PassengerNavHelper.setupBottomNavigation(this, bottomNav);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyBookingAdapter(this::showCancelDialog);
        recyclerView.setAdapter(adapter);
        ImageButton menuBtn = findViewById(R.id.menuBtn);
        if (menuBtn != null) {
            menuBtn.setOnClickListener(v -> PassengerNavHelper.setupNavigationMenu(this, v));
        }
    }
    private void loadData() {
        tourDAO.getAllTours().addOnSuccessListener(snapshot -> {
            for (DataSnapshot tourSnapshot : snapshot.getChildren()) {
                Tour tour = tourSnapshot.getValue(Tour.class);
                if (tour != null) {
                    toursMap.put(tour.getId(), tour);
                }
            }
            loadInstances();
        });
    }
    private void loadInstances() {
        tourInstanceDAO.getAllTourInstances().addOnSuccessListener(snapshot -> {
            for (DataSnapshot instanceSnapshot : snapshot.getChildren()) {
                TourInstance instance = instanceSnapshot.getValue(TourInstance.class);
                if (instance != null) {
                    instancesMap.put(instance.getId(), instance);
                }
            }
            loadBookings();
        });
    }
    private void loadBookings() {
        if (mAuth.getCurrentUser() == null) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();
            Log.d(TAG, "Loading bookings for user: " + userId);
        bookingDAO.getAllBookings().addOnSuccessListener(snapshot -> {
            List<Booking> userBookings = new ArrayList<>();
            Log.d(TAG, "Total bookings in database: " + snapshot.getChildrenCount());
            for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                Booking booking = bookingSnapshot.getValue(Booking.class);
                if (booking != null && userId.equals(booking.getUserId())) {
                    Log.d(TAG, "Booking ID: " + booking.getId() + " Status: " + booking.getStatus());
                    TourInstance instance = instancesMap.get(booking.getTourInstanceId());
                    if (instance != null) {
                        Tour tour = toursMap.get(instance.getTourId());
                        if (tour != null) {
                            booking.setTourName(tour.getName());
                            booking.setFromLocation(tour.getFrom());
                            booking.setToLocation(tour.getTo());
                        } else {
                            Log.w(TAG, "Tour not found for ID: " + instance.getTourId());
                            booking.setTourName("Unknown Tour");
                            booking.setFromLocation("N/A");
                            booking.setToLocation("N/A");
                        }
                        booking.setDepartureDate(instance.getStartDate());
                        booking.setReturnDate(instance.getEndDate());
                    } else {
                        Log.w(TAG, "Tour instance not found for ID: " + booking.getTourInstanceId());
                        booking.setTourName("Unknown Tour");
                        booking.setFromLocation("N/A");
                        booking.setToLocation("N/A");
                        booking.setDepartureDate("N/A");
                        booking.setReturnDate("N/A");
                    }
                    userBookings.add(booking);
                }
            }
            Log.d(TAG, "User bookings count: " + userBookings.size());
            if (userBookings.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.submitList(userBookings);
            }
        }).addOnFailureListener(e -> {
            Log.d("booking","Failed to load bookings: " + e.getMessage());
            Toast.makeText(this, "Failed to load bookings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    private void showCancelDialog(Booking booking) {
        new AlertDialog.Builder(this)
            .setTitle("Cancel Booking")
            .setMessage("Are you sure you want to cancel this booking?")
            .setPositiveButton("Yes", (dialog, which) -> cancelBooking(booking))
            .setNegativeButton("No", null)
            .show();
    }
    private void cancelBooking(Booking booking) {
        bookingDAO.deleteBooking(booking.getId())
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Booking cancelled successfully", Toast.LENGTH_SHORT).show();
                loadBookings();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to cancel booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}