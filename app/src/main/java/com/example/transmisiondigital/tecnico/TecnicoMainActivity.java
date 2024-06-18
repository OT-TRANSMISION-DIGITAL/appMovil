package com.example.transmisiondigital.tecnico;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import com.example.transmisiondigital.R;

public class TecnicoMainActivity extends AppCompatActivity {

    private TextView textViewTecnico;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tecnico_main);

        textViewTecnico = findViewById(R.id.textViewTecnico);
        String nombreTecnico = getIntent().getStringExtra("nombreTecnico");

        textViewTecnico.setText("Bienvenido," + nombreTecnico +"!");
    }
}