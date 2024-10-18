package com.example.plantas;

public class Planta {
    private String id; // Agrega un campo para el ID
    private String nombre;
    private String fecha_plantacion;
    private String foto_principal;

    public Planta() {
        // Constructor vacío requerido para Firestore
    }

    public Planta(String id, String nombre, String fecha_plantacion, String foto_principal) {
        this.id = id; // Inicializa el ID
        this.nombre = nombre;
        this.fecha_plantacion = fecha_plantacion;
        this.foto_principal = foto_principal;
    }

    public String getId() {
        return id; // Método para obtener el ID
    }

    public void setId(String id) {
        this.id = id; // Método para establecer el ID
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFecha_plantacion() {
        return fecha_plantacion;
    }

    public void setFecha_plantacion(String fecha_plantacion) {
        this.fecha_plantacion = fecha_plantacion;
    }

    public String getFoto_principal() {
        return foto_principal;
    }

    public void setFoto_principal(String foto_principal) {
        this.foto_principal = foto_principal;
    }
}
