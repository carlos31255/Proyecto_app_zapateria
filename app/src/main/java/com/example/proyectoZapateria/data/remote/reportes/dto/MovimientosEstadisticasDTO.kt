package com.example.proyectoZapateria.data.remote.reportes.dto

data class MovimientosEstadisticasDTO(
    val totalMovimientos: Long,
    val movimientosEntrada: Long,
    val movimientosSalida: Long,
    val totalEntradas: Int,
    val totalSalidas: Int,
    val saldoNeto: Int
)

