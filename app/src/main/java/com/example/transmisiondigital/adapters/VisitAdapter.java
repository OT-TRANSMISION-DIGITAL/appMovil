package com.example.transmisiondigital.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.transmisiondigital.OrderActivity;
import com.example.transmisiondigital.R;
import com.example.transmisiondigital.VisitActivity;
import com.example.transmisiondigital.models.Visits;

import java.util.List;

public class VisitAdapter extends RecyclerView.Adapter<VisitAdapter.VisitViewHolder>{
    private List<Visits> visitList;
    private Context context;

    public VisitAdapter(List<Visits> visitList, Context context){
        this.visitList = visitList;
        this.context = context;
    }

    @NonNull
    @Override
    public VisitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_element_visits, parent, false);
        return new VisitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VisitAdapter.VisitViewHolder holder, int position) {
        Visits visit = visitList.get(position);
        holder.tvFolio.setText("Folio: " + visit.getFolio());
        holder.tvHour.setText("Hora: " + visit.getHour());
        holder.tvStatus.setText("Estatus: " + visit.getStatus());
        holder.tvDate.setText("Fecha: " + visit.getDate().toString());

        holder.btnDetails.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), VisitActivity.class);
            intent.putExtra("idVisit", visit.getIdOrder());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return visitList.size();
    }

    public static class VisitViewHolder extends RecyclerView.ViewHolder {
        Button btnDetails;
        TextView tvFolio, tvHour, tvStatus, tvDate;

        public VisitViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFolio = itemView.findViewById(R.id.textViewFolio);
            tvHour = itemView.findViewById(R.id.textViewHour);
            tvStatus = itemView.findViewById(R.id.textViewStatus);
            tvDate = itemView.findViewById(R.id.textViewDate);
            btnDetails = itemView.findViewById(R.id.btnDetails);
        }

        void onBindData(final Visits visit){
            tvFolio.setText(visit.getFolio());
            tvHour.setText(visit.getHour());
            tvStatus.setText(visit.getStatus());
            tvDate.setText(visit.getDate());
        }
    }
}
