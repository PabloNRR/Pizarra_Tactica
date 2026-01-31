package com.example.pizarra_tactica

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("equipos")
    suspend fun obtenerEquipos(@Query("user_id") userId: String): List<EquipoRemote>


    @GET("jugadores/{eq_id}")
    suspend fun obtenerJugadores(@Path("eq_id") equipoId: String): List<JugadorRemote>

    @POST("jugador")
    suspend fun guardarJugador(@Body jugador: JugadorRemote): Response<Unit>

    // NUEVO: Para actualizar el nombre y escudo del equipo en la nube
    @POST("equipo")
    suspend fun guardarEquipo(@Body equipo: EquipoRemote): Response<Unit>
}