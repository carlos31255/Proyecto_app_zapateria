package com.example.proyectoZapateria.data.remote.entregas.dto

import kotlinx.serialization.Serializable

@Serializable
data class ActualizarEstadoRequest(
    val estadoEntrega: String,
    val observacion: String? = null
)

