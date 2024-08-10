package com.example.transmisiondigital;

import static com.example.transmisiondigital.globalVariables.Conexion.URL;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
import com.example.transmisiondigital.request.MultipartRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OrderActivity extends AppCompatActivity {

    private String idOrder, sucursalName, firmaUrl;
    private SharedPreferences sharedPreferences;
    private TextView textViewFolio, textViewDate, textViewHour, textViewAddress, textViewCustomer;
    private LinearLayout container;
    private TextView textViewTechnical, textViewApplicant, textViewPosition, textViewStatus, textViewEntryTime, textViewDepartureTime, textViewTotal;
    private Spinner spinnerStatus;
    private Button buttonSave, buttonPrint, buttonSigned, buttonSaveTechnical;
    private ImageView imageViewSignature;
    private ArrayAdapter<String> adapter;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 2;
    private BluetoothAdapter bluetoothAdapter;
    private JSONArray detallesArray;
    private ProgressBar progressBar;

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
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        if (sharedPreferences.getString("rol", null).equals("Técnico")) {
            spinnerStatus.setVisibility(View.GONE);
            buttonSave.setVisibility(View.GONE);
            buttonPrint.setVisibility(View.VISIBLE);
            buttonSigned.setVisibility(View.VISIBLE);
            imageViewSignature.setVisibility(View.VISIBLE);
            buttonSaveTechnical.setVisibility(View.VISIBLE);
        }
        spinnerStatus = findViewById(R.id.spinnerStatus);
        String[] items = {"Autorizar", "Finalizar", "Cancelar"};

        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinnerStatus.setAdapter(adapter);

        init();
        buttonSave();
        buttonPrint();
        buttonSigned();
        buttonSaveTechnical();
        footer();
    }

    public void init() {
        progressBar.setVisibility(View.VISIBLE);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL + "ordenes/" + idOrder, null, response -> {
            try {
                // Obtener el objeto JSON de la respuesta
                JSONObject data = response;
                detallesArray = data.getJSONArray("detalles");
                String clienteNombre = data.getJSONObject("cliente").getString("nombre");
                String tecnicoNombre = data.getJSONObject("tecnico").getString("nombre");
                sucursalName = data.getJSONObject("sucursal").getString("nombre");

                String fechaHoraSolicitudStr = data.getString("fechaHoraSolicitud");
                String fechaHoraLLegadaStr = data.optString("fechaHoraLlegada", "");
                String fechaHoraSalidaStr = data.optString("fechaHoraSalida", "");
                firmaUrl = data.optString("firma", "");
                Log.i("firmaUrl", "firmaUrl: " + firmaUrl);

                SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
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

                // Formatear a strings de fecha y hora
                String soloFecha = formatoFecha.format(fechaHoraSolicitud);
                String soloHora = formatoHora.format(fechaHoraSolicitud);
                String horaLLegada = (fechaHoraLLegada != null) ? formatoHora.format(fechaHoraLLegada) : "";
                String horaSalida = (fechaHoraSalida != null) ? formatoHora.format(fechaHoraSalida) : "";
                String estatus = data.getString("estatus");

                textViewFolio.setText("Folio: " + data.getString("id"));
                textViewDate.setText("Fecha: " + soloFecha);
                textViewHour.setText("Hora: " + soloHora);
                textViewAddress.setText("Dirección: " + data.getString("direccion"));
                textViewCustomer.setText("Cliente: " + clienteNombre);
                textViewTechnical.setText("Tecnico: " + tecnicoNombre);
                textViewApplicant.setText("Persona que solicita: " + data.getString("persona_solicitante"));
                textViewPosition.setText("Puesto: " + data.getString("puesto"));
                textViewStatus.setText("Estatus: " + estatus);
                textViewEntryTime.setText("Hora de llegada: " + horaLLegada);
                textViewDepartureTime.setText("Hora de salida: " + horaSalida);

                if (estatus.equals("Finalizada")) {
                    estatus = "Finalizar";
                } else if (estatus.equals("Autorizada")) {
                    estatus = "Autorizar";
                } else if (estatus.equals("Cancelada")) {
                    estatus = "Cancelar";
                }

                int defaultPosition = adapter.getPosition(estatus);
                spinnerStatus.setSelection(defaultPosition);

                double total = 0.0;
                for (int i = 0; i < detallesArray.length(); i++) {
                    JSONObject detalle = detallesArray.getJSONObject(i);

                    // Obtén el objeto "producto"
                    JSONObject producto = detalle.getJSONObject("producto");

                    // Obtén el nombre del producto
                    String nombre = producto.getString("nombre");
                    int cantidad = detalle.getInt("cantidad");
                    double precio = producto.getDouble("precio");

                    TextView textView = new TextView(this);
                    textView.setText((i + 1) + ".- " + nombre + " x" + cantidad + " $" + precio);
                    textView.setTextSize(18);
                    textView.setPadding(16, 16, 16, 16);

                    // Agregar el TextView al contenedor
                    container.addView(textView);
                    // Agrega el nombre a la lista
                    total += cantidad * precio;
                }
                textViewTotal.setText("Total: $" + total);
                loadImage();
                progressBar.setVisibility(View.GONE);
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
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OrderActivity.this, message, Toast.LENGTH_SHORT).show();
            } catch (UnsupportedEncodingException | JSONException e) {
                e.printStackTrace();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(OrderActivity.this);
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
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, URL + "ordenes/" + apiEstatus + idOrder, jsonObject, response -> {
                try {
                    String message = response.getString("msg");
                    Toast.makeText(OrderActivity.this, message, Toast.LENGTH_SHORT).show();
                    // Create a Handler to introduce a delay
                    Handler handler = new Handler(Looper.getMainLooper());

                    // Post a Runnable with a delay of 2 seconds (2000 milliseconds)
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            init();
                        }
                    }, 2000);
                } catch (JSONException e) {
                    e.printStackTrace();
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
        });
    }

    public void buttonSigned() {
        buttonSigned.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignatureActivity.class);
            intent.putExtra("idOrder", idOrder);
            Log.i("idOrder", "idOrder: " + idOrder);
            startActivity(intent);
        });
    }

    public void loadImage() {
        Log.i("ImageView", "firma: " + firmaUrl);
        if (firmaUrl != null && !firmaUrl.isEmpty()) {
            // Make a network request to fetch the image
            ImageRequest imageRequest = new ImageRequest(firmaUrl, response -> {
                // Set the Bitmap to the ImageView
                imageViewSignature.setImageBitmap(response);
                imageViewSignature.setScaleType(ImageView.ScaleType.CENTER_CROP); // Adjust the image to fit the ImageView
            }, 0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.RGB_565, error -> {
                Log.e("ImageView", "Error fetching image: " + error.getMessage());
            });

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
    }

    public void loadImageOnResume() {
        // Path to the image file
        File imageFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "signatureOrder_" + idOrder + ".png");

        if (imageFile.exists()) {
            Log.i("ImageView", "Image file exists: " + imageFile.getAbsolutePath());
            imageViewSignature.setImageBitmap(BitmapFactory.decodeFile(imageFile.getAbsolutePath()));
            imageViewSignature.setScaleType(ImageView.ScaleType.CENTER_CROP); // Adjust the image to fit the ImageView
        } else {
            //Log.i("ImageView", "Image file does not exist: " + imageFile.getAbsolutePath());
            //imageViewSignature.setVisibility(View.GONE);
            if (firmaUrl != null && !firmaUrl.isEmpty()) {
                // Make a network request to fetch the image
                ImageRequest imageRequest = new ImageRequest(firmaUrl, response -> {
                    // Set the Bitmap to the ImageView
                    imageViewSignature.setImageBitmap(response);
                    imageViewSignature.setScaleType(ImageView.ScaleType.CENTER_CROP); // Adjust the image to fit the ImageView
                }, 0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.RGB_565, error -> {
                    Log.e("ImageView", "Error fetching image: " + error.getMessage());
                });

                RequestQueue requestQueue = Volley.newRequestQueue(OrderActivity.this);
                requestQueue.add(imageRequest);
            }
        }
    }

    public void buttonSaveTechnical() {
    buttonSaveTechnical.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            progressBar.setVisibility(View.VISIBLE);
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
                        progressBar.setVisibility(View.GONE);
                    } catch (UnsupportedEncodingException | JSONException e) {
                        e.printStackTrace();
                    }
                }, response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        String responseBody = new String(response.data, "utf-8");
                        Log.i("ImagenPeticion", "Response: " + responseBody);

                        // Delete the image file after successful upload
                        if (imageFile.delete()) {
                            Log.i("ImageView", "Image file deleted: " + imageFile.getAbsolutePath());
                        } else {
                            Log.e("ImageView", "Failed to delete image file: " + imageFile.getAbsolutePath());
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }, imageFile, "firma");

                RequestQueue requestQueue = Volley.newRequestQueue(OrderActivity.this);
                requestQueue.add(multipartRequest);
            } else {
                progressBar.setVisibility(View.GONE);
                Log.i("ImageView", "Image file does not exist: " + imageFile.getAbsolutePath());
            }

            progressBar.setVisibility(View.VISIBLE);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, URL + "ordenes/finalizar/" + idOrder, null, response -> {
                try {
                    String message = response.getString("msg");
                    Toast.makeText(OrderActivity.this, message, Toast.LENGTH_SHORT).show();
                    // Create a Handler to introduce a delay
                    Handler handler = new Handler(Looper.getMainLooper());

                    progressBar.setVisibility(View.GONE);
                    // Post a Runnable with a delay of 2 seconds (2000 milliseconds)
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            init();
                        }
                    }, 2000);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> {
                progressBar.setVisibility(View.GONE);
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
            });

            RequestQueue requestQueue = Volley.newRequestQueue(OrderActivity.this);
            requestQueue.add(jsonObjectRequest);
        }
    });
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

            for (int i = 0; i < detallesArray.length(); i++) {
                JSONObject detalle = detallesArray.getJSONObject(i);
                JSONObject producto = detalle.getJSONObject("producto");

                // Obtén los datos del producto
                String nombre = producto.getString("nombre");
                int cantidad = detalle.getInt("cantidad");
                double precio = producto.getDouble("precio");

                // Agrega la información del producto al texto de impresión
                printText.append("[L]<b>").append(nombre).append("</b>[R]").append(precio).append("\n")
                        .append("[L]  x").append(cantidad).append("\n")
                        .append("[L]\n");

                // Actualiza el total
                total += cantidad * precio;
            }

            printText.append("[C]--------------------------------\n")
                    .append("[R]TOTAL PRICE :[R]").append(String.format("%.2f", total)).append("\n");

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
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadImageOnResume(); // Reload data and image when the activity resumes
    }
}
