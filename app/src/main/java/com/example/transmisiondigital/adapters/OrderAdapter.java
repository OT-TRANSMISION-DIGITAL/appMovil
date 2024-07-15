package com.example.transmisiondigital.adapters;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.transmisiondigital.OrderActivity;
import com.example.transmisiondigital.R;
import com.example.transmisiondigital.models.Orders;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Orders> ordersList;
    private Context context;

    public OrderAdapter(List<Orders> ordersList, Context context) {
        this.ordersList = ordersList;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_element_orders, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Orders order = ordersList.get(position);
        holder.tvFolio.setText(order.getFolio());
        holder.tvHour.setText(order.getHour());
        holder.tvStatus.setText(order.getStatus());
        holder.tvDate.setText(order.getDate().toString());

        holder.btnDetails.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), OrderActivity.class);
            // Aquí cambiamos tvFolio.getText().toString() por order.getIdOrder()
            intent.putExtra("idOrder", order.getIdOrder());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return ordersList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        Button btnDetails;
        TextView tvFolio, tvHour, tvStatus, tvDate;

    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);
        tvFolio = itemView.findViewById(R.id.textViewFolio);
        tvHour = itemView.findViewById(R.id.textViewHour);
        tvStatus = itemView.findViewById(R.id.textViewStatus);
        tvDate = itemView.findViewById(R.id.textViewHour);
        btnDetails = itemView.findViewById(R.id.btnDetails);
    }

        void bindData(final Orders order) {
            tvFolio.setText(order.getFolio());
            tvHour.setText(order.getHour());
            tvStatus.setText(order.getStatus());
            tvDate.setText(order.getDate().toString());
        }
    }
}
