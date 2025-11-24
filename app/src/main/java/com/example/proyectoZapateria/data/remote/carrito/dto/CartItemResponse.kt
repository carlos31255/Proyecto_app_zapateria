package com.example.proyectoZapateria.data.remote.carrito.dto

data class CartItemResponse(
    val id: Long?,
    val clienteId: Long,
    val modeloId: Long,
    val talla: String,
    val cantidad: Int,
    val precioUnitario: Int,
    val nombreProducto: String?,
    val fechaCreacion: Long?,
    val fechaActualizacion: Long?
)

