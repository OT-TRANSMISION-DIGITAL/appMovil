package com.example.transmisiondigital;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.transmisiondigital.globalVariables.Conexion;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class AccountActivity extends AppCompatActivity {

    private Button buttonLogout, buttonGenerateCode;
    private ProgressDialog progressDialog;
    private String URL = Conexion.URL;
    private TextView textViewUserName, textViewUserRol, textViewCode;
    private View bottomLineCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        buttonLogout = findViewById(R.id.buttonLogout);
        buttonGenerateCode = findViewById(R.id.buttonGenerateCode);

        SharedPreferences sharedPreferences = getSharedPreferences("sessionUser", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);
        Integer idUser = sharedPreferences.getInt("idUser", 0);
        String name = sharedPreferences.getString("userName", null);
        String rol = sharedPreferences.getString("rol", null);

        textViewUserName = findViewById(R.id.textViewUserName);
        textViewUserRol = findViewById(R.id.textViewUserRol);
        textViewCode = findViewById(R.id.textViewCode);
        bottomLineCode = findViewById(R.id.bottomLineCode);

        textViewUserName.setText(name);
        textViewUserRol.setText(rol);

        footer();

        buttonLogout.setOnClickListener(v -> {
            progressDialog = new ProgressDialog(AccountActivity.this);
            progressDialog.setMessage("Cerrando sesión...");
            progressDialog.show();
            String url = URL + "logout";

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    // Manejar la respuesta del servidor

                        // Extraer el token del objeto JSON
                        //String token = response.getString("token");

                        //JSONObject usuario = response.getJSONObject("usuario");

                        // Guardar el token de autenticación en SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("sessionUser", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();

                        // Manejar el token según sea necesario
                        //Log.d("Token", "Token obtenido: " + token);
                        progressDialog.dismiss();

                        Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Obtener el mensaje de error de VolleyError
                    String mensajeError = "";

                    // Verificar si hay una respuesta de error
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            // Convertir los datos de la respuesta de error a una cadena
                            mensajeError = new String(error.networkResponse.data, "UTF-8");
                            // Convertir la cadena JSON a un objeto JSONObject
                            JSONObject jsonObject = new JSONObject(mensajeError);
                            // Obtener el mensaje de error del JSONObject
                            mensajeError = jsonObject.getString("msg"); // Cambiado "mensaje" por "msg"
                        } catch (UnsupportedEncodingException | JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // Si no hay datos en la respuesta de error, mostrar un mensaje genérico
                        mensajeError = "net";
                    }

                    // Imprimir el mensaje de error en el registro (Log)
                    Log.e("Error", "Error en la petición: " + mensajeError);
                    progressDialog.dismiss();
                    // Mostrar el mensaje de error en un cuadro de diálogo o Toast
                    Toast.makeText(AccountActivity.this, mensajeError, Toast.LENGTH_SHORT).show();
                }
            }) {
               @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };

            // Añadir la petición a la cola de solicitudes
            RequestQueue queue = Volley.newRequestQueue(AccountActivity.this);
            queue.add(request);
        });

        buttonGenerateCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(AccountActivity.this);
                progressDialog.setMessage("Obteniendo codigo...");
                progressDialog.show();
                String url = URL + "generateCode/" + idUser;

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("response", String.valueOf(response));
                            String code = response.getString("code");
                            textViewCode.setVisibility(View.VISIBLE);
                            bottomLineCode.setVisibility(View.VISIBLE);
                            textViewCode.setText(code);
                            progressDialog.dismiss();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Obtener el mensaje de error de VolleyError
                        String mensajeError = "";

                        // Verificar si hay una respuesta de error
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            try {
                                // Convertir los datos de la respuesta de error a una cadena
                                mensajeError = new String(error.networkResponse.data, "UTF-8");
                                // Convertir la cadena JSON a un objeto JSONObject
                                JSONObject jsonObject = new JSONObject(mensajeError);
                                // Obtener el mensaje de error del JSONObject
                                mensajeError = jsonObject.getString("msg"); // Cambiado "mensaje" por "msg"
                            } catch (UnsupportedEncodingException | JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            // Si no hay datos en la respuesta de error, mostrar un mensaje genérico
                            mensajeError = "net";
                        }

                        // Imprimir el mensaje de error en el registro (Log)
                        Log.e("Error", "Error en la petición: " + mensajeError);
                        progressDialog.dismiss();
                        // Mostrar el mensaje de error en un cuadro de diálogo o Toast
                        Toast.makeText(AccountActivity.this, mensajeError, Toast.LENGTH_SHORT).show();
                    }
                });
                // Añadir la petición a la cola de solicitudes
                RequestQueue queue = Volley.newRequestQueue(AccountActivity.this);
                queue.add(request);
            }
        });
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
            //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        });

        btnOrder.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrdersActivity.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        });

        btnCalendar.setOnClickListener(v -> {

        });

        BtnAccount.setOnClickListener(v -> {
            Intent intent = new Intent(this, AccountActivity.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        });
    }
}