package com.example.transmisiondigital;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.example.transmisiondigital.drawing.DrawingView;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.transmisiondigital.databinding.ActivitySignatureBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SignatureActivity extends AppCompatActivity {

    private DrawingView drawingView;
    private Button buttonCancelar, buttonLimpiar, buttonGuardar;
    private String idOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);
        Intent intent = getIntent();
        idOrder = intent.getStringExtra("idOrder");
        Log.i("SignatureActivity", "idOrder: " + idOrder);
        drawingView = findViewById(R.id.drawingView);

        buttonCancelar = findViewById(R.id.buttonCancelar);
        buttonLimpiar = findViewById(R.id.buttonLimpiar);
        buttonGuardar = findViewById(R.id.buttonGuardar);

        buttonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        buttonLimpiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.clear();
            }
        });

        buttonGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = drawingView.getBitmap();
                try {
                    File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "signatureOrder_"+idOrder+".png");
                    FileOutputStream outputStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();
                    Snackbar.make(v, "Firma guardada en: " + file.getAbsolutePath(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    Log.d("ImageView", "Firma guardada en: " + file.getAbsolutePath());
                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                    Snackbar.make(v, "Error al guardar la firma", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

}