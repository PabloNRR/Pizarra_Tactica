package com.example.pizarra_tactica

import com.google.gson.annotations.SerializedName

data class JugadorRemote(
    @SerializedName("equipo_id") val equipo_id: String,
    @SerializedName("dorsal") val dorsal: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("posicion") val posicion: String,
    @SerializedName("nota") val nota: String,
    @SerializedName("old_dorsal") val old_dorsal: Int? = null // Aseguramos el nombre exacto
)

data class EquipoRemote(
    @SerializedName("id") val id: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("imageUri") val imageUri: String,
    @SerializedName("user_id") val user_id: String
)