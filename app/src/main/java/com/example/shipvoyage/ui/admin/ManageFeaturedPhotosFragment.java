package com.example.shipvoyage.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shipvoyage.R;
import com.example.shipvoyage.adapter.FeaturedPhotoAdapter;
import com.example.shipvoyage.dao.FeaturedPhotoDAO;
import com.example.shipvoyage.model.FeaturedPhoto;
import com.example.shipvoyage.util.ThreadPool;

import java.util.ArrayList;
import java.util.List;

public class ManageFeaturedPhotosFragment extends Fragment {
    private RecyclerView photosRecyclerView;
    private EditText titleField, descriptionField, imagePathField, searchField;
    private Button saveBtn, cancelBtn, searchBtn;
    private FeaturedPhotoDAO photoDAO;
    private List<FeaturedPhoto> photosList = new ArrayList<>();
    private FeaturedPhotoAdapter photoAdapter;
    private String editingPhotoId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_featured_photos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        photoDAO = new FeaturedPhotoDAO();
        initViews(view);
        setupListeners();
        loadPhotos();
    }

    private void initViews(View view) {
        photosRecyclerView = view.findViewById(R.id.photosRecyclerView);
        titleField = view.findViewById(R.id.titleField);
        descriptionField = view.findViewById(R.id.descriptionField);
        imagePathField = view.findViewById(R.id.imagePathField);
        searchField = view.findViewById(R.id.searchField);
        saveBtn = view.findViewById(R.id.saveBtn);
        cancelBtn = view.findViewById(R.id.cancelBtn);
        searchBtn = view.findViewById(R.id.searchBtn);

        photosRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        photoAdapter = new FeaturedPhotoAdapter(
            photo -> {
                editingPhotoId = photo.getId();
                titleField.setText(photo.getTitle());
                descriptionField.setText(photo.getDescription());
                imagePathField.setText(photo.getImagePath());
            },
            photo -> {
                photoDAO.deletePhoto(photo.getId()).addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "Photo deleted", Toast.LENGTH_SHORT).show();
                    loadPhotos();
                }).addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to delete photo", Toast.LENGTH_SHORT).show();
                });
            }
        );
        photosRecyclerView.setAdapter(photoAdapter);
    }

    private void setupListeners() {
        saveBtn.setOnClickListener(v -> savePhoto());
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

    private void loadPhotos() {
        photoDAO.getAllPhotos().addOnSuccessListener(dataSnapshot -> {
            ThreadPool.getExecutor().execute(() -> {
                List<FeaturedPhoto> newPhotos = new ArrayList<>();
                for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FeaturedPhoto photo = snapshot.getValue(FeaturedPhoto.class);
                    if (photo != null) {
                        newPhotos.add(photo);
                    }
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        photosList.clear();
                        photosList.addAll(newPhotos);
                        updateRecyclerView();
                    });
                }
            });
        }).addOnFailureListener(e -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Failed to load photos", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void savePhoto() {
        String title = titleField.getText().toString().trim();
        String description = descriptionField.getText().toString().trim();
        String imagePath = imagePathField.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || imagePath.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        for (FeaturedPhoto existingPhoto : photosList) {
            if (existingPhoto.getTitle().equalsIgnoreCase(title) &&
                (editingPhotoId == null || !existingPhoto.getId().equals(editingPhotoId))) {
                Toast.makeText(requireContext(), "Photo title already exists", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String photoId = editingPhotoId != null ? editingPhotoId : photoDAO.photosRef.push().getKey();
        if (photoId != null) {
            FeaturedPhoto photo = new FeaturedPhoto(photoId, title, description, imagePath);
            photoDAO.addPhoto(photo).addOnSuccessListener(unused -> {
                Toast.makeText(requireContext(), editingPhotoId != null ? "Photo updated" : "Photo saved", Toast.LENGTH_SHORT).show();
                clearForm();
                loadPhotos();
            }).addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Failed to save photo", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void clearForm() {
        editingPhotoId = null;
        titleField.setText("");
        descriptionField.setText("");
        imagePathField.setText("");
    }

    private void performSearch() {
        String query = searchField.getText().toString().trim().toLowerCase();
        if (query.isEmpty()) {
            photoAdapter.submitList(new ArrayList<>(photosList));
            return;
        }

        List<FeaturedPhoto> filteredPhotos = new ArrayList<>();
        for (FeaturedPhoto photo : photosList) {
            if (photo.getTitle().toLowerCase().contains(query) ||
                photo.getDescription().toLowerCase().contains(query)) {
                filteredPhotos.add(photo);
            }
        }
        photoAdapter.submitList(new ArrayList<>(filteredPhotos));
    }

    private void updateRecyclerView() {
        photoAdapter.submitList(new ArrayList<>(photosList));
    }
}
