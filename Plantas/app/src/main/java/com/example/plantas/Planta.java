package com.example.plantas;

public class Planta {
    private String nombre;
    private String fecha_plantacion;
    private String foto_principal;

    // Constructor vacío (Firebase requiere uno vacío)
    public Planta() {}

    public Planta(String nombre, String fecha_plantacion, String foto_principal) {
        this.nombre = nombre;
        this.fecha_plantacion = fecha_plantacion;
        this.foto_principal = foto_principal;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFechaPlantacion() {
        return fecha_plantacion;
    }

    public void setFechaPlantacion(String fecha_plantacion) {
        this.fecha_plantacion = fecha_plantacion;
    }

    public String getFotoPrincipal() {
        return foto_principal;
    }

    public void setFotoPrincipal(String foto_principal) {
        this.foto_principal = foto_principal;
    }
}
