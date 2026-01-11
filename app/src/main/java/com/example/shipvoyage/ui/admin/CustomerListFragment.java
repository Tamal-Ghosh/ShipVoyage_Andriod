package com.example.shipvoyage.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import com.example.shipvoyage.adapter.CustomerAdapter;
import com.example.shipvoyage.dao.TourDAO;
import com.example.shipvoyage.dao.TourInstanceDAO;
import com.example.shipvoyage.dao.UserDAO;
import com.example.shipvoyage.model.Tour;
import com.example.shipvoyage.model.TourInstance;
import com.example.shipvoyage.model.User;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CustomerListFragment extends Fragment {
    private RecyclerView customersRecyclerView;
    private Spinner instanceSpinner;
    private EditText searchField;
    private Button searchBtn;
    private UserDAO userDAO;
    private TourDAO tourDAO;
    private TourInstanceDAO tourInstanceDAO;
    private List<User> customersList = new ArrayList<>();
    private List<Tour> toursList = new ArrayList<>();
    private List<TourInstance> instancesList = new ArrayList<>();
    private CustomerAdapter customerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customer_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        userDAO = new UserDAO();
        tourDAO = new TourDAO();
        tourInstanceDAO = new TourInstanceDAO();
        
        initViews(view);
        setupListeners();
        loadTours();
        loadCustomers();
    }

    private void initViews(View view) {
        customersRecyclerView = view.findViewById(R.id.customersRecyclerView);
        instanceSpinner = view.findViewById(R.id.instanceSpinner);
        searchField = view.findViewById(R.id.searchField);
        searchBtn = view.findViewById(R.id.searchBtn);

        customersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        customerAdapter = new CustomerAdapter(new CustomerAdapter.OnCustomerClickListener() {
            @Override
            public void onViewClick(User customer) {
                Toast.makeText(requireContext(), "View: " + customer.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(User customer) {
                userDAO.deleteUser(customer.getId()).addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "Customer deleted", Toast.LENGTH_SHORT).show();
                    loadCustomers();
                }).addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to delete customer", Toast.LENGTH_SHORT).show();
                });
            }
        });
        customersRecyclerView.setAdapter(customerAdapter);
    }

    private void setupListeners() {
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
            toursList.clear();
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                Tour tour = snapshot.getValue(Tour.class);
                if (tour != null) {
                    toursList.add(tour);
                }
            }
            loadInstances();
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Failed to load tours", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadInstances() {
        tourInstanceDAO.getAllTourInstances().addOnSuccessListener(dataSnapshot -> {
            instancesList.clear();
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
                    instancesList.add(instance);
                    instanceNames.add(instance.getTourName() + " - " + instance.getStartDate());
                }
            }
            
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, instanceNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            instanceSpinner.setAdapter(adapter);
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Failed to load instances", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadCustomers() {
        userDAO.getAllUsers().addOnSuccessListener(dataSnapshot -> {
            customersList.clear();
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                User user = snapshot.getValue(User.class);
                if (user != null && "PASSENGER".equalsIgnoreCase(user.getRole())) {
                    customersList.add(user);
                }
            }
            updateRecyclerView();
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Failed to load customers", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateRecyclerView() {
        customerAdapter.submitList(new ArrayList<>(customersList));
    }

    private void performSearch() {
        String query = searchField.getText().toString().trim().toLowerCase();
        int selectedPosition = instanceSpinner.getSelectedItemPosition();
        
        List<User> filteredList = new ArrayList<>();
        for (User customer : customersList) {
            boolean matchesSearch = query.isEmpty() || 
                customer.getName().toLowerCase().contains(query) ||
                customer.getEmail().toLowerCase().contains(query) ||
                customer.getPhone().contains(query);
            
            boolean matchesInstance = selectedPosition == 0 ||
                (customer.getLastInstance() != null && customer.getLastInstance().equals(instancesList.get(selectedPosition - 1).getId()));
            
            if (matchesSearch && matchesInstance) {
                filteredList.add(customer);
            }
        }
        customerAdapter.submitList(filteredList);
    }
}
