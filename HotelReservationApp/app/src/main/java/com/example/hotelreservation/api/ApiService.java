package com.example.hotelreservation.api;


import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("usuario/inicioSesion")//echa
    Call<JsonObject> login(@Body Map<String, String> credentials);
    
    @POST("usuario/registro")//echa
    Call<JsonObject> register(@Body Map<String, String> userData);

    @GET("api/private/hotel/listarHoteles")//echa
Call<List<String>> listarHoteles(@Header("Authorization") String token);

@GET("api/private/hotel/listarHabitaciones")//echa
Call<List<Map<String, Object>>> listarHabitaciones(
    @Header("Authorization") String token,
    @Query("hotel") String hotel
);

@POST("api/private/reserva/crearReserva")//echa
Call<Map<String, Object>> crearReserva(
    @Header("Authorization") String token,
    @Body Map<String, Object> reservaData
);

@GET("api/private/reserva/ListaReservas/{idUsuario}")//echa
Call<ResponseBody> listarReservas(
    @Header("Authorization") String token,
    @Path("idUsuario") long usuario
);

@POST("api/private/reserva/cancelarReserva")//falta
Call<JsonObject> borrarReserva(
    @Header("Authorization") String token,
    @Body Map<String, String> reservationData
);
}