package com.example.shipvoyage.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shipvoyage.R;
import com.example.shipvoyage.model.Ship;
import com.example.shipvoyage.model.Tour;
import com.example.shipvoyage.model.TourInstance;

public class TourSearchResultAdapter extends ListAdapter<TourInstance, TourSearchResultAdapter.ViewHolder> {

    private final OnTourClickListener listener;

    public interface OnTourClickListener {
        void onBookClick(TourInstance instance);
    }

    public TourSearchResultAdapter(OnTourClickListener listener) {
        super(new TourInstanceDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tour_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TourInstance instance = getItem(position);
        holder.bind(instance, listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tourName, tourRoute, departDate, returnDate, duration, shipName, price;
        Button bookButton;

        ViewHolder(View itemView) {
            super(itemView);
            tourName = itemView.findViewById(R.id.tourName);
            tourRoute = itemView.findViewById(R.id.tourRoute);
            departDate = itemView.findViewById(R.id.departDate);
            returnDate = itemView.findViewById(R.id.returnDate);
            duration = itemView.findViewById(R.id.duration);
            shipName = itemView.findViewById(R.id.shipName);
            price = itemView.findViewById(R.id.price);
            bookButton = itemView.findViewById(R.id.bookButton);
        }

        void bind(TourInstance instance, OnTourClickListener listener) {
            tourName.setText(instance.getTourName() != null ? instance.getTourName() : "Tour");
            tourRoute.setText((instance.getTourName() != null ? instance.getTourName() : "Tour"));
            departDate.setText("Depart: " + instance.getStartDate());
            returnDate.setText("Return: " + instance.getEndDate());
            duration.setText("Duration: " + calculateDuration(instance.getStartDate(), instance.getEndDate()) + " days");
            shipName.setText("Ship: " + (instance.getShipName() != null ? instance.getShipName() : "N/A"));
            
            int durationDays = calculateDuration(instance.getStartDate(), instance.getEndDate());
            price.setText("৳" + (durationDays * 100));

            bookButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookClick(instance);
                }
            });
        }

        private int calculateDuration(String startDate, String endDate) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                java.util.Date start = sdf.parse(startDate);
                java.util.Date end = sdf.parse(endDate);
                if (start != null && end != null) {
                    long diff = end.getTime() - start.getTime();
                    return (int) (diff / (1000 * 60 * 60 * 24));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
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
