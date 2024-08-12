package com.example.transmisiondigital;

import static com.example.transmisiondigital.globalVariables.Conexion.URL;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.transmisiondigital.services.PusherService;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.transmisiondigital.databinding.ActivitySplashBinding;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;

public class SplashActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        sharedPreferences = getSharedPreferences("sessionUser", Context.MODE_PRIVATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.POST_NOTIFICATIONS"}, REQUEST_CODE_POST_NOTIFICATIONS);
            } else {
                startService(new Intent(this, PusherService.class));
            }
        } else {
            startService(new Intent(this, PusherService.class));
        }
        sharedPreferences();
    }

    public void sharedPreferences() {
        String token = sharedPreferences.getString("token", null);
        if (token == null) {
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }, 3000); // 3 seconds delay
        }

        int userId = sharedPreferences.getInt("idUser", -1);

        if (userId == -1) {
            Log.e("fetchUserImage", "User ID not found in SharedPreferences");
            return;
        }

        String url = URL + "usuarios/" + userId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                String userImage = response.getString("img");
                loadImage(userImage);
                Log.i("fetchUserImage", "User image URL saved: " + userImage);
                Intent intent = new Intent(SplashActivity.this, OrdersActivity.class);
                startActivity(intent);
                finish();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Log.e("fetchUserImage", "Error fetching user image: " + error.getMessage());
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    public void loadImage(String urlImage) {
        ImageRequest imageRequest = new ImageRequest(urlImage, response -> {
            // Save the Bitmap to the device
            try {
                File imageFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "signatureOrder.png");
                FileOutputStream fos = new FileOutputStream(imageFile);
                response.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                Log.i("ImageView", "Image saved to: " + imageFile.getAbsolutePath());
                sharedPreferences.edit().putString("userImage", imageFile.getAbsolutePath()).apply();
            } catch (Exception e) {
                Log.e("ImageView", "Error saving image: " + e.getMessage());
            }
        }, 0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.RGB_565, error -> {
            Log.e("ImageView", "Error fetching image: " + error.getMessage());
        });

        RequestQueue requestQueue = Volley.newRequestQueue(SplashActivity.this);
        requestQueue.add(imageRequest);
    }
}