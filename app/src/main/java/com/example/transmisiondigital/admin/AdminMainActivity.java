package com.example.transmisiondigital.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageButton;

import com.example.transmisiondigital.R;

public class AdminMainActivity extends AppCompatActivity {

    private ImageButton imageButtonPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        imageButtonPerfil = findViewById(R.id.imageButtonPerfil);

        /*imageButtonPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainActivity.this, CuentaActivity.class);
            startActivity(intent);
            finish();
        });*/
    }
}