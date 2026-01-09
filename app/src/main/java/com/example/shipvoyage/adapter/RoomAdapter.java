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
import com.example.shipvoyage.model.Room;
public class RoomAdapter extends ListAdapter<Room, RoomAdapter.RoomViewHolder> {
    private OnRoomActionListener listener;
    public interface OnRoomActionListener {
        void onEdit(Room room);
        void onDelete(Room room);
    }
    public RoomAdapter(OnRoomActionListener listener) {
        super(new DiffUtil.ItemCallback<Room>() {
            @Override
            public boolean areItemsTheSame(@NonNull Room oldItem, @NonNull Room newItem) {
                return oldItem.getId().equals(newItem.getId());
            }
            @Override
            public boolean areContentsTheSame(@NonNull Room oldItem, @NonNull Room newItem) {
                return oldItem.getId().equals(newItem.getId()) &&
                        oldItem.getRoomNumber().equals(newItem.getRoomNumber()) &&
                        oldItem.getType().equals(newItem.getType()) &&
                        oldItem.getPrice() == newItem.getPrice() &&
                        oldItem.isAvailability() == newItem.isAvailability();
            }
        });
        this.listener = listener;
    }
    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = getItem(position);
        holder.bind(room, listener);
    }
    static class RoomViewHolder extends RecyclerView.ViewHolder {
        private final TextView roomNumber;
        private final TextView roomType;
        private final TextView roomPrice;
        private final ImageButton editButton;
        private final ImageButton deleteButton;
        RoomViewHolder(View itemView) {
            super(itemView);
            roomNumber = itemView.findViewById(R.id.roomNumber);
            roomType = itemView.findViewById(R.id.roomType);
            roomPrice = itemView.findViewById(R.id.roomPrice);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
        void bind(Room room, OnRoomActionListener listener) {
            roomNumber.setText(room.getRoomNumber());
            roomType.setText(room.getType());
            roomPrice.setText(String.format("৳%.0f", room.getPrice()));
            editButton.setOnClickListener(v -> listener.onEdit(room));
            deleteButton.setOnClickListener(v -> listener.onDelete(room));
        }
    }
}