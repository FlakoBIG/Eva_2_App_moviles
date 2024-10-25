package com.example.plantas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.List;

public class ComentariosDialog extends BottomSheetDialogFragment {

    private ListView listViewComentarios;
    private EditText editComentario;
    private Button btnEnviarComentario;
    private FirebaseFirestore db;
    private String documentId;
    private String amigoUid;

    public ComentariosDialog(String documentId, String amigoUid) {
        this.documentId = documentId;
        this.amigoUid = amigoUid;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_comentarios, container, false);

        listViewComentarios = view.findViewById(R.id.list_comentarios);
        editComentario = view.findViewById(R.id.edit_comentario);
        btnEnviarComentario = view.findViewById(R.id.btn_enviar);
        db = FirebaseFirestore.getInstance();

        // Cargar los comentarios existentes
        cargarComentarios();

        // Configurar el botón para enviar un nuevo comentario
        btnEnviarComentario.setOnClickListener(v -> enviarComentario());

        return view;
    }

    private void cargarComentarios() {
        DocumentReference docRef = db.collection(amigoUid)
                .document("plantas")
                .collection("Fotos_publica")
                .document(documentId);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.get("comentarios") != null) {
                List<String> comentarios = (List<String>) documentSnapshot.get("comentarios");
                // Configurar adaptador para mostrar los comentarios en el ListView
                ComentarioAdapter adapter = new ComentarioAdapter(getContext(), comentarios);
                listViewComentarios.setAdapter(adapter);
            }
        }).addOnFailureListener(e -> {
            // Manejar errores al cargar comentarios
            e.printStackTrace();
        });
    }

    private void enviarComentario() {
        String nuevoComentario = editComentario.getText().toString().trim();
        if (!nuevoComentario.isEmpty()) {
            DocumentReference docRef = db.collection(amigoUid)
                    .document("plantas")
                    .collection("Fotos_publica")
                    .document(documentId);

            docRef.update("comentarios", FieldValue.arrayUnion(nuevoComentario))
                    .addOnSuccessListener(aVoid -> {
                        editComentario.setText(""); // Limpiar campo de texto
                        cargarComentarios(); // Recargar comentarios
                    })
                    .addOnFailureListener(e -> {
                        // Manejar errores al enviar comentario
                        e.printStackTrace();
                    });
        }
    }
}
