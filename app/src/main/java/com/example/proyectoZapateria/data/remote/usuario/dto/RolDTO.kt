package com.example.proyectoZapateria.data.remote.usuario.dto

import com.google.gson.annotations.SerializedName


data class RolDTO(
    @SerializedName("idRol")
    val idRol: Long?,

    @SerializedName("nombreRol")
    val nombreRol: String?,

    @SerializedName("descripcion")
    val descripcion: String?
)
