package com.example.shipvoyage.ui.admin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shipvoyage.R;
import com.example.shipvoyage.adapter.FeaturedPhotoAdapter;
import com.example.shipvoyage.dao.FeaturedPhotoDAO;
import com.example.shipvoyage.model.FeaturedPhoto;
import com.example.shipvoyage.util.ThreadPool;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageFeaturedPhotosActivity extends AppCompatActivity {

    private EditText titleField, descriptionField;
    private ImageView preview;
    private Button browseButton, saveButton, resetButton;
    private RecyclerView photosRecyclerView;
    private FeaturedPhotoAdapter adapter;
    private FeaturedPhotoDAO photoDAO;

    private String editingPhotoId = null;
    private String selectedImagePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_featured_photos);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.featuredPhotoRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        titleField = findViewById(R.id.titleField);
        descriptionField = findViewById(R.id.descriptionField);
        preview = findViewById(R.id.preview);
        browseButton = findViewById(R.id.browseButton);
        saveButton = findViewById(R.id.saveButton);
        resetButton = findViewById(R.id.resetButton);
        photosRecyclerView = findViewById(R.id.photosRecyclerView);

        photoDAO = new FeaturedPhotoDAO();

        photosRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        adapter = new FeaturedPhotoAdapter((photo) -> {
            editingPhotoId = photo.getId();
            titleField.setText(photo.getTitle());
            descriptionField.setText(photo.getDescription());
            selectedImagePath = photo.getImagePath();
        }, (photo) -> {
            photoDAO.deletePhoto(photo.getId()).addOnSuccessListener(unused -> {
                Toast.makeText(this, "Photo deleted", Toast.LENGTH_SHORT).show();
                loadPhotos();
            }).addOnFailureListener(e ->
                    Toast.makeText(this, "Failed to delete photo", Toast.LENGTH_SHORT).show());
        });
        photosRecyclerView.setAdapter(adapter);

        browseButton.setOnClickListener(v -> openImagePicker());
        saveButton.setOnClickListener(v -> onSave());
        resetButton.setOnClickListener(v -> onReset());

        loadPhotos();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                selectedImagePath = imageUri.toString();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    preview.setImageBitmap(bitmap);
                } catch (Exception e) {
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void onSave() {
        String title = titleField.getText().toString().trim();
        String description = descriptionField.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        FeaturedPhoto photo = new FeaturedPhoto(
                editingPhotoId,
                title,
                description,
                selectedImagePath != null ? selectedImagePath : ""
        );

        if (editingPhotoId == null) {
            photoDAO.addPhoto(photo).addOnSuccessListener(unused -> {
                Toast.makeText(this, "Photo added", Toast.LENGTH_SHORT).show();
                loadPhotos();
                onReset();
            }).addOnFailureListener(e ->
                    Toast.makeText(this, "Failed to add photo", Toast.LENGTH_SHORT).show());
        } else {
            photoDAO.updatePhoto(editingPhotoId, photo).addOnSuccessListener(unused -> {
                Toast.makeText(this, "Photo updated", Toast.LENGTH_SHORT).show();
                loadPhotos();
                onReset();
            }).addOnFailureListener(e ->
                    Toast.makeText(this, "Failed to update photo", Toast.LENGTH_SHORT).show());
        }
    }

    private void onReset() {
        titleField.setText("");
        descriptionField.setText("");
        preview.setImageResource(android.R.color.transparent);
        selectedImagePath = null;
        editingPhotoId = null;
    }

    private void loadPhotos() {
        ThreadPool.getExecutor().execute(() -> {
            photoDAO.getAllPhotos().addOnSuccessListener(dataSnapshot -> {
                List<FeaturedPhoto> photos = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FeaturedPhoto photo = snapshot.getValue(FeaturedPhoto.class);
                    if (photo != null) {
                        photos.add(photo);
                    }
                }
                runOnUiThread(() -> adapter.submitList(photos));
            });
        });
    }
}
