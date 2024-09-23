package com.example.plantas;  // Añade esta línea al principio

public class PlantIdentificationResponse {
    private String bestMatch;

    public String getBestMatch() {
        return bestMatch;
    }

    public void setBestMatch(String bestMatch) {
        this.bestMatch = bestMatch;
    }
}
