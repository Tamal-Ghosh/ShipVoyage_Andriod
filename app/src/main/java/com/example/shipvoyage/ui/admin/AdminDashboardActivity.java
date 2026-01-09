package com.example.shipvoyage.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shipvoyage.R;
import com.example.shipvoyage.dao.BookingDAO;
import com.example.shipvoyage.dao.ShipDAO;
import com.example.shipvoyage.dao.TourDAO;
import com.example.shipvoyage.dao.TourInstanceDAO;
import com.example.shipvoyage.dao.UserDAO;
import com.example.shipvoyage.model.TourInstance;
import com.example.shipvoyage.util.AdminNavHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminDashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private TextView lblTotalShips, lblTotalTours, lblTourInstances, lblUpcomingTours, lblCurrentTours, lblTotalBookings, lblTotalCustomers;
    private ShipDAO shipDAO;
    private TourDAO tourDAO;
    private TourInstanceDAO tourInstanceDAO;
    private BookingDAO bookingDAO;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.adminDashboardRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        shipDAO = new ShipDAO();
        tourDAO = new TourDAO();
        tourInstanceDAO = new TourInstanceDAO();
        bookingDAO = new BookingDAO();
        userDAO = new UserDAO();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        loadDashboardData();
    }

    private void initViews() {
        lblTotalShips = findViewById(R.id.lblTotalShips);
        lblTotalTours = findViewById(R.id.lblTotalTours);
        lblTourInstances = findViewById(R.id.lblTourInstances);
        lblUpcomingTours = findViewById(R.id.lblUpcomingTours);
        lblCurrentTours = findViewById(R.id.lblCurrentTours);
        lblTotalBookings = findViewById(R.id.lblTotalBookings);
        lblTotalCustomers = findViewById(R.id.lblTotalCustomers);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        AdminNavHelper.setupBottomNavigation(this, bottomNav);

        Button btnViewShips = findViewById(R.id.btnViewShips);
        btnViewShips.setOnClickListener(v -> startActivity(new Intent(this, ManageShipsActivity.class)));

        Button btnViewTours = findViewById(R.id.btnViewTours);
        btnViewTours.setOnClickListener(v -> startActivity(new Intent(this, ManageToursActivity.class)));

        Button btnViewInstances = findViewById(R.id.btnViewInstances);
        btnViewInstances.setOnClickListener(v -> startActivity(new Intent(this, ManageTourInstancesActivity.class)));

        Button btnViewUpcoming = findViewById(R.id.btnViewUpcoming);
        btnViewUpcoming.setOnClickListener(v -> startActivity(new Intent(this, ManageTourInstancesActivity.class)));

        Button btnViewCurrent = findViewById(R.id.btnViewCurrent);
        btnViewCurrent.setOnClickListener(v -> startActivity(new Intent(this, ManageTourInstancesActivity.class)));

        Button btnViewBookings = findViewById(R.id.btnViewBookings);
        btnViewBookings.setOnClickListener(v -> startActivity(new Intent(this, ViewBookingsActivity.class)));

        Button btnViewCustomers = findViewById(R.id.btnViewCustomers);
        btnViewCustomers.setOnClickListener(v -> startActivity(new Intent(this, CustomerListActivity.class)));
    }

    private void loadDashboardData() {
        loadShipsCount();
        loadToursCount();
        loadTourInstancesCount();
        loadBookingsCount();
        loadCustomersCount();
    }

    private void loadShipsCount() {
        shipDAO.getAllShips().addOnSuccessListener(dataSnapshot -> {
            int count = (int) dataSnapshot.getChildrenCount();
            lblTotalShips.setText(String.valueOf(count));
        });
    }

    private void loadToursCount() {
        tourDAO.getAllTours().addOnSuccessListener(dataSnapshot -> {
            int count = (int) dataSnapshot.getChildrenCount();
            lblTotalTours.setText(String.valueOf(count));
        });
    }

    private void loadTourInstancesCount() {
        tourInstanceDAO.getAllTourInstances().addOnSuccessListener(dataSnapshot -> {
            int totalCount = 0;
            int upcomingCount = 0;
            int currentCount = 0;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date today = new Date();

            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                TourInstance instance = snapshot.getValue(TourInstance.class);
                if (instance != null) {
                    totalCount++;
                    try {
                        Date startDate = sdf.parse(instance.getStartDate());
                        Date endDate = sdf.parse(instance.getEndDate());
                        if (startDate != null && endDate != null) {
                            if (startDate.after(today)) {
                                upcomingCount++;
                            } else if (!endDate.before(today)) {
                                currentCount++;
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            lblTourInstances.setText(String.valueOf(totalCount));
            lblUpcomingTours.setText(String.valueOf(upcomingCount));
            lblCurrentTours.setText(String.valueOf(currentCount));
        });
    }

    private void loadBookingsCount() {
        bookingDAO.getAllBookings().addOnSuccessListener(dataSnapshot -> {
            int count = (int) dataSnapshot.getChildrenCount();
            lblTotalBookings.setText(String.valueOf(count));
        });
    }

    private void loadCustomersCount() {
        userDAO.getAllUsers().addOnSuccessListener(dataSnapshot -> {
            int count = 0;
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                String role = snapshot.child("role").getValue(String.class);
                if ("PASSENGER".equals(role)) {
                    count++;
                }
            }
            lblTotalCustomers.setText(String.valueOf(count));
        });
    }
}

