package com.example.transmisiondigital.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.transmisiondigital.R;
import com.example.transmisiondigital.models.Products;

import java.util.ArrayList;

public class ProductDialogAdapter extends RecyclerView.Adapter<ProductDialogAdapter.ViewHolder> {

    private ArrayList<Products> productsList;
    private ArrayList<Products> selectedProducts = new ArrayList<>();

    public ProductDialogAdapter(ArrayList<Products> productsList) {
        this.productsList = productsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_dialog, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Products product = productsList.get(position);
        holder.textViewProductName.setText(product.getName());
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedProducts.add(product);
            } else {
                selectedProducts.remove(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    public ArrayList<Products> getSelectedProducts() {
        return selectedProducts;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewProductName;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}
