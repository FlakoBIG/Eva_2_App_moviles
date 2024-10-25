package com.example.plantas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotaAdapter extends RecyclerView.Adapter<NotaAdapter.NotaViewHolder> {
    private List<String> listaNotas;

    public NotaAdapter(List<String> listaNotas) {
        this.listaNotas = listaNotas;
    }

    @NonNull
    @Override
    public NotaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new NotaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotaViewHolder holder, int position) {
        if (position < listaNotas.size()) {
            holder.textViewNota.setText(listaNotas.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return listaNotas != null ? listaNotas.size() : 0;
    }

    static class NotaViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNota;

        public NotaViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNota = itemView.findViewById(android.R.id.text1);
        }
    }
}
