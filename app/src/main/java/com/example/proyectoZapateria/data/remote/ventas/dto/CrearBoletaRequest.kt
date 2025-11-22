package com.example.proyectoZapateria.data.remote.ventas.dto

data class CrearBoletaRequest(
    val clienteId: Long,
    val metodoPago: String,
    val observaciones: String? = null,
    val detalles: List<DetalleBoletaDTO>
)
