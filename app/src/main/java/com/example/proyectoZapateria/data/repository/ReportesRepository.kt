package com.example.proyectoZapateria.data.repository

import com.example.proyectoZapateria.data.local.boletaventa.BoletaVentaDao
import com.example.proyectoZapateria.domain.model.DetalleVentaReporte
import com.example.proyectoZapateria.domain.model.FiltroReporte
import com.example.proyectoZapateria.domain.model.ReporteVentas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

class ReportesRepository @Inject constructor(
    private val boletaVentaDao: BoletaVentaDao
) {
    suspend fun generarReporte(filtro: FiltroReporte): Result<ReporteVentas> = withContext(Dispatchers.IO) {
        try {
            val (fechaInicio, fechaFin) = calcularRangoFechas(filtro)

            val boletas = boletaVentaDao.getBoletasByRangoFechas(fechaInicio, fechaFin)

            val ventasRealizadas = boletas.filter { it.estado != "CANCELADA" }
            val ventasCanceladas = boletas.filter { it.estado == "CANCELADA" }

            val ingresosTotal = ventasRealizadas.sumOf { it.monto_total }

            // Si el filtro es por mes, incluir detalles
            val detallesVentas = if (filtro.mes != null) {
                boletas.map { boleta ->
                    DetalleVentaReporte(
                        numeroBoleta = boleta.numero_boleta,
                        fecha = boleta.fecha,
                        nombreCliente = "${boleta.nombre_cliente} ${boleta.apellido_cliente}",
                        montoTotal = boleta.monto_total,
                        estado = if (boleta.estado == "CANCELADA") "cancelada" else "realizada"
                    )
                }
            } else {
                emptyList()
            }

            Result.success(
                ReporteVentas(
                    numeroVentasRealizadas = ventasRealizadas.size,
                    numeroVentasCanceladas = ventasCanceladas.size,
                    ingresosTotal = ingresosTotal,
                    detallesVentas = detallesVentas
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calcularRangoFechas(filtro: FiltroReporte): Pair<Long, Long> {
        val calendar = Calendar.getInstance()

        if (filtro.mes != null) {
            // Filtro por mes específico
            calendar.set(Calendar.YEAR, filtro.anio)
            calendar.set(Calendar.MONTH, filtro.mes - 1) // Calendar.MONTH es 0-indexed
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val fechaInicio = calendar.timeInMillis

            // Último día del mes
            calendar.add(Calendar.MONTH, 1)
            val fechaFin = calendar.timeInMillis

            return Pair(fechaInicio, fechaFin)
        } else {
            // Filtro por año completo
            calendar.set(Calendar.YEAR, filtro.anio)
            calendar.set(Calendar.MONTH, 0) // Enero
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val fechaInicio = calendar.timeInMillis

            // Primer día del año siguiente
            calendar.set(Calendar.YEAR, filtro.anio + 1)
            val fechaFin = calendar.timeInMillis

            return Pair(fechaInicio, fechaFin)
        }
    }
}

