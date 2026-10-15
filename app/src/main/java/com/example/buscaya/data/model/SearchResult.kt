package com.example.buscaya.data.model

import com.google.gson.annotations.SerializedName

data class SearchResult(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("nombre")
    val nombre: String? = null,
    @SerializedName("ubicacion")
    val direccion: String? = null,
    @SerializedName("tipo")
    val tipo: String? = null,
    @SerializedName("imagen")
    val imagen: String? = null,
    @SerializedName("telefono")
    val telefono: String? = null,
    @SerializedName("descripcion")
    val descripcion: String? = null,
    @SerializedName("precio")
    val precio: String? = null,
    @SerializedName("negocio_nombre")
    val negocioNombre: String? = null,
    @SerializedName("email_contacto")
    val emailContacto: String? = null,
    @SerializedName("rating")
    val rating: Double? = null
)
