package com.example.pizarra_tactica

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.DELETE

// suspend indica que usa corrutinas, es decir, mientras espera la respuesta del servidor
// no se bloquea el hilo principal
interface ApiService {
    @GET("equipos")
    suspend fun obtenerEquipos(@Query("user_id") userId: String): List<EquipoRemote>


    @GET("jugadores/{eq_id}")
    suspend fun obtenerJugadores(@Path("eq_id") equipoId: String): List<JugadorRemote>


    @GET("estrategias/{eq_id}")
    suspend fun obtenerEstrategias(@Path("eq_id") eqId: String): List<EstrategiaRemote>

    @POST("jugador")
    suspend fun guardarJugador(@Body jugador: JugadorRemote): Response<Unit>

    @POST("estrategia")
    suspend fun guardarEstrategia(@Body estrategia: EstrategiaRemote): Response<Unit>

    // NUEVO: Para actualizar el nombre y escudo del equipo en la nube
    @POST("equipo")
    suspend fun guardarEquipo(@Body equipo: EquipoRemote): Response<Unit>

    @DELETE("jugador/{eq_id}/{dorsal}")
    suspend fun eliminarJugador(
        @Path("eq_id") eqId: String,
        @Path("dorsal") dorsal: Int
    ): Response<Unit>

}