package com.example.shipvoyage.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import com.example.shipvoyage.util.ThreadPool;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewBookingsFragment extends Fragment {
    private RecyclerView bookingsRecyclerView;
    private Spinner tourInstanceSpinner;
    private BookingDAO bookingDAO;
    private TourDAO tourDAO;
    private TourInstanceDAO tourInstanceDAO;
    private UserDAO userDAO;
    private List<Booking> bookingsList = new ArrayList<>();
    private List<Tour> toursList = new ArrayList<>();
    private List<TourInstance> instancesList = new ArrayList<>();
    private Map<String, User> usersMap = new HashMap<>();
    private BookingAdapter bookingAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        bookingDAO = new BookingDAO();
        tourDAO = new TourDAO();
        tourInstanceDAO = new TourInstanceDAO();
        userDAO = new UserDAO();
        
        initViews(view);
        loadTours();
    }

    private void initViews(View view) {
        bookingsRecyclerView = view.findViewById(R.id.bookingsRecyclerView);
        tourInstanceSpinner = view.findViewById(R.id.tourInstanceSpinner);

        bookingsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        bookingAdapter = new BookingAdapter(new BookingAdapter.OnBookingClickListener() {
            @Override
            public void onViewClick(Booking booking) {
                Toast.makeText(requireContext(), "Booking details: " + booking.getId(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(Booking booking) {
                bookingDAO.deleteBooking(booking.getId()).addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "Booking deleted", Toast.LENGTH_SHORT).show();
                    loadBookings();
                }).addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to delete booking", Toast.LENGTH_SHORT).show();
                });
            }
        });
        bookingsRecyclerView.setAdapter(bookingAdapter);
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
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        toursList.clear();
                        toursList.addAll(newTours);
                        loadInstances();
                    });
                }
            });
        }).addOnFailureListener(e -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Failed to load tours", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadInstances() {
        tourInstanceDAO.getAllTourInstances().addOnSuccessListener(dataSnapshot -> {
            ThreadPool.getExecutor().execute(() -> {
                List<TourInstance> newInstances = new ArrayList<>();
                List<String> instanceNames = new ArrayList<>();
                instanceNames.add("Select Tour Instance");
                
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
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        instancesList.clear();
                        instancesList.addAll(newInstances);
                        
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, instanceNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        tourInstanceSpinner.setAdapter(adapter);
                        tourInstanceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                filterBookings();
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {}
                        });
                        loadBookings();
                    });
                }
            });
        }).addOnFailureListener(e -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Failed to load instances", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadBookings() {
        bookingDAO.getAllBookings().addOnSuccessListener(dataSnapshot -> {
            List<Booking> newBookings = new ArrayList<>();
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                Booking booking = snapshot.getValue(Booking.class);
                if (booking != null) {
                    newBookings.add(booking);
                }
            }
            bookingsList.clear();
            bookingsList.addAll(newBookings);
            
            // Fetch customer data for each booking
            fetchCustomerDataForBookings();
        }).addOnFailureListener(e -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Failed to load bookings", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void fetchCustomerDataForBookings() {
        if (bookingsList.isEmpty()) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::filterBookings);
            }
            return;
        }

        ThreadPool.getExecutor().execute(() -> {
            userDAO.getAllUsers().addOnSuccessListener(dataSnapshot -> {
                Map<String, User> usersMap = new HashMap<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        usersMap.put(user.getId(), user);
                    }
                }

                // Now match bookings with their customer data
                for (Booking booking : bookingsList) {
                    User user = usersMap.get(booking.getUserId());
                    if (user != null) {
                        booking.setCustomerName(user.getName() != null ? user.getName() : "N/A");
                        booking.setCustomerEmail(user.getEmail() != null ? user.getEmail() : "N/A");
                        booking.setCustomerPhone(user.getPhone() != null ? user.getPhone() : "N/A");
                    }
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(this::filterBookings);
                }
            }).addOnFailureListener(e -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(this::filterBookings);
                }
            });
        });
    }

    private void filterBookings() {
        int selectedPosition = tourInstanceSpinner.getSelectedItemPosition();
        if (instancesList == null || instancesList.isEmpty()) {
            bookingAdapter.submitList(new ArrayList<>(bookingsList));
            return;
        }
        
        if (selectedPosition == 0) {
            bookingAdapter.submitList(new ArrayList<>(bookingsList));
        } else if (selectedPosition - 1 < instancesList.size()) {
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
}
