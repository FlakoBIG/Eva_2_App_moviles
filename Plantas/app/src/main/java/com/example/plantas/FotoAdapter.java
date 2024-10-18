package com.example.plantas;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import java.util.ArrayList;


public class FotoAdapter extends RecyclerView.Adapter<FotoAdapter.FotoViewHolder> {

    private List<String> fotoUrls;
    private Context context;

    public FotoAdapter(Context context, List<String> fotoUrls) {
        this.context = context;
        this.fotoUrls = fotoUrls;
    }

    @NonNull
    @Override
    public FotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_foto, parent, false);
        return new FotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FotoViewHolder holder, int position) {
        String url = fotoUrls.get(position);
        Glide.with(context).load(url).into(holder.imageView);

        // Configurar el listener para abrir la nueva actividad al hacer clic
        holder.imageView.setOnClickListener(v -> {
            Intent intent = new Intent(context, Foto_planta_seleccionada.class);
            intent.putStringArrayListExtra("fotos", new ArrayList<>(fotoUrls)); // Pasar la lista de fotos
            intent.putExtra("current_index", position); // Pasar el índice actual
            context.startActivity(intent);  // Iniciar la nueva actividad
        });
    }

    @Override
    public int getItemCount() {
        return fotoUrls.size();
    }

    public static class FotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public FotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
        }
    }
}
