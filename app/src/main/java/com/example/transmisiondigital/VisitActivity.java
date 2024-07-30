package com.example.transmisiondigital;

import static com.example.transmisiondigital.globalVariables.Conexion.URL;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VisitActivity extends AppCompatActivity {

    private String idVisit;
    private SharedPreferences sharedPreferences;
    private TextView textViewFolio, textViewDate, textViewHour, textViewAddress, textViewCustomer;
    private TextView textViewTechnical, textViewApplicant, textViewPosition, textViewStatus, textViewEntryTime;
    private Button buttonSave;
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

    public void init(){
        textViewFolio = findViewById(R.id.textViewFolio);
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
        /*if (sharedPreferences.getString("rol", null).equals("Técnico")) {
            spinnerStatus.setVisibility(View.GONE);
            buttonSave.setVisibility(View.GONE);
        }*/
        spinnerStatus = findViewById(R.id.spinnerStatus);
        String[] items = {"Autorizar", "Finalizar", "Cancelar"};

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);


        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        spinnerStatus.setAdapter(adapter);
        requestApi();
        buttonSave();

    }

    public void requestApi() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL + "visitas/" + idVisit, null, response -> {
            try {
                // Obtener el objeto JSON de la respuesta
                JSONObject data = response;

                String clienteNombre = data.getJSONObject("cliente").getString("nombre");
                String tecnicoNombre = data.getJSONObject("tecnico").getString("nombre");

                String fechaHoraSolicitudStr = data.getString("fechaHoraSolicitud");
                String fechaHoraLlegadaStr = data.getString("fechaHoraLlegada");
                SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");

                Date fechaHoraSolicitud = formatoOriginal.parse(fechaHoraSolicitudStr);
                Date fechaHoraLlegada = formatoOriginal.parse(fechaHoraLlegadaStr);
                String fechaSolicitud = formatoFecha.format(fechaHoraSolicitud);
                String horaSolicitud = formatoHora.format(fechaHoraSolicitud);
                String horaLlegada = formatoHora.format(fechaHoraLlegada);


                textViewFolio.setText("Folio: " + data.getString("id"));
                textViewDate.setText("Fecha: " + fechaSolicitud);
                textViewHour.setText("Hora: "+ horaSolicitud);
                textViewAddress.setText("Dirección: " + data.getString("direccion"));
                textViewCustomer.setText("Cliente: " + clienteNombre);
                textViewTechnical.setText("Tecnico: " + tecnicoNombre);
                textViewStatus.setText("Estatus: " + data.getString("estatus"));
                textViewEntryTime.setText("Hora de llegada: " + horaLlegada);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                throw new RuntimeException(e);
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
            if(status.equals("Autorizar"))
            {
                apiEstatus = "autorizar/";
            }
            else if(status.equals("Finalizar"))
            {
                apiEstatus = "finalizar/";
            }
            else if(status.equals("Cancelar"))
            {
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