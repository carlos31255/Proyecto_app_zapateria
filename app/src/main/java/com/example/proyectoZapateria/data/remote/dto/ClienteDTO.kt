package com.example.proyectoZapateria.data.remote.dto

import com.google.gson.annotations.SerializedName


data class ClienteDTO(
    @SerializedName("idPersona")
    val idPersona: Int?,

    @SerializedName("categoria")
    val categoria: String?,

    @SerializedName("nombreCompleto")
    val nombreCompleto: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("telefono")
    val telefono: String?
)

