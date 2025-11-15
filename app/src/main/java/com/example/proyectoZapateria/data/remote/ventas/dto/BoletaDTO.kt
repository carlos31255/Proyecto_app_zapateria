package com.example.proyectoZapateria.data.remote.ventas.dto

data class BoletaDTO(
    val id: Int? = null,
    val clienteId: Int,
    val fechaVenta: String,
    val total: Int,
    val estado: String,
    val metodoPago: String,
    val observaciones: String? = null,
    val fechaCreacion: String? = null,
    val fechaActualizacion: String? = null,
    val detalles: List<DetalleBoletaDTO>? = null
)