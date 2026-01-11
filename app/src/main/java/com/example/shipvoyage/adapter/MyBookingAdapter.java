package com.example.shipvoyage.adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.shipvoyage.R;
import com.example.shipvoyage.model.Booking;
public class MyBookingAdapter extends ListAdapter<Booking, MyBookingAdapter.ViewHolder> {
    private OnCancelClickListener listener;
    public MyBookingAdapter(OnCancelClickListener listener) {
        super(new DiffCallback());
        this.listener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_booking, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = getItem(position);
        holder.bind(booking, listener);
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tourName;
        private TextView routeText;
        private TextView totalAmount;
        private TextView departureDate;
        private TextView returnDate;
        private Button cancelButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tourName = itemView.findViewById(R.id.tourName);
            routeText = itemView.findViewById(R.id.routeText);
            totalAmount = itemView.findViewById(R.id.totalAmount);
            departureDate = itemView.findViewById(R.id.departureDate);
            returnDate = itemView.findViewById(R.id.returnDate);
            cancelButton = itemView.findViewById(R.id.cancelButton);
        }
        public void bind(Booking booking, OnCancelClickListener listener) {
            tourName.setText(booking.getTourName() != null ? booking.getTourName() : "Cruise Booking");
            if (booking.getFromLocation() != null && booking.getToLocation() != null) {
                routeText.setText(booking.getFromLocation() + "  " + booking.getToLocation());
            } else {
                routeText.setText("Route information unavailable");
            }
            totalAmount.setText(String.format("%.0f ", booking.getPrice()));
            if (booking.getDepartureDate() != null) {
                departureDate.setText(booking.getDepartureDate());
            } else {
                departureDate.setText("N/A");
            }
            if (booking.getReturnDate() != null) {
                returnDate.setText(booking.getReturnDate());
            } else {
                returnDate.setText("N/A");
            }
            cancelButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancelClick(booking);
                }
            });
        }
    }
    private static class DiffCallback extends DiffUtil.ItemCallback<Booking> {
        @Override
        public boolean areItemsTheSame(@NonNull Booking oldItem, @NonNull Booking newItem) {
            return oldItem.getId().equals(newItem.getId());
        }
        @Override
        public boolean areContentsTheSame(@NonNull Booking oldItem, @NonNull Booking newItem) {
            return oldItem.getId().equals(newItem.getId()) &&
                    oldItem.getStatus().equals(newItem.getStatus());
        }
    }
    public interface OnCancelClickListener {
        void onCancelClick(Booking booking);
    }
}
