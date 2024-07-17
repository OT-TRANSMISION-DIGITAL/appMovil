package com.example.transmisiondigital;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.transmisiondigital.globalVariables.Conexion;
import com.example.transmisiondigital.tecnico.TecnicoMainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText editTextPassword, editTextEmail;
    private Button btnLogin;
    private ProgressDialog progressDialog;
    private String URL = Conexion.URL;
    private String rol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(getSharedPreferences("sessionUser", Context.MODE_PRIVATE).contains("token")){
            Intent intent = new Intent(MainActivity.this, OrdersActivity.class);
            startActivity(intent);
            finish();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLogin = findViewById(R.id.btnLogin);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextPassword == null || editTextEmail == null ||
                        editTextPassword.getText().toString().isEmpty() ||
                        editTextEmail.getText().toString().isEmpty()) {
                    // Mostrar alerta
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Campos vacíos");
                    builder.setMessage("Por favor, complete todos los campos.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    return;
                }
                // Crear un ProgressDialog
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("Cargando..."); // Establecer el mensaje que se mostrará
                progressDialog.setCancelable(false); // Evitar que el usuario cancele el ProgressDialog
                progressDialog.show(); // Mostrar el ProgressDialog

                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();

                // Construir el objeto JSON con los datos
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("correo", email);
                    jsonBody.put("password", password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Hacer la petición POST
                String url = URL + "login";
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Manejar la respuesta del servidor
                        try {
                            if (response.has("token")) {
                                // Extraer el token del objeto JSON
                                String token = response.getString("token");

                                // Extraer el objeto "usuario" del objeto JSON
                                JSONObject usuario = response.getJSONObject("usuario");
                                // Extraer el nombre del objeto "usuario"
                                Integer idUser = usuario.getInt("id");
                                String userName = usuario.getString("nombre");
                                Integer idRol = usuario.getInt("rol_id");
                                if(idRol == 1){
                                    rol = "Administrador";
                                }
                                if(idRol == 3){
                                    rol = "Técnico";
                                }

                                // Guardar el token de autenticación en SharedPreferences
                                SharedPreferences sharedPreferences = getSharedPreferences("sessionUser", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("token", token);
                                editor.putString("userName", userName);
                                editor.putInt("idUser", idUser);
                                editor.putInt("idRol", idRol);
                                editor.putString("rol", rol);
                                editor.apply();
                                progressDialog.dismiss();

                                Intent intent = new Intent(MainActivity.this, OrdersActivity.class);
                                intent.putExtra("userName", userName);
                                startActivity(intent);
                                finish();
                            } else {
                                progressDialog.dismiss();

                                String rutaFirmada = response.getString("rutaFirmada");
                                Intent intent = new Intent(MainActivity.this, VerificarCodigoActivity.class);
                                intent.putExtra("signedUrl", rutaFirmada);
                                startActivity(intent);
                                finish();
                            }

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
                                String responseData = new String(error.networkResponse.data, "UTF-8");
                                // Convertir la cadena JSON a un objeto JSONObject
                                JSONObject jsonObject = new JSONObject(responseData);
                                // Obtener el mensaje de error del JSONObject
                                mensajeError = jsonObject.optString("msg", "Error en la petición");
                            } catch (UnsupportedEncodingException | JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            // Si no hay datos en la respuesta de error, mostrar un mensaje genérico
                            mensajeError = "Error de red";
                        }

                        // Imprimir el mensaje de error en el registro (Log)
                        Log.e("Error", "Error en la petición: " + mensajeError);
                        progressDialog.dismiss();
                        // Mostrar el mensaje de error en un cuadro de diálogo o Toast
                        Toast.makeText(MainActivity.this, mensajeError, Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        // Establecer el encabezado Content-Type como application/json
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        return headers;
                    }
                };

                // Añadir la petición a la cola de solicitudes
                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                queue.add(request);
            }
        });
    }
}