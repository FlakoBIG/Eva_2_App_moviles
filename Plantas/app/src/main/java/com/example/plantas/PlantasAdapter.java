package com.example.plantas;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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
        holder.nombreTextView.setText(planta.getNombre());
        holder.fechaTextView.setText(planta.getFechaPlantacion());

        // Cargar imagen con Glide
        Glide.with(context).load(planta.getFotoPrincipal()).into(holder.fotoImageView);
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
