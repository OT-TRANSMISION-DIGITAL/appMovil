package com.example.transmisiondigital;

import static com.example.transmisiondigital.globalVariables.Conexion.URL;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OrderActivity extends AppCompatActivity {

    private String idOrder;
    private TextView textViewFolio, textViewDate, textViewHour, textViewAddress, textViewCustomer;
    private LinearLayout container;
    private TextView textViewTechnical, textViewApplicant, textViewPosition, textViewStatus, textViewEntryTime, textViewDepartureTime, textViewTotal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        footer();
        Intent intent = getIntent();
        idOrder = intent.getStringExtra("idOrder");
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
        textViewDepartureTime = findViewById(R.id.textViewDepartureTime);
        //textViewTotal = findViewById(R.id.textViewTotal);
        container = findViewById(R.id.textViewService1);
        init();

    }

    public void init() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL + "ordenes/" + idOrder, null, response -> {
            try {
                // Obtener el objeto JSON de la respuesta
                JSONObject data = response;
                JSONArray detallesArray = data.getJSONArray("detalles");
                String clienteNombre = data.getJSONObject("cliente").getString("nombre");
                String tecnicoNombre = data.getJSONObject("tecnico").getString("nombre");

                String fechaHoraSolicitudStr = data.getString("fechaHoraSolicitud");
                String fechaHoraLLegadaStr = data.getString("fechaHoraLlegada");
                String fechaHoraSalidaStr = data.getString("fechaHoraSalida");

                SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
                Date fechaHoraSolicitud = formatoOriginal.parse(fechaHoraSolicitudStr);
                Date fechaHoraLLegada = formatoOriginal.parse(fechaHoraLLegadaStr);
                Date fechaHoraSalida = formatoOriginal.parse(fechaHoraSalidaStr);

                // Formatear a strings de fecha y hora
                String soloFecha = formatoFecha.format(fechaHoraSolicitud);
                String soloHora = formatoHora.format(fechaHoraSolicitud);
                String horaLLegada = formatoHora.format(fechaHoraLLegada);
                String horaSalida = formatoHora.format(fechaHoraSalida);

                textViewFolio.setText("Folio: "+ data.getString("id"));
                textViewDate.setText("Fecha: " + soloFecha);
                textViewHour.setText("Hora: " + soloHora);
                textViewAddress.setText("Dirección: " + data.getString("direccion"));
                textViewCustomer.setText("Cliente: " + clienteNombre);
                textViewTechnical.setText("Tecnico: " + tecnicoNombre);
                textViewApplicant.setText("Persona que solicita: " + data.getString("persona_solicitante"));
                textViewPosition.setText("Puesto: " + data.getString("puesto"));
                textViewStatus.setText("Estatus: " + data.getString("estatus"));
                textViewEntryTime.setText("Hora de llegada: " + horaLLegada);
                textViewDepartureTime.setText("Hora de salida: " + horaSalida);

                for (int i = 0; i < detallesArray.length(); i++) {
                    JSONObject detalle = detallesArray.getJSONObject(i);

                    // Obtén el objeto "producto"
                    JSONObject producto = detalle.getJSONObject("producto");

                    // Obtén el nombre del producto
                    String nombre = producto.getString("nombre");

                    TextView textView = new TextView(this);
                    textView.setText((i+1) + ".- "  + nombre);
                    textView.setTextSize(18);
                    textView.setPadding(16, 16, 16, 16);

                    // Agregar el TextView al contenedor
                    container.addView(textView);
                    // Agrega el nombre a la lista
                }
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
                Toast.makeText(OrderActivity.this, message, Toast.LENGTH_SHORT).show();
            } catch (UnsupportedEncodingException | JSONException e) {
                e.printStackTrace();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(OrderActivity.this);
        requestQueue.add(jsonObjectRequest);

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