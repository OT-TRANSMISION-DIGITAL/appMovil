package com.example.transmisiondigital;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.transmisiondigital.adapters.OrderAdapter;
import com.example.transmisiondigital.models.Orders;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {

    List<Orders> ordersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        init();
    }

    public void init() {
       ordersList = new ArrayList<>();
       ordersList.add(new Orders(date("2024-06-18"), "En proceso", "1234", "10:00", "1"));
       ordersList.add(new Orders(date("2024-06-18"), "En proceso", "1234", "10:00", "2"));
       ordersList.add(new Orders(date("2024-06-18"), "En proceso", "1234", "10:00", "3"));
       ordersList.add(new Orders(date("2024-06-18"), "En proceso", "1234", "10:00", "4"));
       ordersList.add(new Orders(date("2024-06-18"), "En proceso", "1234", "10:00", "5"));
       ordersList.add(new Orders(date("2024-06-18"), "En proceso", "1234", "10:00", "6"));
       ordersList.add(new Orders(date("2024-06-18"), "En proceso", "1234", "10:00", "7"));
       ordersList.add(new Orders(date("2024-06-18"), "En proceso", "1234", "10:00", "8"));
       ordersList.add(new Orders(date("2024-06-18"), "En proceso", "1234", "10:00", "9"));
       ordersList.add(new Orders(date("2024-06-18"), "En proceso", "1234", "10:00", "10"));


       RecyclerView recyclerView = findViewById(R.id.recyclerViewOrders);
       recyclerView.setLayoutManager(new LinearLayoutManager(this));
       OrderAdapter orderAdapter = new OrderAdapter(ordersList, this);
       recyclerView.setHasFixedSize(true);
       recyclerView.setAdapter(orderAdapter);

    }

    public Date date(String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = format.parse(dateStr);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}