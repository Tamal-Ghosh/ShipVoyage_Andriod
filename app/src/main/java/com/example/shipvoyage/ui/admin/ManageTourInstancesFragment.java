package com.example.shipvoyage.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shipvoyage.R;
import com.example.shipvoyage.adapter.TourInstanceAdapter;
import com.example.shipvoyage.dao.TourDAO;
import com.example.shipvoyage.dao.TourInstanceDAO;
import com.example.shipvoyage.dao.ShipDAO;
import com.example.shipvoyage.model.Tour;
import com.example.shipvoyage.model.TourInstance;
import com.example.shipvoyage.model.Ship;
import com.example.shipvoyage.util.ThreadPool;

import java.util.ArrayList;
import java.util.List;

public class ManageTourInstancesFragment extends Fragment {
    private RecyclerView instancesRecyclerView;
    private EditText startDateField, endDateField, searchField;
    private Spinner tourSpinner, shipSpinner;
    private Button saveBtn, cancelBtn, searchBtn;
    private TourInstanceDAO instanceDAO;
    private TourDAO tourDAO;
    private ShipDAO shipDAO;
    private List<TourInstance> instancesList = new ArrayList<>();
    private List<Tour> toursList = new ArrayList<>();
    private List<Ship> shipsList = new ArrayList<>();
    private TourInstanceAdapter adapter;
    private String editingInstanceId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_tour_instances, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        instanceDAO = new TourInstanceDAO();
        tourDAO = new TourDAO();
        shipDAO = new ShipDAO();
        initViews(view);
        setupListeners();
        loadTours();
        loadShips();
        loadInstances();
    }

    private void initViews(View view) {
        instancesRecyclerView = view.findViewById(R.id.instancesRecyclerView);
        startDateField = view.findViewById(R.id.startDateField);
        endDateField = view.findViewById(R.id.endDateField);
        searchField = view.findViewById(R.id.searchField);
        tourSpinner = view.findViewById(R.id.tourSpinner);
        shipSpinner = view.findViewById(R.id.shipSpinner);
        saveBtn = view.findViewById(R.id.saveBtn);
        cancelBtn = view.findViewById(R.id.cancelBtn);
        searchBtn = view.findViewById(R.id.searchBtn);

        instancesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TourInstanceAdapter(new TourInstanceAdapter.OnTourInstanceClickListener() {
            @Override
            public void onEditClick(TourInstance instance) {
                editingInstanceId = instance.getId();
                startDateField.setText(instance.getStartDate());
                endDateField.setText(instance.getEndDate());
            }

            @Override
            public void onDeleteClick(TourInstance instance) {
                instanceDAO.deleteTourInstance(instance.getId()).addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "Instance deleted", Toast.LENGTH_SHORT).show();
                    loadInstances();
                }).addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to delete instance", Toast.LENGTH_SHORT).show();
                });
            }
        });
        instancesRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        saveBtn.setOnClickListener(v -> saveInstance());
        cancelBtn.setOnClickListener(v -> clearForm());
        searchBtn.setOnClickListener(v -> performSearch());
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch();
            }
            @Override
            public void afterTextChanged(Editable s) {}
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
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        toursList.clear();
                        toursList.addAll(newTours);
                    });
                }
            });
        });
    }

    private void loadShips() {
        shipDAO.getAllShips().addOnSuccessListener(dataSnapshot -> {
            ThreadPool.getExecutor().execute(() -> {
                List<Ship> newShips = new ArrayList<>();
                for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Ship ship = snapshot.getValue(Ship.class);
                    if (ship != null) {
                        newShips.add(ship);
                    }
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        shipsList.clear();
                        shipsList.addAll(newShips);
                    });
                }
            });
        });
    }

    private void loadInstances() {
        instanceDAO.getAllTourInstances().addOnSuccessListener(dataSnapshot -> {
            ThreadPool.getExecutor().execute(() -> {
                List<TourInstance> newInstances = new ArrayList<>();
                for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TourInstance instance = snapshot.getValue(TourInstance.class);
                    if (instance != null) {
                        newInstances.add(instance);
                    }
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        instancesList.clear();
                        instancesList.addAll(newInstances);
                        updateRecyclerView();
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

    private void saveInstance() {
        String startDate = startDateField.getText().toString().trim();
        String endDate = endDateField.getText().toString().trim();

        if (startDate.isEmpty() || endDate.isEmpty() || tourSpinner.getSelectedItem() == null || shipSpinner.getSelectedItem() == null) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Tour selectedTour = (Tour) tourSpinner.getSelectedItem();
        Ship selectedShip = (Ship) shipSpinner.getSelectedItem();

        String instanceId = editingInstanceId != null ? editingInstanceId : instanceDAO.tourInstancesRef.push().getKey();
        if (instanceId != null) {
            TourInstance instance = new TourInstance(instanceId, selectedTour.getId(), selectedShip.getId(), startDate, endDate);
            instance.setTourName(selectedTour.getName());
            instance.setShipName(selectedShip.getName());
            instanceDAO.addTourInstance(instance).addOnSuccessListener(unused -> {
                Toast.makeText(requireContext(), editingInstanceId != null ? "Instance updated" : "Instance saved", Toast.LENGTH_SHORT).show();
                clearForm();
                loadInstances();
            }).addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Failed to save instance", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void clearForm() {
        editingInstanceId = null;
        startDateField.setText("");
        endDateField.setText("");
    }

    private void performSearch() {
        String query = searchField.getText().toString().trim().toLowerCase();
        if (query.isEmpty()) {
            adapter.submitList(new ArrayList<>(instancesList));
            return;
        }

        List<TourInstance> filteredInstances = new ArrayList<>();
        for (TourInstance instance : instancesList) {
            if ((instance.getTourName() != null && instance.getTourName().toLowerCase().contains(query)) ||
                (instance.getShipName() != null && instance.getShipName().toLowerCase().contains(query)) ||
                instance.getStartDate().toLowerCase().contains(query)) {
                filteredInstances.add(instance);
            }
        }
        adapter.submitList(new ArrayList<>(filteredInstances));
    }

    private void updateRecyclerView() {
        adapter.submitList(new ArrayList<>(instancesList));
    }
}
