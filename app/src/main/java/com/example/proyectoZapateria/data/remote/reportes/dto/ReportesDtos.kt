package com.example.proyectoZapateria.data.remote.reportes.dto

// Estadísticas generales simples
data class EstadisticasGeneralesDTO(
    val totalProductos: Int,
    val totalMovimientos: Int,
    val stockTotal: Int,
    val productosConStockBajo: Int,
    val productosSinStock: Int,
    val promedioStockPorProducto: Double
)

// Item para lista de stock bajo
data class StockBajoItemDTO(
    val id: Long,
    val productoId: Long,
    val nombre: String,
    val talla: String,
    val cantidadActual: Int,
    val stockMinimo: Int,
    val diferencia: Int
)

// Estadísticas de movimientos
data class MovimientosEstadisticasDTO(
    val totalMovimientos: Long,
    val movimientosEntrada: Long,
    val movimientosSalida: Long,
    val cantidadTotalEntradas: Int,
    val cantidadTotalSalidas: Int,
    val saldoMovimientos: Int
)

// Estadísticas por producto
data class ProductoEstadisticasDTO(
    val productoId: Long,
    val nombreProducto: String,
    val totalTallas: Int,
    val stockTotal: Int,
    val detallePorTalla: List<DetalleTallaDTO>,
    val totalMovimientos: Long
)

data class DetalleTallaDTO(
    val talla: String,
    val cantidad: Int,
    val stockMinimo: Int,
    val alertaStockBajo: Boolean
)

// DTO para top productos por stock
data class TopProductoDTO(
    val productoId: Long,
    val nombre: String,
    val talla: String,
    val cantidad: Int
)

