package com.example.transmisiondigital;

import static com.example.transmisiondigital.globalVariables.Conexion.URL;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.transmisiondigital.adapters.OrderAdapter;
import com.example.transmisiondigital.adapters.ProductAdapter;
import com.example.transmisiondigital.adapters.ProductDialogAdapter;
import com.example.transmisiondigital.models.Products;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.transmisiondigital.databinding.ActivityProductsBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

public class ProductsActivity extends AppCompatActivity {

    private String idOrder, fechaHoraSolicitudStr, fechaHoraLLegadaStr, fechaHoraSalidaStr, personaSolicitante, direccion, puesto, clienteId, sucursalId;
    private SharedPreferences sharedPreferences;
    private Button buttonSaveProducts, buttonAddProduct;
    private ArrayList<Products> productsList;
    private ProductAdapter productAdapter;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);
        buttonAddProduct = findViewById(R.id.buttonAddProduct);
        buttonSaveProducts = findViewById(R.id.buttonSaveProducts);
        sharedPreferences = getSharedPreferences("sessionUser", Context.MODE_PRIVATE);
        Intent intent = getIntent();
        idOrder = intent.getStringExtra("idOrder");
        fechaHoraSolicitudStr = intent.getStringExtra("fechaHoraSolicitud");
        fechaHoraLLegadaStr = intent.getStringExtra("fechaHoraLLegada");
        //fechaHoraLLegadaStr = (fechaHoraLLegadaStr != null && fechaHoraLLegadaStr.isEmpty()) ? null : fechaHoraLLegadaStr;
        fechaHoraSalidaStr = intent.getStringExtra("fechaHoraSalida");
        //fechaHoraSalidaStr = (fechaHoraSalidaStr != null && fechaHoraSalidaStr.isEmpty()) ? null : fechaHoraSalidaStr;
        personaSolicitante = intent.getStringExtra("personaSolicitante");
        direccion = intent.getStringExtra("direccion");
        puesto = intent.getStringExtra("puesto");
        clienteId = String.valueOf(intent.getIntExtra("clienteId", 0));
        sucursalId = String.valueOf(intent.getIntExtra("sucursalId", 0));
        productsList = (ArrayList<Products>) intent.getSerializableExtra("productsList");
        if (productsList == null) {
            productsList = new ArrayList<>();
        }
        TextView textViewTitle = findViewById(R.id.textViewTitle);
        textViewTitle.setText("PRODUCTOS");
        RecyclerView recyclerView = findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(ProductsActivity.this));
        productAdapter = new ProductAdapter(productsList, ProductsActivity.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(productAdapter);
        buttonAddProduct.setOnClickListener(v -> showProductSelectionDialog());
        buttonSaveProducts.setOnClickListener(v -> setButtonSaveProducts());
        progressDialog = new ProgressDialog(ProductsActivity.this);
        progressDialog.setMessage("Cargando...");
        header();
        footer();
    }

    public void showProductSelectionDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_product_selection);

        // Set the dialog to use a larger portion of the screen
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        RecyclerView recyclerViewDialogProducts = dialog.findViewById(R.id.recyclerViewDialogProducts);
        recyclerViewDialogProducts.setLayoutManager(new LinearLayoutManager(this));

        getAllProducts(products -> {
            ProductDialogAdapter dialogAdapter = new ProductDialogAdapter(products);
            recyclerViewDialogProducts.setAdapter(dialogAdapter);

            Button buttonAddSelectedProducts = dialog.findViewById(R.id.buttonAddSelectedProducts);
            buttonAddSelectedProducts.setOnClickListener(v -> {
                ArrayList<Products> selectedProducts = dialogAdapter.getSelectedProducts();
                for (Products selectedProduct : selectedProducts) {
                    boolean exists = false;
                    for (Products p : productsList) {
                        if (p.getId() == selectedProduct.getId()) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        selectedProduct.setQuantity(1); // Set quantity to 1 for new products
                        productsList.add(selectedProduct);
                    }
                }
                productAdapter.notifyDataSetChanged();
                dialog.dismiss();
            });

            dialog.show();
        });
    }

    public void setButtonSaveProducts() {
        progressDialog.show();
        JSONObject order = new JSONObject();
        try {
            if (productsList == null || productsList.isEmpty()) {
                progressDialog.dismiss();
                Log.d("Order JSON", "Debes de ingresar al menos un producto");
                Toast.makeText(ProductsActivity.this, "Debes de ingresar al menos un producto", Toast.LENGTH_SHORT).show();
                return;
            }

            for (Products product : productsList) {
                if (product.getQuantity() < 1) {
                    progressDialog.dismiss();
                    Toast.makeText(ProductsActivity.this, "La cantidad para el producto " + product.getName() + " debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                    Log.e("ProductsActivity", "Quantity for product " + product.getName() + " was less than 1 and has been set to 1");
                    return;
                }
            }

            order.put("fechaHoraSolicitud", fechaHoraSolicitudStr != null && !fechaHoraSolicitudStr.isEmpty() ? fechaHoraSolicitudStr : JSONObject.NULL);
            order.put("fechaHoraLlegada", fechaHoraLLegadaStr != null && !fechaHoraLLegadaStr.isEmpty() && !"null".equals(fechaHoraLLegadaStr) ? fechaHoraLLegadaStr : JSONObject.NULL);
            order.put("fechaHoraSalida", fechaHoraSalidaStr != null && !fechaHoraSalidaStr.isEmpty() && !"null".equals(fechaHoraSalidaStr) ? fechaHoraSalidaStr : JSONObject.NULL);
            order.put("persona_solicitante", personaSolicitante);
            order.put("direccion", direccion);
            order.put("puesto", puesto);
            order.put("cliente_id", clienteId);
            order.put("sucursal_id", sucursalId);
            order.put("tecnico_id", sharedPreferences.getInt("idUser", 0));
            JSONArray products = new JSONArray();
            for (Products p : productsList) {
                JSONObject product = new JSONObject();
                product.put("cantidad", String.valueOf(p.getQuantity()));
                product.put("descripcion", "Editar");
                product.put("producto_id", p.getId());
                products.put(product);
            }
            order.put("detalles", products);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, URL + "ordenes/" + idOrder, order, response -> {
            try {
                progressDialog.dismiss();
                String msg = response.getString("msg");
                Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).show();
                Intent intent = new Intent(ProductsActivity.this, OrderActivity.class);
                intent.putExtra("idOrder", idOrder);
                startActivity(intent);
                finish();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            try {
                progressDialog.dismiss();
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
            } catch (Exception e) {
                Log.e("Volley Error", e.toString());
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    public void header() {
        SharedPreferences sharedPreferences = getSharedPreferences("sessionUser", Context.MODE_PRIVATE);
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

    public void getAllProducts(ProductsCallback callback) {
        progressDialog.show();
        ArrayList<Products> products = new ArrayList<>();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL + "productos", null, response -> {
            try {
                JSONArray dataArray = response.getJSONArray("data");
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject productObject = dataArray.getJSONObject(i);
                    int productId = productObject.getInt("id");

                    boolean exists = false;
                    for (Products p : productsList) {
                        if (p.getId() == productId) {
                            exists = true;
                            break;
                        }
                    }

                    if (!exists) {
                        Products product = new Products(
                                productId,
                                productObject.getString("nombre"),
                                productObject.getString("descripcion"),
                                1,
                                productObject.getDouble("precio")
                        );
                        products.add(product);
                    }
                }
                callback.onSuccess(products);
                progressDialog.dismiss();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            progressDialog.dismiss();
            Toast.makeText(ProductsActivity.this, "Error al cargar los productos", Toast.LENGTH_SHORT).show();
            Log.e("Volley Error", error.toString());
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    public interface ProductsCallback {
        void onSuccess(ArrayList<Products> products);
    }
}
