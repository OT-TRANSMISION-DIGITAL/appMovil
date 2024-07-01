package com.example.transmisiondigital;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.transmisiondigital.adapters.VisitAdapter;
import com.example.transmisiondigital.includes.footerActivity;
import com.example.transmisiondigital.models.Visits;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VisitsActivity extends AppCompatActivity {
    private footerActivity footer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visits);

        // Encuentra el TextView del header y cambia el título
        TextView textViewTitle = findViewById(R.id.textViewTitle);
        textViewTitle.setText("VISITAS");
        footer();
        //footer = new footerActivity(this);
        init();
    }

    public void init(){
        // Inicializa la lista de visitas
        List<Visits> visitsList = new ArrayList<>();
        visitsList.add(new Visits(date("2024-06-18"), "En proceso", "1234", "10:00", "1"));
        visitsList.add(new Visits(date("2024-06-18"), "En proceso", "1234", "10:00", "2"));
        visitsList.add(new Visits(date("2024-06-18"), "En proceso", "1234", "10:00", "3"));
        visitsList.add(new Visits(date("2024-06-18"), "En proceso", "1234", "10:00", "4"));
        visitsList.add(new Visits(date("2024-06-18"), "En proceso", "1234", "10:00", "5"));
        visitsList.add(new Visits(date("2024-06-18"), "En proceso", "1234", "10:00", "6"));
        visitsList.add(new Visits(date("2024-06-18"), "En proceso", "1234", "10:00", "7"));
        visitsList.add(new Visits(date("2024-06-18"), "En proceso", "1234", "10:00", "8"));
        visitsList.add(new Visits(date("2024-06-18"), "En proceso", "1234", "10:00", "9"));
        visitsList.add(new Visits(date("2024-06-18"), "En proceso", "1234", "10:00", "10"));

        // Encuentra el RecyclerView y configura el adaptador
        RecyclerView recyclerView = findViewById(R.id.recyclerViewVisits);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        VisitAdapter visitsAdapter = new VisitAdapter(visitsList, this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(visitsAdapter);
    }

    // Convierte un string en una fecha
    public Date date(String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = format.parse(dateStr);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
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
            Log.d("footerActivity", "onClick: VisitsActivity");
            Intent intent = new Intent(this, VisitsActivity.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
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