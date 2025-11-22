package com.example.proyectoZapateria.data.remote.ventas.dto

data class BoletaDTO(
    val id: Long? = null,
    val clienteId: Long,
    val fechaVenta: String,
    val total: Int,
    val estado: String,
    val metodoPago: String,
    val observaciones: String? = null,
    val fechaCreacion: String? = null,
    val fechaActualizacion: String? = null,
    val detalles: List<DetalleBoletaDTO>? = null
)