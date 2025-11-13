package com.example.proyectoZapateria.data.remote.dto

import com.google.gson.annotations.SerializedName


data class RolDTO(
    @SerializedName("idRol")
    val idRol: Int?,

    @SerializedName("nombreRol")
    val nombreRol: String?,

    @SerializedName("descripcion")
    val descripcion: String?
)

