package com.example.pizarra_tactica

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    // Obtener todos los equipos para la pantalla inicial
    @GET("equipos")
    suspend fun obtenerEquipos(): List<EquipoRemote>

    // Obtener los jugadores de un equipo específico (ej: C1)
    @GET("jugadores/{eq_id}")
    suspend fun obtenerJugadores(@Path("eq_id") equipoId: String): List<JugadorRemote>

    // Guardar o actualizar un jugador en la nube
    @POST("jugador")
    suspend fun guardarJugador(@Body jugador: JugadorRemote): Response<Unit>
}