package com.example.transmisiondigital;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.transmisiondigital.adapters.OrderAdapter;
import com.example.transmisiondigital.includes.footerActivity;
import com.example.transmisiondigital.models.Orders;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {
    private footerActivity footer;
    List<Orders> ordersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        //footer = new footerActivity();
        footer();
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

    public void footer() {
        SharedPreferences sharedPreferences = getSharedPreferences("sessionUser", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);

        ConstraintLayout btnVisits = findViewById(R.id.imageButtonVisits);
        ConstraintLayout btnOrder = findViewById(R.id.imageButtonOrders);
        ConstraintLayout btnCalendar = findViewById(R.id.imageButtonCalendar);
        ConstraintLayout BtnAccount = findViewById(R.id.imageButtonAccount);

        // Verifica si el token es nulo o vacío
        if (token == null || token.isEmpty()) {
            btnVisits.setVisibility(View.GONE);
            btnOrder.setVisibility(View.GONE);
            btnCalendar.setVisibility(View.GONE);
            BtnAccount.setVisibility(View.GONE);
        } else {
            btnVisits.setVisibility(View.VISIBLE);
            btnOrder.setVisibility(View.VISIBLE);
            btnCalendar.setVisibility(View.VISIBLE);
            BtnAccount.setVisibility(View.VISIBLE);
        }

        btnVisits.setOnClickListener(v -> {
            Intent intent = new Intent(this, VisitsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        });

        btnOrder.setOnClickListener(v -> {
            Log.d("footerActivity", "onClick: OrdersActivity");
            Intent intent = new Intent(this, OrdersActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        });

        btnCalendar.setOnClickListener(v -> {

        });

        BtnAccount.setOnClickListener(v -> {
            Intent intent = new Intent(this, AccountActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
    }
}