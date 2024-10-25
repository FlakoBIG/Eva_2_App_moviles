package com.example.plantas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ListaPlantasFragment extends BottomSheetDialogFragment {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> plantas;
    private OnPlantaSeleccionadaListener listener;

    public ListaPlantasFragment(OnPlantaSeleccionadaListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lista_plantas, container, false);

        listView = view.findViewById(R.id.listView_plantas);
        plantas = new ArrayList<>();
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, plantas);
        listView.setAdapter(adapter);

        cargarPlantas();

        // Manejar la selección de una planta
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            String plantaSeleccionada = plantas.get(position);
            if (listener != null) {
                listener.onPlantaSeleccionada(plantaSeleccionada);
            }
            dismiss(); // Cierra el fragmento después de seleccionar
        });

        return view;
    }

    private void cargarPlantas() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("info_planta").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String plantaNombre = document.getString("nombre"); // Cambia segnn tu estructura
                            plantas.add(plantaNombre);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Error al cargar plantas", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public interface OnPlantaSeleccionadaListener {
        void onPlantaSeleccionada(String nombrePlanta);
    }
}
