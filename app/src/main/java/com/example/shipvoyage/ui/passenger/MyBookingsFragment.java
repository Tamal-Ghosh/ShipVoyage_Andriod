package com.example.shipvoyage.ui.passenger;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyBookingsFragment extends Fragment {
    private static final String TAG = "MyBookingsFragment";
    private RecyclerView recyclerView;
    private TextView emptyView;
    private MyBookingAdapter adapter;
    private BookingDAO bookingDAO;
    private TourDAO tourDAO;
    private TourInstanceDAO tourInstanceDAO;
    private FirebaseAuth mAuth;
    private Map<String, Tour> toursMap = new HashMap<>();
    private Map<String, TourInstance> instancesMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        bookingDAO = new BookingDAO();
        tourDAO = new TourDAO();
        tourInstanceDAO = new TourInstanceDAO();
        mAuth = FirebaseAuth.getInstance();
        
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Please log in to view your trips", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireActivity(), com.example.shipvoyage.ui.auth.UserTypeActivity.class));
            requireActivity().finish();
            return;
        }
        
        initViews(view);
        loadData();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.bookingsRecyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MyBookingAdapter(this::showCancelDialog);
        recyclerView.setAdapter(adapter);
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
            Log.d("booking", "Failed to load bookings: " + e.getMessage());
            Toast.makeText(requireContext(), "Failed to load bookings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void showCancelDialog(Booking booking) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Cancel Booking")
            .setMessage("Are you sure you want to cancel this booking?")
            .setPositiveButton("Yes", (dialog, which) -> cancelBooking(booking))
            .setNegativeButton("No", null)
            .show();
    }

    private void cancelBooking(Booking booking) {
        bookingDAO.deleteBooking(booking.getId())
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(requireContext(), "Booking cancelled successfully", Toast.LENGTH_SHORT).show();
                loadBookings();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Failed to cancel booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}
