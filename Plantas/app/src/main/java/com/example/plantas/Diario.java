package com.example.plantas;

import java.util.ArrayList;
import java.util.List;

public class Diario {
    private List<String> notas;

    public Diario(String nota) {
        this.notas = new ArrayList<>();
        this.notas.add(nota);
    }

    public List<String> getNotas() {
        return notas;
    }

    public void setNotas(List<String> notas) {
        this.notas = notas;
    }
}
