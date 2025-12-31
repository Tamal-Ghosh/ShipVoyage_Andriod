package com.example.shipvoyage.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shipvoyage.R;
import com.example.shipvoyage.model.FeaturedPhoto;

public class FeaturedPhotoAdapter extends ListAdapter<FeaturedPhoto, FeaturedPhotoAdapter.ViewHolder> {

    public FeaturedPhotoAdapter() {
        super(new FeaturedPhotoDiffCallback());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_featured_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FeaturedPhoto photo = getItem(position);
        holder.bind(photo);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView photoImage;
        TextView photoTitle, photoDescription;

        ViewHolder(View itemView) {
            super(itemView);
            photoImage = itemView.findViewById(R.id.photoImage);
            photoTitle = itemView.findViewById(R.id.photoTitle);
            photoDescription = itemView.findViewById(R.id.photoDescription);
        }

        void bind(FeaturedPhoto photo) {
            photoTitle.setText(photo.getTitle() != null ? photo.getTitle() : "Featured Destination");
            photoDescription.setText(photo.getDescription() != null ? photo.getDescription() : "");
            
            // TODO: Load image with Glide or Picasso if imagePath is available
            // For now, just show placeholder background
        }
    }

    static class FeaturedPhotoDiffCallback extends DiffUtil.ItemCallback<FeaturedPhoto> {
        @Override
        public boolean areItemsTheSame(@NonNull FeaturedPhoto oldItem, @NonNull FeaturedPhoto newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull FeaturedPhoto oldItem, @NonNull FeaturedPhoto newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                   oldItem.getDescription().equals(newItem.getDescription());
        }
    }
}
