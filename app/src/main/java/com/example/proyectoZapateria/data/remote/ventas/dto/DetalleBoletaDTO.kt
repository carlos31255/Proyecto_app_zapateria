package com.example.proyectoZapateria.data.remote.ventas.dto

data class DetalleBoletaDTO(
    val id: Int? = null,
    val boletaId: Int? = null,
    val inventarioId: Int,
    val nombreProducto: String? = null,
    val talla: String? = null,
    val cantidad: Int,
    val precioUnitario: Int,
    val subtotal: Int
)

