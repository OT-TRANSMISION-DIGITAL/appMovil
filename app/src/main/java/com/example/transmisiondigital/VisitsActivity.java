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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.transmisiondigital.adapters.OrderAdapter;
import com.example.transmisiondigital.adapters.VisitAdapter;
import com.example.transmisiondigital.drawing.LoadingDialog;
import com.example.transmisiondigital.includes.footerActivity;
import com.example.transmisiondigital.models.Orders;
import com.example.transmisiondigital.models.Visits;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class VisitsActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private ArrayAdapter<String> adapter;
    private Spinner spinnerType;
    public String selectedItem;
    private Button buttonDatePicker;
    private String dateFilter;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visits);
        sharedPreferences = getSharedPreferences("sessionUser", Context.MODE_PRIVATE);
        loadingDialog = new LoadingDialog(VisitsActivity.this);
        header();
        filters();
        //init(dateFilter, selectedItem);
        footer();
    }

    public void header() {
        loadingDialog.show();
        SharedPreferences sharedPreferences = getSharedPreferences("sessionUser", Context.MODE_PRIVATE);
        TextView textViewName = findViewById(R.id.textViewName);
        String userName = sharedPreferences.getString("userName", "");
        textViewName.setText(userName);
        TextView textViewTitle = findViewById(R.id.textViewTitle);
        textViewTitle.setText("VISITAS");
        ShapeableImageView imageProfile = findViewById(R.id.imageProfile);
        String userImage = sharedPreferences.getString("userImage", "");
        Log.i("userImage", userImage);
        if (!userImage.isEmpty()) {
            imageProfile.setImageURI(Uri.parse(userImage));
        } else {
            //imageButtonProfile.setImageResource(R.drawable.default_profile_image); // Replace with your default image resource
        }
        loadingDialog.hide();
    }

    public void init(String dateFilter, String type) {
        loadingDialog.show();
        List<Visits> visitsList = new ArrayList<>();

        String queryParams = null;

        try {
            String rol = sharedPreferences.getString("rol", "");
            if ("Administrador".equals(rol)) {
                queryParams = String.format(Locale.US, "?fecha=%s&estatus=%s",
                        URLEncoder.encode(dateFilter, "UTF-8"),
                        URLEncoder.encode(type, "UTF-8"));
            } else {
                queryParams = String.format(Locale.US, "?fecha=%s&estatus=%s&tecnico=%s",
                        URLEncoder.encode(dateFilter, "UTF-8"),
                        URLEncoder.encode(type, "UTF-8"),
                        URLEncoder.encode(Integer.toString(sharedPreferences.getInt("idUser", 0)), "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        String finalURL = URL + "visitas" + queryParams;
        Log.d("finalURL", finalURL);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, finalURL, null, response -> {
            try {
                JSONArray dataArray = response.getJSONArray("data");
                for (int i = 0; i < dataArray.length(); i++) {
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
                    String idVisit = order.getString("id");
                    visitsList.add(new Visits(soloFecha, status, idVisit, soloHora, idVisit));
                }
                // Configurar y establecer el adaptador del RecyclerView aquí
                RecyclerView recyclerView = findViewById(R.id.recyclerViewVisits);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                VisitAdapter visitsAdapter = new VisitAdapter(visitsList, this);
                recyclerView.setHasFixedSize(true);
                recyclerView.setAdapter(visitsAdapter);
            } catch (Exception e) {
                e.printStackTrace();
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

                        Toast.makeText(VisitsActivity.this, errorMessage.toString().trim(), Toast.LENGTH_SHORT).show();
                    } else {
                        String message = data.getString("message");
                        Log.e("ErrorMessage", message);
                        Toast.makeText(VisitsActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                } catch (UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        // Añadir la petición a la cola de solicitudes
        RequestQueue queue = Volley.newRequestQueue(VisitsActivity.this);
        queue.add(jsonObjectRequest);
        loadingDialog.hide();
    }

    public void filters() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
        dateFilter = dateFormat.format(date);
        selectedItem = "Autorizada";
        spinnerSetUp();
        pickerDate();
    }

    public void spinnerSetUp() {
        spinnerType = findViewById(R.id.spinnerType);
        String[] items = {"Estatus: Autorizada", "Estatus: Finalizada"};

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
                selectedItem = selectedText.replace("Estatus: ", "");
                init(dateFilter, selectedItem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
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

            DatePickerDialog datePickerDialog = new DatePickerDialog(VisitsActivity.this, R.style.CustomDatePickerDialog,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Crear un objeto Calendar para formatear la fecha
                        java.util.Calendar selectedDate = java.util.Calendar.getInstance();
                        selectedDate.set(selectedYear, selectedMonth, selectedDay);

                        // Formatear la fecha a "dd-MM-yyyy"
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                        String formattedDate = dateFormat.format(selectedDate.getTime());

                        // Mostrar la fecha formateada en el botón
                        Log.d("buttonDatePicker", "formattedDate: " + formattedDate);
                        buttonDatePicker.setText(" FECHA: " + formattedDate + " ");

                        // Actualizar dateFilter con el formato correcto
                        SimpleDateFormat dateFormatForInit = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
                        dateFilter = dateFormatForInit.format(selectedDate.getTime());
                        //dateFilter = formattedDate;
                        init(dateFilter, selectedItem);
                    }, year, month, day);
            datePickerDialog.show();
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
            Log.d("footerActivity", "onClick: VisitsActivity");
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