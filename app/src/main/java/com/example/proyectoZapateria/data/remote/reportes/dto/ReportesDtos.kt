package com.example.proyectoZapateria.data.remote.reportes.dto

// Estadísticas generales simples
data class EstadisticasGeneralesDTO(
    val totalProductos: Long,
    val totalMovimientos: Long,
    val stockTotal: Int,
    val productosStockBajo: Long,
    val productosSinStock: Long,
    val promedioStock: Double
)

// Item para lista de stock bajo (debe coincidir con ProductoResumenDTO del backend)
data class StockBajoItemDTO(
    val id: Long?,
    val productoId: Long?,
    val nombre: String?,
    val talla: String?,
    val cantidad: Int,
    val stockMinimo: Int,
    val faltante: Int
)

// Alias para compatibilidad con el backend
typealias ProductoResumenDTO = StockBajoItemDTO

// Estadísticas de movimientos
data class MovimientosEstadisticasDTO(
    val totalMovimientos: Long,
    val movimientosEntrada: Long,
    val movimientosSalida: Long,
    val totalEntradas: Int,
    val totalSalidas: Int,
    val saldoNeto: Int
)

// Estadísticas por producto
data class ProductoEstadisticasDTO(
    val productoId: Long,
    val nombreProducto: String,
    val totalTallas: Int,
    val stockTotal: Int,
    val detalleTallas: List<TallaDetalleDTO>,
    val totalMovimientos: Long
)

data class TallaDetalleDTO(
    val talla: String?,
    val cantidad: Int,
    val stockMinimo: Int,
    val alerta: Boolean
)

// DTO para top productos por stock
data class TopProductoDTO(
    val productoId: Long,
    val nombre: String?,
    val talla: String?,
    val cantidad: Int
)

// Alias para compatibilidad con el backend
typealias TopStockDTO = TopProductoDTO

// ==========================================
// DTOs para Reportes de Ventas
// ==========================================

data class ReporteVentasDTO(
    val numeroVentasRealizadas: Int,
    val numeroVentasCanceladas: Int,
    val ingresosTotal: Int,
    val detallesVentas: List<DetalleVentaDTO> = emptyList()
)

data class DetalleVentaDTO(
    val numeroBoleta: String,
    val fecha: Long,
    val nombreCliente: String,
    val montoTotal: Int,
    val estado: String
)

data class FiltroReporteRequest(
    val anio: Int,
    val mes: Int?
)
