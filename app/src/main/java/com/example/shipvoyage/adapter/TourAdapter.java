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
import com.example.shipvoyage.model.Tour;
public class TourAdapter extends ListAdapter<Tour, TourAdapter.TourViewHolder> {
    private OnTourClickListener listener;
    public TourAdapter(OnTourClickListener listener) {
        super(new TourDiffCallback());
        this.listener = listener;
    }
    @NonNull
    @Override
    public TourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tour, parent, false);
        return new TourViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull TourViewHolder holder, int position) {
        Tour tour = getItem(position);
        holder.bind(tour, listener);
    }
    public static class TourViewHolder extends RecyclerView.ViewHolder {
        private TextView tourName;
        private TextView tourRoute;
        private TextView tourDescription;
        private ImageButton editBtn;
        private ImageButton deleteBtn;
        public TourViewHolder(@NonNull View itemView) {
            super(itemView);
            tourName = itemView.findViewById(R.id.tourName);
            tourRoute = itemView.findViewById(R.id.tourRoute);
            tourDescription = itemView.findViewById(R.id.tourDescription);
            editBtn = itemView.findViewById(R.id.editBtn);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
        public void bind(Tour tour, OnTourClickListener listener) {
            tourName.setText(tour.getName());
            tourRoute.setText(tour.getFrom() + " → " + tour.getTo());
            tourDescription.setText(tour.getDescription());
            editBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(tour);
                }
            });
            deleteBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(tour);
                }
            });
        }
    }
    private static class TourDiffCallback extends DiffUtil.ItemCallback<Tour> {
        @Override
        public boolean areItemsTheSame(@NonNull Tour oldItem, @NonNull Tour newItem) {
            return oldItem.getId().equals(newItem.getId());
        }
        @Override
        public boolean areContentsTheSame(@NonNull Tour oldItem, @NonNull Tour newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getFrom().equals(newItem.getFrom()) &&
                    oldItem.getTo().equals(newItem.getTo()) &&
                    oldItem.getDescription().equals(newItem.getDescription());
        }
    }
    public interface OnTourClickListener {
        void onEditClick(Tour tour);
        void onDeleteClick(Tour tour);
    }
}
