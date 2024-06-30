package com.example.transmisiondigital.includes;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.transmisiondigital.R;

public class footerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_footer);

        SharedPreferences sharedPreferences = getSharedPreferences("sessionUser", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);

        ConstraintLayout visitsLayout = findViewById(R.id.imageButtonVisits);
        ConstraintLayout orderLayout = findViewById(R.id.imageButtonOrders);
        ConstraintLayout calendarLayout = findViewById(R.id.imageButtonCalendar);
        ConstraintLayout accountLayout = findViewById(R.id.imageButtonAccount);

        // Verifica si el token es nulo o vacío
        if (token == null || token.isEmpty()) {

            visitsLayout.setVisibility(View.GONE);
            orderLayout.setVisibility(View.GONE);
            calendarLayout.setVisibility(View.GONE);
            accountLayout.setVisibility(View.GONE);
        } else {

            visitsLayout.setVisibility(View.VISIBLE);
            orderLayout.setVisibility(View.VISIBLE);
            calendarLayout.setVisibility(View.VISIBLE);
            accountLayout.setVisibility(View.VISIBLE);
        }

    }
}