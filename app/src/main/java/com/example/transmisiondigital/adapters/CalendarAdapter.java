package com.example.transmisiondigital.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.transmisiondigital.CalendarActivity;
import com.example.transmisiondigital.OrderActivity;
import com.example.transmisiondigital.R;
import com.example.transmisiondigital.VisitActivity;
import com.example.transmisiondigital.models.Calendar;

import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {
    private List<Calendar> calendarList;
    private Context context;

    public CalendarAdapter(List<Calendar> calendarList, Context context) {
        this.calendarList = calendarList;
        this.context = context;
    }

    @NonNull
    @Override
    public CalendarAdapter.CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_element_calendar, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarAdapter.CalendarViewHolder holder, int position) {
        Calendar calendar = calendarList.get(position);
        holder.tvFolio.setText("Folio: "+ calendar.getFolio());
        holder.tvHour.setText("Hora: " +calendar.getHour());
        holder.tvStatus.setText("Estatus: "+ calendar.getStatus());
        holder.tvDate.setText("Fecha: " + calendar.getDate().toString());
        holder.btnDetails.setOnClickListener(v -> {
            Intent intent;
            String selectedItem = calendar.getSelectedItem();
            if(selectedItem.equals("ordenes")){
                intent = new Intent(v.getContext(), OrderActivity.class);
                intent.putExtra("idOrder", calendar.getId());
            }else{
                intent = new Intent(v.getContext(), VisitActivity.class);
                intent.putExtra("idVisit", calendar.getId());
            }

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return calendarList.size();
    }

    public static class CalendarViewHolder extends RecyclerView.ViewHolder {
        Button btnDetails;
        TextView tvFolio, tvHour, tvStatus, tvDate;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFolio = itemView.findViewById(R.id.textViewFolio);
            tvHour = itemView.findViewById(R.id.textViewHour);
            tvStatus = itemView.findViewById(R.id.textViewStatus);
            tvDate = itemView.findViewById(R.id.textViewDate);
            btnDetails = itemView.findViewById(R.id.btnDetails);
        }

        public void bind(Calendar calendar) {
            tvFolio.setText(calendar.getFolio());
            tvHour.setText(calendar.getHour());
            tvStatus.setText(calendar.getStatus());
            tvDate.setText(calendar.getDate());
        }
    }
}
