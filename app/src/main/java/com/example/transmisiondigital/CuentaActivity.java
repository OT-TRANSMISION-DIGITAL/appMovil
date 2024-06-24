package com.example.transmisiondigital;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.transmisiondigital.admin.AdminMainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class CuentaActivity extends AppCompatActivity {

    private Button btnCerrarSesion;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuenta);

        //btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        SharedPreferences sharedPreferences = getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);
        Integer idTecnico = sharedPreferences.getInt("idTecnico", 0);

        btnCerrarSesion.setOnClickListener(v -> {
            String url = "http://192.168.137.98:8000/api/logout/" + idTecnico;
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    // Manejar la respuesta del servidor
                    try {
                        // Extraer el token del objeto JSON
                        String token = response.getString("token");

                        JSONObject usuario = response.getJSONObject("usuario");

                        // Guardar el token de autenticación en SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove("token");
                        editor.remove("idTecnico");
                        editor.apply();

                        // Manejar el token según sea necesario
                        Log.d("Token", "Token obtenido: " + token);
                        progressDialog.dismiss();

                        Intent intent = new Intent(CuentaActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();

                    } catch (JSONException e) {
                        e.printStackTrace();
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
                    Toast.makeText(CuentaActivity.this, mensajeError, Toast.LENGTH_SHORT).show();
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
            RequestQueue queue = Volley.newRequestQueue(CuentaActivity.this);
            queue.add(request);
        });
    }
}