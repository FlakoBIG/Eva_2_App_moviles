package com.example.plantas;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences; // Importa SharedPreferences
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class PlantasAdapter extends RecyclerView.Adapter<PlantasAdapter.PlantaViewHolder> {

    private List<Planta> plantas;
    private Context context;

    public PlantasAdapter(List<Planta> plantas, Context context) {
        this.plantas = plantas;
        this.context = context;
    }

    @NonNull
    @Override
    public PlantaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_planta, parent, false);
        return new PlantaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantaViewHolder holder, int position) {
        Planta planta = plantas.get(position);
        holder.nombreTextView.setText("Nombre: " + planta.getNombre());
        holder.fechaTextView.setText("Fecha de plantación: " + planta.getFecha_plantacion());

        // Cargar imagen con Glide
        String fotoUrl = planta.getFoto_principal();
        Glide.with(context)
                .load(fotoUrl)
                .into(holder.fotoImageView);

        // Establecer un OnClickListener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, View_plantita.class); // Cambia esto por la clase que muestra los detalles.
            intent.putExtra("plantaId", planta.getId()); // Asegúrate de que tienes un método getId() en tu clase Planta.

            // Guardar plantaId en SharedPreferences
            SharedPreferences preferences = context.getSharedPreferences("PlantaPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("plantaId", planta.getId());
            editor.apply();

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return plantas.size();
    }

    public static class PlantaViewHolder extends RecyclerView.ViewHolder {
        TextView nombreTextView, fechaTextView;
        ImageView fotoImageView;

        public PlantaViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreTextView = itemView.findViewById(R.id.nombre_planta);
            fechaTextView = itemView.findViewById(R.id.fecha_plantacion);
            fotoImageView = itemView.findViewById(R.id.foto_planta);
        }
    }
}
