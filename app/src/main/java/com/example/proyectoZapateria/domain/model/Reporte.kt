package com.example.proyectoZapateria.domain.model

data class ReporteVentas(
    val numeroVentasRealizadas: Int,
    val numeroVentasCanceladas: Int,
    val ingresosTotal: Int,
    val detallesVentas: List<DetalleVentaReporte> = emptyList()
)

data class DetalleVentaReporte(
    val numeroBoleta: String,
    val fecha: Long,
    val nombreCliente: String,
    val montoTotal: Int,
    val estado: String // "realizada" o "cancelada"
)

data class FiltroReporte(
    val mes: Int? = null, // 1-12
    val anio: Int
)

