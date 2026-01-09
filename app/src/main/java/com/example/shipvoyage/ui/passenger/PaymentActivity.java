package com.example.shipvoyage.ui.passenger;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.shipvoyage.R;
import com.example.shipvoyage.dao.BookingDAO;
import com.example.shipvoyage.model.Booking;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
public class PaymentActivity extends AppCompatActivity {
    private Booking booking;
    private String paymentMethod;
    private double totalAmount;
    private BookingDAO bookingDAO;
    private TextView totalAmountText;
    private MaterialCardView visaCard, bkashCard;
    private TextInputEditText cardNumberField, expiryField, cvvField, cardHolderField;
    private TextInputEditText mobileNumberField, bkashPinField;
    private Button payNowButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        booking = (Booking) getIntent().getSerializableExtra("booking");
        paymentMethod = getIntent().getStringExtra("paymentMethod");
        totalAmount = getIntent().getDoubleExtra("totalAmount", 0);
        if (booking == null) {
            Toast.makeText(this, "Invalid booking data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        bookingDAO = new BookingDAO();
        initializeViews();
        setupPaymentForm();
    }
    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        totalAmountText = findViewById(R.id.totalAmountText);
        visaCard = findViewById(R.id.visaCard);
        bkashCard = findViewById(R.id.bkashCard);
        cardNumberField = findViewById(R.id.cardNumberField);
        expiryField = findViewById(R.id.expiryField);
        cvvField = findViewById(R.id.cvvField);
        cardHolderField = findViewById(R.id.cardHolderField);
        mobileNumberField = findViewById(R.id.mobileNumberField);
        bkashPinField = findViewById(R.id.bkashPinField);
        payNowButton = findViewById(R.id.payNowButton);
        payNowButton.setOnClickListener(v -> processPayment());
    }
    private void setupPaymentForm() {
        totalAmountText.setText(String.format("Amount: ৳%.0f", totalAmount));
        if ("Visa".equals(paymentMethod)) {
            visaCard.setVisibility(View.VISIBLE);
            bkashCard.setVisibility(View.GONE);
        } else if ("bKash".equals(paymentMethod)) {
            visaCard.setVisibility(View.GONE);
            bkashCard.setVisibility(View.VISIBLE);
        }
    }
    private void processPayment() {
        if ("Visa".equals(paymentMethod)) {
            if (!validateCardPayment()) {
                return;
            }
        } else if ("bKash".equals(paymentMethod)) {
            if (!validateBkashPayment()) {
                return;
            }
        }
        String bookingId = booking.getId();
        if (bookingId == null) {
            bookingId = bookingDAO.bookingsRef.push().getKey();
            booking.setId(bookingId);
        }
        booking.setStatus("Confirmed");
        bookingDAO.bookingsRef.child(bookingId).setValue(booking)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Payment successful! Your booking is confirmed.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(this, PassengerHomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Payment failed. Please try again!", Toast.LENGTH_SHORT).show();
                });
    }
    private boolean validateCardPayment() {
        String cardNumber = cardNumberField.getText() != null ? cardNumberField.getText().toString().trim() : "";
        String expiry = expiryField.getText() != null ? expiryField.getText().toString().trim() : "";
        String cvv = cvvField.getText() != null ? cvvField.getText().toString().trim() : "";
        String cardHolder = cardHolderField.getText() != null ? cardHolderField.getText().toString().trim() : "";
        if (cardNumber.isEmpty()) {
            Toast.makeText(this, "Please fill all Visa details", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (cardHolder.isEmpty()) {
            Toast.makeText(this, "Please fill all Visa details", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (expiry.isEmpty()) {
            Toast.makeText(this, "Please fill all Visa details", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (cvv.isEmpty()) {
            Toast.makeText(this, "Please fill all Visa details", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private boolean validateBkashPayment() {
        String mobileNumber = mobileNumberField.getText() != null ? mobileNumberField.getText().toString().trim() : "";
        String pin = bkashPinField.getText() != null ? bkashPinField.getText().toString().trim() : "";
        if (mobileNumber.isEmpty() || pin.isEmpty()) {
            Toast.makeText(this, "Please fill bKash details", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}