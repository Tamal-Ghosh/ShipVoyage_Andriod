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
import com.example.shipvoyage.model.Ship;
public class ShipAdapter extends ListAdapter<Ship, ShipAdapter.ShipViewHolder> {
    private OnShipClickListener listener;
    public ShipAdapter(OnShipClickListener listener) {
        super(new ShipDiffCallback());
        this.listener = listener;
    }
    @NonNull
    @Override
    public ShipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ship, parent, false);
        return new ShipViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ShipViewHolder holder, int position) {
        Ship ship = getItem(position);
        holder.bind(ship, listener);
    }
    public static class ShipViewHolder extends RecyclerView.ViewHolder {
        private TextView shipName;
        private TextView shipCapacity;
        private ImageButton editBtn;
        private ImageButton deleteBtn;
        public ShipViewHolder(@NonNull View itemView) {
            super(itemView);
            shipName = itemView.findViewById(R.id.shipName);
            shipCapacity = itemView.findViewById(R.id.shipCapacity);
            editBtn = itemView.findViewById(R.id.editBtn);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
        public void bind(Ship ship, OnShipClickListener listener) {
            shipName.setText(ship.getName());
            shipCapacity.setText("Capacity: " + ship.getCapacity());
            editBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(ship);
                }
            });
            deleteBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(ship);
                }
            });
        }
    }
    private static class ShipDiffCallback extends DiffUtil.ItemCallback<Ship> {
        @Override
        public boolean areItemsTheSame(@NonNull Ship oldItem, @NonNull Ship newItem) {
            return oldItem.getId().equals(newItem.getId());
        }
        @Override
        public boolean areContentsTheSame(@NonNull Ship oldItem, @NonNull Ship newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getCapacity() == newItem.getCapacity();
        }
    }
    public interface OnShipClickListener {
        void onEditClick(Ship ship);
        void onDeleteClick(Ship ship);
    }
}