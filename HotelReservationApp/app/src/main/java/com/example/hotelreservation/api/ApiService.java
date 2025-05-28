package com.example.hotelreservation.api;


import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("usuario/inicioSesion")//echa
    Call<JsonObject> login(@Body Map<String, String> credentials);
    
    @POST("usuario/registro")//echa
    Call<JsonObject> registerUser(@Body Map<String, String> userData);
    
    @POST("usuario/registro/hotelmanager")
    Call<JsonObject> registerHotelManager(@Body Map<String, String> userData);
    
    @POST("usuario/registro/admin")
    Call<JsonObject> registerAdmin(@Body Map<String, String> userData);

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

    @GET("api/private/reserva/ListaReservasPorHotel/{hotelId}")
    Call<ResponseBody> listarReservasPorHotel(
        @Header("Authorization") String token,
        @Path("hotelId") long hotelId
    );

    @DELETE("api/private/reserva/cancelarReserva/{reservaId}")
    Call<JsonObject> borrarReserva(
        @Header("Authorization") String token,
        @Path("reservaId") long reservaId
    );

    @POST("api/private/hotel/crearHabitacion")
    Call<JsonObject> crearHabitacion(
        @Header("Authorization") String token,
        @Body Map<String, String> habitacionData
    );

    @DELETE("api/private/hotel/eliminarHabitacion")
    Call<JsonObject> eliminarHabitacion(
        @Header("Authorization") String token,
        @Body Map<String, String> habitacionData
    );

    @POST("api/private/hotel/crearHotel")
    Call<ResponseBody> createHotel(@Header("Authorization") String token, @Body RequestBody hotelData); // hotelData should only include name and address

    @PUT("api/private/hotel/actualizarHotel/{id}")
    Call<ResponseBody> updateHotel(@Header("Authorization") String token, @Path("id") int hotelId, @Body RequestBody hotelData); // hotelData should only include name and address

    @DELETE("api/private/hotel/eliminarHotel")
    Call<ResponseBody> deleteHotel(@Header("Authorization") String token, @Body Map<String, Integer> hotelData);
}