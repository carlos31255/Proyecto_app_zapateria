package com.example.proyectoZapateria.data.remote.usuario.dto

import com.google.gson.annotations.SerializedName


data class PersonaDTO(
    @SerializedName("idPersona")
    val idPersona: Int?,

    @SerializedName("nombre")
    val nombre: String?,

    @SerializedName("apellido")
    val apellido: String?,

    @SerializedName("rut")
    val rut: String?,

    @SerializedName("telefono")
    val telefono: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("idComuna")
    val idComuna: Int?,

    @SerializedName("calle")
    val calle: String?,

    @SerializedName("numeroPuerta")
    val numeroPuerta: String?,

    @SerializedName("username")
    val username: String?,

    @SerializedName("fechaRegistro")
    val fechaRegistro: Long?, //Lo dejamos en long, porque reprsenta fecha en milisegundos

    @SerializedName("estado")
    val estado: String?
)

