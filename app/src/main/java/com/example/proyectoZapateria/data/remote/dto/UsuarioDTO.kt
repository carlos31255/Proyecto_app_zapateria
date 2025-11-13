package com.example.proyectoZapateria.data.remote.dto

import com.google.gson.annotations.SerializedName


data class UsuarioDTO(
    @SerializedName("idPersona")
    val idPersona: Int?,

    @SerializedName("idRol")
    val idRol: Int?,

    @SerializedName("nombreCompleto")
    val nombreCompleto: String?,

    @SerializedName("username")
    val username: String?,

    @SerializedName("nombreRol")
    val nombreRol: String?
)

