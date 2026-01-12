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
        TextView tripTitle, tripStatus, tripRoute, tripDepart, tripReturn, tripShip, tripFrom, tripTo;
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
            
            // Format route as "From → To"
            String route = "N/A";
            if (instance.getFromLocation() != null && instance.getToLocation() != null) {
                route = instance.getFromLocation() + " → " + instance.getToLocation();
            } else if (instance.getFromLocation() != null) {
                route = instance.getFromLocation();
            } else if (instance.getToLocation() != null) {
                route = instance.getToLocation();
            }
            tripRoute.setText(route);
            
            tripDepart.setText(instance.getStartDate() != null ? instance.getStartDate() : "N/A");
            tripReturn.setText(instance.getEndDate() != null ? instance.getEndDate() : "N/A");
            tripShip.setText(instance.getShipName() != null ? instance.getShipName() : "N/A");
        }
    }
    static class TourInstanceDiffCallback extends DiffUtil.ItemCallback<TourInstance> {
        @Override
        public boolean areItemsTheSame(@NonNull TourInstance oldItem, @NonNull TourInstance newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }
        @Override
        public boolean areContentsTheSame(@NonNull TourInstance oldItem, @NonNull TourInstance newItem) {
            return safeEquals(oldItem.getStartDate(), newItem.getStartDate()) &&
                   safeEquals(oldItem.getEndDate(), newItem.getEndDate());
        }
        private boolean safeEquals(String s1, String s2) {
            if (s1 == null && s2 == null) return true;
            if (s1 == null || s2 == null) return false;
            return s1.equals(s2);
        }
    }
}
