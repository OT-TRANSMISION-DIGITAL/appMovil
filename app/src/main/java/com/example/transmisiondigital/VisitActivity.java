package com.example.transmisiondigital;

import static com.example.transmisiondigital.globalVariables.Conexion.URL;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class VisitActivity extends AppCompatActivity {

    private String idVisit, horaLlegada, fechaHoraSolicitudStr, motivo, direccion, tecnicoId, clienteId, sucursalId;
    private SharedPreferences sharedPreferences;
    private TextView textViewFolio, textViewDate, textViewHour, textViewAddress, textViewCustomer, textViewReason;
    private TextView textViewTechnical, textViewApplicant, textViewPosition, textViewStatus, textViewEntryTime;
    private Button buttonSave, buttonAttend, buttonSetHour;
    private Spinner spinnerStatus;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit);
        TextView textViewTitle = findViewById(R.id.textViewTitle);
        textViewTitle.setText("VISITAS");
        Intent intent = getIntent();
        idVisit = intent.getStringExtra("idVisit");
        sharedPreferences = getSharedPreferences("sessionUser", Context.MODE_PRIVATE);
        footer();
        init();
    }

    public void init() {
        textViewFolio = findViewById(R.id.textViewFolio);
        textViewReason = findViewById(R.id.textViewReason);
        textViewDate = findViewById(R.id.textViewDate);
        textViewHour = findViewById(R.id.textViewHour);
        textViewAddress = findViewById(R.id.textViewAddress);
        textViewCustomer = findViewById(R.id.textViewCustomer);
        textViewTechnical = findViewById(R.id.textViewTechnical);
        textViewApplicant = findViewById(R.id.textViewApplicant);
        textViewPosition = findViewById(R.id.textViewPosition);
        textViewStatus = findViewById(R.id.textViewStatus);
        textViewEntryTime = findViewById(R.id.textViewEntryTime);
        buttonSave = findViewById(R.id.buttonSave);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        buttonAttend = findViewById(R.id.buttonAttend);
        buttonSetHour = findViewById(R.id.buttonSetHour);

        if (sharedPreferences.getString("rol", null).equals("Técnico")) {
            spinnerStatus.setVisibility(View.GONE);
            buttonSave.setVisibility(View.GONE);
            buttonAttend.setVisibility(View.VISIBLE);
            buttonSetHour.setVisibility(View.VISIBLE);
        }

        header();
        String[] items = {"Autorizar", "Finalizar", "Cancelar"};

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);


        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        spinnerStatus.setAdapter(adapter);
        requestApi();
        buttonSave();
        setButtonAttend();
        setHour();
    }

    public void header() {
        TextView textViewName = findViewById(R.id.textViewName);
        String userName = sharedPreferences.getString("userName", "");
        textViewName.setText(userName);
        ShapeableImageView imageProfile = findViewById(R.id.imageProfile);
        String userImage = sharedPreferences.getString("userImage", "");
        Log.i("userImage", userImage);
        if (!userImage.isEmpty()) {
            imageProfile.setImageURI(Uri.parse(userImage));
        } else {
            //imageButtonProfile.setImageResource(R.drawable.default_profile_image); // Replace with your default image resource
        }
    }

    public void requestApi() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL + "visitas/" + idVisit, null, response -> {
            try {
                // Obtener el objeto JSON de la respuesta
                JSONObject data = response;

                String clienteNombre = data.getJSONObject("cliente").getString("nombre");
                String tecnicoNombre = data.getJSONObject("tecnico").getString("nombre");

                fechaHoraSolicitudStr = data.optString("fechaHoraSolicitud", "");
                String fechaHoraLlegadaStr = data.optString("fechaHoraLlegada", "");
                SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");

                Date fechaHoraSolicitud = null;
                Date fechaHoraLlegada = null;
                try {
                    fechaHoraSolicitud = formatoOriginal.parse(fechaHoraSolicitudStr);
                } catch (ParseException e) {
                    // Handle the exception or log it if necessary
                }
                try {
                    fechaHoraLlegada = formatoOriginal.parse(fechaHoraLlegadaStr);
                } catch (ParseException e) {
                    // Handle the exception or log it if necessary
                }

                String fechaSolicitud = (fechaHoraSolicitud != null) ? formatoFecha.format(fechaHoraSolicitud) : "";
                String horaSolicitud = (fechaHoraSolicitud != null) ? formatoHora.format(fechaHoraSolicitud) : "";
                String horaLlegada = (fechaHoraLlegada != null) ? formatoHora.format(fechaHoraLlegada) : "";
                motivo = data.optString("motivo", "");
                direccion = data.optString("direccion", "");
                tecnicoId = data.optString("tecnico_id", "");
                clienteId = data.optString("cliente_id", "");
                sucursalId = data.optString("sucursal_id", "");
                String estatus = data.getString("estatus");

                textViewFolio.setText("Folio: " + data.getString("id"));
                textViewReason.setText("Motivo: " + motivo);
                textViewDate.setText("Fecha: " + fechaSolicitud);
                textViewHour.setText("Hora: " + horaSolicitud);
                textViewAddress.setText("Dirección: " + direccion);
                textViewCustomer.setText("Cliente: " + clienteNombre);
                textViewTechnical.setText("Tecnico: " + tecnicoNombre);
                textViewStatus.setText("Estatus: " + data.getString("estatus"));
                textViewEntryTime.setText("Hora de llegada: " + horaLlegada);

                if (estatus.equals("Finalizada")) {
                    buttonSetHour.setVisibility(View.GONE);
                    buttonAttend.setVisibility(View.GONE);
                    estatus = "Finalizar";
                } else if (estatus.equals("Autorizada")) {
                    estatus = "Autorizar";
                } else if (estatus.equals("Cancelada")) {
                    estatus = "Cancelar";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            try {
                String responseBody = new String(error.networkResponse.data, "utf-8");
                JSONObject data = new JSONObject(responseBody);
                String message = data.getString("message");
                Toast.makeText(VisitActivity.this, message, Toast.LENGTH_SHORT).show();
            } catch (UnsupportedEncodingException | JSONException e) {
                e.printStackTrace();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(VisitActivity.this);
        requestQueue.add(jsonObjectRequest);
    }

    public void buttonSave() {
        buttonSave.setOnClickListener(v -> {
            String status = spinnerStatus.getSelectedItem().toString();
            String apiEstatus = "";
            JSONObject jsonObject = new JSONObject();
            if (status.equals("Autorizar")) {
                apiEstatus = "autorizar/";
            } else if (status.equals("Finalizar")) {
                apiEstatus = "finalizar/";
            } else if (status.equals("Cancelar")) {
                apiEstatus = "cancelar/";
            }
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, URL + "visitas/" + apiEstatus + idVisit, jsonObject, response -> {
                try {
                    String message = response.getString("msg");
                    Toast.makeText(VisitActivity.this, message, Toast.LENGTH_SHORT).show();
                    // Create a Handler to introduce a delay
                    Handler handler = new Handler(Looper.getMainLooper());

                    // Post a Runnable with a delay of 2 seconds (2000 milliseconds)
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            requestApi();
                        }
                    }, 2000);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> {
                try {
                    String responseBody = new String(error.networkResponse.data, "utf-8");
                    JSONObject data = new JSONObject(responseBody);
                    String message = data.getString("msg");
                    Toast.makeText(VisitActivity.this, message, Toast.LENGTH_SHORT).show();
                } catch (UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                }
            });

            RequestQueue requestQueue = Volley.newRequestQueue(VisitActivity.this);
            requestQueue.add(jsonObjectRequest);
        });
    }

    public void setButtonAttend() {
        buttonAttend.setOnClickListener(v -> {
            String entryTimeText = textViewEntryTime.getText().toString();
            if (entryTimeText.contains("Hora de llegada: ")) {
                Log.i("", entryTimeText);
                String[] parts = entryTimeText.split("Hora de llegada: ");
                if (parts.length > 1 && !parts[1].trim().isEmpty()) {
                    horaLlegada = parts[1];
                } else {
                    horaLlegada = "";
                }
            } else {
                horaLlegada = "";
            }

            if (horaLlegada.isEmpty()) {
                Toast.makeText(VisitActivity.this, "Debe seleccionar una hora de llegada", Toast.LENGTH_SHORT).show();
                return;
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, URL + "visitas/finalizar/" + idVisit, null, response -> {
                requestApiupdate();
            }, error -> {
                try {
                    String responseBody = new String(error.networkResponse.data, "utf-8");
                    JSONObject data = new JSONObject(responseBody);
                    String message = data.getString("msg");
                    Toast.makeText(VisitActivity.this, message, Toast.LENGTH_SHORT).show();
                } catch (UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                }
            });

            RequestQueue requestQueue = Volley.newRequestQueue(VisitActivity.this);
            requestQueue.add(jsonObjectRequest);
        });
    }

    public void setHour() {
        buttonSetHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the current time
                Calendar calendar = Calendar.getInstance();
                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                int currentMinute = calendar.get(Calendar.MINUTE);

                // Subtract one hour from the current time
                calendar.add(Calendar.HOUR_OF_DAY, -1);
                int defaultHour = calendar.get(Calendar.HOUR_OF_DAY);
                int defaultMinute = calendar.get(Calendar.MINUTE);

                // Open a time picker dialog with the default time set to one hour less than the current time
                TimePickerDialog timePickerDialog = new TimePickerDialog(VisitActivity.this, (view, hourOfDay, minute) -> {
                    // Set the selected time to the textViewHour
                    String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    Log.d("HourSelected", "onClick: " + selectedTime);
                    textViewEntryTime.setText("Hora de llegada: " + selectedTime);
                }, defaultHour, defaultMinute, true);

                timePickerDialog.show();
            }
        });
    }

    public void requestApiupdate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(calendar.getTime());

        // Combine current date with horaLlegada and add seconds as 00
        String formattedDateTime = currentDate + " " + horaLlegada + ":00";
        horaLlegada = formattedDateTime;

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String fechaHoraSalida = dateTimeFormat.format(calendar.getTime());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("motivo", motivo);
            jsonObject.put("direccion", direccion);
            jsonObject.put("tecnico_id", tecnicoId);
            jsonObject.put("cliente_id", clienteId);
            jsonObject.put("sucursal_id", sucursalId);
            jsonObject.put("fechaHoraSolicitud", fechaHoraSolicitudStr);
            jsonObject.put("fechaHoraLlegada", horaLlegada);
            jsonObject.put("fechaHoraSalida", fechaHoraSalida);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, URL + "visitas/" + idVisit, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String message = null;
                try {
                    message = response.getString("msg");
                    Toast.makeText(VisitActivity.this, message, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(VisitActivity.this, VisitsActivity.class);
                    startActivity(intent);
                    finish();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    Log.e("Error", "onErrorResponse: " + error);
                    String responseBody = new String(error.networkResponse.data, "utf-8");
                    JSONObject data = new JSONObject(responseBody);

                    if (data.has("errors")) {
                        JSONObject errors = data.getJSONObject("errors");
                        StringBuilder errorMessage = new StringBuilder();

                        for (Iterator<String> it = errors.keys(); it.hasNext(); ) {
                            String key = it.next();
                            JSONArray errorMessages = errors.getJSONArray(key);
                            for (int i = 0; i < errorMessages.length(); i++) {
                                String errorMsg = errorMessages.getString(i);
                                errorMessage.append(errorMsg).append("\n");
                                Log.e("ErrorDetail", key + ": " + errorMsg);
                            }
                        }

                        Toast.makeText(VisitActivity.this, errorMessage.toString().trim(), Toast.LENGTH_SHORT).show();
                    } else {
                        String message = data.getString("message");
                        Log.e("ErrorMessage", message);
                        Toast.makeText(VisitActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                } catch (UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(VisitActivity.this);
        requestQueue.add(jsonObjectRequest);
    }

    public void footer() {
        sharedPreferences = getSharedPreferences("sessionUser", Context.MODE_PRIVATE);
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
            Log.d("footerActivity", "onClick: VisitsActivity");
            Intent intent = new Intent(this, VisitsActivity.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        });

        btnOrder.setOnClickListener(v -> {
            Log.d("footerActivity", "onClick: OrdersActivity");
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