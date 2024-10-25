package com.example.plantas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.ArrayAdapter;

import java.util.List;

public class ComentarioAdapter extends ArrayAdapter<String> {

    private List<String> comentarios;

    public ComentarioAdapter(@NonNull Context context, List<String> comentarios) {
        super(context, R.layout.item_comentario, comentarios); // item_comentario es el layout para cada comentario
        this.comentarios = comentarios;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        // Usar ViewHolder para mejorar el rendimiento
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_comentario, parent, false);
            holder = new ViewHolder();
            holder.textViewComentario = convertView.findViewById(R.id.text_comentario);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Obtener el comentario actual
        String comentario = getItem(position);

        // Asignar el comentario al TextView
        if (comentario != null) {
            holder.textViewComentario.setText(comentario);
        }

        return convertView;
    }

    // Clase ViewHolder para mejorar el rendimiento
    private static class ViewHolder {
        TextView textViewComentario;
    }
}
