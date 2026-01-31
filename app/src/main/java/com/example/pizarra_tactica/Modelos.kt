package com.example.pizarra_tactica

import com.google.gson.annotations.SerializedName

data class JugadorRemote(
    val equipo_id: String,
    val dorsal: Int,
    val nombre: String,
    val posicion: String,
    val nota: String,
    val old_dorsal: Int? = null // <-- AÑADIR ESTO
)
data class EquipoRemote(
    val id: String,
    val nombre: String,
    val imageUri: String, // Coincide con datos.get('imageUri') en tu Python
    val user_id: String   // El UID de Firebase
)