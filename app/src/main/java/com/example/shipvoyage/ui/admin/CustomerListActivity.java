package com.example.shipvoyage.ui.admin;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.shipvoyage.adapter.CustomerAdapter;
import com.example.shipvoyage.dao.TourDAO;
import com.example.shipvoyage.dao.TourInstanceDAO;
import com.example.shipvoyage.dao.UserDAO;
import com.example.shipvoyage.model.Tour;
import com.example.shipvoyage.model.TourInstance;
import com.example.shipvoyage.model.User;
import com.example.shipvoyage.util.AdminNavHelper;
import com.google.firebase.database.DataSnapshot;
import java.util.ArrayList;
import java.util.List;
public class CustomerListActivity extends AppCompatActivity {
    private RecyclerView customersRecyclerView;
    private Spinner instanceSpinner;
    private EditText searchField;
    private Button searchBtn;
    private UserDAO userDAO;
    private TourDAO tourDAO;
    private TourInstanceDAO tourInstanceDAO;
    private List<User> customersList;
    private List<Tour> toursList;
    private List<TourInstance> instancesList;
    private CustomerAdapter customerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.customerListRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
        userDAO = new UserDAO();
        tourDAO = new TourDAO();
        tourInstanceDAO = new TourInstanceDAO();
        customersList = new ArrayList<>();
        toursList = new ArrayList<>();
        instancesList = new ArrayList<>();
        initViews();
        setupListeners();
        loadTours();
        loadCustomers();
    }
    private void initViews() {
        customersRecyclerView = findViewById(R.id.customersRecyclerView);
        instanceSpinner = findViewById(R.id.instanceSpinner);
        searchField = findViewById(R.id.searchField);
        searchBtn = findViewById(R.id.searchBtn);
        ImageButton menuBtn = findViewById(R.id.menuBtn);
        menuBtn.setOnClickListener(v -> AdminNavHelper.setupNavigationMenu(this, v));
        customersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        customerAdapter = new CustomerAdapter(new CustomerAdapter.OnCustomerClickListener() {
            @Override
            public void onViewClick(User customer) {
                Toast.makeText(CustomerListActivity.this, "View: " + customer.getName(), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onDeleteClick(User customer) {
                userDAO.deleteUser(customer.getId()).addOnSuccessListener(unused -> {
                    Toast.makeText(CustomerListActivity.this, "Customer deleted", Toast.LENGTH_SHORT).show();
                    loadCustomers();
                }).addOnFailureListener(e -> {
                    Toast.makeText(CustomerListActivity.this, "Failed to delete customer", Toast.LENGTH_SHORT).show();
                });
            }
        });
        customersRecyclerView.setAdapter(customerAdapter);
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) {
            AdminNavHelper.setupBottomNavigation(this, bottomNav);
        }
    }
    private void setupListeners() {
        searchBtn.setOnClickListener(v -> performSearch());
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch();
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
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
            Toast.makeText(this, "Failed to load tours", Toast.LENGTH_SHORT).show();
        });
    }
    private void loadInstances() {
        tourInstanceDAO.getAllTourInstances().addOnSuccessListener(dataSnapshot -> {
            instancesList.clear();
            List<String> instanceNames = new ArrayList<>();
            instanceNames.add("All Instances");
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
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, instanceNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            instanceSpinner.setAdapter(adapter);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load instances", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Failed to load customers", Toast.LENGTH_SHORT).show();
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
            boolean matchesSearch = query.isEmpty() || customer.getName().toLowerCase().contains(query) ||
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