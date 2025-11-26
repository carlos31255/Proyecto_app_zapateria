package com.example.proyectoZapateria.data.remote.usuario.dto


data class TransportistaDTO(
    val idTransportista: Long? = null,
    val idPersona: Long,
    val patente: String? = null,
    val tipoVehiculo: String? = null,
    val licencia: String? = null,
    val activo: Boolean = true,
    val fechaRegistro: Long? = null
)