package com.example.transmisiondigital.includes;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.transmisiondigital.AccountActivity;
import com.example.transmisiondigital.R;

public class headerActivity extends AppCompatActivity {

    private TextView textViewTitle;
    private ImageButton imageButtonProfile;
    @SuppressLint("MissingInflatedId")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_header);

        imageButtonProfile = findViewById(R.id.imageButtonProfile);
        textViewTitle = findViewById(R.id.textViewTitle);
    }
}