package com.example.shipvoyage.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shipvoyage.R;
import com.example.shipvoyage.model.TourInstance;

public class TourInstanceAdapter extends ListAdapter<TourInstance, TourInstanceAdapter.TourInstanceViewHolder> {

    private OnTourInstanceClickListener listener;

    public TourInstanceAdapter(OnTourInstanceClickListener listener) {
        super(new TourInstanceDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public TourInstanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tour_instance, parent, false);
        return new TourInstanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TourInstanceViewHolder holder, int position) {
        TourInstance tourInstance = getItem(position);
        holder.bind(tourInstance, listener);
    }

    public static class TourInstanceViewHolder extends RecyclerView.ViewHolder {
        private TextView tourName;
        private TextView shipName;
        private TextView instanceDates;
        private ImageButton editBtn;
        private ImageButton deleteBtn;

        public TourInstanceViewHolder(@NonNull View itemView) {
            super(itemView);
            tourName = itemView.findViewById(R.id.tourName);
            shipName = itemView.findViewById(R.id.shipName);
            instanceDates = itemView.findViewById(R.id.instanceDates);
            editBtn = itemView.findViewById(R.id.editBtn);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }

        public void bind(TourInstance tourInstance, OnTourInstanceClickListener listener) {
            tourName.setText(tourInstance.getTourName());
            shipName.setText(tourInstance.getShipName());
            instanceDates.setText(tourInstance.getStartDate() + " - " + tourInstance.getEndDate());

            editBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(tourInstance);
                }
            });

            deleteBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(tourInstance);
                }
            });
        }
    }

    private static class TourInstanceDiffCallback extends DiffUtil.ItemCallback<TourInstance> {
        @Override
        public boolean areItemsTheSame(@NonNull TourInstance oldItem, @NonNull TourInstance newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull TourInstance oldItem, @NonNull TourInstance newItem) {
            return oldItem.getId().equals(newItem.getId()) &&
                    oldItem.getTourId().equals(newItem.getTourId()) &&
                    oldItem.getShipId().equals(newItem.getShipId()) &&
                    oldItem.getStartDate().equals(newItem.getStartDate()) &&
                    oldItem.getEndDate().equals(newItem.getEndDate());
        }
    }

    public interface OnTourInstanceClickListener {
        void onEditClick(TourInstance tourInstance);
        void onDeleteClick(TourInstance tourInstance);
    }
}
