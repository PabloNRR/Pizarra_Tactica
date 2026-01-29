package com.example.pizarra_tactica

import com.google.gson.annotations.SerializedName

data class JugadorRemote(
    @SerializedName("equipo_id") val equipoId: String,
    val dorsal: Int,
    val nombre: String,
    val posicion: String,
    val nota: String
)

data class EquipoRemote(
    val id: String,
    val nombre: String,
    @SerializedName("image_uri") val imageUri: String
)