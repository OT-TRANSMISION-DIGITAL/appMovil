package com.example.transmisiondigital;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.transmisiondigital.adapters.OrderAdapter;
import com.example.transmisiondigital.globalVariables.Conexion;
import com.example.transmisiondigital.includes.footerActivity;
import com.example.transmisiondigital.models.Orders;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {
    private footerActivity footer;
    List<Orders> ordersList;
    public String URL = Conexion.URL;
    private Button buttonDatePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        buttonDatePicker = findViewById(R.id.buttonDatePicker);

        Date currentDate = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        footer();
        init(dateFormat);
        pickerDate();
    }

    public void init(SimpleDateFormat dateFilter) {
       ordersList = new ArrayList<>();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL + "ordenes", null, response -> {
            try {
                JSONArray dataArray = response.getJSONArray("data");
                for (int i = 0; i < dataArray.length(); i++) {
                    Log.e("OrdersActivity", "init: " + dataArray.length());
                    JSONObject order = dataArray.getJSONObject(i);
                    String fechaHoraSolicitudStr = order.getString("fechaHoraSolicitud");
                    SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    // Formatos para fecha y hora
                    SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
                    SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
                    Date fechaHoraSolicitud = formatoOriginal.parse(fechaHoraSolicitudStr);

                    // Formatear a strings de fecha y hora
                    String soloFecha = formatoFecha.format(fechaHoraSolicitud);
                    String soloHora = formatoHora.format(fechaHoraSolicitud);
                    String status = order.getString("estatus");
                    String idOrder = order.getString("id");
                    ordersList.add(new Orders(soloFecha, status, idOrder, soloHora, idOrder));
                }
                // Configurar y establecer el adaptador del RecyclerView aquí
                RecyclerView recyclerView = findViewById(R.id.recyclerViewOrders);
                recyclerView.setLayoutManager(new LinearLayoutManager(OrdersActivity.this));
                OrderAdapter orderAdapter = new OrderAdapter(ordersList, OrdersActivity.this);
                recyclerView.setHasFixedSize(true);
                recyclerView.setAdapter(orderAdapter);
            } catch (Exception e) {
                e.printStackTrace();
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
                //progressDialog.dismiss();
                // Mostrar el mensaje de error en un cuadro de diálogo o Toast
                Toast.makeText(OrdersActivity.this, mensajeError, Toast.LENGTH_SHORT).show();
            }
        });

        // Añadir la petición a la cola de solicitudes
        RequestQueue queue = Volley.newRequestQueue(OrdersActivity.this);
        queue.add(jsonObjectRequest);

    }

    public Date date(String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = format.parse(dateStr);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void pickerDate(){
        buttonDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int day = c.get(Calendar.DAY_OF_MONTH);
                int month = c.get(Calendar.MONTH);
                int year = c.get(Calendar.YEAR);

                DatePickerDialog datePickerDialog = new DatePickerDialog(OrdersActivity.this,
                        (view, selectedYear, selectedMonth, selectedDay) -> {
                            String selectedDate =" Fecha: " + selectedDay + "-" + (selectedMonth + 1) + "-" + selectedYear + " ";
                            buttonDatePicker.setText(selectedDate);
                        }, year, month, day);
                datePickerDialog.show();
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
            Log.d("footerActivity", "onClick: OrdersActivity");
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