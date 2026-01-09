package com.example.shipvoyage.ui.auth;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.shipvoyage.R;
import com.example.shipvoyage.dao.UserDAO;
import com.example.shipvoyage.model.User;
import com.example.shipvoyage.ui.admin.AdminDashboardActivity;
import com.example.shipvoyage.ui.passenger.PassengerHomeActivity;
import com.google.firebase.auth.FirebaseAuth;
public class LoginActivity extends AppCompatActivity {
    private EditText emailField;
    private EditText passwordField;
    private Button loginBtn;
    private TextView signupLink;
    private String selectedRole = null;
    private UserDAO userDAO;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        selectedRole = getIntent().getStringExtra("role");
        if (selectedRole == null) {
            Toast.makeText(this, "Please select user type first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userDAO = new UserDAO();
        mAuth = FirebaseAuth.getInstance();
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        loginBtn = findViewById(R.id.loginBtn);
        signupLink = findViewById(R.id.signupLink);
        loginBtn.setOnClickListener(v -> attemptLogin());
        signupLink.setOnClickListener(v -> openSignup());
    }
    private void attemptLogin() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                String uid = authResult.getUser().getUid();
                userDAO.getUser(uid).addOnSuccessListener(snapshot -> {
                    User user = snapshot.getValue(User.class);
                    if (user == null) {
                        Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (user.getRole() != null && !user.getRole().equalsIgnoreCase(selectedRole)) {
                        Toast.makeText(this, "Role mismatch for this account", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                        return;
                    }
                    Toast.makeText(this, "Login success", Toast.LENGTH_SHORT).show();
                    navigateToHome(user.getRole());
                }).addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            })
            .addOnFailureListener(e -> {
                String message = "Login failed";
                if (e.getMessage() != null) {
                    if (e.getMessage().contains("no user")) {
                        message = "User not found";
                    } else if (e.getMessage().contains("wrong")) {
                        message = "Incorrect password";
                    }
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            });
    }
    private void navigateToHome(String role) {
        String effectiveRole = role != null ? role.trim() : "";
        String selected = selectedRole != null ? selectedRole.trim() : "";
        Intent intent;
        if (effectiveRole.equalsIgnoreCase("admin") || selected.equalsIgnoreCase("admin")) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else {
            intent = new Intent(this, PassengerHomeActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void openSignup() {
        Intent intent = new Intent(this, SignupActivity.class);
        intent.putExtra("role", selectedRole);
        startActivity(intent);
    }
}