package com.example.transmisiondigital;

import static com.example.transmisiondigital.globalVariables.Conexion.URL;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import com.example.transmisiondigital.drawing.LoadingDialog;
import com.example.transmisiondigital.models.Products;
import com.example.transmisiondigital.request.MultipartRequest;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class OrderActivity extends AppCompatActivity {

    private String idOrder, sucursalName, firmaUrl, clienteNombre, tecnicoNombre, fechaHoraSolicitudStr, fechaHoraLLegadaStr, fechaHoraSalidaStr, personaSolicitante, direccion, puesto, horaLlegada, soloHora;
    private int clienteId, sucursalId;
    private SharedPreferences sharedPreferences;
    private TextView textViewFolio, textViewDate, textViewHour, textViewAddress, textViewCustomer, textViewIva, textViewSubtotal;
    private LinearLayout container;
    private TextView textViewTechnical, textViewApplicant, textViewPosition, textViewStatus, textViewEntryTime, textViewDepartureTime, textViewTotal;
    private Spinner spinnerStatus;
    private Button buttonSave, buttonPrint, buttonSigned, buttonSaveTechnical, buttonEditProducts, buttonSetHour;
    private ImageView imageViewSignature;
    private ArrayAdapter<String> adapter;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 2;
    private BluetoothAdapter bluetoothAdapter;
    private JSONArray detallesArray;
    private List<Products> productsList;
    private FrameLayout frameLayoutSpinner;
    private ProgressDialog progressDialog;
    private static final int REQUEST_LOCATION_PERMISSIONS = 3;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        sharedPreferences = getSharedPreferences("sessionUser", Context.MODE_PRIVATE);
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
        container = findViewById(R.id.textViewService1);
        buttonSave = findViewById(R.id.buttonSave);
        textViewTotal = findViewById(R.id.textViewTotal);
        buttonPrint = findViewById(R.id.buttonPrint);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        buttonSigned = findViewById(R.id.buttonSigned);
        imageViewSignature = findViewById(R.id.imageViewSignature);
        buttonSaveTechnical = findViewById(R.id.buttonSaveTechnical);
        buttonEditProducts = findViewById(R.id.buttonEditProducts);
        frameLayoutSpinner = findViewById(R.id.frameLayoutSpinner);
        buttonSetHour = findViewById(R.id.buttonSetHour);
        textViewIva = findViewById(R.id.textViewIva);
        textViewSubtotal = findViewById(R.id.textViewSubtotal);
        progressDialog = new ProgressDialog(OrderActivity.this);
        progressDialog.setMessage("Cargando...");

        if (sharedPreferences.getString("rol", null).equals("Técnico")) {
            spinnerStatus.setVisibility(View.GONE);
            buttonSave.setVisibility(View.GONE);
            buttonPrint.setVisibility(View.VISIBLE);
            buttonSigned.setVisibility(View.VISIBLE);
            buttonSaveTechnical.setVisibility(View.VISIBLE);
            buttonEditProducts.setVisibility(View.VISIBLE);
            buttonSetHour.setVisibility(View.VISIBLE);
            frameLayoutSpinner.setVisibility(View.GONE);
        }
        header();
        setUpSpinner();
        init();
        buttonSave();
        buttonPrint();
        buttonSigned();
        setButtonEditProducts();
        buttonSaveTechnical();
        setButtonHour();
        footer();
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

    public void init() {
        progressDialog.show();
        productsList = new ArrayList<>();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL + "ordenes/" + idOrder, null, response -> {
            try {
                // Obtener el objeto JSON de la respuesta
                JSONObject data = response;
                detallesArray = data.getJSONArray("detalles");
                clienteNombre = data.getJSONObject("cliente").getString("nombre");
                clienteId = data.getJSONObject("cliente").getInt("id");
                tecnicoNombre = data.getJSONObject("tecnico").getString("nombre");
                sucursalName = data.optJSONObject("sucursal") != null ? data.getJSONObject("sucursal").optString("nombre", null) : null;
                sucursalId = data.optJSONObject("sucursal") != null ? data.getJSONObject("sucursal").optInt("id", -1) : -1;

                fechaHoraSolicitudStr = data.getString("fechaHoraSolicitud");
                fechaHoraLLegadaStr = data.optString("fechaHoraLlegada", "");
                fechaHoraSalidaStr = data.optString("fechaHoraSalida", "");
                firmaUrl = data.optString("firma", "");

                SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
                Date fechaHoraSolicitud = formatoOriginal.parse(fechaHoraSolicitudStr);

                Date fechaHoraLLegada = null;
                Date fechaHoraSalida = null;
                try {
                    fechaHoraLLegada = formatoOriginal.parse(fechaHoraLLegadaStr);
                } catch (ParseException e) {
                    // Handle the exception or log it if necessary
                }
                try {
                    fechaHoraSalida = formatoOriginal.parse(fechaHoraSalidaStr);
                } catch (ParseException e) {
                    Log.e("OrderActivity", "error: " + e.getMessage());
                }

                SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");

                Date fechaActual = new Date();
                String fechaActualStr = formatoFecha.format(fechaActual);
                String horaActualStr = formatoHora.format(fechaActual);

                String soloFecha = formatoFecha.format(fechaHoraSolicitud);
                soloHora = formatoHora.format(fechaHoraSolicitud);

                if (!soloFecha.equals(fechaActualStr) || horaActualStr.compareTo(soloHora) < 0) {
                    buttonPrint.setVisibility(View.GONE);
                    buttonSigned.setVisibility(View.GONE);
                    buttonSaveTechnical.setVisibility(View.GONE);
                    buttonEditProducts.setVisibility(View.GONE);
                    buttonSetHour.setVisibility(View.GONE);
                }

                /*if (fechaActual.after(soloFecha)){
                    buttonPrint.setVisibility(View.GONE);
                }*/

                soloHora = formatoHora.format(fechaHoraSolicitud);
                horaLlegada = (fechaHoraLLegada != null) ? formatoHora.format(fechaHoraLLegada) : "";
                String horaSalida = (fechaHoraSalida != null) ? formatoHora.format(fechaHoraSalida) : "";
                String estatus = data.getString("estatus");
                direccion = data.getString("direccion");
                personaSolicitante = data.getString("persona_solicitante");
                puesto = data.getString("puesto");

                textViewFolio.setText("Folio: " + data.getString("id"));
                textViewDate.setText("Fecha: " + soloFecha);
                textViewHour.setText("Hora: " + soloHora);
                textViewAddress.setText("Dirección: " + direccion);
                textViewCustomer.setText("Cliente: " + clienteNombre);
                textViewTechnical.setText("Tecnico: " + tecnicoNombre);
                textViewApplicant.setText("Persona que solicita: " + personaSolicitante);
                textViewPosition.setText("Puesto: " + puesto);
                textViewStatus.setText("Estatus: " + estatus);
                textViewEntryTime.setText("Hora de llegada: " + horaLlegada);
                textViewDepartureTime.setText("Hora de salida: " + horaSalida);

                if (estatus.equals("Finalizada")) {
                    buttonSetHour.setVisibility(View.GONE);
                    buttonSaveTechnical.setVisibility(View.GONE);
                    buttonEditProducts.setVisibility(View.GONE);
                    buttonSigned.setVisibility(View.GONE);
                    buttonPrint.setVisibility(View.VISIBLE);
                    spinnerStatus.setSelection(1);
                    estatus = "Finalizar";
                } else if (estatus.equals("Autorizada")) {
                    spinnerStatus.setSelection(0);
                    estatus = "Autorizar";
                } else if (estatus.equals("Cancelada")) {
                    spinnerStatus.setSelection(2);
                    estatus = "Cancelar";
                }
                //Log.d("Hora de llegada", horaLlegada);

                if (horaLlegada != null && !horaLlegada.isEmpty() && !horaLlegada.equals("")) {
                    buttonSetHour.setVisibility(View.GONE);
                }

                int defaultPosition = adapter.getPosition(estatus);
                spinnerStatus.setSelection(defaultPosition);
                container.removeAllViews();
                double subtotal = 0.0;
                for (int i = 0; i < detallesArray.length(); i++) {
                    JSONObject detalle = detallesArray.getJSONObject(i);

                    // Obtén el objeto "producto"
                    JSONObject producto = detalle.getJSONObject("producto");

                    // Obtén el nombre del producto
                    String nombre = producto.getString("nombre");
                    int cantidad = detalle.getInt("cantidad");
                    double precio = producto.getDouble("precio");
                    double subTotalPrecio = cantidad * precio;

                    TextView textView = new TextView(this);
                    textView.setText((i + 1) + ".- " + nombre + "  $" + precio + "\nx" + cantidad + "  $" + subTotalPrecio);
                    textView.setTextSize(18);
                    textView.setPadding(16, 16, 16, 16);

                    // Agregar el TextView al contenedor
                    container.addView(textView);
                    // Agrega el nombre a la lista
                    subtotal += subTotalPrecio;
                    productsList.add(new Products(producto.getInt("id"), nombre, producto.getString("descripcion"), cantidad, precio));
                }
                double iva = subtotal * 0.16;
                double total = subtotal + iva;
                textViewSubtotal.setText("Subtotal: $" + String.format("%.2f", subtotal));
                textViewIva.setText("IVA (16%): $" + String.format("%.2f", iva));
                textViewTotal.setText("Total: $" + String.format("%.2f", total));
                loadImage();
                progressDialog.dismiss();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }, error -> {
            try {
                progressDialog.dismiss();
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

    public void setUpSpinner() {
        spinnerStatus = findViewById(R.id.spinnerStatus);
        String[] items = {"Autorizar", "Finalizar", "Cancelar"};

        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinnerStatus.setAdapter(adapter);
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
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, URL + "ordenes/" + apiEstatus + idOrder, jsonObject, response -> {
                try {
                    progressDialog.dismiss();
                    String message = response.getString("msg");
                    Toast.makeText(OrderActivity.this, message, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> {
                try {
                    progressDialog.dismiss();
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
        });
    }

    public void buttonSigned() {
        buttonSigned.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignatureActivity.class);
            intent.putExtra("idOrder", idOrder);
            startActivity(intent);
        });
    }

    public void loadImage() {
        progressDialog.show();
        if (firmaUrl != null && !firmaUrl.isEmpty()) {
            // Make a network request to fetch the image
            ImageRequest imageRequest = new ImageRequest(firmaUrl, response -> {
                // Set the Bitmap to the ImageView
                imageViewSignature.setImageBitmap(response);
                imageViewSignature.setScaleType(ImageView.ScaleType.CENTER_CROP); // Adjust the image to fit the ImageView
            }, 0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.RGB_565, error -> {
                Log.e("ImageView", "Error fetching image: " + error.getMessage());
            });
            progressDialog.dismiss();
            RequestQueue requestQueue = Volley.newRequestQueue(OrderActivity.this);
            requestQueue.add(imageRequest);
            return;
        }
        // Path to the image file
        File imageFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "signatureOrder_" + idOrder + ".png");

        if (imageFile.exists()) {
            Log.i("ImageView", "Image file exists: " + imageFile.getAbsolutePath());
            imageViewSignature.setImageBitmap(BitmapFactory.decodeFile(imageFile.getAbsolutePath()));
            imageViewSignature.setScaleType(ImageView.ScaleType.CENTER_CROP); // Adjust the image to fit the ImageView
        } else {
            Log.i("ImageView", "Image file does not exist: " + imageFile.getAbsolutePath());
            imageViewSignature.setVisibility(View.GONE);
        }
        progressDialog.dismiss();
    }

    public void loadImageOnResume() {
        progressDialog.show();
        // Path to the image file
        File imageFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "signatureOrder_" + idOrder + ".png");

        if (imageFile.exists()) {
            imageViewSignature.setImageBitmap(BitmapFactory.decodeFile(imageFile.getAbsolutePath()));
            imageViewSignature.setScaleType(ImageView.ScaleType.CENTER_CROP); // Adjust the image to fit the ImageView
        } else {
            if (firmaUrl != null && !firmaUrl.isEmpty()) {
                // Make a network request to fetch the image
                ImageRequest imageRequest = new ImageRequest(firmaUrl, response -> {
                    // Set the Bitmap to the ImageView
                    imageViewSignature.setImageBitmap(response);
                    imageViewSignature.setScaleType(ImageView.ScaleType.CENTER_CROP); // Adjust the image to fit the ImageView
                    progressDialog.dismiss();
                }, 0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.RGB_565, error -> {
                    Log.e("ImageView", "Error fetching image: " + error.getMessage());
                    progressDialog.dismiss();
                });

                RequestQueue requestQueue = Volley.newRequestQueue(OrderActivity.this);
                requestQueue.add(imageRequest);
            }
        }
        progressDialog.dismiss();
    }

    public void buttonSaveTechnical() {
        buttonSaveTechnical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(OrderActivity.this)
                        .setTitle("Confirmar")
                        .setMessage("¿Desea finalizar la orden?")
                        .setPositiveButton("Confirmar", (dialog, which) -> {
                            // Acción a realizar si se confirma
                            finalizarOrden();
                        })
                        .setNegativeButton("Cancelar", (dialog, which) -> {
                            // No hacer nada si se cancela
                            dialog.dismiss();
                        })
                        .show();
            }
        });
    }

    private void finalizarOrden() {
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
            Toast.makeText(OrderActivity.this, "Debe seleccionar una hora de llegada", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(OrderActivity.this, "Debe esperar 5 minutos después de la hora de llegada", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();
        // Path to the image file
        File imageFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "signatureOrder_" + idOrder + ".png");

        if (imageFile.exists()) {
            // Send multipart/form-data request
            MultipartRequest multipartRequest = new MultipartRequest(URL + "ordenes/guardarFirma/" + idOrder, error -> {
                try {
                    String responseBody = new String(error.networkResponse.data, "utf-8");
                    JSONObject data = new JSONObject(responseBody);
                    JSONObject errors = data.getJSONObject("errors");
                    JSONArray firmaErrors = errors.getJSONArray("firma");

                    for (int i = 0; i < firmaErrors.length(); i++) {
                        Log.i("ImagenPeticion", "Error: " + firmaErrors.getString(i));
                    }
                    progressDialog.dismiss();
                } catch (UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                }
            }, response -> {
                try {
                    String responseBody = new String(response.data, "utf-8");
                    Log.i("ImagenPeticion", "Response: " + responseBody);
                    if (imageFile.delete()) {
                        Log.i("ImageView", "Image file deleted: " + imageFile.getAbsolutePath());
                    } else {
                        Log.e("ImageView", "Failed to delete image file: " + imageFile.getAbsolutePath());
                    }
                    progressDialog.dismiss();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }, imageFile, "firma");

            RequestQueue requestQueue = Volley.newRequestQueue(OrderActivity.this);
            requestQueue.add(multipartRequest);
        } else {
            //progressBar.setVisibility(View.GONE);
            Log.i("ImageView", "Image file does not exist: " + imageFile.getAbsolutePath());
        }

        if (!requestLocationUpdates()) {
            Toast.makeText(OrderActivity.this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
            return;
        }

        String coorSalida = convertToDMS(latitude, true) + " " + convertToDMS(longitude, false);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(calendar.getTime());

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String fechaHoraSalida = dateTimeFormat.format(calendar.getTime());

        JSONObject order = new JSONObject();
        try {
            order.put("fechaHoraSalida", fechaHoraSalida);
            order.put("|", coorSalida);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        progressDialog.show();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, URL + "ordenes/finalizar/" + idOrder, order, response -> {
            try {
                String message = response.getString("msg");
                Intent intent = new Intent(OrderActivity.this, OrdersActivity.class);
                startActivity(intent);
                finish();
                progressDialog.dismiss();
                Toast.makeText(OrderActivity.this, message, Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            if (error.networkResponse != null) {
                try {
                    String responseBody = new String(error.networkResponse.data, "utf-8");
                    JSONObject data = new JSONObject(responseBody);
                    String message = data.getString("message");
                    Toast.makeText(OrderActivity.this, message, Toast.LENGTH_SHORT).show();
                } catch (UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(OrderActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
        });

        RequestQueue requestQueue = Volley.newRequestQueue(OrderActivity.this);
        requestQueue.add(jsonObjectRequest);
    }

    public void setButtonEditProducts() {
        buttonEditProducts.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProductsActivity.class);
            intent.putExtra("productsList", (ArrayList<Products>) productsList);
            intent.putExtra("idOrder", idOrder);
            intent.putExtra("fechaHoraSolicitud", fechaHoraSolicitudStr);
            intent.putExtra("fechaHoraLLegada", fechaHoraLLegadaStr);
            intent.putExtra("fechaHoraSalida", fechaHoraSalidaStr);
            intent.putExtra("personaSolicitante", personaSolicitante);
            intent.putExtra("direccion", direccion);
            intent.putExtra("puesto", puesto);
            intent.putExtra("clienteId", clienteId);
            intent.putExtra("sucursalId", sucursalId);
            startActivity(intent);
        });
    }

    public void setButtonHour() {
        buttonSetHour.setOnClickListener(v -> {
            new AlertDialog.Builder(OrderActivity.this)
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
        Log.d("SoloHora", soloHora);
        String[] timeParts = soloHora.split(":");
        int requestHour = Integer.parseInt(timeParts[0]);
        int requestMinute = Integer.parseInt(timeParts[1]);

        // Calculate the time 10 minutes after the soloHora
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, requestHour);
        calendar.set(Calendar.MINUTE, requestMinute);
        int limitHour = calendar.get(Calendar.HOUR_OF_DAY);
        int limitMinute = calendar.get(Calendar.MINUTE);

        // Check if the current time is before the request time
        if (currentHour < requestHour || (currentHour == requestHour && currentMinute < requestMinute)) {
            Toast.makeText(OrderActivity.this, "No se puede elegir una hora antes de las " + String.format(Locale.getDefault(), "%02d:%02d", requestHour, requestMinute), Toast.LENGTH_SHORT).show();
        }
        // Check if the current time is within 10 minutes after the request time
        /*else if (currentHour < limitHour || (currentHour == limitHour && currentMinute <= limitMinute)) {
            Toast.makeText(OrderActivity.this, "No se puede elegir una hora dentro de los 10 minutos después de la hora de solicitud", Toast.LENGTH_SHORT).show();
        }*/
        // Set the current time to the textViewHour
        else {
            String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", currentHour, currentMinute);
            updateEntryTime();
            //textViewEntryTime.setText("Hora de llegada: " + selectedTime + ":00");
        }
    }

    public void updateEntryTime() {
        progressDialog.show();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(calendar.getTime());

        if (!requestLocationUpdates()) {
            Toast.makeText(OrderActivity.this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
            return;
        }
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String fechaHoraLlegada = dateTimeFormat.format(calendar.getTime());

        String coorLlegada = convertToDMS(latitude, true) + " " + convertToDMS(longitude, false);

        JSONObject order = new JSONObject();
        try {
            order.put("fechaHoraLlegada", fechaHoraLlegada);
            order.put("coorLlegada", coorLlegada);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL + "ordenes/horaLlegada/" + idOrder, order, response -> {
            progressDialog.dismiss();
            String message = null;
            try {
                message = response.getString("msg");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            Toast.makeText(OrderActivity.this, message, Toast.LENGTH_SHORT).show();
            init();
        }, error -> {
            try {
                String responseBody = new String(error.networkResponse.data, "utf-8");
                JSONObject jsonObject = new JSONObject(responseBody);
                JSONObject errors = jsonObject.getJSONObject("errors");
                for (Iterator<String> it = errors.keys(); it.hasNext(); ) {
                    String key = it.next();
                    JSONArray errorMessages = errors.getJSONArray(key);
                    for (int i = 0; i < errorMessages.length(); i++) {
                        Log.e("Volley Error", key + ": " + errorMessages.getString(i));
                    }
                }
                progressDialog.dismiss();
            } catch (Exception e) {
                Log.e("Volley Error", e.toString());
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    public void buttonPrint() {
        buttonPrint.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestBluetoothPermissions();
                return;
            }

            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                Toast.makeText(this, "Bluetooth no está disponible", Toast.LENGTH_SHORT).show();
                finish();
            }

            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                connectToPrinter();
            }
        });
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

        if (ActivityCompat.checkSelfPermission(OrderActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(OrderActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request the necessary permissions
            ActivityCompat.requestPermissions(OrderActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSIONS);
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
                Toast.makeText(OrderActivity.this, "No se pudo obtener la ubicacion", Toast.LENGTH_SHORT).show();
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

    private void connectToPrinter() {
        try {
            EscPosPrinter printer = new EscPosPrinter(BluetoothPrintersConnections.selectFirstPaired(), 203, 48f, 32);
            double total = 0.0;
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            String currentDate = dateFormat.format(new Date());
            String currentTime = timeFormat.format(new Date());
            String userName = sharedPreferences.getString("userName", null);

            // Construye el texto de impresión dinámicamente
            StringBuilder printText = new StringBuilder();
            printText.append("[C]<img>")
                    .append(PrinterTextParserImg.bitmapToHexadecimalString(printer, this.getApplicationContext().getResources().getDrawableForDensity(R.drawable.logoblanconegro, DisplayMetrics.DENSITY_MEDIUM)))
                    .append("</img>\n")
                    .append("[L]\n")
                    .append("[C]<u><font size='big'>").append(textViewFolio.getText()).append("</font></u>\n")
                    .append("[L]\n")
                    .append("[L]Fecha:").append(currentDate).append("[R]Hora:").append(currentTime).append("\n")
                    .append("[L]\n")
                    .append("[L]Tecnico: ").append(userName).append("\n")
                    .append("[L]\n")
                    .append("[L]Sucursal: ").append(sucursalName).append("\n")
                    .append("[L]\n")
                    .append("[C]================================\n")
                    .append("[L]\n");

            total = 0.0;
            for (int i = 0; i < detallesArray.length(); i++) {
                JSONObject detalle = detallesArray.getJSONObject(i);
                JSONObject producto = detalle.getJSONObject("producto");

                // Obtén los datos del producto
                String nombre = producto.getString("nombre");
                int cantidad = detalle.getInt("cantidad");
                double precio = producto.getDouble("precio");
                double subtotal = precio * cantidad;

                // Agrega la información del producto al texto de impresión
                printText.append("[L]<b>").append(nombre).append("</b>[R]").append(String.format("%.2f", precio)).append("\n")
                        .append("[L]\n")
                        .append("[L]  x").append(cantidad).append("[R]").append(String.format("%.2f", subtotal)).append("\n")
                        .append("[L]\n");

                // Actualiza el total
                total += subtotal;
            }

            double iva = total * 0.16;
            double finalTotal = total + iva;

            printText.append("[C]--------------------------------\n")
                    .append("[L]Subtotal:[R]").append(String.format("%.2f", total)).append("\n")
                    .append("[L]IVA (16%):[R]").append(String.format("%.2f", iva)).append("\n")
                    .append("[L]Total:[R]").append(String.format("%.2f", finalTotal)).append("\n");
            // Imprimir el texto generado
            printer.printFormattedText(printText.toString());
        } catch (EscPosConnectionException | EscPosParserException | EscPosEncodingException |
                 EscPosBarcodeException | JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al imprimir: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN
                    },
                    REQUEST_BLUETOOTH_PERMISSIONS
            );
        }
    }

    private void checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSIONS);
        }
    }

    public void footer() {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, puedes continuar con la conexión
                //connectToPrinter();
            } else {
                // Permiso denegado, muestra un mensaje o maneja la situación
                Toast.makeText(this, "Permiso de Bluetooth denegado", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_LOCATION_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, puedes continuar con las tareas relacionadas con la ubicación
            } else {
                // Permiso denegado, muestra un mensaje
                Toast.makeText(this, "Debe activar los permisos de ubicación para continuar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadImageOnResume(); // Reload data and image when the activity resumes
    }
}
