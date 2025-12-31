package com.example.shipvoyage.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shipvoyage.R;
import com.example.shipvoyage.model.TourInstance;

public class UpcomingTripAdapter extends ListAdapter<TourInstance, UpcomingTripAdapter.ViewHolder> {

    public UpcomingTripAdapter() {
        super(new TourInstanceDiffCallback());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_upcoming_trip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TourInstance instance = getItem(position);
        holder.bind(instance);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tripTitle, tripStatus, tripRoute, tripDepart, tripReturn, tripShip;

        ViewHolder(View itemView) {
            super(itemView);
            tripTitle = itemView.findViewById(R.id.tripTitle);
            tripStatus = itemView.findViewById(R.id.tripStatus);
            tripRoute = itemView.findViewById(R.id.tripRoute);
            tripDepart = itemView.findViewById(R.id.tripDepart);
            tripReturn = itemView.findViewById(R.id.tripReturn);
            tripShip = itemView.findViewById(R.id.tripShip);
        }

        void bind(TourInstance instance) {
            tripTitle.setText(instance.getTourName() != null ? instance.getTourName() : "Tour");
            tripStatus.setText("Upcoming");
            tripRoute.setText(instance.getTourName() != null ? instance.getTourName() : "Tour");
            tripDepart.setText("Depart: " + instance.getStartDate());
            tripReturn.setText("Return: " + instance.getEndDate());
            tripShip.setText("Ship: " + (instance.getShipName() != null ? instance.getShipName() : "N/A"));
        }
    }

    static class TourInstanceDiffCallback extends DiffUtil.ItemCallback<TourInstance> {
        @Override
        public boolean areItemsTheSame(@NonNull TourInstance oldItem, @NonNull TourInstance newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull TourInstance oldItem, @NonNull TourInstance newItem) {
            return oldItem.getStartDate().equals(newItem.getStartDate()) &&
                   oldItem.getEndDate().equals(newItem.getEndDate());
        }
    }
}
