package com.example.proyectoZapateria.data.remote.reportes.dto

data class StockBajoItemDTO(
    val id: Long?,
    val productoId: Long?,
    val nombre: String?,
    val talla: String?,
    val cantidad: Int,
    val stockMinimo: Int,
    val faltante: Int
)

