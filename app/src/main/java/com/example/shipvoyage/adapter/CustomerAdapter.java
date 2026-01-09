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
import com.example.shipvoyage.model.User;
public class CustomerAdapter extends ListAdapter<User, CustomerAdapter.CustomerViewHolder> {
    private OnCustomerClickListener listener;
    public CustomerAdapter(OnCustomerClickListener listener) {
        super(new CustomerDiffCallback());
        this.listener = listener;
    }
    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer, parent, false);
        return new CustomerViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        User customer = getItem(position);
        holder.bind(customer, listener);
    }
    public static class CustomerViewHolder extends RecyclerView.ViewHolder {
        private TextView customerName;
        private TextView customerEmail;
        private TextView customerPhone;
        private TextView customerInstance;
        private TextView customerPayment;
        private ImageButton viewBtn;
        private ImageButton deleteBtn;
        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            customerName = itemView.findViewById(R.id.customerName);
            customerEmail = itemView.findViewById(R.id.customerEmail);
            customerPhone = itemView.findViewById(R.id.customerPhone);
            customerInstance = itemView.findViewById(R.id.customerInstance);
            customerPayment = itemView.findViewById(R.id.customerPayment);
            viewBtn = itemView.findViewById(R.id.viewBtn);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
        public void bind(User customer, OnCustomerClickListener listener) {
            customerName.setText(customer.getName());
            customerEmail.setText(customer.getEmail());
            customerPhone.setText(customer.getPhone());
            customerInstance.setText("Tour Instance: " + (customer.getLastInstance() != null ? customer.getLastInstance() : "N/A"));
            customerPayment.setText("Payment: " + (customer.getPaymentStatus() != null ? customer.getPaymentStatus() : "N/A"));
            viewBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewClick(customer);
                }
            });
            deleteBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(customer);
                }
            });
        }
    }
    private static class CustomerDiffCallback extends DiffUtil.ItemCallback<User> {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getId().equals(newItem.getId());
        }
        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getId().equals(newItem.getId()) &&
                    oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getEmail().equals(newItem.getEmail()) &&
                    oldItem.getPhone().equals(newItem.getPhone());
        }
    }
    public interface OnCustomerClickListener {
        void onViewClick(User customer);
        void onDeleteClick(User customer);
    }
}