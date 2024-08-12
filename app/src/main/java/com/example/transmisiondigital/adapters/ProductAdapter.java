package com.example.transmisiondigital.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.transmisiondigital.R;
import com.example.transmisiondigital.models.Products;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Products> productsList;
    private Context context;

    public ProductAdapter(List<Products> productsList, Context context) {
        this.productsList = productsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_element_products, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.productName.setText(productsList.get(position).getName());

        // Remove existing TextWatcher to avoid triggering it during setText
        if (holder.productQuantity.getTag() instanceof TextWatcher) {
            holder.productQuantity.removeTextChangedListener((TextWatcher) holder.productQuantity.getTag());
        }

        holder.productQuantity.setText(String.valueOf(productsList.get(position).getQuantity()));
        holder.productPrice.setText("$" + String.valueOf(productsList.get(position).getPrice()));

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed before text changes
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No action needed during text changes
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int newQuantity = Integer.parseInt(s.toString());
                    productsList.get(position).setQuantity(newQuantity);
                    Log.i("ProductAdapter", "Product quantity updated: " + newQuantity);
                } catch (NumberFormatException e) {
                    // Handle the case where the input is not a valid number
                    Log.e("ProductAdapter", "Invalid quantity input: " + s.toString());
                }
            }
        };

        holder.productQuantity.addTextChangedListener(textWatcher);
        holder.productQuantity.setTag(textWatcher);

        holder.buttonDelete.setOnClickListener(v -> {
            if (position >= 0 && position < productsList.size()) {
                productsList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, productsList.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productQuantity, productPrice;
        Button buttonDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.textViewProductEdit);
            productQuantity = itemView.findViewById(R.id.textViewCantidadEdit);
            productPrice = itemView.findViewById(R.id.textViewPriceEdit);
            buttonDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
