package com.example.proyectoZapateria.data.local.detalleboleta

/**
 * Representa un producto mapeado desde los detalles de boleta (forma simple para UI).
 */
data class ProductoDetalle(
    val nombreZapato: String = "",
    val talla: String = "",
    val cantidad: Int = 0,
    val marca: String = ""
)

