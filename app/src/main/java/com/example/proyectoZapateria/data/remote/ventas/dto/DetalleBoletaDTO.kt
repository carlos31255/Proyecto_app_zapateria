package com.example.proyectoZapateria.data.remote.ventas.dto

data class DetalleBoletaDTO(
    val id: Long? = null,
    val boletaId: Long? = null,
    val inventarioId: Long,
    val nombreProducto: String? = null,
    val talla: String? = null,
    val cantidad: Int,
    val precioUnitario: Int,
    val subtotal: Int
)
