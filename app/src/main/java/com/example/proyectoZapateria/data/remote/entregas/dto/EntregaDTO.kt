package com.example.proyectoZapateria.data.remote.entregas.dto

data class EntregaDTO(
    val idEntrega: Int? = null,
    val idBoleta: Int,
    val idTransportista: Int?,
    val estadoEntrega: String,
    val fechaAsignacion: String,
    val fechaEntrega: String?,
    val observacion: String?,
    val numeroBoleta: String? = null,
    val nombreCliente: String? = null,
    val direccionEntrega: String? = null
)

