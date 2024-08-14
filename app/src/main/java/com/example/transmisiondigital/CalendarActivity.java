package com.example.transmisiondigital;

import static com.example.transmisiondigital.globalVariables.Conexion.URL;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.transmisiondigital.adapters.CalendarAdapter;
import com.example.transmisiondigital.adapters.OrderAdapter;
import com.example.transmisiondigital.models.Calendar;
import com.example.transmisiondigital.models.Orders;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    private ArrayList<Calendar> calendarsList;
    private Spinner spinnerType;
    private ArrayAdapter<String> adapter;
    public String selectedItem;
    private Button buttonDatePicker;
    private String dateFilter;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        sharedPreferences = getSharedPreferences("sessionUser", Context.MODE_PRIVATE);
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
        dateFilter = dateFormat.format(date);
        header();
        footer();
        spinnerSetUp();
        pickerDate();
        selectedItem = "ordenes";
        //init(dateFilter, selectedItem);
    }

    public void header() {
        SharedPreferences sharedPreferences = getSharedPreferences("sessionUser", Context.MODE_PRIVATE);
        TextView textViewTitle = findViewById(R.id.textViewTitle);
        textViewTitle.setText("AGENDA");
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

    public void pickerDate() {
        Date date = new Date();
        buttonDatePicker = findViewById(R.id.buttonDatePicker);
        SimpleDateFormat dateFormatInit = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDateInit = dateFormatInit.format(date);
        buttonDatePicker.setText(" FECHA: " + formattedDateInit + " ");
        buttonDatePicker.setOnClickListener(v -> {
            final java.util.Calendar calendar = java.util.Calendar.getInstance();
            int year = calendar.get(java.util.Calendar.YEAR);
            int month = calendar.get(java.util.Calendar.MONTH);
            int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(CalendarActivity.this, R.style.CustomDatePickerDialog,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Crear un objeto Calendar para formatear la fecha
                        java.util.Calendar selectedDate = java.util.Calendar.getInstance();
                        selectedDate.set(selectedYear, selectedMonth, selectedDay);

                        // Formatear la fecha a "dd-MM-yyyy"
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                        String formattedDate = dateFormat.format(selectedDate.getTime());

                        // Mostrar la fecha formateada en el botón
                        buttonDatePicker.setText(" FECHA: " + formattedDate + " ");
                        Log.d("buttonDatePicker", "formattedDate: " + formattedDate);

                        // Actualizar dateFilter con el formato correcto
                        SimpleDateFormat dateFormatForInit = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
                        dateFilter = dateFormatForInit.format(selectedDate.getTime());
                        //dateFilter = formattedDate;
                        Log.i("buttonDatePicker", "dateFilter: " + dateFilter);
                        init(dateFilter, selectedItem);
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    public void spinnerSetUp() {
        spinnerType = findViewById(R.id.spinnerType);
        String[] items = {"Tipo: Ordenes", "Tipo: Visitas"};

        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = new ArrayAdapter<>(this, R.layout.spinner_item, items);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.spinner_item_dropdown);

        // Apply the adapter to the spinner
        spinnerType.setAdapter(adapter);

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedText = parent.getItemAtPosition(position).toString();
                selectedItem = selectedText.replace("Tipo: ", "").toLowerCase();
                init(dateFilter, selectedItem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    public void init(String dateFilter, String type) {
        calendarsList = new ArrayList<>();
        String queryParams = null;

        try {
            String rol = sharedPreferences.getString("rol", "");
            if ("Administrador".equals(rol)) {
                queryParams = String.format(Locale.US, "?fecha=%s&tipo=%s",
                        URLEncoder.encode(dateFilter, "UTF-8"),
                        URLEncoder.encode(type, "UTF-8"));
            } else {
                queryParams = String.format(Locale.US, "?fecha=%s&estatus=%s&tipo=%s&tecnico=%s",
                        URLEncoder.encode(dateFilter, "UTF-8"),
                        URLEncoder.encode("Autorizada", "UTF-8"),
                        URLEncoder.encode(type, "UTF-8"),
                        URLEncoder.encode(Integer.toString(sharedPreferences.getInt("idUser", 0)), "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        String finalURL = URL + "agenda" + queryParams;
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, finalURL, null, response -> {
            try {
                JSONArray dataArray = new JSONArray(response.toString());
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject calendar = dataArray.getJSONObject(i);
                    String fechaHoraSolicitudStr = calendar.getString("fechaHoraSolicitud");
                    SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
                    SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
                    Date fechaHoraSolicitud = formatoOriginal.parse(fechaHoraSolicitudStr);

                    // Formatear a strings de fecha y hora
                    String soloFecha = formatoFecha.format(fechaHoraSolicitud);
                    String soloHora = formatoHora.format(fechaHoraSolicitud);
                    String status = calendar.getString("estatus");
                    String id = calendar.getString("id");

                    calendarsList.add(new Calendar(soloFecha, status, id, soloHora, id, selectedItem));
                }
                // Configurar y establecer el adaptador del RecyclerView aquí
                RecyclerView recyclerView = findViewById(R.id.recyclerViewCalendar);
                recyclerView.setLayoutManager(new LinearLayoutManager(CalendarActivity.this));
                CalendarAdapter calendarAdapter = new CalendarAdapter(calendarsList, CalendarActivity.this);
                recyclerView.setHasFixedSize(true);
                recyclerView.setAdapter(calendarAdapter);
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
                        String responseData = new String(error.networkResponse.data, "UTF-8");
                        JSONObject jsonObject = new JSONObject(responseData);
                        mensajeError = jsonObject.optString("msg", "Error en la petición");
                    } catch (UnsupportedEncodingException | JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    mensajeError = "Error de red";
                }

                //progressDialog.dismiss();
                // Mostrar el mensaje de error en un cuadro de diálogo o Toast
                Toast.makeText(CalendarActivity.this, mensajeError, Toast.LENGTH_SHORT).show();
            }
        });

        // Añadir la petición a la cola de solicitudes
        RequestQueue queue = Volley.newRequestQueue(CalendarActivity.this);
        queue.add(jsonObjectRequest);

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