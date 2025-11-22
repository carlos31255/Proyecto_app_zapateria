package com.example.proyectoZapateria.data.remote.entregas.dto

data class EntregaDTO(
    val idEntrega: Long? = null,
    val idBoleta: Long,
    val idTransportista: Long?,
    val estadoEntrega: String,
    val fechaAsignacion: String,
    val fechaEntrega: String?,
    val observacion: String?,
    val numeroBoleta: String? = null,
    val nombreCliente: String? = null,
    val direccionEntrega: String? = null
)
