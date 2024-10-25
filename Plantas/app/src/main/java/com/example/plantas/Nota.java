package com.example.plantas;

public class Nota {
    private String texto;

    public Nota() {
        // Constructor vacío requerido para Firestore
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }
}
