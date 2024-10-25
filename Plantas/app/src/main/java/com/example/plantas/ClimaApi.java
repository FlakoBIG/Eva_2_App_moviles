package com.example.plantas;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class ClimaApi {
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String API_KEY = "4660ca2db86863b9d3c403a228826bec";
    private RequestQueue requestQueue;

    public ClimaApi(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    public void obtenerClima(String ciudad, final ClimaListener listener) {
        String url = API_URL + "?q=" + ciudad + "&units=metric&lang=es&appid=" + API_KEY;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            double temperatura = response.getJSONObject("main").getDouble("temp");
                            int humedad = response.getJSONObject("main").getInt("humidity");

                            listener.onClimaRecibido(temperatura, humedad);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            listener.onError("Error al analizar los datos del clima");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        listener.onError("No se pudo obtener el clima");
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
    }

    public interface ClimaListener {
        void onClimaRecibido(double temperatura, int humedad);
        void onError(String mensaje);
    }
}
