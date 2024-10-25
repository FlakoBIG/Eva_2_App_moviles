package com.example.plantas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class ProblemaAdapter extends RecyclerView.Adapter<ProblemaAdapter.ProblemaViewHolder> {
    private List<Map<String, String>> listaProblemas;

    public ProblemaAdapter(List<Map<String, String>> listaProblemas) {
        this.listaProblemas = listaProblemas;
    }

    @NonNull
    @Override
    public ProblemaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ProblemaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProblemaViewHolder holder, int position) {
        Map<String, String> registro = listaProblemas.get(position);

        // Modificación para mostrar el texto con el formato requerido
        String problema = registro.get("problema") != null ? registro.get("problema") : "No disponible";
        String solucion = registro.get("solucion") != null ? registro.get("solucion") : "No disponible";

        holder.textViewProblema.setText("Problema: " + problema);
        holder.textViewSolucion.setText("Solución: " + solucion);
    }

    @Override
    public int getItemCount() {
        return listaProblemas.size();
    }

    static class ProblemaViewHolder extends RecyclerView.ViewHolder {
        TextView textViewProblema;
        TextView textViewSolucion;

        public ProblemaViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProblema = itemView.findViewById(android.R.id.text1);
            textViewSolucion = itemView.findViewById(android.R.id.text2);
        }
    }
}
