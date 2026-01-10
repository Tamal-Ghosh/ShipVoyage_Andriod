package com.example.shipvoyage.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.shipvoyage.R;
import com.example.shipvoyage.dao.ShipDAO;
import com.example.shipvoyage.dao.TourDAO;
import com.example.shipvoyage.dao.TourInstanceDAO;
import com.example.shipvoyage.dao.BookingDAO;
import com.example.shipvoyage.dao.UserDAO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminDashboardFragment extends Fragment {
    private static final String TAG = "AdminDashboardFragment";

    private TextView lblTotalShips, lblTotalTours, lblTourInstances;
    private TextView lblUpcomingTours, lblCurrentTours, lblTotalBookings, lblTotalCustomers;

    private ShipDAO shipDAO;
    private TourDAO tourDAO;
    private TourInstanceDAO tourInstanceDAO;
    private BookingDAO bookingDAO;
    private UserDAO userDAO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        shipDAO = new ShipDAO();
        tourDAO = new TourDAO();
        tourInstanceDAO = new TourInstanceDAO();
        bookingDAO = new BookingDAO();
        userDAO = new UserDAO();

        initViews(view);

        loadDashboardData();
    }

    private void initViews(View view) {
        lblTotalShips = view.findViewById(R.id.lblTotalShips);
        lblTotalTours = view.findViewById(R.id.lblTotalTours);
        lblTourInstances = view.findViewById(R.id.lblTourInstances);
        lblUpcomingTours = view.findViewById(R.id.lblUpcomingTours);
        lblCurrentTours = view.findViewById(R.id.lblCurrentTours);
        lblTotalBookings = view.findViewById(R.id.lblTotalBookings);
        lblTotalCustomers = view.findViewById(R.id.lblTotalCustomers);

        NavController navController = Navigation.findNavController(view);

        Button btnViewShips = view.findViewById(R.id.btnViewShips);
        btnViewShips.setOnClickListener(v -> 
            navController.navigate(R.id.action_adminDashboardFragment_to_manageShipsFragment)
        );

        Button btnViewTours = view.findViewById(R.id.btnViewTours);
        btnViewTours.setOnClickListener(v -> 
            navController.navigate(R.id.action_adminDashboardFragment_to_manageToursFragment)
        );

        Button btnViewInstances = view.findViewById(R.id.btnViewInstances);
        btnViewInstances.setOnClickListener(v -> 
            navController.navigate(R.id.action_adminDashboardFragment_to_manageTourInstancesFragment)
        );

        Button btnViewUpcoming = view.findViewById(R.id.btnViewUpcoming);
        btnViewUpcoming.setOnClickListener(v -> 
            navController.navigate(R.id.action_adminDashboardFragment_to_manageTourInstancesFragment)
        );

        Button btnViewCurrent = view.findViewById(R.id.btnViewCurrent);
        btnViewCurrent.setOnClickListener(v -> 
            navController.navigate(R.id.action_adminDashboardFragment_to_manageTourInstancesFragment)
        );

        Button btnViewBookings = view.findViewById(R.id.btnViewBookings);
        btnViewBookings.setOnClickListener(v -> 
            navController.navigate(R.id.action_adminDashboardFragment_to_viewBookingsFragment)
        );

        Button btnViewCustomers = view.findViewById(R.id.btnViewCustomers);
        btnViewCustomers.setOnClickListener(v -> 
            navController.navigate(R.id.action_adminDashboardFragment_to_customerListFragment)
        );
    }

    private void loadDashboardData() {
        loadShipsCount();
        loadToursCount();
        loadTourInstancesCount();
        loadBookingsCount();
        loadCustomersCount();
    }

    private void loadShipsCount() {
        shipDAO.getAllShips().addOnSuccessListener(snapshot -> {
            long count = snapshot.getChildrenCount();
            lblTotalShips.setText(String.valueOf(count));
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading ships count: " + e.getMessage());
            lblTotalShips.setText("0");
        });
    }

    private void loadToursCount() {
        tourDAO.getAllTours().addOnSuccessListener(snapshot -> {
            long count = snapshot.getChildrenCount();
            lblTotalTours.setText(String.valueOf(count));
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading tours count: " + e.getMessage());
            lblTotalTours.setText("0");
        });
    }

    private void loadTourInstancesCount() {
        tourInstanceDAO.getAllTourInstances().addOnSuccessListener(snapshot -> {
            long totalCount = snapshot.getChildrenCount();
            int upcomingCount = 0;
            int currentCount = 0;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date now = new Date();

            for (DataSnapshot instanceSnapshot : snapshot.getChildren()) {
                String startDateStr = instanceSnapshot.child("start_date").getValue(String.class);
                String endDateStr = instanceSnapshot.child("end_date").getValue(String.class);

                if (startDateStr != null && endDateStr != null) {
                    try {
                        Date startDate = sdf.parse(startDateStr);
                        Date endDate = sdf.parse(endDateStr);

                        if (startDate != null && endDate != null) {
                            if (now.before(startDate)) {
                                upcomingCount++;
                            } else if (now.after(startDate) && now.before(endDate)) {
                                currentCount++;
                            }
                        }
                    } catch (ParseException e) {
                        Log.e(TAG, "Error parsing date: " + e.getMessage());
                    }
                }
            }

            lblTourInstances.setText(String.valueOf(totalCount));
            lblUpcomingTours.setText(String.valueOf(upcomingCount));
            lblCurrentTours.setText(String.valueOf(currentCount));
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading tour instances: " + e.getMessage());
            lblTourInstances.setText("0");
            lblUpcomingTours.setText("0");
            lblCurrentTours.setText("0");
        });
    }

    private void loadBookingsCount() {
        bookingDAO.getAllBookings().addOnSuccessListener(snapshot -> {
            long count = snapshot.getChildrenCount();
            lblTotalBookings.setText(String.valueOf(count));
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading bookings count: " + e.getMessage());
            lblTotalBookings.setText("0");
        });
    }

    private void loadCustomersCount() {
        userDAO.getAllUsers().addOnSuccessListener(snapshot -> {
            int customerCount = 0;
            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                String role = userSnapshot.child("role").getValue(String.class);
                if ("Passenger".equalsIgnoreCase(role)) {
                    customerCount++;
                }
            }
            lblTotalCustomers.setText(String.valueOf(customerCount));
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading customers count: " + e.getMessage());
            lblTotalCustomers.setText("0");
        });
    }
}
