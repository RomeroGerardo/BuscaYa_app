package com.example.buscaya.data.model

import com.google.gson.annotations.SerializedName

data class Farmacia(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("nombre")
    val nombre: String? = null,
    @SerializedName("direccion")
    val direccion: String? = null,
    @SerializedName("telefono")
    val telefono: String? = null,
    @SerializedName("horario")
    val horario: String? = null,
    @SerializedName("latitude")
    val latitude: Double? = null,
    @SerializedName("longitude")
    val longitude: Double? = null,
    @SerializedName("fecha_raw")
    val fechaRaw: String? = null,
    @SerializedName("fecha_iso")
    val fechaIso: String? = null,
    @SerializedName("dia_semana")
    val diaSemana: String? = null,
    @SerializedName("imagen")
    val imagen: String? = null
)
