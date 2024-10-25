package com.example.plantas;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Foto_amigo_Adapter extends RecyclerView.Adapter<Foto_amigo_Adapter.FotoViewHolder> {

    private List<String> fotoUrls; // Lista de URLs de fotos
    private List<String> documentIds; // Lista de IDs de documentos de Firestore
    private List<Integer> listaLikes; // Lista de likes
    private Context context;
    private FirebaseFirestore db;
    private String amigoUid; // Agregar amigoUid

    public Foto_amigo_Adapter(Context context, List<String> fotoUrls, List<String> documentIds, List<Integer> listaLikes, String amigoUid) {
        this.context = context;
        this.fotoUrls = fotoUrls;
        this.documentIds = documentIds; // Agrega la lista de IDs de documentos
        this.listaLikes = listaLikes; // Inicializar la lista de likes
        this.amigoUid = amigoUid; // Inicializar amigoUid
        db = FirebaseFirestore.getInstance();
    }

    // Método para establecer el UID del amigo
    public void setAmigoUid(String amigoUid) {
        this.amigoUid = amigoUid;
    }

    @NonNull
    @Override
    public FotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_foto_amigo, parent, false);
        return new FotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FotoViewHolder holder, int position) {
        String url = fotoUrls.get(position);
        Glide.with(context).load(url).into(holder.imageView);

        // Obtener el ID del documento
        String documentId = documentIds.get(position);
        DocumentReference photoRef = db.collection("fotos").document(documentId);

        // Mostrar el número de "likes" desde la lista
        holder.tvLikes.setText(String.valueOf(listaLikes.get(position)));

        // Click en la imagen para mostrar detalles de la foto
        holder.imageView.setOnClickListener(v -> {
            Intent intent = new Intent(context, Foto_amigo_planta_seleccionada.class);
            intent.putStringArrayListExtra("fotos", new ArrayList<>(fotoUrls));
            intent.putExtra("current_index", position);
            intent.putExtra("amigoUid", amigoUid); // Pasar el amigoUid
            intent.putExtra("documentId", documentId); // Pasar el documentId correspondiente
            context.startActivity(intent);
        });

        // Manejo del botón "like"
        holder.btnLike.setOnClickListener(v -> {
            photoRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    int likesCount = documentSnapshot.getLong("likes") != null ? documentSnapshot.getLong("likes").intValue() : 0;

                    // Cambiar el estado del like
                    if (holder.isLiked) {
                        holder.isLiked = false;
                        holder.btnLike.setImageResource(R.drawable.corazon_sin_relleno);
                        photoRef.update("likes", likesCount - 1).addOnSuccessListener(aVoid -> {
                            // Actualizar la cantidad de likes en la vista
                            holder.tvLikes.setText(String.valueOf(likesCount - 1));
                        });
                    } else {
                        holder.isLiked = true;
                        holder.btnLike.setImageResource(R.drawable.corazon_relleno);
                        photoRef.update("likes", likesCount + 1).addOnSuccessListener(aVoid -> {
                            // Actualizar la cantidad de likes en la vista
                            holder.tvLikes.setText(String.valueOf(likesCount + 1));
                        });
                    }
                }
            }).addOnFailureListener(e -> {
                Log.e("FirestoreError", "Error updating likes count", e);
            });
        });
    }

    @Override
    public int getItemCount() {
        return fotoUrls.size();
    }

    public static class FotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton btnLike;
        TextView tvLikes;
        boolean isLiked = false; // Estado del like

        public FotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            btnLike = itemView.findViewById(R.id.btn_like);
            tvLikes = itemView.findViewById(R.id.tv_likes);
        }
    }
}
