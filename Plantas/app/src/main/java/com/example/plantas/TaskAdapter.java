package com.example.plantas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import androidx.annotation.Nullable;
import java.util.List;

public class TaskAdapter extends ArrayAdapter<Task> {
    private final List<Task> tareas;
    private final AgregarTareaActivity activity;

    public TaskAdapter(@NonNull Context context, List<Task> tareas) {
        super(context, 0, tareas);
        this.tareas = tareas;
        this.activity = (AgregarTareaActivity) context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_tarea, parent, false);
        }

        Task tarea = getItem(position);

        TextView tvNombreTarea = convertView.findViewById(R.id.tv_tarea_nombre);
        Button btnEditar = convertView.findViewById(R.id.btn_editar_tarea);
        Button btnEliminar = convertView.findViewById(R.id.btn_eliminar_tarea);

        tvNombreTarea.setText(tarea.getNombre());

        btnEditar.setOnClickListener(v -> activity.editarTarea(tarea));
        btnEliminar.setOnClickListener(v -> activity.eliminarTarea(tarea));

        return convertView;
    }
}
