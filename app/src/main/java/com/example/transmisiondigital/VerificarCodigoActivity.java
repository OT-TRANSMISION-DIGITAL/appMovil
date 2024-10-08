package com.example.transmisiondigital;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class VerificarCodigoActivity extends AppCompatActivity {

    private EditText editTextCodigo;
    private Button btnVerificarCodigo;
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferences;
    private String rol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verificar_codigo);

        Intent intent = getIntent();
        String signedUrl = intent.getStringExtra("signedUrl");
        sharedPreferences = getSharedPreferences("sessionUser", Context.MODE_PRIVATE);

        editTextCodigo = findViewById(R.id.editTextCodigo);
        btnVerificarCodigo = findViewById(R.id.btnVerificarCodigo);

        btnVerificarCodigo.setOnClickListener(v -> {
            String codigo = editTextCodigo.getText().toString();
            if (codigo.isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(VerificarCodigoActivity.this);
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

            progressDialog = new ProgressDialog(VerificarCodigoActivity.this);
            progressDialog.setMessage("Cargando..."); // Establecer el mensaje que se mostrará
            progressDialog.setCancelable(false); // Evitar que el usuario cancele el ProgressDialog
            progressDialog.show(); // Mostrar el ProgressDialog

            //String email = editTextEmail.getText().toString();;

            // Construir el objeto JSON con los datos
            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("codigo", codigo);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, signedUrl, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    // Manejar la respuesta del servidor
                    try {
                        // Extraer el token del objeto JSON
                        String token = response.getString("token");

                        JSONObject user = response.getJSONObject("usuario");
                        Integer idUser = user.getInt("id");
                        String userName = user.getString("nombre");
                        Integer idRol = user.getInt("rol_id");
                        if (idRol == 1) {
                            rol = "Administrador";
                        }
                        if (idRol == 3) {
                            rol = "Técnico";
                        }
                        String userImage = user.optString("img", "");

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("token", token);
                        editor.putString("userName", userName);
                        editor.putInt("idUser", idUser);
                        editor.putInt("idRol", idRol);
                        editor.putString("rol", rol);
                        Log.i("Login", "Rol saved: " + rol);
                        if (!userImage.isEmpty()) {
                            loadImage(userImage);
                        } else {
                            editor.putString("userImage", "");
                        }
                        editor.apply();
                        progressDialog.dismiss();

                        Intent intent = new Intent(VerificarCodigoActivity.this, OrdersActivity.class);
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
                    Toast.makeText(VerificarCodigoActivity.this, mensajeError, Toast.LENGTH_SHORT).show();
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
            RequestQueue queue = Volley.newRequestQueue(VerificarCodigoActivity.this);
            queue.add(request);
        });
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

        RequestQueue requestQueue = Volley.newRequestQueue(VerificarCodigoActivity.this);
        requestQueue.add(imageRequest);
    }
}