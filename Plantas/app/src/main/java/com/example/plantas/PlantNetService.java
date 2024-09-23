package com.example.plantas;
import com.example.plantas.PlantIdentificationResponse;


import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface PlantNetService {

    @Multipart
    @POST("/v2/identify/all")
    Call<PlantIdentificationResponse> identifyPlant(
            @Part("2b10U0eWCgPpoH8ZA2sjEc2") RequestBody apiKey,
            @Part MultipartBody.Part image
    );
}
