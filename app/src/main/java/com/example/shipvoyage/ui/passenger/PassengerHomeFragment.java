package com.example.shipvoyage.ui.passenger;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shipvoyage.R;
import com.example.shipvoyage.adapter.FeaturedPhotoAdapter;
import com.example.shipvoyage.adapter.TourSearchResultAdapter;
import com.example.shipvoyage.adapter.UpcomingTripAdapter;
import com.example.shipvoyage.dao.FeaturedPhotoDAO;
import com.example.shipvoyage.dao.ShipDAO;
import com.example.shipvoyage.dao.TourDAO;
import com.example.shipvoyage.dao.TourInstanceDAO;
import com.example.shipvoyage.dao.UserDAO;
import com.example.shipvoyage.model.FeaturedPhoto;
import com.example.shipvoyage.model.Ship;
import com.example.shipvoyage.model.Tour;
import com.example.shipvoyage.model.TourInstance;
import com.example.shipvoyage.model.User;
import com.example.shipvoyage.util.ThreadPool;
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

public class PassengerHomeFragment extends Fragment {
    private AutoCompleteTextView fromField, toField;
    private TextInputEditText dateField;
    private Button searchButton;
    private LinearLayout resultsSection;
    private RecyclerView searchResultsRecyclerView, featuredRecyclerView, upcomingRecyclerView;
    private TextView viewAllUpcoming;
    private TextView welcomeText;

    private TourSearchResultAdapter searchResultsAdapter;
    private FeaturedPhotoAdapter featuredAdapter;
    private UpcomingTripAdapter upcomingAdapter;

    private TourDAO tourDAO;
    private TourInstanceDAO tourInstanceDAO;
    private ShipDAO shipDAO;
    private FeaturedPhotoDAO photoDAO;
    private UserDAO userDAO;

    private List<TourInstance> allInstances = new ArrayList<>();
    private Map<String, Tour> toursMap = new HashMap<>();
    private Map<String, Ship> shipsMap = new HashMap<>();
    private Set<String> fromLocations = new HashSet<>();
    private Set<String> toLocations = new HashSet<>();
    private String selectedDate = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_passenger_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tourDAO = new TourDAO();
        tourInstanceDAO = new TourInstanceDAO();
        shipDAO = new ShipDAO();
        photoDAO = new FeaturedPhotoDAO();
        userDAO = new UserDAO();

        initViews(view);
        loadData();
        loadUserProfile();
    }

    private void initViews(View view) {
        fromField = view.findViewById(R.id.fromField);
        toField = view.findViewById(R.id.toField);
        dateField = view.findViewById(R.id.dateField);
        searchButton = view.findViewById(R.id.searchButton);
        resultsSection = view.findViewById(R.id.resultsSection);
        searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView);
        featuredRecyclerView = view.findViewById(R.id.featuredRecyclerView);
        upcomingRecyclerView = view.findViewById(R.id.upcomingRecyclerView);
        viewAllUpcoming = view.findViewById(R.id.viewAllUpcoming);
        welcomeText = view.findViewById(R.id.welcomeText);

        dateField.setOnClickListener(v -> showDatePicker());
        searchButton.setOnClickListener(v -> searchTours());
        viewAllUpcoming.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.upcomingToursFragment);
        });

        searchResultsAdapter = new TourSearchResultAdapter(instance -> {
            Intent intent = new Intent(requireContext(), BookingActivity.class);
            intent.putExtra("tourInstanceId", instance.getId());
            startActivity(intent);
        });
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        searchResultsRecyclerView.setAdapter(searchResultsAdapter);

        featuredAdapter = new FeaturedPhotoAdapter();
        featuredRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        featuredRecyclerView.setAdapter(featuredAdapter);

        upcomingAdapter = new UpcomingTripAdapter();
        upcomingRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        upcomingRecyclerView.setAdapter(upcomingAdapter);
    }

    private void setupAutoComplete() {
        ArrayAdapter<String> fromAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>(fromLocations)
        );
        ArrayAdapter<String> toAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>(toLocations)
        );
        fromField.setAdapter(fromAdapter);
        fromField.setThreshold(1);
        toField.setAdapter(toAdapter);
        toField.setThreshold(1);
    }

    private void loadUserProfile() {
        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }
        String userId = currentUser.getUid();
        userDAO.getUser(userId).addOnSuccessListener(snapshot -> {
            User user = snapshot.getValue(User.class);
            if (user != null && user.getUsername() != null) {
                welcomeText.setText("Hello, " + user.getUsername() + "!");
            }
        });
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
            ThreadPool.getExecutor().execute(() -> {
                Map<String, Tour> newTours = new HashMap<>();
                Set<String> newFrom = new HashSet<>();
                Set<String> newTo = new HashSet<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Tour tour = snapshot.getValue(Tour.class);
                    if (tour != null) {
                        newTours.put(tour.getId(), tour);
                        newFrom.add(tour.getFrom());
                        newTo.add(tour.getTo());
                    }
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        toursMap.clear();
                        toursMap.putAll(newTours);
                        fromLocations.clear();
                        fromLocations.addAll(newFrom);
                        toLocations.clear();
                        toLocations.addAll(newTo);
                        setupAutoComplete();
                    });
                }
            });
        });
    }

    private void loadShips() {
        shipDAO.getAllShips().addOnSuccessListener(dataSnapshot -> {
            ThreadPool.getExecutor().execute(() -> {
                Map<String, Ship> newShips = new HashMap<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Ship ship = snapshot.getValue(Ship.class);
                    if (ship != null) {
                        newShips.put(ship.getId(), ship);
                    }
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        shipsMap.clear();
                        shipsMap.putAll(newShips);
                    });
                }
            });
        });
    }

    private void loadInstances() {
        tourInstanceDAO.getAllTourInstances().addOnSuccessListener(dataSnapshot -> {
            ThreadPool.getExecutor().execute(() -> {
                List<TourInstance> newInstances = new ArrayList<>();
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
                        newInstances.add(instance);
                    }
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        allInstances.clear();
                        allInstances.addAll(newInstances);
                    });
                }
            });
        });
    }

    private void loadFeaturedPhotos() {
        photoDAO.getAllPhotos().addOnSuccessListener(dataSnapshot -> {
            ThreadPool.getExecutor().execute(() -> {
                List<FeaturedPhoto> photos = new ArrayList<>();
                int count = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (count >= 6) break;
                    FeaturedPhoto photo = snapshot.getValue(FeaturedPhoto.class);
                    if (photo != null) {
                        photo.setId(snapshot.getKey());
                        photos.add(photo);
                        count++;
                    }
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> featuredAdapter.submitList(photos));
                }
            });
        });
    }

    private void loadUpcomingTrips() {
        tourInstanceDAO.getAllTourInstances().addOnSuccessListener(dataSnapshot -> {
            ThreadPool.getExecutor().execute(() -> {
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
                            instance.setFromLocation(tour.getFrom());
                            instance.setToLocation(tour.getTo());
                        }
                        if (ship != null) {
                            instance.setShipName(ship.getName());
                        }
                        upcoming.add(instance);
                        count++;
                    }
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> upcomingAdapter.submitList(upcoming));
                }
            });
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
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
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(requireContext(), "No tours found", Toast.LENGTH_SHORT).show();
        } else {
            searchResultsAdapter.submitList(results);
            resultsSection.setVisibility(View.VISIBLE);
        }
    }
}
