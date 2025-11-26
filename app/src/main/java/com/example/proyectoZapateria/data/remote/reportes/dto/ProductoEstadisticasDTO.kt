package com.example.proyectoZapateria.data.remote.reportes.dto

data class ProductoEstadisticasDTO(
    val productoId: Long,
    val nombreProducto: String,
    val totalTallas: Int,
    val stockTotal: Int,
    val detalleTallas: List<TallaDetalleDTO>,
    val totalMovimientos: Long
)

