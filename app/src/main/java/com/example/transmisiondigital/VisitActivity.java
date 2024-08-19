package com.example.transmisiondigital;

import static com.example.transmisiondigital.globalVariables.Conexion.URL;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.transmisiondigital.drawing.LoadingDialog;
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
    private String idVisit, horaLlegada, fechaHoraSolicitudStr, motivo, direccion, tecnicoId, clienteId, sucursalId, horaSolicitud, fechaHoraSalidaStr;
    private SharedPreferences sharedPreferences;
    private TextView textViewFolio, textViewDate, textViewHour, textViewAddress, textViewCustomer, textViewReason, textViewDepartureTime;
    private TextView textViewTechnical, textViewApplicant, textViewPosition, textViewStatus, textViewEntryTime;
    private Button buttonSave, buttonAttend, buttonSetHour;
    private Spinner spinnerStatus;
    private ArrayAdapter<String> adapter;
    private FrameLayout frameLayoutSpinner;
    private ProgressDialog progressDialog;
    private double latitude, longitude;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int REQUEST_LOCATION_PERMISSIONS = 3;

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
        frameLayoutSpinner = findViewById(R.id.frameLayoutSpinner);
        textViewDepartureTime = findViewById(R.id.textViewDepartureTime);
        progressDialog = new ProgressDialog(VisitActivity.this);
        progressDialog.setMessage("Cargando...");

        if (sharedPreferences.getString("rol", null).equals("Técnico")) {
            spinnerStatus.setVisibility(View.GONE);
            buttonSave.setVisibility(View.GONE);
            buttonAttend.setVisibility(View.VISIBLE);
            buttonSetHour.setVisibility(View.VISIBLE);
            frameLayoutSpinner.setVisibility(View.GONE);
        }

        header();
        String[] items = {"Autorizar", "Finalizar", "Cancelar"};

        adapter = new ArrayAdapter<>(this, R.layout.spinner_item, items);


        adapter.setDropDownViewResource(R.layout.spinner_item_dropdown);


        spinnerStatus.setAdapter(adapter);
        requestApi();
        buttonSave();
        setButtonAttend();
        setButtonHour();
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
        progressDialog.show();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL + "visitas/" + idVisit, null, response -> {
            try {
                // Obtener el objeto JSON de la respuesta
                JSONObject data = response;

                String clienteNombre = data.getJSONObject("cliente").getString("nombre");
                String tecnicoNombre = data.getJSONObject("tecnico").getString("nombre");

                fechaHoraSolicitudStr = data.optString("fechaHoraSolicitud", "");
                fechaHoraSalidaStr = data.optString("fechaHoraSalida", "");
                String fechaHoraLlegadaStr = data.optString("fechaHoraLlegada", "");
                SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");

                Date fechaHoraSolicitud = null;
                Date fechaHoraLlegada = null;
                Date fechaHoraSalida = null;
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
                try {
                    fechaHoraSalida = formatoOriginal.parse(fechaHoraSalidaStr);
                } catch (ParseException e) {
                    // Handle the exception or log it if necessary
                }

                String fechaSolicitud = (fechaHoraSolicitud != null) ? formatoFecha.format(fechaHoraSolicitud) : "";
                horaSolicitud = (fechaHoraSolicitud != null) ? formatoHora.format(fechaHoraSolicitud) : "";
                String horaSalida = (fechaHoraSalida != null) ? formatoHora.format(fechaHoraSalida) : "";
                horaLlegada = (fechaHoraLlegada != null) ? formatoHora.format(fechaHoraLlegada) : "";
                Date fechaActual = new Date();
                String fechaActualStr = formatoFecha.format(fechaActual);
                String horaActualStr = formatoHora.format(fechaActual);

                if (!fechaSolicitud.equals(fechaActualStr) || (fechaSolicitud.equals(fechaActualStr) && horaActualStr.compareTo(horaSolicitud) < 0)) {
                    // Hide buttons if the request date is not today or if the current time is before the request time
                    buttonAttend.setVisibility(View.GONE);
                    buttonSetHour.setVisibility(View.GONE);
                }
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
                textViewDepartureTime.setText("Hora de salida: " + horaSalida);
                Log.d("Hora de llegada", horaLlegada);

                if (horaLlegada != null && !horaLlegada.isEmpty() && !horaLlegada.equals("")) {
                    buttonSetHour.setVisibility(View.GONE);
                }

                if (estatus.equals("Finalizada")) {
                    buttonSetHour.setVisibility(View.GONE);
                    buttonAttend.setVisibility(View.GONE);
                    spinnerStatus.setSelection(1);
                    estatus = "Finalizar";
                } else if (estatus.equals("Autorizada")) {
                    spinnerStatus.setSelection(0);
                    estatus = "Autorizar";
                } else if (estatus.equals("Cancelada")) {
                    spinnerStatus.setSelection(2);
                    estatus = "Cancelar";
                }

                progressDialog.dismiss();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            try {
                progressDialog.dismiss();
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
            progressDialog.show();
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
                    progressDialog.dismiss();

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
                    progressDialog.dismiss();
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
            new AlertDialog.Builder(VisitActivity.this)
                    .setTitle("Confirmar")
                    .setMessage("¿Desea finalizar la orden?")
                    .setPositiveButton("Confirmar", (dialog, which) -> {
                        // Acción a realizar si se confirma
                        finalizarVisita();
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> {
                        // No hacer nada si se cancela
                        dialog.dismiss();
                    })
                    .show();
        });
    }

    private void finalizarVisita() {
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

        // Parse the horaLlegada string to extract hour, minute, and second
        String[] timeParts = horaLlegada.split(":");
        int llegadaHour = Integer.parseInt(timeParts[0]);
        int llegadaMinute = Integer.parseInt(timeParts[1]);
        int llegadaSecond = Integer.parseInt(timeParts[2]);

        // Get the current time
        Calendar calendarCurrent = Calendar.getInstance();
        int currentHour = calendarCurrent.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendarCurrent.get(Calendar.MINUTE);
        int currentSecond = calendarCurrent.get(Calendar.SECOND);

        // Calculate the time 5 minutes after the horaLlegada
        Calendar calendarLlegada = Calendar.getInstance();
        calendarLlegada.set(Calendar.HOUR_OF_DAY, llegadaHour);
        calendarLlegada.set(Calendar.MINUTE, llegadaMinute);
        calendarLlegada.set(Calendar.SECOND, llegadaSecond);
        calendarLlegada.add(Calendar.MINUTE, 5);
        int limitHour = calendarLlegada.get(Calendar.HOUR_OF_DAY);
        int limitMinute = calendarLlegada.get(Calendar.MINUTE);
        int limitSecond = calendarLlegada.get(Calendar.SECOND);

        // Check if the current time is before the calculated time
        if (currentHour < limitHour || (currentHour == limitHour && currentMinute < limitMinute) || (currentHour == limitHour && currentMinute == limitMinute && currentSecond < limitSecond)) {
            Toast.makeText(VisitActivity.this, "Debe esperar 5 minutos después de la hora de llegada", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!requestLocationUpdates()) {
            Toast.makeText(VisitActivity.this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
            return;
        }

        String coorSalida = convertToDMS(latitude, true) + " " + convertToDMS(longitude, false);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(calendar.getTime());

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String fechaHoraSalida = dateTimeFormat.format(calendar.getTime());

        JSONObject visita = new JSONObject();
        try {
            visita.put("fechaHoraSalida", fechaHoraSalida);
            visita.put("coorSalida", coorSalida);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        progressDialog.show();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, URL + "visitas/finalizar/" + idVisit, visita, response -> {
            progressDialog.dismiss();
            Intent intent = new Intent(VisitActivity.this, VisitsActivity.class);
            startActivity(intent);
            finish();
        }, error -> {
            try {
                progressDialog.dismiss();
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

    }

    public void setButtonHour() {
        buttonSetHour.setOnClickListener(v -> {
            new AlertDialog.Builder(VisitActivity.this)
                    .setTitle("Confirmar")
                    .setMessage("¿Desea asignar hora de llegada?")
                    .setPositiveButton("Confirmar", (dialog, which) -> {
                        // Acción a realizar si se confirma
                        setHour();
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> {
                        // No hacer nada si se cancela
                        dialog.dismiss();
                    })
                    .show();
        });
    }

    public void setHour() {
        // Get the current time
        Calendar calendarCurrent = Calendar.getInstance();
        int currentHour = calendarCurrent.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendarCurrent.get(Calendar.MINUTE);

        // Parse the soloHora string to extract hour and minute
        Log.d("SoloHora", horaSolicitud);
        String[] timeParts = horaSolicitud.split(":");
        int requestHour = Integer.parseInt(timeParts[0]);
        int requestMinute = Integer.parseInt(timeParts[1]);

        // Calculate the time 10 minutes after the soloHora
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, requestHour);
        calendar.set(Calendar.MINUTE, requestMinute);
        calendar.add(Calendar.MINUTE, 10);
        int limitHour = calendar.get(Calendar.HOUR_OF_DAY);
        int limitMinute = calendar.get(Calendar.MINUTE);

        // Check if the current time is before the request time
        if (currentHour < requestHour || (currentHour == requestHour && currentMinute < requestMinute)) {
            Toast.makeText(VisitActivity.this, "No se puede elegir una hora antes de las " + String.format(Locale.getDefault(), "%02d:%02d", requestHour, requestMinute), Toast.LENGTH_SHORT).show();
        }
        // Check if the current time is within 10 minutes after the request time
        else if (currentHour < limitHour || (currentHour == limitHour && currentMinute <= limitMinute)) {
            Toast.makeText(VisitActivity.this, "No se puede elegir una hora dentro de los 10 minutos después de la hora de solicitud", Toast.LENGTH_SHORT).show();
        }
        // Set the current time to the textViewHour
        else {
            String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", currentHour, currentMinute);
            requestApiupdate();
            //textViewEntryTime.setText("Hora de llegada: " + selectedTime + ":00");
        }
    }

    public void requestApiupdate() {
        progressDialog.show();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(calendar.getTime());

        if (!requestLocationUpdates()) {
            Toast.makeText(VisitActivity.this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
            return;
        }
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String fechaHoraLlegada = dateTimeFormat.format(calendar.getTime());

        String coorLlegada = convertToDMS(latitude, true) + " " + convertToDMS(longitude, false);

        JSONObject visita = new JSONObject();
        try {
            visita.put("fechaHoraLlegada", fechaHoraLlegada);
            visita.put("coorLlegada", coorLlegada);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL + "visitas/horaLlegada/" + idVisit, visita, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String message = null;
                try {
                    progressDialog.dismiss();
                    message = response.getString("msg");
                    Toast.makeText(VisitActivity.this, message, Toast.LENGTH_SHORT).show();
                    requestApi();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    progressDialog.dismiss();
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

    public boolean requestLocationUpdates() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                // Use the latitude and longitude as needed
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
            }
        };

        if (ActivityCompat.checkSelfPermission(VisitActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(VisitActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request the necessary permissions
            ActivityCompat.requestPermissions(VisitActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSIONS);
            return false;
        } else {
            // Permissions are already granted, proceed to get the last known location
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                // Use latitude and longitude as needed
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
                return true;
            } else {
                // Handle the case where location is null
                Toast.makeText(VisitActivity.this, "No se pudo obtener la ubicacion", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
    }

    private String convertToDMS(double coord, boolean isLatitude) {
        String direction;
        if (isLatitude) {
            direction = coord >= 0 ? "N" : "S";
        } else {
            direction = coord >= 0 ? "E" : "W";
        }
        coord = Math.abs(coord);
        int degrees = (int) coord;
        coord = (coord - degrees) * 60;
        int minutes = (int) coord;
        coord = (coord - minutes) * 60;
        double seconds = coord;

        //Log.d("Coordenadas", String.format(Locale.getDefault(), "%d°%02d'%05.1f\"%s", degrees, minutes, seconds, direction));
        return String.format(Locale.getDefault(), "%d°%02d'%05.1f\"%s", degrees, minutes, seconds, direction).replace("\\", "");
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
            Intent intent = new Intent(this, CalendarActivity.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        });

        BtnAccount.setOnClickListener(v -> {
            Intent intent = new Intent(this, AccountActivity.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        });
    }
}