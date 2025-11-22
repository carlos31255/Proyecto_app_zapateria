package com.example.proyectoZapateria.data.remote.usuario.dto

import com.google.gson.annotations.SerializedName


data class UsuarioDTO(
    @SerializedName("idPersona")
    val idPersona: Long?,

    @SerializedName("idRol")
    val idRol: Long?,

    @SerializedName("nombreCompleto")
    val nombreCompleto: String?,

    @SerializedName("username")
    val username: String?,

    @SerializedName("nombreRol")
    val nombreRol: String?,

    @SerializedName("activo")
    val activo: Boolean? = true
)
