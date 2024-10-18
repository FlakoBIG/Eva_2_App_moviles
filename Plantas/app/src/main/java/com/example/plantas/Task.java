package com.example.plantas;

public class Task {
    private String id;
    private String nombre;
    private String regar; // Nuevo campo para indicar si es regar

    // Constructor para tareas
    public Task(String id, String nombre, String regar) {
        this.id = id;
        this.nombre = nombre;
        this.regar = regar; // Inicializa el nuevo campo
    }

    // Métodos getter
    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRegar() {
        return regar; // Método getter para el campo "regar"
    }
}
