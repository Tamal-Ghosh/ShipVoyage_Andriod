package com.example.shipvoyage.adapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.net.HttpURLConnection;
import java.net.URL;
import com.example.shipvoyage.util.ThreadPool;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.shipvoyage.R;
import com.example.shipvoyage.model.FeaturedPhoto;
public class FeaturedPhotoAdapter extends ListAdapter<FeaturedPhoto, FeaturedPhotoAdapter.ViewHolder> {
    @FunctionalInterface
    public interface OnEditClickListener {
        void onEdit(FeaturedPhoto photo);
    }
    @FunctionalInterface
    public interface OnDeleteClickListener {
        void onDelete(FeaturedPhoto photo);
    }
    private final OnEditClickListener editListener;
    private final OnDeleteClickListener deleteListener;
    public FeaturedPhotoAdapter(OnEditClickListener editListener, OnDeleteClickListener deleteListener) {
        super(new FeaturedPhotoDiffCallback());
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }
    public FeaturedPhotoAdapter() {
        super(new FeaturedPhotoDiffCallback());
        this.editListener = null;
        this.deleteListener = null;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_featured_photo, parent, false);
        return new ViewHolder(view, editListener, deleteListener);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FeaturedPhoto photo = getItem(position);
        holder.bind(photo);
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView photoImage;
        TextView photoTitle, photoDescription;
        Button editButton, deleteButton;
        private FeaturedPhoto currentPhoto;
        private final boolean showEdit;
        private final boolean showDelete;
        ViewHolder(View itemView, OnEditClickListener editListener, OnDeleteClickListener deleteListener) {
            super(itemView);
            photoImage = itemView.findViewById(R.id.photoImage);
            photoTitle = itemView.findViewById(R.id.photoTitle);
            photoDescription = itemView.findViewById(R.id.photoDescription);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            showEdit = editListener != null && editButton != null;
            showDelete = deleteListener != null && deleteButton != null;
            if (showEdit) {
                editButton.setOnClickListener(v -> {
                    if (currentPhoto != null) {
                        editListener.onEdit(currentPhoto);
                    }
                });
            } else if (editButton != null) {
                editButton.setVisibility(View.GONE);
            }
            if (showDelete) {
                deleteButton.setOnClickListener(v -> {
                    if (currentPhoto != null) {
                        deleteListener.onDelete(currentPhoto);
                    }
                });
            } else if (deleteButton != null) {
                deleteButton.setVisibility(View.GONE);
            }
        }
        void bind(FeaturedPhoto photo) {
            this.currentPhoto = photo;
            photoTitle.setText(photo.getTitle() != null ? photo.getTitle() : "Featured Destination");
            photoDescription.setText(photo.getDescription() != null ? photo.getDescription() : "");
            if (photoImage != null) {
                if (photo.getImagePath() != null && !photo.getImagePath().isEmpty()) {
                    loadImage(photo.getImagePath());
                } else {
                    photoImage.setImageResource(android.R.color.darker_gray);
                }
            }
        }
        private void loadImage(String path) {
            try {
                Uri uri = Uri.parse(path);
                String scheme = uri.getScheme();
                if (scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                    ThreadPool.getExecutor().execute(() -> {
                        Bitmap bmp = fetchBitmap(path);
                        if (bmp != null) {
                            new Handler(Looper.getMainLooper()).post(() -> photoImage.setImageBitmap(bmp));
                        } else {
                            new Handler(Looper.getMainLooper()).post(() -> photoImage.setImageResource(android.R.color.darker_gray));
                        }
                    });
                } else {
                    photoImage.setImageURI(uri);
                }
            } catch (Exception e) {
                photoImage.setImageResource(android.R.color.darker_gray);
            }
        }
        private Bitmap fetchBitmap(String urlString) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(4000);
                connection.setReadTimeout(6000);
                connection.setDoInput(true);
                connection.connect();
                try (var stream = connection.getInputStream()) {
                    return BitmapFactory.decodeStream(stream);
                }
            } catch (Exception ignored) {
                return null;
            } finally {
                if (connection != null) connection.disconnect();
            }
        }
    }
    static class FeaturedPhotoDiffCallback extends DiffUtil.ItemCallback<FeaturedPhoto> {
        @Override
        public boolean areItemsTheSame(@NonNull FeaturedPhoto oldItem, @NonNull FeaturedPhoto newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }
        @Override
        public boolean areContentsTheSame(@NonNull FeaturedPhoto oldItem, @NonNull FeaturedPhoto newItem) {
            return (oldItem.getTitle() != null ? oldItem.getTitle().equals(newItem.getTitle()) : newItem.getTitle() == null) &&
                   (oldItem.getDescription() != null ? oldItem.getDescription().equals(newItem.getDescription()) : newItem.getDescription() == null);
        }
    }
}