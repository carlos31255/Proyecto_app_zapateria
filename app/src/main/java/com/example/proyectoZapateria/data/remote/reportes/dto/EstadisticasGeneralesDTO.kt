package com.example.proyectoZapateria.data.remote.reportes.dto

data class EstadisticasGeneralesDTO(
    val totalProductos: Long,
    val totalMovimientos: Long,
    val stockTotal: Int,
    val productosStockBajo: Long,
    val productosSinStock: Long,
    val promedioStock: Double
)

