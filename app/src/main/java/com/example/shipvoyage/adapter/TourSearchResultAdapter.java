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
        TextView tourName, tourRoute, departDate, returnDate, duration, shipName;
        Button bookButton;
        ViewHolder(View itemView) {
            super(itemView);
            tourName = itemView.findViewById(R.id.tourName);
            tourRoute = itemView.findViewById(R.id.tourRoute);
            departDate = itemView.findViewById(R.id.departDate);
            returnDate = itemView.findViewById(R.id.returnDate);
            duration = itemView.findViewById(R.id.duration);
            shipName = itemView.findViewById(R.id.shipName);
            bookButton = itemView.findViewById(R.id.bookButton);
        }
        void bind(TourInstance instance, OnTourClickListener listener) {
            tourName.setText(instance.getTourName() != null ? instance.getTourName() : "Tour");
            String routeText = instance.getTourName() != null ? instance.getTourName() : "Tour";
            if (instance.getShipName() != null) {
                routeText = routeText + " · " + instance.getShipName();
            }
            tourRoute.setText(routeText);
            departDate.setText("Depart: " + instance.getStartDate());
            returnDate.setText("Return: " + instance.getEndDate());
            duration.setText("Duration: " + safeDuration(instance.getStartDate(), instance.getEndDate()) + " days");
            shipName.setText("Ship: " + (instance.getShipName() != null ? instance.getShipName() : "N/A"));
            bookButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookClick(instance);
                }
            });
        }
        private int safeDuration(String startDate, String endDate) {
            try {
                java.time.LocalDate start = java.time.LocalDate.parse(startDate);
                java.time.LocalDate end = java.time.LocalDate.parse(endDate);
                return (int) java.time.temporal.ChronoUnit.DAYS.between(start, end);
            } catch (Exception e) {
                return 0;
            }
        }
    }
    static class TourInstanceDiffCallback extends DiffUtil.ItemCallback<TourInstance> {
        @Override
        public boolean areItemsTheSame(@NonNull TourInstance oldItem, @NonNull TourInstance newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }
        @Override
        public boolean areContentsTheSame(@NonNull TourInstance oldItem, @NonNull TourInstance newItem) {
            return safeEq(oldItem.getStartDate(), newItem.getStartDate()) &&
                   safeEq(oldItem.getEndDate(), newItem.getEndDate()) &&
                   safeEq(oldItem.getTourName(), newItem.getTourName()) &&
                   safeEq(oldItem.getShipName(), newItem.getShipName());
        }
        private boolean safeEq(String a, String b) {
            return a == null ? b == null : a.equals(b);
        }
    }
}