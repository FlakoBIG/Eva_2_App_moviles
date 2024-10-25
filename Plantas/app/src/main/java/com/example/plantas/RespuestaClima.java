package com.example.plantas;

import com.google.gson.annotations.SerializedName;

public class RespuestaClima {

    @SerializedName("main")
    private Main main;

    public Main getMain() {
        return main;
    }

    public class Main {
        @SerializedName("temp")
        private double temperatura;

        @SerializedName("humidity")
        private int humedad;

        public double getTemperatura() {
            return temperatura;
        }

        public int getHumedad() {
            return humedad;
        }
    }
}
