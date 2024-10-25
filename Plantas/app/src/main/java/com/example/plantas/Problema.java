package com.example.plantas;

public class Problema {
    private String descripcion;

    public Problema() {
        // Constructor vacío requerido para Firestore
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
