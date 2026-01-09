package com.example.shipvoyage.ui.passenger;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.shipvoyage.R;
import com.example.shipvoyage.dao.BookingDAO;
import com.example.shipvoyage.dao.UserDAO;
import com.example.shipvoyage.model.User;
import com.example.shipvoyage.ui.auth.UserTypeActivity;
import com.example.shipvoyage.util.PassengerNavHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
public class ProfileActivity extends AppCompatActivity {
    private EditText usernameField;
    private EditText emailField;
    private EditText phoneField;
    private EditText currentPasswordField;
    private EditText newPasswordField;
    private EditText confirmPasswordField;
    private TextView memberSinceLabel;
    private TextView totalBookingsLabel;
    private ImageView profileImage;
    private Button editButton;
    private Button logoutButton;
    private Button changePasswordButton;
    private UserDAO userDAO;
    private BookingDAO bookingDAO;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private boolean isEditing = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profileRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        PassengerNavHelper.setupBottomNavigation(this, bottomNav);
        userDAO = new UserDAO();
        bookingDAO = new BookingDAO();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to view your profile", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, UserTypeActivity.class));
            finish();
            return;
        }
        initViews();
        loadUserProfile();
        loadBookingStats();
    }
    private void initViews() {
        usernameField = findViewById(R.id.usernameField);
        emailField = findViewById(R.id.emailField);
        phoneField = findViewById(R.id.phoneField);
        currentPasswordField = findViewById(R.id.currentPasswordField);
        newPasswordField = findViewById(R.id.newPasswordField);
        confirmPasswordField = findViewById(R.id.confirmPasswordField);
        memberSinceLabel = findViewById(R.id.memberSinceLabel);
        totalBookingsLabel = findViewById(R.id.totalBookingsLabel);
        editButton = findViewById(R.id.editButton);
        logoutButton = findViewById(R.id.logoutButton);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        editButton.setOnClickListener(v -> toggleEditMode());
        logoutButton.setOnClickListener(v -> logout());
        changePasswordButton.setOnClickListener(v -> changePassword());
        setFieldsEnabled(false);
    }
    private void loadUserProfile() {
        if (mAuth.getCurrentUser() == null) {
            return;
        }
        currentUserId = mAuth.getCurrentUser().getUid();
        userDAO.getUser(currentUserId).addOnSuccessListener(snapshot -> {
            User user = snapshot.getValue(User.class);
            if (user != null) {
                usernameField.setText(user.getUsername());
                emailField.setText(user.getEmail());
                phoneField.setText(user.getPhone());
                if (user.getCreatedAt() > 0) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    memberSinceLabel.setText(sdf.format(new Date(user.getCreatedAt())));
                } else {
                    memberSinceLabel.setText("N/A");
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    private void loadBookingStats() {
        if (mAuth.getCurrentUser() == null) {
            totalBookingsLabel.setText("0");
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();
        bookingDAO.getAllBookings().addOnSuccessListener(snapshot -> {
            long count = 0;
            for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                com.example.shipvoyage.model.Booking booking = bookingSnapshot.getValue(com.example.shipvoyage.model.Booking.class);
                if (booking != null && userId.equals(booking.getUserId())) {
                    count++;
                }
            }
            totalBookingsLabel.setText(String.valueOf(count));
        }).addOnFailureListener(e -> {
            totalBookingsLabel.setText("0");
        });
    }
    private void toggleEditMode() {
        if (!isEditing) {
            setFieldsEnabled(true);
            editButton.setText("Save");
            isEditing = true;
        } else {
            saveUserProfile();
        }
    }
    private void saveUserProfile() {
        String username = usernameField.getText().toString().trim();
        String phone = phoneField.getText().toString().trim();
        if (username.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        updates.put("phone", phone);
        userDAO.updateUser(currentUserId, updates).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            setFieldsEnabled(false);
            editButton.setText("Edit Profile");
            isEditing = false;
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    private void changePassword() {
        String currentPassword = currentPasswordField.getText().toString().trim();
        String newPassword = newPasswordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all password fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPassword.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to change password", Toast.LENGTH_SHORT).show();
            return;
        }
        String email = mAuth.getCurrentUser().getEmail();
        mAuth.signInWithEmailAndPassword(email, currentPassword)
            .addOnSuccessListener(authResult -> {
                mAuth.getCurrentUser().updatePassword(newPassword)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                        currentPasswordField.setText("");
                        newPasswordField.setText("");
                        confirmPasswordField.setText("");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to change password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
            });
    }
    private void setFieldsEnabled(boolean enabled) {
        usernameField.setEnabled(enabled);
        phoneField.setEnabled(enabled);
        emailField.setEnabled(false);
    }
    private void logout() {
        mAuth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, UserTypeActivity.class));
        finish();
    }
}