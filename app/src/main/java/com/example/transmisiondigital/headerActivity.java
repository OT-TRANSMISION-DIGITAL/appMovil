package com.example.transmisiondigital;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class headerActivity extends AppCompatActivity {

    private ImageButton imageButtonProfile;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_header);

        imageButtonProfile = findViewById(R.id.imageButtonProfile);

        imageButtonProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, CuentaActivity.class);
            startActivity(intent);
            finish();
        });
    }
}